# 🔧 HANDOFF BACKEND — Iteración Correctiva 84-DEV-LANE-ROLE-FIX

> **Tipo:** Corrección de defectos post-auditoría PM-IA
> **Fecha de emisión:** 2026-07-14
> **Prioridad:** CRÍTICA

---

## 1. Metadatos y SSOT

| Campo | Valor |
|-------|-------|
| **Iteración** | `84-DEV-LANE-ROLE-FIX` |
| **US** | US-005 (Motor BPMN) + US-036 (RBAC) — Extensión Lane-Role Assignment |
| **CAs** | Corrección de defectos D-01, D-02, D-03, D-05, D-06, D-07 |
| **Rama Git** | `DevDavid` |
| **SSOT** | `docs/requirements/epics/epic_B_formularios_bpmn.md` (US-005) + `docs/requirements/epics/epic_E_seguridad_identidad_config.md` (US-036) |
| **Secuencia** | Backend (MC-1) ▸ en PARALELO con Frontend (MC-2) ▸ Gobernanza (MC-3) |
| **Paquete base** | `com.ibpms.poc` (NO `com.ibpms.core.bpmn`) |

> ⚠️ **CONTEXTO CRÍTICO:** Esta es una iteración CORRECTIVA. NO estás construyendo funcionalidad nueva. Estás corrigiendo 6 defectos detectados por auditoría forense en código YA EXISTENTE. El blast radius es ESTRICTO — solo puedes tocar los 4 archivos listados abajo. Cualquier cambio fuera de estos archivos será rechazado.

> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

---

## 2. Alineación Arquitectónica

### ADRs Aplicables
| ADR | Impacto en esta iteración |
|-----|--------------------------|
| ADR-001 (Hexagonal) | D-06: Controller debe depender de `BpmnLanePort` (abstracción), no de `BpmnLaneService` (concreto). **NOTA:** La clase `BpmnLaneService` en `application/service/` importa tipos de `infrastructure/jpa` — esto es un patrón sistémico del proyecto (16+ services lo hacen). NO refactorices los imports de BpmnLaneService; solo corrige la inyección del controller. |
| ADR-009 (PostgreSQL) | Sin cambios DDL. Las tablas `ibpms_bpmn_lane` y `ibpms_lane_role_assignment` ya existen. |

### Decisiones Arquitectónicas Pre-Aprobadas
- **DA-01:** La violación hexagonal en `BpmnLaneService` (imports de infrastructure) se acepta como "excepción consistente" — NO se modifica.
- **DA-03:** Anti-patrón proxy entity se corrige con `EntityManager.getReference()` — consistente con el patrón JPA establecido.
- **DA-05:** `processDesignId` se resuelve vía `BpmnDesignPort` (puerto existente), NO vía repository directo.

---

## 3. Rutas Exactas y Contexto Preexistente

### Archivo 1: `DesplegarDefinicionService.java`
- **Ruta:** `backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/DesplegarDefinicionService.java`
- **Total líneas:** 105
- **Estado actual:** En línea 93, llama a `bpmnLanePort.syncLanesFromDeployment(processId, null, parsedLanes)` — el segundo parámetro es `null` porque no resuelve el `processDesignId`.
- **Dependencias existentes:** Ya inyecta `BpmnLanePort`. NO inyecta `BpmnDesignPort` todavía.

### Archivo 2: `BpmnLaneService.java`
- **Ruta:** `backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/bpmn/BpmnLaneService.java`
- **Total líneas:** 127
- **Estado actual:**
  - L8-12: Imports de `infrastructure/jpa` (entities + repositories) — **NO TOCAR** (DA-01)
  - L47-78: `syncLanesFromDeployment()` — tiene guard `if(processDesignId != null)` en L61 pero recibe siempre `null`
  - L80-84: `assignRoleToLane()` — **CUERPO VACÍO** (solo comentario) — **ELIMINAR**
  - L86-90: `removeRoleFromLane()` — **CUERPO VACÍO** (solo comentario) — **ELIMINAR**
  - L105-125: `replaceAssignmentsForRole()` — Funcional pero con 3 defectos:
    - L120: `entity.setAssignedBy("system")` — hard-code (D-02)
    - L109-115: `new RoleEntity(); role.setId(roleId)` — anti-patrón proxy (D-07)
    - Sin validaciones de existencia roleId/laneId (D-03)

### Archivo 3: `BpmnLanePort.java`
- **Ruta:** `backend/ibpms-core/src/main/java/com/ibpms/poc/application/port/out/BpmnLanePort.java`
- **Total líneas:** 17
- **Estado actual:** 5 métodos declarados:
  - `getLanesByProcessKey(String)` ← CONSERVAR
  - `syncLanesFromDeployment(String, UUID, List<LaneInfo>)` ← CONSERVAR
  - `assignRoleToLane(UUID, UUID, boolean, boolean)` ← **ELIMINAR** (D-05)
  - `removeRoleFromLane(UUID, UUID)` ← **ELIMINAR** (D-05)
  - `getAssignmentsByRoleId(UUID)` ← CONSERVAR
