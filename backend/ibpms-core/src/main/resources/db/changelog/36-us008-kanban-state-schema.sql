-- liquibase formatted sql

-- changeset antigravity:36-us008-kanban-state-schema
-- comment: Tablas y columnas requeridas para US-008 (Kanban State & Time Tracking)

CREATE TABLE ibpms_time_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reference_id UUID NOT NULL,
    reference_type VARCHAR(30) NOT NULL CHECK (reference_type IN ('TASK_BPMN','TASK_AGILE','TASK_GANTT')),
    started_at TIMESTAMPTZ NOT NULL,
    stopped_at TIMESTAMPTZ,
    duration_minutes INT,
    user_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_timelog_ref ON ibpms_time_logs (reference_id, reference_type);
CREATE INDEX idx_timelog_user ON ibpms_time_logs (user_id);

CREATE TABLE ibpms_kanban_columns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    board_id VARCHAR(50) NOT NULL REFERENCES ibpms_kanban_board(id),
    name VARCHAR(50) NOT NULL,
    position INT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT uq_kanban_board_col_name UNIQUE (board_id, name)
);

ALTER TABLE ibpms_task ADD COLUMN IF NOT EXISTS blocked_reason TEXT;

-- Seed data: columnas default para boards existentes
INSERT INTO ibpms_kanban_columns (board_id, name, position)
SELECT id, 'TODO', 0 FROM ibpms_kanban_board
UNION ALL
SELECT id, 'IN_PROGRESS', 1 FROM ibpms_kanban_board
UNION ALL
SELECT id, 'BLOCKED', 2 FROM ibpms_kanban_board
UNION ALL
SELECT id, 'DONE', 3 FROM ibpms_kanban_board;
