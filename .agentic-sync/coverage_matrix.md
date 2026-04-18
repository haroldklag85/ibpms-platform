# 📊 Matriz de Cobertura de Implementación (iBPMS V1)

> **Última actualización:** 2026-04-18 (Auditoría Integral — `/auditoriaIntegralUSDesarrollo.md`) | **Responsable:** Arquitecto Líder
> **Fuente de Verdad:** Checklist validado manualmente por el PO/Arquitecto Líder
> **Leyenda:** ✅ Implementado | ⏳ En progreso | ❌ Pendiente | 🚫 Excluido (V2+) | 🔄 Remediación pendiente | ⚠️ Falso Positivo Corregido

## Instrucciones de Uso

1. **¿Quién actualiza esta matriz?** Cada agente de desarrollo (Backend/Frontend) DEBE marcar sus CAs como ✅ después de hacer `git commit` y `git push` (ver `agent_git_governance_policy.md` §2).
2. **¿Quién la audita?** El Arquitecto Líder ejecuta `/reconciliacionCoberturaCa.md` al cierre de cada Sprint para cruzar esta matriz contra `git log` y detectar falsos positivos.
3. **¿Cómo se lee?** Cada US tiene su tabla. Las columnas Back/Front/QA indican si esa capa fue implementada. La columna Handoff referencia el archivo de delegación.

> [!CAUTION]
> **Corrección 2026-04-10:** Se detectaron 4 Falsos Positivos en US-001 (CA-4, CA-5, CA-6, CA-8) que estaban marcados como ✅ pero NO están confirmados por el PO. Se corrigen a ❌ Pendiente. Esto valida que la sincronización automática por agentes es insuficiente y requiere auditoría manual periódica.

---

## Resumen Ejecutivo Global

| Métrica | Valor |
|---------|-------|
| **Total US en V1** | 56 |
| **US Completadas** | 11 (US-000, US-001, US-003, US-005, US-028, US-034, US-036, US-038, US-039, US-043, US-048) |
| **US En Construcción** | 5 (US-002 ~9%, US-004 ~71%, US-007 ~48%, US-029 ~55%, US-030 ~85%) |
| **US Scaffolding (Fencing activo)** | 5 (US-008 ~10%, US-011, US-021, US-035, US-045) |
| **US Refactoring** | 1 (US-017 — ADR-001 compliance) |
| **US Pendientes** | 34 |
| **CAs Implementados (estimado)** | ~248+ |
| **CAs Validados QA** | ~38 (~15%) |
| **Seguridad Crítica** | 🔴 IDOR activo en US-007 (tenantId hardcodeado) + Persistencia BD ausente en US-002 |
| **Principal Brecha** | 🔴 QA < 15% global. US-002 y US-008 declaradas Operativas sin base real. |

> [!CAUTION]
> **Corrección 2026-04-18 — Auditoría Integral Sección 1.2:** Se detectaron 2 Falsos Positivos críticos en `future_backlog_v3.md`:
> - **US-002** declarada ✅ Operativa — real: ~9% (BD no persiste, assignee hardcodeado, sin bulk-claim, sin force-unclaim)
> - **US-008** declarada ✅ Operativa — real: ~10% Scaffolding (KanbanView.vue usa mock data hardcodeado, sin state machine)
> Adicionalmente: US-004, US-007, US-029 declaradas Operativas pero son Beta parciales (48–71%).
> La única US declarada Operativa con alta fidelidad real es US-030 (~85%).

---

## US-000: Resiliencia Integrada y Enmascaramiento PII Visual
**Épica:** 0 — Gobernanza Global | **Estado:** ✅ COMPLETADA (Transversal)

| CA | Título (corto) | Back | Front | QA | Sprint | Notas |
|----|----------------|------|-------|----|--------|-------|
| CA-1 | Degradación Grácil HTTP 500/503 | ✅ | ✅ | ❌ | S-1 | Transversal — interceptor global |
| CA-2 | Triage Semántico Validaciones 400/422 | ✅ | ✅ | ❌ | S-1 | Array DTO {field, issue, translatedMessage} |
| CA-3 | Concurrencia Optimista 409 | ✅ | ✅ | ❌ | S-1 | Control de versión en BD |
| CA-4 | Enmascaramiento PII Redaction | ✅ | ✅ | ❌ | S-1 | Interceptor regex/LLM |

### Resumen US-000
- **Total CAs:** 4 | **✅ Back+Front:** 4/4 (100%) | **QA:** ❌ 0% Pendiente
- **Nota:** US transversal. Todos los CAs aplican como reglas globales a todas las demás US.

---

## US-001: Bandeja de Entrada Unificada (Hybrid Workdesk)
**Épica:** 1 — Orquestación | **Estado:** 🔨 EN CONSTRUCCIÓN (26/30 CAs activos — 86%)

