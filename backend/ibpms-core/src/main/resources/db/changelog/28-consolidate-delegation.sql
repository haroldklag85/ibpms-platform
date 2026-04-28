-- liquibase formatted sql

-- changeset antigravity:28-consolidate-user-schema
CREATE TABLE IF NOT EXISTS ibpms_security_user (
    id UUID NOT NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_external_idp BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    skills JSON,
    CONSTRAINT pk_ibpms_security_user PRIMARY KEY (id)
);

-- changeset antigravity:28-consolidate-delegation-schema
CREATE TABLE IF NOT EXISTS ibpms_security_delegation (
    id UUID PRIMARY KEY,
    delegator_id VARCHAR(50) NOT NULL,
    substitute_id VARCHAR(50) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT true,
    reason VARCHAR(255)
);

-- changeset antigravity:28-consolidate-delegation splitStatements:false
-- comment: HO-02 — Consolida 3 esquemas de delegación en 1 (ibpms_security_delegation)

-- Migrar datos legacy de user_delegation → ibpms_security_delegation (si existe)

DO $$
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'user_delegation') THEN
        INSERT INTO ibpms_security_delegation (id, delegator_id, substitute_id, start_date, end_date, is_active, reason)
        SELECT gen_random_uuid(), d.id, a.id, 
               COALESCE(ud.start_date, CURRENT_TIMESTAMP), 
               COALESCE(ud.end_date, CURRENT_TIMESTAMP + INTERVAL '365 days'), 
               COALESCE(ud.is_active, true), 
               'Migrado desde user_delegation (HO-02)'
        FROM user_delegation ud
        JOIN ibpms_security_user d ON d.username = ud.supervisor_id
        JOIN ibpms_security_user a ON a.username = ud.assistant_id
        ON CONFLICT DO NOTHING;

        -- Marcar tabla legacy como deprecated (no DROP para no romper scripts existentes)
        COMMENT ON TABLE user_delegation IS '[DEPRECATED Sprint 6.2] Usar ibpms_security_delegation (DelegationEntity.java)';
    END IF;
END $$;

-- Documentar tabla canónica
COMMENT ON TABLE ibpms_security_delegation IS 'Tabla canónica de delegación — Fuente única de verdad desde Sprint 6.2 (HO-02)';
