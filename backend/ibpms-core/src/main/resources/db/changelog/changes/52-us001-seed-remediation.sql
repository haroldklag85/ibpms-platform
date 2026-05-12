-- liquibase formatted sql
-- changeset Antigravity:us001-seed-remediation context:dev,test
-- comment US-001: Seed Data para CA-04 y CA-08 (Feature Toggles y Delegacion)
-- @Traceability: US-001 - CA-04, CA-08 (Seed Data)

-- 1. Usuarios (SUPER_ADMIN, OPERATIVO_1, OPERATIVO_2)
INSERT INTO ibpms_security_user (id, username, email, is_active, created_at)
VALUES 
    ('a0000000-0000-4000-8000-000000000001', 'admin.test', 'admin@ibpms.com', true, CURRENT_TIMESTAMP),
    ('b0000000-0000-4000-8000-000000000001', 'operativo.1', 'op1@ibpms.com', true, CURRENT_TIMESTAMP),
    ('c0000000-0000-4000-8000-000000000001', 'operativo.2', 'op2@ibpms.com', true, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 2. Feature Toggles
INSERT INTO ibpms_feature_toggles (id, tenant_id, toggle_key, enabled, changed_by, changed_at, description)
VALUES (
    'f0000000-0000-4000-8000-000000000002',
    'default',
    'force-routing',
    false,
    'system_seed',
    CURRENT_TIMESTAMP,
    'Fuerza el Atender Siguiente Ocultando Grilla'
) ON CONFLICT (tenant_id, toggle_key) DO NOTHING;

-- 3. Roles (SUPER_ADMIN, ROLE_OPERATIVO)
INSERT INTO ibpms_security_role (id, name, description, is_template, source)
VALUES 
    ('e0000000-0000-4000-8000-000000000001', 'SUPER_ADMIN', 'Super Admin Global', false, 'LOCAL'),
    ('e0000000-0000-4000-8000-000000000002', 'ROLE_OPERATIVO', 'Rol Operativo Estandar', false, 'LOCAL')
ON CONFLICT (id) DO NOTHING;

-- 4. Asignacion de Roles
INSERT INTO ibpms_security_user_roles (user_id, role_id)
VALUES 
    ('a0000000-0000-4000-8000-000000000001', 'e0000000-0000-4000-8000-000000000001'),
    ('b0000000-0000-4000-8000-000000000001', 'e0000000-0000-4000-8000-000000000002'),
    ('c0000000-0000-4000-8000-000000000001', 'e0000000-0000-4000-8000-000000000002')
ON CONFLICT DO NOTHING;

-- 5. Delegacion (Admin delega en Operativo 1)
INSERT INTO ibpms_security_delegation (id, delegator_id, substitute_id, start_date, end_date, is_active, reason)
VALUES (
    'd0000000-0000-4000-8000-000000000002',
    ('a0000000-0000-4000-8000-000000000001'),
    ('b0000000-0000-4000-8000-000000000001'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP + INTERVAL '30 days',
    true,
    'Seed Delegacion CA-04'
) ON CONFLICT (id) DO NOTHING;
