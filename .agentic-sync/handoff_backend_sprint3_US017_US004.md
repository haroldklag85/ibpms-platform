# Handoff Arquitectónico — ⚙️ Agente Backend
# Sprint 3: Iteraciones 1–4 (US-017 + US-004)

> **Emitido por:** `[🧠 ARQUITECTO LÍDER]` | **Fecha:** 2026-04-17
> **Sprint / Rama:** `sprint-3/feature/us017-cqrs-event-store` y `sprint-3/feature/us004-webhook-intake`
> **Protocolo Aplicado:** `.agents/skills/architect_handoff_protocol/SKILL.md`

---

## 1. Metadatos y SSOT

| Campo | Valor |
|-------|-------|
| **Sprint** | 3 — Retorno al Feature Factory |
| **User Stories** | US-017 (18 CAs), US-004 (17 CAs) |
| **SSOT** | `docs/requirements/epics/epic_A_motor_core.md` (líneas 1009–1234 para US-017, líneas 593–736 para US-004) |
| **Índice** | `docs/requirements/v1_user_stories_index.md` |
| **NFRs** | `docs/requirements/non_functional_requirements.md` |
| **Flujo** | Backend (Iteraciones 1→4) → Frontend (Iteraciones 5→7) → QA (Regresión + Nuevos E2E) |

---

## 2. Alineación Arquitectónica y ADRs

### ADRs Regentes

| ADR | Impacto |
|-----|---------|
| `docs/architecture/adr-001-hexagonal-architecture.md` | Toda nueva entidad DEBE seguir Ports/Adapters. El Event Store y el Webhook son Adapters de Persistencia e Ingesta. |
| `docs/architecture/adr_011_local_cqrs_v1.md` | CQRS local in-process. El Worker de proyección vive en el mismo JVM (no microservicio separado). |
| `docs/architecture/adr-003-camunda7-embedded.md` | Camunda embebido. Prohibido empujar payloads pesados a `ACT_RU_VARIABLE`. Solo DTOs minificados. |
| `docs/architecture/adr_010_testing_pyramid_governance.md` | Toda iteración DEBE tener Testcontainers antes del merge. |

### Lineamientos Transversales
- **Zero-Trust:** Todo endpoint nuevo valida JWT internamente. No se confía en el API Gateway.
- **Inmutabilidad:** La tabla `form_event_store` PROHIBE `UPDATE` y `DELETE`. Los rollbacks generan eventos compensatorios (`FORM_SUBMIT_ROLLED_BACK`).
- **Cifrado PII:** Se usará `IBPMS_PII_ENCRYPTION_KEY` (variable de entorno, AES-256) en desarrollo local, abstrayendo tras `EncryptionKeyProvider` interface para intercambio con Azure Key Vault en producción.

---

## 3. Rutas Exactas y Contexto Preexistente

### Estructura Hexagonal Actual
```
backend/ibpms-core/src/main/java/com/ibpms/poc/
├── api/controller/          ← REST Controllers (adapters in)
├── application/             ← Use Cases / Application Services
├── domain/
│   ├── model/               ← Entidades de dominio
│   ├── port/                ← Interfaces (ports)
│   └── exception/           ← Excepciones de dominio
└── infrastructure/          ← Adapters out (repos, MQ, WS)
```

### Archivos a CREAR (Nuevos)

#### Iteración 1: Event Store & Drafts
| Capa | Archivo | Propósito |
|------|---------|-----------|
| DB | `backend/ibpms-core/src/main/resources/db/changelog/sprint3/001_create_form_event_store.sql` | DDL de la tabla de eventos inmutables |
| DB | `backend/ibpms-core/src/main/resources/db/changelog/sprint3/002_create_task_drafts.sql` | DDL de la tabla de borradores efímeros |
| Domain | `domain/model/FormEvent.java` | Entidad JPA del Event Store |
| Domain | `domain/model/TaskDraft.java` | Entidad JPA de borradores |
| Domain | `domain/model/EventType.java` | Enum: `FORM_SUBMITTED`, `TASK_AUTO_CLAIMED`, `FORM_REJECTED`, `FORM_SUBMIT_ROLLED_BACK` |
| Port | `domain/port/FormEventRepository.java` | Puerto de persistencia de eventos |
| Port | `domain/port/TaskDraftRepository.java` | Puerto de persistencia de borradores |
| Infra | `infrastructure/persistence/FormEventRepositoryJpa.java` | Adapter JPA para el Event Store |
| Infra | `infrastructure/persistence/TaskDraftRepositoryJpa.java` | Adapter JPA para borradores |
| API | `api/controller/TaskDraftController.java` | REST: GET/PUT/DELETE `/api/v1/workbox/tasks/{taskId}/draft` |
| Test | `src/test/java/.../FormEventRepositoryTest.java` | Testcontainers: inmutabilidad y append-only |
| Test | `src/test/java/.../TaskDraftRepositoryTest.java` | Testcontainers: TTL 72h y purga |

