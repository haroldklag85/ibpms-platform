# 🧠→⚙️ Handoff: ARQUITECTO LÍDER → BACKEND - JAVA
# US-029: Resolución Gap B-J04-01 y Soporte Backend US-029

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** ⚙️ BACKEND - JAVA
**Fecha:** 2026-06-05T01:30:00Z
**Sprint:** 8 — PM-01
**Prioridad:** 🔴 Alta
**Dependencia:** Ninguna

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
cat docs/sprints/gobernanza_pm/01_ROADMAP_Y_METODOLOGIA.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar
> la anotación @Traceability o comentario `// @Traceability: US-XXX, CA-XX`.
> Esto es INNEGOCIABLE.

## 🔬 Diagnóstico del Arquitecto

El Gap B-J04-01 indica que la tabla `form_event_store` requerida para CQRS no existe o presenta inconsistencias graves en el entorno (duplicidad con `ibpms_form_event_store`).

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Tabla Inexistente/Duplicada | `db/changelog/sprint3/001_create_form_event_store.sql` vs `db/changelog/changes/016-create-cqrs-event-store.sql` | Conflictos en Liquibase y nombres de tablas (`form_event_store` vs `ibpms_form_event_store`). |
| Entidad JPA duplicada | `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/FormEventStoreEntity.java` | Apunta a la tabla incorrecta o genera ambigüedad con `FormEventEntity.java`. |

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Consolidación de Liquibase DDL para `form_event_store`

**Archivo:** `backend/ibpms-core/src/main/resources/db/changelog/40-us029-fix-form-event-store.sql`
**Archivo:** `backend/ibpms-core/src/main/resources/db/changelog/db.changelog-master.yaml`

Crea una migración explícita (agregada en el master yaml) que resuelva la ambigüedad, eliminando `ibpms_form_event_store` y garantizando `form_event_store`:

```sql
-- liquibase formatted sql
-- changeset backend:40-us029-fix-form-event-store
-- @Traceability: US-029, Gap B-J04-01

DROP TABLE IF EXISTS ibpms_form_event_store;

CREATE TABLE IF NOT EXISTS form_event_store (
    event_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type        VARCHAR(50)  NOT NULL,
    task_id           VARCHAR(255) NOT NULL,
    process_instance_id VARCHAR(255) NOT NULL,
    user_id           VARCHAR(255) NOT NULL,
    payload_json      JSONB        NOT NULL,
    schema_version    VARCHAR(10)  NOT NULL,
    idempotency_key   UUID         UNIQUE,
    original_event_id UUID,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_fes_task_id ON form_event_store(task_id);
CREATE INDEX IF NOT EXISTS idx_fes_process ON form_event_store(process_instance_id);
CREATE INDEX IF NOT EXISTS idx_fes_created ON form_event_store(created_at);
```

### Paso 2: Limpieza y Consolidación de Entidad JPA

**Archivo:** `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/FormEventStoreEntity.java`
**Acción:** Eliminar este archivo (DELETE) para evitar conflicto de beans/entities.

**Archivo:** `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/FormEventEntity.java`
**Acción:** Asegurar que sea la única entidad y apunte a `form_event_store`.

```java
// @Traceability: US-029, CA-16
package com.ibpms.poc.infrastructure.jpa.entity;

import com.ibpms.poc.domain.model.EventType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "form_event_store")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormEventEntity {
    @Id
    @Column(name = "event_id")
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "task_id")
    private String taskId;

    @Column(name = "process_instance_id")
    private String processInstanceId;

    @Column(name = "user_id")
    private String userId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json", columnDefinition = "jsonb")
    private String payloadJson;

    @Column(name = "idempotency_key", unique = true)
    private UUID idempotencyKey;

    @Column(name = "schema_version", nullable = false)
    private String schemaVersion;

    @Column(name = "original_event_id")
    private UUID originalEventId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;
}
```

### Paso 3: Crear/Actualizar Spring Data Repository

**Archivo:** `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/repository/FormEventRepository.java`

```java
// @Traceability: US-029, CA-16
package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.FormEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FormEventRepository extends JpaRepository<FormEventEntity, UUID> {
    List<FormEventEntity> findByTaskIdOrderByCreatedAtDesc(String taskId);
    Optional<FormEventEntity> findByIdempotencyKey(UUID idempotencyKey);
}
```

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | La tabla `form_event_store` existe en la BD y `ibpms_form_event_store` no | Liquibase corre exitosamente sin errores de schema |
| 2 | No existe ambigüedad de entidades JPA | `FormEventStoreEntity.java` fue eliminado |
| 3 | Repository está disponible para inyección | Compilación y arranque Spring Boot exitoso |
| 4 | Build exitoso | `mvn clean verify` o auditoría SRE |

## 🚦 SECUENCIA DE EJECUCIÓN

1. Crear migración `40-us029-fix-form-event-store.sql`.
2. Actualizar `db.changelog-master.yaml`.
3. Eliminar `FormEventStoreEntity.java`.
4. Validar `FormEventEntity.java`.
5. Crear `FormEventRepository.java`.
6. Compilar el backend para asegurar que no haya bean conflicts.
7. Commit: `git add . && git commit -m "fix(backend): resolver gap B-J04-01 con form_event_store (US-029)" && git push origin sprint-8/pm-01/us-029-form-exec`

## 📋 Instrucciones para Copiar y Pegar

```text
Asume el rol de ⚙️ BACKEND - JAVA.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat .agents/skills/backend_sre_compilation_audit/SKILL.md
3. cat .agents/skills/clean_code_standards/SKILL.md
4. cat .agents/skills/zero_mock_enforcement/SKILL.md
5. cat .agentic-sync/handoff_BACKEND_US029_B_J04_01.md

TU MISIÓN:

1. Crear la migración 40-us029-fix-form-event-store.sql y agregarla al master yaml.
2. Eliminar la entidad duplicada FormEventStoreEntity.java.
3. Consolidar FormEventEntity.java y FormEventRepository.java.
4. Build/Compile: Ejecutar auditoría SRE del backend (maven compile y bootRun temporal).
5. Commit: git add . && git commit -m "fix(backend): resolver gap B-J04-01 con form_event_store (US-029)" && git push origin sprint-8/pm-01/us-029-form-exec

REGLAS INQUEBRANTABLES:
- DEBES eliminar ibpms_form_event_store en la migración si existe, es basura.
- DEBES asegurarte de que el contexto de Spring cargue sin ConflictingBeanDefinitionException.
- Cero tolerancia a mocks o inyecciones falsas.
```
