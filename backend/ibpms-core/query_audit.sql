SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'ibpms_impersonation_audit_log';

-- 3. Insert with INVALID action (should fail)
INSERT INTO ibpms_impersonation_audit_log (admin_id, target_user_id, action) 
VALUES ('11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', 'INVALID');

-- 4. Insert with START action (should work)
INSERT INTO ibpms_impersonation_audit_log (admin_id, target_user_id, action) 
VALUES ('11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', 'START');
