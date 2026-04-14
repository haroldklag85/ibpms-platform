# Sprint 2 — DUAL TRACK: CQRS/DMN (J-02) + Validación Restante

> **Sprint:** 2  
> **Duración:** 4 días laborales  
> **Prerequisito:** Gate Sprint 1 aprobado (J-04 funcional + ≥5 US validadas)  
> **Objetivo:** Completar el Journey J-02 (Diseñar Proceso → Crear Formulario → Desplegar → Ejecutar) e implementar el motor CQRS.  
> **Journeys:** J-02 (Build) + Validación restante + Remediación fantasmas (Validate)

---

## Contexto del Sprint

### Input del Sprint 1
- **J-04 funcional:** Operario puede ver tareas, reclamar y enviar formulario básico
- **US validadas:** US-005, US-003, US-036, US-038, US-048 clasificadas (QA TRACK B)
- **Fantasmas detectados:** [Se completa post-Sprint 1]
- **Coverage matrix:** Actualizada tras Sprint 1

### Qué falta para J-02
El J-02 requiere que el flujo completo BPMN → Formulario → Despliegue → Ejecución funcione. Las piezas existentes (US-005 modeler + US-003 form builder) ya están construidas. Falta:
1. **US-017 (CQRS):** El motor de persistencia y event sourcing
2. **US-007 (DMN):** El motor de reglas de negocio
3. **Integración:** Vincular formulario ↔ BPMN ↔ CQRS

---

## Pre-Sprint (Día 0 — 2-3 horas)

### 👤 Jefe de Equipo
- [ ] Confirma que J-02 es la prioridad del sprint
- [ ] Revisa el backlog de fantasmas del Sprint 1 y aprueba plan de remediación

### 📋 Agente PO
- [ ] Refina CAs de US-017 (CQRS) — verificar que los 16 CAs tienen Gherkin completo
- [ ] Refina CAs de US-007 (DMN) — verificar que los CAs tienen Gherkin completo
- [ ] Prepara escenarios UAT J-02 detallados (step-by-step para cada CA)

### ★ Arquitecto Lead
- [ ] Emite ADR: "Event Sourcing Pattern para form_event_store"
- [ ] Emite ADR: "DMN Engine Integration (Camunda DMN vs custom)"
- [ ] Descompone US-017 y US-007 en tasks con contratos de persistencia
- [ ] Genera sprint_plan_s2.json con orden de ejecución
- [ ] Revisa fantasmas del Sprint 1 y asigna remediación al TRACK B

---

## TRACK A — BUILD (Días 1-3)

### US-017: Persistencia Hexagonal CQRS y Task Completion (16 CAs)

> **Estado actual:** 0% implementado. Es la pieza más crítica del sistema — conecta el formulario con Camunda.

#### Fase 1 — Core CQRS (Día 1)

| CA | Título | Prioridad | Handoff |
|----|--------|-----------|---------|
| CA-1 | Enviar datos válidos POST /complete | MUST | T-S2-01 |
| CA-2 | Validación JSON Schema 400 | MUST | T-S2-01 |
| CA-9 | Zod Isomórfico Guillotina | MUST | T-S2-02 |
| CA-12 | CQRS Event Sourcing | MUST | T-S2-03 |

**Handoff Backend (T-S2-01):**
```
Endpoint de Task Completion:
- POST /api/v1/tasks/{taskId}/complete
  Body: { formData: {...}, schemaVersion: "1.0", idempotencyKey: UUID }
- Validación: formData contra JSON Schema derivado de Zod schema de la US-003
- Response: 200 { eventId, taskId, nextNodeId }
- Response: 400 { errors: [{field, issue}] } si validation falla
- Response: 409 si idempotencyKey ya existe → CA-8

Persistencia CQRS:
- INSERT en form_event_store: { event_id, task_id, event_type, payload_json, created_at, version }
- NO hace UPDATE. Solo INSERT (append-only, event sourcing)
- Señalización a Camunda: RuntimeService.signal(executionId) tras INSERT exitoso

Tests:
- TaskCompletionControllerTest (happy path + 400 + 409)
- FormEventStoreRepositoryTest (append-only verification)
- CamundaSignalIntegrationTest (mock Camunda RuntimeService)
```

**Handoff Frontend (T-S2-02):**
```
Form Submission Flow:
- Componente TaskFormView.vue consume useFormSubmission store
- Al submit: generar idempotencyKey (crypto.randomUUID())
- POST /complete con formData + schemaVersion + idempotencyKey
- Validación Zod client-side ANTES del POST (guillotina: si Zod rechaza, no enviar)
- Loading state: botón "Enviando..." + disabled
- Success: navegar al Workdesk + toast "Tarea completada"
- Error 400: mostrar errores inline bajo cada campo
- Error 409: toast "Esta tarea ya fue enviada" (anti-doble-clic)
```

