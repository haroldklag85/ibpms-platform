-- liquibase formatted sql

-- changeset ibpms-team:sprint6-bugs-fix
CREATE INDEX IF NOT EXISTS idx_workdesk_search 
ON ibpms_workdesk_projection (tenant_id, assignee, impact_level DESC, sla_expiration_date ASC);
