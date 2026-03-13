--liquibase formatted sql
--changeset hb-dev:11-create-cognitive-bpmn-tables

CREATE TABLE ibpms_prompt_template (
    id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    template_string TEXT NOT NULL,
    version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_prompt_template PRIMARY KEY (id),
    CONSTRAINT uq_prompt_template_name UNIQUE (name)
);

CREATE TABLE ibpms_ai_audit_log (
    id UUID NOT NULL,
    execution_id VARCHAR(64) NOT NULL,
    system_prompt TEXT NOT NULL,
    response_payload TEXT,
    confidence_score DOUBLE PRECISION,
    chain_of_thought TEXT,
    human_override TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_ai_audit_log PRIMARY KEY (id)
);
CREATE INDEX idx_ai_audit_execution ON ibpms_ai_audit_log(execution_id);