#### Fase 2 — Hardening CQRS (Día 2)

| CA | Título | Prioridad | Handoff |
|----|--------|-----------|---------|
| CA-3 | Inyección BFF Megalítica | MUST | T-S2-04 |
| CA-4 | Lazy Patching V1→V2 | SHOULD | T-S2-04 |
| CA-5 | Upload-First + Anti-IDOR | MUST | T-S2-05 |
| CA-6 | Draft Sync + Cifrado PII LS | SHOULD | T-S2-06 |
| CA-7 | RYOW Consistencia Eventual | MUST | T-S2-07 |
| CA-8 | Idempotencia Anti-Doble-Clic | MUST | (ya incluido en T-S2-01) |

**Handoff Backend (T-S2-04):**
```
BFF Mega-DTO Endpoint:
- GET /api/v1/tasks/{taskId}/form-context
  Response: {
    task: { id, processInstanceId, slaDeadline, assigneeId },
    formSchema: { zodSchema, layoutVue, version },
    formDraft: { savedData, savedAt } | null,
    attachments: [ { id, filename, url, uploadedAt } ],
    processContext: { variables, currentNodeName }
  }
- Este endpoint reemplaza 4-5 llamadas individuales
- Lazy Patching: si formSchema.version != formDraft.version → migrar draft
```

**Handoff Backend (T-S2-05):**
```
Upload-First Pattern:
- POST /api/v1/attachments/upload (multipart, pre-form-submit)
  Response: { attachmentId, presignedUrl, expiresAt }
- Anti-IDOR: attachmentId vinculado a taskId + currentUser
  Si otro usuario intenta acceder → 403
- Al submit del formulario: el formData referencia attachmentIds, no archivos raw
```

**Handoff Frontend (T-S2-06):**
```
Draft Sync:
- useFormDraft store:
  - Auto-save cada 30s a LocalStorage (cifrado AES con key derivada de userId)
  - Sync con Backend: POST /api/v1/tasks/{taskId}/draft cada 60s
  - Al abrir formulario: restaurar draft si existe
  - Al enviar: limpiar draft de LS + Backend
```

#### Fase 3 — Integración Camunda (Día 2-3)

| CA | Título | Prioridad | Handoff |
|----|--------|-----------|---------|
| CA-13 | Exclusión Topológica Camunda | SHOULD | T-S2-08 |
| CA-14 | ACID Fallback Saga Inverso | MUST | T-S2-08 |
| CA-15 | Auto-Claim Group-Level | SHOULD | T-S2-09 |
| CA-16 | Trazabilidad Rechazos BFF | SHOULD | T-S2-09 |

**Handoff Backend (T-S2-08):**
```
Integración Camunda:
- Tras INSERT en form_event_store:
  1. RuntimeService.signal(executionId, {formData}) → avanza flujo BPMN
  2. Si Camunda falla: Saga Inversa → marcar evento como ROLLBACK en form_event_store
  3. Exclusión topológica: verificar que el nodo actual acepta signal antes de enviar
- Tests: CamundaSagaTest (simulate Camunda failure → verify rollback event)
```

---

### US-007: Motor de Reglas DMN

> **Estado actual:** Parcialmente implementado (auditoría previa). Requiere integración con el flujo BPMN.

**Handoff Backend (T-S2-10):**
```
DMN Execution:
- Cuando un gateway BPMN requiere evaluación DMN:
  1. Leer tabla DMN del deployment de Camunda
  2. Evaluar con DmnEngine.evaluateDecision(decisionKey, variables)
  3. Retornar resultado al gateway para routing
- Endpoint manual: POST /api/v1/dmn/{decisionKey}/evaluate
  Body: { variables: {...} }
  Response: { result: {...}, hitPolicy: "FIRST" }
```

**Handoff Frontend (T-S2-11):**
```
DMN Viewer (solo lectura):
- Componente DmnViewer.vue que renderiza tabla DMN
- Usado dentro del BPMN modeler cuando se selecciona un BusinessRuleTask
- Read-only en Sprint 2 (edición visual en Sprint 3+)
```

---

### Orden de Ejecución TRACK A

```
Día 1:
  Backend(US-017 Core CQRS: CA-1,2,9,12) → ★Lead audita
  Frontend(US-017 Form Submission: CA-1,9) → ★Lead audita

Día 2:
  Backend(US-017 Hardening: CA-3,4,5,8) → ★Lead audita
  Backend(US-017 Camunda Integration: CA-13,14) → ★Lead audita  
  Frontend(US-017 Draft Sync + Upload: CA-5,6) → ★Lead audita

Día 3:
  Backend(US-007 DMN Engine: CA-1..3) → ★Lead audita
  Frontend(US-007 DMN Viewer) → ★Lead audita
  ★Lead: Integración J-02 completo (BPMN → Form → CQRS → Camunda)
```

---

## TRACK B — VALIDATE + REMEDIATE (Días 1-3)

