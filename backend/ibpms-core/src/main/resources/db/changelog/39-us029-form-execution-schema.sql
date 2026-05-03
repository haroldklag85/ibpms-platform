-- liquibase formatted sql

-- changeset antigravity:39-us029-form-execution-schema
-- comment: Tablas y columnas para validación de ejecución de formularios (US-029 Bloque 1)

CREATE TABLE ibpms_temp_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    original_filename VARCHAR(500) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    mime_detected VARCHAR(100),
    file_size_bytes BIGINT NOT NULL,
    storage_path VARCHAR(1000) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    uploaded_at TIMESTAMP DEFAULT NOW(),
    confirmed_at TIMESTAMP
);

CREATE INDEX idx_temp_docs_task_user ON ibpms_temp_documents(task_id, user_id);
CREATE INDEX idx_temp_docs_status ON ibpms_temp_documents(status, uploaded_at);

ALTER TABLE form_event_store ADD COLUMN IF NOT EXISTS visible_fields JSONB;

-- INFRA-029-04: Cron Job de Limpieza Documentado
-- La capa de Backend debe implementar un proceso @Scheduled que ejecute periódicamente:
-- DELETE FROM ibpms_temp_documents WHERE status = 'PENDING' AND uploaded_at < NOW() - INTERVAL '24 hours';