| CA | Título (corto) | Back | Front | QA | Sprint | Handoff | Notas |
|----|----------------|------|-------|----|--------|---------|-------|
| CA-1 | Vista 360 Grid paginada | ✅ | ✅ | ✅ | 77-DEV | handoff_77DEV_US001 | Auditado en 77-DEV |
| CA-2 | Búsqueda Híbrida Reactiva | 🚫 | 🚫 | 🚫 | — | Anulado por CA-19 | Reemplazado por búsqueda 100% Server-Side |
| CA-3 | Data Grid tabular 5 cols | ✅ | ✅ | ✅ | 77-DEV | handoff_77DEV_US001 | Auditado en 77-DEV |
| CA-4 | Toggle Delegación Mis Tareas/Equipo | ✅ | ✅ | ✅ | 81-DEV | handoff_81DEV_US001_CA04_CA15 | Auditado en 81-DEV |
| CA-5 | SLA Ticking Engine Vivo | ✅ | ✅ | ✅ | 80-DEV | handoff_80DEV_US001_CA05_CA11_CA24_CA25_CA31 | Auditado en 80-DEV |
| CA-6 | Ghost Deletion STOMP WebSocket | ✅ | ✅ | ✅ | 79-DEV | handoff_79DEV_US001_CA06_CA13_CA26_CA27 | Auditado en 79-DEV |
| CA-7 | Tolerancia Fallas CQRS | ✅ | ✅ | ✅ | 77-DEV | handoff_77DEV_US001 | Auditado en 77-DEV |
| CA-8 | Anti-Cherry Picking Feature Flag | ✅ | ✅ | ✅ | 82-DEV | handoff_82DEV_US001_CA08_CA16_CA21_CA28 | Auditado en 82-DEV |
| CA-9 | Paginación Máxima Visual | ✅ | ✅ | ✅ | 76-DEV | handoff_76DEV_us001 | Auditado en 76-DEV |
| CA-10 | Paginación Server-Side y pg_trgm | ✅ | ✅ | ✅ | 76-DEV | handoff_76DEV_us001 | Auditado en 76-DEV |
| CA-11 | Heartbeat Store rAF | ✅ | ✅ | ✅ | 80-DEV | handoff_80DEV_US001_CA05_CA11_CA24_CA25_CA31 | Auditado en 80-DEV |
| CA-12 | Ergonomía KeepAlive Empty State | N/A | ✅ | ✅ | 77-DEV | handoff_77DEV_US001 | Frontend only |
| CA-13 | Minificación WebSocket Throttling | ✅ | ✅ | ✅ | 79-DEV | handoff_79DEV_US001_CA06_CA13_CA26_CA27 | Auditado en 79-DEV |
| CA-14 | Sanitización DTO y Aislamiento RLS | ✅ | ✅ | ✅ | 76-DEV | handoff_76DEV_us001 | Auditado en 76-DEV |
| CA-15 | Delegación Segura Anti-IDOR | ✅ | ✅ | ✅ | 81-DEV | handoff_81DEV_US001_CA04_CA15 | Auditado en 81-DEV |
| CA-16 | Skill-Based Routing | ✅ | ✅ | ✅ | 82-DEV | handoff_82DEV_US001_CA08_CA16_CA21_CA28 | Auditado en 82-DEV |
| CA-17 | Ordenamiento SLA y Priority Fallback | ✅ | ✅ | ✅ | 76-DEV | handoff_76DEV_us001 | Auditado en 76-DEV |
| CA-18 | Degradación Multi-Motor | ✅ | ✅ | ✅ | 77-DEV | handoff_77DEV_US001 | Auditado en 77-DEV |
| CA-19 | Búsqueda Exclusiva Server-Side | ✅ | ✅ | ✅ | 76-DEV | handoff_76DEV_us001 | Auditado en 76-DEV |
| CA-20 | Estandarización Contrato API | ✅ | ✅ | ✅ | 76-DEV | handoff_76DEV_us001 | Auditado en 76-DEV |
| CA-21 | Skill-Based Skipeo Justificado | ✅ | ✅ | ✅ | 82-DEV | handoff_82DEV_US001_CA08_CA16_CA21_CA28 | Auditado en 82-DEV |
| CA-22 | Filtros Facetados por Status | ✅ | ✅ | ✅ | 78-DEV | handoff_78DEV_US001 | Auditado en 78-DEV |
| CA-23 | Fórmula Avance Determinista | ✅ | ✅ | ✅ | 77-DEV | handoff_77DEV_US001 | Auditado en 77-DEV |
| CA-24 | Umbrales Semáforo SLA Configurables | ✅ | ✅ | ✅ | 80-DEV | handoff_80DEV_US001_CA05_CA11_CA24_CA25_CA31 | Auditado en 80-DEV |
| CA-25 | Recálculo Semáforos Tab Inactiva | ✅ | ✅ | ✅ | 80-DEV | handoff_80DEV_US001_CA05_CA11_CA24_CA25_CA31 | Auditado en 80-DEV |
| CA-26 | Relleno Automático Post-WebSocket | ✅ | ✅ | ✅ | 79-DEV | handoff_79DEV_US001_CA06_CA13_CA26_CA27 | Auditado en 79-DEV |
| CA-27 | Vocabulario Completo WebSocket | ✅ | ✅ | ✅ | 79-DEV | handoff_79DEV_US001_CA06_CA13_CA26_CA27 | Auditado en 79-DEV |
| CA-28 | Prevención Race Condition Atender | ✅ | ✅ | ✅ | 82-DEV | handoff_82DEV_US001_CA08_CA16_CA21_CA28 | Auditado en 82-DEV |
| CA-29 | Contadores en Filtros por Tenant | ✅ | ✅ | ✅ | 78-DEV | handoff_78DEV_US001 | Auditado en 78-DEV |
| CA-30 | Rate Limiting API 429 | ✅ | ✅ | ✅ | 78-DEV | handoff_78DEV_US001 | Auditado en 78-DEV |
| CA-31 | Auto-Refresco Pasivo Inactividad | ✅ | ✅ | ✅ | 80-DEV | handoff_80DEV_US001_CA05_CA11_CA24_CA25_CA31 | Auditado en 80-DEV |

### Resumen US-001
- **Total CAs:** 31 (1 anulado = 30 activos)
- **✅ Construidos:** 30/30 (100%)
- **❌ Pendientes:** 0/30 (0%)
- **⚠️ Falsos Positivos Corregidos:** 1 (CA-8) — CA-4 fue implementado en 81-DEV
- **QA validado:** 26/26 construidos (100% de lo construido)
- **Última auditoría:** 81-DEV (2026-04-13) — CA-04, CA-15 ✅

#### CAs Pendientes Agrupados por Dominio Funcional
| Grupo | CAs | Descripción |
|-------|-----|-------------|
| **Delegación / RBAC** | CA-4, CA-15 | Toggle delegación, anti-IDOR |
| **Routing / Anti-Abuse** | CA-8, CA-16, CA-21, CA-28 | Anti-cherry-picking, skill-based routing, skipeo, race condition |

> ✅ **Grupo SLA/Semáforos CERRADO en 80-DEV:** CA-05, CA-11, CA-24, CA-25, CA-31 — Auditados y certificados.

---

## US-002: Reclamar una Tarea de Grupo (Claim Task)
**Épica:** A — Motor Core | **Estado:** 🔨 EN CONSTRUCCIÓN (~9%) | **Auditado:** 2026-04-18
**Archivos verificados:** `TaskClaimController.java` · `AutoClaimService.java`

> [!WARNING]
> **FALSO POSITIVO DETECTADO:** `future_backlog_v3.md` declaraba esta US como ✅ Operativa.
> La persistencia en BD está **explícitamente comentada** (`taskRepository.assignTask()` — línea 41).
> `assignee` y `tenantId` hardcodeados (`"e2e_user"`, `"tenant_1"`). Todo reclamo se pierde al reiniciar.

