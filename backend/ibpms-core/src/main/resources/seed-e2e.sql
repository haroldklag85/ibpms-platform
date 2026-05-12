-- Extension for pgvector
CREATE EXTENSION IF NOT EXISTS vector;

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
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description VARCHAR(255)
);
-- @Traceability: US-001, CA-08 (Feature Toggles)
INSERT INTO ibpms_feature_toggles (id, tenant_id, toggle_key, enabled, changed_by, description)
VALUES (gen_random_uuid(), 'default', 'FORCE_ROUTING', false, 'admin', 'Toggle CA-08')
ON CONFLICT (tenant_id, toggle_key) DO NOTHING;



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
DROP TABLE IF EXISTS ibpms_workdesk_projection;
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
    impact_level INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    progress_percent INT,
    total_steps INT,
    current_step INT,
    process_definition_key VARCHAR(100),
    category_tag VARCHAR(100)
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
INSERT INTO ibpms_security_role (id, name, description, is_template, is_active, source) VALUES
  (gen_random_uuid(), 'ROLE_SUPER_ADMIN', 'Super Administrador Global con acceso total', false, true, 'LOCAL'),
  (gen_random_uuid(), 'ROLE_OPERARIO', 'Operario de Bandeja Unificada', false, true, 'LOCAL'),
  (gen_random_uuid(), 'ROLE_SUPERVISOR', 'Supervisor de Área con delegación', false, true, 'LOCAL')
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
-- @Traceability: US-001, CA-04 (Múltiples Delegantes)
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

