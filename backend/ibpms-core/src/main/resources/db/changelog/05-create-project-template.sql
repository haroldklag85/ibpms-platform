-- liquibase formatted sql

-- changeset backend-agent:05-create-project-template
CREATE TABLE ibpms_project_template (
    id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE ibpms_pt_phase (
    id VARCHAR(36) NOT NULL,
    template_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    phase_order INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_phase_template FOREIGN KEY (template_id) REFERENCES ibpms_project_template(id) ON DELETE CASCADE
);

CREATE TABLE ibpms_pt_milestone (
    id VARCHAR(36) NOT NULL,
    phase_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    milestone_order INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_milestone_phase FOREIGN KEY (phase_id) REFERENCES ibpms_pt_phase(id) ON DELETE CASCADE
);

CREATE TABLE ibpms_pt_task (
    id VARCHAR(36) NOT NULL,
    milestone_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    duration_days INT NOT NULL,
    role_id VARCHAR(100),
    form_key VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_task_milestone FOREIGN KEY (milestone_id) REFERENCES ibpms_pt_milestone(id) ON DELETE CASCADE
);

CREATE TABLE ibpms_pt_dependency (
    id VARCHAR(36) NOT NULL,
    source_task_id VARCHAR(36) NOT NULL,
    target_task_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_dep_source FOREIGN KEY (source_task_id) REFERENCES ibpms_pt_task(id) ON DELETE CASCADE,
    CONSTRAINT fk_dep_target FOREIGN KEY (target_task_id) REFERENCES ibpms_pt_task(id) ON DELETE CASCADE
);