| CA | Título (corto) | Back | Front | QA | Notas |
|----|----------------|------|-------|----|-------|
| CA-1 | Reclamo Simultáneo (anti race-condition) | ⚠️ | ✅ | ✅ | Redis SETNX funciona; test E2E/Vitest cubierto |
| CA-2 | Reclamo Masivo en Lote (bulk-claim) | ❌ | ❌ | ❌ | Endpoint `/tasks/bulk-claim` no existe |
| CA-4 | Liberación con Mensaje Interno | ⚠️ | ❌ | ❌ | `unclaim` borra Redis + WS; sin campo mensaje; sin BD |
| CA-5 | Modo Sólo Lectura (pre-claim) | ❌ | ❌ | ❌ | Sin endpoint dedicado de read-only |
| CA-6 | Ghost Job Timeout (Auto-Unclaim Cron) | ⚠️ | ❌ | ❌ | `AutoClaimService` existe; umbral tenant-configurable no verificado |
| CA-7 | Amnesia Transaccional al Liberar | ❌ | ❌ | ❌ | Sin validación de payload parcial; sin modal advertencia |
| CA-8 | Despojo Forzoso Supervisor | ❌ | ❌ | ❌ | Endpoint `force-unclaim` no existe |
| CA-9 | Trazabilidad Forense Pop-Up | ❌ | ❌ | ❌ | Endpoint `audit-trail` no existe; sin tabla de auditoría de reclamos |
| CA-10 | Resiliencia Offline | ❌ | ❌ | ❌ | Sin Optimistic UI + rollback |
| CA-11 | Bloqueo Atómico BD (Camunda/SKIP LOCKED) | ❌ | N/A | ❌ | Solo Redis SETNX; sin `SELECT FOR UPDATE SKIP LOCKED`; BD comentada |
| CA-12 | Evento WebSocket Post-Commit | ✅ | ✅ | ❌ | `notifyTaskClaimed()` / `notifyTaskUnclaimed()` emitidos |
| CA-14 | Contrato API Estandarizado OpenAPI | ❌ | N/A | ❌ | Sin OpenAPI annotations; endpoints faltantes |
| CA-22 | Separación Visual Bandeja/Cola Equipo | N/A | ❌ | ❌ | Sin tabs "Mi Bandeja" / "Cola Equipo" |

### Resumen US-002
- **CAs Totales:** 23 | **CAs Back Implementados:** ~2 | **CAs Front Implementados:** ~2 | **% Real:** ~9%
- **QA:** 1 CA Certificado Parcialmente.
- **Bloqueadores Críticos de Seguridad:**
  - `assignee` hardcodeado `"e2e_user"` — cualquier usuario autenticado actúa como el mismo usuario
  - Persistencia BD comentada — estado se pierde en cada restart
- **Handoff requerido:** Sprint 5 Iteración 1 (`handoff_backend_sprint5_iteracion1.md` abierto en IDE)

---

## US-004: Iniciar un Proceso mediante Webhook (Plugin O365 Listener)
**Épica:** A — Motor Core | **Estado:** 🔨 EN CONSTRUCCIÓN (~71%) | **Auditado:** 2026-04-18
**Archivos verificados:** `WebhookIntakeController.java` · `WebhookIntakeService.java` · `OrphanPayloadRepositoryJpa.java` · `ClamAvScannerAdapter.java` · `TriagePurgeScheduler.java` · `EmailWebhookController.java`

| CA | Título (corto) | Back | Front | QA | Notas |
|----|----------------|------|-------|----|-------|
| CA-1 | Idempotencia (duplicados silenciosos) | ✅ | N/A | ❌ | `WebhookTransaction` con UNIQUE en `message_id` |
| CA-2 | Bloqueo auto-responders | ✅ | N/A | ❌ | Regex: no-reply, mailer-daemon, postmaster, bounce |
| CA-3 | Payloads basura → tabla OrphanPayload | ✅ | N/A | ❌ | SHA-256 hash + tipo de error persistido |
| CA-4 | Whitelist dominios autorizados | ✅ | N/A | ❌ | `existsByDomainAndTenantIdAndIsActiveTrue()` por tenant |
| CA-5 | Alerta admin si Camunda falla | ❌ | N/A | ❌ | No evidenciado en código auditado |
| CA-6 | Resiliencia RabbitMQ (Camunda offline) | ❌ | N/A | ❌ | Cola definida en config; **sin `@RabbitListener`** — webhooks se pierden si Camunda cae |
| CA-7 | Límite de peso configurable (HTTP 413) | ✅ | N/A | ❌ | `maxSizeBytes` default 10MB |
| CA-8/9 | Pre-Triaje humano en Camunda | ✅ | ❌ | ❌ | `TriageTask` entity + proceso Camunda `Process_PreTriaje_Intake`; UI Pantalla 16 pendiente |
| CA-10 | HMAC signature validation | ⚠️ | ❌ | ❌ | HmacSHA256 + tiempo constante ✅; sin switch Bearer Token legacy |
| CA-11 | ClamAV Anti-Malware (fail-secure) | ✅ | N/A | ❌ | REST adapter 5s timeout; fallo → HTTP 503 + DLQ |
| CA-12 | CRUD Admin Whitelist dominios | ❌ | ❌ | ❌ | `AllowedDomainAdminController` stub `NOT_IMPLEMENTED` (fenced como US-045) |
| CA-13 | Purga automática 30 días | ✅ | N/A | ❌ | `deleteByCreatedAtBefore` scheduler diario 2AM |
| CA-17 | Sub-segundo ACK al emisor | ✅ | N/A | ❌ | 202 Accepted sincrónico |

### Resumen US-004
- **CAs Totales:** 17 | **CAs Back Implementados:** ~9 | **% Real:** ~71%
- **QA:** ❌ 0%
- **Riesgo Alto:** `EmailWebhookController` legacy activo — **bypasea todo el pipeline** (sin HMAC, sin ClamAV, sin whitelist)
- **Bloqueador CA-6:** Sin consumer RabbitMQ activo — sistema falla sincrónico cuando Camunda está offline

---

## US-007: Generador Cognitivo de DMN (NLP a Tablas de Decisión)
**Épica:** B — Formularios/BPMN | **Estado:** 🔨 EN CONSTRUCCIÓN (~48%) | **Auditado:** 2026-04-18
**Archivos verificados:** `DmnGovernanceController.java` · `DmnIntelligence.vue`

> [!WARNING]
> **IDOR ACTIVO:** `tenantId` hardcodeado en `DmnGovernanceController` — un tenant autenticado puede leer/modificar DMNs de otro tenant. Requiere corrección inmediata antes de cualquier despliegue.

