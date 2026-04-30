INSERT INTO ibpms_security_role (id, name, description, is_vip_restricted, source, is_template)
VALUES (gen_random_uuid(), 'ROLE_ALTA_DIRECCION', 'Alta Direccion VIP', true, 'LOCAL', false)
ON CONFLICT (name) DO UPDATE SET is_vip_restricted = true;

INSERT INTO ibpms_security_user (id, username, email, password_hash, is_active, is_external_idp, created_at)
VALUES (gen_random_uuid(), 'vip_director', 'vip_director@alpha.com', '$2b$10$1OHQ9PUOg9z6LChpq2gtF.6lfkZww5rBsFXjtBA4YBwZkwHVlgmri', true, false, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

INSERT INTO ibpms_security_user_roles (user_id, role_id)
SELECT u.id, r.id FROM ibpms_security_user u CROSS JOIN ibpms_security_role r 
WHERE u.email = 'vip_director@alpha.com' AND r.name = 'ROLE_ALTA_DIRECCION'
ON CONFLICT DO NOTHING;
