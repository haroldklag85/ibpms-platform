-- liquibase formatted sql
-- changeset antigravity:38-us007-dmn-manual-edit-schema

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

ALTER TABLE ibpms_dmn_definitions
ADD COLUMN IF NOT EXISTS is_manual BOOLEAN DEFAULT FALSE;
