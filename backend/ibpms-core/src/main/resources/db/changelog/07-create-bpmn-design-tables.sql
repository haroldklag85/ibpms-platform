--liquibase formatted sql
--changeset hb-dev:7-create-bpmn-design-tables

-- Tabla principal de diseños de procesos BPMN
CREATE TABLE ibpms_bpmn_process_design (
    id BINARY(16) NOT NULL,
    name VARCHAR(200) NOT NULL,
    technical_id VARCHAR(200) NOT NULL,
    form_pattern VARCHAR(30) NOT NULL DEFAULT 'SIMPLE',
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    current_version INT NOT NULL DEFAULT 0,
    locked_by VARCHAR(100),
    locked_at TIMESTAMP NULL,
    xml_draft LONGTEXT,
    max_nodes INT NOT NULL DEFAULT 100,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    opt_lock_version BIGINT DEFAULT 0,
    CONSTRAINT pk_bpmn_process_design PRIMARY KEY (id),
    CONSTRAINT uq_bpmn_technical_id UNIQUE (technical_id)
);

CREATE INDEX idx_bpmn_design_status ON ibpms_bpmn_process_design(status);

-- Tabla de auditoría (Git-Log del diseñador)
CREATE TABLE ibpms_bpmn_design_audit_log (
    id BINARY(16) NOT NULL,
    process_design_id BINARY(16) NOT NULL,
    action VARCHAR(30) NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version_affected INT NOT NULL DEFAULT 0,
    details JSON,
    CONSTRAINT pk_bpmn_design_audit PRIMARY KEY (id),
    CONSTRAINT fk_audit_process_design FOREIGN KEY (process_design_id)
        REFERENCES ibpms_bpmn_process_design(id) ON DELETE CASCADE
);

CREATE INDEX idx_bpmn_audit_design_id ON ibpms_bpmn_design_audit_log(process_design_id);
CREATE INDEX idx_bpmn_audit_action ON ibpms_bpmn_design_audit_log(action);
