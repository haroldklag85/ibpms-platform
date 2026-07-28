-- 1. Tenants
INSERT INTO ibpms_tenant (slug, name, is_active) 
VALUES 
  ('tenant_alpha', 'Tenant Primario de Operaciones', true),
  ('tenant_sso', 'SSO Tenant', true),
  ('tenant_ibpms', 'iBPMS Tenant', true),
  ('tenant_local', 'Local Tenant', true)
ON CONFLICT (slug) DO NOTHING;

-- 2. UAT Users
DELETE FROM ibpms_security_user_roles WHERE user_id IN (SELECT id FROM ibpms_security_user WHERE email IN ('admin@alpha.com', 'bpmn_release_manager@sso.local', 'root@ibpms.local'));
DELETE FROM ibpms_security_user WHERE email IN ('admin@alpha.com', 'bpmn_release_manager@sso.local', 'root@ibpms.local');

INSERT INTO ibpms_security_user (id, username, email, password_hash, status, is_external_idp, created_at, must_change_password)
VALUES (
  '20000000-0000-0000-0000-000000000001', 
  'admin', 
  'admin@alpha.com', 
  '$2b$10$1OHQ9PUOg9z6LChpq2gtF.6lfkZww5rBsFXjtBA4YBwZkwHVlgmri', 
  'ACTIVE', 
  false, 
  CURRENT_TIMESTAMP, 
  false
) ON CONFLICT (email) DO UPDATE SET password_hash = EXCLUDED.password_hash, status = 'ACTIVE';

INSERT INTO ibpms_security_user (id, username, email, password_hash, status, is_external_idp, created_at, must_change_password)
VALUES (
  '20000000-0000-0000-0000-000000000005', 
  'BPMN_Release_Manager', 
  'bpmn_release_manager@sso.local', 
  '$2b$10$1OHQ9PUOg9z6LChpq2gtF.6lfkZww5rBsFXjtBA4YBwZkwHVlgmri', 
  'ACTIVE', 
  false, 
  CURRENT_TIMESTAMP, 
  false
) ON CONFLICT (email) DO UPDATE SET password_hash = EXCLUDED.password_hash, status = 'ACTIVE';

INSERT INTO ibpms_security_user (id, username, email, password_hash, status, is_external_idp, created_at, must_change_password)
VALUES (
  '20000000-0000-0000-0000-000000000003', 
  '[Super_Administrador]', 
  'root@ibpms.local', 
  '$2b$10$1OHQ9PUOg9z6LChpq2gtF.6lfkZww5rBsFXjtBA4YBwZkwHVlgmri', 
  'ACTIVE', 
  false, 
  CURRENT_TIMESTAMP, 
  false
) ON CONFLICT (email) DO UPDATE SET password_hash = EXCLUDED.password_hash, status = 'ACTIVE';

-- 3. Roles Mappings
INSERT INTO ibpms_security_user_roles (user_id, role_id)
SELECT '20000000-0000-0000-0000-000000000001', id FROM ibpms_security_role WHERE name IN ('SUPER_ADMIN', 'ROLE_SUPER_ADMIN')
ON CONFLICT DO NOTHING;

INSERT INTO ibpms_security_user_roles (user_id, role_id)
SELECT '20000000-0000-0000-0000-000000000005', id FROM ibpms_security_role WHERE name IN ('SUPER_ADMIN', 'ROLE_SUPER_ADMIN')
ON CONFLICT DO NOTHING;

INSERT INTO ibpms_security_user_roles (user_id, role_id)
SELECT '20000000-0000-0000-0000-000000000003', id FROM ibpms_security_role WHERE name IN ('SUPER_ADMIN', 'ROLE_SUPER_ADMIN')
ON CONFLICT DO NOTHING;

-- 4. Tasks from inject_seed.sql
DELETE FROM ibpms_workdesk_projection 
WHERE id IN ('task_admin_1', 'task_admin_2', 'task_rm_1', 'task_rm_2', 'task_root_1', 'task_root_2', 'task_pool_1', 'task_pool_2', 'task_pool_3');

INSERT INTO ibpms_workdesk_projection (id, source_system, original_task_id, title, assignee, candidate_group, sla_expiration_date, status, tenant_id, impact_level, progress_percent, category_tag)
VALUES 
  ('task_admin_1', 'BPMN', 'task_admin_1', 'Aprobación de Solicitud de Crédito E2E (Alta Prioridad)', 'admin', 'ROLE_SUPER_ADMIN', CURRENT_TIMESTAMP + INTERVAL '12 hours', 'PENDING', 'tenant_alpha', 10, 75, 'Crédito'),
  ('task_admin_2', 'KANBAN', 'task_admin_2', 'Verificación de Perfil de Seguridad (Admin)', 'admin', 'ROLE_SUPER_ADMIN', CURRENT_TIMESTAMP + INTERVAL '2 days', 'PENDING', 'tenant_alpha', 8, 50, 'Seguridad'),
  ('task_rm_1', 'BPMN', 'task_rm_1', 'Despliegue de Proceso Core BPMN', 'BPMN_Release_Manager', 'ROLE_SUPER_ADMIN', CURRENT_TIMESTAMP + INTERVAL '6 hours', 'PENDING', 'tenant_sso', 9, 20, 'BPMN'),
  ('task_rm_2', 'KANBAN', 'task_rm_2', 'Liberación de Versión J-02', 'BPMN_Release_Manager', 'ROLE_SUPER_ADMIN', CURRENT_TIMESTAMP + INTERVAL '3 days', 'PENDING', 'tenant_sso', 6, 80, 'Release'),
  ('task_root_1', 'BPMN', 'task_root_1', 'Auditoría de Logs del Sistema - Root', '[Super_Administrador]', 'ROLE_SUPER_ADMIN', CURRENT_TIMESTAMP + INTERVAL '12 hours', 'PENDING', 'tenant_ibpms', 10, 10, 'Auditoría'),
  ('task_root_2', 'KANBAN', 'task_root_2', 'Configuración de Parámetros Globales iBPMS', '[Super_Administrador]', 'ROLE_SUPER_ADMIN', CURRENT_TIMESTAMP + INTERVAL '4 days', 'PENDING', 'tenant_ibpms', 7, 30, 'Configuración'),
  ('task_pool_1', 'BPMN', 'task_pool_1', 'Validar Documentación de Soporte (Cola)', NULL, 'ROLE_SUPER_ADMIN', CURRENT_TIMESTAMP + INTERVAL '8 hours', 'PENDING', 'tenant_alpha', 6, null, 'Soporte'),
  ('task_pool_2', 'BPMN', 'task_pool_2', 'Revisión y Firma de Acta de Cierre (Cola)', NULL, 'ROLE_SUPER_ADMIN', CURRENT_TIMESTAMP + INTERVAL '1 days', 'PENDING', 'tenant_alpha', 7, null, 'Legal'),
  ('task_pool_3', 'BPMN', 'task_pool_3', 'Certificación de Entorno SSO (Cola)', NULL, 'ROLE_SUPER_ADMIN', CURRENT_TIMESTAMP + INTERVAL '4 hours', 'PENDING', 'tenant_sso', 9, null, 'Seguridad');
