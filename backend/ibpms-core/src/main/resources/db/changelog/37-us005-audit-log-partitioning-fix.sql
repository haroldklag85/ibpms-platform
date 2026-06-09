--liquibase formatted sql
--changeset hb-dev:37-us005-audit-log-partitioning-fix

-- 1. Crear nueva tabla particionada por RANGE (mes) usando JSONB
CREATE TABLE ibpms_bpmn_design_audit_log_part (
    id UUID NOT NULL,
    process_design_id UUID NOT NULL,
    action VARCHAR(30) NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version_affected INT NOT NULL DEFAULT 0,
    details JSONB,
    CONSTRAINT pk_bpmn_design_audit_part PRIMARY KEY (id, timestamp),
    CONSTRAINT fk_audit_process_design_part FOREIGN KEY (process_design_id)
        REFERENCES ibpms_bpmn_process_design(id) ON DELETE CASCADE
) PARTITION BY RANGE (timestamp);

-- 2. Crear particiones iniciales (desde el momento de inicio del proyecto en 2026 hacia adelante)
CREATE TABLE ibpms_bpmn_design_audit_log_2026m05 
    PARTITION OF ibpms_bpmn_design_audit_log_part 
    FOR VALUES FROM ('2026-05-01 00:00:00') TO ('2026-06-01 00:00:00');

CREATE TABLE ibpms_bpmn_design_audit_log_2026m06 
    PARTITION OF ibpms_bpmn_design_audit_log_part 
    FOR VALUES FROM ('2026-06-01 00:00:00') TO ('2026-07-01 00:00:00');

CREATE TABLE ibpms_bpmn_design_audit_log_default 
    PARTITION OF ibpms_bpmn_design_audit_log_part DEFAULT;

-- 3. Volcar datos existentes (transformando de JSON a JSONB)
INSERT INTO ibpms_bpmn_design_audit_log_part (id, process_design_id, action, user_id, timestamp, version_affected, details)
SELECT id, process_design_id, action, user_id, timestamp, version_affected, details::jsonb 
FROM ibpms_bpmn_design_audit_log;

-- 4. Reemplazar la tabla vieja por la nueva particionada
DROP TABLE ibpms_bpmn_design_audit_log;
ALTER TABLE ibpms_bpmn_design_audit_log_part RENAME TO ibpms_bpmn_design_audit_log;

-- 5. Recrear índices para soporte de búsqueda rápida
CREATE INDEX idx_bpmn_audit_design_id ON ibpms_bpmn_design_audit_log(process_design_id);
CREATE INDEX idx_bpmn_audit_action ON ibpms_bpmn_design_audit_log(action);
CREATE INDEX idx_bpmn_audit_details_gin ON ibpms_bpmn_design_audit_log USING GIN (details);
