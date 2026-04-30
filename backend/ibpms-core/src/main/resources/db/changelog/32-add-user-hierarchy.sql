-- liquibase formatted sql

-- changeset antigravity:32-add-user-hierarchy
-- comment: Agrega campo manager_id para soportar delegación jerárquica en base a requerimientos UAT J-04 (PRE-04)
ALTER TABLE ibpms_security_user ADD COLUMN manager_id UUID REFERENCES ibpms_security_user(id);
