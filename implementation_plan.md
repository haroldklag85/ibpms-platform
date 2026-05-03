# Plan de Implementación: Infraestructura/DB US-007 Bloque 1

## Análisis de Tareas

1. **Esquema DMN (Tarea 1 y 2)**
   - Tras revisar el historial y los changelogs, el changeset `38` actual no incluye la creación de las tablas base, solo ejecutó un `ALTER TABLE`.
   - Se creará un nuevo changeset `41-us007-dmn-schema-complement.sql` (el 39 y 40 fueron utilizados en la US-029).
   - Este changeset incluirá:
     - `CREATE TABLE IF NOT EXISTS ibpms_dmn_definitions` con las columnas requeridas más la columna `source VARCHAR(20) DEFAULT 'NLP' NOT NULL`.
     - `CREATE TABLE IF NOT EXISTS ibpms_dmn_drafts` con las columnas requeridas y el `expires_at NOT NULL`.
     - Índices: UNIQUE en `(decision_ref, version, tenant_id)`, B-Tree en `status`, `tenant_id`.

2. **Topología RabbitMQ (Tarea 3)**
   - El archivo `docs/architecture/rabbitmq_topology.md` no contiene actualmente la topología de mutación de formularios.
   - Se actualizará este documento para añadir las filas correspondientes al Exchange `ibpms.events.forms`, Routing Key `form.schema.changed.{form_id}` y Queue `ibpms.dmn.form-schema-invalidation` ligada al DLX `ibpms.dlx.forms`.

3. **Auditoría DMN (Tarea 4)**
   - El esquema actual no cuenta con la tabla `ibpms_dmn_audit_log`.
   - Se agregará la creación de esta tabla en el changeset `41-us007-dmn-schema-complement.sql`.

4. **Validación de Compilación**
   - Se modificará el archivo `db.changelog-master.yaml` para incluir `41-us007-dmn-schema-complement.sql`.
   - Se ejecutará `docker exec ibpms-core-dev mvn compile` para certificar la validez de los cambios en la BD y código fuente.

## Archivos a Crear / Modificar
- `backend/ibpms-core/src/main/resources/db/changelog/41-us007-dmn-schema-complement.sql`
- `backend/ibpms-core/src/main/resources/db/changelog/db.changelog-master.yaml`
- `docs/architecture/rabbitmq_topology.md`
