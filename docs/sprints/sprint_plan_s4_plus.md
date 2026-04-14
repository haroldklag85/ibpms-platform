# Sprint 4+ — Épicas Completas con ACP a Plena Velocidad

> **Sprint:** 4, 5, 6... (iterativo hasta completar V1)  
> **Duración:** 4 días laborales por sprint  
> **Prerequisito:** Gate Sprint 3 aprobado (3 Journeys E2E verdes J-02/J-03/J-04 + Performance OK)  
> **Objetivo:** Completar las épicas restantes por dominio funcional, alcanzando cobertura total de Journeys (13) y User Stories (53+).  
> **Velocidad objetivo:** 7-10 US/sprint (vs 5 en sprints anteriores)  
> **Modelo:** ACP consolidado — mismo ciclo Pre-Sprint(Lead) → Build(Back→Lead→Front→Lead) → Validate(QA→Lead) → Gate(Lead+AgPO+Jefe)

---

## Estado Proyectado al Inicio del Sprint 4

### Journeys Completados (Sprints 0-3)

| Journey | Criticidad | Estado | Sprint |
|---------|:----------:|:------:|:------:|
| J-04 | 🔴 ALTA | ✅ | S1 |
| J-02 | 🔴 ALTA | ✅ | S2 |
| J-03 | 🔴 ALTA | ✅ | S3 |
| J-06 | 🟡 MEDIA | ✅ Parcial | S3 |
| J-07 | 🟡 MEDIA | ✅ Parcial | S3 |

### Journeys Pendientes

| Journey | Criticidad | US Core | Sprint Target |
|---------|:----------:|---------|:-------------:|
| **J-01** | 🔴 ALTA | US-037, US-016, US-004, US-012, US-013, US-014, US-040, US-022, US-023, US-049, US-026 | **S4-S5** |
| **J-08** | 🟡 MEDIA | US-037, US-016, US-004, US-012 | S4 (reutiliza J-01) |
| **J-05** | 🟡 MEDIA | US-050, US-049, US-026 | S5 |
| **J-09** | 🟡 MEDIA | US-042, US-033, US-046 | S5 |
| **J-10** | 🟡 MEDIA | US-052, US-053, US-032, US-044 | S6 |
| **J-11** | ⚪ BAJA | US-031, US-006, US-009 | S6 |
| **J-12** | ⚪ BAJA | US-007, US-005, US-027 | S6 |
| **J-13** | ⚪ BAJA | US-010, US-035 | S7 |

### US Completadas Acumuladas vs Pendientes (Estimado)

| Métrica | Valor |
|---------|-------|
| **US completadas (S0-S3)** | ~18-20 |
| **US pendientes** | ~33-35 |
| **CAs validados QA** | ~60% |
| **Tests E2E acumulados** | ≥18 specs |

---

## Sprint 4: J-01 Intake IA (Épica 9 — Buzones + Épica 10 — CRM)

> **Rumbo:** El Journey más largo y complejo del sistema. 12 pasos, 7 épicas cruzadas, 10+ US.  
> **Estrategia:** Dividir J-01 en dos mitades: S4 cubre pasos 1-6 (Intake IA), S5 cubre pasos 7-12 (ya construidos en S1-S2, solo integración).

### Pre-Sprint (★ Lead)

- [ ] Emitir ADR: "MS Graph Integration Pattern" (polling vs webhook para buzones)
- [ ] Emitir ADR: "LLM Pipeline Architecture" (clasificación intención + PII detection)
- [ ] Descomponer US por dependencia: US-037 → US-016 → US-004 → US-012/013/014 → US-040 → US-022/023
- [ ] Generar handoffs con contratos de integración MS Graph + RabbitMQ

### TRACK A — BUILD

#### US-037: Registro y Gestión de Buzones SAC

