--liquibase formatted sql
--changeset ibpms:27-us001-workdesk-progress

-- CA-23: Columnas para cálculo determinista de Avance
ALTER TABLE ibpms_workdesk_projection 
ADD COLUMN progress_percent SMALLINT DEFAULT NULL,
ADD COLUMN total_steps SMALLINT DEFAULT NULL,
ADD COLUMN current_step SMALLINT DEFAULT NULL,
ADD COLUMN process_definition_key VARCHAR(255) DEFAULT NULL;

-- CA-01/CA-03: Badge de tipo visual
-- (source_system ya existe: BPMN/KANBAN. No se necesita columna adicional.)

COMMENT ON COLUMN ibpms_workdesk_projection.progress_percent IS 'CA-23: Porcentaje 0-100 calculado server-side. NULL = N/D.';
COMMENT ON COLUMN ibpms_workdesk_projection.total_steps IS 'CA-23: Total UserTasks del proceso BPMN o columnas Kanban.';
COMMENT ON COLUMN ibpms_workdesk_projection.current_step IS 'CA-23: Índice ordinal actual (1-based).';
