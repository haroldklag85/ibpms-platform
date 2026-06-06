# Handoff Arquitectónico — 🧪 Agente QA
# Sprint 3: Matriz RTM Completa (US-017, US-004, US-030)

> **Emitido por:** `[🧠 ARQUITECTO LÍDER]` | **Fecha:** 2026-04-17
> **Sprint / Rama:** `sprint-3/qa/rtm-sprint3`
> **Protocolo Aplicado:** `.agents/skills/architect_handoff_protocol/SKILL.md`

---

## 1. Metadatos y SSOT

| Campo | Valor |
|-------|-------|
| **Sprint** | 3 — Retorno al Feature Factory |
| **User Stories** | US-017 (18 CAs), US-004 (17 CAs), US-030 (14 CAs) |
| **SSOT** | `docs/requirements/epics/epic_A_motor_core.md` (líneas 593–1234) |
| **Prerequisito** | Backend + Frontend DEBEN estar compilados antes de ejecutar E2E |
| **Framework E2E** | Playwright (configuración existente en `frontend/playwright.config.ts`) |
| **Framework Unit Backend** | JUnit 5 + Testcontainers (configuración en `backend/ibpms-core/pom.xml`) |
| **Framework Unit Frontend** | Vitest + Happy-DOM (configuración en `frontend/vitest.config.ts`) |

---

## 2. Alineación con la Regla de Oro V2

Toda funcionalidad del Sprint 3 DEBE tener certificación en las **3 capas obligatorias**:

| Capa | Herramienta | Responsable |
|------|-------------|:-----------:|
| **Backend Unit/Integration** | JUnit 5 + Testcontainers (PostgreSQL, Redis, RabbitMQ) | ⚙️ Backend |
| **Frontend Unit/Logic** | Vitest + Happy-DOM | 🎨 Frontend |
| **End-to-End (E2E)** | Playwright | 🧪 QA |

> [!IMPORTANT]
> Los tests Testcontainers y Happy-DOM son responsabilidad de los agentes Backend y Frontend respectivamente (prescritos en sus handoffs). Este handoff de QA se enfoca en los **Playwright E2E specs** y la **Matriz RTM** de certificación cruzada.

---

## 3. Rutas de Specs E2E a Crear

| Archivo | US | Pantalla | Flujo |
|---------|:--:|:--------:|-------|
| `frontend/e2e/intake-webhook.spec.ts` | 004 | P16 | Simular Webhook → verificar tarea de Pre-Triaje aparece |
| `frontend/e2e/intake-triage-approve.spec.ts` | 004 | P16 | Operario aprueba correo → selecciona proceso → caso BPMN creado |
| `frontend/e2e/intake-triage-reject.spec.ts` | 004 | P16 | Operario rechaza correo → motivo obligatorio → estado Cancelado |
| `frontend/e2e/intake-sla-indicator.spec.ts` | 004 | P16 | Crear tarea Pre-Triaje → verificar semáforo SLA cambia con el tiempo |
| `frontend/e2e/agile-hub-crud.spec.ts` | 030 | P10 | Crear proyecto vacío → crear tarjeta → editar → eliminar |
| `frontend/e2e/agile-hub-assign.spec.ts` | 030 | P10 | Crear tarjeta → asignar 2 responsables → verificar multi-select |
| `frontend/e2e/agile-hub-dragdrop.spec.ts` | 030 | P10 | Crear 3 tarjetas → drag tarea 3 a posición 1 → verificar orden persistido |
| `frontend/e2e/agile-hub-filters.spec.ts` | 030 | P10 | Crear tarjetas con tags → filtrar por tag → verificar resultado |
| `frontend/e2e/agile-hub-stale.spec.ts` | 030 | P10 | Crear tarjeta con fecha antigua (API seed) → verificar badge ámbar |
| `frontend/e2e/agile-hub-close-project.spec.ts` | 030 | P10 | Crear proyecto con tareas → cerrar proyecto → tareas canceladas + solo lectura |

---

## 4. Matriz RTM (Requirements Traceability Matrix) — Sprint 3

### US-017: Persistencia CQRS & Event Sourcing