| CA (estimado) | Título | Prioridad |
|---------------|--------|-----------|
| CA-1 | CRUD de buzones corporativos | MUST |
| CA-2 | Configurar credenciales MS Graph (OAuth2 Client Credentials) | MUST |
| CA-3 | Test de conexión (ping al buzón) | MUST |
| CA-4 | Polling scheduler configurable (intervalo por buzón) | MUST |
| CA-5 | Dashboard de estado de buzones (activo/error/pausado) | SHOULD |

**Handoff Backend (T-S4-01):**
```
Buzón Service:
- POST /api/v1/mailboxes → { email, tenantId, graphClientId, graphClientSecret }
  Credenciales se almacenan en Azure Key Vault (ref: application.yml)
- GET /api/v1/mailboxes → lista buzones con último poll + status
- POST /api/v1/mailboxes/{id}/test → ping MS Graph → 200 si éxito
- ScheduledTask: MailboxPollerJob cada N minutos por buzón
  → MS Graph: GET /users/{email}/mailFolders/Inbox/messages?$filter=isRead eq false
  → Por cada correo: publicar evento a RabbitMQ exchange ibpms.intake

Tests:
- MailboxControllerTest, MSGraphIntegrationTest (mock)
- MailboxPollerJobTest (verify event published)
```

**Handoff Frontend (T-S4-02):**
```
Pantalla 16: Admin Buzones
- DataGrid de buzones (email, status, último poll, correos pendientes)
- Modal crear/editar buzón con campos OAuth2
- Botón "Test Conexión" con feedback visual (✅/❌)
- Toggle activo/pausado por buzón
```

---

#### US-016: Políticas de Procesamiento de Correos

| CA (estimado) | Título | Prioridad |
|---------------|--------|-----------|
| CA-1 | Reglas de filtrado (spam, duplicados, dominios blacklist) | MUST |
| CA-2 | Reglas de ruteo por tipo de correo | MUST |
| CA-3 | Configuración de auto-respuesta | SHOULD |

**Handoff Backend (T-S4-03):**
```
Policy Engine:
- POST /api/v1/mailbox-policies → { mailboxId, rules: [...] }
- Cada regla: { type: "SPAM_FILTER"|"DUPLICATE"|"BLACKLIST"|"ROUTE", config: {} }
- El MailboxPollerJob aplica políticas ANTES de publicar a RabbitMQ
- Si correo filtrado: marcar como leído en MS Graph + log en audit_trail
```

---

#### US-012/013/014: Pipeline de Clasificación IA

| US | Título | Prioridad |
|----|--------|-----------|
| US-012 | Clasificación de intención (LLM) | MUST |
| US-013 | Enriquecimiento con datos CRM | SHOULD |
| US-014 | Extracción de entidades (NER) | SHOULD |

**Handoff Backend (T-S4-04):**
```
AI Classification Pipeline:
- RabbitMQ consumer: IntakeEventProcessor
  1. Recibe evento CORREO_RECIBIDO de exchange ibpms.intake
  2. Llama a LLM (Azure OpenAI / Gemini) con prompt de clasificación
     → Resultado: { intention, confidence, entities, suggestedProcess }
  3. Enriquece con CRM (US-013): GET /api/v1/crm/contacts?email={sender}
  4. Publica evento CORREO_CLASIFICADO al exchange ibpms.triage

Modelo de integración LLM:
- Strategy pattern: LlmClassifier interface (AzureOpenAI, Gemini, Fallback)
- FinOps: log token usage (US-044 depende)
- PII detection: aplicar regex + LLM para detectar datos sensibles
- Timeout: 30s max por clasificación, retry 2x, luego DLQ
```

---

#### US-040: Embudo de Intake (Triaje Humano-IA)

| CA (estimado) | Título | Prioridad |
|---------------|--------|-----------|
| CA-1 | Pantalla 2C: vista de correos pre-clasificados | MUST |
| CA-2 | Líder SAC confirma/rechaza propuesta IA | MUST |
| CA-3 | Al confirmar: crear caso → instanciar proceso BPMN | MUST |
| CA-4 | Métricas de accuracy del triaje IA | SHOULD |

