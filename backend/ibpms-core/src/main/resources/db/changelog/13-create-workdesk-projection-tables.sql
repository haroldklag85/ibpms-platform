-- liquibase formatted sql

-- changeset ibpms:13-create-workdesk-projection-tables
CREATE TABLE ibpms_workdesk_projection (
    id VARCHAR(255) PRIMARY KEY, /* Hash original ID + Source System */
    source_system VARCHAR(50) NOT NULL, /* BPMN o KANBAN */
    original_task_id VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    assignee VARCHAR(255),
    candidate_group VARCHAR(255),
    sla_expiration_date TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    payload_metadata JSON
);

-- REGLA ARQUITECTURA: AC-1 (NFR-PER-01) - Indexación obligatoria para consultas CQRS rápidas
CREATE INDEX idx_workdesk_sla ON ibpms_workdesk_projection(sla_expiration_date);
CREATE INDEX idx_workdesk_assignee ON ibpms_workdesk_projection(assignee);
CREATE INDEX idx_workdesk_group ON ibpms_workdesk_projection(candidate_group);
