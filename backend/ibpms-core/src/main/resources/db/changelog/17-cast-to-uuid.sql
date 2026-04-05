-- liquibase formatted sql

-- changeset QA_Agent:17-cast-to-uuid
-- validCheckSum: ANY

-- 1. Drop foreign keys
ALTER TABLE ibpms_pt_dependency DROP CONSTRAINT IF EXISTS fk_dep_source;
ALTER TABLE ibpms_pt_dependency DROP CONSTRAINT IF EXISTS fk_dep_target;
ALTER TABLE ibpms_pt_task DROP CONSTRAINT IF EXISTS fk_task_milestone;
ALTER TABLE ibpms_pt_milestone DROP CONSTRAINT IF EXISTS fk_milestone_phase;
ALTER TABLE ibpms_pt_phase DROP CONSTRAINT IF EXISTS fk_phase_template;

-- 2. Alter columns
ALTER TABLE ibpms_project_template ALTER COLUMN id TYPE UUID USING id::uuid;

ALTER TABLE ibpms_pt_phase ALTER COLUMN id TYPE UUID USING id::uuid;
ALTER TABLE ibpms_pt_phase ALTER COLUMN template_id TYPE UUID USING template_id::uuid;

ALTER TABLE ibpms_pt_milestone ALTER COLUMN id TYPE UUID USING id::uuid;
ALTER TABLE ibpms_pt_milestone ALTER COLUMN phase_id TYPE UUID USING phase_id::uuid;

ALTER TABLE ibpms_pt_task ALTER COLUMN id TYPE UUID USING id::uuid;
ALTER TABLE ibpms_pt_task ALTER COLUMN milestone_id TYPE UUID USING milestone_id::uuid;

ALTER TABLE ibpms_pt_dependency ALTER COLUMN id TYPE UUID USING id::uuid;
ALTER TABLE ibpms_pt_dependency ALTER COLUMN source_task_id TYPE UUID USING source_task_id::uuid;
ALTER TABLE ibpms_pt_dependency ALTER COLUMN target_task_id TYPE UUID USING target_task_id::uuid;

-- 3. Recreate foreign keys
ALTER TABLE ibpms_pt_phase ADD CONSTRAINT fk_phase_template FOREIGN KEY (template_id) REFERENCES ibpms_project_template(id) ON DELETE CASCADE;
ALTER TABLE ibpms_pt_milestone ADD CONSTRAINT fk_milestone_phase FOREIGN KEY (phase_id) REFERENCES ibpms_pt_phase(id) ON DELETE CASCADE;
ALTER TABLE ibpms_pt_task ADD CONSTRAINT fk_task_milestone FOREIGN KEY (milestone_id) REFERENCES ibpms_pt_milestone(id) ON DELETE CASCADE;
ALTER TABLE ibpms_pt_dependency ADD CONSTRAINT fk_dep_source FOREIGN KEY (source_task_id) REFERENCES ibpms_pt_task(id) ON DELETE CASCADE;
ALTER TABLE ibpms_pt_dependency ADD CONSTRAINT fk_dep_target FOREIGN KEY (target_task_id) REFERENCES ibpms_pt_task(id) ON DELETE CASCADE;