**Handoff Frontend (T-S4-05):**
```
Pantalla 2C: Embudo de Intake
- Lista de correos clasificados (DataGrid):
  Columnas: Remitente, Asunto, Intención IA (badge), Confianza (%), Acciones
- Al seleccionar: panel de detalle con cuerpo del correo + sugerencia IA
- Botón "Confirmar → Crear Caso" → POST /api/v1/cases (instancia proceso BPMN)
- Botón "Rechazar → Marcar Spam/Irrelevante" → actualiza accuracy metrics
- Filtros: por buzón, por intención, por confianza (>80%, 50-80%, <50%)
```

---

#### US-022/023: Gestión de Casos y Correlación de Hilos

**Handoff Backend (T-S4-06):**
```
Case Service:
- POST /api/v1/cases → { correoId, processKey, variables }
  1. Crea entidad Case en BD
  2. RuntimeService.startProcessInstanceByKey(processKey, {caseId, variables})
  3. Retorna caseId + processInstanceId

Thread Correlation (US-023):
- Cuando llega correo nuevo, buscar In-Reply-To / References headers
- Si correlaciona con caso existente → agregar como mensaje al caso
- Si no correlaciona → crear nuevo caso
```

### Orden de Ejecución TRACK A (Sprint 4)

```
Día 1:
  Backend(US-037 Buzones + MS Graph: CA-1..4) → ★Lead audita
  Frontend(US-037 Admin Buzones UI) → ★Lead audita

Día 2:
  Backend(US-016 Políticas: CA-1..3) → ★Lead audita
  Backend(US-012/013/014 Pipeline IA: clasificación + NER) → ★Lead audita

Día 3:
  Frontend(US-040 Embudo Intake P2C) → ★Lead audita
  Backend(US-022/023 Cases + Thread Correlation) → ★Lead audita
  ★Lead: Integración J-01 pasos 1-6 (Correo → IA → Triaje → Caso)
```

### TRACK B — VALIDATE + HARDEN

| Actividad | Detalle |
|-----------|---------|
| QA: E2E J-01 pasos 1-6 | `e2e/j01/email-intake-pipeline.spec.ts` |
| QA: Regresión Suites J-02, J-03, J-04 | Ejecutar suites completas, ≤0 regresiones |
| QA: Validar US-004 (Webhook S3) E2E | Webhook trigger → Camunda instance starts |
| Performance: LLM latency smoke | Clasificación < 5s en P95 |
| Security: PII masking en logs de IA | Verificar que datos sensibles no aparecen en logs |

### Gate Sprint 4

```
GATE S4 = Gate Técnico (≥6 US construidas + J-01 pasos 1-6 E2E + regresión 0)
         ∧ Gate Funcional (UAT J-01 parcial: correo llega → se clasifica → caso creado)
         ∧ Gate Final (Demo: Jefe envía correo → aparece clasificado en P2C)
```

---

## Sprint 5: J-01 Integración Final + J-05 Portal B2C + J-09 DevPortal

### Pre-Sprint (★ Lead)

- [ ] Cerrar J-01 integrando pasos 7-12 (ya construidos en S1-S2: Workdesk → Form → CQRS)
- [ ] Emitir ADR: "Portal B2C Architecture" (SPA separada vs micro-frontend)
- [ ] Descomponer US-049 (Notificaciones), US-050 (Onboarding), US-026 (Portal consulta)

### TRACK A — BUILD

#### J-01 Cierre: Pasos 7-12 (Integración)

> Los pasos 7-12 de J-01 son idénticos a J-04 (Workdesk → Form → CQRS). Solo falta **vincular** el caso creado en S4 con la tarea que aparece en el Workdesk.

**Handoff Backend (T-S5-01):**
```
Integración J-01:
- Verificar que al crear caso (US-022, paso 6), Camunda genera user task
- La user task aparece en Workdesk (US-001) con referencia al caseId
- El formulario (US-029/017) incluye datos del caso + correo original
- Al completar: notificación al cliente (US-049)
```

