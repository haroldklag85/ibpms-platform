-- liquibase formatted sql

-- changeset ibpms:11-create-sac-mailbox-tables
CREATE TABLE ibpms_sac_mailbox (
    id VARCHAR(36) PRIMARY KEY,
    alias VARCHAR(255) NOT NULL UNIQUE,
    tenant_id VARCHAR(255) NOT NULL,
    client_id VARCHAR(255) NOT NULL,
    key_vault_ref_id VARCHAR(255) NOT NULL,
    protocol VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    default_bpmn_process VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sac_mailbox_protocol ON ibpms_sac_mailbox(protocol);
CREATE INDEX idx_sac_mailbox_status ON ibpms_sac_mailbox(is_active);
