-- liquibase formatted sql

-- changeset antigravity:29-consolidate-roles-schema
CREATE TABLE IF NOT EXISTS ibpms_security_role (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    parent_role_id UUID,
    is_template BOOLEAN NOT NULL DEFAULT false,
    source VARCHAR(50) NOT NULL DEFAULT 'LOCAL'
);

-- changeset antigravity:29-consolidate-roles
-- comment: Consolidacion de esquema de roles multiples hacia tabla maestra ibpms_security_role
ALTER TABLE ibpms_security_role ADD COLUMN IF NOT EXISTS is_vip_restricted BOOLEAN DEFAULT FALSE;
ALTER TABLE ibpms_security_role ADD COLUMN IF NOT EXISTS process_definition_id VARCHAR(255);
ALTER TABLE ibpms_security_role ADD COLUMN IF NOT EXISTS lane_id VARCHAR(255);

-- Dropping legacy tables
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS sys_role CASCADE;
DROP TABLE IF EXISTS ibpms_roles CASCADE;
