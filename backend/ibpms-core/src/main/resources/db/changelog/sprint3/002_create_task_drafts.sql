-- Liquibase Changelog: sprint3/002_create_task_drafts.sql
-- changeset architect:sprint3-002-task-drafts

CREATE TABLE task_drafts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id         VARCHAR(255) NOT NULL,
    user_id         VARCHAR(255) NOT NULL,
    current_step    INTEGER,
    partial_data    JSONB        NOT NULL,
    schema_version  VARCHAR(10)  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_task_draft_per_user UNIQUE (task_id, user_id)
);

COMMENT ON TABLE task_drafts IS 'Snapshots efímeros de borradores (US-017 CA-07). TTL 72h. Sobrescribibles.';
