# 📋 DELEGACIÓN CORRECTIVA PM-IA → ARQUITECTO LÍDER
# Iteración: 84-DEV-LANE-ROLE-FIX (Corrección de Defectos Post-Auditoría)
**Fecha de Emisión:** 2026-07-14 | **Prioridad:** CRÍTICA | **Origen:** Auditoría Forense PM-IA

---

> [!CAUTION]
> ## 🚨 CONTEXTO DE URGENCIA
> La auditoría forense del PM-IA detectó **14 defectos** en la iteración 84-DEV-LANE-ROLE. De estos, **1 es CRÍTICO** (bug de runtime que impide el funcionamiento de toda la feature), **3 son ALTOS** (violan contratos API y arquitectura hexagonal), y **5 son MEDIOS** (código muerto, anti-patrones, UX). Esta iteración correctiva DEBE completarse ANTES de las pruebas UAT del humano.
>
> **POLÍTICAS VIGENTES:** Zero Hard-Code, Zero Mocks, Zero Alucinaciones, Compilación Obligatoria, Contención de Alcance. Aplican con la misma severidad que en la iteración original.

---

## 1. INVENTARIO COMPLETO DE DEFECTOS A CORREGIR

### 🔴 PRIORIDAD 1 — BLOQUEAN UAT (Corregir PRIMERO)

#### D-01: `processDesignId = null` al sincronizar lanes (CRÍTICO)
- **Archivo:** `backend/.../application/service/DesplegarDefinicionService.java` → Línea 93
- **Problema:** `bpmnLanePort.syncLanesFromDeployment(processId, null, parsedLanes)` — El segundo parámetro es `null` porque el servicio no tiene acceso al `BpmnProcessDesignEntity`. Cuando `BpmnLaneService.syncLanesFromDeployment()` intenta crear nuevas lanes con `processDesign = null`, JPA lanza `ConstraintViolationException` porque `@JoinColumn(nullable = false)`.
- **Corrección exacta:**
  1. En `DesplegarDefinicionService.java`, ANTES de la línea 93, resolver el `UUID processDesignId` consultando `BpmnProcessDesignEntity` por su `technicalId` (que es el `processId` string).
  2. Inyectar `BpmnProcessDesignJpaRepository` (o usar un puerto existente) para hacer: `UUID designId = processDesignRepository.findByTechnicalId(processId).map(BpmnProcessDesignEntity::getId).orElse(null);`
  3. Pasar `designId` en vez de `null`: `bpmnLanePort.syncLanesFromDeployment(processId, designId, parsedLanes);`
  4. En `BpmnLaneService.syncLanesFromDeployment()` (líneas 56-65), agregar un guard: si `processDesignId == null`, hacer log.warn y return sin insertar (no lanzar excepción para no romper el deploy).
- **Criterio de salida:** Desplegar un BPMN con 2 lanes → `SELECT * FROM ibpms_bpmn_lane` debe retornar 2 registros con `process_design_id` NOT NULL.

#### D-03: Sin validación 400/404 en `replaceAssignmentsForRole` (ALTO)
- **Archivo:** `backend/.../application/service/bpmn/BpmnLaneService.java` → Líneas 105-125
- **Problema:** El método hace INSERT sin verificar que `roleId` exista en `ibpms_security_role` ni que cada `laneId` exista en `ibpms_bpmn_lane`. El contrato API promete `400 Bad Request` y `404 Not Found` pero el código no los implementa.
- **Corrección exacta:**
  1. Al inicio del método, verificar: `if (!roleRepository.existsById(roleId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found: " + roleId);`
  2. Para cada `req` en `assignments`, verificar: `if (!bpmnLaneRepository.existsById(req.laneId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lane not found: " + req.laneId());`
  3. Inyectar `RoleJpaRepository` (o equivalente) si no está ya inyectado.
- **Criterio de salida:** `PUT /api/v1/admin/roles/{UUID-INEXISTENTE}/lane-assignments` retorna `404`. `PUT` con `laneId` inexistente retorna `400`.

---

### 🟡 PRIORIDAD 2 — Deuda técnica (Corregir en la misma iteración)

#### D-02: `assigned_by` hard-coded a `"system"` (ALTO)
- **Archivo:** `backend/.../application/service/bpmn/BpmnLaneService.java` → Línea 120
- **Corrección:** Reemplazar `entity.setAssignedBy("system")` con:
  ```java
  String currentUser = SecurityContextHolder.getContext().getAuthentication() != null
      ? SecurityContextHolder.getContext().getAuthentication().getName()
      : "system";
  entity.setAssignedBy(currentUser);
  ```
