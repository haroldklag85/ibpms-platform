-- 1. Insert tenants to ibpms_tenant
INSERT INTO ibpms_tenant (slug, name, is_active) 
VALUES 
  ('tenant_alpha', 'Tenant Primario de Operaciones', true),
  ('tenant_sso', 'SSO Tenant', true),
  ('tenant_ibpms', 'iBPMS Tenant', true),
  ('tenant_local', 'Local Tenant', true)
ON CONFLICT (slug) DO NOTHING;

-- 2. Ensure users exist and reset statuses
-- admin@alpha.com
INSERT INTO ibpms_security_user (id, username, email, password_hash, status, is_external_idp, created_at)
VALUES 
  ('20000000-0000-0000-0000-000000000001', 'admin', 'admin@alpha.com', '$2b$10$1OHQ9PUOg9z6LChpq2gtF.6lfkZww5rBsFXjtBA4YBwZkwHVlgmri', 'ACTIVE', false, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO UPDATE SET id = EXCLUDED.id, status = 'ACTIVE', password_hash = EXCLUDED.password_hash;

-- bpmn_release_manager@sso.local
INSERT INTO ibpms_security_user (id, username, email, password_hash, status, is_external_idp, created_at)
VALUES 
  ('20000000-0000-0000-0000-000000000002', 'BPMN_Release_Manager', 'bpmn_release_manager@sso.local', '$2b$10$1OHQ9PUOg9z6LChpq2gtF.6lfkZww5rBsFXjtBA4YBwZkwHVlgmri', 'ACTIVE', false, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO UPDATE SET id = EXCLUDED.id, status = 'ACTIVE', password_hash = EXCLUDED.password_hash;

-- root@ibpms.local
INSERT INTO ibpms_security_user (id, username, email, password_hash, status, is_external_idp, created_at)
VALUES 
  ('20000000-0000-0000-0000-000000000003', '[Super_Administrador]', 'root@ibpms.local', '$2b$10$1OHQ9PUOg9z6LChpq2gtF.6lfkZww5rBsFXjtBA4YBwZkwHVlgmri', 'ACTIVE', false, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO UPDATE SET id = EXCLUDED.id, status = 'ACTIVE', password_hash = EXCLUDED.password_hash;

-- 3. Clean up existing tasks from projection
DELETE FROM ibpms_workdesk_projection 
WHERE id IN ('task_admin_1', 'task_admin_2', 'task_rm_1', 'task_rm_2', 'task_root_1', 'task_root_2', 'task_pool_1', 'task_pool_2', 'task_pool_3');

-- 4. Insert tasks for admin (tenant_alpha) - PERSONAL view
INSERT INTO ibpms_workdesk_projection (id, source_system, original_task_id, title, assignee, candidate_group, sla_expiration_date, status, tenant_id, impact_level, progress_percent, category_tag)
VALUES 
  ('task_admin_1', 'BPMN', 'task_admin_1', 'Aprobación de Solicitud de Crédito E2E (Alta Prioridad)', 'admin', 'ROLE_SUPER_ADMIN', CURRENT_TIMESTAMP + INTERVAL '12 hours', 'PENDING', 'tenant_alpha', 10, 75, 'Crédito'),
  ('task_admin_2', 'KANBAN', 'task_admin_2', 'Verificación de Perfil de Seguridad (Admin)', 'admin', 'ROLE_SUPER_ADMIN', CURRENT_TIMESTAMP + INTERVAL '2 days', 'PENDING', 'tenant_alpha', 8, 50, 'Seguridad');

-- 5. Insert tasks for BPMN_Release_Manager (tenant_sso) - PERSONAL view
INSERT INTO ibpms_workdesk_projection (id, source_system, original_task_id, title, assignee, candidate_group, sla_expiration_date, status, tenant_id, impact_level, progress_percent, category_tag)
VALUES 
  ('task_rm_1', 'BPMN', 'task_rm_1', 'Despliegue de Proceso Core BPMN', 'BPMN_Release_Manager', 'ROLE_SUPER_ADMIN', CURRENT_TIMESTAMP + INTERVAL '6 hours', 'PENDING', 'tenant_sso', 9, 20, 'BPMN'),
  ('task_rm_2', 'KANBAN', 'task_rm_2', 'Liberación de Versión J-02', 'BPMN_Release_Manager', 'ROLE_SUPER_ADMIN', CURRENT_TIMESTAMP + INTERVAL '3 days', 'PENDING', 'tenant_sso', 6, 80, 'Release');

-- 6. Insert tasks for [Super_Administrador] (tenant_ibpms) - PERSONAL view
INSERT INTO ibpms_workdesk_projection (id, source_system, original_task_id, title, assignee, candidate_group, sla_expiration_date, status, tenant_id, impact_level, progress_percent, category_tag)
VALUES 
  ('task_root_1', 'BPMN', 'task_root_1', 'Auditoría de Logs del Sistema - Root', '[Super_Administrador]', 'ROLE_SUPER_ADMIN', CURRENT_TIMESTAMP + INTERVAL '12 hours', 'PENDING', 'tenant_ibpms', 10, 10, 'Auditoría'),
  ('task_root_2', 'KANBAN', 'task_root_2', 'Configuración de Parámetros Globales iBPMS', '[Super_Administrador]', 'ROLE_SUPER_ADMIN', CURRENT_TIMESTAMP + INTERVAL '4 days', 'PENDING', 'tenant_ibpms', 7, 30, 'Configuración');

-- 7. Insert pool tasks (assignee IS NULL) for admin / tenant_alpha - POOL view
INSERT INTO ibpms_workdesk_projection (id, source_system, original_task_id, title, assignee, candidate_group, sla_expiration_date, status, tenant_id, impact_level, progress_percent, category_tag)
VALUES 
  ('task_pool_1', 'BPMN', 'task_pool_1', 'Validar Documentación de Soporte (Cola)', NULL, 'ROLE_SUPER_ADMIN', CURRENT_TIMESTAMP + INTERVAL '8 hours', 'PENDING', 'tenant_alpha', 6, null, 'Soporte'),
  ('task_pool_2', 'BPMN', 'task_pool_2', 'Revisión y Firma de Acta de Cierre (Cola)', NULL, 'ROLE_SUPER_ADMIN', CURRENT_TIMESTAMP + INTERVAL '1 days', 'PENDING', 'tenant_alpha', 7, null, 'Legal');

-- 8. Insert pool tasks (assignee IS NULL) for BPMN_Release_Manager / tenant_sso - POOL view
INSERT INTO ibpms_workdesk_projection (id, source_system, original_task_id, title, assignee, candidate_group, sla_expiration_date, status, tenant_id, impact_level, progress_percent, category_tag)
VALUES 
  ('task_pool_3', 'BPMN', 'task_pool_3', 'Certificación de Entorno SSO (Cola)', NULL, 'ROLE_SUPER_ADMIN', CURRENT_TIMESTAMP + INTERVAL '4 hours', 'PENDING', 'tenant_sso', 9, null, 'Seguridad');
