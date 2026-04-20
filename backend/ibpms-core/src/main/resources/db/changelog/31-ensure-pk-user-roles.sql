-- liquibase formatted sql
-- changeset antigravity:31-ensure-pk-user-roles

ALTER TABLE ibpms_security_user_roles 
ADD CONSTRAINT pk_ibpms_sec_user_roles PRIMARY KEY (user_id, role_id);
