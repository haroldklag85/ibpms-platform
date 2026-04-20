-- liquibase formatted sql

-- changeset antigravity:29-consolidate-roles
-- comment: Consolidacion de esquema de roles multiples hacia tabla maestra ibpms_security_role
ALTER TABLE ibpms_security_role ADD COLUMN IF NOT EXISTS is_vip_restricted BOOLEAN DEFAULT FALSE;
ALTER TABLE ibpms_security_role ADD COLUMN IF NOT EXISTS process_definition_id VARCHAR(255);
ALTER TABLE ibpms_security_role ADD COLUMN IF NOT EXISTS lane_id VARCHAR(255);

-- Dropping legacy tables
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS ibpms_roles;
