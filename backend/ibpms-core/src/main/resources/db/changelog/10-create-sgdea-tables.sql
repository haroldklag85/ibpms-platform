--liquibase formatted sql
--changeset hb-dev:10-create-sgdea-tables

CREATE TABLE ibpms_document_reference (
    id UUID NOT NULL,
    sharepoint_graph_id VARCHAR(150) NOT NULL,
    sharepoint_url VARCHAR(500),
    file_name VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    sha256_hash VARCHAR(64) NOT NULL,
    trd_expiration_date DATE,
    version INT NOT NULL DEFAULT 1,
    process_instance_id VARCHAR(64),
    uploaded_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_document_reference PRIMARY KEY (id)
);
CREATE INDEX idx_doc_ref_process ON ibpms_document_reference(process_instance_id);
CREATE UNIQUE INDEX uq_doc_ref_graph_id ON ibpms_document_reference(sharepoint_graph_id);