#### US-049: Motor de Notificaciones

| CA (estimado) | Título | Prioridad |
|---------------|--------|-----------|
| CA-1 | Enviar notificación por email (SendGrid/SMTP) | MUST |
| CA-2 | Enviar notificación in-app (WebSocket toast) | MUST |
| CA-3 | Templates de notificación (Handlebars/Thymeleaf) | MUST |
| CA-4 | Preferencias de notificación por usuario | SHOULD |
| CA-5 | Cola de notificaciones con retry (RabbitMQ) | MUST |

**Handoff Backend (T-S5-02):**
```
Notification Service:
- RabbitMQ consumer: NotificationEventProcessor
  exchange: ibpms.notifications, routing: notification.*
- Strategies: EmailNotificationStrategy, InAppNotificationStrategy
- Templates: Thymeleaf en /resources/templates/notifications/
- POST /api/v1/notifications/preferences → { userId, channels: ["EMAIL","IN_APP"] }
- WebSocket: /topic/notifications/{userId} para in-app
```

#### US-050: Onboarding de Clientes Externos (Magic Link)

**Handoff Backend (T-S5-03):**
```
Magic Link Service:
- POST /api/v1/invitations → { email, caseId, expiresIn: "72h" }
  1. Genera token JWT con claim { email, caseId, exp }
  2. Envía email con link: {portalUrl}/activate?token={jwt}
  3. Almacena invitación en BD con estado PENDING
- POST /api/v1/invitations/activate → { token, password }
  1. Valida JWT
  2. Crea cuenta de cliente externo
  3. Marca invitación como ACTIVATED
```

#### US-026: Portal B2C (Consulta de Estado)

**Handoff Frontend (T-S5-04):**
```
Portal B2C (SPA separada o micro-frontend):
- Login: email + password (cuenta creada por Magic Link)
- Dashboard: lista de casos del cliente
- Detalle de caso: timeline de eventos + documentos + estado actual
- Descarga de documentos adjuntos (pre-signed URL)
- Read-only: el cliente NO puede modificar datos, solo consultar
```

#### US-042/033/046: DevPortal y API Keys (J-09)

**Handoff Backend (T-S5-05):**
```
DevPortal API:
- POST /api/v1/service-accounts → { name, permissions: [...] }
  Genera API Key (SHA-256 hash almacenado, plaintext mostrado UNA sola vez)
- API Gateway: interceptor que valida X-API-Key → resolve ServiceAccount
- Rate limiting: Bucket4j por ServiceAccount (configurable)
- GET /api/v1/api-docs → OpenAPI 3.0 spec para consumidores externos
```

### Orden de Ejecución TRACK A (Sprint 5)

```
Día 1:
  Backend(US-049 Notificaciones: CA-1..5) → ★Lead audita
  ★Lead: Integración J-01 pasos 7-12 (vincular caso → Workdesk)

Día 2:
  Backend(US-050 Magic Link + US-026 Portal API) → ★Lead audita
  Frontend(US-026 Portal B2C SPA) → ★Lead audita

Día 3:
  Backend(US-042/033/046 DevPortal + API Keys) → ★Lead audita
  Frontend(DevPortal Dashboard) → ★Lead audita
  ★Lead: Integración J-05 (Onboarding → Portal) + J-09 (DevPortal)
```

### Gate Sprint 5

```
GATE S5 = Gate Técnico (J-01 completo E2E + J-05 funcional + J-09 funcional)
         ∧ Gate Funcional (UAT J-01 12 pasos + UAT J-05 + UAT J-09)
         ∧ Gate Final (Demo: correo → clasificación → tarea → formulario → notificación → portal)
```

---

## Sprint 6: J-10 Agentes IA + J-11 Gantt + J-12 DMN

### TRACK A — BUILD

#### US-052/053: Configuración y Command Center de Agentes IA (J-10)

