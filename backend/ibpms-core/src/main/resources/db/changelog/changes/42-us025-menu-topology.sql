--liquibase formatted sql
--changeset architect:us025-menu-topology-create

CREATE TABLE ibpms_menu_topology (
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

CREATE INDEX idx_menu_topology_parent ON ibpms_menu_topology(parent_id);
CREATE INDEX idx_menu_topology_roles ON ibpms_menu_topology USING GIN (required_roles);

COMMENT ON TABLE ibpms_menu_topology IS 'US-025: Topología dinámica de menús del App Shell. Reemplaza el if/else hardcodeado de MenuLayoutUseCase.java';
COMMENT ON COLUMN ibpms_menu_topology.required_roles IS 'Array JSONB de roles requeridos. Vacío [] = acceso universal. Ej: ["ROLE_SUPER_ADMIN","ROLE_CISO"]';
COMMENT ON COLUMN ibpms_menu_topology.parent_id IS 'Self-reference para jerarquía carpeta→hijos. NULL = raíz del menú';

--changeset architect:us025-menu-topology-seed

-- Raíz: Acceso universal (required_roles vacío = todos los roles)
INSERT INTO ibpms_menu_topology (id, label, icon, path, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000001', 'Inicio', 'mdi-home', '/home', 0, '[]'),
  ('a0000001-0000-0000-0000-000000000002', 'Mi Workdesk', 'mdi-desktop-mac', '/workdesk', 1, '[]');

-- Aprobaciones (roles específicos)
INSERT INTO ibpms_menu_topology (id, label, icon, path, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000003', 'Aprobaciones Pendientes', 'mdi-check-decagram', '/approvals', 2, '["ROLE_APROBADOR_FINANCIERO","ROLE_ALTA_DIRECCION"]');

-- Carpeta: Administración y Gobernanza (padre sin path)
INSERT INTO ibpms_menu_topology (id, label, icon, path, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000010', 'Administración y Gobernanza', 'mdi-cog-box', null, 3, '["ROLE_SUPER_ADMIN","ROLE_CISO"]');

-- Hijos de Administración
INSERT INTO ibpms_menu_topology (id, label, icon, path, parent_id, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000011', 'Generador de Entidades MDE', 'mdi-database-plus', '/config/mde', 'a0000001-0000-0000-0000-000000000010', 0, '["ROLE_SUPER_ADMIN"]'),
  ('a0000001-0000-0000-0000-000000000012', 'Centro de IA (MLOps)', 'mdi-brain', '/config/ai-center', 'a0000001-0000-0000-0000-000000000010', 1, '["ROLE_SUPER_ADMIN"]'),
  ('a0000001-0000-0000-0000-000000000013', 'Gestor de Festivos', 'mdi-calendar-alert', '/config/holidays', 'a0000001-0000-0000-0000-000000000010', 2, '["ROLE_SUPER_ADMIN"]'),
  ('a0000001-0000-0000-0000-000000000014', 'Tablero de Anomalías de Seguridad', 'mdi-shield-alert', '/security/anomalies', 'a0000001-0000-0000-0000-000000000010', 3, '["ROLE_CISO","ROLE_SUPER_ADMIN"]'),
  ('a0000001-0000-0000-0000-000000000015', 'Matriz Transaccional SoD', 'mdi-file-tree', '/security/sod-matrix', 'a0000001-0000-0000-0000-000000000010', 4, '["ROLE_CISO","ROLE_SUPER_ADMIN"]');

-- Carpeta: Service Delivery
INSERT INTO ibpms_menu_topology (id, label, icon, path, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000020', 'Service Delivery', 'mdi-account-group', null, 4, '["ROLE_SUPER_ADMIN","Global Admin"]');

INSERT INTO ibpms_menu_topology (id, label, icon, path, parent_id, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000021', 'Triaje Intake', 'mdi-filter', '/intake-triage', 'a0000001-0000-0000-0000-000000000020', 0, '["ROLE_SUPER_ADMIN","Global Admin"]'),
  ('a0000001-0000-0000-0000-000000000022', 'Intake Manual', 'mdi-text-box-plus', '/admin/intake', 'a0000001-0000-0000-0000-000000000020', 1, '["ROLE_SUPER_ADMIN","Global Admin"]'),
  ('a0000001-0000-0000-0000-000000000023', 'Customer 360', 'mdi-account-details', '/admin/customer360', 'a0000001-0000-0000-0000-000000000020', 2, '["ROLE_SUPER_ADMIN","Global Admin"]');

-- Carpeta: Project Builder
INSERT INTO ibpms_menu_topology (id, label, icon, path, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000030', 'Project Builder', 'mdi-rocket', null, 5, '["ROLE_SUPER_ADMIN","Global Admin"]');

INSERT INTO ibpms_menu_topology (id, label, icon, path, parent_id, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000031', 'Project Builder', 'mdi-hammer-wrench', '/admin/project-builder', 'a0000001-0000-0000-0000-000000000030', 0, '["ROLE_SUPER_ADMIN","Global Admin"]'),
  ('a0000001-0000-0000-0000-000000000032', 'Gestor de Proyectos', 'mdi-view-dashboard-variant', '/admin/projects/manager', 'a0000001-0000-0000-0000-000000000030', 1, '["ROLE_SUPER_ADMIN","Global Admin"]'),
  ('a0000001-0000-0000-0000-000000000033', 'Agile Hub', 'mdi-chart-timeline-variant', '/admin/projects/agile-hub', 'a0000001-0000-0000-0000-000000000030', 2, '["ROLE_SUPER_ADMIN","Global Admin"]');

-- Carpeta: Analytics & BAM
INSERT INTO ibpms_menu_topology (id, label, icon, path, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000040', 'Analytics & BAM', 'mdi-chart-bar', null, 6, '["ROLE_SUPER_ADMIN","Global Admin"]');

INSERT INTO ibpms_menu_topology (id, label, icon, path, parent_id, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000041', 'Dashboard BAM', 'mdi-monitor-dashboard', '/admin/analytics/bam', 'a0000001-0000-0000-0000-000000000040', 0, '["ROLE_SUPER_ADMIN","Global Admin"]');

-- Carpeta: Integration Hub
INSERT INTO ibpms_menu_topology (id, label, icon, path, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000050', 'Integration Hub', 'mdi-api', null, 7, '["ROLE_SUPER_ADMIN","Global Admin"]');

INSERT INTO ibpms_menu_topology (id, label, icon, path, parent_id, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000051', 'Catálogo de Conectores', 'mdi-book-open-page-variant', '/admin/integration/catalog', 'a0000001-0000-0000-0000-000000000050', 0, '["ROLE_SUPER_ADMIN","Global Admin"]'),
  ('a0000001-0000-0000-0000-000000000052', 'Connector Builder', 'mdi-puzzle-edit', '/admin/integration/builder', 'a0000001-0000-0000-0000-000000000050', 1, '["ROLE_SUPER_ADMIN","Global Admin"]'),
  ('a0000001-0000-0000-0000-000000000053', 'Visual Mapper', 'mdi-sitemap', '/admin/integration/mapper', 'a0000001-0000-0000-0000-000000000050', 2, '["ROLE_SUPER_ADMIN","Global Admin"]'),
  ('a0000001-0000-0000-0000-000000000054', 'DLQ Dashboard', 'mdi-alert-octagon', '/admin/integration/dlq', 'a0000001-0000-0000-0000-000000000050', 3, '["ROLE_SUPER_ADMIN","Global Admin"]');

-- Carpeta: SGDEA
INSERT INTO ibpms_menu_topology (id, label, icon, path, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000060', 'SGDEA', 'mdi-folder-lock', null, 8, '["ROLE_SUPER_ADMIN","Global Admin"]');

INSERT INTO ibpms_menu_topology (id, label, icon, path, parent_id, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000061', 'Bóveda Documental', 'mdi-safe', '/sgdea/vault', 'a0000001-0000-0000-0000-000000000060', 0, '["ROLE_SUPER_ADMIN","Global Admin"]');

-- Carpeta: Gobernanza
INSERT INTO ibpms_menu_topology (id, label, icon, path, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000070', 'Gobernanza', 'mdi-gavel', null, 9, '["ROLE_SUPER_ADMIN","Global Admin"]');

INSERT INTO ibpms_menu_topology (id, label, icon, path, parent_id, sort_order, required_roles)
VALUES
  ('a0000001-0000-0000-0000-000000000071', 'Gobernanza de Identidades', 'mdi-card-account-details', '/admin/security/identity', 'a0000001-0000-0000-0000-000000000070', 0, '["ROLE_SUPER_ADMIN","Global Admin"]'),
  ('a0000001-0000-0000-0000-000000000072', 'PMO / SLA Management', 'mdi-timer-settings', '/admin/pmo/settings', 'a0000001-0000-0000-0000-000000000070', 1, '["ROLE_SUPER_ADMIN","Global Admin"]');
