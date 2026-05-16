--liquibase formatted sql
--changeset architect:us003-form-design-remediation

-- 1. Asegurar la persistencia inmutable y estructura faltante según US-003
ALTER TABLE ibpms_form_design ADD COLUMN IF NOT EXISTS form_fields TEXT;
ALTER TABLE ibpms_form_design ADD COLUMN IF NOT EXISTS schema_content JSONB;

-- 2. Crear índice compuesto de performance para las consultas JPA
CREATE INDEX IF NOT EXISTS idx_formdesign_techname_version ON ibpms_form_design(technical_name, version DESC);
