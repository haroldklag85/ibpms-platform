# Sprint 3 — Consolidación + Expansión (J-03, J-06, J-07)

> **Sprint:** 3  
> **Duración:** 4 días laborales  
> **Prerequisito:** Gate Sprint 2 aprobado (J-02 funcional + 10/10 US validadas + CQRS operativo)  
> **Objetivo:** Expandir la plataforma con Webhooks (US-004), Kanban (US-008), Proyectos Ágiles (US-030), y endurecer la seguridad (J-03) y los NFRs.  
> **Journeys:** J-03 (RBAC E2E), J-06 (Kanban/BAM), J-07 (SLA Escalamiento)

---

## Contexto del Sprint

### Estado Acumulado Post-Sprint 2
- **J-04:** ✅ Funcional — Operario ve tareas, reclama, completa formulario, CQRS persiste
- **J-02:** ✅ Funcional — Arquitecto BPM diseña proceso, crea formulario, despliega, ejecuta primera tarea
- **US validadas QA:** 10/10 del scope de Sprints 1-2
- **Tests E2E:** ≥13 specs verdes (smoke + J-04 + J-02)
- **CQRS:** Event sourcing operativo con Camunda signal
- **Deuda técnica conocida:** US-043 CA-6, OBS US-005 (si no cerradas en S2)

### Qué aporta Sprint 3
1. **J-03 (RBAC):** Verificar que la seguridad Zero Trust funciona E2E
2. **J-06 (Kanban):** PMO puede gestionar proyectos con tablero Kanban
3. **J-07 (SLA):** Verificar que las alertas de quiebre SLA funcionan en ejecución
4. **Hardening:** Tests E2E negativos + edge cases + performance smoke
5. **US-004 (Webhook):** Trigger de proceso por evento externo (prerequisito para J-01)

---

## Pre-Sprint (Día 0 — 2-3 horas)

### 👤 Jefe de Equipo
- [ ] Confirma que la expansión a J-03/J-06/J-07 es la prioridad
- [ ] Revisa el backlog de deuda técnica y aprueba plan de remediación
- [ ] Decide: ¿Se incluye US-004 (Webhook) en este sprint?

### 📋 Agente PO
- [ ] Refina CAs de US-004 (Webhook/Trigger), US-008 (Kanban), US-030 (Proyecto Ágil)
- [ ] Crea Casos de Uso UAT para J-03, J-06, J-07 (si no existen)
- [ ] Valida que los CAs Gherkin del sprint están completos

### ★ Arquitecto Lead
- [ ] Evalúa deuda técnica acumulada de Sprints 1-2
- [ ] Descompone US-004, US-008, US-030 en tasks técnicos
- [ ] Emite ADR si necesario: "Webhook Gateway Pattern" para US-004
- [ ] Genera sprint_plan_s3.json
- [ ] Planifica hardening NFR para TRACK B

---

## TRACK A — BUILD (Días 1-3)

### US-004: Webhook Trigger / Inicio de Proceso por Evento Externo

> **Criticidad:** ALTA — Prerequisito para J-01 (Intake de correo). En J-02 el proceso se inicia manualmente; US-004 habilita el inicio automático.

| CA (estimado) | Título | Prioridad |
|---------------|--------|-----------|
| CA-1 | Registrar Webhook endpoint por proceso BPMN | MUST |
| CA-2 | Validar payload contra schema JSON esperado | MUST |
| CA-3 | Iniciar instancia de proceso con variables del webhook | MUST |
| CA-4 | Autenticación: API Key o HMAC signature | MUST |
| CA-5 | Rate limiting por webhook endpoint | SHOULD |
| CA-6 | Retry logic con DLQ para webhooks fallidos | SHOULD |
| CA-7 | Dashboard de webhooks recibidos (Pantalla Admin) | COULD |

**Handoff Backend (T-S3-01):**
```
Webhook Gateway:
- POST /api/v1/webhooks/{webhookId}/trigger
  Headers: X-Webhook-Secret: <HMAC-SHA256>
  Body: { ...payload }
- Flujo:
  1. Validar HMAC signature
  2. Validar payload contra schema registrado
  3. RuntimeService.startProcessInstanceByKey(processKey, variables)
  4. Publicar evento en RabbitMQ para auditoría
- Rate limiting: Bucket4j (10 req/min por webhookId)
- Si falla Camunda: enviar a DLQ (retry 3x con backoff)

Registro de Webhooks:
- POST /api/v1/webhooks (Admin only)
  Body: { processKey, expectedSchema, secret }
  Response: { webhookId, url, createdAt }

Tests:
- WebhookControllerTest (happy + invalid signature + rate limit)
- WebhookCamundaIntegrationTest (process starts on trigger)
```

**Handoff Frontend (T-S3-02):**
```
Pantalla Admin de Webhooks:
- Lista de webhooks registrados (DataGrid)
- Modal de creación: seleccionar proceso → auto-generar secret → copiar URL
- Historial de invocaciones (últimas 50)
- Indicadores: ✅ éxito, ❌ fallo, ⏳ retry pendiente
```

---

### US-008: Tablero Kanban

