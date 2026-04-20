-- liquibase formatted sql
-- changeset antigravity:31-ensure-pk-user-roles endDelimiter:/
-- validCheckSum: ANY

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_type = 'PRIMARY KEY'
        AND table_name = 'ibpms_security_user_roles'
    ) THEN
        ALTER TABLE ibpms_security_user_roles ADD CONSTRAINT pk_ibpms_sec_user_roles PRIMARY KEY (user_id, role_id);
    END IF;
END $$;
/
