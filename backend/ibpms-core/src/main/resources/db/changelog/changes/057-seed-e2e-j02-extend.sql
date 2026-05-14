-- liquibase formatted sql
-- changeset antigravity:057-seed-e2e-j02-extend context:dev,test

-- @Traceability: Semilla E2E J-02 (T-24)

-- 1. Insertar usuarios (operario_a, operario_b, supervisor)
INSERT INTO ibpms_security_user (id, username, email, password_hash, is_active, is_external_idp, created_at) VALUES 
('f750b31e-450f-48d6-a212-32a2656911c7', 'operario_a', 'operario_a@alpha.com', '$2a$10$dummyHash...', true, false, CURRENT_TIMESTAMP),
('3cd2fc32-9c9d-4cba-a15d-8de5a9a45612', 'operario_b', 'operario_b@alpha.com', '$2a$10$dummyHash...', true, false, CURRENT_TIMESTAMP),
('a3b12345-6789-abcd-ef01-23456789abcd', 'supervisor_e2e', 'supervisor_e2e@alpha.com', '$2a$10$dummyHash...', true, false, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

-- Insertar roles adicionales (si no existen)
INSERT INTO ibpms_security_role (id, name, description, is_template, is_active, source) VALUES
  ('r1c2d3e4-f5a6-4b7c-8d9e-0f1a2b3c4d5e', 'ROLE_OPERARIO', 'Operario', false, true, 'LOCAL'),
  ('r2c2d3e4-f5a6-4b7c-8d9e-0f1a2b3c4d5e', 'ROLE_SUPERVISOR', 'Supervisor', false, true, 'LOCAL')
ON CONFLICT (name) DO NOTHING;

-- Asignar roles a operario_a
INSERT INTO ibpms_security_user_roles (user_id, role_id)
SELECT u.id, r.id FROM ibpms_security_user u, ibpms_security_role r
WHERE u.email = 'operario_a@alpha.com' AND r.name = 'ROLE_OPERARIO'
ON CONFLICT DO NOTHING;

-- Asignar roles a operario_b
INSERT INTO ibpms_security_user_roles (user_id, role_id)
SELECT u.id, r.id FROM ibpms_security_user u, ibpms_security_role r
WHERE u.email = 'operario_b@alpha.com' AND r.name = 'ROLE_OPERARIO'
ON CONFLICT DO NOTHING;

-- Asignar roles a supervisor
INSERT INTO ibpms_security_user_roles (user_id, role_id)
SELECT u.id, r.id FROM ibpms_security_user u, ibpms_security_role r
WHERE u.email = 'supervisor_e2e@alpha.com' AND r.name = 'ROLE_SUPERVISOR'
ON CONFLICT DO NOTHING;

-- 2. Feature Toggle FORCE_ROUTING
INSERT INTO ibpms_feature_toggles (id, tenant_id, toggle_key, enabled, changed_by, changed_at)
VALUES ('f1t2g3g4-1234-5678-9abc-def012345678', 'tenant_alpha', 'FORCE_ROUTING', false, 'sysadmin', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 3. Tablero Kanban
INSERT INTO kanban_boards (id, project_name, description, owner_id, created_at)
VALUES ('b1c2d3e4-1234-5678-9abc-def012345678', 'QA Sprint E2E', 'Board E2E', 'supervisor_e2e', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 4. Columnas Kanban (5)
INSERT INTO ibpms_kanban_columns (id, board_id, name, position, created_at) VALUES
  ('c1d2e3f4-1111-5678-9abc-def012345678', 'b1c2d3e4-1234-5678-9abc-def012345678', 'TODO', 1, CURRENT_TIMESTAMP),
  ('c1d2e3f4-2222-5678-9abc-def012345678', 'b1c2d3e4-1234-5678-9abc-def012345678', 'DOING', 2, CURRENT_TIMESTAMP),
  ('c1d2e3f4-3333-5678-9abc-def012345678', 'b1c2d3e4-1234-5678-9abc-def012345678', 'REVIEW', 3, CURRENT_TIMESTAMP),
  ('c1d2e3f4-4444-5678-9abc-def012345678', 'b1c2d3e4-1234-5678-9abc-def012345678', 'BLOCKED', 4, CURRENT_TIMESTAMP),
  ('c1d2e3f4-5555-5678-9abc-def012345678', 'b1c2d3e4-1234-5678-9abc-def012345678', 'DONE', 5, CURRENT_TIMESTAMP)
ON CONFLICT ON CONSTRAINT uq_kanban_board_col_name DO NOTHING;

-- 5. Tarjetas Kanban: 3 específicas + 17 adicionales = 20 tareas mixtas
INSERT INTO ibpms_task (id, board_id, title, status, created_at) VALUES
  ('t1d2e3f4-0001-5678-9abc-def012345678', 'b1c2d3e4-1234-5678-9abc-def012345678', 'Tarea Kanban E2E 1', 'TODO', CURRENT_TIMESTAMP),
  ('t1d2e3f4-0002-5678-9abc-def012345678', 'b1c2d3e4-1234-5678-9abc-def012345678', 'Tarea Kanban E2E 2', 'TODO', CURRENT_TIMESTAMP),
  ('t1d2e3f4-0003-5678-9abc-def012345678', 'b1c2d3e4-1234-5678-9abc-def012345678', 'Tarea Kanban E2E 3', 'TODO', CURRENT_TIMESTAMP),
  ('t1d2e3f4-0004-5678-9abc-def012345678', 'b1c2d3e4-1234-5678-9abc-def012345678', 'Tarea Mixta E2E 4', 'TODO', CURRENT_TIMESTAMP),
  ('t1d2e3f4-0005-5678-9abc-def012345678', 'b1c2d3e4-1234-5678-9abc-def012345678', 'Tarea Mixta E2E 5', 'DOING', CURRENT_TIMESTAMP),
  ('t1d2e3f4-0006-5678-9abc-def012345678', 'b1c2d3e4-1234-5678-9abc-def012345678', 'Tarea Mixta E2E 6', 'TODO', CURRENT_TIMESTAMP),
  ('t1d2e3f4-0007-5678-9abc-def012345678', 'b1c2d3e4-1234-5678-9abc-def012345678', 'Tarea Mixta E2E 7', 'REVIEW', CURRENT_TIMESTAMP),
  ('t1d2e3f4-0008-5678-9abc-def012345678', 'b1c2d3e4-1234-5678-9abc-def012345678', 'Tarea Mixta E2E 8', 'TODO', CURRENT_TIMESTAMP),
  ('t1d2e3f4-0009-5678-9abc-def012345678', 'b1c2d3e4-1234-5678-9abc-def012345678', 'Tarea Mixta E2E 9', 'BLOCKED', CURRENT_TIMESTAMP),
  ('t1d2e3f4-0010-5678-9abc-def012345678', 'b1c2d3e4-1234-5678-9abc-def012345678', 'Tarea Mixta E2E 10', 'TODO', CURRENT_TIMESTAMP),
  ('t1d2e3f4-0011-5678-9abc-def012345678', 'b1c2d3e4-1234-5678-9abc-def012345678', 'Tarea Mixta E2E 11', 'DONE', CURRENT_TIMESTAMP),
  ('t1d2e3f4-0012-5678-9abc-def012345678', 'b1c2d3e4-1234-5678-9abc-def012345678', 'Tarea Mixta E2E 12', 'TODO', CURRENT_TIMESTAMP),
  ('t1d2e3f4-0013-5678-9abc-def012345678', 'b1c2d3e4-1234-5678-9abc-def012345678', 'Tarea Mixta E2E 13', 'DOING', CURRENT_TIMESTAMP),
  ('t1d2e3f4-0014-5678-9abc-def012345678', 'b1c2d3e4-1234-5678-9abc-def012345678', 'Tarea Mixta E2E 14', 'TODO', CURRENT_TIMESTAMP),
  ('t1d2e3f4-0015-5678-9abc-def012345678', 'b1c2d3e4-1234-5678-9abc-def012345678', 'Tarea Mixta E2E 15', 'REVIEW', CURRENT_TIMESTAMP),
  ('t1d2e3f4-0016-5678-9abc-def012345678', 'b1c2d3e4-1234-5678-9abc-def012345678', 'Tarea Mixta E2E 16', 'TODO', CURRENT_TIMESTAMP),
  ('t1d2e3f4-0017-5678-9abc-def012345678', 'b1c2d3e4-1234-5678-9abc-def012345678', 'Tarea Mixta E2E 17', 'BLOCKED', CURRENT_TIMESTAMP),
  ('t1d2e3f4-0018-5678-9abc-def012345678', 'b1c2d3e4-1234-5678-9abc-def012345678', 'Tarea Mixta E2E 18', 'TODO', CURRENT_TIMESTAMP),
  ('t1d2e3f4-0019-5678-9abc-def012345678', 'b1c2d3e4-1234-5678-9abc-def012345678', 'Tarea Mixta E2E 19', 'DOING', CURRENT_TIMESTAMP),
  ('t1d2e3f4-0020-5678-9abc-def012345678', 'b1c2d3e4-1234-5678-9abc-def012345678', 'Tarea Mixta E2E 20', 'TODO', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;
