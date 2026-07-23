-- liquibase formatted sql

-- changeset antigravity:37-us002-claim-audit-log-orphans
-- comment: Ampliación de claim_audit_log y creación de ibpms_orphaned_attachments para US-002

ALTER TABLE claim_audit_log
    ADD COLUMN user_id VARCHAR(100),
    ADD COLUMN previous_assignee VARCHAR(100),
    ADD COLUMN reason TEXT,
    ADD COLUMN message TEXT;

CREATE TABLE ibpms_orphaned_attachments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id VARCHAR(100) NOT NULL,
    file_reference VARCHAR(500) NOT NULL,
    uploaded_by VARCHAR(100) NOT NULL,
    orphaned_at TIMESTAMPTZ DEFAULT now(),
    purged BOOLEAN DEFAULT false
);

CREATE INDEX idx_orphan_cleanup ON ibpms_orphaned_attachments (orphaned_at, purged);