| CA | Título (corto) | Back | Front | QA | Notas |
|----|----------------|------|-------|----|-------|
| CA-1 | Streaming SSE generación IA | ✅ | ✅ | ✅ | Endpoint SSE + reconexión automática en `DmnIntelligence.vue`, test 504 cubierto |
| CA-2 | Caché criptográfica (anti DoW) | ⚠️ | N/A | ❌ | Caché por hash ✅; multi-tenant roto por tenantId hardcodeado |
| CA-3 | GC y Compresión XML borradores | ❌ | N/A | ❌ | No evidenciado `DmnDraftCleanupScheduler` |
| CA-4 | Sandboxing Anti-RCE & XSS | ⚠️ | ⚠️ | ❌ | Validación XML estructural ✅; XSS en render DOM no verificado |
| CA-5 | Seudonimización PII del Prompt | ❌ | N/A | ❌ | No evidenciado pre-procesamiento PII antes del LLM |
| CA-6 | Inmutabilidad DMN & RBAC (anti-IDOR) | ❌ | N/A | ❌ | **IDOR crítico** — tenantId hardcodeado en controller |
| CA-7 | Hit Policy FIRST + Catch-All | ✅ | ✅ | ❌ | Validación catch-all implementada |
| CA-8 | Variables planas, prohibición Date-Math | ⚠️ | N/A | ❌ | Validación de tipos básica; date-math no verificado |
| CA-9 | Límites cognitivos + validación inversa | ❌ | N/A | ❌ | No evidenciado |
| CA-10 | Virtual Scrolling grilla alta densidad | N/A | ✅ | ❌ | Implementado en `DmnIntelligence.vue` |
| CA-11 | XAI Explicabilidad + Simulador | N/A | ✅ | ❌ | Panel XAI y simulador de decisiones en `DmnIntelligence.vue` |
| CA-12 | Contención de Pánico + Trazabilidad Chat | N/A | ✅ | ❌ | Panic modal implementado |
| CA-13 a CA-18 | [REMEDIACIÓN] Persistencia dual borradores, endpoint simulador, invalidación caché Redis, catálogo DMN, contrato API | ❌ | ❌ | ❌ | Sin verificar — CAs de remediación pendientes de auditoría |
| CA-19 a CA-25 | [REFINAMIENTO] Resiliencia SSE (429, 422, 403), normalización prompt | ❌ | ✅ | ✅ | Tests de resiliencia y errores semánticos cubiertos |

### Resumen US-007
- **CAs Totales:** 25 | **CAs verificados:** 12 | **CAs cumplidos:** ~7 | **% Real:** ~48%
- **QA:** ✅ CAs de resiliencia validados (CA-1, 19-25 parcialmente).
- **Bloqueador Crítico:** IDOR activo por tenantId hardcodeado — **no apto para producción**
- **Pendiente auditar:** CAs 13-25 (13 CAs de remediación y refinamiento)

---

## US-008: Mover Tarjeta en Tablero Kanban (Cambio de Estado)
**Épica:** A — Motor Core | **Estado:** 🔨 Scaffolding (~10%) | **Auditado:** 2026-04-18
**Archivos verificados:** `KanbanBoardService.java` · `KanbanView.vue`

> [!WARNING]
> **FALSO POSITIVO DETECTADO:** `future_backlog_v3.md` declaraba esta US como ✅ Operativa.
> `KanbanView.vue` usa 4 tareas hardcodeadas con `loadBoard()` simulado via `setTimeout`. No hay ninguna llamada real a API. `KanbanBoardService` solo gestiona delegación, no la máquina de estados del tablero.
> Esta US debería clasificarse en la sección 1.3 de Deuda Técnica Controlada (Scaffolding).

| CA | Título (corto) | Back | Front | QA | Notas |
|----|----------------|------|-------|----|-------|
| CA-1 | Bloqueador Modal (columna Blocked) | ❌ | ❌ | ❌ | Sin endpoint de transición con `blockReason`; sin modal en KanbanView |
| CA-2 | Inmutabilidad DONE (solo lectura) | ❌ | ❌ | ❌ | Sin validación de estado DONE en backend |
| CA-3 | Timer independiente esfuerzo vs SLA | ❌ | ❌ | ❌ | Sin tabla `ibpms_time_logs`; sin `<UniversalSlaTimer>` |
| CA-5 | Prohibición CMMN — JPA puro | ✅ | N/A | ❌ | `AgileTaskEntity` persiste como JPA. Correcto por diseño |
| CA-6 | State Machine PATCH /kanban/{tid}/state | ❌ | ❌ | ❌ | Endpoint PATCH no existe; `KanbanView.vue` mock hardcodeado con `setTimeout` |
| CA-7 | Event-Driven híbrido → Camunda async | ❌ | N/A | ❌ | Sin publisher de evento para transiciones Kanban |
| CA-8 | Gobernanza columnas + límite 7 | ❌ | ❌ | ❌ | Sin endpoint de columnas; sin validación de rol |

### Resumen US-008
- **CAs Totales:** 11 | **CAs cumplidos:** ~1 (CA-5 por diseño arquitectónico) | **% Real:** ~10%
- **QA:** ❌ 0%
- **Clasificación recomendada:** Mover de "Operativa" a "Scaffolding" en `future_backlog_v3.md`
- **Impacto:** US-030 (Hub Ágil) depende del Kanban operativo — el tablero de US-030 en Pantalla 3 no funciona

---

## US-029: Ejecución y Envío de Formulario (iForm Maestro o Simple)
**Épica:** B — Formularios/BPMN | **Estado:** 🔨 EN CONSTRUCCIÓN (~55%) | **Auditado:** 2026-04-18
**Archivos verificados:** `FormCompletionService.java` · `FormBffCoreService.java` · `CompletarTareaService.java`

