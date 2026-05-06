-- liquibase formatted sql
-- changeset david:36-us036-ca12-ca16-reports

-- CA-16: Tabla para Auditoría de Reportes ISO 27001
CREATE TABLE ibpms_audit_reports (
    id UUID PRIMARY KEY,
    report_type VARCHAR(50) NOT NULL,
    generated_by VARCHAR(100) NOT NULL,
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    file_hash VARCHAR(64) NOT NULL,
    metadata_json JSONB
);

-- CA-15: Flag para Trámites Públicos
ALTER TABLE ibpms_bpmn_process_design ADD COLUMN is_public BOOLEAN DEFAULT FALSE;

-- CA-14: Tabla para Blacklist (Fallback DB si Redis falla - Fail-Open Policy)
-- Nota: La lógica primaria usará Redis, pero persistimos aquí para auditoría histórica
CREATE TABLE ibpms_security_token_blacklist (
    id UUID PRIMARY KEY,
    token_signature VARCHAR(64) NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    revoked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    reason TEXT
);

CREATE INDEX idx_blacklist_signature ON ibpms_security_token_blacklist(token_signature);
CREATE INDEX idx_blacklist_user_id ON ibpms_security_token_blacklist(user_id);
