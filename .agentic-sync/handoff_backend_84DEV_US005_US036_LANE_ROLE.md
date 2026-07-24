# 📦 Handoff Backend — Iteración 84-DEV-LANE-ROLE
# Micro-Sprint 2 + 3: Entidades JPA, Repositorios, Puerto Hexagonal, Servicio y Endpoints REST

---

## 1. Metadatos y SSOT

| Campo | Valor |
|-------|-------|
| **Iteración** | `84-DEV-LANE-ROLE` |
| **US** | US-005 (Extensión: Lane Actor Assignment) + US-036 (Extensión: RBAC Lane Integration) |
| **CAs** | Extensión funcional aprobada por PO — No son CAs numerados existentes |
| **Rama Git** | `feature/lane-role-assignment` |
| **Agente** | Backend |
| **Dependencias** | ✅ Infra/BD completado y pusheado (MS-1 debe estar mergeado primero) |
| **SSOT** | `docs/requirements/v1_user_stories_index.md` → Epic B (US-005) + Epic E (US-036) |
| **Flujo de Trabajo** | Infra/BD → **Backend** → Frontend → QA |
| **API Contracts** | `docs/sprints/gobernanza_pm/API_CONTRACTS.md` — Sección 5.9 Lane Management (3 endpoints) |

---

## 2. Alineación Arquitectónica y ADRs

### ADRs Aplicables

| ADR | Impacto |
|-----|--------|
| ADR-001 (Hexagonal Architecture) | Lógica de negocio en `domain/`, JPA en `infrastructure/jpa/`. Puertos como interfaces en `application/port/`. Disposable Adapters. |
| ADR-003 (Camunda 7 Embedded) | Camunda solo como Driven Adapter. NO insertar Camunda APIs en domain. La extensión de `generarRolesDesdeLanes()` vive en `application/service/` como Use Case. |
| ADR-009 (PostgreSQL) | JPA Entities mapean a `ibpms_*` tables. UUID para PKs. PostgreSQL dialect. |
| ADR-011 (Local CQRS) | DTOs de lectura (BpmnLaneDTO, LaneRoleAssignmentDTO) separados de DTOs de escritura (LaneRoleAssignmentRequest). No reutilizar across read/write. |
| ADR-016 (Hexagonal Operational) | Entities en `com.ibpms.poc.infrastructure.jpa.entity`. REST Controllers en `com.ibpms.poc.infrastructure.web`. Ports en `com.ibpms.poc.application.port`. Traceability tag en línea 1. |

### Confirmación de Stack
- **Backend:** Java 17+ / Spring Boot 3.2.3 / JPA / PostgreSQL / Liquibase
- **Arquitectura:** Hexagonal (Puertos y Adaptadores)
- **Prohibiciones:** No `@Entity` en domain, no Camunda APIs en domain, no H2, no mocks

### Trazabilidad
Las 2 entidades JPA nuevas (`BpmnLaneEntity`, `LaneRoleAssignmentEntity`) viven en `infrastructure/jpa/entity/` como Driven Adapters, cumpliendo ADR-001. Los 3 endpoints REST nuevos viven en `infrastructure/web/` como Driving Adapters. El puerto hexagonal `BpmnLanePort` sigue el patrón Output Port del ADR-016. La extensión de `generarRolesDesdeLanes()` mantiene retrocompatibilidad con el Sistema B (`ProfileBpmnAssignment`) cumpliendo la decisión P3 del PO.

---

## 3. Rutas Exactas y Contexto Preexistente

### Archivos a CREAR (Nuevos)

| Archivo | Ruta Absoluta |
|---------|---------------|
| `BpmnLaneEntity.java` | `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/bpmn/BpmnLaneEntity.java` |
| `LaneRoleAssignmentEntity.java` | `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/security/LaneRoleAssignmentEntity.java` |
| `BpmnLaneJpaRepository.java` | `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/repository/BpmnLaneJpaRepository.java` |
| `LaneRoleAssignmentJpaRepository.java` | `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/repository/LaneRoleAssignmentJpaRepository.java` |
| `BpmnLanePort.java` | `backend/ibpms-core/src/main/java/com/ibpms/poc/application/port/BpmnLanePort.java` |
| `BpmnLaneService.java` | `backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/bpmn/BpmnLaneService.java` |

### Archivos a MODIFICAR

