-- liquibase formatted sql
-- changeset infra-agent:45-us036-ca23-ca28-infra

-- ============================================================
-- US-036 CA-23/CA-24: Saneamiento de esquema ibpms_audit_reports
-- Elimina columnas orphan no mapeadas por AuditReportEntity.java
-- y añade índices de rendimiento para consultas de comparativa.
-- ============================================================

-- 1. Eliminar columna orphan 'requested_by' (creada por changeset 20, no mapeada por JPA)
ALTER TABLE ibpms_audit_reports DROP COLUMN IF EXISTS requested_by;

-- 2. Eliminar columna orphan 'content_hash' (creada por changeset 20, reemplazada por 'file_hash' en changeset 36)
ALTER TABLE ibpms_audit_reports DROP COLUMN IF EXISTS content_hash;

-- 3. Índice para consultas de comparativa entre periodos (CA-24: "Estado de permisos en Enero vs Febrero")
CREATE INDEX IF NOT EXISTS idx_audit_reports_generated_at ON ibpms_audit_reports(generated_at);

-- 4. Índice para búsquedas de verificación de integridad SHA-256
CREATE INDEX IF NOT EXISTS idx_audit_reports_file_hash ON ibpms_audit_reports(file_hash);

-- rollback ALTER TABLE ibpms_audit_reports ADD COLUMN IF NOT EXISTS requested_by VARCHAR(100);
-- rollback ALTER TABLE ibpms_audit_reports ADD COLUMN IF NOT EXISTS content_hash VARCHAR(255);
-- rollback DROP INDEX IF EXISTS idx_audit_reports_generated_at;
-- rollback DROP INDEX IF EXISTS idx_audit_reports_file_hash;
