# 📋 DELEGACIÓN PM-IA → ARQUITECTO LÍDER
# Feature: Asignación de Actores/Roles en Lanes BPMN + Integración RBAC
**Fecha de Emisión:** 2026-07-08 | **Prioridad:** ALTA | **Cadenas Impactadas:** 1 (Security) + 2 (BPMN)

---

> [!CAUTION]
> ## 🚨 RECORDATORIO OBLIGATORIO — POLÍTICAS DE TOLERANCIA CERO
> 1. **CERO HARD-CODE:** Todo ID, ruta, nombre de tabla, endpoint DEBE ser dinámico o parametrizado. Si encuentras datos quemados, DETÉNTE y reporta.
> 2. **CERO ALUCINACIONES:** NO inventes endpoints, tablas, nombres de variables ni rutas que no estén en este documento o en el código existente. Si algo no está claro, PREGUNTA.
> 3. **ANCLAJE AL CÓDIGO REAL:** Antes de crear cualquier handoff, VERIFICA que los archivos y funciones referenciados EXISTEN usando `grep` o `view_file`. No confíes en tu memoria.
> 4. **CERO MOCKS:** Está PROHIBIDO usar datos falsos, mockAdapter, stubs o JSON estáticos. Todo debe funcionar contra la BD real PostgreSQL (Docker, puerto 5433).
> 5. **CONTENCIÓN DE ALCANCE:** Solo puedes modificar los archivos listados en la sección "Blast Radius". Cualquier archivo NO listado es INTOCABLE.
> 6. **COMPILACIÓN OBLIGATORIA:** Cada Micro-Sprint DEBE terminar con `mvn clean compile` (Backend) o `npm run build` (Frontend) exitoso. Sin evidencia de compilación = tarea NO entregada.

---

## 1. CONTEXTO Y DECISIONES CONFIRMADAS POR EL PO

### Requerimiento del Cliente
Habilitar la asignación de actores/participantes en los Lanes del diseñador BPMN (`/admin/modeler/bpmn`) y conectar estos lanes con los roles del módulo RBAC (`/admin/security/identity`) para que usuarios funcionales puedan ser asignados a tareas específicas de cada lane.

### Decisiones de Diseño (Confirmadas por Harold — PO)

| # | Decisión | Selección |
|---|----------|-----------|
| P1 | UI de actor en Lane | **Opción C:** Texto libre para nombre del Lane + Dropdown para vincular rol RBAC existente |
| P2 | Granularidad en RBAC | **Opción B:** Con I (Initiate) / E (Execute) por Lane, UX agradable y sencillo |
| P3 | Sistema B existente | **Migración:** Mantener lo funcional de `profile_bpmn_assignment`, agregar lo nuevo, omitir lo obsoleto |

---

## 2. ESTADO REAL DEL CÓDIGO (VERIFICADO — NO SUPONER)

### 2.1 Archivos Existentes Relevantes

| Archivo | Ruta Absoluta | Líneas | Propósito |
|---------|---------------|--------|-----------|
| BpmnDesigner.vue | `frontend/src/views/admin/Modeler/BpmnDesigner.vue` | 4,468 | Modeler monolítico — panel propiedades en líneas 504-775 |
| IdentityGovernance.vue | `frontend/src/views/admin/Security/IdentityGovernance.vue` | 1,470 | RBAC — modal edición rol en líneas 583-710 |
| rbacStore.js | `frontend/src/stores/rbacStore.js` | 347 | Store RBAC — `fetchSystemProcesses()` en línea 246 |
| DesplegarDefinicionService.java | `backend/.../application/service/bpmn/DesplegarDefinicionService.java` | ~100 | Deploy hook — `generarRolesDesdeLanes()` línea 44 |
| RoleAdminController.java | `backend/.../infrastructure/web/rest/admin/RoleAdminController.java` | ~100 | REST controller roles |
| RoleEntity.java | `backend/.../infrastructure/jpa/entity/security/RoleEntity.java` | ~60 | Entidad JPA — ya tiene `lane_id` y `process_definition_id` |
| ProcessPermissionEntity.java | `backend/.../infrastructure/jpa/entity/security/ProcessPermissionEntity.java` | ~30 | Permisos I/E a nivel proceso |
| ProfileBpmnAssignmentEntity.java | `backend/.../infrastructure/jpa/entity/ProfileBpmnAssignmentEntity.java` | ~30 | Lane binding (Sistema B — a migrar) |
| RbacPort.java | `backend/.../core/sac/port/RbacPort.java` | - | Puerto hexagonal con `bindLaneToProfile()` |
| RbacAuthorizationService.java | `backend/.../application/service/security/RbacAuthorizationService.java` | - | Implementa `RbacPort` |
| api-schema.d.ts | `frontend/src/types/api-schema.d.ts` | - | Tipos TS — ya tiene `laneId` y `bpmnLaneId` |