> **Criticidad:** MEDIA — Journey J-06. PMO gestiona proyectos con metodología ágil.

| CA (estimado) | Título | Prioridad |
|---------------|--------|-----------|
| CA-1 | Visualizar tablero Kanban con columnas configurables | MUST |
| CA-2 | Drag & drop de tarjetas entre columnas | MUST |
| CA-3 | Crear/editar tarjeta con campos: título, descripción, asignado, prioridad | MUST |
| CA-4 | WIP limits por columna | SHOULD |
| CA-5 | Filtros por asignado, prioridad, etiquetas | SHOULD |
| CA-6 | Swimlanes por épica o equipo | COULD |

**Handoff Backend (T-S3-03):**
```
Endpoints Kanban:
- GET /api/v1/projects/{projectId}/board → columnas + tarjetas
- PUT /api/v1/cards/{cardId}/move → { columnId, position }
- POST /api/v1/cards → crear tarjeta
- PATCH /api/v1/cards/{cardId} → editar
- Validar WIP limits: si columna está llena → 422 con mensaje específico

Tests:
- KanbanBoardControllerTest
- CardMoveValidationTest (WIP limits)
```

**Handoff Frontend (T-S3-04):**
```
Componente KanbanBoard.vue:
- Librería drag & drop: vue-draggable-plus o @formkit/drag-and-drop
- Columnas renderizadas dinámicamente desde API
- Cada tarjeta: Card component con avatar asignado + badge prioridad
- Optimistic update: mover card inmediatamente, revertir si API falla
- WIP limit: indicador visual cuando columna está al límite (borde amarillo/rojo)
```

---

### US-030: Gestión de Proyectos Ágiles

> **Criticidad:** MEDIA — Journey J-06. Complementa US-008 con la capa de gestión de proyectos.

| CA (estimado) | Título | Prioridad |
|---------------|--------|-----------|
| CA-1 | Crear proyecto con nombre, descripción, tipo (Ágil/Tradicional) | MUST |
| CA-2 | Configurar sprints con fecha inicio/fin | MUST |
| CA-3 | Asignar tareas a sprint (backlog → sprint) | MUST |
| CA-4 | Dashboard de velocidad del equipo (velocity chart) | SHOULD |
| CA-5 | Burndown chart por sprint | SHOULD |

**Handoff Backend (T-S3-05):**
```
Endpoints Proyecto Ágil:
- POST /api/v1/projects → { name, description, type: "AGILE" }
- POST /api/v1/projects/{id}/sprints → { name, startDate, endDate }
- POST /api/v1/sprints/{id}/cards → { cardIds: [...] }
- GET /api/v1/projects/{id}/velocity → { sprints: [{completedPoints, ..}] }

Tests:
- ProjectControllerTest, SprintPlanningTest
```

**Handoff Frontend (T-S3-06):**
```
ProjectDashboard.vue:
- Lista de proyectos con filtros (tipo, estado)
- Sprint planning view: columnas Backlog | Sprint N | Done
- Drag & drop de tarjetas entre Backlog y Sprint
- Charts: ApexCharts para velocity + burndown
```

---

### Orden de Ejecución TRACK A

```
Día 1:
  Backend(US-004 Webhook Gateway: CA-1..4) → ★Lead audita
  Frontend(US-004 Admin Webhooks UI) → ★Lead audita

Día 2:
  Backend(US-008 Kanban Board: CA-1..4) → ★Lead audita
  Frontend(US-008 KanbanBoard.vue + drag&drop) → ★Lead audita

Día 3:
  Backend(US-030 Proyectos Ágiles: CA-1..3) → ★Lead audita
  Frontend(US-030 Project Dashboard) → ★Lead audita
  ★Lead: Integración J-06 (Proyecto → Kanban → Dashboard)
```

---

## TRACK B — HARDEN + VALIDATE (Días 1-3)

### Objetivo
1. **Tests E2E negativos y edge cases** para J-04 y J-02
2. **Performance smoke** (NFR-PER-01, NFR-PER-02)
3. **Auditoría de seguridad** (NFR-SEC-01 Zero Trust) → validar J-03
4. **Remediar deuda técnica** acumulada

### Tests E2E Negativos / Edge Cases

| Test | Journey | Tipo | CA |
|------|---------|------|----|
| Submit formulario con campos vacíos → 400 | J-04 | Negative | US-017 CA-2 |
| Doble-clic en submit → idempotencia | J-04 | Edge | US-017 CA-8 |
| Reclamar tarea ya reclamada → 409 | J-04 | Negative | US-002 CA-3 |
| Desplegar BPMN con XML inválido → error claro | J-02 | Negative | US-005 |
| Webhook con HMAC inválido → 401 | J-06 | Negative | US-004 CA-4 |
| Rate limit webhook → 429 | J-06 | Negative | US-004 CA-5 |
| WIP limit Kanban → no mover → 422 | J-06 | Negative | US-008 CA-4 |

### Performance Smoke

