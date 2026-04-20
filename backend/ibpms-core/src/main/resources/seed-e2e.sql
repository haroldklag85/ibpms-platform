-- Tenants E2E
INSERT INTO tenants (id, name, domain) VALUES 
  ('tenant_alpha', 'Alpha Corp', 'alpha.com')
ON CONFLICT (id) DO NOTHING;

-- Usuarios E2E (passwords: BCrypt de 'Test123!')
-- Hash: $2b$10$1OHQ9PUOg9z6LChpq2gtF.6lfkZww5rBsFXjtBA4YBwZkwHVlgmri
INSERT INTO users (id, email, password_hash, tenant_id, display_name) VALUES
  ('usr_admin_alpha', 'admin@alpha.com', '$2b$10$1OHQ9PUOg9z6LChpq2gtF.6lfkZww5rBsFXjtBA4YBwZkwHVlgmri', 'tenant_alpha', 'Admin Alpha'),
  ('analista_n1', 'analista_n1@alpha.com', '$2b$10$1OHQ9PUOg9z6LChpq2gtF.6lfkZww5rBsFXjtBA4YBwZkwHVlgmri', 'tenant_alpha', 'Analista N1'),
  ('perito_a', 'perito_a@alpha.com', '$2b$10$1OHQ9PUOg9z6LChpq2gtF.6lfkZww5rBsFXjtBA4YBwZkwHVlgmri', 'tenant_alpha', 'Perito A'),
  ('perito_b', 'perito_b@alpha.com', '$2b$10$1OHQ9PUOg9z6LChpq2gtF.6lfkZww5rBsFXjtBA4YBwZkwHVlgmri', 'tenant_alpha', 'Perito B'),
  ('director_1', 'director_1@alpha.com', '$2b$10$1OHQ9PUOg9z6LChpq2gtF.6lfkZww5rBsFXjtBA4YBwZkwHVlgmri', 'tenant_alpha', 'Director 1')
ON CONFLICT (id) DO NOTHING;

-- Roles RBAC
INSERT INTO user_roles (user_id, role) VALUES
  ('usr_admin_alpha', 'ROLE_SUPER_ADMIN'),
  ('analista_n1', 'ROLE_OPERARIO'),
  ('perito_a', 'ROLE_OPERARIO'),
  ('perito_b', 'ROLE_OPERARIO'),
  ('director_1', 'ROLE_SUPERVISOR')
ON CONFLICT DO NOTHING;

-- Delegaciones J-04 (PRE-04)
CREATE TABLE IF NOT EXISTS user_delegation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    supervisor_id VARCHAR(100),
    assistant_id VARCHAR(100),
    tenant_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO user_delegation (supervisor_id, assistant_id, tenant_id) VALUES
  ('director_1', 'analista_n1', 'tenant_alpha')
ON CONFLICT DO NOTHING;

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

-- DMN Definition Publish (B4 re-run)
CREATE TABLE IF NOT EXISTS dmn_definition (
    id VARCHAR(100) PRIMARY KEY,
    key VARCHAR(100),
    version INT,
    resource_name VARCHAR(200),
    tenant_id VARCHAR(100)
);
INSERT INTO dmn_definition (id, key, version, resource_name, tenant_id) VALUES
  ('dmn-1', 'risk_assessment', 1, 'risk_assessment.dmn', 'tenant_alpha')
ON CONFLICT DO NOTHING;

-- Kanban Board E2E
INSERT INTO kanban_boards (id, project_name, owner_id, created_at) VALUES 
  ('123e4567-e89b-12d3-a456-426614174000', 'Project Alpha', 'director_1', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Kanban Columns (PRE-08)
CREATE TABLE IF NOT EXISTS kanban_column (
    id VARCHAR(50) PRIMARY KEY,
    board_id UUID,
    name VARCHAR(50),
    position INT
);
INSERT INTO kanban_column (id, board_id, name, position) VALUES
  ('TODO', '123e4567-e89b-12d3-a456-426614174000', 'To Do', 1),
  ('IN_PROGRESS', '123e4567-e89b-12d3-a456-426614174000', 'In Progress', 2),
  ('BLOCKED', '123e4567-e89b-12d3-a456-426614174000', 'Blocked', 3),
  ('DONE', '123e4567-e89b-12d3-a456-426614174000', 'Done', 4)
ON CONFLICT DO NOTHING;

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