### 2.2 Funciones Clave Existentes (REUTILIZAR — NO REINVENTAR)

| Función | Ubicación | Qué Hace |
|---------|-----------|----------|
| `syncElementProperties(key, value)` | BpmnDesigner.vue:4244 | Escribe CUALQUIER propiedad al XML BPMN — **GENÉRICA, LISTA PARA USAR** |
| `selection.changed` handler | BpmnDesigner.vue:3046 | Captura tipo, ID y nombre del elemento seleccionado |
| `generarRolesDesdeLanes()` | DesplegarDefinicionService.java:44 | Parsea `<bpmn:lane>` del XML y crea perfiles |
| `bindLaneToProfile()` | RbacPort.java | Puerto hexagonal para vincular lanes a perfiles |
| `getPermittedBpmnLanesForGroups()` | RbacPort.java | Consulta lanes permitidos por grupo |
| `fetchSystemProcesses()` | rbacStore.js:246 | Llama `GET /api/v1/design/processes` |

---

## 3. BLAST RADIUS — ARCHIVOS AUTORIZADOS vs INTOCABLES

### ✅ AUTORIZADO MODIFICAR

| Archivo | Tipo de Cambio | Zona Exacta |
|---------|---------------|-------------|
| `BpmnDesigner.vue` | Agregar bloque `v-else-if` para `bpmn:Lane` y `bpmn:Participant` | Insertar DESPUÉS de línea 772 (antes del `v-else` genérico) |
| `IdentityGovernance.vue` | Extender sección "Definición BPMN" en el modal | Zona líneas 617-640 (tabla de Concesiones Zod) |
| `rbacStore.js` | Agregar funciones `fetchLanesByProcess()` y `saveLaneRoleAssignments()` | Agregar DESPUÉS de `fetchSystemProcesses()` (línea 253) |
| `DesplegarDefinicionService.java` | Extender `generarRolesDesdeLanes()` para INSERT en nueva tabla | Dentro de método existente, línea 44+ |
| `RoleAdminController.java` | Agregar 2 endpoints nuevos | Agregar DESPUÉS de los endpoints existentes |
| `RbacAuthorizationService.java` | Implementar nuevos métodos del puerto | Agregar métodos nuevos |
| `api-schema.d.ts` | Agregar tipos para Lane y LaneRoleAssignment | Agregar al final del archivo |
| `db.changelog-master.yaml` | Registrar nueva migración | Agregar include al final |

### ✅ AUTORIZADO CREAR (Archivos Nuevos)

| Archivo Nuevo | Ruta |
|---------------|------|
| `BpmnLaneEntity.java` | `backend/.../infrastructure/jpa/entity/bpmn/BpmnLaneEntity.java` |
| `LaneRoleAssignmentEntity.java` | `backend/.../infrastructure/jpa/entity/security/LaneRoleAssignmentEntity.java` |
| `BpmnLaneJpaRepository.java` | `backend/.../infrastructure/jpa/repository/BpmnLaneJpaRepository.java` |
| `LaneRoleAssignmentJpaRepository.java` | `backend/.../infrastructure/jpa/repository/LaneRoleAssignmentJpaRepository.java` |
| `BpmnLanePort.java` | `backend/.../core/project/port/BpmnLanePort.java` (o dominio apropiado) |
| `XXX-lane-role-assignment-tables.sql` | `backend/.../resources/db/changelog/changes/` |

### 🚫 INTOCABLE (PROHIBIDO MODIFICAR)

| Archivo/Módulo | Razón |
|----------------|-------|
| `Workdesk.vue` | No se altera el flujo de tareas |
| `Login.vue`, `AuthService`, JWT | Cero impacto en autenticación |
| `FormDesigner.vue`, `FormRenderer` | Cero impacto en formularios |
| `router/index.ts` | No se crean rutas nuevas |
| `docker-compose.yml` | No cambia infraestructura |
| Todas las migraciones Liquibase existentes (001-059) | Solo se AGREGA nueva migración |
| `ProcessRolesTable.vue` | Se mantiene como tabla de lectura de roles auto-generados |
| Tests E2E existentes | No se alteran journeys certificados |