**Handoff Backend (T-S6-01):**
```
Agent Management:
- CRUD de configuraciones de agente: model, temperature, maxTokens, systemPrompt
- POST /api/v1/agents/{id}/execute → ejecutar tarea cognitiva dentro de proceso BPMN
- FinOps dashboard: GET /api/v1/finops/summary → tokens usados por agente/periodo

Command Center Frontend:
- Panel de agentes configurados con métricas de uso
- Gráfica de consumo de tokens (ApexCharts)
- Alertas de presupuesto excedido
```

#### US-032/044: Tarea Cognitiva BPMN + FinOps (J-10)

**Handoff Backend (T-S6-02):**
```
Cognitive BPMN Task:
- ServiceTask delegate en Camunda que invoca pipeline LLM
- Variables de proceso → prompt → LLM → resultado → variables de salida
- FinOps: INSERT en token_usage_log (agent_id, tokens_in, tokens_out, cost_usd)
- Budget alert: si consumo > umbral → notificación al Admin IA (US-049)
```

#### US-031/006: Proyecto Tradicional Gantt + WBS (J-11)

**Handoff Backend (T-S6-03):**
```
Gantt Module:
- POST /api/v1/projects → { type: "TRADITIONAL", ... }
- POST /api/v1/projects/{id}/wbs → { tasks: [{ name, startDate, endDate, dependencies }] }
- GET /api/v1/projects/{id}/gantt → { tasks with computed criticalPath }
- Algoritmo CPM (Critical Path Method) para calcular ruta crítica
```

**Handoff Frontend (T-S6-04):**
```
Gantt Viewer:
- Componente GanttChart.vue (librería: dhtmlxGantt o frappe-gantt)
- Visualización de barras con dependencias (líneas de conexión)
- Resaltado de ruta crítica en rojo
- Drag & drop para ajustar fechas
```

#### US-007/027: Motor DMN Avanzado + Generación IA de Reglas (J-12)

**Handoff Backend (T-S6-05):**
```
DMN Advanced:
- Editor visual de tablas DMN (expandir DMN viewer de S2 → editor)
- US-027: POST /api/v1/dmn/generate → { description: "regla en lenguaje natural" }
  → LLM genera tabla DMN XML → retorna para edición humana
- Integración con BPMN modeler: vincular BusinessRuleTask → decisión DMN
```

### Gate Sprint 6

```
GATE S6 = Gate Técnico (J-10 + J-11 + J-12 funcionales + ≥28 specs E2E)
         ∧ Gate Funcional (UAT 10/13 Journeys cubiertos)
         ∧ Gate Final (Demo: Admin IA configura agente → ejecuta tarea cognitiva en BPMN)
```

---

## Sprint 7+: Cierre de V1

### US Restantes (Estimadas)

| Sprint | US Target | Journey | Notas |
|:------:|-----------|---------|-------|
| S7 | US-010 (PDF), US-035 (Firma/SharePoint) | J-13 | Generación de documentos |
| S7 | US-011 (Docketing SAC) | J-01 ext | Bandeja avanzada del Líder SAC |
| S7 | US-025 (Cards Dinámicas) | J-04 ext | Visual enhancement del Workdesk |
| S7 | US-041 (Vista 360 Cliente) | J-05 ext | Pantalla administrativa |
| S7 | US-045 (Restricciones Dominio) | Transversal | Configuración de límites |
| S7 | US-021 (Mapeo Variables CRM) | J-01 precond | Configuración CRM |
| S8 | Deuda técnica acumulada | — | Remediación batch |
| S8 | NFR hardening | — | Performance, seguridad, observabilidad |
| S8 | E2E regression suite completa | — | 13/13 Journeys E2E verdes |

### Hitos de Cierre V1

