-- liquibase formatted sql

-- changeset antigravity:48-seed-e2e-operational context:dev,test
-- comment: Seed operacional para UI E2E (Lotes B3-B5) - J-04 Sprint 6

-- 1. Kanban Board & Items
INSERT INTO ibpms_kanban_board (id, title, wip_limit, order_index)
VALUES ('board-e2e-1', 'Tablero Principal E2E', 10, 1)
ON CONFLICT (id) DO NOTHING;

INSERT INTO ibpms_kanban_item (id, board_id, title, status, priority, assignee, created_at, sla_hours)
VALUES 
    ('kb-task-e2e-1', 'board-e2e-1', 'Tarea KANBAN 1 - Pendiente', 'TODO', 'HIGH', NULL, NOW(), 24),
    ('kb-task-e2e-2', 'board-e2e-1', 'Tarea KANBAN 2 - Pendiente', 'TODO', 'MEDIUM', NULL, NOW(), 48),
    ('kb-task-e2e-3', 'board-e2e-1', 'Tarea KANBAN 3 - Pendiente', 'TODO', 'LOW', NULL, NOW(), 72)
ON CONFLICT (id) DO NOTHING;

-- 2. DMN Definitions
INSERT INTO ibpms_dmn_definitions (id, name, decision_ref, xml_content, status, hit_policy, version, author_hash, tenant_id, source)
VALUES 
    ('dmn-e2e-1', 'Decisión Triage E2E', 'triage_decision', '<?xml version="1.0" encoding="UTF-8"?><definitions id="Definitions_1" name="Decisión Triage E2E" namespace="http://camunda.org/schema/1.0/dmn"></definitions>', 'ACTIVE', 'FIRST', 1, 'admin_hash', 'default', 'NLP')
ON CONFLICT (id) DO NOTHING;

-- 3. Agile Projects & Tasks (Hub)
INSERT INTO ibpms_agile_projects (id, name, description, methodology, status, created_by, created_at)
VALUES 
    ('50000000-0000-0000-0000-000000000001', 'Proyecto E2E Core', 'Proyecto para pruebas E2E UI', 'KANBAN_CONTINUOUS', 'ACTIVE', 'admin', NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO ibpms_agile_tasks (id, project_id, title, description, effort_estimated, status, position, created_by, created_at)
VALUES 
    ('60000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', 'Tarea Agile 1', 'Desc 1', 5.0, 'TODO', 1, 'admin', NOW()),
    ('60000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000001', 'Tarea Agile 2', 'Desc 2', 8.0, 'TODO', 2, 'admin', NOW()),
    ('60000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000001', 'Tarea Agile 3', 'Desc 3', 3.0, 'TODO', 3, 'admin', NOW())
ON CONFLICT (id) DO NOTHING;