| CA | Título | Test Backend (Testcontainers) | Test Frontend (Happy-DOM) | Test E2E (Playwright) |
|:--:|--------|:-----------------------------:|:-------------------------:|:---------------------:|
| CA-01 | CQRS Event Sourcing | `FormCompletionSagaTest` | — | — |
| CA-02 | Exclusión Topológica Camunda | `FormCompletionSagaTest` | — | — |
| CA-03 | Rollback Saga | `FormCompletionSagaTest` | — | — |
| CA-04 | Auto-Claim grupo | `AutoClaimGroupTaskTest` | — | — |
| CA-05 | Trazabilidad rechazos BFF | `RejectionLogServiceTest` | — | — |
| CA-06 | Esquema Event Store | `FormEventStoreImmutabilityTest` | — | — |
| CA-07 | Endpoints Draft GET/PUT/DELETE | `TaskDraftCrudTest` | — | — |
| CA-08 | Reconciliación US-017↔US-029 | Documentación | — | — |
| CA-09 | Exclusión Drafts del Event Store | `FormEventStoreImmutabilityTest` | — | — |
| CA-10 | Rollback con Retry 3x | `FormCompletionSagaTest` | — | — |
| CA-11 | Estructura rejectionLogs | `RejectionLogServiceTest` | — | — |
| CA-12 | Cifrado PII At-Rest | `PiiEncryptionRoundTripTest` | — | — |
| CA-13 | Validación grupo Auto-Claim | `AutoClaimGroupTaskTest` | — | — |
| CA-14 | Rate-Limiting /draft | `RateLimitDraftTest` | — | — |
| CA-15 | Event Reference EVT-XXXXX | `EventReferenceFormatTest` | — | — |
| CA-16 | Cleanup Draft on Submit | `DraftCleanupOnSubmitTest` | — | — |
| CA-17 | SLA Latencia ≤5s/17s | `FormCompletionSagaTest` (timing) | — | — |
| CA-18 | Archivado Anual | `EventArchiveSchedulerTest` | — | — |

### US-004: Webhook Intake (O365 Listener)

| CA | Título | Test Backend | Test Frontend | Test E2E |
|:--:|--------|:------------:|:-------------:|:--------:|
| CA-1 | Idempotencia | `IdempotencyWebhookTest` | — | — |
| CA-2 | Bloqueo auto-responders | `AutoResponderBlockTest` | — | — |
| CA-3 | Payloads huérfanos | `MalformedPayloadTest` | — | — |
| CA-4 | Whitelist dominios | `WhitelistDomainTest` | — | — |
| CA-5 | Notificación falla admin | `AdminNotificationTest` | — | — |
| CA-6 | Resiliencia RabbitMQ | `RabbitMqBufferTest` | — | — |
| CA-7 | Límite de peso | `PayloadSizeLimitTest` | — | — |
| CA-8/9 | Intake Triage / Pre-Triaje | `PreTriageTaskCreationTest` | — | `intake-webhook.spec.ts` |
| CA-10 | HMAC / Bearer switch | `HmacValidationTest` | — | — |
| CA-11 | Anti-Malware ClamAV | `ClamAvScanTest` | — | — |
| CA-12 | Admin CRUD Whitelist | `WhitelistDomainTest` | — | — |
| CA-13 | Purga 30 días | `PurgeRejectedPayloadsTest` | — | — |
| CA-14 | Pre-visualización Aprobar/Rechazar | — | `intakeStore.spec.ts` | `intake-triage-approve.spec.ts` |
| CA-15 | Canalización proceso BPMN | — | `intakeStore.spec.ts` | `intake-triage-approve.spec.ts` |
| CA-16 | Reloj SLA de entrada | — | `SlaIndicator.spec.ts` | `intake-sla-indicator.spec.ts` |
| CA-17 | ACK sub-segundo | `WebhookResponseTimeTest` | — | — |

### US-030: Hub Ágil (Backlog Kanban Continuo)

