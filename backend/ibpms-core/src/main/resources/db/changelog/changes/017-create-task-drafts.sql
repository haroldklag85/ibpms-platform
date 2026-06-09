-- liquibase formatted sql
-- changeset liquibase:017-create-task-drafts
-- @Traceability: US-017, CA-07, CA-09
CREATE TABLE ibpms_task_drafts (
    task_id VARCHAR(100) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    current_step INT DEFAULT 1,
    partial_data JSONB NOT NULL,
    schema_version VARCHAR(20) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);
