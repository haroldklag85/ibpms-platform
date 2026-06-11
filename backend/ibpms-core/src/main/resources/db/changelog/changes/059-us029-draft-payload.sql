-- liquibase formatted sql -- // @Traceability: US-005, CA-42 - Activity Timeline

-- changeset antigravity:059-us029-draft-payload
-- comment: Agrega columnas draft_payload, draft_payload_hash, team_id y timeout_extensions a ibpms_agile_tasks, y crea tabla ibpms_agile_timeboxes (US-029)

ALTER TABLE ibpms_agile_tasks ADD COLUMN IF NOT EXISTS draft_payload TEXT;
ALTER TABLE ibpms_agile_tasks ADD COLUMN IF NOT EXISTS draft_payload_hash VARCHAR(64);
ALTER TABLE ibpms_agile_tasks ADD COLUMN IF NOT EXISTS team_id VARCHAR(255);
ALTER TABLE ibpms_agile_tasks ADD COLUMN IF NOT EXISTS timeout_extensions INTEGER DEFAULT 0;

CREATE TABLE IF NOT EXISTS ibpms_agile_timeboxes (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id  UUID NOT NULL REFERENCES ibpms_agile_projects(id),
    name        VARCHAR(150) NOT NULL,
    goal        VARCHAR(500),
    start_date  DATE NOT NULL,
    end_date    DATE NOT NULL,
    status      VARCHAR(30) NOT NULL DEFAULT 'PLANNING',
    created_by  VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- changeset antigravity:059-us029-business-hours-holidays-task-log
-- comment: Crea tablas ibpms_business_hours, ibpms_holiday y ibpms_generic_task_log para corregir validación de JPA (US-005)

CREATE TABLE IF NOT EXISTS ibpms_business_hours (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    start_time        TIME NOT NULL DEFAULT '08:00:00',
    end_time          TIME NOT NULL DEFAULT '17:00:00',
    work_on_weekends  BOOLEAN NOT NULL DEFAULT FALSE,
    timezone          VARCHAR(50) DEFAULT 'America/Bogota'
);

CREATE TABLE IF NOT EXISTS ibpms_holiday (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    holiday_date  DATE NOT NULL UNIQUE,
    description   VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS ibpms_generic_task_log (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id              VARCHAR(64) NOT NULL,
    process_instance_id  VARCHAR(64) NOT NULL,
    user_id              VARCHAR(100) NOT NULL,
    comments             TEXT,
    has_evidence         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- changeset antigravity:059-us029-forensic-iso-overrides
-- comment: Crea tabla ibpms_forensic_iso_overrides y sus índices para corregir validación de JPA (US-005)

CREATE TABLE IF NOT EXISTS ibpms_forensic_iso_overrides (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             VARCHAR(150) NOT NULL,
    session_id          VARCHAR(150) NOT NULL,
    ignored_warning_code VARCHAR(100) NOT NULL,
    forced_xml          TEXT NOT NULL,
    metrics_json        TEXT,
    override_timestamp  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_forensic_user ON ibpms_forensic_iso_overrides (user_id);
CREATE INDEX IF NOT EXISTS idx_forensic_session ON ibpms_forensic_iso_overrides (session_id);

-- changeset antigravity:059-us029-missing-tables
-- comment: Crea todas las tablas restantes de JPA faltantes en la base de datos de prueba (US-005)

CREATE TABLE IF NOT EXISTS kanban_boards (
    id                             UUID PRIMARY KEY,
    project_name                   VARCHAR(150) NOT NULL,
    description                    TEXT,
    associated_process_instance_id VARCHAR(64),
    owner_id                       VARCHAR(100) NOT NULL,
    created_at                     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                     TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ibpms_form_design_audit (
    id             BIGSERIAL PRIMARY KEY,
    form_id        UUID NOT NULL,
    action         VARCHAR(20) NOT NULL,
    user_id        VARCHAR(50) NOT NULL,
    dummy_payload  TEXT,
    hash_value     VARCHAR(64),
    timestamp      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS idp_group_mapping (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idp_group_id  VARCHAR(255) NOT NULL UNIQUE,
    profile_id    UUID REFERENCES ibpms_profile(id)
);

CREATE TABLE IF NOT EXISTS inbound_webhooks (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                    VARCHAR(100) NOT NULL,
    source_type             VARCHAR(50) NOT NULL,
    target_bpmn_process_key VARCHAR(100) NOT NULL,
    is_active               BOOLEAN NOT NULL,
    security_token          VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS outbound_configs (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(100) NOT NULL,
    endpoint_url  VARCHAR(500) NOT NULL,
    http_method   VARCHAR(10) NOT NULL,
    auth_type     VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS ibpms_kanban_board_v2 (
    id           VARCHAR(50) PRIMARY KEY,
    title        VARCHAR(100) NOT NULL,
    wip_limit    INTEGER,
    order_index  INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS ibpms_security_process_permission (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    process_definition_key  VARCHAR(150) NOT NULL,
    can_initiate_process    BOOLEAN NOT NULL DEFAULT FALSE,
    can_execute_tasks       BOOLEAN NOT NULL DEFAULT FALSE,
    role_id                 UUID NOT NULL REFERENCES ibpms_security_role(id)
);

CREATE TABLE IF NOT EXISTS sac_mailboxes (
    id                       VARCHAR(255) PRIMARY KEY,
    alias                    VARCHAR(255) NOT NULL UNIQUE,
    protocol                 VARCHAR(255) NOT NULL,
    tenant_id                VARCHAR(255) NOT NULL,
    client_id                VARCHAR(255) NOT NULL,
    key_vault_reference_id   VARCHAR(255) NOT NULL,
    default_bpmn_process_id  VARCHAR(255) NOT NULL,
    active                   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at               TIMESTAMP,
    updated_at               TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ibpms_security_permission (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name         VARCHAR(100) NOT NULL UNIQUE,
    description  VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS ibpms_security_role_permissions (
    role_id UUID NOT NULL REFERENCES ibpms_security_role(id),
    permission_id UUID NOT NULL REFERENCES ibpms_security_permission(id),
    PRIMARY KEY (role_id, permission_id)
);


CREATE TABLE IF NOT EXISTS ibpms_ui_template (
    id          VARCHAR(255) PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    type        VARCHAR(50) NOT NULL,
    raw_code    TEXT NOT NULL,
    version     VARCHAR(50) NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ibpms_security_service_account (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(100) NOT NULL UNIQUE,
    description   VARCHAR(255),
    api_key_hash  VARCHAR(255) NOT NULL,
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    expires_at    TIMESTAMP,
    role_id       UUID NOT NULL REFERENCES ibpms_security_role(id)
);

CREATE TABLE IF NOT EXISTS sys_catalog_cache (
    id            VARCHAR(100) PRIMARY KEY,
    payload       TEXT NOT NULL,
    last_sync_at  TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS user_delegation (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    supervisor_id  VARCHAR(100) NOT NULL,
    assistant_id   VARCHAR(100) NOT NULL,
    tenant_id      VARCHAR(100) NOT NULL,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS task_skip_audit (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id     VARCHAR(255) NOT NULL,
    skipped_by  VARCHAR(100) NOT NULL,
    tenant_id   VARCHAR(100) NOT NULL,
    reason      VARCHAR(50) NOT NULL,
    details     TEXT,
    skipped_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- changeset antigravity:059-us029-missing-columns
-- comment: Agrega columnas faltantes a ibpms_security_audit_log, ibpms_triage_tasks, ibpms_task y ibpms_security_user para corregir validación de JPA (US-005)

ALTER TABLE ibpms_security_audit_log ADD COLUMN IF NOT EXISTS username VARCHAR(100);
ALTER TABLE ibpms_security_audit_log ADD COLUMN IF NOT EXISTS timestamp TIMESTAMP;
ALTER TABLE ibpms_security_audit_log ADD COLUMN IF NOT EXISTS ip_address VARCHAR(50);
ALTER TABLE ibpms_security_audit_log ADD COLUMN IF NOT EXISTS details VARCHAR(500);

ALTER TABLE ibpms_triage_tasks ADD COLUMN IF NOT EXISTS scan_status VARCHAR(50);
ALTER TABLE ibpms_triage_tasks ADD COLUMN IF NOT EXISTS file_sha256_hash VARCHAR(64);

ALTER TABLE ibpms_task ADD COLUMN IF NOT EXISTS title VARCHAR(255);
ALTER TABLE ibpms_task ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE ibpms_task ADD COLUMN IF NOT EXISTS board_id UUID REFERENCES kanban_boards(id);
ALTER TABLE ibpms_task ADD COLUMN IF NOT EXISTS parent_task_id CHAR(36) REFERENCES ibpms_task(id);
ALTER TABLE ibpms_task ADD COLUMN IF NOT EXISTS sla_due_date TIMESTAMP;
ALTER TABLE ibpms_task ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE ibpms_task ALTER COLUMN priority TYPE VARCHAR(20);

ALTER TABLE ibpms_security_user ADD COLUMN IF NOT EXISTS must_change_password BOOLEAN DEFAULT FALSE;

ALTER TABLE ibpms_workdesk_projection ALTER COLUMN progress_percent TYPE INTEGER;
ALTER TABLE ibpms_workdesk_projection ALTER COLUMN total_steps TYPE INTEGER;
ALTER TABLE ibpms_workdesk_projection ALTER COLUMN current_step TYPE INTEGER;

-- changeset antigravity:059-us029-kanban-nullable-case-v2
-- comment: Permite que las tareas Kanban existan sin estar asociadas a un caso (US-008)

ALTER TABLE ibpms_task ALTER COLUMN case_id DROP NOT NULL;
ALTER TABLE ibpms_task ALTER COLUMN name DROP NOT NULL;
ALTER TABLE ibpms_task ALTER COLUMN source_type DROP NOT NULL;
ALTER TABLE ibpms_task ALTER COLUMN ref_id DROP NOT NULL;
