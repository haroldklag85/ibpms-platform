--liquibase formatted sql
--changeset hb-dev:6-create-task-audit-log

CREATE TABLE ibpms_task_audit_log (
    id BINARY(16) NOT NULL,
    task_id VARCHAR(64) NOT NULL,
    action VARCHAR(50) NOT NULL,
    username VARCHAR(100) NOT NULL,
    target_username VARCHAR(100),
    reason TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_task_audit_log PRIMARY KEY (id)
);

CREATE INDEX idx_task_audit_log_task_id ON ibpms_task_audit_log(task_id);
CREATE INDEX idx_task_audit_log_action ON ibpms_task_audit_log(action);
