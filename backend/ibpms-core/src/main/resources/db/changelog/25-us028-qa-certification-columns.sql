--liquibase formatted sql
--changeset antigravity:25-us028-qa-certification-columns

-- CA-12: Columnas de certificación QA
ALTER TABLE ibpms_form_definitions ADD COLUMN is_qa_certified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE ibpms_form_definitions ADD COLUMN certified_schema_hash VARCHAR(64);
ALTER TABLE ibpms_form_definitions ADD COLUMN certified_by VARCHAR(100);
ALTER TABLE ibpms_form_definitions ADD COLUMN certified_at TIMESTAMP;

-- CA-15: Columna de payload snapshot comprimido en audit_log
ALTER TABLE ibpms_audit_log ADD COLUMN IF NOT EXISTS payload_snapshot BYTEA;
ALTER TABLE ibpms_audit_log ADD COLUMN IF NOT EXISTS is_compressed BOOLEAN DEFAULT FALSE;
ALTER TABLE ibpms_audit_log ADD COLUMN IF NOT EXISTS truncated BOOLEAN DEFAULT FALSE;
ALTER TABLE ibpms_audit_log ADD COLUMN IF NOT EXISTS details JSONB;