| Archivo | Ruta | Zona de Cambio |
|---------|------|----------------|
| `DesplegarDefinicionService.java` | `backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/bpmn/DesplegarDefinicionService.java` | Extender `generarRolesDesdeLanes()` (línea 44+) para INSERT en `ibpms_bpmn_lane` |
| `RoleAdminController.java` | `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/rest/admin/RoleAdminController.java` | Agregar 2 endpoints nuevos de lane-assignments |
| `api-schema.d.ts` | `frontend/src/types/api-schema.d.ts` | Agregar tipos TS para `BpmnLaneDTO` y `LaneRoleAssignmentDTO` |

### Contexto Preexistente

**`DesplegarDefinicionService.java`** (línea 44):
- Método `generarRolesDesdeLanes()` ya parsea `<bpmn:lane>` del XML BPMN y crea perfiles con `ProfileBpmnAssignmentEntity`.
- **INSTRUCCION:** MANTENER la lógica existente del Sistema B (`ProfileBpmnAssignment`) intacta. AGREGAR después de ella la nueva lógica de INSERT/UPSERT en `ibpms_bpmn_lane`.

**`RoleEntity.java`** (ya existente):
- Tabla `ibpms_security_role`, PK `UUID id`
- Ya tiene campos `lane_id` (VARCHAR) y `process_definition_id` (VARCHAR) — estos son del Sistema B legacy.
- Tiene relación `@OneToMany` con `ProcessPermissionEntity`.

**`BpmnProcessDesignEntity.java`** (ya existente):
- Tabla `ibpms_bpmn_process_design`, PK `UUID id`
- Campo `technical_id` (UNIQUE) — es el `processKey` del motor Camunda.

**`RbacPort.java`** (ya existente):
- Puerto hexagonal con métodos `bindLaneToProfile()` y `getPermittedBpmnLanesForGroups()`.
- **NO modificar** — el nuevo puerto `BpmnLanePort` es INDEPENDIENTE.

---

## 4. Snippets Prescriptivos

### T2.1 — `BpmnLaneEntity.java`

Sigue el patrón de `ProcessPermissionEntity.java` para consistencia:

```java
// @Traceability: US-005/US-036 - ADR-001, ADR-009
package com.ibpms.poc.infrastructure.jpa.entity.bpmn;

import jakarta.persistence.*;
import java.util.UUID;
import java.time.LocalDateTime;
import com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity;

@Entity
@Table(name = "ibpms_bpmn_lane")
public class BpmnLaneEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_design_id", nullable = false)
    private com.ibpms.poc.infrastructure.jpa.entity.BpmnProcessDesignEntity processDesign;

    @Column(name = "lane_xml_id", nullable = false, length = 150)
    private String laneXmlId;

    @Column(name = "lane_name", nullable = false, length = 255)
    private String laneName;

    @Column(name = "actor_description", length = 500)
    private String actorDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_role_id")
    private RoleEntity linkedRole;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Constructor vacío requerido por JPA
    public BpmnLaneEntity() {}

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters y Setters completos (generarlos todos)
    // getId, setId, getProcessDesign, setProcessDesign,
    // getLaneXmlId, setLaneXmlId, getLaneName, setLaneName,
    // getActorDescription, setActorDescription, getLinkedRole, setLinkedRole,
    // getCreatedAt, setCreatedAt, getUpdatedAt, setUpdatedAt
}
```

### T2.2 — `LaneRoleAssignmentEntity.java`

```java
// @Traceability: US-005/US-036 - ADR-001, ADR-009
package com.ibpms.poc.infrastructure.jpa.entity.security;

import jakarta.persistence.*;
import java.util.UUID;
import java.time.LocalDateTime;
import com.ibpms.poc.infrastructure.jpa.entity.bpmn.BpmnLaneEntity;

@Entity
@Table(name = "ibpms_lane_role_assignment")
public class LaneRoleAssignmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lane_id", nullable = false)
    private BpmnLaneEntity lane;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    @Column(name = "can_initiate", nullable = false)
    private Boolean canInitiate = false;

    @Column(name = "can_execute", nullable = false)
    private Boolean canExecute = true;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Column(name = "assigned_by", length = 255)
    private String assignedBy;

    public LaneRoleAssignmentEntity() {}

    // Getters y Setters completos
}
```

### T2.3 — Repositorios JPA

**`BpmnLaneJpaRepository.java`:**
```java
// @Traceability: US-005/US-036 - ADR-001
package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.bpmn.BpmnLaneEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface BpmnLaneJpaRepository extends JpaRepository<BpmnLaneEntity, UUID> {
    List<BpmnLaneEntity> findByProcessDesign_TechnicalId(String technicalId);
    void deleteByProcessDesign_Id(UUID processDesignId);
}
```

