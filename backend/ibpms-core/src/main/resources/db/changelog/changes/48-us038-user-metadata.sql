--liquibase formatted sql

--changeset ibpms-infra:48-us038-user-metadata.sql context:dev,uat,prod
--comment: Soporte DDL para Claims JIT de EntraID y Auditoria Break-Glass (US-038)

ALTER TABLE ibpms_security_user
ADD COLUMN jit_claims_json JSONB;

ALTER TABLE ibpms_security_audit_log
ADD COLUMN is_break_glass BOOLEAN DEFAULT false NOT NULL;

ALTER TABLE ibpms_security_audit_log
ADD COLUMN justification TEXT;
