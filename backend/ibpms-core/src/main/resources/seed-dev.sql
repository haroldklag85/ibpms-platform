-- ==========================================================================
-- SEED-DEV.SQL — Datos mínimos para desarrollo local (BD: ibpms_db :5432)
-- HO-03: Garantiza que la BD de desarrollo tenga datos funcionales
-- sin depender de fallbacks hardcodeados.
-- Carga automática vía spring.sql.init en application.yml
-- ==========================================================================

-- ==========================================
-- 1. Catálogo de Tenants (ibpms_tenant)
-- ==========================================
INSERT INTO ibpms_tenant (slug, name) VALUES 
  ('T-100', 'Tenant E2E Test')
ON CONFLICT (slug) DO NOTHING;

-- ==========================================
-- 2. Usuarios de Seguridad (ibpms_security_user)
-- ==========================================
INSERT INTO ibpms_security_user (id, username, email, password_hash, is_active, is_external_idp, created_at) VALUES 
  (gen_random_uuid(), 'admin', 'admin@alpha.com', '$2b$10$1OHQ9PUOg9z6LChpq2gtF.6lfkZww5rBsFXjtBA4YBwZkwHVlgmri', true, false, CURRENT_TIMESTAMP),
  (gen_random_uuid(), 'analista', 'analista_n1@alpha.com', '$2b$10$1OHQ9PUOg9z6LChpq2gtF.6lfkZww5rBsFXjtBA4YBwZkwHVlgmri', true, false, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO UPDATE 
SET password_hash = EXCLUDED.password_hash,
    username = EXCLUDED.username;

-- ==========================================
-- 2. Catálogo de Roles JPA (ibpms_security_role)
-- ==========================================
INSERT INTO ibpms_security_role (id, name, description, is_template, source) VALUES
  (gen_random_uuid(), 'ROLE_SUPER_ADMIN', 'Super Administrador Global con acceso total', false, 'LOCAL'),
  (gen_random_uuid(), 'ROLE_OPERARIO', 'Operario de Bandeja Unificada', false, 'LOCAL'),
  (gen_random_uuid(), 'ROLE_SUPERVISOR', 'Supervisor de Área con delegación', false, 'LOCAL')
ON CONFLICT (name) DO NOTHING;

-- ==========================================
-- 3. Mapeo User ↔ Role (ibpms_security_user_roles)
-- ==========================================
INSERT INTO ibpms_security_user_roles (user_id, role_id)
SELECT u.id, r.id FROM ibpms_security_user u, ibpms_security_role r
WHERE u.email = 'admin@alpha.com' AND r.name = 'ROLE_SUPER_ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO ibpms_security_user_roles (user_id, role_id)
SELECT u.id, r.id FROM ibpms_security_user u, ibpms_security_role r
WHERE u.email = 'analista_n1@alpha.com' AND r.name = 'ROLE_OPERARIO'
ON CONFLICT DO NOTHING;

-- ==========================================
-- 4. Feature Toggle por defecto
-- ==========================================
INSERT INTO ibpms_feature_toggles (id, tenant_id, toggle_key, enabled, changed_by, changed_at) VALUES
  (gen_random_uuid(), 'tenant_alpha', 'forceRouting', false, 'system', CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;