---

## 4. MICRO-SPRINTS — INSTRUCCIONES ATÓMICAS

### 📦 Micro-Sprint 1: Infraestructura de Datos
**Agente:** Infra/BD | **Duración Estimada:** ≤2 horas | **Dependencias:** Ninguna

**Tarea Atómica:**
Crear una nueva migración Liquibase con el siguiente DDL exacto:

```sql
-- Migración: XXX-lane-role-assignment-tables.sql
-- US-005/US-036 Extension: Lane Actor Assignment + RBAC Integration

-- Tabla 1: Lanes como entidad de primer nivel
CREATE TABLE IF NOT EXISTS ibpms_bpmn_lane (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    process_design_id UUID NOT NULL,
    lane_xml_id       VARCHAR(150) NOT NULL,
    lane_name         VARCHAR(255) NOT NULL,
    actor_description VARCHAR(500),
    linked_role_id    UUID,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_lane_process FOREIGN KEY (process_design_id) 
        REFERENCES ibpms_bpmn_process_design(id) ON DELETE CASCADE,
    CONSTRAINT fk_lane_linked_role FOREIGN KEY (linked_role_id) 
        REFERENCES ibpms_security_role(id) ON DELETE SET NULL,
    CONSTRAINT uq_lane_per_process UNIQUE (process_design_id, lane_xml_id)
);

CREATE INDEX idx_bpmn_lane_process ON ibpms_bpmn_lane(process_design_id);

-- Tabla 2: Asignación Lane↔Rol (Many-to-Many con granularidad I/E)
CREATE TABLE IF NOT EXISTS ibpms_lane_role_assignment (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lane_id         UUID NOT NULL,
    role_id         UUID NOT NULL,
    can_initiate    BOOLEAN NOT NULL DEFAULT false,
    can_execute     BOOLEAN NOT NULL DEFAULT true,
    assigned_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    assigned_by     VARCHAR(255),
    CONSTRAINT fk_lra_lane FOREIGN KEY (lane_id) 
        REFERENCES ibpms_bpmn_lane(id) ON DELETE CASCADE,
    CONSTRAINT fk_lra_role FOREIGN KEY (role_id) 
        REFERENCES ibpms_security_role(id) ON DELETE CASCADE,
    CONSTRAINT uq_lane_role UNIQUE (lane_id, role_id)
);

CREATE INDEX idx_lra_lane ON ibpms_lane_role_assignment(lane_id);
CREATE INDEX idx_lra_role ON ibpms_lane_role_assignment(role_id);
```

**Criterio de Salida:**
1. `mvn clean compile` exitoso
2. Spring Boot arranca sin errores de Liquibase
3. Las 2 tablas existen en PostgreSQL (verificar con `\dt ibpms_bpmn_lane` y `\dt ibpms_lane_role_assignment`)

---

### 📦 Micro-Sprint 2: Backend — Entidades JPA y Repositorios
**Agente:** Backend | **Duración Estimada:** ≤2 horas | **Dependencia:** Micro-Sprint 1 ✅

**Tareas Atómicas:**

**T2.1:** Crear `BpmnLaneEntity.java` siguiendo el patrón exacto de las entidades existentes (ej: `ProcessPermissionEntity.java`):
- `@Table(name = "ibpms_bpmn_lane")`
- Campos: `id (UUID)`, `processDesign (ManyToOne → BpmnProcessDesignEntity)`, `laneXmlId (String)`, `laneName (String)`, `actorDescription (String)`, `linkedRole (ManyToOne → RoleEntity, nullable)`, `createdAt`, `updatedAt`

**T2.2:** Crear `LaneRoleAssignmentEntity.java`:
- `@Table(name = "ibpms_lane_role_assignment")`
- Campos: `id (UUID)`, `lane (ManyToOne → BpmnLaneEntity)`, `role (ManyToOne → RoleEntity)`, `canInitiate (Boolean)`, `canExecute (Boolean)`, `assignedAt`, `assignedBy`