#### Iteración 2: CQRS Transaccional
| Capa | Archivo | Propósito |
|------|---------|-----------|
| Application | `application/service/FormCompletionService.java` | Orquestador: validación → Event Store → Camunda → cleanup draft |
| Application | `application/service/AutoClaimService.java` | Lógica de Auto-Claim para tareas de grupo (CA-04) |
| Application | `application/service/CamundaCompletionAdapter.java` | Adapter: DTO minificado a Camunda (CA-02), retry 3x (CA-10) |
| Infra | `infrastructure/config/RateLimitingConfig.java` | Configuración de rate-limiting para `/draft` endpoints (6 req/min) |
| Test | `src/test/java/.../FormCompletionServiceTest.java` | Testcontainers: Saga, Auto-Claim, Race Condition |

#### Iteración 3: Trazabilidad & Cifrado
| Capa | Archivo | Propósito |
|------|---------|-----------|
| Application | `application/service/RejectionLogService.java` | Consulta de `rejectionLogs` para BFF `/form-context` |
| Infra | `infrastructure/security/PiiEncryptionService.java` | AES-256, consume `EncryptionKeyProvider` |
| Domain | `domain/port/EncryptionKeyProvider.java` | Interface abstracta del proveedor de llaves |
| Infra | `infrastructure/security/EnvEncryptionKeyProvider.java` | Implementación local: lee `IBPMS_PII_ENCRYPTION_KEY` |
| Application | `application/service/EventReferenceGenerator.java` | Genera `EVT-A3F8K9` (12 chars legibles del UUID) |
| Test | `src/test/java/.../PiiEncryptionServiceTest.java` | Unit test: cifrado/descifrado PII fields |
| Test | `src/test/java/.../RejectionLogServiceTest.java` | Testcontainers: inyección de `rejectionLogs` en form-context |

#### Iteración 4: Webhook Intake Core (US-004)
| Capa | Archivo | Propósito |
|------|---------|-----------|
| DB | `db/changelog/sprint3/003_create_webhook_intake_tables.sql` | DDL: `ibpms_webhook_transactions`, `ibpms_orphan_payloads`, `ibpms_webhook_allowed_domains` |
| Domain | `domain/model/WebhookTransaction.java` | Entidad: transacción de entrada idempotente |
| Domain | `domain/model/OrphanPayload.java` | Entidad: payloads rechazados/malformados |
| Domain | `domain/model/AllowedDomain.java` | Entidad: dominios en whitelist |
| API | `api/controller/WebhookIntakeController.java` | `POST /api/v1/webhook/intake` — endpoint público |
| API | `api/controller/AllowedDomainController.java` | CRUD `/api/v1/admin/webhook/allowed-domains` |
| Application | `application/service/WebhookIntakeService.java` | Orquestador: idempotencia → autoresponder → whitelist → HMAC → ClamAV → RabbitMQ |
| Infra | `infrastructure/antimalware/ClamAvScannerAdapter.java` | Adapter: REST call al sidecar ClamAV |
| Infra | `infrastructure/config/ClamAvProperties.java` | Config properties: `ibpms.clamav.url`, `ibpms.clamav.timeout` |
| Test | `src/test/java/.../WebhookIntakeControllerTest.java` | Testcontainers: idempotencia, HMAC, whitelist, ClamAV mock |

---

## 4. Snippets Prescriptivos

### DDL — Event Store (Iteración 1)
```sql
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
```

### DDL — Task Drafts (Iteración 1)
```sql
-- Liquibase Changelog: sprint3/002_create_task_drafts.sql
-- changeset architect:sprint3-002-task-drafts

CREATE TABLE task_drafts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id         VARCHAR(255) NOT NULL,
    user_id         VARCHAR(255) NOT NULL,
    current_step    INTEGER,
    partial_data    JSONB        NOT NULL,
    schema_version  VARCHAR(10)  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_task_draft_per_user UNIQUE (task_id, user_id)
);

COMMENT ON TABLE task_drafts IS 'Snapshots efímeros de borradores (US-017 CA-07). TTL 72h. Sobrescribibles.';
```

