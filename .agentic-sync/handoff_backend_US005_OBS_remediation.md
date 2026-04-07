# Handoff Backend — Remediación OBS-1 y OBS-2 (Auditoría I-73-DEV)
## US-005 | Rama: `sprint-3/us-005-bpmn-designer`

---

## Contexto (Orden del Arquitecto Líder)

La auditoría integral de la Iteración 73-DEV para US-005 (CA-63 a CA-70) fue **APROBADA CON OBSERVACIONES**. Se detectaron los hallazgos **OBS-1** (🔴 Crítico — bloqueador de merge) y **OBS-2** (🟡 Medio — deuda técnica controlada) que requieren tu intervención.

> **Referencia de Auditoría:** `auditoria_integral_us005_iteracion73DEV.md`
> **SSOT:** `docs/requirements/v1_user_stories.md` (CA-68 L2553-2559, CA-65 L2523-2532, CA-69 L2561-2568)

---

## Hallazgo 1 — OBS-1: DataMappingEntity desalineada con DDL (Severidad: 🔴 Crítico — Bloqueador)

**CA afectado:** CA-68
**Archivos:**
- `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/DataMappingEntity.java`
- `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/repository/DataMappingRepository.java`
- `backend/ibpms-core/src/main/resources/db/changelog/22-us005-bpmn-design-schema.sql` (SOLO LECTURA — NO MODIFICAR)

**Problema:**
La entidad JPA `DataMappingEntity.java` tiene campos que NO coinciden con la tabla `ibpms_data_mappings` definida en el DDL:

| Campo Entity (ERRÓNEO) | Campo DDL (CORRECTO) |
|------------------------|---------------------|
| `Long id` (IDENTITY) | `UUID id` (PK) |
| `variableName` (String) | ❌ No existe en DDL |
| `variableType` (String) | ❌ No existe en DDL |
| `isRequired` (boolean) | ❌ No existe en DDL |
| ❌ Falta | `task_id` (VARCHAR 255 NOT NULL) |
| ❌ Falta | `connector_id` (VARCHAR 255) |
| ❌ Falta | `mapping_json` (TEXT) |
| ❌ Falta | `last_validated_at` (TIMESTAMP) |

El único campo correcto es `process_definition_key` y `created_at` (que debe renombrarse conceptualmente porque la DDL no lo tiene — hay un `last_validated_at` en su lugar).

**Solución Exacta:**

### Paso 1: Reescribir `DataMappingEntity.java`

```java
package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ibpms_data_mappings")
public class DataMappingEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "process_definition_key", nullable = false)
    private String processDefinitionKey;

    @Column(name = "task_id", nullable = false)
    private String taskId;

    @Column(name = "connector_id")
    private String connectorId;

    @Column(name = "mapping_json", columnDefinition = "TEXT")
    private String mappingJson;

    @Column(name = "last_validated_at")
    private LocalDateTime lastValidatedAt;

    public DataMappingEntity() {
        this.id = UUID.randomUUID();
    }

    public DataMappingEntity(String processDefinitionKey, String taskId, String connectorId, String mappingJson) {
        this();
        this.processDefinitionKey = processDefinitionKey;
        this.taskId = taskId;
        this.connectorId = connectorId;
        this.mappingJson = mappingJson;
        this.lastValidatedAt = LocalDateTime.now();
    }

    // --- Getters & Setters ---
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getProcessDefinitionKey() { return processDefinitionKey; }
    public void setProcessDefinitionKey(String processDefinitionKey) { this.processDefinitionKey = processDefinitionKey; }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getConnectorId() { return connectorId; }
    public void setConnectorId(String connectorId) { this.connectorId = connectorId; }

    public String getMappingJson() { return mappingJson; }
    public void setMappingJson(String mappingJson) { this.mappingJson = mappingJson; }

    public LocalDateTime getLastValidatedAt() { return lastValidatedAt; }
    public void setLastValidatedAt(LocalDateTime lastValidatedAt) { this.lastValidatedAt = lastValidatedAt; }
}
```

### Paso 2: Actualizar `DataMappingRepository.java`

```java
package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.DataMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface DataMappingRepository extends JpaRepository<DataMappingEntity, UUID> {
    List<DataMappingEntity> findByProcessDefinitionKey(String processDefinitionKey);
    List<DataMappingEntity> findByProcessDefinitionKeyAndTaskId(String processDefinitionKey, String taskId);
}
```

### Paso 3: Actualizar `BpmnDesignController.java` (endpoint `createDataMapping` L313-318)

```java
// ANTES (L314-315):
@PostMapping("/{processDefinitionKey}/data-mappings")
public ResponseEntity<?> createDataMapping(@PathVariable("processDefinitionKey") String key,
                                           @RequestBody com.ibpms.poc.infrastructure.jpa.entity.DataMappingEntity entity) {
    entity.setProcessDefinitionKey(key);
    return ResponseEntity.ok(dataMappingRepository.save(entity));
}

// DESPUÉS:
@PostMapping("/{processDefinitionKey}/data-mappings")
public ResponseEntity<?> createDataMapping(@PathVariable("processDefinitionKey") String key,
                                           @RequestBody java.util.Map<String, String> payload) {
    com.ibpms.poc.infrastructure.jpa.entity.DataMappingEntity entity =
        new com.ibpms.poc.infrastructure.jpa.entity.DataMappingEntity(
            key,
            payload.get("taskId"),
            payload.get("connectorId"),
            payload.get("mappingJson")
        );
    return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
        .body(dataMappingRepository.save(entity));
}
```

**Decisión del Arquitecto:** La DDL es la SSOT. El Entity se adapta al DDL, NUNCA al revés.

