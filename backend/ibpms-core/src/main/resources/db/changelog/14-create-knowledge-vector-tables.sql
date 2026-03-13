-- liquibase formatted sql

-- changeset antigravity:14-create-knowledge-vector-tables context:uat,prod
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE ai_knowledge_vectors (
    id UUID NOT NULL,
    context_email_body TEXT,
    human_approved_reply TEXT,
    embedding vector(1536),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_ai_knowledge_vectors PRIMARY KEY (id)
);
