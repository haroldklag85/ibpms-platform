--liquibase formatted sql
--changeset architect:us025-impersonation-correlation-id

ALTER TABLE ibpms_impersonation_audit_log ADD COLUMN correlation_id VARCHAR(100);

COMMENT ON COLUMN ibpms_impersonation_audit_log.correlation_id IS 'UUID de correlación para trazabilidad transaccional E2E';
