-- Liquibase formatted SQL
-- changeset antigravity:006_create_mq_resilience_tables

-- CREATE TABLE ibpms_queue_fallback (
--     id UUID PRIMARY KEY,
--     message_body TEXT NOT NULL,
--     target_queue VARCHAR(100) NOT NULL,
--     headers_json TEXT,
--     created_at TIMESTAMP NOT NULL
-- );

CREATE TABLE ibpms_system_audit_log (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    action VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    message_count INT
);

-- rollback DROP TABLE ibpms_system_audit_log;
-- rollback DROP TABLE ibpms_queue_fallback;