**`LaneRoleAssignmentJpaRepository.java`:**
```java
// @Traceability: US-005/US-036 - ADR-001
package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.security.LaneRoleAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface LaneRoleAssignmentJpaRepository extends JpaRepository<LaneRoleAssignmentEntity, UUID> {
    List<LaneRoleAssignmentEntity> findByRole_Id(UUID roleId);
    List<LaneRoleAssignmentEntity> findByLane_Id(UUID laneId);
    void deleteByRole_Id(UUID roleId);
}
```

### T3.1 — Puerto Hexagonal `BpmnLanePort.java`

```java
// @Traceability: US-005/US-036 - ADR-001, ADR-016
package com.ibpms.poc.application.port;

import java.util.List;
import java.util.UUID;

public interface BpmnLanePort {
    List<BpmnLaneDTO> getLanesByProcessKey(String processDefinitionKey);
    void syncLanesFromDeployment(String processKey, UUID processDesignId, List<LaneInfo> lanes);
    void assignRoleToLane(UUID laneId, UUID roleId, boolean canInitiate, boolean canExecute);
    void removeRoleFromLane(UUID laneId, UUID roleId);
    List<LaneRoleAssignmentDTO> getAssignmentsByRoleId(UUID roleId);
}
```

> **NOTA:** Los records `BpmnLaneDTO`, `LaneRoleAssignmentDTO`, `LaneRoleAssignmentRequest` y `LaneInfo` deben crearse como records Java en el mismo paquete o en un paquete `dto` dentro de `application/`.

### T3.2 — Extensión de `generarRolesDesdeLanes()`

Dentro del método existente en `DesplegarDefinicionService.java`, AGREGAR después de la lógica actual de `ProfileBpmnAssignment`:

```java
// === INICIO: Extensión Lane Actor Assignment (US-005/US-036) ===
// MANTENER la lógica existente de ProfileBpmnAssignment arriba (retrocompatibilidad)
// AGREGAR: Sincronizar lanes como entidades de primer nivel en ibpms_bpmn_lane

List<BpmnLaneEntity> existingLanes = bpmnLaneJpaRepository.findByProcessDesign_TechnicalId(processKey);
Set<String> xmlLaneIds = new HashSet<>();

for (LaneInfo laneInfo : parsedLanes) {
    xmlLaneIds.add(laneInfo.laneXmlId());
    
    BpmnLaneEntity laneEntity = existingLanes.stream()
        .filter(l -> l.getLaneXmlId().equals(laneInfo.laneXmlId()))
        .findFirst()
        .orElseGet(() -> {
            BpmnLaneEntity newLane = new BpmnLaneEntity();
            newLane.setProcessDesign(processDesignEntity);
            newLane.setLaneXmlId(laneInfo.laneXmlId());
            return newLane;
        });
    
    laneEntity.setLaneName(laneInfo.laneName());
    bpmnLaneJpaRepository.save(laneEntity);
}

// Purgar lanes zombies (que ya no existen en el XML)
existingLanes.stream()
    .filter(l -> !xmlLaneIds.contains(l.getLaneXmlId()))
    .forEach(bpmnLaneJpaRepository::delete);
// === FIN: Extensión Lane Actor Assignment ===
```

### T3.3 — DTOs (Records Java)

```java
// @Traceability: US-005/US-036 - ADR-011 (CQRS: DTOs de lectura separados de escritura)
package com.ibpms.poc.application.dto;

import java.util.UUID;

// DTO de LECTURA — para GET /api/v1/admin/lanes
public record BpmnLaneDTO(
    UUID id,
    String processKey,
    String laneXmlId,
    String laneName,
    String actorDescription,
    String linkedRoleName
) {}

// DTO de LECTURA — para GET /api/v1/admin/roles/{roleId}/lane-assignments
public record LaneRoleAssignmentDTO(
    UUID laneId,
    String laneName,
    String processKey,
    boolean canInitiate,
    boolean canExecute
) {}

// DTO de ESCRITURA — para PUT /api/v1/admin/roles/{roleId}/lane-assignments
public record LaneRoleAssignmentRequest(
    UUID laneId,
    boolean canInitiate,
    boolean canExecute
) {}

// Auxiliar interno — datos parseados del XML BPMN
public record LaneInfo(
    String laneXmlId,
    String laneName
) {}
```

### T3.4 — Endpoints REST (agregar en `RoleAdminController.java` o nuevo `LaneAdminController.java`)

Contratos definidos en `API_CONTRACTS.md` Sección 5.9:

