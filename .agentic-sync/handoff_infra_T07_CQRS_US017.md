# 🧠→🗄️ Handoff: Arquitecto Líder → Infra / DB
# T-07: Implementación de Esquemas CQRS y Event Sourcing (US-017)

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** 🗄️ INFRA / DB
**Fecha:** 2026-05-12T09:30:00-05:00
**Sprint:** 7 — Sprint 7.1
**Prioridad:** 🔴 Alta
**Dependencia:** Ninguna

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
cat .cursorrules

# 2. Skill principal del agente receptor (Ficticio para Infra, si existe usar el pertinente, sino usar el genérico)
cat .agents/skills/clean_code_standards/SKILL.md

# 3. Skills transversales aplicables
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. ADRs relevantes
cat docs/architecture/adr_011_local_cqrs_v1.md
cat docs/requirements/epics/epic_A_motor_core.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar la anotación @Traceability o comentario `-- @Traceability: US-XXX, CA-XX`. Esto es INNEGOCIABLE.

---

## 🔬 Diagnóstico del Arquitecto

Actualmente el sistema carece del soporte persistente para la separación de lecturas y escrituras definida en el ADR-011 (Local CQRS) para la US-017. El motor de formularios persiste directamente en Camunda, pero no existe un Event Store inmutable ni soporte de borradores asíncronos.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Ausencia de Event Store | `src/main/resources/db/changelog/changes/` | No existe DDL para `form_event_store`, bloqueando CA-06 de US-017. |
| Ausencia de Drafts | `src/main/resources/db/changelog/changes/` | No existe tabla `task_drafts` para borradores, bloqueando CA-07. |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Crear el changelog del Event Store

**Archivo:** `ibpms-platform/backend/ibpms-core/src/main/resources/db/changelog/changes/016-create-cqrs-event-store.sql`

Crea la tabla `ibpms_form_event_store` para soportar el patrón Append-Only.

```sql
-- @Traceability: US-017, CA-06
CREATE TABLE ibpms_form_event_store (
    event_id UUID PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL, 
    task_id VARCHAR(100) NOT NULL,
    process_instance_id VARCHAR(100) NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    payload_json JSONB NOT NULL,
    schema_version VARCHAR(20) NOT NULL,
    idempotency_key UUID UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

CREATE INDEX idx_event_task ON ibpms_form_event_store(task_id);
CREATE INDEX idx_event_pii ON ibpms_form_event_store(process_instance_id);
```

### Paso 2: Crear el changelog para Task Drafts

**Archivo:** `ibpms-platform/backend/ibpms-core/src/main/resources/db/changelog/changes/017-create-task-drafts.sql`

```sql
-- @Traceability: US-017, CA-07, CA-09
CREATE TABLE ibpms_task_drafts (
    task_id VARCHAR(100) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    current_step INT DEFAULT 1,
    partial_data JSONB NOT NULL,
    schema_version VARCHAR(20) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);
```

### Paso 3: Registrar changelogs en el master

**Archivo:** `ibpms-platform/backend/ibpms-core/src/main/resources/db/changelog/db.changelog-master.yaml`

```yaml
databaseChangeLog:
  - include:
      file: db/changelog/changes/016-create-cqrs-event-store.sql
  - include:
      file: db/changelog/changes/017-create-task-drafts.sql
```

---

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Tabla `ibpms_form_event_store` creada con índices | `SELECT * FROM ibpms_form_event_store LIMIT 1;` retorna o falla limpiamente si vacía, y tabla existe. |
| 2 | Tabla `ibpms_task_drafts` creada | `SELECT * FROM ibpms_task_drafts LIMIT 1;` funciona. |
| 3 | Trazabilidad Inversa Cumplida | `grep -r "@Traceability: US-017" src/main/resources/db/changelog/changes/` -> 2 resultados. |
| 4 | Construcción Exitosa | `mvn clean compile` pasa sin errores de parseo de Liquibase en startup. |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Crea el archivo `016-create-cqrs-event-store.sql`
2. Crea el archivo `017-create-task-drafts.sql`
3. Modifica `db.changelog-master.yaml`
4. Ejecuta el build: `cd ibpms-platform/backend/ibpms-core && mvn spring-boot:run` para validar que la DB local levante.
5. Commit: `git add . && git commit -m "feat(infra): create tables form_event_store and task_drafts for US-017" && git push`

---

## 📋 Instrucciones para Copiar y Pegar

```
Asume el rol de 🗄️ INFRA / DB.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat ibpms-platform/.cursorrules
2. cat ibpms-platform/.agents/skills/clean_code_standards/SKILL.md
3. cat ibpms-platform/.agents/skills/zero_mock_enforcement/SKILL.md
4. cat ibpms-platform/docs/architecture/adr_011_local_cqrs_v1.md
5. cat ibpms-platform/.agentic-sync/handoff_infra_T07_CQRS_US017.md

TU MISIÓN:

1. Crea `016-create-cqrs-event-store.sql` con la definición del Event Store (usando JSONB para payloads).
2. Crea `017-create-task-drafts.sql` para el almacenamiento de borradores asíncronos.
3. Añade ambos archivos al master changelog.
4. Build/Compile: `cd ibpms-platform/backend/ibpms-core && mvn spring-boot:run`
5. Commit: `git add . && git commit -m "feat(infra): create form_event_store and task_drafts schema for US-017" && git push`

REGLAS INQUEBRANTABLES:
- DEBES incluir `-- @Traceability: US-017, CA-06` (o similar) en los scripts SQL.
- DEBES usar el tipo `JSONB` para `payload_json` y `partial_data` garantizando flexibilidad de esquema.
- PROHIBIDO usar sentencias destructivas (`DROP TABLE`), solo migraciones aditivas.
```
