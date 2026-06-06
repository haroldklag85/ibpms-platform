-- liquibase formatted sql
-- changeset infra-agent:47-us034-idempotency-schema

-- 1. Eliminar PK actual
ALTER TABLE ibpms_processed_messages DROP CONSTRAINT ibpms_processed_messages_pkey;

-- 2. Agregar columna 'id' (UUID) como Primary Key
ALTER TABLE ibpms_processed_messages ADD COLUMN id UUID DEFAULT gen_random_uuid() NOT NULL;
ALTER TABLE ibpms_processed_messages ADD PRIMARY KEY (id);

-- 3. Alterar 'idempotency_key' a UUID (si es posible castear) y hacerla UNIQUE
ALTER TABLE ibpms_processed_messages ALTER COLUMN idempotency_key TYPE UUID USING idempotency_key::uuid;
ALTER TABLE ibpms_processed_messages ADD CONSTRAINT uq_processed_messages_idempotency_key UNIQUE (idempotency_key);
