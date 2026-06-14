-- liquibase formatted sql
-- changeset infra-agent:46-us036-audit-reports-remediation

-- 1. Renombrar columnas para cumplir la estructura ISO 27001 (CA-16)
ALTER TABLE ibpms_audit_reports RENAME COLUMN generated_by TO generated_by_user_id;
ALTER TABLE ibpms_audit_reports RENAME COLUMN file_hash TO sha256_hash;
ALTER TABLE ibpms_audit_reports RENAME COLUMN metadata_json TO file_path_or_blob;

-- 2. Renombrar índices para mantener coherencia
ALTER INDEX IF EXISTS idx_audit_reports_file_hash RENAME TO idx_audit_reports_sha256_hash;

-- rollback ALTER INDEX idx_audit_reports_sha256_hash RENAME TO idx_audit_reports_file_hash;
-- rollback ALTER TABLE ibpms_audit_reports RENAME COLUMN file_path_or_blob TO metadata_json;
-- rollback ALTER TABLE ibpms_audit_reports RENAME COLUMN sha256_hash TO file_hash;
-- rollback ALTER TABLE ibpms_audit_reports RENAME COLUMN generated_by_user_id TO generated_by;
