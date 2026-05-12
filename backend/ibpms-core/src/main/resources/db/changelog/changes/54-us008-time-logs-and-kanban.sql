-- liquibase formatted sql

-- changeset antigravity:54-us008-time-logs-and-kanban
-- comment: Tablas y columnas de Time Logs y Bloqueo Kanban para US-008 V2
-- @Traceability: US-008 - CA-01, CA-03

-- GAP CA-03: Time Logs (Append-Only)
CREATE TABLE ibpms_time_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reference_type VARCHAR(50) NOT NULL,
    reference_id VARCHAR(100) NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    effort_seconds INTEGER NOT NULL DEFAULT 0,
    action VARCHAR(20) NOT NULL,
    recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_time_logs_ref ON ibpms_time_logs (reference_type, reference_id);

-- GAP CA-01: Blocked Reason
ALTER TABLE ibpms_kanban_item
    ADD COLUMN block_reason TEXT;
