-- liquibase formatted sql
-- changeset ibpms:create_deploy_requests_table
-- validCheckSum: 9:4ffe81159d38a7e85ca5b35516bcc62f
-- @Traceability: US-005, CA-34

CREATE TABLE IF NOT EXISTS ibpms_deploy_requests (
    id UUID PRIMARY KEY,
    process_definition_key VARCHAR(255) NOT NULL,
    requested_by VARCHAR(255) NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    reviewed_by VARCHAR(255),
    reviewed_at TIMESTAMP,
    review_comment TEXT
);

ALTER TABLE ibpms_deploy_requests ADD COLUMN IF NOT EXISTS xml_payload TEXT NOT NULL;

CREATE INDEX IF NOT EXISTS idx_deploy_req_status ON ibpms_deploy_requests(status);