- **Falta:** `replaceAssignmentsForRole(UUID, List<LaneRoleAssignmentRequest>)` (D-06)

### Archivo 4: `LaneAdminController.java`
- **Ruta:** `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/security/LaneAdminController.java`
- **Total líneas:** 46
- **Estado actual:**
  - L7: `import com.ibpms.poc.application.service.bpmn.BpmnLaneService;` — **ELIMINAR** (D-06)
  - L18-19: Inyecta AMBOS `BpmnLanePort` Y `BpmnLaneService` — **ELIMINAR** la inyección concreta
  - L29, L35: Usan `bpmnLanePort` (correcto)
  - L42: Usa `bpmnLaneService.replaceAssignmentsForRole(...)` — cambiar a `bpmnLanePort`

### Archivo de referencia (NO MODIFICAR — solo consultar):
- **BpmnDesignPort:** `backend/ibpms-core/src/main/java/com/ibpms/poc/application/port/out/BpmnDesignPort.java`
  - Método existente: `Optional<BpmnProcessDesignEntity> findByTechnicalId(String technicalId)`
  - Adaptador: `BpmnDesignJpaAdapter` wrappea `BpmnProcessDesignRepository`

---

## 4. Correcciones Prescriptivas (Snippets)

### D-01: Resolver `processDesignId = null` (CRÍTICO)

**En `DesplegarDefinicionService.java`:**

1. Agregar inyección de `BpmnDesignPort` en el constructor:
```java
private final BpmnDesignPort bpmnDesignPort;

// En constructor, agregar parámetro:
public DesplegarDefinicionService(/* ... params existentes ...*/, BpmnDesignPort bpmnDesignPort) {
    // ... asignaciones existentes ...
    this.bpmnDesignPort = bpmnDesignPort;
}
```

2. ANTES de la línea 93 (la llamada a `syncLanesFromDeployment`), resolver el ID:
```java
// === INICIO: Extensión Lane Actor Assignment (US-005/US-036) ===
UUID processDesignId = bpmnDesignPort.findByTechnicalId(processId)
    .map(design -> design.getId())
    .orElse(null);
bpmnLanePort.syncLanesFromDeployment(processId, processDesignId, parsedLanes);
// === FIN: Extensión Lane Actor Assignment ===
```

**En `BpmnLaneService.syncLanesFromDeployment()`:**

3. Agregar guard al inicio del método (ANTES de cualquier lógica):
```java
public void syncLanesFromDeployment(String processKey, UUID processDesignId, List<LaneInfo> lanes) {
    if (processDesignId == null) {
        log.warn("syncLanesFromDeployment: processDesignId is null for processKey={}. Skipping lane sync to avoid ConstraintViolationException.", processKey);
        return;
    }
    // ... resto del método existente ...
}
```

---

### D-02: `assigned_by` hard-coded a `"system"` (ALTO)

**En `BpmnLaneService.replaceAssignmentsForRole()`, línea 120:**

Reemplazar:
```java
entity.setAssignedBy("system");
```

Con:
```java
String currentUser = SecurityContextHolder.getContext().getAuthentication() != null
    ? SecurityContextHolder.getContext().getAuthentication().getName()
    : "system";
entity.setAssignedBy(currentUser);
```

Agregar import si no existe:
```java
import org.springframework.security.core.context.SecurityContextHolder;
```

---

### D-03: Sin validación 400/404 en `replaceAssignmentsForRole` (ALTO)

**En `BpmnLaneService.replaceAssignmentsForRole()`, AL INICIO del método:**

```java
public void replaceAssignmentsForRole(UUID roleId, List<LaneRoleAssignmentRequest> assignments) {
    // Validación D-03: Verificar existencia del role
    if (!roleRepository.existsById(roleId)) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found: " + roleId);
    }
    
    // Validación D-03: Verificar existencia de cada lane
    for (LaneRoleAssignmentRequest req : assignments) {
        if (!bpmnLaneRepository.existsById(req.laneId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lane not found: " + req.laneId());
        }
    }
    
    // ... resto del método (DELETE + INSERT) ...
}
```

Agregar imports:
```java
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
```

**NOTA:** `roleRepository` → Verifica si ya existe inyectado en el servicio. Si no, busca el nombre exacto del repository que maneja `RoleEntity` (puede ser `RoleJpaRepository` o `SecurityRoleRepository`). Inyéctalo en el constructor. NO crees uno nuevo si ya existe.

---

