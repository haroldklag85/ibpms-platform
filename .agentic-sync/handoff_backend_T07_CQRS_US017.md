# 🧠→⚙️ Handoff: Arquitecto Líder → Backend - Java
# T-07: Implementación Controladores CQRS (US-017)

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** ⚙️ BACKEND - JAVA
**Fecha:** 2026-05-12T09:30:00-05:00
**Sprint:** 7 — Sprint 7.1
**Prioridad:** 🔴 Alta
**Dependencia:** T-07 Infraestructura (Tablas creadas)

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
cat .cursorrules

# 2. Skill principal del agente receptor
cat .agents/skills/backend_sre_compilation_audit/SKILL.md

# 3. Skills transversales aplicables
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. ADRs relevantes
cat docs/architecture/adr_011_local_cqrs_v1.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar la anotación @Traceability o comentario `// @Traceability: US-XXX, CA-XX`. Esto es INNEGOCIABLE.

---

## 🔬 Diagnóstico del Arquitecto

Actualmente, las peticiones de formularios se dirigen sincrónicamente a servicios dependientes de estado de Camunda, con lógica acoplada, violando la CA-02 y exponiendo fallos 5xx al cliente si Camunda se detiene.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| No existe TaskCompletionController | `src/main/java/com/ibpms/poc/application/controller/` | Controlador para `/api/v1/workbox/tasks/{id}/complete` ausente (CA-01). |
| Falta Event Store Entity | `src/main/java/com/ibpms/poc/domain/entity/` | No hay entidad `FormEventStoreEntity` mapeada a la BD para guardar CQRS (CA-06). |
| Falta Rollback/Saga Compensatorio | `src/main/java/com/ibpms/poc/application/service/` | Ausencia de lógica que guarde `FORM_SUBMIT_ROLLED_BACK` si falla Camunda (CA-10). |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Crear las Entidades y Repositorios

**Archivo:** `ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/domain/entity/FormEventStoreEntity.java`

```java
package com.ibpms.poc.domain.entity;

import com.ibpms.poc.shared.annotations.Traceability;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "ibpms_form_event_store")
@Traceability(US = "US-017", CA = {"CA-06"})
public class FormEventStoreEntity {
    @Id
    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "task_id")
    private String taskId;

    @Column(name = "process_instance_id")
    private String processInstanceId;

    @Column(name = "user_id")
    private String userId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json", columnDefinition = "jsonb")
    private String payloadJson;

    @Column(name = "schema_version")
    private String schemaVersion;

    @Column(name = "idempotency_key")
    private UUID idempotencyKey;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
```

(Crea también su respectivo repositorio `FormEventStoreRepository.java`)

### Paso 2: Crear el Controlador Principal

**Archivo:** `ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/application/controller/TaskCompletionController.java`

```java
package com.ibpms.poc.application.controller;

import com.ibpms.poc.shared.annotations.Traceability;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/workbox/tasks")
@Traceability(US = "US-017", CA = {"CA-01", "CA-15"})
public class TaskCompletionController {

    // Inyecta el FormSubmissionUseCase (créalo)

    @PostMapping("/{id}/complete")
    public ResponseEntity<Map<String, String>> completeTask(@PathVariable String id, @RequestBody Map<String, Object> payload) {
        // Delegar al caso de uso: Guardar Evento, Notificar Camunda, Retornar eventReference
        String eventReference = "TBD_EVENT_ID"; // Usar el servicio
        return ResponseEntity.ok(Map.of("eventReference", eventReference));
    }
}
```

### Paso 3: Lógica Compensatoria SAGA

**Archivo:** `ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/FormSubmissionUseCase.java`

Debe guardar primero `FORM_SUBMITTED`. Luego invocar Camunda. Si falla por Timeout o 5xx, atrapar excepción y guardar `FORM_SUBMIT_ROLLED_BACK`.

---

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Controlador `/complete` responde 200 con eventReference | Ejecutar Test de Integración REST (Ej. `TaskCompletionControllerTest`). |
| 2 | Entidades Mapeadas a BD | Al levantar la aplicación (`spring-boot:run`), Hibernate valida y arranca. |
| 3 | Trazabilidad Inversa Implementada | `grep "@Traceability" src/main/java/.../FormEventStoreEntity.java` arroja resultados. |
| 4 | Build SRE Exitosa | `mvn clean compile test` (o verify) no muestra errores. |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Crea las entidades y repositorios JPA para `FormEventStore` y `TaskDrafts`.
2. Crea `FormSubmissionUseCase.java` implementando el patrón Append-Only y Saga (Rollback compensatorio).
3. Crea `TaskCompletionController.java` y los endpoints.
4. Ejecuta pruebas de unidad e integración: `cd ibpms-platform/backend/ibpms-core && mvn test`
5. Ejecuta compilación total: `mvn clean package -DskipTests`
6. Commit: `git add . && git commit -m "feat(backend): implement form event store CQRS and controllers US-017" && git push`

---

## 📋 Instrucciones para Copiar y Pegar

```
Asume el rol de ⚙️ BACKEND - JAVA.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat ibpms-platform/.cursorrules
2. cat ibpms-platform/.agents/skills/backend_sre_compilation_audit/SKILL.md
3. cat ibpms-platform/.agents/skills/clean_code_standards/SKILL.md
4. cat ibpms-platform/docs/architecture/adr_011_local_cqrs_v1.md
5. cat ibpms-platform/.agentic-sync/handoff_backend_T07_CQRS_US017.md

TU MISIÓN:

1. Crea las entidades `FormEventStoreEntity` y `TaskDraftEntity`.
2. Implementa `FormSubmissionUseCase` que inserta en la BD, intenta enviar a Camunda, y si falla graba rollback.
3. Implementa `TaskCompletionController` que exponga el endpoint de finalización.
4. Build/Compile: `cd ibpms-platform/backend/ibpms-core && mvn clean compile`
5. Commit: `git add . && git commit -m "feat(backend): implement CQRS endpoints and entities US-017" && git push`

REGLAS INQUEBRANTABLES:
- DEBES inyectar `@Traceability(US = "US-017", CA = {"CA-01", "CA-06"})` en las clases/métodos nuevos.
- PROHIBIDO usar lógicas sincronas o Mocks en producción. El EventStore manda.
- DEBES capturar fallos de Camunda y crear el evento `FORM_SUBMIT_ROLLED_BACK`.
```
