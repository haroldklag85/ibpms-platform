-- liquibase formatted sql
-- changeset antigravity:30-tenant-fk

CREATE TABLE ibpms_tenant (
    slug VARCHAR(100) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);
INSERT INTO ibpms_tenant (slug, name) VALUES ('default', 'Tenant Global Default');
INSERT INTO ibpms_tenant (slug, name) VALUES ('tenant_alpha', 'Tenant Primario de Operaciones');

-- Agregar FKs a todas las tablas base de dominio
ALTER TABLE ibpms_workdesk_projection 
ADD CONSTRAINT fk_workdesk_tenant FOREIGN KEY (tenant_id) REFERENCES ibpms_tenant(slug);