-- ====================================================================
-- FIX: DDL de Menu Topology (US-025) que no se crea por falta de Liquibase en E2E
-- ====================================================================
CREATE TABLE IF NOT EXISTS ibpms_menu_topology (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    label           VARCHAR(100) NOT NULL,
    icon            VARCHAR(50) NOT NULL,
    path            VARCHAR(200),
    parent_id       UUID REFERENCES ibpms_menu_topology(id) ON DELETE CASCADE,
    sort_order      INT NOT NULL DEFAULT 0,
    required_roles  JSONB NOT NULL DEFAULT '[]'::jsonb,
    is_active       BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_menu_topology_parent ON ibpms_menu_topology(parent_id);
CREATE INDEX IF NOT EXISTS idx_menu_topology_roles ON ibpms_menu_topology USING GIN (required_roles);

-- Raíz: Acceso universal (required_roles vacío = todos los roles)
INSERT INTO ibpms_menu_topology (id, label, icon, path, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000001', 'Inicio', 'mdi-home', '/home', 0, '[]'),
  ('a0000001-0000-0000-0000-000000000002', 'Mi Workdesk', 'mdi-desktop-mac', '/workdesk', 1, '[]')
ON CONFLICT (id) DO NOTHING;

-- Aprobaciones (roles específicos)
INSERT INTO ibpms_menu_topology (id, label, icon, path, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000003', 'Aprobaciones Pendientes', 'mdi-check-decagram', '/approvals', 2, '["ROLE_APROBADOR_FINANCIERO","ROLE_ALTA_DIRECCION"]')
ON CONFLICT (id) DO NOTHING;

-- Carpeta: Administración y Gobernanza (padre sin path)
INSERT INTO ibpms_menu_topology (id, label, icon, path, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000010', 'Administración y Gobernanza', 'mdi-cog-box', null, 3, '["ROLE_SUPER_ADMIN","ROLE_CISO"]')
ON CONFLICT (id) DO NOTHING;

-- Hijos de Administración
INSERT INTO ibpms_menu_topology (id, label, icon, path, parent_id, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000011', 'Generador de Entidades MDE', 'mdi-database-plus', '/config/mde', 'a0000001-0000-0000-0000-000000000010', 0, '["ROLE_SUPER_ADMIN"]'),
  ('a0000001-0000-0000-0000-000000000012', 'Centro de IA (MLOps)', 'mdi-brain', '/config/ai-center', 'a0000001-0000-0000-0000-000000000010', 1, '["ROLE_SUPER_ADMIN"]'),
  ('a0000001-0000-0000-0000-000000000013', 'Gestor de Festivos', 'mdi-calendar-alert', '/config/holidays', 'a0000001-0000-0000-0000-000000000010', 2, '["ROLE_SUPER_ADMIN"]'),
  ('a0000001-0000-0000-0000-000000000014', 'Tablero de Anomalías de Seguridad', 'mdi-shield-alert', '/security/anomalies', 'a0000001-0000-0000-0000-000000000010', 3, '["ROLE_CISO","ROLE_SUPER_ADMIN"]'),
  ('a0000001-0000-0000-0000-000000000015', 'Matriz Transaccional SoD', 'mdi-file-tree', '/security/sod-matrix', 'a0000001-0000-0000-0000-000000000010', 4, '["ROLE_CISO","ROLE_SUPER_ADMIN"]')
ON CONFLICT (id) DO NOTHING;

-- Carpeta: Service Delivery
INSERT INTO ibpms_menu_topology (id, label, icon, path, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000020', 'Service Delivery', 'mdi-account-group', null, 4, '["ROLE_SUPER_ADMIN","Global Admin"]')
ON CONFLICT (id) DO NOTHING;

INSERT INTO ibpms_menu_topology (id, label, icon, path, parent_id, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000021', 'Triaje Intake', 'mdi-filter', '/intake-triage', 'a0000001-0000-0000-0000-000000000020', 0, '["ROLE_SUPER_ADMIN","Global Admin"]'),
  ('a0000001-0000-0000-0000-000000000022', 'Intake Manual', 'mdi-text-box-plus', '/admin/intake', 'a0000001-0000-0000-0000-000000000020', 1, '["ROLE_SUPER_ADMIN","Global Admin"]'),
  ('a0000001-0000-0000-0000-000000000023', 'Customer 360', 'mdi-account-details', '/admin/customer360', 'a0000001-0000-0000-0000-000000000020', 2, '["ROLE_SUPER_ADMIN","Global Admin"]')
ON CONFLICT (id) DO NOTHING;

-- Carpeta: Project Builder
INSERT INTO ibpms_menu_topology (id, label, icon, path, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000030', 'Project Builder', 'mdi-rocket', null, 5, '["ROLE_SUPER_ADMIN","Global Admin"]')
ON CONFLICT (id) DO NOTHING;

INSERT INTO ibpms_menu_topology (id, label, icon, path, parent_id, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000031', 'Project Builder', 'mdi-hammer-wrench', '/admin/project-builder', 'a0000001-0000-0000-0000-000000000030', 0, '["ROLE_SUPER_ADMIN","Global Admin"]'),
  ('a0000001-0000-0000-0000-000000000032', 'Gestor de Proyectos', 'mdi-view-dashboard-variant', '/admin/projects/manager', 'a0000001-0000-0000-0000-000000000030', 1, '["ROLE_SUPER_ADMIN","Global Admin"]'),
  ('a0000001-0000-0000-0000-000000000033', 'Agile Hub', 'mdi-chart-timeline-variant', '/admin/projects/agile-hub', 'a0000001-0000-0000-0000-000000000030', 2, '["ROLE_SUPER_ADMIN","Global Admin"]')
ON CONFLICT (id) DO NOTHING;

-- Carpeta: Analytics & BAM
INSERT INTO ibpms_menu_topology (id, label, icon, path, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000040', 'Analytics & BAM', 'mdi-chart-bar', null, 6, '["ROLE_SUPER_ADMIN","Global Admin"]')
ON CONFLICT (id) DO NOTHING;

INSERT INTO ibpms_menu_topology (id, label, icon, path, parent_id, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000041', 'Dashboard BAM', 'mdi-monitor-dashboard', '/admin/analytics/bam', 'a0000001-0000-0000-0000-000000000040', 0, '["ROLE_SUPER_ADMIN","Global Admin"]')
ON CONFLICT (id) DO NOTHING;

-- Carpeta: Integration Hub
INSERT INTO ibpms_menu_topology (id, label, icon, path, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000050', 'Integration Hub', 'mdi-api', null, 7, '["ROLE_SUPER_ADMIN","Global Admin"]')
ON CONFLICT (id) DO NOTHING;

INSERT INTO ibpms_menu_topology (id, label, icon, path, parent_id, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000051', 'Catálogo de Conectores', 'mdi-book-open-page-variant', '/admin/integration/catalog', 'a0000001-0000-0000-0000-000000000050', 0, '["ROLE_SUPER_ADMIN","Global Admin"]'),
  ('a0000001-0000-0000-0000-000000000052', 'Connector Builder', 'mdi-puzzle-edit', '/admin/integration/builder', 'a0000001-0000-0000-0000-000000000050', 1, '["ROLE_SUPER_ADMIN","Global Admin"]'),
  ('a0000001-0000-0000-0000-000000000053', 'Visual Mapper', 'mdi-sitemap', '/admin/integration/mapper', 'a0000001-0000-0000-0000-000000000050', 2, '["ROLE_SUPER_ADMIN","Global Admin"]'),
  ('a0000001-0000-0000-0000-000000000054', 'DLQ Dashboard', 'mdi-alert-octagon', '/admin/integration/dlq', 'a0000001-0000-0000-0000-000000000050', 3, '["ROLE_SUPER_ADMIN","Global Admin"]')
ON CONFLICT (id) DO NOTHING;

-- Carpeta: SGDEA
INSERT INTO ibpms_menu_topology (id, label, icon, path, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000060', 'SGDEA', 'mdi-folder-lock', null, 8, '["ROLE_SUPER_ADMIN","Global Admin"]')
ON CONFLICT (id) DO NOTHING;

INSERT INTO ibpms_menu_topology (id, label, icon, path, parent_id, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000061', 'Bóveda Documental', 'mdi-safe', '/sgdea/vault', 'a0000001-0000-0000-0000-000000000060', 0, '["ROLE_SUPER_ADMIN","Global Admin"]')
ON CONFLICT (id) DO NOTHING;

-- Carpeta: Gobernanza
INSERT INTO ibpms_menu_topology (id, label, icon, path, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000070', 'Gobernanza', 'mdi-gavel', null, 9, '["ROLE_SUPER_ADMIN","Global Admin"]')
ON CONFLICT (id) DO NOTHING;

INSERT INTO ibpms_menu_topology (id, label, icon, path, parent_id, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000071', 'Gobernanza de Identidades', 'mdi-card-account-details', '/admin/security/identity', 'a0000001-0000-0000-0000-000000000070', 0, '["ROLE_SUPER_ADMIN","Global Admin"]'),
  ('a0000001-0000-0000-0000-000000000072', 'PMO / SLA Management', 'mdi-timer-settings', '/admin/pmo/settings', 'a0000001-0000-0000-0000-000000000070', 1, '["ROLE_SUPER_ADMIN","Global Admin"]')
ON CONFLICT (id) DO NOTHING;

-- ====================================================================
-- SEED OPERACIONAL E2E (Lotes B3-B5) - J-04 Sprint 6
-- ====================================================================

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
