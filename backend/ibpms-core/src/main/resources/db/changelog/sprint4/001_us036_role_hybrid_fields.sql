-- liquibase formatted sql
-- changeset arquitecto-lider:us036-ca1-role-hybrid-fields
-- comment: CA-1 US-036 — Campos híbridos para soportar Modelo de Rol Global (EntraID/LOCAL) y Plantillas Clonables

ALTER TABLE ibpms_security_role
    ADD COLUMN IF NOT EXISTS is_template BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS source      VARCHAR(50) NOT NULL DEFAULT 'LOCAL';

-- Marcar ROOT como no-plantilla y fuente LOCAL de forma explícita
UPDATE ibpms_security_role
SET is_template = FALSE,
    source      = 'LOCAL'
WHERE name = 'ROLE_SUPER_ADMIN';

-- rollback ALTER TABLE ibpms_security_role DROP COLUMN IF EXISTS is_template, DROP COLUMN IF EXISTS source;
