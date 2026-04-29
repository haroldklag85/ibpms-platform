-- liquibase formatted sql

-- changeset antigravity:35-arq02804-split-certification

-- Paso 1: Crear tabla segregada de certificaciones QA
CREATE TABLE ibpms_form_certifications (
    id UUID NOT NULL,
    form_definition_id UUID NOT NULL,
    is_qa_certified BOOLEAN NOT NULL DEFAULT FALSE,
    certified_schema_hash VARCHAR(64),
    certified_by VARCHAR(100),
    certified_at TIMESTAMP,
    CONSTRAINT pk_ibpms_form_certifications PRIMARY KEY (id),
    CONSTRAINT fk_fc_form_definition FOREIGN KEY (form_definition_id) REFERENCES ibpms_form_definitions(id) ON DELETE CASCADE
);

-- Paso 2: Migración de datos existentes
INSERT INTO ibpms_form_certifications (id, form_definition_id, is_qa_certified, certified_schema_hash, certified_by, certified_at)
SELECT gen_random_uuid(), id, is_qa_certified, certified_schema_hash, certified_by, certified_at
FROM ibpms_form_definitions;

-- Paso 3: Eliminar columnas migradas de la tabla original
ALTER TABLE ibpms_form_definitions
DROP COLUMN is_qa_certified,
DROP COLUMN certified_schema_hash,
DROP COLUMN certified_by,
DROP COLUMN certified_at;

-- rollback ALTER TABLE ibpms_form_definitions ADD COLUMN is_qa_certified BOOLEAN NOT NULL DEFAULT FALSE;
-- rollback ALTER TABLE ibpms_form_definitions ADD COLUMN certified_schema_hash VARCHAR(64);
-- rollback ALTER TABLE ibpms_form_definitions ADD COLUMN certified_by VARCHAR(100);
-- rollback ALTER TABLE ibpms_form_definitions ADD COLUMN certified_at TIMESTAMP;
-- rollback DROP TABLE ibpms_form_certifications;
