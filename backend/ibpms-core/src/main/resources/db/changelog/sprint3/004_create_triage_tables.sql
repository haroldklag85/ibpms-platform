-- Liquibase Changelog: sprint3/004_create_triage_tables.sql
-- changeset architect:sprint3-004-triage

CREATE TABLE ibpms_triage_tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    camunda_process_instance_id VARCHAR(255) NOT NULL,
    message_id VARCHAR(255) NOT NULL,
    sender_email VARCHAR(255) NOT NULL,
    subject VARCHAR(500),
    attachment_count INTEGER DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    rejection_reason TEXT,
    sla_deadline TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_triage_tasks_status ON ibpms_triage_tasks(status);
CREATE INDEX idx_triage_tasks_process_instance_id ON ibpms_triage_tasks(camunda_process_instance_id);

-- Para purgas masivas (CA-13)
CREATE INDEX idx_triage_tasks_updated_at ON ibpms_triage_tasks(updated_at);
