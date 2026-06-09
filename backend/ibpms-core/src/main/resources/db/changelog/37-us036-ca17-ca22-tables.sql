-- liquibase formatted sql
-- changeset david:37-us036-ca17-ca22-tables

-- CA-17: Tabla para Auditoría de Roles Forense
CREATE TABLE IF NOT EXISTS ibpms_security_role_audit_log (
    id UUID PRIMARY KEY,
    role_id UUID NOT NULL,
    admin_id VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    action VARCHAR(20) NOT NULL,
    delta_json TEXT NOT NULL
);

-- CA-11: Tabla para Tablero CISO de Anomalías
CREATE TABLE IF NOT EXISTS ibpms_security_anomalies (
    id UUID PRIMARY KEY,
    type VARCHAR(100) NOT NULL,
    suspect_user_id VARCHAR(100),
    resource_id VARCHAR(100),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    resolved_by VARCHAR(100),
    resolved_at TIMESTAMP
);
