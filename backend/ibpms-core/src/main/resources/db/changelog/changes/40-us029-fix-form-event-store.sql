-- liquibase formatted sql
-- changeset backend:40-us029-fix-form-event-store
-- @Traceability: US-029, Gap B-J04-01

DROP TABLE IF EXISTS ibpms_form_event_store;

CREATE TABLE IF NOT EXISTS form_event_store (
    event_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type        VARCHAR(50)  NOT NULL,
    task_id           VARCHAR(255) NOT NULL,
    process_instance_id VARCHAR(255) NOT NULL,
    user_id           VARCHAR(255) NOT NULL,
    payload_json      JSONB        NOT NULL,
    schema_version    VARCHAR(10)  NOT NULL,
    idempotency_key   UUID         UNIQUE,
    original_event_id UUID,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_fes_task_id ON form_event_store(task_id);
CREATE INDEX IF NOT EXISTS idx_fes_process ON form_event_store(process_instance_id);
CREATE INDEX IF NOT EXISTS idx_fes_created ON form_event_store(created_at);