### DDL — Webhook Intake Tables (Iteración 4)
```sql
-- Liquibase Changelog: sprint3/003_create_webhook_intake_tables.sql
-- changeset architect:sprint3-003-webhook-intake

CREATE TABLE ibpms_webhook_transactions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id      VARCHAR(500) NOT NULL UNIQUE, -- Idempotencia CA-1
    sender_email    VARCHAR(500) NOT NULL,
    sender_domain   VARCHAR(255) NOT NULL,
    payload_hash    VARCHAR(128),
    status          VARCHAR(30)  NOT NULL DEFAULT 'RECEIVED',  -- RECEIVED|PROCESSED|REJECTED|QUARANTINED
    rejection_reason VARCHAR(100),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE ibpms_orphan_payloads (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    raw_payload     JSONB,
    error_type      VARCHAR(50)  NOT NULL,  -- MALFORMED|UNAUTHORIZED|MALWARE_QUARANTINE
    file_hash_sha256 VARCHAR(64),
    file_name       VARCHAR(255),
    file_size_bytes BIGINT,
    sender_email    VARCHAR(500),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE ibpms_webhook_allowed_domains (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    domain          VARCHAR(255) NOT NULL,
    tenant_id       VARCHAR(255) NOT NULL,
    description     VARCHAR(500),
    created_by      VARCHAR(255) NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_domain_per_tenant UNIQUE (domain, tenant_id)
);

CREATE INDEX idx_wad_domain ON ibpms_webhook_allowed_domains(domain) WHERE is_active = TRUE;
```

### Firma — FormCompletionService (Iteración 2)
```java
@Service
@Transactional
public class FormCompletionService {

    // CA-01: Persistir evento inmutable
    // CA-02: DTO minificado a Camunda (SOLO variables de gateway)
    // CA-03 + CA-10: Rollback Saga con 3 retries (1s, 2s, 4s) + timeout 10s
    // CA-04 + CA-13: Auto-Claim con validación de grupo
    // CA-15: Generar eventReference (EVT-XXXXXX)
    // CA-16: Eliminar draft dentro de la misma transacción
    // CA-17: SLA ≤5s normal, ≤17s worst case
    public FormSubmitResponse completeTask(String taskId, FormSubmitRequest request, String userId) {
        // 1. Validar Implicit Locking o Auto-Claim (CA-04, CA-13)
        // 2. Verificar idempotency_key (CA-06, NFR-AVA-02)
        // 3. Cifrar campos PII en payload (CA-12)
        // 4. INSERT evento FORM_SUBMITTED en form_event_store
        // 5. Preparar DTO minificado para Camunda (CA-02)
        // 6. Intentar taskService.complete() con retry 3x (CA-10)
        //    - Si falla: INSERT evento FORM_SUBMIT_ROLLED_BACK + HTTP 500
        // 7. DELETE draft de task_drafts (CA-16)
        // 8. Retornar eventReference (CA-15)
        throw new UnsupportedOperationException("Implement following CA-01 through CA-17");
    }
}
```

### Firma — WebhookIntakeService (Iteración 4)
```java
@Service
public class WebhookIntakeService {

    // Flujo secuencial de validación perimetral (US-004)
    public WebhookResponse processIncomingWebhook(WebhookPayload payload) {
        // 1. ACK inmediato — responder sub-segundo (CA-17)
        //    (procesar asincrónicamente tras el ACK)
        // 2. Verificar idempotencia por message_id (CA-1)
        // 3. Bloquear auto-responders: no-reply@, mailer-daemon@ (CA-2)
        // 4. Validar payload JSON (CA-3) — si malformado → orphan_payloads
        // 5. Verificar whitelist de dominio (CA-4) — caché Redis TTL 5min (CA-12)
        // 6. Verificar firma HMAC/Bearer (CA-10)
        // 7. Validar peso de adjuntos ≤ límite parametrizable (CA-7)
        // 8. Escanear adjuntos con ClamAV (CA-11) — fail-secure → DLQ
        // 9. Si Camunda offline → encolar en RabbitMQ (CA-6)
        // 10. Crear tarea de Pre-Triaje (CA-8/CA-9) — NO instanciar proceso definitivo
        throw new UnsupportedOperationException("Implement following CA-1 through CA-17");
    }
}
```

