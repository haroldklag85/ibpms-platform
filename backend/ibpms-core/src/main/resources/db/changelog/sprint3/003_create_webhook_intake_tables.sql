-- liquibase formatted sql
-- changeset architect:sprint3-003-webhook-intake

CREATE TABLE ibpms_webhook_transactions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id      VARCHAR(500) NOT NULL UNIQUE,
    sender_email    VARCHAR(500) NOT NULL,
    sender_domain   VARCHAR(255) NOT NULL,
    subject         VARCHAR(1000),
    payload_hash    VARCHAR(128),
    status          VARCHAR(30)  NOT NULL DEFAULT 'RECEIVED',
    rejection_reason VARCHAR(100),
    camunda_process_instance_id VARCHAR(255),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_wt_message_id ON ibpms_webhook_transactions(message_id);
CREATE INDEX idx_wt_status ON ibpms_webhook_transactions(status);
CREATE INDEX idx_wt_created ON ibpms_webhook_transactions(created_at);

CREATE TABLE ibpms_orphan_payloads (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    raw_payload     JSONB,
    error_type      VARCHAR(50)  NOT NULL,
    file_hash_sha256 VARCHAR(64),
    file_name       VARCHAR(255),
    file_size_bytes BIGINT,
    sender_email    VARCHAR(500),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_op_error_type ON ibpms_orphan_payloads(error_type);
CREATE INDEX idx_op_created ON ibpms_orphan_payloads(created_at);

CREATE TABLE ibpms_webhook_allowed_domains (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    domain          VARCHAR(255) NOT NULL,
    tenant_id       VARCHAR(255) NOT NULL,
    description     VARCHAR(500),
    created_by      VARCHAR(255) NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_domain_per_tenant UNIQUE (domain, tenant_id)
);

CREATE INDEX idx_wad_domain ON ibpms_webhook_allowed_domains(domain) WHERE is_active = TRUE;

COMMENT ON TABLE ibpms_webhook_transactions IS 'Registro transaccional de webhooks entrantes con idempotencia (US-004 CA-1).';
COMMENT ON TABLE ibpms_orphan_payloads IS 'Payloads rechazados/malformados/en cuarentena (US-004 CA-3, CA-11).';
COMMENT ON TABLE ibpms_webhook_allowed_domains IS 'Lista blanca de dominios autorizados por tenant (US-004 CA-4, CA-12).';
