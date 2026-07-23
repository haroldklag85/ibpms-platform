-- liquibase formatted sql
-- changeset liquibase:015-create-tenant-config
-- @Traceability: US-004, CA-18

CREATE TABLE ibpms_tenant_config (
    tenant_id VARCHAR(50) PRIMARY KEY,
    webhook_sla_hours INT NOT NULL DEFAULT 48,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- rollback DROP TABLE ibpms_tenant_config;
