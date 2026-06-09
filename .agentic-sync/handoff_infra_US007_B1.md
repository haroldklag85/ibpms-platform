# 🔧 Handoff Infra/BD — US-007 Bloque 1
> **Fecha:** 2026-05-03 | **Rama:** `sprint-6` | **Origen:** Auditoría Arquitectónica US-007
> **Agente destino:** Infra/BD | **Prioridad:** 🔴 ALTA (Paso 1 de 4 — sin esto, Backend no arranca)

---

## 1. Objetivo
Validar y completar la infraestructura de base de datos y mensajería necesaria para la remediación del módulo DMN (US-007). Asegurar que el esquema de PostgreSQL y la topología RabbitMQ soporten los 26 GAPs identificados en la auditoría arquitectónica.

## 2. Alineación Arquitectónica
- **ADR-009 (PostgreSQL):** Única base de datos permitida. Sin MongoDB ni ElasticSearch.
- **ADR-001 (Hexagonal):** Las migraciones BD van en Liquibase, nunca SQL directo en producción.
- **RabbitMQ:** Topología documentada en `docs/architecture/rabbitmq_topology.md`.

## 3. Tareas Específicas

### Tarea 1: Verificar Changeset 38 (`38-us007-dmn-manual-edit-schema.sql`)
- **Archivo:** `backend/ibpms-core/src/main/resources/db/changelog/38-us007-dmn-manual-edit-schema.sql`
- **Acción:** Confirmar que el changeset incluye:
  - Tabla `ibpms_dmn_definitions` con columnas: `id`, `name`, `decision_ref`, `xml_content`, `status` (DRAFT/ACTIVE/ARCHIVED), `hit_policy`, `version`, `author_hash`, `tenant_id`, `created_at`, `updated_at`.
  - Tabla `ibpms_dmn_drafts` con columnas: `id`, `user_id`, `prompt_hash`, `xml_content`, `created_at`, `expires_at` (TTL 24h).
  - Índices: UNIQUE en `(decision_ref, version, tenant_id)`, B-Tree en `status`, `tenant_id`.
  - Constraint: `expires_at` NOT NULL en `ibpms_dmn_drafts`.
- **Si falta algo:** Crear changeset complementario `39-us007-dmn-schema-complement.sql`.
- **Verificación:** Confirmar que `db.changelog-master.yaml` incluye el changeset en la secuencia correcta.

### Tarea 2: Verificar columna `source` en `ibpms_dmn_definitions`
- **CA-32:** El badge "Modificada Manualmente" requiere una columna `source` con valores posibles: `NLP`, `XML_UPLOAD`, `MANUAL`, `NLP_MODIFIED`.
- **Acción:** Si la columna no existe, agregarla en changeset complementario:
  ```sql
  ALTER TABLE ibpms_dmn_definitions ADD COLUMN source VARCHAR(20) DEFAULT 'NLP' NOT NULL;
  ```

### Tarea 3: Provisionar Exchange RabbitMQ para `FORM_SCHEMA_CHANGED` (CA-16)
- **Acción:** Verificar en `rabbitmq_topology.md` si existe un exchange para eventos de mutación de formularios.
- **Si NO existe:** Documentar la topología necesaria:
  - Exchange: `ibpms.events.forms` (tipo: `topic`)
  - Routing Key: `form.schema.changed.{form_id}`
  - Queue para DMN consumer: `ibpms.dmn.form-schema-invalidation`
  - Dead Letter Exchange: `ibpms.dlx.forms`
- **NO implementar el consumer** (eso es responsabilidad del Backend). Solo provisionar la topología.

### Tarea 4: Verificar tabla `ibpms_dmn_audit_log`
- **CA-05 (PII) y CA-32 (Trazabilidad):** Confirmar existencia de tabla de auditoría para el módulo DMN.
- **Columnas requeridas:** `id`, `dmn_id`, `action` (CREATE/UPDATE/PUBLISH/ROLLBACK/ARCHIVE), `author_hash`, `source_badge`, `tenant_id`, `created_at`, `details_json`.
- **Si no existe:** Crear en changeset complementario.

## 4. Entregables Esperados
- [ ] Confirmación de que changeset 38 cubre el esquema completo.
- [ ] Changeset complementario (si aplica) con columnas/tablas faltantes.
- [ ] Topología RabbitMQ documentada para `FORM_SCHEMA_CHANGED`.
- [ ] Compilación limpia del backend (`mvn compile` exit 0).

## 5. Restricciones
- **PROHIBIDO:** Crear tablas fuera de Liquibase.
- **PROHIBIDO:** Usar SQL directo en producción.
- **PROHIBIDO:** Modificar changelogs ya aplicados (inmutabilidad de Liquibase).

---

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_infra_US007.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_infra_US007.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama `sprint-6`. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).

> **Validación de esquema obligatoria:** Ejecuta verificaciones de sintaxis Liquibase o configuraciones docker-compose antes de hacer push.
