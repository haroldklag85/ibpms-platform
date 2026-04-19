-- liquibase formatted sql
-- changeset arquitecto-lider:us036-ca9-delegation-reason
-- comment: CA-9 US-036 — Agrega columna reason a la tabla de delegaciones temporales

ALTER TABLE ibpms_security_delegation
    ADD COLUMN IF NOT EXISTS reason VARCHAR(500);

-- rollback ALTER TABLE ibpms_security_delegation DROP COLUMN IF EXISTS reason;
