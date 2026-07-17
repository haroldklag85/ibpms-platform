-- 063-seed-bpmn-release-manager-role.sql
-- US-005/US-036: Seed del rol BPMN_Release_Manager para deploy granular
INSERT INTO ibpms_security_role (id, name, description, is_system_role, created_at)
SELECT gen_random_uuid(), 'ROLE_BPMN_Release_Manager', 'Rol especializado para despliegue de definiciones BPMN', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM ibpms_security_role WHERE name = 'ROLE_BPMN_Release_Manager');
