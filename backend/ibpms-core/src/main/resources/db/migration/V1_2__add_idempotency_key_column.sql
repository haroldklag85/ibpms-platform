-- V1_2__add_idempotency_key_column.sql
-- Misión Crítica: Inyección de la columna faltante reportada por Tomcat 8080.
ALTER TABLE ibpms_idempotency_key ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(36);
