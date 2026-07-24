-- liquibase formatted sql
-- changeset backend-agent:059-us002-add-tenant-id context:!test
-- comment: CA-15: Agrega columna tenant_id a ibpms_agile_tasks para habilitar ghost timeout per-tenant (ADR-009).
ALTER TABLE ibpms_agile_tasks ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) DEFAULT 'default';