- **Criterio de salida:** Crear una asignación lane-role → `SELECT assigned_by FROM ibpms_lane_role_assignment` muestra el username del JWT, no `"system"`.

#### D-04: Violación Hexagonal — Service importa infrastructure (ALTO)
- **Archivo:** `backend/.../application/service/bpmn/BpmnLaneService.java` → Líneas 8-12
- **Corrección:** Esta corrección requiere EVALUACIÓN del Arquitecto. Hay 2 opciones:
  - **Opción A (pragmática):** Aceptar como deuda técnica documentada — el patrón ya existe en otros services del proyecto (verificar si `RbacAuthorizationService` también importa repos directamente). Si es un patrón establecido en el proyecto, documentar como "excepción consistente".
  - **Opción B (purista):** Crear un adaptador en `infrastructure/` que implemente `BpmnLanePort`, y mover la lógica de acceso a datos allí. El service en `application/` solo llama al port.
- **Decisión:** El Arquitecto Líder DEBE verificar qué patrón se usa en el resto del proyecto y aplicar el MISMO. NO mezclar patrones.

#### D-05: 2 métodos fantasma — `assignRoleToLane()` y `removeRoleFromLane()` (MEDIO)
- **Archivo:** `backend/.../application/service/bpmn/BpmnLaneService.java` → Líneas 80-90
- **Corrección:** Eliminar ambos métodos del servicio Y de la interfaz `BpmnLanePort.java` (líneas 13-14). No se usan en ningún controller. El controller usa `replaceAssignmentsForRole()` que es la estrategia DELETE+INSERT.
- **Criterio de salida:** `BpmnLanePort.java` tiene 3 métodos (no 5). `BpmnLaneService.java` no tiene cuerpos vacíos.

#### D-06: `replaceAssignmentsForRole()` no declarada en Port (MEDIO)
- **Archivo:** `backend/.../application/port/out/BpmnLanePort.java` + `backend/.../infrastructure/web/security/LaneAdminController.java`
- **Corrección:**
  1. Agregar `void replaceAssignmentsForRole(UUID roleId, List<LaneRoleAssignmentRequest> assignments);` a `BpmnLanePort.java`
  2. En `LaneAdminController.java`, eliminar la inyección de `BpmnLaneService` concreto (línea 19). Usar SOLO `BpmnLanePort`.
  3. Cambiar línea 42: `bpmnLaneService.replaceAssignmentsForRole(...)` → `bpmnLanePort.replaceAssignmentsForRole(...)`
- **Criterio de salida:** Controller solo inyecta `BpmnLanePort`, no `BpmnLaneService`.

#### D-07: Anti-patrón proxy entity (MEDIO)
- **Archivo:** `backend/.../application/service/bpmn/BpmnLaneService.java` → Líneas 109-115
- **Corrección:** Reemplazar `new RoleEntity(); role.setId(roleId)` con:
  ```java
  RoleEntity role = entityManager.getReference(RoleEntity.class, roleId);
  ```
  Y lo mismo para `BpmnLaneEntity`. Inyectar `EntityManager` en el constructor del servicio.
- **Criterio de salida:** No hay `new RoleEntity()` ni `new BpmnLaneEntity()` con solo ID seteado.

---

### 🟢 PRIORIDAD 3 — Mejoras menores y gobernanza

#### D-08: Tipos TS duales en `api-schema.d.ts` (MEDIO — Frontend)
- **Archivo:** `frontend/src/types/api-schema.d.ts` → Líneas 3013-3029 (OpenAPI auto) + 18543-18565 (manual)
- **Corrección:** Eliminar las interfaces manuales al final del archivo (L18543-18565) y usar SOLO las definiciones dentro de `components.schemas`. O bien, si el frontend necesita campos `required`, actualizar la definición OpenAPI para que sean `required`.
- **Criterio de salida:** Solo UNA definición de cada tipo en el archivo.

#### D-09: Sin toast de error para API de lanes (MEDIO — Frontend)
- **Archivo:** `frontend/src/views/admin/Security/IdentityGovernance.vue` → Líneas 1162, 1191, 1269
- **Corrección:** En cada bloque `catch`, agregar: `showToast('Error al cargar/guardar lanes: ' + error.message, 'error');` (la función `showToast` ya existe en el componente).
- **Criterio de salida:** Si la API de lanes falla, el usuario ve un toast rojo con el mensaje de error.