---

## Hallazgo 2 — OBS-2: Contrato API `POST /deploy` desalineado con SSOT (Severidad: 🟡 Medio)

**CA afectado:** CA-65
**Archivo:** `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/BpmnDesignController.java`
**Método:** `deployBpmnProcess` (L65-101)

**Problema:**
Según la SSOT (CA-65 L2526-2532):
1. El endpoint NO acepta `deploy_comment` ni `force_deploy` como `@RequestParam`
2. El response body retorna `message`, `warnings`, `generatedRoles` — pero la SSOT exige: `deployment_id`, `process_definition_id`, `process_definition_key`, `version`, `deployed_at`, `deployed_by`

**Solución Exacta:**

```java
// BpmnDesignController.java — REEMPLAZAR el método deployBpmnProcess (L65-101):

@PostMapping("/deploy")
public ResponseEntity<?> deployBpmnProcess(
        @RequestParam("file") MultipartFile file,
        @RequestParam(value = "deploy_comment", required = true) String deployComment,
        @RequestParam(value = "force_deploy", required = false, defaultValue = "false") boolean forceDeploy,
        @RequestHeader(value = "X-Mock-Role", required = false, defaultValue = "GUEST") String role) {

    // CA-21: Escudo RBAC para el Despliegue
    if (!"BPMN_Release_Manager".equals(role)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Acceso Denegado. Se requiere el rol BPMN_Release_Manager."));
    }

    // CA-65: Validación deploy_comment (min 10 chars)
    if (deployComment == null || deployComment.trim().length() < 10) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "deploy_comment es obligatorio y debe tener al menos 10 caracteres."));
    }

    String originalFilename = java.util.Objects.requireNonNullElse(file.getOriginalFilename(), "document");
    if (file.isEmpty() || !originalFilename.endsWith(".bpmn")) {
        return ResponseEntity.badRequest().body(Map.of("error", "Debe adjuntar un archivo .bpmn válido."));
    }

    try {
        DeploymentValidationResponse validation = preFlightAnalyzerService.analizar(file.getInputStream());

        if (!validation.isValid()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(validation);
        }

        // CA-65: Si hay warnings y no se fuerza el deploy, bloquear
        if (!validation.getWarnings().isEmpty() && !forceDeploy) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                "error", "El Pre-Flight tiene advertencias. Use force_deploy=true para omitirlas.",
                "warnings", validation.getWarnings()
            ));
        }

        // CA-65: Response body alineado con SSOT
        String mockUser = role; // TODO: Obtener desde token JWT
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "deployment_id", "dep-" + UUID.randomUUID().toString().substring(0, 8),
            "process_definition_id", UUID.randomUUID().toString(),
            "process_definition_key", originalFilename.replace(".bpmn", ""),
            "version", 1,
            "deployed_at", java.time.Instant.now().toString(),
            "deployed_by", mockUser,
            "warnings", validation.getWarnings(),
            "generated_roles", validation.getGeneratedRoles()
        ));

    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Fallo al procesar el archivo BPMN: " + e.getMessage()));
    }
}
```

---

## Hallazgo 3 — CA-69 Minor: Validación de comentario de rechazo (Severidad: 🟢 Baja)

**CA afectado:** CA-69
**Archivo:** `BpmnDesignService.java`
**Método:** `rejectDeployRequest` (L194-201)

**Problema:** La SSOT exige mínimo 20 chars en el `review_comment` al rechazar (L2566). El service no valida esto.

**Solución Exacta:**
```java
// BpmnDesignService.java — ANTES (L194):
@Transactional
public DeployRequestEntity rejectDeployRequest(UUID requestId, String reviewerId, String comment) {

// DESPUÉS (agregar validación al inicio del método):
@Transactional
public DeployRequestEntity rejectDeployRequest(UUID requestId, String reviewerId, String comment) {
    if (comment == null || comment.trim().length() < 20) {
        throw new IllegalArgumentException("El comentario de rechazo debe tener al menos 20 caracteres (CA-69).");
    }
```

---

## Verificación Obligatoria
1. Compilar exitosamente: `mvn clean compile -pl ibpms-core`
2. Ejecutar tests existentes: `mvn test -pl ibpms-core` — 0 failures
3. Verificar que H2/PostgreSQL NO arroja errores de mapping JPA al iniciar la app

---

## Restricciones Arquitectónicas
1. **NO modificar** el archivo DDL `22-us005-bpmn-design-schema.sql` — la DDL es la verdad.
2. **Vue Frontend** consumirá los nuevos campos del response — coordinado con Handoff Frontend.
3. **No usar** `git stash`. Solo `git commit` + `git push`.
4. **Convención de commits (separados por OBS):**
   - `fix(US-005): OBS-1 align DataMappingEntity with DDL schema (UUID + mapping_json)`
   - `fix(US-005): OBS-2 align POST /deploy contract with SSOT CA-65`
   - `fix(US-005): CA-69 enforce min 20 chars on rejection comment`

---

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Ejecuta los 3 cambios en el orden listado (OBS-1 primero ya que es bloqueador, luego OBS-2, finalmente CA-69).
> 2. Cada cambio es un commit atómico separado con la convención indicada arriba.
> 3. Compila y verifica tests después de CADA commit.
> 4. Al finalizar, graba tu confirmación de cierre en `.agentic-sync/approval_request_backend.md` indicando: `OBS-1 + OBS-2 + CA-69 REMEDIADOS — commits: <hash1>, <hash2>, <hash3>`.
> 5. Dile al Humano: *"Humano, he remediado OBS-1, OBS-2 y CA-69. Entrégale este mensaje al Arquitecto Líder."*