### Objetivo
1. Validar las US restantes que aún no tienen QA
2. Remediar fantasmas detectados en Sprint 1
3. Escribir E2E completo para J-02

### US a Validar

| US | Estado Actual | QA Pendiente | Acción Sprint 2 |
|----|---------------|-------------|-----------------|
| US-000 | ✅ Completada | ❌ 0% | Validar degradación grácil + PII masking |
| US-034 | ✅ Completada | ✅ CA-4..10 | Verificar Dashboard DLQ funciona |
| US-039 | ✅ Completada | ✅ CA-4..8 | Verificar formulario genérico base |
| US-028 | ✅ Completada | ✅ CA-12..17 | Verificar test suites auto-generadas |
| US-043 | ✅ Completada (deuda) | ❌ 0% | Validar SLA config + detectar estado CA-6 |

### Handoff QA (T-S2-12)

```
Validación Funcional Sprint 2:
1. US-000: Provocar error 500 → verificar degradación grácil + PII masked
2. US-034: Enviar mensaje a RabbitMQ → verificar que llega + DLQ funciona
3. US-043: Configurar SLA → verificar que se aplica a tareas nuevas
4. US-028: Generar test suite desde schema Zod → verificar que ejecuta

Tests E2E Playwright J-02:
- e2e/j02/process-design-deploy.spec.ts
  → Crear proceso BPMN → deploy → verificar en lista
- e2e/j02/form-link-process.spec.ts
  → Vincular formulario a user task → deploy → verificar
- e2e/j02/first-task-execution.spec.ts
  → Iniciar instancia → tarea aparece en Workdesk → reclamar → completar
```

### Remediación de Fantasmas

| US | Hallazgo Sprint 1 | Remediación |
|----|-------------------|-------------|
| [Se completa post-Sprint 1] | — | — |

---

## Día 4 — Integración y Gate

### ★ Arquitecto Lead — Gate Técnico

| # | Criterio | Meta | Estado |
|---|----------|------|--------|
| 1 | US-017 CQRS implementado | ≥12/16 CAs construidos | ⬜ |
| 2 | US-007 DMN evaluable | Engine funcional + Viewer | ⬜ |
| 3 | J-02 E2E completo | BPMN → Form → Deploy → Execute → CQRS | ⬜ |
| 4 | ADRs emitidos | Event Sourcing + DMN Engine documentados | ⬜ |
| 5 | Tests E2E Sprint 2 | ≥8 specs verdes (J-02 + validación) | ⬜ |
| 6 | US validadas TRACK B | ≥5 US clasificadas | ⬜ |
| 7 | Coverage matrix | 10/10 US del scope validadas | ⬜ |
| 8 | OBS US-005 cerradas | OBS-1 (CA-68) + OBS-2 (CA-65) resueltas | ⬜ |

### 📋 Agente PO — Gate Funcional

| # | Criterio | Meta | Estado |
|---|----------|------|--------|
| 1 | CAs US-017 coinciden con spec | Sin gaps funcionales en CQRS | ⬜ |
| 2 | UAT J-02 cubierto | ≥10/12 escenarios passing | ⬜ |
| 3 | Deuda técnica US-043 CA-6 | Plan de remediación definido | ⬜ |

### 👤 Jefe de Equipo — Gate Final

| # | Criterio | Meta | Estado |
|---|----------|------|--------|
| 1 | Demo J-02 funcional | Crear proceso → form → deploy → ejecutar 1ª tarea | ⬜ |
| 2 | UAT manual J-02 | Flujo completo sin errores bloqueantes | ⬜ |
| 3 | J-04 sigue funcionando | Regresión: demo J-04 sin degradación | ⬜ |

### Definición de Listo

```
GATE S2 = Gate Técnico (≥12 CAs CQRS + J-02 E2E + ≥8 specs + 10/10 US validadas)
         ∧ Gate Funcional (UAT J-02 ≥10/12 + plan deuda US-043)
         ∧ Gate Final (Demo J-02 + J-04 regresión OK)
```

---

## Riesgos y Mitigaciones

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|:---:|:---:|------------|
| Camunda signal integration falla | Media | Alto | Fallback: polling con ScheduledTask cada 5s |
| Event sourcing complejidad excesiva | Baja | Medio | Simplificar a INSERT-only sin replay en Sprint 2 |
| DMN engine no disponible en Camunda embebido | Media | Medio | Evaluar DMN como microservicio separado |
| RYOW consistencia eventual inconsistente | Media | Alto | Redis cache de 2s post-write para RYOW |
| Fantasmas del Sprint 1 consumen todo el TRACK B | Baja | Alto | Limitar remediación a ≤2 US fantasma, posponer resto |

---

## Historial de Cambios

| Fecha | Cambio | Autor |
|-------|--------|-------|
| 2026-04-10 | Creación del Sprint 2 plan | Arquitecto Lead |