| Hito | Sprint | Criterio |
|------|:------:|----------|
| **MVP funcional** | S3 | J-02, J-03, J-04 verdes + ≥18 E2E specs |
| **Core completo** | S5 | J-01 (el más largo) completo E2E + Portal B2C |
| **Feature complete** | S7 | 50/53 US implementadas + 13/13 Journeys cubiertos |
| **Release candidate** | S8 | ≥40 E2E specs + 0 P0 bugs + Performance NFRs met |
| **V1 GA** | S8+ | Gate final del Jefe de Equipo + stakeholder sign-off |

---

## Template de Sprint Reutilizable (para S4+)

### Estructura del Día

```
┌────────────────────────────────────────────────────────────┐
│ DÍA 0 (Pre-Sprint): ★ Lead + PO + Jefe                    │
│   → Lead descompone US + genera handoffs                    │
│   → PO refina CAs + escribe UAT                            │
│   → Jefe confirma prioridades                               │
├────────────────────────────────────────────────────────────┤
│ DÍA 1-3 (Ejecución Dual Track):                            │
│   TRACK A:                                                   │
│     Backend(US-A) → ★Lead audita                            │
│     Frontend(US-A) → ★Lead audita                           │
│     Backend(US-B) → ★Lead audita                            │
│     Frontend(US-B) → ★Lead audita                           │
│   TRACK B:                                                   │
│     QA: E2E tests del sprint + regresión de suites previas  │
│     QA: Validación funcional de CAs                          │
├────────────────────────────────────────────────────────────┤
│ DÍA 4 (Gate):                                               │
│   ★ Lead: Gate Técnico (merge + E2E suite + coverage)       │
│   📋 PO: Gate Funcional (CAs vs spec + UAT cubiertos)      │
│   👤 Jefe: Gate Final (demo + UAT manual)                   │
└────────────────────────────────────────────────────────────┘
```

### Gate Checklist Genérico

| # | Criterio | Responsable | Meta |
|---|----------|:-----------:|------|
| 1 | CAs construidos ≥ target | ★ Lead | 100% de CAs del sprint |
| 2 | Tests E2E del sprint verdes | QA Agent | ≥ target specs |
| 3 | Regresión suites previas | QA Agent | 0 regresiones |
| 4 | Coverage matrix actualizada | ★ Lead | Todos los CAs registrados |
| 5 | ADRs documentados (si aplica) | ★ Lead | Decisiones arquitectónicas |
| 6 | UAT scenarios cubiertos | 📋 PO | ≥80% escenarios passing |
| 7 | CAs vs spec sin gaps | 📋 PO | 0 gaps funcionales |
| 8 | Demo del Journey | 👤 Jefe | Flujo E2E sin errores bloqueantes |
| 9 | UAT manual positiva | 👤 Jefe | Aprobación explícita |

---

## Métricas de Velocidad Esperadas

| Sprint | US Target | Journeys Target | E2E Specs Acumulados | US Completadas Acumuladas |
|:------:|:---------:|:----------------:|:--------------------:|:-------------------------:|
| S0 | 0 | — | 1 (smoke) | 10 (pre-existentes) |
| S1 | 3 (US-001r, US-002, UI-029) | J-04 | ≥6 | ~13 |
| S2 | 2 (US-017, US-007) | J-02 | ≥14 | ~15 |
| S3 | 3 (US-004, US-008, US-030) | J-03, J-06, J-07 | ≥22 | ~18 |
| **S4** | **7 (US-037..US-023)** | **J-01 parcial, J-08** | **≥28** | **~25** |
| **S5** | **5 (US-049..US-046)** | **J-01 completo, J-05, J-09** | **≥35** | **~30** |
| **S6** | **7 (US-052..US-027)** | **J-10, J-11, J-12** | **≥40** | **~37** |
| **S7** | **6 (US-010..US-021)** | **J-13 + extensiones** | **≥45** | **~43** |
| **S8** | **Deuda + hardening** | **Regresión 13/13** | **≥50** | **~50 (V1 RC)** |

---

## Historial de Cambios

| Fecha | Cambio | Autor |
|-------|--------|-------|
| 2026-04-10 | Creación del Sprint 4+ plan con desglose S4-S8 | Arquitecto Lead |
