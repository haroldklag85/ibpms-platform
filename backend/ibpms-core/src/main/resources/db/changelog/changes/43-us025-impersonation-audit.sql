--liquibase formatted sql
--changeset architect:us025-impersonation-audit-create

CREATE TABLE ibpms_impersonation_audit_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    admin_id        UUID NOT NULL,
    target_user_id  UUID NOT NULL,
    action          VARCHAR(20) NOT NULL CHECK (action IN ('START', 'EXIT', 'TIMEOUT', 'REVOKED')),
    ip_address      VARCHAR(45),
    user_agent      VARCHAR(500),
    metadata        JSONB DEFAULT '{}'::jsonb,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_impersonation_admin ON ibpms_impersonation_audit_log(admin_id);
CREATE INDEX idx_impersonation_target ON ibpms_impersonation_audit_log(target_user_id);
CREATE INDEX idx_impersonation_created ON ibpms_impersonation_audit_log(created_at DESC);

COMMENT ON TABLE ibpms_impersonation_audit_log IS 'US-025 CA-31: Registro inmutable de eventos de impersonación. Zero-Delete policy.';
COMMENT ON COLUMN ibpms_impersonation_audit_log.action IS 'Tipo de evento: START=admin inicia impersonación, EXIT=admin sale voluntariamente, TIMEOUT=JWT 30min expiró, REVOKED=admin forzó cierre desde otro dispositivo';
COMMENT ON COLUMN ibpms_impersonation_audit_log.admin_id IS 'UUID del SUPER_ADMIN que ejecutó la impersonación';
COMMENT ON COLUMN ibpms_impersonation_audit_log.target_user_id IS 'UUID del usuario que fue impersonado';
COMMENT ON COLUMN ibpms_impersonation_audit_log.metadata IS 'Datos adicionales: roles del target al momento, motivo, etc.';