| NFR | Test | Meta | Herramienta |
|-----|------|------|-------------|
| NFR-PER-01 | Carga de Workdesk con 100 tareas | < 2s | Playwright + performance.now() |
| NFR-PER-02 | Renderizado de formulario con 50 campos | < 1s | Playwright + performance.now() |
| NFR-PER-04 | INSERT en form_event_store (100 eventos) | < 500ms | JUnit + StopWatch |

### Auditoría de Seguridad (J-03)

| Escenario | US | Resultado Esperado |
|-----------|----|--------------------|
| Login con rol "Operario_Limitado" | US-036 | Solo ve Workdesk + Formularios |
| Intento URL /admin/modeler → Gaslighting 404 | US-051 | 404 (NO 403) |
| API call sin JWT → 401 | US-036 | Respuesta genérica sin info |
| Intento IDOR: acceder a tarea de otro tenant | US-036 CA-20 | 403 + audit log |
| Service Account API Key → solo endpoints permitidos | US-036 CA-22 | 200 en permitidos, 403 en otros |

**Handoff QA (T-S3-07):**
```
Tests E2E Playwright (Security):
- e2e/j03/rbac-sidebar-filter.spec.ts → login + verificar sidebar items
- e2e/j03/gaslighting-404.spec.ts → navegar URL prohibida → 404
- e2e/j03/idor-prevention.spec.ts → API call con taskId ajeno → 403
- e2e/j03/service-account-access.spec.ts → API Key valid → 200

Tests E2E Playwright (Performance):
- e2e/performance/workdesk-load-100.spec.ts
- e2e/performance/form-render-50-fields.spec.ts
```

### Remediación Deuda Técnica

| Deuda | Sprint Origen | Acción |
|-------|---------------|--------|
| US-043 CA-6 | S1 | Backend fix + test |
| US-005 OBS-1 (CA-68 Entity/DDL) | S1 | Alinear Entity con DDL |
| US-005 OBS-2 (CA-65 Contrato API) | S1 | Completar contrato REST |
| Fantasmas Sprint 1-2 | S1-S2 | Remediar según sprint reports |

---

## Día 4 — Integración y Gate

### ★ Arquitecto Lead — Gate Técnico

| # | Criterio | Meta | Estado |
|---|----------|------|--------|
| 1 | US-004 Webhook funcional | ≥4 CAs construidos | ⬜ |
| 2 | US-008 Kanban funcional | ≥4 CAs construidos + drag&drop | ⬜ |
| 3 | US-030 Proyectos | ≥3 CAs construidos | ⬜ |
| 4 | J-03 RBAC E2E pasando | ≥4 specs security verdes | ⬜ |
| 5 | Performance smoke | NFR-PER-01 < 2s, NFR-PER-02 < 1s | ⬜ |
| 6 | Tests E2E acumulados | ≥18 specs verdes (S0+S1+S2+S3) | ⬜ |
| 7 | Deuda técnica | ≥2/4 items resueltos | ⬜ |
| 8 | ADRs | Webhook Gateway documentado | ⬜ |

### 📋 Agente PO — Gate Funcional

| # | Criterio | Meta | Estado |
|---|----------|------|--------|
| 1 | UAT J-03 cubierto | ≥5 escenarios security passing | ⬜ |
| 2 | UAT J-06 cubierto | Proyecto → Kanban → Dashboard fluye | ⬜ |
| 3 | UAT J-07 cubierto | SLA configura → semáforo cambia → alerta | ⬜ |
| 4 | Sin gaps funcionales | CAs del sprint coinciden con spec | ⬜ |

### 👤 Jefe de Equipo — Gate Final

| # | Criterio | Meta | Estado |
|---|----------|------|--------|
| 1 | Demo 3 Journeys | J-02 + J-04 + J-06 funcionales | ⬜ |
| 2 | Seguridad J-03 | Gaslighting 404 funciona en vivo | ⬜ |
| 3 | Performance aceptable | App responde < 2s en escenarios medidos | ⬜ |
| 4 | Regresión J-02 + J-04 | Sin degradación de sprints anteriores | ⬜ |

### Definición de Listo

```
GATE S3 = Gate Técnico (US-004+008+030 + J-03 E2E + Performance + ≥18 specs)
         ∧ Gate Funcional (UAT J-03+J-06+J-07 + sin gaps)
         ∧ Gate Final (Demo 3 Journeys + Security vivo + Performance OK)
```

---

## Riesgos y Mitigaciones

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|:---:|:---:|------------|
| Kanban drag&drop lib incompatible con Vue 3.4+ | Media | Medio | Evaluar @formkit/drag-and-drop vs nativo HTML5 |
| Performance NFR-PER-01 no pasa con 100 tareas | Media | Alto | Optimizar: virtual scrolling + lazy loading |
| RBAC Gaslighting 404 falla con Vue Router | Baja | Alto | Implementar como navigation guard personalizado |
| Webhook retry storm | Baja | Medio | Circuit breaker + max 3 retries + DLQ |
| Deuda técnica consume todo el TRACK B | Media | Medio | Limitar deuda a ≤2 items, posponer resto a S4 |

---

## Historial de Cambios

| Fecha | Cambio | Autor |
|-------|--------|-------|
| 2026-04-10 | Creación del Sprint 3 plan | Arquitecto Lead |