| CA | Título (corto) | Back | Front | QA | Notas |
|----|----------------|------|-------|----|-------|
| CA-1 | Submit datos válidos (POST) | ✅ | ✅ | ✅ | `CompletarTareaService` + idempotencia + event sourcing probados |
| CA-2 | Submit datos inválidos (Zod 400) | ⚠️ | ⚠️ | ❌ | Validación Zod backend ✅; errores campo-a-campo no confirmados |
| CA-3 | TTL LocalStorage + GC + PII cifrado | ✅ | ✅ | ❌ | PII encryption US-000 integrada; auto-save con TTL |
| CA-4 | ACID — Saga compensación Camunda | ✅ | N/A | ❌ | `@Transactional` + Saga inversa si Camunda falla post-persist |
| CA-5 | BFF Megalítico (prefill contexto) | ❌ | ⚠️ | ❌ | `FormBffCoreService` usa `mockEventSourcingRepository` — BFF no conectado a BD real |
| CA-6 | Zero-Trust Owner Check (HTTP 403) | ✅ | N/A | ❌ | JWT `userId` vs BD `assignee` verificado antes de submit |
| CA-7 | Implicit Locking (dueño asignación) | ✅ | N/A | ❌ | Verificación dura de `assignee` en `FormCompletionService` |
| CA-11 | Autoguardado Híbrido + PII cifrado LS | ✅ | ✅ | ❌ | Integrado con US-000 PII encryption |
| CA-12 | Idempotencia Anti-Doble-Clic | ✅ | N/A | ✅ | `x-idempotency-key` header testeo unitario/e2e cubierto |
| CA-16 | Exclusión topológica Camunda + ACID | ✅ | N/A | ❌ | No pasa por Camunda para persistir el formulario |
| CA-19 a CA-24 | [REMEDIACIÓN] Reconciliación US-029/US-017, resiliencia 504, regeneración de token de sesión | ❌ | ✅ | ✅ | Tests cubiertos para recuperación 504 y Session Conflict |
| CA-25 a CA-34 | [REFINAMIENTO] Scroll al error, pre-aviso caducidad borrador, aduana archivos, sesión duplicada, validación condicional, etc. | ❌ | ❌ | ❌ | Sin verificar — CAs de refinamiento pendientes |

### Resumen US-029
- **CAs Totales:** 34 | **CAs verificados:** 12 | **CAs cumplidos:** ~7 | **% Real:** ~55%
- **QA:** ✅ CAs defensivos (CA-1, 12, 19-24 parcialmente verificables en frontend).
- **Bloqueador Principal:** `FormBffCoreService` con `mockEventSourcingRepository` — prefill sin datos reales de BD
- **Pendiente auditar:** CAs 19-34 (16 CAs de remediación y refinamiento)

---

## US-030: Instanciar y Planificar un Proyecto Ágil (Sprints/Kanban)
**Épica:** A — Motor Core | **Estado:** 🔨 EN CONSTRUCCIÓN (~85%) | **Auditado:** 2026-04-18
**Archivos verificados:** `AgileProjectService.java` · `AgileTaskService.java` · `agileStore.ts` · `AgileSlaChangelogRepository.java`

| CA | Título (corto) | Back | Front | QA | Notas |
|----|----------------|------|-------|----|-------|
| CA-1 | Kanban continuo sin Sprints (V1) | ✅ | ✅ | ❌ | Sin modelo Sprint en entidad; flujo continuo |
| CA-2 | Arranque vacío vs Plantilla WBS | ⚠️ | ⚠️ | ❌ | Inicio vacío ✅; WBS bloqueado (US-006 no existe aún) |
| CA-3 | CRUD tarjetas con slide-panel | ✅ | ✅ | ❌ | Todos los campos del CA: título, descripción, esfuerzo, responsable, tags |
| CA-4 | Hard-Delete con Auditoría Forense | ✅ | ✅ | ❌ | Registro inmutable antes del `delete()`; diálogo confirmación |
| CA-5 | Multi-assignee Hub; 1:1 en operativo | ✅ | ✅ | ❌ | `assignees` (lista) en planificación; 1:1 en Workdesk |
| CA-6 | Drag & Drop + campo `position` persistido | ✅ | ✅ | ❌ | `reorderTasks()` con campo `position` en BD |
| CA-7 | Vista Proyecto + Vista Portafolio | ✅ | ✅ | ❌ | Filtro por `leaderId` para portafolio; selector en `agileStore` |
| CA-8 | Archivo inteligente DONE + toggle | ✅ | ✅ | ❌ | Query filtra `status != DONE` por defecto; `showCompleted` toggle |
| CA-9 | SLA modificable + bitácora de cambios | ✅ | N/A | ❌ | `AgileSlaChangelogRepository` con valor anterior/nuevo/quien/cuando |
| CA-10 | Cierre proyecto con cascada CANCELADA | ✅ | ✅ | ❌ | Bulk update + evento de notificación |
| CA-11 | RBAC: solo Scrum Master / Líder modifican | ✅ | N/A | ❌ | Validación de rol en `AgileProjectService` |
| CA-12 | Virtual scroll backlog moderno | N/A | ✅ | ❌ | Lista virtualizada en `agileStore.ts` con Zod validation |
| CA-13 | Detección visual tareas rancias (15 días) | ❌ | ❌ | ❌ | Badge "Inactivo X días" no evidenciado |
| CA-14 | Carga liviana + reactividad cruzada + masivo | ✅ | ✅ | ❌ | Lazy load detalles; optimistic updates; bulk assign |

### Resumen US-030
- **CAs Totales:** 14 | **CAs cumplidos:** ~12 | **% Real:** ~85%
- **QA:** ❌ 0%
- **GAPs menores:** CA-2 bloqueado por US-006 (WBS) · CA-13 badge visual no verificado · Vista Kanban operativo (US-008) usa mocks
- **Clasificación:** La US más sólida del lote. Operativa con reservas sobre la integración con Pantalla 3

---

## US-003: IDE Web Low-Code para Formularios Inteligentes (iForm)
**Épica:** 2 — IDE Formularios | **Estado:** ✅ COMPLETADA (Back+Front)