#### D-13: Status API_CONTRACTS no actualizado (BAJO — Gobernanza)
- **Archivo:** `docs/sprints/gobernanza_pm/API_CONTRACTS.md` → Sección 5.9
- **Corrección:** Cambiar `Estado: ❌ Missing` → `Estado: ✅ Implemented` en los 3 endpoints de Lane Management.

#### D-14: Sin entrada Frontend en CHANGELOG (BAJO — Gobernanza)
- **Archivo:** `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md`
- **Corrección:** Agregar entrada: `"Diseñador de Flujos ahora permite definir quién trabaja en cada Carril"` — describir el panel de propiedades de Lane y la integración RBAC.

#### Coverage Matrix + Cierre de Iteración (Gobernanza)
- **Archivo:** `.agentic-sync/coverage_matrix.md` — Agregar entradas para la iteración 84-DEV
- **Archivo:** `.agentic-sync/cierre_iteracion_84-DEV-LANE-ROLE.md` — Crear acta de cierre una vez las correcciones estén hechas

---

## 2. MICRO-SPRINTS CORRECTIVOS

### 📦 MC-1: Backend — Corrección de Defectos Críticos y Altos
**Agente:** Backend | **Duración:** ≤3 horas | **Dependencias:** Ninguna

**Alcance estricto — SOLO estos archivos:**
| Acción | Archivo |
|--------|---------|
| MODIFICAR | `DesplegarDefinicionService.java` (D-01) |
| MODIFICAR | `BpmnLaneService.java` (D-01, D-02, D-03, D-05, D-06, D-07) |
| MODIFICAR | `BpmnLanePort.java` (D-05 eliminar fantasmas, D-06 agregar replaceAssignments) |
| MODIFICAR | `LaneAdminController.java` (D-06 eliminar inyección concreta) |

**Criterio de salida global:**
1. `mvn clean compile` exitoso
2. Spring Boot arranca sin errores en puerto 8080
3. Desplegar BPMN con lanes → `ibpms_bpmn_lane` tiene registros con `process_design_id` NOT NULL
4. `PUT /roles/{roleId-inexistente}/lane-assignments` retorna 404
5. `assigned_by` muestra username real, no "system"
6. No hay métodos vacíos en `BpmnLaneService`
7. `LaneAdminController` solo inyecta `BpmnLanePort`

---

### 📦 MC-2: Frontend — Corrección de Defectos Medios
**Agente:** Frontend | **Duración:** ≤1 hora | **Dependencias:** Ninguna (paralelo con MC-1)

**Alcance estricto — SOLO estos archivos:**
| Acción | Archivo |
|--------|---------|
| MODIFICAR | `IdentityGovernance.vue` (D-09: agregar toasts de error) |
| MODIFICAR | `api-schema.d.ts` (D-08: eliminar tipos duplicados) |

**Criterio de salida:**
1. `npm run build` exitoso
2. Si la API de lanes falla, el usuario ve un toast rojo

---

### 📦 MC-3: Gobernanza — Actualización Documental
**Agente:** Arquitecto Líder (directamente) | **Duración:** ≤30 min | **Dependencia:** MC-1 y MC-2 completados

**Alcance:**
| Acción | Archivo |
|--------|---------|
| MODIFICAR | `API_CONTRACTS.md` (D-13) |
| MODIFICAR | `CHANGELOG_NO_TECNICO.md` (D-14) |
| MODIFICAR | `.agentic-sync/coverage_matrix.md` |
| CREAR | `.agentic-sync/cierre_iteracion_84-DEV-LANE-ROLE.md` |

---

## 3. SECUENCIA DE EJECUCIÓN

```
       MC-1 (Backend Fixes)  ──────────┐
                                        ├──→ MC-3 (Gobernanza) → UAT Humano
       MC-2 (Frontend Fixes) ──────────┘
       [PARALELO]
```

> [!IMPORTANT]
> MC-1 y MC-2 pueden ejecutarse en **PARALELO** porque tocan archivos diferentes (backend vs frontend). MC-3 se ejecuta después de ambos como cierre formal. Luego Harold ejecuta las pruebas UAT manuales.
