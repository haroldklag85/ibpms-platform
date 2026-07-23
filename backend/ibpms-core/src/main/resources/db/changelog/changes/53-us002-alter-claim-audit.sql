-- liquibase formatted sql

-- changeset antigravity:53-us002-alter-claim-audit
-- comment: US-002 V2 - Agregar columna para internal message en claim_audit_log (Unclaim)
-- @Traceability: US-002 - CA-04

ALTER TABLE claim_audit_log
    ADD COLUMN internal_message TEXT;