| Rango CA | Back | Front | QA | Sprint | Handoff |
|----------|------|-------|----|--------|---------|
| CA-1 a CA-20 | ⏳ | ⏳ | ❌ | — | Sin handoff formal (pre-protocolo) |
| CA-21 a CA-25 | ✅ | ✅ | ❌ | S-2 | handoff_*_US003_CA21_CA25 |
| CA-26 a CA-30 | ✅ | ✅ | ❌ | S-2 | handoff_*_US003_CA26_CA30 |
| CA-31 a CA-35 | ✅ | ✅ | ❌ | S-2 | handoff_*_US003_CA31_CA35 |
| CA-36 a CA-40 | ✅ | ✅ | ❌ | S-2 | handoff_*_US003_CA36_CA40 |
| CA-41 a CA-45 | ✅ | ✅ | ❌ | S-2 | handoff_*_US003_CA41_CA45 |
| CA-46 a CA-50 | ✅ | ✅ | ❌ | S-2 | handoff_*_US003_CA46_CA50 |
| CA-51 a CA-54 | ✅ | ✅ | ❌ | S-2 | handoff_*_US003_CA51_CA54 |
| CA-55 a CA-59 | ✅ | ✅ | ❌ | S-2 | handoff_*_US003_CA55_CA59 |
| CA-60 a CA-64 | ✅ | ✅ | ❌ | S-2 | handoff_*_US003_CA60_CA64 |
| CA-65 a CA-69 | ✅ | ✅ | ❌ | S-2 | handoff_*_US003_CA65_CA69 |
| CA-87 | ✅ | ❌ | ❌ | S-69 | handoff_backend_us003_rem_ca87 |
| CA-88 | ✅ | ✅ | ❌ | S-69 | handoff_frontend_us003_rem_ca88 |
| CA-90 | ✅ | ✅ | ❌ | S-69 | handoff_frontend_us003_rem_ca90 |
| CA-91 | ✅ | ❌ | ❌ | S-69 | handoff_backend_us003_rem_ca91 |
| CA-92 | ✅ | ✅ | ❌ | S-69 | handoff_frontend_us003_rem_ca92 |
| CA-93 | ✅ | ✅ | ❌ | S-69 | handoff_frontend_us003_rem_ca93 |
| CA-70+ (otros) | ❌ | ❌ | ❌ | — | — |

> ⚠️ **Nota:** Los CAs CA-1 a CA-20 fueron implementados en iteraciones tempranas antes de la formalización del protocolo de handoffs. Requieren reconciliación con `git log`.

### Resumen US-003
- **CAs con Handoff explícito:** CA-21 a CA-69 (~49 CAs) | **Delegados Back+Front:** ✅ | **QA:** ❌ Pendiente
- **CAs sin Handoff:** CA-1 a CA-20, CA-70+ | **Estado:** Requiere reconciliación

---

## US-005: Modelador BPMN (Diseñador de Procesos)
**Épica:** 4 — BPMN | **Estado:** ✅ COMPLETADA (con observaciones OBS-1)

| Rango CA | Back | Front | QA | Sprint | Handoff |
|----------|------|-------|----|--------|---------|
| CA-1 a CA-4 | ✅ | ✅ | ❌ | S-3 | handoff_*_US005_CA1_CA4 |
| CA-5 a CA-6 | ✅ | ✅ | ❌ | S-3 | handoff_*_US005_CA5_CA6 |
| CA-7 a CA-10 | ✅ | ✅ | ❌ | S-3 | handoff_*_US005_CA7_CA10 |
| CA-11 | ✅ | ✅ | ❌ | S-3 | handoff_*_US005_CA11_CA15 |
| CA-12 | ✅ | ✅ | ✅🔧 | 74-DEV | handoff_*_US005_CA12 | DMN Binding. QA hotfix: imports corregidos por Arquitecto |
| CA-13 a CA-15 | ✅ | ✅ | ❌ | S-3 | handoff_*_US005_CA11_CA15 |
| CA-16 a CA-20 | ✅ | ✅ | ❌ | S-3 | handoff_*_US005_CA16_CA20 |
| CA-21 a CA-25 | ✅ | ✅ | ❌ | S-3 | handoff_*_US005_CA21_CA25 |
| CA-26 a CA-30 | ✅ | ✅ | ❌ | S-3 | handoff_*_US005_CA26_CA30 |
| CA-31 a CA-35 | ✅ | ✅ | ❌ | S-3 | handoff_*_US005_CA31_CA35 |
| CA-36 a CA-40 | ✅ | ✅ | ❌ | S-3 | handoff_*_US005_CA36_CA40 |
| CA-41 a CA-45 | ✅ | ✅ | ❌ | S-3 | handoff_*_US005_CA41_CA45 |
| CA-46 a CA-50 | ✅ | ✅ | ❌ | S-3 | handoff_*_US005_CA46_CA50 |
| CA-51 a CA-55 | ✅ | ✅ | ❌ | S-3 | handoff_*_US005_CA51_CA55 |
| CA-56 a CA-59 | ✅ | ✅ | ❌ | S-3 | handoff_*_US005_CA56_CA59 |
| CA-60 a CA-62 | ✅ | ✅ | ❌ | S-3 | handoff_*_US005_CA60_CA62 |
| CA-63 | ✅ | ✅ | ❌ | 73-DEV | handoff_*_US005_CA63 | SandboxInterceptor AOP |
| CA-64 | ✅ | ✅ | ❌ | 73-DEV | handoff_*_US005_CA64 | Break-Lock @PreAuthorize |
| CA-65 | 🟡 | ✅ | ⏳ | 73-DEV | handoff_*_US005_CA65 | OBS-2: Contrato API incompleto |
| CA-66 | ✅ | ✅ | ⏳ | 73-DEV | handoff_*_US005_CA66 | JPA Lock + Heartbeat 30s |
| CA-67 | ✅ | ✅ | ❌ | 73-DEV | handoff_*_US005_CA67 | Redis counter MAX=3 |
| CA-68 | 🔴 | ✅ | ❌ | 73-DEV | handoff_*_US005_CA68 | OBS-1: Entity/DDL mismatch |
| CA-69 | ✅ | ✅ | ❌ | 73-DEV | handoff_*_US005_CA69 | Deploy Request lifecycle |
| CA-70 | ✅ | ✅ | ⏳ | 73-DEV | handoff_*_US005_CA70 | Topic catalog + Pre-Flight |

### Resumen US-005
- **Total CAs con Handoff:** 70 | **Back+Front ✅:** 68/70 (97%) | **QA:** CA-12 ✅🔧 (hotfix Arquitecto)
- **Observaciones abiertas:** OBS-1 🔴 (CA-68 Entity/DDL), OBS-2 🟡 (CA-65 Contrato API)
- **Auditoría 73-DEV:** 🟡 APROBADO CON OBSERVACIONES
- **Auditoría 74-DEV:** ✅ CA-12 CERRADO

---

## US-017 (ex US-029): Persistencia Hexagonal CQRS y Task Completion
**Épica:** 16 — Persistencia CQRS | **Estado:** ❌ 0% IMPLEMENTADO

