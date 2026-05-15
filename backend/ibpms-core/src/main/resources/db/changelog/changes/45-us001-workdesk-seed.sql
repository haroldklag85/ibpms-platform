-- liquibase formatted sql

-- changeset antigravity:45-us001-workdesk-seed context:dev,test

-- Insert roles if they don't exist
INSERT INTO ibpms_security_role (id, name, description, is_template, source)
VALUES 
    ('e1111111-1111-1111-1111-111111111111', 'ROLE_PERITO', 'Perito', false, 'LOCAL'),
    ('d2222222-2222-2222-2222-222222222222', 'ROLE_DIRECTOR', 'Director', false, 'LOCAL')
ON CONFLICT (name) DO NOTHING;

-- Insert users
INSERT INTO ibpms_security_user (id, username, email, password_hash, status, is_external_idp, created_at)
VALUES 
    ('10000000-0000-0000-0000-000000000001', 'perito_a', 'perito_a@ibpms.com', '$2a$10$placeholder', 'ACTIVE', false, CURRENT_TIMESTAMP),
    ('20000000-0000-0000-0000-000000000002', 'director_1', 'director_1@ibpms.com', '$2a$10$placeholder', 'ACTIVE', false, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- Link users to roles
INSERT INTO ibpms_security_user_roles (user_id, role_id)
SELECT u.id, r.id FROM ibpms_security_user u CROSS JOIN ibpms_security_role r
WHERE u.username = 'perito_a' AND r.name = 'ROLE_PERITO'
ON CONFLICT DO NOTHING;

INSERT INTO ibpms_security_user_roles (user_id, role_id)
SELECT u.id, r.id FROM ibpms_security_user u CROSS JOIN ibpms_security_role r
WHERE u.username = 'director_1' AND r.name = 'ROLE_DIRECTOR'
ON CONFLICT DO NOTHING;

-- Seed Workdesk projection initial state
INSERT INTO ibpms_workdesk_projection (id, source_system, original_task_id, title, assignee, candidate_group, status, tenant_id, impact_level)
VALUES 
    ('wd-task-perito-1', 'KANBAN', 'kb-task-1', 'Revisión Pericial Inicial', 'perito_a', NULL, 'AVAILABLE', 'tenant_alpha', 1),
    ('wd-task-director-1', 'BPMN', 'bpmn-task-1', 'Aprobación de Director', 'director_1', NULL, 'IN_PROGRESS', 'tenant_alpha', 2),
    ('wd-task-perito-2', 'BPMN', 'bpmn-task-2', 'Validación Técnica', NULL, 'ROLE_PERITO', 'AVAILABLE', 'tenant_alpha', 1)
ON CONFLICT (id) DO NOTHING;