**T2.3:** Crear repositorios JPA:
- `BpmnLaneJpaRepository extends JpaRepository<BpmnLaneEntity, UUID>` con query `findByProcessDesign_TechnicalId(String technicalId)`
- `LaneRoleAssignmentJpaRepository extends JpaRepository<LaneRoleAssignmentEntity, UUID>` con query `findByRole_Id(UUID roleId)` y `findByLane_Id(UUID laneId)`

**Criterio de Salida:** `mvn clean compile` exitoso. Cero warnings de JPA mapping.

---

### 📦 Micro-Sprint 3: Backend — Puerto Hexagonal, Servicio y Endpoints REST
**Agente:** Backend | **Duración Estimada:** ≤3 horas | **Dependencia:** Micro-Sprint 2 ✅

**Tareas Atómicas:**

**T3.1:** Crear/extender puerto hexagonal `BpmnLanePort.java` (interfaz):
```java
public interface BpmnLanePort {
    List<BpmnLaneDTO> getLanesByProcessKey(String processDefinitionKey);
    void syncLanesFromDeployment(String processKey, UUID processDesignId, List<LaneInfo> lanes);
    void assignRoleToLane(UUID laneId, UUID roleId, boolean canInitiate, boolean canExecute);
    void removeRoleFromLane(UUID laneId, UUID roleId);
    List<LaneRoleAssignmentDTO> getAssignmentsByRoleId(UUID roleId);
}
```

**T3.2:** Extender `DesplegarDefinicionService.generarRolesDesdeLanes()`:
- MANTENER la lógica existente de `ProfileBpmnAssignmentEntity` (retrocompatibilidad)
- AGREGAR: Después de crear perfiles, también hacer INSERT/UPSERT en `ibpms_bpmn_lane`
- Usar `UNIQUE(process_design_id, lane_xml_id)` para idempotencia (ON CONFLICT UPDATE name)
- Purgar lanes que ya no existan en el XML (zombies)

**T3.3:** Agregar 3 endpoints REST en `RoleAdminController.java` (o en un nuevo `LaneAdminController.java` si el Arquitecto lo prefiere):

| Método | Ruta | Body | Respuesta | Propósito |
|--------|------|------|-----------|-----------|
| `GET` | `/api/v1/admin/lanes?processKey={key}` | — | `List<BpmnLaneDTO>` | Listar lanes de un proceso |
| `GET` | `/api/v1/admin/roles/{roleId}/lane-assignments` | — | `List<LaneRoleAssignmentDTO>` | Obtener asignaciones lane↔rol |
| `PUT` | `/api/v1/admin/roles/{roleId}/lane-assignments` | `List<LaneRoleAssignmentRequest>` | `200 OK` | Guardar/actualizar asignaciones |

**T3.4:** DTOs necesarios:
```java
record BpmnLaneDTO(UUID id, String processKey, String laneXmlId, String laneName, 
                   String actorDescription, String linkedRoleName) {}

record LaneRoleAssignmentDTO(UUID laneId, String laneName, String processKey,
                              boolean canInitiate, boolean canExecute) {}

record LaneRoleAssignmentRequest(UUID laneId, boolean canInitiate, boolean canExecute) {}
```

**Criterio de Salida:**
1. `mvn clean compile` exitoso
2. Spring Boot arranca en puerto 8080
3. `GET /api/v1/admin/lanes?processKey=test` responde 200 (array vacío aceptable)
4. Swagger muestra los 3 endpoints nuevos

---

### 📦 Micro-Sprint 4: Frontend — Panel de Propiedades Lane (BPMN Modeler)
**Agente:** Frontend | **Duración Estimada:** ≤3 horas | **Dependencia:** Micro-Sprint 3 ✅

**Tarea Atómica:**

**T4.1:** En `BpmnDesigner.vue`, INSERTAR un nuevo bloque `v-else-if` **ANTES** de la línea 773 (el fallback genérico). Este bloque debe activarse cuando `selectedElement.type === 'bpmn:Lane' || selectedElement.type === 'bpmn:Participant'`.

El panel debe contener:
1. **Nombre del Lane** — Input text, vinculado a `selectedElement.name`. Al cambiar, llamar `syncElementProperties('name', newValue)` para actualizar el XML BPMN.
2. **Actor / Participante** — Input text descriptivo (ej: "Departamento de Contabilidad"). Guardar como extensión Camunda: `syncElementProperties('camunda:assignee', value)`.
3. **Rol Vinculado** — Dropdown (`<select>`) que carga los roles existentes del sistema via `rbacStore.roles`. Al seleccionar, guardar como extensión: `syncElementProperties('camunda:candidateGroups', selectedRoleName)`.
4. **Indicador visual** — Badge que muestra si el Lane tiene un rol vinculado o no.

