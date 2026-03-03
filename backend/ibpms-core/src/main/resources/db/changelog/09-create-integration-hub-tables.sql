--liquibase formatted sql
--changeset hb-dev:9-create-integration-hub-tables

CREATE TABLE ibpms_api_connector (
    id BINARY(16) NOT NULL,
    name VARCHAR(100) NOT NULL,
    system_code VARCHAR(100) NOT NULL,
    base_url VARCHAR(500) NOT NULL,
    http_method VARCHAR(20) NOT NULL,
    default_headers JSON,
    version VARCHAR(20) NOT NULL,
    is_cached BOOLEAN NOT NULL DEFAULT FALSE,
    cache_ttl_minutes INT,
    auth_config JSON,
    pgp_public_key TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_api_connector PRIMARY KEY (id),
    CONSTRAINT uq_api_connector_code_version UNIQUE (system_code, version)
);

CREATE INDEX idx_api_connector_system_code ON ibpms_api_connector(system_code);
