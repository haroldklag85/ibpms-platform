-- liquibase formatted sql

-- changeset antigravity:12-identity-rbac-tables

-- Table: ibpms_sec_role_template
CREATE TABLE ibpms_sec_role_template (
    id BINARY(16) NOT NULL,
    role_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    granular_permissions JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_ibpms_sec_role_template PRIMARY KEY (id)
);

-- Table: ibpms_sec_role_hierarchy
CREATE TABLE ibpms_sec_role_hierarchy (
    id BINARY(16) NOT NULL,
    parent_role_id BINARY(16) NOT NULL,
    child_role_id BINARY(16) NOT NULL,
    CONSTRAINT pk_ibpms_sec_role_hierarchy PRIMARY KEY (id),
    CONSTRAINT fk_role_hier_parent FOREIGN KEY (parent_role_id) REFERENCES ibpms_sec_role_template(id),
    CONSTRAINT fk_role_hier_child FOREIGN KEY (child_role_id) REFERENCES ibpms_sec_role_template(id)
);

-- Table: ibpms_sec_identity
CREATE TABLE ibpms_sec_identity (
    id BINARY(16) NOT NULL,
    entraid_object_id VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    role_id BINARY(16),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_ibpms_sec_identity PRIMARY KEY (id),
    CONSTRAINT fk_identity_role FOREIGN KEY (role_id) REFERENCES ibpms_sec_role_template(id)
);

-- Table: ibpms_sec_delegation_log
CREATE TABLE ibpms_sec_delegation_log (
    id BINARY(16) NOT NULL,
    donor_id BINARY(16) NOT NULL,
    recipient_id BINARY(16) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    reason VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_ibpms_sec_delegation_log PRIMARY KEY (id),
    CONSTRAINT fk_delegation_donor FOREIGN KEY (donor_id) REFERENCES ibpms_sec_identity(id),
    CONSTRAINT fk_delegation_recipient FOREIGN KEY (recipient_id) REFERENCES ibpms_sec_identity(id)
);

-- Table: ibpms_sec_api_key
CREATE TABLE ibpms_sec_api_key (
    id BINARY(16) NOT NULL,
    client_name VARCHAR(100) NOT NULL UNIQUE,
    client_secret_hash VARCHAR(255) NOT NULL,
    role_id BINARY(16),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_ibpms_sec_api_key PRIMARY KEY (id),
    CONSTRAINT fk_apikey_role FOREIGN KEY (role_id) REFERENCES ibpms_sec_role_template(id)
);