**T4.2:** En `rbacStore.js`, verificar que `roles` ya está disponible (probablemente `fetchRoles()` ya existe). Si no, agregar un fetch para obtener roles del sistema.

**Reglas de UX:**
- El panel debe seguir el mismo estilo visual que los paneles de UserTask y ServiceTask (mismas clases CSS, mismo layout).
- El dropdown de roles debe tener un `placeholder` como "— Sin rol vinculado —".
- Usar las mismas convenciones de `data-testid` para QA.

**Criterio de Salida:**
1. `npm run build` exitoso
2. Al seleccionar un Lane en el canvas, el panel derecho muestra los 3 campos
3. Al escribir un nombre de Lane, se actualiza en el XML BPMN (verificar con Export/Canvas)
4. El dropdown carga roles reales del sistema (no mocks)

---

### 📦 Micro-Sprint 5: Frontend — Integración RBAC (Identity Governance)
**Agente:** Frontend | **Duración Estimada:** ≤4 horas | **Dependencia:** Micro-Sprint 3 ✅ + Micro-Sprint 4 ✅

**Tarea Atómica:**

**T5.1:** En `rbacStore.js`, agregar:
```javascript
// Después de fetchSystemProcesses() (línea 253)
async function fetchLanesByProcess(processKey) {
    const response = await apiClient.get(`/admin/lanes?processKey=${processKey}`);
    return response.data; // List<BpmnLaneDTO>
}

async function saveLaneRoleAssignments(roleId, assignments) {
    await apiClient.put(`/admin/roles/${roleId}/lane-assignments`, assignments);
}

async function fetchLaneAssignmentsByRole(roleId) {
    const response = await apiClient.get(`/admin/roles/${roleId}/lane-assignments`);
    return response.data; // List<LaneRoleAssignmentDTO>
}
```

**T5.2:** En `IdentityGovernance.vue`, extender la sección "Matriz de Concesiones Zod (CA-4)" (líneas 617-640):

**ANTES (actual):** Tabla plana con procesos, checkbox I/E por proceso.

**DESPUÉS (nuevo):** Tabla jerárquica expandible:
```
┌───────────────────────┬───────────┬───────────┐
│ DEFINICIÓN BPMN       │ I (INIT)  │ E (EXEC)  │
├───────────────────────┼───────────┼───────────┤
│ ▸ Proceso_Siniestros  │    ☑      │    ☑      │  ← proceso (existente)
│   └ Contabilidad      │    ☐      │    ☑      │  ← lane (NUEVO)
│   └ Aprobación        │    ☑      │    ☐      │  ← lane (NUEVO)
│   └ Archivo           │    ☐      │    ☑      │  ← lane (NUEVO)
│ ▸ Crédito_Hipotecario │    ☑      │    ☐      │  ← proceso
│   └ Analista_Riesgos  │    ☐      │    ☑      │  ← lane (NUEVO)
│   └ Gerencia          │    ☑      │    ☑      │  ← lane (NUEVO)
└───────────────────────┴───────────┴───────────┘
```

**Reglas de UX (decisión del PO: "agradable y sencillo"):**
- Los procesos son filas principales (con icono ▸/▾ para expandir/colapsar)
- Los lanes son filas hijas, indentadas, con estilo visual diferenciado (ej: fondo más claro, borde izquierdo coloreado)
- Los checkboxes I/E de los lanes son independientes de los del proceso padre
- Agregar un icono de lane (ej: 🏊 o ≡) junto al nombre de cada lane
- Si un proceso no tiene lanes, mostrar texto sutil: "Sin lanes definidos"
- Al abrir el modal en modo edición, cargar las asignaciones existentes del rol via `fetchLaneAssignmentsByRole(roleId)`

**T5.3:** En la función de guardado del modal (`consolidarRol` o equivalente), agregar:
- Recopilar las asignaciones lane-rol del formulario
- Llamar `saveLaneRoleAssignments(roleId, assignments)` junto con el guardado existente de `process-permissions`

