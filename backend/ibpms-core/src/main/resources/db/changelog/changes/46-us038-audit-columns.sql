--liquibase formatted sql
--changeset architect:us038-audit-columns

ALTER TABLE ibpms_system_audit_log ADD COLUMN IF NOT EXISTS correlation_id VARCHAR(100);
ALTER TABLE ibpms_system_audit_log ADD COLUMN IF NOT EXISTS active_roles_json JSONB;

COMMENT ON COLUMN ibpms_system_audit_log.correlation_id IS 'US-038: ID Transaccional de Red inyectado por Axios';
COMMENT ON COLUMN ibpms_system_audit_log.active_roles_json IS 'US-038: Estructura JSON para respaldar roles activos en el momento del log';
