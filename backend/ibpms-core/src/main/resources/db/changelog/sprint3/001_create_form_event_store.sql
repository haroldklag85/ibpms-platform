-- Liquibase Changelog: sprint3/001_create_form_event_store.sql
-- changeset architect:sprint3-001-form-event-store

CREATE TABLE form_event_store (
    event_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type        VARCHAR(50)  NOT NULL,  -- FORM_SUBMITTED | TASK_AUTO_CLAIMED | FORM_REJECTED | FORM_SUBMIT_ROLLED_BACK
    task_id           VARCHAR(255) NOT NULL,
    process_instance_id VARCHAR(255) NOT NULL,
    user_id           VARCHAR(255) NOT NULL,
    payload_json      JSONB        NOT NULL,  -- Contenido cifrado PII (CA-12)
    schema_version    VARCHAR(10)  NOT NULL,  -- Ej: "V3"
    idempotency_key   UUID         UNIQUE,    -- Desde Frontend (US-029 CA-12)
    original_event_id UUID,                   -- Solo para FORM_SUBMIT_ROLLED_BACK (CA-10)
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_fes_task_id ON form_event_store(task_id);
CREATE INDEX idx_fes_process ON form_event_store(process_instance_id);
CREATE INDEX idx_fes_created ON form_event_store(created_at);

-- POLÍTICA DE INMUTABILIDAD: Prohibir UPDATE y DELETE vía trigger
CREATE OR REPLACE FUNCTION prevent_event_mutation() RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'FORBIDDEN: Event Store is append-only. UPDATE/DELETE prohibited.';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_prevent_event_update BEFORE UPDATE ON form_event_store
    FOR EACH ROW EXECUTE FUNCTION prevent_event_mutation();
CREATE TRIGGER trg_prevent_event_delete BEFORE DELETE ON form_event_store
    FOR EACH ROW EXECUTE FUNCTION prevent_event_mutation();

COMMENT ON TABLE form_event_store IS 'Bóveda inmutable de eventos CQRS (US-017 CA-01, CA-06). Append-only.';
