-- liquibase formatted sql
-- changeset antigravity:38-us007-dmn-manual-edit-schema

ALTER TABLE ibpms_dmn_definitions 
ADD COLUMN is_manual BOOLEAN DEFAULT FALSE;
