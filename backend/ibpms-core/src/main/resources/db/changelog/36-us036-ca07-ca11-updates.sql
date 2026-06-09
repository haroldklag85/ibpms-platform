-- US-036 CA-07: Soft-Delete Refactor (is_active -> status)
ALTER TABLE ibpms_security_user ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE';
UPDATE ibpms_security_user SET status = 'INACTIVE' WHERE is_active = false;
ALTER TABLE ibpms_security_user ALTER COLUMN status SET NOT NULL;
ALTER TABLE ibpms_security_user DROP COLUMN is_active;

-- US-036 CA-09: Nueva tabla de Delegación Piramidal
CREATE TABLE ibpms_security_role_delegation (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    delegate_id UUID NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT true,
    CONSTRAINT fk_delegation_owner FOREIGN KEY (owner_id) REFERENCES ibpms_security_user(id)
);

-- US-036 CA-10: Expiración de Cuentas de Servicio
-- REMOVIDO: La tabla correcta es ibpms_service_accounts y ya incluye expires_at desde 20-us036-rbac-schema.sql
