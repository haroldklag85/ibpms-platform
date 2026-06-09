-- liquibase formatted sql
-- changeset liquibase:016-create-cqrs-event-store
-- @Traceability: US-017, CA-06
CREATE TABLE ibpms_form_event_store (
    event_id UUID PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL, 
    task_id VARCHAR(100) NOT NULL,
    process_instance_id VARCHAR(100) NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    payload_json JSONB NOT NULL,
    schema_version VARCHAR(20) NOT NULL,
    idempotency_key UUID UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

CREATE INDEX idx_event_task ON ibpms_form_event_store(task_id);
CREATE INDEX idx_event_pii ON ibpms_form_event_store(process_instance_id);
