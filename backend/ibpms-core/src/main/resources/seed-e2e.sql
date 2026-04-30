-- Tenants E2E
INSERT INTO tenants (id, name, domain) VALUES 
  ('tenant_alpha', 'Alpha Corp', 'alpha.com')
ON CONFLICT (id) DO NOTHING;



-- Feature Toggle (CU-J04-23)
CREATE TABLE IF NOT EXISTS ibpms_feature_toggles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(100) NOT NULL,
    toggle_key VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT false,
    changed_by VARCHAR(100) NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO ibpms_feature_toggles (tenant_id, toggle_key, enabled, changed_by) VALUES
  ('tenant_alpha', 'forceRouting', false, 'admin')
ON CONFLICT DO NOTHING;



-- Kanban Board E2E
INSERT INTO kanban_boards (id, project_name, owner_id, created_at) VALUES 
  ('123e4567-e89b-12d3-a456-426614174000', 'Project Alpha', 'director_1', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;



-- Kanban Tasks (F7: 3 TODO, 1 DONE)
INSERT INTO ibpms_task (id, board_id, title, status, created_at) VALUES
  ('11111111-1111-1111-1111-111111111111', '123e4567-e89b-12d3-a456-426614174000', 'Kanban Task 1', 'TODO', CURRENT_TIMESTAMP),
  ('22222222-2222-2222-2222-222222222222', '123e4567-e89b-12d3-a456-426614174000', 'Kanban Task 2', 'TODO', CURRENT_TIMESTAMP),
  ('33333333-3333-3333-3333-333333333333', '123e4567-e89b-12d3-a456-426614174000', 'Kanban Task 3', 'TODO', CURRENT_TIMESTAMP),
  ('44444444-4444-4444-4444-444444444444', '123e4567-e89b-12d3-a456-426614174000', 'Kanban Task 4 (Done)', 'DONE', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Workdesk Tasks (CU-J04-04: 4 tasks PENDING, SLAs: >50% green, 15-50% yellow, <15% red, expired gray)
CREATE TABLE IF NOT EXISTS ibpms_workdesk_projection (
    id VARCHAR(100) PRIMARY KEY,
    source_system VARCHAR(50) NOT NULL,
    original_task_id VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    assignee VARCHAR(100),
    candidate_group VARCHAR(100),
    sla_expiration_date TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    payload_metadata JSON,
    tenant_id VARCHAR(100) NOT NULL,
    impact_level INT NOT NULL
);

INSERT INTO ibpms_workdesk_projection (id, source_system, original_task_id, title, assignee, candidate_group, sla_expiration_date, status, tenant_id, impact_level) VALUES
  -- Green SLA (>50% remaining)
  ('wd_task_1', 'BPMN', 'task_1', 'Workdesk Task 1 (Green)', NULL, 'Adjusters', CURRENT_TIMESTAMP + INTERVAL '10 days', 'PENDING', 'tenant_alpha', 1),
  -- Yellow SLA (15-50% remaining)
  ('wd_task_2', 'BPMN', 'task_2', 'Workdesk Task 2 (Yellow)', NULL, 'Adjusters', CURRENT_TIMESTAMP + INTERVAL '2 days', 'PENDING', 'tenant_alpha', 2),
  -- Red SLA (<15% remaining)
  ('wd_task_3', 'BPMN', 'task_3', 'Workdesk Task 3 (Red)', NULL, 'Adjusters', CURRENT_TIMESTAMP + INTERVAL '1 hours', 'PENDING', 'tenant_alpha', 3),
  -- Gray SLA (Expired)
  ('wd_task_4', 'BPMN', 'task_4', 'Workdesk Task 4 (Gray Expired)', NULL, 'Adjusters', CURRENT_TIMESTAMP - INTERVAL '1 days', 'PENDING', 'tenant_alpha', 4)
ON CONFLICT (id) DO NOTHING;

-- Seeds for Security Login (Emergency Login requires ibpms_security_user)
INSERT INTO ibpms_security_user (id, username, email, password_hash, is_active, is_external_idp, created_at) VALUES 
  (gen_random_uuid(), 'admin', 'admin@alpha.com', '$2b$10$1OHQ9PUOg9z6LChpq2gtF.6lfkZww5rBsFXjtBA4YBwZkwHVlgmri', true, false, CURRENT_TIMESTAMP),
  (gen_random_uuid(), 'analista', 'analista_n1@alpha.com', '$2b$10$1OHQ9PUOg9z6LChpq2gtF.6lfkZww5rBsFXjtBA4YBwZkwHVlgmri', true, false, CURRENT_TIMESTAMP),
  (gen_random_uuid(), 'perito_a', 'perito_a@alpha.com', '$2b$10$1OHQ9PUOg9z6LChpq2gtF.6lfkZww5rBsFXjtBA4YBwZkwHVlgmri', true, false, CURRENT_TIMESTAMP),
  (gen_random_uuid(), 'perito_b', 'perito_b@alpha.com', '$2b$10$1OHQ9PUOg9z6LChpq2gtF.6lfkZww5rBsFXjtBA4YBwZkwHVlgmri', true, false, CURRENT_TIMESTAMP),
  (gen_random_uuid(), 'director_1', 'director_1@alpha.com', '$2b$10$1OHQ9PUOg9z6LChpq2gtF.6lfkZww5rBsFXjtBA4YBwZkwHVlgmri', true, false, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO UPDATE 
SET password_hash = EXCLUDED.password_hash,
    username = EXCLUDED.username;

-- Catálogo de Roles JPA (ibpms_security_role) — Requerido por FK de la tabla pivote
INSERT INTO ibpms_security_role (id, name, description, is_template, source) VALUES
  (gen_random_uuid(), 'ROLE_SUPER_ADMIN', 'Super Administrador Global con acceso total', false, 'LOCAL'),
  (gen_random_uuid(), 'ROLE_OPERARIO', 'Operario de Bandeja Unificada', false, 'LOCAL'),
  (gen_random_uuid(), 'ROLE_SUPERVISOR', 'Supervisor de Área con delegación', false, 'LOCAL')
ON CONFLICT (name) DO NOTHING;

-- Mapeo ManyToMany: ibpms_security_user_roles (User ↔ Role)
INSERT INTO ibpms_security_user_roles (user_id, role_id)
SELECT u.id, r.id FROM ibpms_security_user u, ibpms_security_role r
WHERE u.email = 'admin@alpha.com' AND r.name = 'ROLE_SUPER_ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO ibpms_security_user_roles (user_id, role_id)
SELECT u.id, r.id FROM ibpms_security_user u, ibpms_security_role r
WHERE u.email = 'analista_n1@alpha.com' AND r.name = 'ROLE_OPERARIO'
ON CONFLICT DO NOTHING;

INSERT INTO ibpms_security_user_roles (user_id, role_id)
SELECT u.id, r.id FROM ibpms_security_user u, ibpms_security_role r
WHERE u.email = 'perito_a@alpha.com' AND r.name = 'ROLE_OPERARIO'
ON CONFLICT DO NOTHING;

INSERT INTO ibpms_security_user_roles (user_id, role_id)
SELECT u.id, r.id FROM ibpms_security_user u, ibpms_security_role r
WHERE u.email = 'perito_b@alpha.com' AND r.name = 'ROLE_OPERARIO'
ON CONFLICT DO NOTHING;

INSERT INTO ibpms_security_user_roles (user_id, role_id)
SELECT u.id, r.id FROM ibpms_security_user u, ibpms_security_role r
WHERE u.email = 'director_1@alpha.com' AND r.name = 'ROLE_SUPERVISOR'
ON CONFLICT DO NOTHING;

-- ====================================================================
-- B-1 FIX: Delegación en tabla JPA correcta (ibpms_security_delegation)
-- Requerido por: CU-J04-20 a 22, CU-J04-NEG-04
-- La tabla 'user_delegation' (línea 26) es LEGACY/DEPRECATED.
-- El backend (TaskDelegationService + DelegationEntity) usa esta tabla.
-- ====================================================================
INSERT INTO ibpms_security_delegation (id, delegator_id, substitute_id, start_date, end_date, is_active, reason)
SELECT gen_random_uuid(), d.id, a.id,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '365 days',
       true, 'Delegación de escritorio para UAT J-04'
FROM ibpms_security_user d, ibpms_security_user a
WHERE d.username = 'director_1' AND a.username = 'analista'
ON CONFLICT DO NOTHING;

-- ====================================================================
-- B-2 FIX: Tarea de Director en Workdesk Projection
-- Requerido por: CU-J04-39 (Firma Final Director)
-- Sin esta tarea, el Director no ve nada en su bandeja personal.
-- ====================================================================
INSERT INTO ibpms_workdesk_projection (id, source_system, original_task_id, title, assignee, candidate_group,
    sla_expiration_date, status, tenant_id, impact_level) VALUES
  ('wd_task_5', 'BPMN', 'task_5', 'Firma Final (Director)', NULL, 'Directors',
   CURRENT_TIMESTAMP + INTERVAL '5 days', 'PENDING', 'tenant_alpha', 1)
ON CONFLICT (id) DO NOTHING;
