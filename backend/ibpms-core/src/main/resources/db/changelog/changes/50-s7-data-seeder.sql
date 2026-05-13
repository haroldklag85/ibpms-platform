-- liquibase formatted sql
-- changeset antigravity:50-s7-data-seeder context:dev,test

-- 1. Usuarios y Perfiles Base (Perito A, Perito B, Arquitecto, Supervisor)
INSERT INTO ibpms_users (id, username, email, full_name, is_active, tenant_id)
VALUES 
    ('usr_perito_a', 'perito.a', 'perito.a@ibpms.com', 'Perito Alpha', true, 'default'),
    ('usr_perito_b', 'perito.b', 'perito.b@ibpms.com', 'Perito Beta', true, 'default'),
    ('usr_arquitecto', 'arquitecto', 'arquitecto@ibpms.com', 'Arquitecto BPM', true, 'default'),
    ('usr_supervisor', 'supervisor', 'supervisor@ibpms.com', 'Supervisor Siniestros', true, 'default')
ON CONFLICT (id) DO NOTHING;

-- 2. Roles de Sistema (Aseguramos que existen y los asignamos)
INSERT INTO ibpms_roles (id, name, description, is_active)
VALUES 
    ('role_perito', 'ROLE_PERITO', 'Rol para Ajustadores/Peritos', true),
    ('role_supervisor', 'ROLE_SUPERVISOR', 'Rol para Supervisores', true),
    ('role_arquitecto', 'ROLE_ARQUITECTO', 'Rol para Arquitectos de Procesos', true),
    ('role_super_admin', 'ROLE_SUPER_ADMIN', 'Rol de Administración Global', true)
ON CONFLICT (id) DO NOTHING;

-- Asignación de Roles
INSERT INTO ibpms_user_roles (user_id, role_id)
VALUES 
    ('usr_perito_a', 'role_perito'),
    ('usr_perito_b', 'role_perito'),
    ('usr_arquitecto', 'role_arquitecto'),
    ('usr_supervisor', 'role_supervisor'),
    ('usr_supervisor', 'role_super_admin')
ON CONFLICT DO NOTHING;

-- 3. Feature Toggles Inicializados (US-001 CA-08)
INSERT INTO ibpms_feature_toggles (id, tenant_id, toggle_key, enabled, description)
VALUES
    ('ft_routing_1', 'default', 'FORCE_ROUTING', false, 'Toggle para el enrutamiento inteligente (Skill-Routing) CA-08')
ON CONFLICT (tenant_id, toggle_key) DO NOTHING;

-- 4. Reglas de Delegación (US-001 CA-04)
-- Supervisor puede ver las bandejas de Perito A y Perito B
INSERT INTO user_delegation (id, supervisor_id, assistant_id, tenant_id, created_at)
VALUES 
    ('del_1', 'usr_supervisor', 'usr_perito_a', 'default', CURRENT_TIMESTAMP),
    ('del_2', 'usr_supervisor', 'usr_perito_b', 'default', CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- 5. Datos Temporales de Prefill (US-029 CA-5)
INSERT INTO task_drafts (id, task_id, user_id, current_step, partial_data, schema_version, created_at, updated_at)
VALUES
    ('10000000-0000-4000-8000-000000000001', 'task_mock_1', 'usr_perito_a', 1, '{"asegurado": "Juan Perez", "poliza": "POL-12345", "siniestro_id": "SIN-987"}', '1.0', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 6. Tareas Simuladas en Estado Vivo (Workdesk Timeout prevention)
INSERT INTO ibpms_workdesk_projection (id, task_id, tenant_id, assignee, title, status, origin, sla_expiration_date, impact_level, created_at)
VALUES
    ('wp_mock_1', 'task_mock_1', 'default', 'usr_perito_a', 'Liquidación Siniestro Juan Perez', 'ASSIGNED', 'BPMN', CURRENT_TIMESTAMP + INTERVAL '2 days', 5, CURRENT_TIMESTAMP),
    ('wp_mock_2', 'task_mock_2', 'default', 'usr_perito_b', 'Inspección Vehicular POL-888', 'ASSIGNED', 'BPMN', CURRENT_TIMESTAMP + INTERVAL '1 day', 3, CURRENT_TIMESTAMP),
    ('wp_mock_3', 'task_mock_3', 'default', null, 'Aprobación Especial Siniestro Masivo', 'UNASSIGNED', 'BPMN', CURRENT_TIMESTAMP + INTERVAL '4 hours', 9, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;
