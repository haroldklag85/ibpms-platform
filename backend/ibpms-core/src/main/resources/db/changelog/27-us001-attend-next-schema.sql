-- liquibase formatted sql
-- changeset system:27-us001-attend-next context:us001
-- 27-us001-attend-next-schema.sql

-- Feature Toggles Table
CREATE TABLE IF NOT EXISTS ibpms_feature_toggles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    toggle_key VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT false,
    changed_by VARCHAR(100) NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_toggle_tenant_key UNIQUE (tenant_id, toggle_key)
);

-- Task Skip Audit Log
CREATE TABLE IF NOT EXISTS ibpms_task_skips (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    task_id VARCHAR(100) NOT NULL,
    skip_reason VARCHAR(50) NOT NULL,
    skip_reason_detail TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_task_skips_user ON ibpms_task_skips (user_id, created_at DESC);

-- Category tag en la proyección (para match de skills)
ALTER TABLE ibpms_workdesk_projection
    ADD COLUMN IF NOT EXISTS category_tag VARCHAR(100);
