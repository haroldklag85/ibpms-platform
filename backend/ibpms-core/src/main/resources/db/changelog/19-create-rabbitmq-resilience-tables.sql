-- liquibase formatted sql
-- changeset dev:19-rabbitmq-resilience splitStatements:true endDelimiter:;
-- US-034: RabbitMQ Resilience Tables

CREATE TABLE ibpms_processed_messages (
    idempotency_key VARCHAR(36) PRIMARY KEY,
    processed_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    queue_name      VARCHAR(100) NOT NULL
);

CREATE TABLE ibpms_dlq_archive (
    message_id     VARCHAR(36) PRIMARY KEY,
    original_queue VARCHAR(100),
    headers_json   TEXT,
    body_summary   VARCHAR(1024),
    archived_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE ibpms_queue_fallback (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_body    TEXT NOT NULL,
    target_queue    VARCHAR(100) NOT NULL,
    headers_json    TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);
