-- Cleanup conflicting local admin users to allow admin@alpha.com to be the primary 'admin'
DELETE FROM ibpms_security_user_roles 
WHERE user_id IN (
  SELECT id FROM ibpms_security_user 
  WHERE username = 'admin' AND email != 'admin@alpha.com'
);

DELETE FROM ibpms_security_user 
WHERE username = 'admin' AND email != 'admin@alpha.com';
