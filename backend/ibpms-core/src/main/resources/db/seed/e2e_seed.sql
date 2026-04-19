-- Tenants E2E
INSERT INTO tenants (id, name, domain) VALUES 
  ('tenant_alpha', 'Alpha Corp', 'alpha.com'),
  ('tenant_beta', 'Beta Inc', 'beta.com')
ON CONFLICT (id) DO NOTHING;

-- Usuarios E2E (passwords: BCrypt de 'Test1234!')
INSERT INTO users (id, email, password_hash, tenant_id, display_name) VALUES
  ('usr_admin_alpha', 'admin@alpha.com', '$2a$10$DFbDYKETdPj16DESqtusCeTPksmRuSi/TKlPiAtLRDLIYDW0sL3k.', 'tenant_alpha', 'Admin Alpha'),
  ('usr_oper_alpha', 'operario@alpha.com', '$2a$10$DFbDYKETdPj16DESqtusCeTPksmRuSi/TKlPiAtLRDLIYDW0sL3k.', 'tenant_alpha', 'Operario Alpha'),
  ('usr_arch_alpha', 'arquitecto@alpha.com', '$2a$10$DFbDYKETdPj16DESqtusCeTPksmRuSi/TKlPiAtLRDLIYDW0sL3k.', 'tenant_alpha', 'Arquitecto Alpha'),
  ('usr_admin_beta', 'admin@beta.com', '$2a$10$DFbDYKETdPj16DESqtusCeTPksmRuSi/TKlPiAtLRDLIYDW0sL3k.', 'tenant_beta', 'Admin Beta'),
  ('usr_oper_beta', 'operario@beta.com', '$2a$10$DFbDYKETdPj16DESqtusCeTPksmRuSi/TKlPiAtLRDLIYDW0sL3k.', 'tenant_beta', 'Operario Beta')
ON CONFLICT (id) DO NOTHING;

-- Roles RBAC
INSERT INTO user_roles (user_id, role) VALUES
  ('usr_admin_alpha', 'ROLE_SUPER_ADMIN'),
  ('usr_oper_alpha', 'ROLE_OPERARIO'),
  ('usr_arch_alpha', 'ROLE_PROCESS_ARCHITECT'),
  ('usr_admin_beta', 'ROLE_SUPER_ADMIN'),
  ('usr_oper_beta', 'ROLE_OPERARIO')
ON CONFLICT DO NOTHING;