**Criterio de Salida:**
1. `npm run build` exitoso
2. Al editar un rol, la sección "Definición BPMN" muestra procesos expandibles con sus lanes
3. Los checkboxes I/E por lane funcionan y persisten al guardar
4. No se rompe la funcionalidad existente de I/E a nivel proceso

---

### 📦 Micro-Sprint 6: Integración E2E y Certificación
**Agente:** QA | **Duración Estimada:** ≤2 horas | **Dependencia:** Todos los anteriores ✅

**Escenarios de Validación:**

| # | Escenario | Criterio PASS |
|---|-----------|---------------|
| E1 | Crear BPMN con Pool + 2 Lanes ("Contabilidad", "Aprobación") en el modeler | Lanes se crean, panel de propiedades muestra campos |
| E2 | Asignar nombre y actor al Lane "Contabilidad" | Valores se reflejan en el XML BPMN (verificar Export) |
| E3 | Vincular rol "ROLE_PERITO" al Lane "Contabilidad" via dropdown | `camunda:candidateGroups` se escribe en el XML |
| E4 | Desplegar el BPMN | Toast muestra roles generados + Tabla `ibpms_bpmn_lane` tiene 2 registros |
| E5 | En RBAC, editar "ROLE_PERITO" → Sección "Definición BPMN" | El proceso desplegado aparece con sus 2 lanes expandibles |
| E6 | Marcar checkbox E (Execute) para Lane "Contabilidad" | Se guarda en `ibpms_lane_role_assignment` |
| E7 | Verificar que J-02 y J-04 NO se rompieron | Journeys existentes pasan sin regresión |

**Criterio de Salida:**
1. 7/7 escenarios PASS
2. Cero regresiones en funcionalidad existente
3. Evidencia: Screenshots de cada paso + consulta SQL a las 2 tablas nuevas

---

## 5. CONTRATOS DE API NUEVOS (AGREGAR AL SSOT)

```yaml
# Nuevo — Lane CRUD
GET /api/v1/admin/lanes:
  queryParams:
    processKey: string (required)
  response: 200
    body: BpmnLaneDTO[]
      - id: UUID
      - processKey: string
      - laneXmlId: string
      - laneName: string
      - actorDescription: string | null
      - linkedRoleName: string | null

# Nuevo — Lane-Role Assignments por Rol
GET /api/v1/admin/roles/{roleId}/lane-assignments:
  response: 200
    body: LaneRoleAssignmentDTO[]
      - laneId: UUID
      - laneName: string
      - processKey: string
      - canInitiate: boolean
      - canExecute: boolean

# Nuevo — Guardar Lane-Role Assignments
PUT /api/v1/admin/roles/{roleId}/lane-assignments:
  body: LaneRoleAssignmentRequest[]
    - laneId: UUID
    - canInitiate: boolean
    - canExecute: boolean
  response: 200
```

---

## 6. MIGRACIÓN DEL SISTEMA B (`profile_bpmn_assignment`)

**Instrucción al Arquitecto:**
- **MANTENER** la tabla `profile_bpmn_assignment` y su lógica en `generarRolesDesdeLanes()` — NO eliminar.
- **AGREGAR** la lógica nueva de INSERT en `ibpms_bpmn_lane` DENTRO del mismo método `generarRolesDesdeLanes()`.
- El Sistema B sigue funcionando como "legacy read-only" mientras el nuevo Sistema A (`ibpms_bpmn_lane` + `ibpms_lane_role_assignment`) se convierte en el sistema principal.
- En un sprint futuro se evaluará la deprecación completa del Sistema B.

---

## 7. SECUENCIA DE EJECUCIÓN ESTRICTA

```
Micro-Sprint 1 (Infra/BD) ──→ Micro-Sprint 2 (Backend Entidades)
                                    │
                                    ▼
                              Micro-Sprint 3 (Backend Servicios+API)
                                    │
                              ┌─────┴─────┐
                              ▼           ▼
                    Micro-Sprint 4    Micro-Sprint 5
                    (FE Modeler)      (FE RBAC)
                              │           │
                              └─────┬─────┘
                                    ▼
                              Micro-Sprint 6 (QA E2E)
```

> [!WARNING]
> **Micro-Sprint 4 y 5 pueden ejecutarse en paralelo** ya que tocan archivos diferentes (`BpmnDesigner.vue` vs `IdentityGovernance.vue`), PERO ambos dependen de que Micro-Sprint 3 esté completado (endpoints backend listos).