| Método | Ruta | Body | Respuesta | Propósito |
|--------|------|------|-----------|-----------|
| `GET` | `/api/v1/admin/lanes?processKey={key}` | — | `List<BpmnLaneDTO>` | Listar lanes de un proceso |
| `GET` | `/api/v1/admin/roles/{roleId}/lane-assignments` | — | `List<LaneRoleAssignmentDTO>` | Obtener asignaciones lane↔rol |
| `PUT` | `/api/v1/admin/roles/{roleId}/lane-assignments` | `List<LaneRoleAssignmentRequest>` | `200 OK` | Guardar/actualizar asignaciones |

```java
// En RoleAdminController.java o nuevo LaneAdminController.java

@GetMapping("/api/v1/admin/lanes")
public ResponseEntity<List<BpmnLaneDTO>> getLanesByProcess(
        @RequestParam String processKey) {
    return ResponseEntity.ok(bpmnLanePort.getLanesByProcessKey(processKey));
}

@GetMapping("/api/v1/admin/roles/{roleId}/lane-assignments")
public ResponseEntity<List<LaneRoleAssignmentDTO>> getLaneAssignments(
        @PathVariable UUID roleId) {
    return ResponseEntity.ok(bpmnLanePort.getAssignmentsByRoleId(roleId));
}

@PutMapping("/api/v1/admin/roles/{roleId}/lane-assignments")
public ResponseEntity<Void> saveLaneAssignments(
        @PathVariable UUID roleId,
        @RequestBody List<LaneRoleAssignmentRequest> assignments) {
    // 1. Eliminar asignaciones previas del rol
    // 2. Crear nuevas asignaciones
    // 3. assigned_by del SecurityContext (JWT)
    bpmnLaneService.replaceAssignmentsForRole(roleId, assignments);
    return ResponseEntity.ok().build();
}
```

---

## 5. Matriz de QA y Testing Atómico

| Test ID | Validación | CA | Aserción Esperada |
|---------|-----------|-----|-------------------|
| BE-01 | `mvn clean compile` exitoso | — | Build sin errores |
| BE-02 | Spring Boot arranca en 8080 | — | `Tomcat started on port 8080` |
| BE-03 | GET /api/v1/admin/lanes?processKey=test | Lane CRUD | 200 OK (array vacío aceptable) |
| BE-04 | Swagger muestra 3 endpoints nuevos | — | Endpoints visibles en /swagger-ui |
| BE-05 | Deploy BPMN con lanes inserta en ibpms_bpmn_lane | Lane sync | Tabla tiene N filas = N lanes del XML |
| BE-06 | Re-deploy purga lanes zombies | Lane sync | Lane eliminado del XML desaparece de la tabla |
| BE-07 | PUT lane-assignments persiste en ibpms_lane_role_assignment | RBAC | SELECT confirma filas insertadas |

Compilación obligatoria: Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.

---

## 6. Mensaje de Despacho

> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

> 📝 **POLÍTICA ANTIAMNESIA:** Antes de iniciar, lee `docs/architecture/arquitecturar.md` y `docs/architecture/adr-001-hexagonal-architecture.md` para re-entrenar tu contexto. NO asumas cómo funciona la arquitectura hexagonal — léela.

> 📋 **DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA:**
> El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Antes de iniciar tu trabajo, ejecuta el protocolo de pre-validación:
> 1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
> 2. Si no responde, arráncalo con: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
> 3. Verifica los servicios Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`) y RabbitMQ (`5672`) deben estar `Up (healthy)`.
> **PROHIBIDO** levantar el backend vía Docker o modificar el `docker-compose.yml`.

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_BACKEND.md`.
> 4. Al grabar el archivo, deténte y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_BACKEND.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION` y programa la solución.
> 6. **ANTES del commit final**, actualiza `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` explicando tu trabajo en lenguaje sencillo y amigable para un CEO.
> 7. Finaliza consolidando tus cambios mediante `git commit` y `git push` en la rama `feature/lane-role-assignment`. PROHIBIDO usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md`.
> - Aplica estrictamente las normativas de **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md`.

### Archivos INTOCABLES (Blast Radius = 0)
- `Workdesk.vue`, `Login.vue`, `FormDesigner.vue`, `FormRenderer`
- `router/index.ts`, `docker-compose.yml`
- Todas las migraciones Liquibase existentes (001-059+)
- Tests E2E existentes (J-02, J-04)
- `RbacPort.java` — NO modificar, el nuevo puerto `BpmnLanePort` es independiente
- `ProfileBpmnAssignmentEntity.java` — MANTENER como legacy read-only
