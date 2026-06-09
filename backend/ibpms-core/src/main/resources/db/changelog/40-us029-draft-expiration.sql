-- liquibase formatted sql

-- changeset antigravity:40-us029-draft-expiration
-- comment: Agrega columna draft_expires_at a ibpms_agile_tasks para soporte de TTL en borradores (US-029 CA-26)

ALTER TABLE ibpms_agile_tasks ADD COLUMN IF NOT EXISTS draft_expires_at TIMESTAMP;

COMMENT ON COLUMN ibpms_agile_tasks.draft_expires_at IS 'Fecha de expiración del borrador (72h desde última edición). Usado para warning a 48h.';

CREATE INDEX IF NOT EXISTS idx_agile_tasks_draft_expires ON ibpms_agile_tasks(draft_expires_at) WHERE draft_expires_at IS NOT NULL;