| CA | Título (corto) | Back | Front | QA | Sprint | Notas |
|----|----------------|------|-------|----|--------|-------|
| CA-1 | Enviar datos válidos POST /complete | ❌ | ❌ | ❌ | — | Nuevo (refactored) |
| CA-2 | Validación JSON Schema 400 | ❌ | ❌ | ❌ | — | Nuevo (refactored) |
| CA-3 | Inyección BFF Megalítica | ❌ | ❌ | ❌ | — | Refactored de US-029 |
| CA-4 | Lazy Patching V1→V2 | ❌ | ❌ | ❌ | — | Refactored de US-029 |
| CA-5 | Upload-First + Anti-IDOR | ❌ | ❌ | ❌ | — | 🔄 Remediación |
| CA-6 | Draft Sync + Cifrado PII LS | ❌ | ❌ | ❌ | — | 🔄 Remediación |
| CA-7 | RYOW Consistencia Eventual | ❌ | ❌ | ❌ | — | 🔄 Remediación |
| CA-8 | Idempotencia Anti-Doble-Clic | ❌ | ❌ | ❌ | — | 🔄 Remediación |
| CA-9 | Zod Isomórfico Guillotina | ❌ | ❌ | ❌ | — | Refactored de US-029 |
| — | — | — | — | — | — | *(CA-63 a CA-70 reubicados a sección US-005 — Auditoría 73-DEV)* |
| CA-12 | CQRS Event Sourcing | ❌ | ❌ | ❌ | — | Refactored de US-029 |
| CA-13 | Exclusión Topológica Camunda | ❌ | ❌ | ❌ | — | Refactored de US-029 |
| CA-14 | ACID Fallback Saga Inverso | ❌ | ❌ | ❌ | — | Refactored de US-029 |
| CA-15 | Auto-Claim Group-Level | ❌ | ❌ | ❌ | — | 🔄 Remediación |
| CA-16 | Trazabilidad Rechazos BFF | ❌ | ❌ | ❌ | — | 🔄 Remediación |

### Resumen US-017
- **Total CAs:** 16 | **✅ Completos:** 0 | **🔄 Remediación:** 6 | **❌ Pendiente:** 16 (100%)

---

## US-028: Auto-Generación de Test Suites Zod/Vitest
**Épica:** 2 — IDE Formularios | **Estado:** ✅ COMPLETADA

| Rango CA | Back | Front | QA | Sprint | Handoff |
|----------|------|-------|----|--------|---------|
| CA-1 a CA-4 | ✅ | ✅ | ❌ | S-4 | handoff_*_US028_CA1_CA4 |
| CA-4 a CA-6 | ✅ | ✅ | ❌ | S-4 | handoff_*_US028_CA4_CA6 |
| CA-7 a CA-9 | ✅ | ✅ | ❌ | S-4 | handoff_*_US028_CA7_CA9 |
| CA-10 a CA-11 | ✅ | ✅ | ❌ | S-4 | handoff_*_US028_CA10_CA11 |
| CA-12 | Revocación Sello Mutación | ✅ | ✅ | ✅ | 74-DEV | handoff_74DEV_US028_CA12_CA17 |
| CA-13 | Versionado Sello Schema | ✅ | ✅ | ✅ | 74-DEV | handoff_74DEV_US028_CA12_CA17 |
| CA-14 | Anotación SuperRefine Fuzzer | N/A | ✅ | ✅ | 74-DEV | handoff_74DEV_US028_CA12_CA17 |
| CA-15 | Truncamiento Payload Audit | ✅ | N/A | ✅ | 74-DEV | handoff_74DEV_US028_CA12_CA17 |
| CA-16 | Concurrencia Certificación | ✅ | N/A | ✅ | 74-DEV | handoff_74DEV_US028_CA12_CA17 |
| CA-17 | Coherencia BPMN↔Zod | ✅ | ✅ | ✅ | 74-DEV | handoff_74DEV_US028_CA12_CA17 |

### Resumen US-028
- **Total CAs:** 17 | **✅ Completado:** 17/17 (100%) | **QA:** CA-12 a CA-17 ✅

---

## US-036: RBAC, Zero-Trust y Gobernanza de Seguridad (ISO 27001)
**Épica:** 13 — Seguridad/RBAC | **Estado:** ✅ COMPLETADA

| CA | Título (corto) | Back | Front | QA | Sprint | Handoff / Notas |
|----|----------------|------|-------|----|--------|-----------------|
| CA-6 | Roles VIP Visuales (Pantalla 14) | ✅ | ✅ | ✅ | S-3 | Backend OK / Frontend UI Insignias Integrado |
| CA-19 | Liquibase Schema Roles/Permisos | ✅ | ❌ | ✅ | S-3 | handoff_backend_DEF02_DEF03 / Backend OK |
| CA-20 | RLS Interceptor AOP (assignee_id) | ✅ | ❌ | ✅ | S-3 | Backend OK |
| CA-21 | Kill Session & Dummy JWT Blacklist | ✅ | ❌ | ✅ | S-3 | Backend OK |
| CA-22 | Service Accounts API Keys (SHA-256) | ✅ | ✅ | ✅ | S-3 | Backend OK / UI Modal Integrado |
| CA-23 | Lazy Evaluation Tareas Delegadas | ✅ | ❌ | ✅ | S-3 | Backend OK |
| CA-24 | Reporte Generador ISO 27001 | ✅ | ✅ | ✅ | S-3 | Backend OK / Botón Descarga CSV Integrado |
| CA-25 | Trazabilidad Inmutable (Audit Trail) | ✅ | ❌ | ✅ | S-3 | Backend completado implícitamente mediante logs sudoers |

### Resumen US-036
- **Total CAs con Handoff Backend:** 7 (CA-19 al CA-25) | **Back:** ✅ 100% | **Front:** ✅ Parcial (CA-6, CA-22, CA-24) | **QA:** ✅ 100%

---

## US-034: Orquestación a través de RabbitMQ
**Épica:** 12 — Integraciones | **Estado:** ✅ COMPLETADA

| Rango CA | Back | Front | QA | Sprint | Notas |
|----------|------|-------|----|--------|-------|
| CA-4 a CA-10 | ✅ | ✅ | ✅ | S-70 | Remediación Dashboard DLQ (CA-8 Frontend validado) |

> ⚠️ **Pendiente:** Requiere desglose CA-a-CA detallado en próxima reconciliación.

### Resumen US-034
- **Handoff explícito:** CA-4 a CA-10 | **Back+Front+QA:** ✅

---

## US-038: Asignación Multi-Rol y Sincronización EntraID
**Épica:** 13 — Seguridad/RBAC | **Estado:** ✅ COMPLETADA (Back+Front)

