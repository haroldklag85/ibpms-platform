-- liquibase formatted sql
-- changeset antigravity:41-us007-dmn-schema-complement
-- comment: Schema complementario para US-007 Bloque 1 (Auditoría, Topología DMN y Drafts)

CREATE TABLE IF NOT EXISTS ibpms_dmn_definitions (
    id VARCHAR(100) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    decision_ref VARCHAR(255) NOT NULL,
    xml_content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    hit_policy VARCHAR(50),
    version INT NOT NULL DEFAULT 1,
    author_hash VARCHAR(255),
    tenant_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- OBSERVACIÓN OBLIGATORIA: La columna 'is_manual' introducida en el changeset 38 queda deprecada.
-- La fuente de verdad para capturar si es manual (MANUAL, NLP, NLP_MODIFIED, XML_UPLOAD) es 'source'.
ALTER TABLE ibpms_dmn_definitions 
ADD COLUMN IF NOT EXISTS source VARCHAR(20) DEFAULT 'NLP' NOT NULL;

CREATE TABLE IF NOT EXISTS ibpms_dmn_drafts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(255) NOT NULL,
    prompt_hash VARCHAR(255),
    xml_content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP NOT NULL DEFAULT NOW() + INTERVAL '24 hours'
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_dmn_def_unique ON ibpms_dmn_definitions(decision_ref, version, tenant_id);
CREATE INDEX IF NOT EXISTS idx_dmn_def_status ON ibpms_dmn_definitions(status);
CREATE INDEX IF NOT EXISTS idx_dmn_def_tenant ON ibpms_dmn_definitions(tenant_id);

CREATE TABLE IF NOT EXISTS ibpms_dmn_audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dmn_id VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    author_hash VARCHAR(255) NOT NULL,
    source_badge VARCHAR(50),
    tenant_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    details_json TEXT
);
