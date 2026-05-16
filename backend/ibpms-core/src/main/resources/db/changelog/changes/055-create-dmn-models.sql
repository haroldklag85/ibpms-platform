-- liquibase formatted sql
-- changeset system:055-create-dmn-models

CREATE TABLE ibpms_dmn_models (
    id VARCHAR(100) PRIMARY KEY,
    name VARCHAR(200),
    xml_content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL,
    author_jwt_hash VARCHAR(255),
    tenant_id VARCHAR(100),
    chat_history_json TEXT,
    is_manual BOOLEAN
);

CREATE INDEX idx_dmn_model_tenant ON ibpms_dmn_models(tenant_id);
