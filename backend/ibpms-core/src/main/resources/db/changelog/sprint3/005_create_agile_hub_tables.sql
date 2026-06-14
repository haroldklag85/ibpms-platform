/* // @Traceability: US-005, CA-42 - Activity Timeline */
-- Liquibase Changelog: sprint3/005_create_agile_hub_tables.sql
-- changeset architect:sprint3-005-agile-hub

CREATE TABLE ibpms_agile_projects (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(200)  NOT NULL,
    description     TEXT,
    methodology     VARCHAR(20)   NOT NULL DEFAULT 'KANBAN_CONTINUOUS', -- Solo KANBAN en V1 (CA-1)
    status          VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE | CLOSED
    created_by      VARCHAR(255)  NOT NULL,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    closed_at       TIMESTAMPTZ,
    closed_by       VARCHAR(255),
    max_active_tasks INTEGER      NOT NULL DEFAULT 500  -- Límite rígido V1 (CA-11)
);

CREATE TABLE ibpms_agile_tasks (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id      UUID         NOT NULL REFERENCES ibpms_agile_projects(id),
    title           VARCHAR(300) NOT NULL,
    description     TEXT,          -- Sanitizado contra XSS (CA-11)
    effort_estimated NUMERIC(8,2),  -- Horas (CA-3)
    effort_actual   NUMERIC(8,2),  -- Horas reales (CA-3)
    notes           TEXT,
    status          VARCHAR(20)  NOT NULL DEFAULT 'TODO', -- TODO | IN_PROGRESS | DONE | CANCELLED | DELETED
    position        INTEGER      NOT NULL DEFAULT 0,      -- Orden vertical (CA-6)
    sla_deadline    TIMESTAMPTZ,
    last_activity_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),  -- Para detección de "Ticket Rancio" (CA-13)
    created_by      VARCHAR(255) NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE ibpms_agile_task_assignees (
    task_id         UUID         NOT NULL REFERENCES ibpms_agile_tasks(id) ON DELETE CASCADE,
    user_id         VARCHAR(255) NOT NULL,
    assigned_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    PRIMARY KEY (task_id, user_id)
);

CREATE TABLE ibpms_agile_tags (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id      UUID         NOT NULL REFERENCES ibpms_agile_projects(id),
    name            VARCHAR(50)  NOT NULL,
    color_hex       VARCHAR(7)   NOT NULL DEFAULT '#6366f1',  -- Color ad-hoc (CA-3, CA-12)
    created_by      VARCHAR(255) NOT NULL,
    CONSTRAINT uq_tag_per_project UNIQUE (project_id, name)
);

CREATE TABLE ibpms_agile_task_tags (
    task_id         UUID NOT NULL REFERENCES ibpms_agile_tasks(id) ON DELETE CASCADE,
    tag_id          UUID REFERENCES ibpms_agile_tags(id),
    tag             VARCHAR(50) NOT NULL,
    PRIMARY KEY (task_id, tag)
);

CREATE TABLE ibpms_agile_sla_changelog (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id         UUID         NOT NULL REFERENCES ibpms_agile_tasks(id),
    previous_value  TIMESTAMPTZ,
    new_value       TIMESTAMPTZ,
    changed_by      VARCHAR(255) NOT NULL,
    changed_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_agt_project ON ibpms_agile_tasks(project_id, status);
CREATE INDEX idx_agt_position ON ibpms_agile_tasks(project_id, position);
CREATE INDEX idx_agt_activity ON ibpms_agile_tasks(last_activity_at);
