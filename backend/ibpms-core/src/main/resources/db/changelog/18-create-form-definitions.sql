--liquibase formatted sql
--changeset hb-dev:18-create-form-definitions

CREATE TABLE ibpms_form_definitions (
    id UUID NOT NULL,
    form_id UUID NOT NULL,
    version_id INT NOT NULL,
    schema_content JSONB NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    hash_sha256 VARCHAR(64) NOT NULL,
    CONSTRAINT pk_form_definitions PRIMARY KEY (id),
    CONSTRAINT uq_form_version UNIQUE (form_id, version_id),
    CONSTRAINT uq_form_hash_per_form UNIQUE (form_id, hash_sha256)
);

CREATE INDEX idx_form_definitions_form_id ON ibpms_form_definitions(form_id);