| CA | Título | Test Backend | Test Frontend | Test E2E |
|:--:|--------|:------------:|:-------------:|:--------:|
| CA-1 | Sin Sprints (Kanban Continuo) | `AgileProjectCreationTest` | — | `agile-hub-crud.spec.ts` |
| CA-2 | Arranque: solo "Iniciar vacío" | `AgileProjectCreationTest` | `agileStore.spec.ts` | `agile-hub-crud.spec.ts` |
| CA-3 | CRUD Tarjetas (panel lateral) | `AgileTaskCrudTest` | `agileStore.spec.ts` | `agile-hub-crud.spec.ts` |
| CA-4 | Eliminación con auditoría | `AgileTaskDeleteAuditTest` | — | `agile-hub-crud.spec.ts` |
| CA-5 | Asignación multi-persona | `AgileAssignmentTest` | `agileStore.spec.ts` | `agile-hub-assign.spec.ts` |
| CA-6 | Drag & Drop reorden | `AgileReorderTest` | `useAgileDragDrop.spec.ts` | `agile-hub-dragdrop.spec.ts` |
| CA-7 | Vista Proyecto / Portafolio | — | `portfolioView.spec.ts` | — |
| CA-8 | Archivo de completadas (stub) | `AgileCompletedArchiveTest` | `agileStore.spec.ts` | — |
| CA-9 | Modificación SLA + bitácora | `SlaChangeLogTest` | — | — |
| CA-10 | Cierre cascada cancelación | `AgileProjectClosureTest` | — | `agile-hub-close-project.spec.ts` |
| CA-11 | RBAC + anti-abuso (XSS, límites) | `AgileSecurityTest` | — | — |
| CA-12 | Anatomía visual Backlog | — | `AgileHubView.spec.ts` | `agile-hub-filters.spec.ts` |
| CA-13 | Ticket Rancio (>15 días) | — | `staleBadge.spec.ts` | `agile-hub-stale.spec.ts` |
| CA-14 | Carga liviana + masivo | `AgileBulkAssignTest` | `bulkAssign.spec.ts` | — |

---

## 5. Precondiciones de Ejecución E2E

```bash
# 1. Levantar infraestructura completa
docker compose up -d ibpms-postgres ibpms-redis ibpms-rabbitmq ibpms-clamav ibpms-core

# 2. Verificar health de todos los servicios
curl http://localhost:8080/actuator/health

# 3. Ejecutar tests Playwright
cd frontend && npx playwright test e2e/ --reporter=html
```

### Seed Data Strategy
Todos los specs E2E DEBEN utilizar **APIRequestContext** de Playwright para sembrar datos programáticamente ANTES de cada test. **PROHIBIDO** depender de datos preexistentes en la BD.

Ejemplo para el spec de Intake:
```typescript
// intake-webhook.spec.ts
test.beforeEach(async ({ request }) => {
  // Seed: enviar webhook programáticamente via POST
  const webhookPayload = {
    id_mensaje: `test-${Date.now()}`,
    sender: 'cliente@ibm.com',
    subject: 'Solicitud de Servicio',
    body: 'Cuerpo del correo de prueba',
    attachments: []
  };
  await request.post('/api/v1/webhook/intake', { data: webhookPayload });
});
```

---

## 6. Regresión Obligatoria

Además de los nuevos specs E2E, el QA DEBE ejecutar regresión completa de los specs existentes:

| Suite | Path | Estado Esperado |
|-------|------|:---------------:|
| Workdesk (Sprint 2) | `frontend/e2e/workdesk.spec.ts` | ✅ Verde |
| Forms (Sprint 2) | `frontend/e2e/forms.spec.ts` | ✅ Verde |
| **Nuevos Sprint 3** | `frontend/e2e/intake-*.spec.ts` | ✅ Verde |
| **Nuevos Sprint 3** | `frontend/e2e/agile-hub-*.spec.ts` | ✅ Verde |

---

## 7. Mensaje de Despacho

> **Para el Agente QA:**
> Tu misión es certificar las 3 User Stories del Sprint 3 (US-017, US-004, US-030) con un total de 49 Criterios de Aceptación. Los tests de Testcontainers y Happy-DOM ya fueron prescritos a los agentes Backend y Frontend. Tu responsabilidad es crear y ejecutar los **10 specs Playwright E2E** listados en la sección 3, y verificar que la **Matriz RTM** (sección 4) tenga cobertura 100% antes de firmar el Gate del Sprint.
>
> **Seed Data:** Usa `APIRequestContext` siguiendo el patrón de la sección 5. PROHIBIDO seed manual.
>
> **Regresión:** Ejecuta los specs de Sprints anteriores (workdesk + forms) para certificar no-regresión.
>
> Lee los CAs en `docs/requirements/epics/epic_A_motor_core.md` (líneas 593–1234).
