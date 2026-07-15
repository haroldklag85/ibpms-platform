-- Changeset: 062-lane-role-assignment-tables
-- Author: ibpms-architect
-- US-005/US-036 Extension: Lane Actor Assignment + RBAC Lane Integration
-- @Traceability: US-005/US-036 - ADR-009, ADR-015

-- ============================================================
-- Tabla 1: Lanes BPMN como entidad de primer nivel
-- Permite registrar lanes del XML BPMN como filas consultables
-- ============================================================
CREATE TABLE IF NOT EXISTS ibpms_bpmn_lane (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    process_design_id UUID NOT NULL,
    lane_xml_id       VARCHAR(150) NOT NULL,
    lane_name         VARCHAR(255) NOT NULL,
    actor_description VARCHAR(500),
    linked_role_id    UUID,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_lane_process FOREIGN KEY (process_design_id)
        REFERENCES ibpms_bpmn_process_design(id) ON DELETE CASCADE,
    CONSTRAINT fk_lane_linked_role FOREIGN KEY (linked_role_id)
        REFERENCES ibpms_security_role(id) ON DELETE SET NULL,
    CONSTRAINT uq_lane_per_process UNIQUE (process_design_id, lane_xml_id)
);

CREATE INDEX idx_bpmn_lane_process ON ibpms_bpmn_lane(process_design_id);

-- ============================================================
-- Tabla 2: Asignación Lane↔Rol (Many-to-Many con granularidad I/E)
-- Un rol puede tener permisos Initiate y/o Execute por lane
-- ============================================================
CREATE TABLE IF NOT EXISTS ibpms_lane_role_assignment (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lane_id         UUID NOT NULL,
    role_id         UUID NOT NULL,
    can_initiate    BOOLEAN NOT NULL DEFAULT false,
    can_execute     BOOLEAN NOT NULL DEFAULT true,
    assigned_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    assigned_by     VARCHAR(255),
    CONSTRAINT fk_lra_lane FOREIGN KEY (lane_id)
        REFERENCES ibpms_bpmn_lane(id) ON DELETE CASCADE,
    CONSTRAINT fk_lra_role FOREIGN KEY (role_id)
        REFERENCES ibpms_security_role(id) ON DELETE CASCADE,
    CONSTRAINT uq_lane_role UNIQUE (lane_id, role_id)
);

CREATE INDEX idx_lra_lane ON ibpms_lane_role_assignment(lane_id);
CREATE INDEX idx_lra_role ON ibpms_lane_role_assignment(role_id);
