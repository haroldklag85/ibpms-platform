-- liquibase formatted sql

-- changeset ibpms:26-us001-workdesk-schema

-- Extensión para CA-10 / CA-19 (PG_TRGM para Búsquedas GIN tolerantes a fallos)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Adición de Columnas a la Proyección
ALTER TABLE ibpms_workdesk_projection 
ADD COLUMN tenant_id VARCHAR(50) NOT NULL DEFAULT 'default',
ADD COLUMN impact_level INT NOT NULL DEFAULT 0;

-- Construcción de Índices SRE y Negocio
CREATE INDEX idx_workdesk_title_trgm ON ibpms_workdesk_projection USING gin (title gin_trgm_ops);
CREATE INDEX idx_workdesk_tenant ON ibpms_workdesk_projection(tenant_id);
CREATE INDEX idx_workdesk_impact ON ibpms_workdesk_projection(impact_level);