### Docker Compose — ClamAV Sidecar
```yaml
# Agregar al docker-compose.yml existente
ibpms-clamav:
  image: clamav/clamav:1.3
  container_name: ibpms-clamav
  ports:
    - "3310:3310"
  environment:
    - CLAMAV_NO_FRESHCLAMD=true  # No actualizar firmas en dev
  healthcheck:
    test: ["CMD", "clamdcheck"]
    interval: 30s
    timeout: 10s
    retries: 3
  networks:
    - ibpms-network
```

---

## 5. Matriz de QA y Testing Atómico (Backend)

| Test Name | US | CA Evaluados | Aserción Esperada |
|-----------|:--:|:------------:|-------------------|
| `FormEventStoreImmutabilityTest` | 017 | CA-06, CA-09 | INSERT OK; UPDATE/DELETE lanzan exception; solo 3 tipos de evento admitidos |
| `TaskDraftCrudTest` | 017 | CA-07 | GET retorna 404 si no existe; PUT crea/actualiza; DELETE elimina; TTL 72h |
| `FormCompletionSagaTest` | 017 | CA-01,02,03,10 | Happy path: evento + Camunda OK → 200. Camunda fail: 3 retries → ROLLED_BACK → 500 |
| `AutoClaimGroupTaskTest` | 017 | CA-04,13 | Sin assignee + grupo válido → claim + submit OK. Grupo inválido → 403 |
| `RateLimitDraftTest` | 017 | CA-14 | 7ª petición en 1 minuto → HTTP 429, header Retry-After: 10 |
| `PiiEncryptionRoundTripTest` | 017 | CA-12 | Encrypt → Decrypt con misma key = original. PII no visible en JSONB raw |
| `EventReferenceFormatTest` | 017 | CA-15 | Genera string alfanumérico ≤12 chars, prefijo `EVT-` |
| `DraftCleanupOnSubmitTest` | 017 | CA-16 | Tras /complete exitoso, GET /draft → 404 |
| `IdempotencyWebhookTest` | 004 | CA-1 | Mismo message_id 3x → solo 1 transacción, duplicados retornan 200 silencioso |
| `AutoResponderBlockTest` | 004 | CA-2 | no-reply@test.com → HTTP 400; mailer-daemon@test.com → HTTP 400 |
| `MalformedPayloadTest` | 004 | CA-3 | JSON roto → 400 + registro en orphan_payloads |
| `WhitelistDomainTest` | 004 | CA-4,12 | Dominio registrado → pass; no registrado → 403. CRUD admin works |
| `HmacValidationTest` | 004 | CA-10 | Firma válida → pass; firma inválida → 401 |
| `ClamAvScanTest` | 004 | CA-11 | Archivo limpio → CLEAN; archivo infectado → 422; ClamAV caído → 503 + DLQ |
| `PayloadSizeLimitTest` | 004 | CA-7 | 10MB → OK; 11MB → 413 Payload Too Large |
| `RabbitMqBufferTest` | 004 | CA-6 | Camunda offline → mensaje en cola; Camunda online → procesamiento diferido |
| `PreTriageTaskCreationTest` | 004 | CA-8,9 | Webhook aprobado → tarea Pre-Triaje creada, proceso BPMN NO instanciado |
| `PurgeRejectedPayloadsTest` | 004 | CA-13 | Payloads > 30 días → purgados físicamente |

---

## 6. Mensaje de Despacho

> **Para el Agente Backend:**
> Lee los Criterios de Aceptación actualizados en `docs/requirements/epics/epic_A_motor_core.md` (US-017: líneas 1009–1234, US-004: líneas 593–736). Implementa las Iteraciones 1 a 4 secuencialmente. Los DDL de Liquibase, firmas de servicios y estructura de paquetes están prescritos arriba — NO improvises la arquitectura.
>
> **Compilación obligatoria:** Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.
>
> **Variable de entorno nueva requerida:** Agrega `IBPMS_PII_ENCRYPTION_KEY=dev-local-aes256-key-32chars!!` al `application.yml` (profile: default).
>
> **Docker nuevo requerido:** Agrega el servicio `ibpms-clamav` al `docker-compose.yml` con la configuración provista arriba.
