-- liquibase formatted sql

-- changeset antigravity:21-us039-generic-form-schema

-- CA-5: Configuración de Whitelist Regex por Proceso
ALTER TABLE ibpms_bpmn_process_design ADD COLUMN generic_form_whitelist JSONB;

-- CA-6: Catálogo Configurable de Roles VIP para Bloqueo Pre-Flight
ALTER TABLE ibpms_roles ADD COLUMN is_vip_restricted BOOLEAN DEFAULT FALSE;

-- Seed Data: marcar como is_vip_restricted = true a los roles VIP principales si no existen previamente
-- Insertamos los roles base o los actualizamos si ya existen
INSERT INTO ibpms_roles (id, name, description, is_vip_restricted, source)
VALUES 
    (gen_random_uuid(), 'ALTA_DIRECCION', 'Alta Dirección', TRUE, 'LOCAL'),
    (gen_random_uuid(), 'APROBADOR_FINANCIERO', 'Aprobador Financiero', TRUE, 'LOCAL'),
    (gen_random_uuid(), 'SELLO_LEGAL', 'Sello Legal', TRUE, 'LOCAL')
ON CONFLICT (name) DO UPDATE SET is_vip_restricted = TRUE;
