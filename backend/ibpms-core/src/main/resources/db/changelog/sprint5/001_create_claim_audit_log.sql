-- liquibase formatted sql
-- changeset antigravity:sprint5-001
CREATE TABLE claim_audit_log (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL,
    supervisor_id VARCHAR(255) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    tenant_id VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP NOT NULL
);
CREATE INDEX idx_claim_audit_log_task_id ON claim_audit_log(task_id);
-- rollback DROP TABLE claim_audit_log;
