-- Liquibase script to drop mock properties from ibpms_task

ALTER TABLE ibpms_task DROP COLUMN status;
ALTER TABLE ibpms_task DROP COLUMN blocked_reason;
ALTER TABLE ibpms_task DROP CONSTRAINT IF EXISTS fk_ibpms_task_parent;
ALTER TABLE ibpms_task DROP COLUMN parent_task_id;
