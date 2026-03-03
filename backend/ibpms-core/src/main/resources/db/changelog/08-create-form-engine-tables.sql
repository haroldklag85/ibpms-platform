--liquibase formatted sql
--changeset hb-dev:8-create-form-engine-tables

CREATE TABLE ibpms_form_design (
    id BINARY(16) NOT NULL,
    name VARCHAR(150) NOT NULL,
    technical_name VARCHAR(100) NOT NULL,
    pattern VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    version INT NOT NULL,
    vue_template LONGTEXT,
    zod_schema LONGTEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    author_id VARCHAR(50) NOT NULL,
    CONSTRAINT pk_form_design PRIMARY KEY (id),
    CONSTRAINT uq_form_design_tech_version UNIQUE (technical_name, version)
);
CREATE INDEX idx_form_design_tech_name ON ibpms_form_design(technical_name);

CREATE TABLE ibpms_form_field_value_audit (
    id BINARY(16) NOT NULL,
    process_instance_id VARCHAR(64) NOT NULL,
    form_design_id BINARY(16) NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    changed_by VARCHAR(100) NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_form_field_value_audit PRIMARY KEY (id)
);
CREATE INDEX idx_ffva_instance_field ON ibpms_form_field_value_audit(process_instance_id, field_name);

CREATE TABLE ibpms_form_attachment_routing (
    id BINARY(16) NOT NULL,
    form_design_id BINARY(16) NOT NULL,
    component_id VARCHAR(100) NOT NULL,
    target_system VARCHAR(20) NOT NULL,
    target_config JSON,
    CONSTRAINT pk_form_attachment_routing PRIMARY KEY (id)
);
CREATE INDEX idx_far_form_design ON ibpms_form_attachment_routing(form_design_id);
