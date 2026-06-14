--liquibase formatted sql
--changeset antigravity:24-create-audit-log-table

-- @Traceability: US-038, CA-17 (Traza Indeleble de operaciones del sistema)
-- Tabla central de auditoría del sistema. Referenciada por AuditLogJdbcAdapter.
-- NOTA: La migración 25-us028-qa-certification-columns.sql hace ALTER sobre esta tabla,
-- por tanto DEBE ejecutarse después de este changeset.
CREATE TABLE IF NOT EXISTS ibpms_audit_log (
    id              VARCHAR(36)  NOT NULL,
    entity_type     VARCHAR(100) NOT NULL,
    entity_id       VARCHAR(100),
    event_type      VARCHAR(100) NOT NULL,
    performed_by    VARCHAR(255),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    payload_snapshot BYTEA,
    is_compressed   BOOLEAN      NOT NULL DEFAULT FALSE,
    truncated       BOOLEAN      NOT NULL DEFAULT FALSE,
    details         JSONB,
    CONSTRAINT pk_ibpms_audit_log PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_audit_log_entity_type ON ibpms_audit_log(entity_type);
CREATE INDEX IF NOT EXISTS idx_audit_log_event_type  ON ibpms_audit_log(event_type);
CREATE INDEX IF NOT EXISTS idx_audit_log_performed_by ON ibpms_audit_log(performed_by);
CREATE INDEX IF NOT EXISTS idx_audit_log_created_at  ON ibpms_audit_log(created_at DESC);
