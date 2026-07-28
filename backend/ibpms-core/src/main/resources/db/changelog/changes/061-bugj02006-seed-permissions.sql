-- liquibase formatted sql
-- changeset admin:061-bugj02006-seed-permissions

-- 1. Insert WORKDESK_ACCESS permission
INSERT INTO ibpms_security_permission (id, name, description) 
VALUES ('c95f12a3-952b-422f-87a3-1b9c2a8c3d1f', 'WORKDESK_ACCESS', 'Acceso al módulo Workdesk')
ON CONFLICT (name) DO NOTHING;

-- 2. Asignar a ROLE_USER_INTERNAL
INSERT INTO ibpms_security_role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM ibpms_security_role r, ibpms_security_permission p
WHERE r.name = 'ROLE_USER_INTERNAL' AND p.name = 'WORKDESK_ACCESS'
AND NOT EXISTS (
    SELECT 1 FROM ibpms_security_role_permissions rp 
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);
