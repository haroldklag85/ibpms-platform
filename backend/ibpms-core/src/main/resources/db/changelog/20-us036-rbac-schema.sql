-- liquibase formatted sql

-- changeset antigravity:20-us036-rbac-schema

-- Table: ibpms_roles
CREATE TABLE ibpms_roles (
    id UUID NOT NULL,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    parent_role_id UUID,
    is_template BOOLEAN NOT NULL DEFAULT FALSE,
    source VARCHAR(50) NOT NULL, -- ENTRA_ID | LOCAL
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_ibpms_roles PRIMARY KEY (id),
    CONSTRAINT fk_role_parent FOREIGN KEY (parent_role_id) REFERENCES ibpms_roles(id)
);

-- Table: ibpms_permissions
CREATE TABLE ibpms_permissions (
    id UUID NOT NULL,
    resource VARCHAR(100) NOT NULL, -- PROCESS, FORM, ADMIN_PANEL
    action VARCHAR(50) NOT NULL, -- INITIATE, EXECUTE, READ, WRITE, DELETE
    process_definition_id VARCHAR(100), -- nullable
    CONSTRAINT pk_ibpms_permissions PRIMARY KEY (id)
);

-- Table: ibpms_role_permissions (pivote)
CREATE TABLE ibpms_role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    CONSTRAINT pk_ibpms_role_permissions PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES ibpms_roles(id),
    CONSTRAINT fk_rp_perm FOREIGN KEY (permission_id) REFERENCES ibpms_permissions(id)
);

-- Table: ibpms_user_roles (pivote)
CREATE TABLE ibpms_user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    assigned_by VARCHAR(100),
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_ibpms_user_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES ibpms_roles(id)
);

-- Table: ibpms_service_accounts
CREATE TABLE ibpms_service_accounts (
    id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    api_key_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    role_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_ibpms_service_accounts PRIMARY KEY (id),
    CONSTRAINT fk_sa_role FOREIGN KEY (role_id) REFERENCES ibpms_roles(id)
);

-- Table: ibpms_audit_reports
CREATE TABLE ibpms_audit_reports (
    id UUID NOT NULL,
    report_type VARCHAR(100) NOT NULL,
    requested_by VARCHAR(100) NOT NULL,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    content_hash VARCHAR(255) NOT NULL,
    CONSTRAINT pk_ibpms_audit_reports PRIMARY KEY (id)
);

-- Ensure ibpms_audit_log exists for general logs as per CA-22 and CA-8 (from US-034/036)
CREATE TABLE IF NOT EXISTS ibpms_audit_log (
    id UUID NOT NULL,
    user_id VARCHAR(100),
    action VARCHAR(100) NOT NULL,
    message_count INT,
    service_account_id UUID,
    endpoint_invocado VARCHAR(255),
    ip_origen VARCHAR(100),
    timestamp_utc TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_ibpms_audit_log PRIMARY KEY (id)
);
