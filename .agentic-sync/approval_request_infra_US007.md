# 🏛️ Solicitud de Aprobación de Plan: Infraestructura US-007 Bloque 1

Arquitecto Líder, he finalizado el modo `PLANNING` para el Handoff del Bloque 1 de la US-007.

## Resumen del Plan Propuesto:

1. **Changeset Complementario (Tareas 1, 2 y 4):**
   Dado que el changeset `38` original no incluía la estructura base sino una mutación (ALTER TABLE), crearé un nuevo script **`41-us007-dmn-schema-complement.sql`** (las secuencias 39 y 40 fueron consumidas por US-029). En este archivo se aprovisionarán:
   - `ibpms_dmn_definitions`: Esquema completo incluyendo la columna requerida `source VARCHAR(20) DEFAULT 'NLP' NOT NULL`.
   - `ibpms_dmn_drafts`: Esquema efímero incluyendo TTL (`expires_at NOT NULL`).
   - `ibpms_dmn_audit_log`: Esquema de auditoría DMN (CA-05/CA-32).
   - Se aplicarán los índices `UNIQUE` y condicionales solicitados en `status`, `tenant_id` y `decision_ref`.

2. **Topología RabbitMQ (Tarea 3):**
   El archivo actual `rabbitmq_topology.md` no contiene la definición del Exchange de Formularios. Se procederá a documentar la siguiente topología:
   - **Exchange:** `ibpms.events.forms` (topic)
   - **Queue:** `ibpms.dmn.form-schema-invalidation`
   - **DLX:** `ibpms.dlx.forms`
   - **Routing Key:** `form.schema.changed.{form_id}`

3. **Validación:**
   - Registraré el changeset `41` al final de `db.changelog-master.yaml`.
   - Compilaré el proyecto invocando `mvn compile` en el contenedor docker `ibpms-core-dev`.

Solicito autorización para transicionar a modo `EXECUTION`.
