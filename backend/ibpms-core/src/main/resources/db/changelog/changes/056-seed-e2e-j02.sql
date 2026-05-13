-- liquibase formatted sql
-- changeset antigravity:056-seed-e2e-j02 context:dev,test

-- @Traceability: Semilla E2E J-02 (T-24)

-- Insertar Tenant Base
INSERT INTO ibpms_tenant (slug, name, is_active) 
VALUES ('tenant_alpha', 'Alpha Corp', true)
ON CONFLICT (slug) DO NOTHING;

-- Insertar Perfil Sysadmin para Anti-Spoofing DMN
INSERT INTO ibpms_security_user (id, username, email, password_hash, is_active, is_external_idp, created_at) 
VALUES (gen_random_uuid(), 'sysadmin', 'sysadmin@alpha.com', '$2a$10$dummyHash...', true, false, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

-- Insertar Perfil Analista Base
INSERT INTO ibpms_security_user (id, username, email, password_hash, is_active, is_external_idp, created_at) 
VALUES (gen_random_uuid(), 'analista_n1', 'analista_n1@alpha.com', '$2a$10$dummyHash...', true, false, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

-- Asignar roles
INSERT INTO ibpms_security_role (id, name, description, is_template, is_active, source) VALUES
  (gen_random_uuid(), 'ROLE_SYSADMIN', 'Sysadmin', false, true, 'LOCAL'),
  (gen_random_uuid(), 'ROLE_ANALISTA', 'Analista', false, true, 'LOCAL')
ON CONFLICT (name) DO NOTHING;

INSERT INTO ibpms_security_user_roles (user_id, role_id)
SELECT u.id, r.id FROM ibpms_security_user u, ibpms_security_role r
WHERE u.email = 'sysadmin@alpha.com' AND r.name = 'ROLE_SYSADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO ibpms_security_user_roles (user_id, role_id)
SELECT u.id, r.id FROM ibpms_security_user u, ibpms_security_role r
WHERE u.email = 'analista_n1@alpha.com' AND r.name = 'ROLE_ANALISTA'
ON CONFLICT DO NOTHING;