### D-05: Eliminar métodos fantasma (MEDIO)

**En `BpmnLanePort.java`:**
- Eliminar la declaración `void assignRoleToLane(UUID laneId, UUID roleId, boolean canInitiate, boolean canExecute);`
- Eliminar la declaración `void removeRoleFromLane(UUID laneId, UUID roleId);`

**En `BpmnLaneService.java`:**
- Eliminar completamente los métodos `assignRoleToLane()` (L80-84) y `removeRoleFromLane()` (L86-90) — son cuerpos vacíos con solo comentarios.

---

### D-06: `replaceAssignmentsForRole` fuera del Port + Controller con inyección concreta (MEDIO)

**En `BpmnLanePort.java`:**
- Agregar: `void replaceAssignmentsForRole(UUID roleId, List<LaneRoleAssignmentRequest> assignments);`
- Agregar el import necesario para `LaneRoleAssignmentRequest` (verifica el paquete exacto en el codebase).

**En `LaneAdminController.java`:**
1. Eliminar `import com.ibpms.poc.application.service.bpmn.BpmnLaneService;`
2. Eliminar el campo `private final BpmnLaneService bpmnLaneService;`
3. Actualizar el constructor para recibir SOLO `BpmnLanePort`:
```java
public LaneAdminController(BpmnLanePort bpmnLanePort) {
    this.bpmnLanePort = bpmnLanePort;
}
```
4. En línea 42, cambiar `bpmnLaneService.replaceAssignmentsForRole(...)` → `bpmnLanePort.replaceAssignmentsForRole(...)`

---

### D-07: Anti-patrón proxy entity (MEDIO)

**En `BpmnLaneService.replaceAssignmentsForRole()`:**

Reemplazar el patrón:
```java
RoleEntity role = new RoleEntity();
role.setId(roleId);
```
Con:
```java
RoleEntity role = entityManager.getReference(RoleEntity.class, roleId);
```

Y lo mismo para `BpmnLaneEntity`:
```java
// Antes (anti-patrón):
BpmnLaneEntity lane = new BpmnLaneEntity();
lane.setId(req.laneId());

// Después:
BpmnLaneEntity lane = entityManager.getReference(BpmnLaneEntity.class, req.laneId());
```

Inyectar `EntityManager` en el constructor de `BpmnLaneService`:
```java
import jakarta.persistence.EntityManager;

// En campos:
private final EntityManager entityManager;

// En constructor (agregar parámetro):
public BpmnLaneService(/* ... params existentes ...*/, EntityManager entityManager) {
    // ... asignaciones existentes ...
    this.entityManager = entityManager;
}
```

---

## 5. Matriz de Verificación

| # | Defecto | Verificación | Comando/Query |
|---|---------|-------------|---------------|
| D-01 | processDesignId null | Deploy BPMN con 2 lanes → `SELECT process_design_id FROM ibpms_bpmn_lane` NOT NULL | SQL directo en PostgreSQL |
| D-02 | assigned_by "system" | Crear asignación lane-role → `SELECT assigned_by FROM ibpms_lane_role_assignment` ≠ "system" | SQL + API PUT |
| D-03 | Sin 400/404 | `PUT /api/v1/admin/roles/{UUID-INEXISTENTE}/lane-assignments` → HTTP 404 | `curl -X PUT` |
| D-03 | Sin 400/404 | PUT con `laneId` inexistente → HTTP 400 | `curl -X PUT` |
| D-05 | Métodos fantasma | `BpmnLanePort.java` tiene exactamente 4 métodos (no 5) | Inspección visual |
| D-06 | Controller concreto | `LaneAdminController` NO importa `BpmnLaneService` | `grep -r "BpmnLaneService" LaneAdminController.java` |
| D-07 | Proxy entity | No existe `new RoleEntity()` ni `new BpmnLaneEntity()` en `BpmnLaneService` | `grep -r "new RoleEntity\|new BpmnLaneEntity" BpmnLaneService.java` |

---

## 6. Instrucciones Operativas y de Comunicación

> 📋 **DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA:**
> El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Antes de iniciar tu trabajo, ejecuta el protocolo de pre-validación:
> 1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
> 2. Si no responde, arráncalo con: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
> 3. Verifica los servicios Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`) y RabbitMQ (`5672`) deben estar `Up (healthy)`.
> **PROHIBIDO** levantar el backend vía Docker o modificar el `docker-compose.yml`.

> **Compilación obligatoria:** Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_BACKEND.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_BACKEND.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION` y programa la solución.
6. **ANTES del commit final**, actualiza `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` explicando tu trabajo en lenguaje sencillo y amigable para un CEO (QUÉ hiciste y PARA QUÉ sirve).
7. Finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama `DevDavid`. Queda estrictamente prohibido usar git stash.
