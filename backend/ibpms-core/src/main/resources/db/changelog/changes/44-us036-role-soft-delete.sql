-- liquibase formatted sql
-- changeset antigravity:44-us036-role-soft-delete

ALTER TABLE ibpms_security_role ADD COLUMN is_active BOOLEAN DEFAULT TRUE NOT NULL;

-- rollback ALTER TABLE ibpms_security_role DROP COLUMN is_active;