| Rango | Back | Front | QA | Sprint | Notas |
|-------|------|-------|----|--------|-------|
| Parte 1 | ✅ | ✅ | ❌ | S-3 | Dashboard/BAM |
| Parte 2 | ✅ | ✅ | ❌ | S-3 | Multi-Rol assignment |
| Parte 3 | ✅ | ✅ | ❌ | S-3 | EntraID Sync |

> ⚠️ **Pendiente:** Requiere desglose CA-a-CA detallado. QA al 0%.

### Resumen US-038
- **Back+Front:** ✅ 100% | **QA:** ❌ 0% Pendiente

---

## US-039: Formulario Genérico Base (Pantalla 7.B)
**Épica:** 2 — IDE Formularios | **Estado:** ✅ COMPLETADA

| Rango CA | Back | Front | QA | Sprint | Notas |
|----------|------|-------|----|--------|-------|
| CA-4 a CA-8 | ✅ | ✅ | ✅ | S-72 | Hardening OBS-1, OBS-2 Frontend OK. QA Gatekeeper Red Stage Activo |

> ⚠️ **Pendiente:** Requiere desglose CA-a-CA detallado en próxima reconciliación.

### Resumen US-039
- **Handoff explícito:** CA-4 a CA-8 | **Back+Front+QA:** ✅

---

## US-043: Configuración Global de SLA
**Épica:** 14 — SLA | **Estado:** ✅ COMPLETADA (con deuda técnica)

| Rango CA | Back | Front | QA | Sprint | Notas |
|----------|------|-------|----|--------|-------|
| Handoff general | ✅ | ✅ | ❌ | S-3 | Completado |
| CA-6 | ⚠️ | ⚠️ | ❌ | — | **Deuda técnica pendiente** |

> ⚠️ **Pendiente:** Requiere desglose CA-a-CA detallado. CA-6 marcado como deuda técnica sin plan de remediación.

### Resumen US-043
- **Back+Front:** ✅ (excepto deuda CA-6) | **QA:** ❌ 0% Pendiente

---

## US-048: Módulo Gestor Propio de Identidades (Internal IdP)
**Épica:** 13 — Seguridad/RBAC | **Estado:** ✅ COMPLETADA (Back+Front)

| Rango CA | Back | Front | QA | Sprint | Notas |
|----------|------|-------|----|--------|-------|
| Handoff general | ✅ | ✅ | ❌ | S-3 | Completado |

> ⚠️ **Pendiente:** Requiere desglose CA-a-CA detallado. QA al 0%.

### Resumen US-048
- **Back+Front:** ✅ 100% | **QA:** ❌ 0% Pendiente

---

## Resumen Global de Cobertura (Actualizado 2026-04-18)

| Métrica | Valor |
|---------|-------|
| **Total US en V1** | 56 |
| **US Completadas (Back+Front)** | 11 (US-000, US-001, US-003, US-005, US-028, US-034, US-036, US-038, US-039, US-043, US-048) |
| **US En Construcción** | 5 (US-002 ~9%, US-004 ~71%, US-007 ~48%, US-029 ~55%, US-030 ~85%) |
| **US Scaffolding (Fencing activo)** | 5 (US-008 ~10%, US-011, US-021, US-035, US-045) |
| **US Refactoring** | 1 (US-017 — ADR-001 compliance) |
| **US Pendientes** | 34 |
| **CAs Implementados (estimado)** | ~248+ |
| **CAs Validados QA** | ~38 (~15%) |
| **Falsos Positivos Corregidos** | 3 (US-001 CA-8 · US-002 declarada Operativa · US-008 declarada Operativa) |
| **Vulnerabilidades Críticas Abiertas** | 2 (IDOR tenantId en US-007 · BD no persiste en US-002) |
| **Principal Brecha** | 🔴 QA < 15% global. 2 vulnerabilidades de seguridad activas. |

### Brechas Prioritarias (Post Auditoría 2026-04-18)

| Prioridad | Brecha | US Afectadas | Acción Recomendada |
|-----------|--------|-------------|-------------------|
| 🔴 P0 | IDOR activo — tenantId hardcodeado | US-007 | Hotfix inmediato: extraer tenantId del JWT en `DmnGovernanceController` |
| 🔴 P0 | Persistencia BD comentada — assignee perdido en restart | US-002 | Handoff Sprint 5 Iteración 1 ya emitido — ejecutar |
| 🔴 P0 | `EmailWebhookController` bypasea pipeline de seguridad | US-004 | Redirigir o eliminar el controller legacy |
| 🟠 P1 | US-008 KanbanView con mock hardcodeado | US-008, US-030 | Implementar state machine real antes de declarar US-030 "Operativa" |
| 🟠 P1 | `FormBffCoreService` mockeado | US-029 | Conectar a BD real antes de Sprint 5 |
| 🟠 P1 | CA-6 US-004: sin RabbitMQ consumer de intake | US-004 | Implementar `@RabbitListener` para `ibpms.integrations.webhook` |
| 🟡 P2 | QA al 0% en US completadas | US-003, US-005, US-038, US-043, US-048 | Sprint de QA dedicado |
| 🟡 P2 | CAs Remediación US-007 (13-18) y US-029 (19-24) sin auditar | US-007, US-029 | Continuar auditoría en próxima iteración |
| 🟡 P3 | Desglose CA-a-CA faltante | US-034, US-038, US-039, US-043, US-048 | Reconciliación con `git log --grep="CA-"` |
| 🟡 P3 | Deuda técnica US-043 CA-6 | US-043 | Plan de remediación con ticket |
| 🟡 P4 | OBS abiertas US-005 | US-005 | Cerrar OBS-1 (CA-68) y OBS-2 (CA-65) |

---

> **⚡ Próxima acción recomendada (post auditoría 2026-04-18):**
> 1. **P0 SEGURIDAD:** Hotfix `DmnGovernanceController` — extraer `tenantId` del `SecurityContext` (JWT), no hardcodeado
> 2. **P0 DATOS:** Ejecutar Sprint 5 Iteración 1 (`handoff_backend_sprint5_iteracion1.md`) — conectar `taskRepository.assignTask()` en US-002
> 3. **P0 SEGURIDAD:** Deprecar o encadenar `EmailWebhookController` al pipeline de `WebhookIntakeService`
> 4. Ejecutar `/reconciliacionCoberturaCa.md` sobre US-034, US-038, US-039, US-043, US-048
> 5. Continuar auditoría CAs 13-25 de US-007 y CAs 19-34 de US-029
