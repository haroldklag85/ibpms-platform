-- liquibase formatted sql
-- changeset system:22-us005-bpmn-design-schema

-- CA-66: Process Locks
CREATE TABLE ibpms_process_locks (
    process_definition_key VARCHAR(255) PRIMARY KEY,
    locked_by VARCHAR(255) NOT NULL,
    locked_at TIMESTAMP NOT NULL,
    browser_session_id VARCHAR(255)
);

-- CA-69: Deploy Requests
CREATE TABLE ibpms_deploy_requests (
    id UUID PRIMARY KEY,
    process_definition_key VARCHAR(255) NOT NULL,
    requested_by VARCHAR(255) NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    reviewed_by VARCHAR(255),
    reviewed_at TIMESTAMP,
    review_comment TEXT
);

-- CA-70: External Task Topics
CREATE TABLE ibpms_external_task_topics (
    topic_name VARCHAR(255) PRIMARY KEY,
    description TEXT,
    worker_class VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    registered_at TIMESTAMP NOT NULL
);

-- Seed CA-70
INSERT INTO ibpms_external_task_topics (topic_name, description, worker_class, registered_at) VALUES 
('ibpms.send_email', 'Envío de correos electrónicos', 'com.ibpms.poc.workers.EmailWorker', CURRENT_TIMESTAMP),
('ibpms.sync_erp', 'Sincronización con ERP', 'com.ibpms.poc.workers.ErpSyncWorker', CURRENT_TIMESTAMP),
('ibpms.sync_sharepoint', 'Sharepoint Sync', 'com.ibpms.poc.workers.SharepointWorker', CURRENT_TIMESTAMP),
('ibpms.generate_pdf', 'Generación de PDFs', 'com.ibpms.poc.workers.PdfGeneratorWorker', CURRENT_TIMESTAMP),
('ibpms.ai_copilot', 'Delegación a Inteligencia Artificial', 'com.ibpms.poc.workers.AiCopilotWorker', CURRENT_TIMESTAMP),
('ibpms.webhook_outbound', 'Banda de salida webhook', 'com.ibpms.poc.workers.WebhookWorker', CURRENT_TIMESTAMP);

-- CA-68: Data Mappings
CREATE TABLE ibpms_data_mappings (
    id UUID PRIMARY KEY,
    process_definition_key VARCHAR(255) NOT NULL,
    task_id VARCHAR(255) NOT NULL,
    connector_id VARCHAR(255),
    mapping_json TEXT, -- Or JSONB based on dialect, we use TEXT for universal support if JSONB fails. Actually PG is jsonb but TEXT works with standard parsers.
    last_validated_at TIMESTAMP
);
