--liquibase formatted sql
--changeset architect:us025-user-preferences-create

CREATE TABLE ibpms_user_preferences (
    user_id          UUID NOT NULL,
    preference_key   VARCHAR(100) NOT NULL,
    preference_value VARCHAR(500) NOT NULL,
    updated_at       TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (user_id, preference_key)
);

COMMENT ON TABLE ibpms_user_preferences IS 'US-025 ARQ-025-09: Persistencia de preferencias de interfaz (ej. UI Density) para asegurar continuidad multi-dispositivo.';
COMMENT ON COLUMN ibpms_user_preferences.user_id IS 'ID del usuario (FK lógica al sistema de identidad o UUID)';
COMMENT ON COLUMN ibpms_user_preferences.preference_key IS 'Clave de preferencia (ej. ui_density, theme_mode)';
COMMENT ON COLUMN ibpms_user_preferences.preference_value IS 'Valor de la preferencia (ej. compact, dark)';
