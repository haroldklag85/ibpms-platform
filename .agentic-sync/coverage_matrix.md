# 📊 Matriz de Cobertura de Implementación (iBPMS V1)

> **Última actualización:** 2026-04-22T23:49 (Reconciliación Arquitecto — Cierre Deuda Técnica US-017 CA-19→CA-26 + Delegación S6.2) | **Responsable:** Arquitecto Líder
> **Fuente de Verdad:** Checklist validado manualmente por el PO/Arquitecto Líder
> **Leyenda:** ✅ Implementado | ⏳ En progreso | ❌ Pendiente | 🚫 Excluido (V2+) | 🔄 Remediación pendiente | ⚠️ Falso Positivo Corregido

## Instrucciones de Uso

1. **¿Quién actualiza esta matriz?** Cada agente de desarrollo (Backend/Frontend) DEBE marcar sus CAs como ✅ después de hacer `git commit` y `git push` (ver `agent_git_governance_policy.md` §2).
2. **¿Quién la audita?** El Arquitecto Líder ejecuta `/reconciliacionCoberturaCa.md` al cierre de cada Sprint para cruzar esta matriz contra `git log` y detectar falsos positivos.
3. **¿Cómo se lee?** Cada US tiene su tabla. Las columnas Back/Front/QA indican si esa capa fue implementada. La columna Handoff referencia el archivo de delegación.
4. **PROHIBIDO AGRUPAR CAs:** Está estrictamente prohibido agrupar Criterios de Aceptación (ej. "CA-13 a CA-18"). Cada CA debe tener su propia fila individual para garantizar un rastreo forense preciso del estado de desarrollo y pruebas. Cualquier agrupamiento previo debe ser expandido inmediatamente.

> [!CAUTION]
> **Corrección 2026-04-10:** Se detectaron 4 Falsos Positivos en US-001 (CA-4, CA-5, CA-6, CA-8) que estaban marcados como ✅ pero NO están confirmados por el PO. Se corrigen a ❌ Pendiente. Esto valida que la sincronización automática por agentes es insuficiente y requiere auditoría manual periódica.

---

## Resumen Ejecutivo Global

| Métrica | Valor |
|---------|-------|
| **Total US en V1** | 56 |
| **US Completadas** | 11 (US-000, US-001, US-003, US-005, US-028, US-034, US-036, US-038, US-039, US-043, US-048) |
| **US En Construcción (avanzadas >60%)** | 6 (US-002 ~68%, US-004 ~71%, US-025 ~60%, US-027 ~65%, US-029 ~72%, US-030 ~85%) |
| **US En Construcción (tempranas <50%)** | 2 (US-007 ~48% — IDOR remediado, US-017 ~50% — 8 CAs UX/UI pendientes) |
| **US Scaffolding (Fencing activo)** | 5 (US-008 ~10%, US-011, US-021, US-035, US-045) |
| **US Pendientes** | 32 |
| **CAs Implementados (estimado)** | ~290+ |
| **CAs Validados QA** | ~38 (~13%) |
| **Seguridad Crítica** | ✅ IDOR cerrado en US-007 y US-027 (remediado en S6.1: role prefix + tenant propagation + Anti-IDOR startsWith) — US-002 BD corregida en S5.1 |
| **Principal Brecha** | 🟡 QA < 13% global. US-008 Kanban sigue mock. Data seed E2E pendiente. |
| **E2E Sprint 6.1** | 4/7 PASS (57%) — Seguridad 100%, UI 0% (falta data seed operacional) |

> [!CAUTION]
> **Corrección 2026-04-18 — Auditoría Integral Sección 1.2:** Se detectaron 2 Falsos Positivos críticos en `future_backlog_v3.md`:
> - **US-008** declarada ✅ Operativa — real: ~10% Scaffolding (KanbanView.vue usa mock data hardcodeado, sin state machine)
> Adicionalmente: US-007 declarada Operativa pero es Beta parcial (48%) con IDOR activo.

> [!NOTE]
> **Reconciliación PO 2026-04-18T15:25 — Auditoría Cruzada Código Fuente vs Matrix:**
> Se detectó que la matrix estaba **desactualizada en ~4 sprints** para múltiples US. Los agentes ejecutores implementaron remediaciones sin actualizar este documento.
> - **US-002** pasó de ~9% a **~68%** (assignee corregido, BD activa, force-unclaim, audit-trail, claim-next, rollback-claim implementados)
> - **US-017** pasó de 0% a **~78%** (refactoring hexagonal completado: FormEvent POJO puro, FormEventEntity en infraestructura, Event Sourcing + Saga funcional)
> - **US-029** pasó de ~55% a **~72%** (FormCompletionService con CQRS completo, PII encryption, draft/complete endpoints)
> - **US-025** y **US-027** estaban **completamente ausentes** de la matrix pero tienen implementación real en código (BFF, Auth, Copilot IA)
> - **US-002** P0 resuelto: `assignee` y `tenantId` ahora se extraen del JWT vía `SecurityContextUtils`

---

## US-000: Resiliencia Integrada y Enmascaramiento PII Visual
**Épica:** 0 — Gobernanza Global | **Estado:** ✅ COMPLETADA (Transversal)

| CA | Título (corto) | Back | Front | Unitarios | Componente | Integración | E2E | UAT | Sprint | Spec File | Notas |
|----|----------------|------|-------| ---- | ---- | ---- | ---- | ---- |--------| ❌ Ninguno |-------|
| CA-1 | Degradación Grácil HTTP 500/503 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-1 | ❌ Ninguno | Transversal — interceptor global |
| CA-2 | Triage Semántico Validaciones 400/422 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-1 | ❌ Ninguno | Array DTO {field, issue, translatedMessage} |
| CA-3 | Concurrencia Optimista 409 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-1 | ❌ Ninguno | Control de versión en BD |
| CA-4 | Enmascaramiento PII Redaction | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-1 | ❌ Ninguno | Interceptor regex/LLM |

### Resumen US-000
- **Total CAs:** 4 | **✅ Back+Front:** 4/4 (100%) | **QA:** ❌ 0% Pendiente
- **Nota:** US transversal. Todos los CAs aplican como reglas globales a todas las demás US.

---

## US-001: Bandeja de Entrada Unificada (Hybrid Workdesk)
**Épica:** 1 — Orquestación | **Estado:** 🔨 EN CONSTRUCCIÓN (26/30 CAs activos — 86%)

| CA | Título (corto) | Back | Front | Unitarios | Componente | Integración | E2E | UAT | Sprint | Handoff | Spec File | Notas |
|----|----------------|------|-------| ---- | ---- | ---- | ---- | ---- |--------|---------| ❌ Ninguno |-------|
| CA-1 | Vista 360 Grid paginada | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | 77-DEV | handoff_77DEV_US001 | ❌ Ninguno | Auditado en 77-DEV |
| CA-2 | Búsqueda Híbrida Reactiva | 🚫 | 🚫 | 🚫 | 🚫 | 🚫 | 🚫 | 🚫 | — | Anulado por CA-19 | ❌ Ninguno | Reemplazado por búsqueda 100% Server-Side |
| CA-3 | Data Grid tabular 5 cols | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | 77-DEV | handoff_77DEV_US001 | ❌ Ninguno | Auditado en 77-DEV |
| CA-4 | Toggle Delegación Mis Tareas/Equipo | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | 81-DEV | handoff_81DEV_US001_CA04_CA15 | ❌ Ninguno | Auditado en 81-DEV |
| CA-5 | SLA Ticking Engine Vivo | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | 80-DEV | handoff_80DEV_US001_CA05_CA11_CA24_CA25_CA31 | ❌ Ninguno | Auditado en 80-DEV |
| CA-6 | Ghost Deletion STOMP WebSocket | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | 79-DEV | handoff_79DEV_US001_CA06_CA13_CA26_CA27 | ❌ Ninguno | Auditado en 79-DEV |
| CA-7 | Tolerancia Fallas CQRS | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | 77-DEV | handoff_77DEV_US001 | ❌ Ninguno | Auditado en 77-DEV |
| CA-8 | Anti-Cherry Picking Feature Flag | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | 82-DEV | handoff_82DEV_US001_CA08_CA16_CA21_CA28 | ❌ Ninguno | Auditado en 82-DEV |
| CA-9 | Paginación Máxima Visual | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | 76-DEV | handoff_76DEV_us001 | ❌ Ninguno | Auditado en 76-DEV |
| CA-10 | Paginación Server-Side y pg_trgm | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | 76-DEV | handoff_76DEV_us001 | ❌ Ninguno | Auditado en 76-DEV |
| CA-11 | Heartbeat Store rAF | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | 80-DEV | handoff_80DEV_US001_CA05_CA11_CA24_CA25_CA31 | ❌ Ninguno | Auditado en 80-DEV |
| CA-12 | Ergonomía KeepAlive Empty State | N/A | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | 77-DEV | handoff_77DEV_US001 | ❌ Ninguno | Frontend only |
| CA-13 | Minificación WebSocket Throttling | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | 79-DEV | handoff_79DEV_US001_CA06_CA13_CA26_CA27 | ❌ Ninguno | Auditado en 79-DEV |
| CA-14 | Sanitización DTO y Aislamiento RLS | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | 76-DEV | handoff_76DEV_us001 | ❌ Ninguno | Auditado en 76-DEV |
| CA-15 | Delegación Segura Anti-IDOR | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | 81-DEV | handoff_81DEV_US001_CA04_CA15 | ❌ Ninguno | Auditado en 81-DEV |
| CA-16 | Skill-Based Routing | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | 82-DEV | handoff_82DEV_US001_CA08_CA16_CA21_CA28 | ❌ Ninguno | Auditado en 82-DEV |
| CA-17 | Ordenamiento SLA y Priority Fallback | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | 76-DEV | handoff_76DEV_us001 | ❌ Ninguno | Auditado en 76-DEV |
| CA-18 | Degradación Multi-Motor | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | 77-DEV | handoff_77DEV_US001 | ❌ Ninguno | Auditado en 77-DEV |
| CA-19 | Búsqueda Exclusiva Server-Side | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | 76-DEV | handoff_76DEV_us001 | ❌ Ninguno | Auditado en 76-DEV |
| CA-20 | Estandarización Contrato API | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | 76-DEV | handoff_76DEV_us001 | ❌ Ninguno | Auditado en 76-DEV |
| CA-21 | Skill-Based Skipeo Justificado | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | 82-DEV | handoff_82DEV_US001_CA08_CA16_CA21_CA28 | ❌ Ninguno | Auditado en 82-DEV |
| CA-22 | Filtros Facetados por Status | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | 78-DEV | handoff_78DEV_US001 | ❌ Ninguno | Auditado en 78-DEV |
| CA-23 | Fórmula Avance Determinista | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | 77-DEV | handoff_77DEV_US001 | ❌ Ninguno | Auditado en 77-DEV |
| CA-24 | Umbrales Semáforo SLA Configurables | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | 80-DEV | handoff_80DEV_US001_CA05_CA11_CA24_CA25_CA31 | ❌ Ninguno | Auditado en 80-DEV |
| CA-25 | Recálculo Semáforos Tab Inactiva | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | 80-DEV | handoff_80DEV_US001_CA05_CA11_CA24_CA25_CA31 | ❌ Ninguno | Auditado en 80-DEV |
| CA-26 | Relleno Automático Post-WebSocket | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | 79-DEV | handoff_79DEV_US001_CA06_CA13_CA26_CA27 | ❌ Ninguno | Auditado en 79-DEV |
| CA-27 | Vocabulario Completo WebSocket | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | 79-DEV | handoff_79DEV_US001_CA06_CA13_CA26_CA27 | ❌ Ninguno | Auditado en 79-DEV |
| CA-28 | Prevención Race Condition Atender | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | 82-DEV | handoff_82DEV_US001_CA08_CA16_CA21_CA28 | ❌ Ninguno | Auditado en 82-DEV |
| CA-29 | Contadores en Filtros por Tenant | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | 78-DEV | handoff_78DEV_US001 | ❌ Ninguno | Auditado en 78-DEV |
| CA-30 | Rate Limiting API 429 | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | 78-DEV | handoff_78DEV_US001 | ❌ Ninguno | Auditado en 78-DEV |
| CA-31 | Auto-Refresco Pasivo Inactividad | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | 80-DEV | handoff_80DEV_US001_CA05_CA11_CA24_CA25_CA31 | ❌ Ninguno | Auditado en 80-DEV |

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
**Épica:** A — Motor Core | **Estado:** 🔨 EN CONSTRUCCIÓN (~68%) | **Auditado:** 2026-04-18T15:25 (Reconciliación PO)
**Archivos verificados:** `TaskClaimController.java` · `WorkboxTaskController.java` · `AgileTaskService.java` · `ClaimAuditService.java` · `SecurityContextUtils.java`

> [!NOTE]
> **REMEDIACIÓN CONFIRMADA (Sprint 5.1):** Los 2 bloqueadores P0 detectados en la auditoría anterior han sido **resueltos**:
> - ✅ `assignee` ahora se extrae del JWT vía `SecurityContextUtils.getAssignee()` (ya NO hardcodeado)
> - ✅ `tenantId` ahora se extrae del JWT vía `SecurityContextUtils.getTenantId()` (ya NO hardcodeado)
> - ✅ Persistencia BD activa: `taskRepository.save(task)` + `findByIdForUpdate()` (SKIP LOCKED)

| CA | Título (corto) | Back | Front | Unitarios | Componente | Integración | E2E | UAT | Spec File | Notas |
|----|----------------|------|-------| ---- | ---- | ---- | ---- | ---- | ❌ Ninguno |-------|
| CA-1 | Reclamo Simultáneo (anti race-condition) | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ Ninguno | `findByIdForUpdate()` SKIP LOCKED + Redis SETNX + BD persist |
| CA-2 | Reclamo Masivo en Lote (bulk-claim) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Endpoint `/tasks/bulk-claim` no existe |
| CA-4 | Liberación con Mensaje Interno | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | `unclaim` persiste en BD + WS; sin campo mensaje interno |
| CA-5 | Modo Sólo Lectura (pre-claim) | ✅ | CA-5 | ✅ | ✅ | ✅ | ✅ | ✅ | us002-preview-readonly.spec.ts | Vitest + Playwright (`us002-preview-readonly.spec.ts`) |
| CA-6 | Ghost Job Timeout (Auto-Unclaim Cron) | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | `AutoClaimService` existe; umbral tenant-configurable no verificado |
| CA-7 | Amnesia Transaccional al Liberar | ❌ | CA-7 | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ Ninguno | Vitest: Verificación de modal confirmation de cancelación de unclaim |
| CA-8 | Despojo Forzoso Supervisor | ✅ | CA-8 | ✅ | ✅ | ✅ | ✅ | ✅ | us002-force-unclaim-supervisor.spec.ts | Playwright: `us002-force-unclaim-supervisor.spec.ts` 200 y 403 test |
| CA-9 | Trazabilidad Forense Pop-Up | ✅ | CA-9 | ✅ | ✅ | ✅ | ✅ | ✅ | ClaimAuditTrail.spec.ts | Vitest (`ClaimAuditTrail.spec.ts`) y Playwright audit log assertion |
| CA-10 | Resiliencia Offline | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Sin Optimistic UI + rollback (offline mode) |
| CA-11 | Bloqueo Atómico BD (SKIP LOCKED) | ✅ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | `findByIdForUpdate()` activo en `AgileTaskService` |
| CA-12 | Evento WebSocket Post-Commit | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Eventos tipados: `TASK_CLAIMED`, `TASK_UNCLAIMED`, `TASK_FORCE_UNCLAIMED`, `TASK_POOL_REFRESH` |
| CA-14 | Contrato API Estandarizado OpenAPI | ❌ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Sin OpenAPI annotations formales |
| CA-21 | Rollback Optimistic UI | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | `POST /rollback-claim` en `WorkboxTaskController` |
| CA-22 | Separación Visual Bandeja/Cola Equipo | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Sin tabs "Mi Bandeja" / "Cola Equipo" |
| CA-23 | Claim-Next Atómico (SKIP LOCKED) | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | `POST /claim-next` con `findNextAvailableTaskForUpdate()` |

### Resumen US-002
- **CAs Totales:** 23 | **CAs Back Implementados:** ~10 | **CAs Front Implementados:** ~4 | **% Real:** ~75%
- **QA:** CA-1, CA-5, CA-7, CA-8, CA-9 Certificados (Vitest + Playwright).
- **Bloqueadores P0 Resueltos:** ✅ assignee del JWT · ✅ BD activa con SKIP LOCKED
- **Pendientes principales:** Bulk-claim (CA-2), Offline (CA-10), OpenAPI (CA-14), Frontend tabs (CA-22)

---

## US-004: Iniciar un Proceso mediante Webhook (Plugin O365 Listener)
**Épica:** A — Motor Core | **Estado:** 🔨 EN CONSTRUCCIÓN (~71%) | **Auditado:** 2026-04-18
**Archivos verificados:** `WebhookIntakeController.java` · `WebhookIntakeService.java` · `OrphanPayloadRepositoryJpa.java` · `ClamAvScannerAdapter.java` · `TriagePurgeScheduler.java` · `EmailWebhookController.java`

| CA | Título (corto) | Back | Front | Unitarios | Componente | Integración | E2E | UAT | Spec File | Notas |
|----|----------------|------|-------| ---- | ---- | ---- | ---- | ---- | ❌ Ninguno |-------|
| CA-1 | Idempotencia (duplicados silenciosos) | ✅ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | `WebhookTransaction` con UNIQUE en `message_id` |
| CA-2 | Bloqueo auto-responders | ✅ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Regex: no-reply, mailer-daemon, postmaster, bounce |
| CA-3 | Payloads basura → tabla OrphanPayload | ✅ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | SHA-256 hash + tipo de error persistido |
| CA-4 | Whitelist dominios autorizados | ✅ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | `existsByDomainAndTenantIdAndIsActiveTrue()` por tenant |
| CA-5 | Alerta admin si Camunda falla | ❌ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | No evidenciado en código auditado |
| CA-6 | Resiliencia RabbitMQ (Camunda offline) | ❌ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Cola definida en config; **sin `@RabbitListener`** — webhooks se pierden si Camunda cae |
| CA-7 | Límite de peso configurable (HTTP 413) | ✅ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | `maxSizeBytes` default 10MB |
| CA-8/9 | Pre-Triaje humano en Camunda | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | `TriageTask` entity + proceso Camunda `Process_PreTriaje_Intake`; UI Pantalla 16 pendiente |
| CA-10 | HMAC signature validation | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | HmacSHA256 + tiempo constante ✅; sin switch Bearer Token legacy |
| CA-11 | ClamAV Anti-Malware (fail-secure) | ✅ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | REST adapter 5s timeout; fallo → HTTP 503 + DLQ |
| CA-12 | CRUD Admin Whitelist dominios | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | `AllowedDomainAdminController` stub `NOT_IMPLEMENTED` (fenced como US-045) |
| CA-13 | Purga automática 30 días | ✅ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | `deleteByCreatedAtBefore` scheduler diario 2AM |
| CA-14 | Experiencia Pre-visión y Rechazo (Triaje) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | UI Pantalla 16 y lógica de rechazo pendientes |
| CA-15 | Canalización del Trámite Específico | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Instanciación selectiva de procesos pendiente en UI |
| CA-16 | Reloj SLA de Entrada en Cola | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Timer SLA de pre-triaje no evidenciado |
| CA-17 | Sub-segundo ACK al emisor | ✅ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | 202 Accepted sincrónico |

### Resumen US-004
- **CAs Totales:** 17 | **CAs Back Implementados:** ~9 | **% Real:** ~71%
- **QA:** ❌ 0%
- **Riesgo Alto:** `EmailWebhookController` legacy activo — **bypasea todo el pipeline** (sin HMAC, sin ClamAV, sin whitelist)
- **Bloqueador CA-6:** Sin consumer RabbitMQ activo — sistema falla sincrónico cuando Camunda está offline

---

## US-007: Generador Cognitivo de DMN (NLP a Tablas de Decisión)
**Épica:** B — Formularios/BPMN | **Estado:** 🔨 EN CONSTRUCCIÓN (~94%) | **Auditado:** 2026-05-03 (Auditoría Forense Completa Sprint-6)
**Archivos verificados:** `AiDmnGeneratorController.java` · `DmnGovernanceController.java` · `DmnSimulatorController.java` · `DmnIntelligence.vue` · `DmnGridManual` · `useDmnStore.ts`

> [!NOTE]
> **IDOR REMEDIADO:** El `tenantId` hardcodeado en `DmnGeneratorController` fue corregido utilizando `SecurityContextUtils.getTenantId()`. Aislamiento multitenant asegurado.

| CA | Título (corto) | Back | Front | Unitarios | Componente | Integración | E2E | UAT | Spec File | Notas |
|----|----------------|------|-------| ---- | ---- | ---- | ---- | ---- | ❌ Ninguno |-------|
| CA-1 | Streaming SSE generación IA | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ Ninguno | Endpoint SSE + reconexión automática en `DmnIntelligence.vue`, test 504 cubierto |
| CA-2 | Caché criptográfica (anti DoW) | ✅ | N/A | ✅ | ✅ | ✅ | ✅ | ✅ | us007-rate-limiting.spec.ts | Caché por hash ✅; rate limit 5/min ✅; test `us007-rate-limiting.spec.ts` |
| CA-3 | GC y Compresión XML borradores | ✅ | N/A | ✅ | ✅ | ✅ | ✅ | ✅ | us007-gc-drafts.spec.ts | `DmnDraftCleanupScheduler` TTL 24h ✅; XML minificación ✅; test `us007-gc-drafts.spec.ts` |
| CA-4 | Sandboxing Anti-RCE & XSS | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | DmnAntiXss.spec.ts | DOMPurify inyectado en celdas ✅; `DmnAntiXss.spec.ts` reforzado |
| CA-5 | Seudonimización PII del Prompt | ✅ | N/A | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ Ninguno | `PromptPiiScrubber` alias var_1/var_2 antes de LLM ✅; validación cruzada QA |
| CA-6 | Inmutabilidad DMN & RBAC (anti-IDOR) | ✅ | N/A | ✅ | ✅ | ✅ | ✅ | ✅ | us007-tenant-isolation.spec.ts | Playwright (`us007-tenant-isolation.spec.ts`) ✅ |
| CA-7 | Hit Policy FIRST + Catch-All | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | us007-hit-policy-catchall.spec.ts | Validación catch-all ✅; test `us007-hit-policy-catchall.spec.ts` |
| CA-8 | Variables planas, prohibición Date-Math | ✅ | N/A | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ Ninguno | `DmnVariableValidator` rechaza dot-notation + date-math ✅ |
| CA-9 | Límites cognitivos + validación inversa | ✅ | N/A | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ Ninguno | `DmnRuleValidator` overlap + hard-stop 50 filas ✅; test cruzado QA |
| CA-10 | Virtual Scrolling grilla alta densidad | N/A | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ Ninguno | Virtual scrolling ✅; test `.dmn-row` counting Vitest |
| CA-11 | XAI Explicabilidad + Simulador | N/A | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | us007-simulator-xai.spec.ts | Panel XAI ✅; test `us007-simulator-xai.spec.ts` |
| CA-12 | Contención de Pánico + Trazabilidad Chat | N/A | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | DmnPublishModal.spec.ts | Panic modal CONFIRMO_V2 ✅; test `DmnPublishModal.spec.ts` |
| CA-13 | Persistencia Dual Borradores DMN | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | us007-draft-persistence.spec.ts | PostgreSQL primario + LS caché ✅; test `us007-draft-persistence.spec.ts` |
| CA-14 | Validación Pre-Flight Catch-All DMN | ✅ | N/A | ⚠️ | ⚠️ | ⚠️ | ⚠️ | ⚠️ | ❌ Ninguno | Regla Pre-Flight en US-005 ✅; QA parcial (pendiente seeder BPMN complejo) |
| CA-15 | Endpoint Simulador Decisiones DMN | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | us007-evaluate-test.spec.ts | `DmnSimulatorController` ✅; test `us007-evaluate-test.spec.ts` |
| CA-16 | Invalidación Caché Redis Zod | ✅ | N/A | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ Ninguno | `FormSchemaChangedRabbitListener` consumer ✅; publicador US-003 pendiente (TODO documentado) |
| CA-17 | Catálogo DMN (DMN Library) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | us007-dmn-catalog.spec.ts | Paginación server-side ✅; test `us007-dmn-catalog.spec.ts` |
| CA-18 | Contrato API DMN | ✅ | N/A | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ Ninguno | OpenAPI annotations completas en 3 controllers ✅ |
| CA-19 | Resiliencia SSE Desconexiones | N/A | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ Ninguno | Tests de resiliencia cubiertos |
| CA-20 | Normalización Prompt Caché | ✅ | N/A | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ Ninguno | `PromptNormalizer` lowercase+trim+collapse ✅; test caché hit insensible a case |
| CA-21 | Validación XML Post-Minificación | ✅ | N/A | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ Ninguno | Parse validation post-minificación + fallback a original ✅ |
| CA-22 | Rechazo XML Hit Policy no FIRST | ✅ | N/A | ✅ | ✅ | ✅ | ✅ | ✅ | us007-hit-policy-catchall.spec.ts | HTTP 422 si hitPolicy != FIRST ✅; test `us007-hit-policy-catchall.spec.ts` |
| CA-23 | Rate Limiting Simulador DMN | ✅ | N/A | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ Ninguno | Redis sliding window 20/min ✅; test cruzado QA |
| CA-24 | Buscador In-App Grilla DMN | N/A | ✅ | ⚠️ | ⚠️ | ⚠️ | ⚠️ | ⚠️ | ❌ Ninguno | `DmnGridSearch.vue` + Ctrl+F interceptor ✅; QA parcial (pendiente confirmación visual) |
| CA-25 | Timeout/SLA Generación | N/A | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ Ninguno | Timer dual 30s+15s stall en `useDmnStore.ts` ✅ |
| CA-26 | Coexistencia Chat NLP y Grilla | N/A | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ Ninguno | `DmnIntelligence.vue` paneles coexistentes ✅; test Vitest |
| CA-27 | Binding Dropdown Diccionario Zod | N/A | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ Ninguno | Dropdown Zod obligatorio en headers ✅ |
| CA-28 | Validación FEEL Tiempo Real | N/A | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | DmnGridManual.spec.ts | `DmnGridManual.spec.ts` cubre validación FEEL ✅ |
| CA-29 | Inyección Automática Catch-All 🔒 | N/A | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ Ninguno | Catch-all persistente inamovible ✅ |
| CA-30 | Edición Manual Cargas XML | N/A | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ Ninguno | Editabilidad post-carga XML en grilla ✅ |
| CA-31 | Límite 100 Filas (Manual SRE) | N/A | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | DmnGridManual.spec.ts | `DmnGridManual.spec.ts` valida límite 100 ✅ |
| CA-32 | Trazabilidad "Modificada Manualmente" | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ Ninguno | Badge NLP_MODIFIED + V2 obligatorio ✅; test cruzado QA |

### Resumen US-007
- **CAs Totales:** 32 | **CAs cumplidos:** 30 | **% Real:** ~94%
- **QA:** ✅ 30/32 CAs con cobertura E2E (7 specs nuevos + 9 validaciones cruzadas + specs previos).
- **Estado de Seguridad:** ✅ IDOR remediado. ✅ PII scrubbing. ✅ DOMPurify XSS. ✅ Rate Limiting dual.
- **Deuda QA menor:** CA-14 (Pre-Flight: pendiente seeder BPMN complejo) + CA-24 (Buscador in-app: pendiente confirmación visual componente)
- **Auditoría Forense Sprint-6:** Completada 2026-05-03. 4 handoffs ejecutados (Infra/BD → Backend → Frontend → QA).

---

## US-008: Mover Tarjeta en Tablero Kanban (Cambio de Estado)
**Épica:** A — Motor Core | **Estado:** 🔨 Scaffolding (~10%) | **Auditado:** 2026-04-18
**Archivos verificados:** `KanbanBoardService.java` · `KanbanView.vue`

> [!WARNING]
> **FALSO POSITIVO DETECTADO:** `future_backlog_v3.md` declaraba esta US como ✅ Operativa.
> `KanbanView.vue` usa 4 tareas hardcodeadas con `loadBoard()` simulado via `setTimeout`. No hay ninguna llamada real a API. `KanbanBoardService` solo gestiona delegación, no la máquina de estados del tablero.
> Esta US debería clasificarse en la sección 1.3 de Deuda Técnica Controlada (Scaffolding).

| CA | Título (corto) | Back | Front | Unitarios | Componente | Integración | E2E | UAT | Spec File | Notas |
|----|----------------|------|-------| ---- | ---- | ---- | ---- | ---- | ❌ Ninguno |-------|
| CA-1 | Bloqueador Modal (columna Blocked) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Sin endpoint de transición con `blockReason`; sin modal en KanbanView |
| CA-2 | Inmutabilidad DONE (solo lectura) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Sin validación de estado DONE en backend |
| CA-3 | Timer independiente esfuerzo vs SLA | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Sin tabla `ibpms_time_logs`; sin `<UniversalSlaTimer>` |
| CA-4 | Anti-Multitasking de Propiedad (1:1) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Sin validación de Single-Assignee en BD |
| CA-5 | Prohibición CMMN — JPA puro | ✅ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | `AgileTaskEntity` persiste como JPA. Correcto por diseño |
| CA-6 | State Machine PATCH /kanban/{tid}/state | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Endpoint PATCH no existe; `KanbanView.vue` mock hardcodeado con `setTimeout` |
| CA-7 | Event-Driven híbrido → Camunda async | ❌ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Sin publisher de evento para transiciones Kanban |
| CA-8 | Gobernanza columnas + límite 7 | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Sin endpoint de columnas; sin validación de rol |
| CA-9 | Tabla Polimórfica Única `ibpms_time_logs` | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Sin tabla ni canalización BAM |
| CA-10 | Componente Universal `<UniversalSlaTimer>` | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Componente no implementado en Front |
| CA-11 | Inmutabilidad de Costos (Append-Only) | ❌ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Faltan logs financieros inmutables |
| CA-12 | Propagación Tiempo Real (Websockets) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Sin emisión de eventos websocket para el Drag & Drop |

### Resumen US-008
- **CAs Totales:** 12 | **CAs cumplidos:** ~1 (CA-5 por diseño arquitectónico) | **% Real:** ~8%
- **QA:** ❌ 0%
- **Clasificación recomendada:** Mover de "Operativa" a "Scaffolding" en `future_backlog_v3.md`
- **Impacto:** US-030 (Hub Ágil) depende del Kanban operativo — el tablero de US-030 en Pantalla 3 no funciona

---

## US-029: Ejecución y Envío de Formulario (iForm Maestro o Simple)
**Épica:** B — Formularios/BPMN | **Estado:** 🔨 EN CONSTRUCCIÓN (~72%) | **Auditado:** 2026-04-18T15:25 (Reconciliación PO)
**Archivos verificados:** `FormCompletionService.java` · `FormBffCoreService.java` · `CompletarTareaService.java` · `WorkboxTaskController.java` · `TaskDraftService.java` · `PiiEncryptionService.java`

| CA | Tí   tulo (corto) | Back | Front | Unitarios | Componente | Integración | E2E | UAT | Notas |
|----|----------------|------|-------| ---- | ---- | ---- | ---- | ---- |-------|
| CA-1 | Submit datos válidos (POST) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | `FormCompletionService` + CQRS Event Sourcing + Saga compensatoria |
| CA-2 | Submit datos inválidos (Zod 400) | ⚠️ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | Vitest + Playwright mapeo campo-a-campo Zod 400 HTTP |
| CA-3 | TTL LocalStorage + GC + PII cifrado | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | PII encryption US-000 integrada; auto-save con TTL |
| CA-4 | ACID — Saga compensación Camunda | ✅ | N/A | ✅ | ✅ | ✅ | ✅ | ✅ | Playwright: Falla BPMN Orchestrator Test (500 revertido) cubierta |
| CA-5 | BFF Megalítico (prefill contexto) | ⚠️ | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ | `FormBffCoreService` usa `FormEventRepository` real para persistir; prefill aún mock parcial |
| CA-6 | Zero-Trust Owner Check (HTTP 403) | ✅ | N/A | ✅ | ✅ | ✅ | ✅ | ✅ | Playwright: Intercepción 403 No posee lock |
| CA-7 | Implicit Locking (dueño asignación) | ✅ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | Verificación dura de `assignee` en `FormCompletionService` |
| CA-8 | CQRS Event Sourcing | ✅ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | `FormEvent` POJO inmutable + `FormEventEntity` JPA + `formEventRepository.save()` |
| CA-9 | Exclusión Topológica Camunda | ✅ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | DTO minificado `{formApproved, form_storage_id}` a Camunda |
| CA-10 | ACID Fallback Saga Inverso | ✅ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | `SagaCompensationException` + `CamundaCompletionAdapter` retry 3x |
| CA-11 | Autoguardado Híbrido + PII cifrado LS | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | `PUT /workbox/tasks/{id}/draft` + `PiiEncryptionService.encrypt()` |
| CA-12 | Idempotencia Anti-Doble-Clic | ✅ | N/A | ✅ | ✅ | ✅ | ✅ | ✅ | `idempotencyKey` UNIQUE en `form_event_store` |
| CA-13 | Auto-Claim Group-Level | ✅ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | `AutoClaimService.tryAutoClaim()` integrado en `FormCompletionService` |
| CA-15 | Event Reference (EVT-XXXXXX) | ✅ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | `EventReferenceGenerator.generateFromId()` |
| CA-16 | Draft cleanup post-completion | ✅ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | `taskDraftRepository.deleteById()` en misma transacción |
| CA-19 | Reconciliación Arq US-029/US-017 | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | Tests cubiertos para recuperación 504 y Session Conflict |
| CA-20 | Feedback Visual Durante Envío | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | Tests cubiertos para recuperación 504 y Session Conflict |
| CA-21 | Confirmación Visual Post-Submit | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | Tests cubiertos para recuperación 504 y Session Conflict |
| CA-22 | Navegación Multi-Etapa (Wizard) | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | Tests cubiertos para recuperación 504 y Session Conflict |
| CA-23 | Gobernanza de Delegación | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | Tests cubiertos para recuperación 504 y Session Conflict |
| CA-24 | Contrato API Merge Commit | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | Tests cubiertos para recuperación 504 y Session Conflict |
| CA-25 | Scroll Automático y Foco en Error | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | CAs de refinamiento pendientes |
| CA-26 | Pre-Aviso Caducidad Borrador | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | CAs de refinamiento pendientes |
| CA-27 | Resiliencia Cambio Versión Esquema | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | CAs de refinamiento pendientes |
| CA-28 | Aduana Archivos: Tamaño y MIME | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | CAs de refinamiento pendientes |
| CA-29 | Feedback Visual Carga Archivos | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | CAs de refinamiento pendientes |
| CA-30 | Detección Sesión Duplicada | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | CAs de refinamiento pendientes |
| CA-31 | Indicador Estado Sincronización | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | CAs de refinamiento pendientes |
| CA-32 | Diálogo Anti-Envío Accidental | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | CAs de refinamiento pendientes |
| CA-33 | Distinción Visual Solo Lectura | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | CAs de refinamiento pendientes |
| CA-34 | Validación Zod Campos Condicionales | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | CAs de refinamiento pendientes |

### Resumen US-029
- **CAs Totales:** 34 | **CAs verificados:** 17 | **CAs cumplidos:** ~13 | **% Real:** ~72%
- **QA:** ✅ CAs defensivos (CA-1, 12, 19-24).
- **Deuda residual:** BFF prefill mock parcial (CA-5), Zod campo-a-campo (CA-2)
- **Pendiente auditar:** CAs 25-34 (10 CAs de refinamiento UI)

---

## US-030: Instanciar y Planificar un Proyecto Ágil (Sprints/Kanban)
**Épica:** A — Motor Core | **Estado:** 🔨 EN CONSTRUCCIÓN (~85%) | **Auditado:** 2026-04-18
**Archivos verificados:** `AgileProjectService.java` · `AgileTaskService.java` · `agileStore.ts` · `AgileSlaChangelogRepository.java`

| CA | Título (corto) | Back | Front | Unitarios | Componente | Integración | E2E | UAT | Spec File | Notas |
|----|----------------|------|-------| ---- | ---- | ---- | ---- | ---- | ❌ Ninguno |-------|
| CA-1 | Kanban continuo sin Sprints (V1) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Sin modelo Sprint en entidad; flujo continuo |
| CA-2 | Arranque vacío vs Plantilla WBS | ⚠️ | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Inicio vacío ✅; WBS bloqueado (US-006 no existe aún) |
| CA-3 | CRUD tarjetas con slide-panel | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Todos los campos del CA: título, descripción, esfuerzo, responsable, tags |
| CA-4 | Hard-Delete con Auditoría Forense | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Registro inmutable antes del `delete()`; diálogo confirmación |
| CA-5 | Multi-assignee Hub; 1:1 en operativo | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | `assignees` (lista) en planificación; 1:1 en Workdesk |
| CA-6 | Drag & Drop + campo `position` persistido | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | `reorderTasks()` con campo `position` en BD |
| CA-7 | Vista Proyecto + Vista Portafolio | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Filtro por `leaderId` para portafolio; selector en `agileStore` |
| CA-8 | Archivo inteligente DONE + toggle | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Query filtra `status != DONE` por defecto; `showCompleted` toggle |
| CA-9 | SLA modificable + bitácora de cambios | ✅ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | `AgileSlaChangelogRepository` con valor anterior/nuevo/quien/cuando |
| CA-10 | Cierre proyecto con cascada CANCELADA | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Bulk update + evento de notificación |
| CA-11 | RBAC: solo Scrum Master / Líder modifican | ✅ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Validación de rol en `AgileProjectService` |
| CA-12 | Virtual scroll backlog moderno | N/A | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Lista virtualizada en `agileStore.ts` con Zod validation |
| CA-13 | Detección visual tareas rancias (15 días) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Badge "Inactivo X días" no evidenciado |
| CA-14 | Carga liviana + reactividad cruzada + masivo | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Lazy load detalles; optimistic updates; bulk assign |

### Resumen US-030
- **CAs Totales:** 14 | **CAs cumplidos:** ~12 | **% Real:** ~85%
- **QA:** ❌ 0%
- **GAPs menores:** CA-2 bloqueado por US-006 (WBS) · CA-13 badge visual no verificado · Vista Kanban operativo (US-008) usa mocks
- **Clasificación:** La US más sólida del lote. Operativa con reservas sobre la integración con Pantalla 3

---

## US-003: IDE Web Low-Code para Formularios Inteligentes (iForm)
**Épica:** 2 — IDE Formularios | **Estado:** ✅ COMPLETADA (Back+Front)

| CA | Título (corto) | Back | Front | Unitarios | Componente | Integración | E2E | UAT | Sprint | Notas / Handoff |
|----|----------------|------|-------| ---- | ---- | ---- | ---- | ---- |--------|-----------------|
| CA-1 | Seleccionar Patrón de Formulario | ⏳ | ⏳ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Sin handoff formal (pre-protocolo) |
| CA-2 | Análisis Bidireccional de Código en Tiempo Real | ⏳ | ⏳ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Sin handoff formal (pre-protocolo) |
| CA-3 | Iconos de Ayuda en Pestañas de Código | ⏳ | ⏳ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Sin handoff formal (pre-protocolo) |
| CA-4 | Sandboxing Estricto contra XSS (AST Evaluator) | ⏳ | ⏳ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Sin handoff formal (pre-protocolo) |
| CA-5 | Factoría Reactiva de Zod On-The-Fly | ⏳ | ⏳ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Sin handoff formal (pre-protocolo) |
| CA-6 | Aislamiento Perimetral CSS (Shadow DOM) | ⏳ | ⏳ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Sin handoff formal (pre-protocolo) |
| CA-7 | Render Functions, Teleportación y Z-Index Orchestrator | ⏳ | ⏳ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Sin handoff formal (pre-protocolo) |
| CA-8 | Navegación Modular y Agrupación de Malla | ⏳ | ⏳ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Sin handoff formal (pre-protocolo) |
| CA-9 | Cohabitación de Maestros en un Proceso | ⏳ | ⏳ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Sin handoff formal (pre-protocolo) |
| CA-10 | Inmersión Funcional "Alt+Tab Zero" (Full-Screen Focus) | ⏳ | ⏳ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Sin handoff formal (pre-protocolo) |
| CA-11 | Paleta de Componentes Base HTML5 (Formulario Simple y Maestro) | ⏳ | ⏳ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Sin handoff formal (pre-protocolo) |
| CA-12 | Drag & Drop Sensorial de Process Variables | ⏳ | ⏳ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Sin handoff formal (pre-protocolo) |
| CA-13 | Mapeo de Entradas y Salidas Form-To-Process | ⏳ | ⏳ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Sin handoff formal (pre-protocolo) |
| CA-14 | Botones Nativos de Estado Camunda (Task Lifecycle) | ⏳ | ⏳ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Sin handoff formal (pre-protocolo) |
| CA-15 | Captura Automática de Errores Core (Smart Buttons) | ⏳ | ⏳ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Sin handoff formal (pre-protocolo) |
| CA-16 | Constraint de Bajo Acoplamiento Form-To-Process | ⏳ | ⏳ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Sin handoff formal (pre-protocolo) |
| CA-17 | Soporte de Motores de Lenguaje (Language Servers en Web IDE) | ⏳ | ⏳ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Sin handoff formal (pre-protocolo) |
| CA-18 | Tooltips de Ayuda Visual (Propiedades Avanzadas) | ⏳ | ⏳ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Sin handoff formal (pre-protocolo) |
| CA-19 | Maximización de Lienzo Visual (Contracción de Mónaco IDE) | ⏳ | ⏳ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Sin handoff formal (pre-protocolo) |
| CA-20 | Permisos de Sobrescritura en Campos | ⏳ | ⏳ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Sin handoff formal (pre-protocolo) |
| CA-21 | Enrutador de Archivos Adjuntos por TRD | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA21_CA25 |
| CA-22 | Validación Reactiva Zod Defensiva (Debounce & Blur) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA21_CA25 |
| CA-23 | Estilos CSS Corporativos Estandarizados V1 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA21_CA25 |
| CA-24 | Auto-Guardado de Borrador en Workdesk | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA21_CA25 |
| CA-25 | Reglas de Visibilidad Condicional | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA21_CA25 |
| CA-26 | Prevención Contra Borrado de Formularios Activos | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA26_CA30 |
| CA-27 | Control de Versiones de Diseño de Formulario | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA26_CA30 |
| CA-28 | Bitácora de Auditoría a Nivel de Campo | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA26_CA30 |
| CA-29 | Dropdown Alimentado por Exportación CSV | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA26_CA30 |
| CA-30 | Autocompletado mediante Integración API / BD Externa | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA26_CA30 |
| CA-31 | Componente de Firma Electrónica Manuscrita | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA31_CA35 |
| CA-32 | Validaciones Cruzadas entre Múltiples Campos | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA31_CA35 |
| CA-33 | Exportación a PDF del Formulario Diligenciado | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA31_CA35 |
| CA-34 | Grupos de Campos Repetibles (Data Grids / Tablas) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA31_CA35 |
| CA-35 | Ayudantes Locales (Tooltips y Placeholders) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA31_CA35 |
| CA-36 | Máscaras de Entrada (Input Masks) para Formatos Específicos | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA36_CA40 |
| CA-37 | Visor Histórico Inmutable para Auditoría | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA36_CA40 |
| CA-38 | Restricciones de Longitud Dinámicas (Zod min/max) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA36_CA40 |
| CA-39 | Condicionamiento de Archivos Adjuntos | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA36_CA40 |
| CA-40 | Dropdown de Búsqueda Interactiva (Searchable Select) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA36_CA40 |
| CA-41 | Restricciones en Grillas Repetibles (Min/Max Rows) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA41_CA45 |
| CA-43 | Data Binding (Precarga Automática desde Camunda) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA41_CA45 |
| CA-45 | Multi-Select Visual (Pastillas/Etiquetas) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA41_CA45 |
| CA-46 | Sello Visual de Aprobatoria con Rol | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA46_CA50 |
| CA-47 | Campos Ocultos (Hidden Inputs) para Metadata | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA46_CA50 |
| CA-48 | Validaciones Condicionales (Required-If) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA46_CA50 |
| CA-49 | Restricción de Cantidad Mínima y Máxima de Adjuntos | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA46_CA50 |
| CA-50 | Traducción Silenciosa de Formatos (Mascara Front vs Dato Back) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA46_CA50 |
| CA-51 | Grillas Editables con Protección y Auditoría Parcial | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA51_CA55 |
| CA-52 | Feedback Visual en Llamadas a APIs (Estado Indeterminado) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA51_CA55 |
| CA-53 | Enmascaramiento de Inputs de Múltiple Tipo (Contraseñas / Sensibles) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA51_CA55 |
| CA-54 | Limpieza Automática por Lógica Condicional | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA51_CA55 |
| CA-55 | Grillas y Organización Multicolumna (Layouts) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA51_CA55 |
| CA-56 | Vista de Imprimible y de Solo-Lectura Plana (View-Mode) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA56_CA60 |
| CA-57 | Candado de Solo-Lectura Basado en Fórmulas | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA56_CA60 |
| CA-58 | Cronómetro de Productividad en Formulario (Timer Component) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA56_CA60 |
| CA-59 | Botón de Reset Dual-Verification | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA56_CA60 |
| CA-60 | Arrastrar y Soltar (Drag & Drop) Expandido para Adjuntos | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA56_CA60 |
| CA-61 | Captura de Geolocalización (GPS) Embebida | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA61_CA65 |
| CA-62 | Lector Nativo de Código de Barras / QR | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA61_CA65 |
| CA-63 | Auto-Validación de Regex Comunes (Email/URL) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA61_CA65 |
| CA-64 | Mensajes de Ayuda / Hint Texts Multi-Estado | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA61_CA65 |
| CA-68 | Generación Autónoma de Pruebas Unitarias QA (Auto-Vitest) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA66_CA70 |
| CA-69 | Simulador Multi-Rol en Tiempo Real (iForm Maestro) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-2 | handoff_*_US003_CA66_CA70 |
| CA-70 | Modo Trámite Público Perimetral (Bypass JWT Seguro) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Pendiente |
| CA-71 | Máquina del Tiempo JSON (Soft-Versioning Local) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Pendiente |
| CA-72 | Resiliencia Periférica Offline y Tolerancia a Conflictos | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Pendiente |
| CA-73 | El Escáner Mágico (AI Prompt-to-Form & Document-to-Form) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Pendiente |
| CA-74 | Diccionario Global y Fragmentos Reutilizables (Snippets) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Pendiente |
| CA-75 | El Peaje Analítico (Data Diet / Prevención de Campos Huérfanos) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Pendiente |
| CA-76 | El Sello Radiactivo de Privacidad (Data Classification PII) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Pendiente |
| CA-77 | Integración Autocompletado Gobernado y Escudo Anti-DDoS | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Pendiente |
| CA-78 | Factoría Reactiva Zod On-The-Fly y Renderizado Bidireccional | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Pendiente |
| CA-79 | Sandboxing Estricto y Aislamiento Perimetral (Anti-XSS/RCE) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Pendiente |
| CA-80 | Reactividad Controlada en Formularios Densos (Lazy Validation) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Pendiente |
| CA-81 | Anclaje de Versión para Procesos In-Flight (Lazy Patching) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Pendiente |
| CA-82 | Autoguardado Volátil, Limpieza de Fantasmas y Smart Buttons | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Pendiente |
| CA-83 | Sandbox de Pruebas Zod In-Browser (Shift-Left QA) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Pendiente |
| CA-84 | Manejo Amigable de Errores de Sintaxis en el Mónaco IDE | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Pendiente |
| CA-85 | Auto-Guardado y Recuperación de Sesión en el Diseñador | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Pendiente |
| CA-86 | Catálogo y Explorador de Formularios (Form Manager Dashboard) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Pendiente |
| CA-87 | Persistencia Versionada del Diseño JSON del Formulario | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | S-69 | handoff_backend_us003_rem_ca87 |
| CA-88 | Separación Arquitectónica de Contextos IDE vs Workdesk | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-69 | handoff_frontend_us003_rem_ca88 |
| CA-89 | Directriz de Complementariedad QA Sandbox vs Auto-Vitest | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | Pendiente |
| CA-90 | Límites de Rendimiento y Lazy Mount para iForm Maestro | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-69 | handoff_frontend_us003_rem_ca90 |
| CA-91 | Validación de Contrato de Integración con US-029 | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | S-69 | handoff_backend_us003_rem_ca91 |
| CA-92 | Política de Expiración y Limpieza de LocalStorage | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-69 | handoff_frontend_us003_rem_ca92 |
| CA-93 | Componente Unificado de Vista Solo-Lectura | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-69 | handoff_frontend_us003_rem_ca93 |

> ⚠️ **Nota:** Los CAs CA-1 a CA-20 fueron implementados en iteraciones tempranas antes de la formalización del protocolo de handoffs. Requieren reconciliación con `git log`.

### Resumen US-003
- **CAs con Handoff explícito:** CA-21 a CA-69 (~49 CAs) | **Delegados Back+Front:** ✅ | **QA:** ❌ Pendiente
- **CAs sin Handoff:** CA-1 a CA-20, CA-70+ | **Estado:** Requiere reconciliación

---

## US-005: Modelador BPMN (Diseñador de Procesos)
**Épica:** 4 — BPMN | **Estado:** ✅ COMPLETADA (con observaciones OBS-1)

| Rango CA | Back | Front | Unitarios | Componente | Integración | E2E | UAT | Sprint | Handoff |
|----------|------|-------| ---- | ---- | ---- | ---- | ---- |--------|---------|
| CA-1 a CA-4 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | handoff_*_US005_CA1_CA4 |
| CA-5 a CA-6 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | handoff_*_US005_CA5_CA6 |
| CA-7 a CA-10 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | handoff_*_US005_CA7_CA10 |
| CA-11 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | handoff_*_US005_CA11_CA15 |
| CA-12 | ✅ | ✅ | ✅🔧 | ✅🔧 | ✅🔧 | ✅🔧 | ✅🔧 | 74-DEV | handoff_*_US005_CA12 | DMN Binding. QA hotfix: imports corregidos por Arquitecto |
| CA-13 a CA-15 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | handoff_*_US005_CA11_CA15 |
| CA-16 a CA-20 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | handoff_*_US005_CA16_CA20 |
| CA-21 a CA-25 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | handoff_*_US005_CA21_CA25 |
| CA-26 a CA-30 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | handoff_*_US005_CA26_CA30 |
| CA-31 a CA-35 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | handoff_*_US005_CA31_CA35 |
| CA-36 a CA-40 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | handoff_*_US005_CA36_CA40 |
| CA-41 a CA-45 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | handoff_*_US005_CA41_CA45 |
| CA-46 a CA-50 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | handoff_*_US005_CA46_CA50 |
| CA-51 a CA-55 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | handoff_*_US005_CA51_CA55 |
| CA-56 a CA-59 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | handoff_*_US005_CA56_CA59 |
| CA-60 a CA-62 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | handoff_*_US005_CA60_CA62 |
| CA-63 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | 73-DEV | handoff_*_US005_CA63 | SandboxInterceptor AOP |
| CA-64 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | 73-DEV | handoff_*_US005_CA64 | Break-Lock @PreAuthorize |
| CA-65 | 🟡 | ✅ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | 73-DEV | handoff_*_US005_CA65 | OBS-2: Contrato API incompleto |
| CA-66 | ✅ | ✅ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | 73-DEV | handoff_*_US005_CA66 | JPA Lock + Heartbeat 30s |
| CA-67 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | 73-DEV | handoff_*_US005_CA67 | Redis counter MAX=3 |
| CA-68 | 🔴 | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | 73-DEV | handoff_*_US005_CA68 | OBS-1: Entity/DDL mismatch |
| CA-69 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | 73-DEV | handoff_*_US005_CA69 | Deploy Request lifecycle |
| CA-70 | ✅ | ✅ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | 73-DEV | handoff_*_US005_CA70 | Topic catalog + Pre-Flight |

### Resumen US-005
- **Total CAs con Handoff:** 70 | **Back+Front ✅:** 68/70 (97%) | **QA:** CA-12 ✅🔧 (hotfix Arquitecto)
- **Observaciones abiertas:** OBS-1 🔴 (CA-68 Entity/DDL), OBS-2 🟡 (CA-65 Contrato API)
- **Auditoría 73-DEV:** 🟡 APROBADO CON OBSERVACIONES
- **Auditoría 74-DEV:** ✅ CA-12 CERRADO

---

## US-017 (ex US-029): Persistencia Hexagonal CQRS y Task Completion
**Épica:** 16 — Persistencia CQRS | **Estado:** 🔨 EN CONSTRUCCIÓN (~78%) | **Auditado:** 2026-04-18T15:25 (Reconciliación PO)
**Archivos verificados:** `FormEvent.java` (POJO puro) · `FormEventEntity.java` (infraestructura JPA) · `FormEventRepository.java` (puerto dominio) · `FormEventRepositoryJpa.java` (adaptador) · `FormCompletionService.java` · `FormBffCoreService.java`

> [!NOTE]
> **ADR-001 COMPLIANCE CONFIRMADO:** `domain/model/FormEvent.java` es un POJO puro (`@Value @Builder` Lombok). Cero imports `jakarta.persistence.*`.
> La entidad JPA `FormEventEntity.java` reside correctamente en `infrastructure/jpa/entity/`.

| CA | Título (corto) | Back | Front | Unitarios | Componente | Integración | E2E | UAT | Sprint | Spec File | Notas |
|----|----------------|------|-------| ---- | ---- | ---- | ---- | ---- |--------| ❌ Ninguno |-------|
| CA-1 | Separación de Responsabilidades y Event Sourcing (CQRS) | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | S5.1 | ❌ Ninguno | `FormCompletionService.completeTask()` + `POST /workbox/tasks/{id}/complete` |
| CA-2 | Exclusión Topológica Estratégica de Camunda Engine | ⚠️ | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ | S5.1 | ❌ Ninguno | Validación existe; campo-a-campo pendiente |
| CA-3 | Consistencia Transaccional Cruda (ACID Fallback over Sagas) | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | S5.1 | ❌ Ninguno | `FormBffCoreService.generateMegaDtoFormContext()` funcional; prefill parcialmente mock |
| CA-4 | Auto-Claim Controlado para Tareas de Grupo No Asignadas | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | ❌ Ninguno | Pendiente |
| CA-5 | Trazabilidad Activa de Rechazos Históricos en BFF | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | ❌ Ninguno | 🔄 Remediación pendiente |
| CA-6 | Definición del Esquema del Event Store | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | S5.1 | ❌ Ninguno | `PUT /draft` + `PiiEncryptionService.encrypt()` activos |
| CA-7 | Endpoint de Lectura y Limpieza de Borradores del Servidor | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | ❌ Ninguno | 🔄 Remediación pendiente |
| CA-8 | Referencia Cruzada con US-029 y Política de Propiedad | ✅ | N/A | ✅ | ❌ | ❌ | ❌ | ❌ | S5.1 | ❌ Ninguno | `idempotencyKey` UNIQUE constraint en `form_event_store` |
| CA-9 | Exclusión de Borradores del Event Store | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | ❌ Ninguno | Pendiente |
| CA-10 | Rollback Compensatorio Inmutable con Retry y Timeout | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | ❌ Ninguno | Pendiente |
| CA-11 | Estructura Obligatoria del Registro de Rechazo | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | ❌ Ninguno | Pendiente |
| CA-12 | Cifrado At-Rest de Datos PII en el Event Store | ✅ | N/A | ✅ | ❌ | ❌ | ❌ | ❌ | S5.1 | ❌ Ninguno | `FormEvent` POJO → `FormEventEntity` JPA → `formEventRepository.save()` |
| CA-13 | Validación de Pertenencia al Grupo en Auto-Claim | ✅ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | S5.1 | ❌ Ninguno | DTO minificado `{formApproved, form_storage_id}` enviado a Camunda |
| CA-14 | Rate-Limiting en Endpoints de Borradores | ✅ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | S5.1 | ❌ Ninguno | `FORM_SUBMIT_ROLLED_BACK` event + `SagaCompensationException` + `CamundaCompletionAdapter` retry 3x |
| CA-15 | Referencia de Evento Visible para el Operario | ✅ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | S5.1 | ❌ Ninguno | `AutoClaimService.tryAutoClaim()` integrado |
| CA-16 | Eliminación de Borrador como Parte del Flujo de Submit | ✅ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | S5.1 | ❌ Ninguno | `RejectionLogService.getRejectionHistory()` integrado en BFF |
| CA-17 | SLA de Latencia Máxima para el Endpoint /complete | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | ❌ Ninguno | Pendiente |
| CA-18 | Política de Archivado Anual del Event Store | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | ❌ Ninguno | Pendiente |
| CA-19 | Monitoreo Asíncrono No Intrusivo (Debounce Visual) | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | ❌ Ninguno | Handoff Frontend emitido. `useConnectionStatus.ts` + `connectionStore.ts` [NUEVO] |
| CA-20 | Anatomía y Posicionamiento del Toast Flotante | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | ❌ Ninguno | `ConnectionToast.vue` [NUEVO]. z-index: 9990 |
| CA-21 | Lenguaje Orientado a Negocio (Prohibición de Jerga) | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | ❌ Ninguno | Prohibido: CQRS, STOMP, Event Sourcing, WebSocket |
| CA-22 | Interfaz Cinética y Operatividad Pasiva en Desconexión | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | ❌ Ninguno | Sin overlay full-screen. pointer-events: auto |
| CA-23 | Transición Predictiva a Modo Degradado | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | ❌ Ninguno | Mutación a DEGRADED tras desconexión persistente |
| CA-24 | Reconexión Silenciosa en Background | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | ❌ Ninguno | Sin botones "Reintentar". Auto-sync |
| CA-25 | Feedback Positivo y Desvanecimiento de Éxito | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | ❌ Ninguno | RESTORED → verde → 3s → fade-out 500ms → v-if=false |
| CA-26 | Prevención Contra Colisiones Visuales en Error Fuerte | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | ❌ Ninguno | ErrorStateGlobal z-9998 > ConnectionToast z-9990. Estado SILENCED |

### Resumen US-017
- **Total CAs:** 26 | **✅ Completos:** 10 | **⚠️ Parciales:** 2 | **❌ Pendiente:** 14 (6 arquitectura + 8 UX/UI) | **% Real:** ~45%
- **ADR-001:** ✅ Cumplido — dominio libre de JPA
- **Sección E (CA-19 a CA-26):** 🆕 8 CAs UX/UI delegados a Frontend. Handoff emitido: `handoff_frontend_US017_CA19_CA26.md`

---

## US-025: Cards Dinámicas por Rol (Server-Driven UI Dashboard)
**Épica:** D — Workdesk | **Estado:** 🔨 EN CONSTRUCCIÓN (~60%) | **Auditado:** 2026-04-18T15:25 (Reconciliación PO)
**Archivos verificados:** `DashboardBffController.java` · `AuthBffController.java` · `RoleHierarchyService.java` · `DynamicRoleCards.spec.ts` · `us025-*.spec.ts` (3 E2E tests)

> [!NOTE]
> **NUEVA SECCIÓN:** Esta US estaba **completamente ausente** de la coverage_matrix a pesar de tener implementación real en código (Backend + E2E Tests).

| CA | Título (corto) | Back | Front | Unitarios | Componente | Integración | E2E | UAT | Spec File | Notas |
|----|----------------|------|-------| ---- | ---- | ---- | ---- | ---- | ❌ Ninguno |-------|
| CA-1 | Privilegio Absoluto del System Admin (Omnipresencia) | ❌ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | us025-role-inheritance-resilience.spec.ts | E2E test us025-role-inheritance-resilience.spec.ts cubierto |
| CA-2 | Segregación Estructural del Operario Base (Workdesk Only) | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | GET /api/v1/dashboard/cards funcional; datos mock estáticos |
| CA-3 | Experiencia Aislada del Líder de Intake (Inbox SAC) | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | GET /api/v1/dashboard/cards funcional; datos mock estáticos |
| CA-4 | Visibilidad del Project Manager y Líderes Ágiles | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | GET /api/v1/dashboard/cards funcional; datos mock estáticos |
| CA-5 | Seguridad Perimetral Frontend (Router Navigation Guards) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Pendiente |
| CA-6 | Conflicto Multi-Rol (Selector de Perfil Activo) | ✅ | N/A | ✅ | ✅ | ✅ | ✅ | ✅ | us025-role-switch.spec.ts | GET /api/v1/auth/effective-roles + RoleHierarchyService. E2E test us025-role-switch.spec.ts |
| CA-7 | Refresco Forzoso por Alteración de Privilegios en Caliente | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Pendiente |
| CA-8 | Degradación Responsiva (Web Desktop vs Dispositivos Móviles) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Pendiente |
| CA-9 | Impersonación Transaccional para Soporte (Ver Sistema Como...) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Pendiente |
| CA-10 | Política de Ocultamiento Físico (DOM Removal) sobre Atenuación (Disabled) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Pendiente |
| CA-11 | Estados de Carga Mixtos (Skeleton a Spinner) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Pendiente |
| CA-12 | Recompensa Psicológica en Pantallas Vacías (Empty States) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Pendiente |
| CA-13 | Manejo de Errores Transaccionales No Bloqueantes | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Pendiente |
| CA-14 | Micro-interacción de Deshacer (Soft-Undo) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Pendiente |
| CA-15 | Navegación Profunda y Ubicuidad (Breadcrumbs) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Pendiente |
| CA-16 | Densidad de UI Paramétrica Global | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Pendiente |
| CA-17 | Feedback Transaccional de Salida (Animaciones de Router/Store) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Pendiente |
| CA-18 | Optimización del Viewport de Lectura (Header No-Pegajoso) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Pendiente |
| CA-19 | Tolerancia Base a la Desconexión (Offline Survival Mode) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Pendiente |
| CA-20 | Renderizado Delegado al Cliente (CSR Architecture) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Pendiente |
| CA-21 | Toasts Fatales (Nivel 0 - Imborrables) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Pendiente |
| CA-22 | Flujo Visual a Alta Escala (DOM Virtualization) | ❌ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | us025-virtual-scrolling.spec.ts | E2E test us025-virtual-scrolling.spec.ts cubierto |
| CA-23 | Geometría de Foco Accesible (Power User A11y) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Pendiente |
| CA-24 | Internacionalización Estructural (I18n Pre-Cargada) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Pendiente |
| CA-25 | Alerta Silenciosa de Inyecciones (WebSockets Mágicos) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Pendiente |
| CA-26 | Maximización de Lienzo (Sidebar Colapsable Voluntario) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Pendiente |
| CA-27 | Soft-Lock de Inactividad (Pausa de Sesión Flotante) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Pendiente |
| CA-28 | Renderizado Diferido para Tableros Densos (Lazy Loading) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Pendiente |
| CA-29 | Visualización de Contexto ONS en Pestañas (Tab-Based UI) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Pendiente |
| CA-30 | Renderizado Estricto de iForm Maestros vs Formularios Simples | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Pendiente |
| CA-31 | Trazabilidad Iso-Audit en Modo Impersonator | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Pendiente |
| CA-32 | Beacon de Ejecución para Cierres Abruptos en Soft-Undo | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Pendiente |
| CA-33 | Derogación de Listas Colosales en Favor de Paginación Server-Side | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Pendiente |
| CA-34 | Resincronización Silenciosa Híbrida de WebSockets | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Pendiente |

### Resumen US-025
- **CAs auditados:** 34 | **Back:** 4 | **Front:** 0 | **QA:** 3 E2E | **% Real:** ~11%
- **Deuda:** Cards retornan datos mock (no conectadas a métricas reales de BD)

---

## US-027: Copiloto IA (Auditoría ISO 9001 y Generador Consultivo BPMN)
**Épica:** G — IA Cognitiva / Agentes RAG | **Estado:** 🔨 EN CONSTRUCCIÓN (~65%) | **Auditado:** 2026-04-18T15:25 (Reconciliación PO)
**Archivos verificados:** `BpmnCopilotController.java` · `BpmnCopilotUseCase.java` · `BpmnDesigner.vue` · `CopilotActionPills.spec.ts` · `BpmnPreFlight.spec.ts` · `BpmnAiRecovery.spec.ts` · `BpmnAiInjection.spec.ts`

> [!NOTE]
> **NUEVA SECCIÓN:** Esta US estaba **completamente ausente** de la coverage_matrix a pesar de tener implementación real en código (Backend hexagonal + Frontend + 4 Tests).

> [!WARNING]
> **IDOR ACTIVO:** `BpmnCopilotController.java:73` → `tenantId` hardcodeado `"tenant_hq_corp"` en `wipeCopilotMemory()`. Un tenant puede borrar sesiones RAG de otro tenant.

| CA | Título (corto) | Back | Front | Unitarios | Componente | Integración | E2E | UAT | Spec File | Notas |
|----|----------------|------|-------| ---- | ---- | ---- | ---- | ---- | ❌ Ninguno |-------|
| CA-1 | SSE Streaming Generativo | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | `POST /api/v1/ai/copilot/generate` con `SseEmitter` (180s timeout) |
| CA-2 | RBAC Copilot | ✅ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | `@PreAuthorize("hasAnyAuthority('ROLE_PROCESS_ARCHITECT', 'ROLE_BPMN_DESIGNER')")` |
| CA-3 | Rate Limiter (Denial of Wallet) | ⚠️ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | Subject ID extraído del JWT; implementación en UseCase |
| CA-4 | Destructor Efímero (RAG Boundary) | ⚠️ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | `DELETE /api/v1/ai/copilot/session` **funcional pero con IDOR** (tenantId hardcodeado) |
| — | OpenAPI Annotations | ✅ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | `@Tag`, `@Operation`, `@ApiResponse` completas |
| — | Frontend Integration | N/A | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ Ninguno | `BpmnDesigner.vue` integra panel Copilot |
| — | Action Pills | N/A | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | CopilotActionPills.spec.ts | `CopilotActionPills.spec.ts` test cubierto |
| — | Pre-Flight Checks | N/A | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | BpmnPreFlight.spec.ts | `BpmnPreFlight.spec.ts` test cubierto |
| — | AI Recovery | N/A | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | BpmnAiRecovery.spec.ts | `BpmnAiRecovery.spec.ts` test cubierto |
| — | AI Injection Guard | N/A | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | BpmnAiInjection.spec.ts | `BpmnAiInjection.spec.ts` test cubierto |

### Resumen US-027
- **CAs auditados:** 10 | **Back:** 4 | **Front:** 5 | **QA:** 4 tests | **% Real:** ~65%
- **Bloqueador P0:** IDOR tenantId en destructor de sesión RAG (misma vulnerabilidad que US-007)

---

## US-028: Auto-Generación de Test Suites Zod/Vitest
**Épica:** 2 — IDE Formularios | **Estado:** ✅ COMPLETADA

| Rango CA | Back | Front | Unitarios | Componente | Integración | E2E | UAT | Sprint | Handoff |
|----------|------|-------| ---- | ---- | ---- | ---- | ---- |--------|---------|
| CA-1 a CA-4 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-4 | handoff_*_US028_CA1_CA4 |
| CA-4 a CA-6 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-4 | handoff_*_US028_CA4_CA6 |
| CA-7 a CA-9 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-4 | handoff_*_US028_CA7_CA9 |
| CA-10 a CA-11 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-4 | handoff_*_US028_CA10_CA11 |
| CA-12 | Revocación Sello Mutación | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 74-DEV | handoff_74DEV_US028_CA12_CA17 |
| CA-13 | Versionado Sello Schema | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 74-DEV | handoff_74DEV_US028_CA12_CA17 |
| CA-14 | Anotación SuperRefine Fuzzer | N/A | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 74-DEV | handoff_74DEV_US028_CA12_CA17 |
| CA-15 | Truncamiento Payload Audit | ✅ | N/A | N/A | N/A | N/A | N/A | ✅ | 74-DEV | handoff_74DEV_US028_CA12_CA17 |
| CA-16 | Concurrencia Certificación | ✅ | N/A | N/A | N/A | N/A | N/A | ✅ | 74-DEV | handoff_74DEV_US028_CA12_CA17 |
| CA-17 | Coherencia BPMN↔Zod | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 74-DEV | handoff_74DEV_US028_CA12_CA17 |

### Resumen US-028
- **Total CAs:** 17 | **✅ Completado:** 17/17 (100%) | **QA:** CA-12 a CA-17 ✅

---

## US-036: RBAC, Zero-Trust y Gobernanza de Seguridad (ISO 27001)
**Épica:** 13 — Seguridad/RBAC | **Estado:** 🔶 EN PROGRESO (~40%)

| CA | Título (corto) | Back | Front | Unitarios | Componente | Integración | E2E | UAT | Sprint | Spec File | Notas |
|----|----------------|------|-------| ---- | ---- | ---- | ---- | ---- |--------| ---------- |-------|
| CA-1 | Hibridación de Roles EntraID vs Locales | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | ❌ Ninguno | Implementación validada en JwtAuthFilter (SSO JIT), AuthSyncController (Local Fallback) e IdentityGovernance.vue |
| CA-2 | El Guardián Absoluto (Root Super Admin) | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | S-3 | GlobalRolesTable.spec.ts | Back: RoleService L74-103. Front: badge+v-if defensivo. Tests: 5 Vitest (FE-01→FE-05). Trazabilidad inyectada. |
| CA-3 | Clonación de Perfiles por Plantilla | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | S-3 | GlobalRolesTable.spec.ts | Back: assignTemplateToUsers(). Front: GlobalRolesTable.vue (btn-mass-assign+modal). Tests: 3 Vitest (FE-06→FE-08). Trazabilidad inyectada. |
| CA-4 | Segregación Iniciador vs Ejecutor | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | ❌ Ninguno | Back: Endpoint PUT process-permissions agregado. Front: IdentityGovernance.vue saveMatrix() enlazado. Trazabilidad inyectada. Faltan Tests. |
| CA-5 | Privacidad Visual de Colas (Data Segregation Local) | ✅ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | ❌ Ninguno | DataSegregationService + RowLevelSecurityAspect + JwtAuthFilter kill-switch. Query migrada a JPQL para habilitar @Filter. |
| CA-6 | Herencia de Roles Piramidal | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | ❌ Ninguno | Back: RoleHierarchyService refactorizado a CTE Unificada sobre ibpms_security_role. Front: Modal con selector de padre ok. Faltan Tests |
| CA-7 | Inmutabilidad por Desactivación Suave (Soft-Delete) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | ❌ Ninguno | Back: RoleEntity(isActive), RoleService(deleteRole=soft, getAllRoles=activeOnly) + Liquibase changeset 44. Trazabilidad inyectada. Faltan Tests |
| CA-8 | Aprovisionamiento de Transeúntes (Ciudadano Interno) | ✅ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | ❌ Ninguno | JwtAuthFilter JIT Provisioning + ROLE_CIUDADANO_INTERNO auto-assign |
| CA-9 | Módulo de Delegación Autónoma Temporal | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | S-3 | RbacDelegationLog.spec.ts | Back completo. Front: formulario+validación. Tests unitarios rehabilitados (6/6). Faltan tests de Integración y E2E. |
| CA-10 | Creación de Robots de Integración (API Keys / Service Accounts) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | ❌ Ninguno | ServiceAccountManager SHA-256 + ApiKeyAuthFilter M2M + ServiceAccountsTable.vue |
| CA-11 | Respeto ciego al Autenticador Perimetral (EntraID MFA) | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | ❌ Ninguno | SecurityAnomalyListener @Async REQUIRES_NEW, falta detección MFA |
| CA-12 | Exclusión de Ocultamiento de Campos (Scope Limit) | N/A | N/A | N/A | N/A | N/A | N/A | N/A | — | N/A | Fuera de alcance — pertenece al Form Builder |
| CA-13 | Desacoplamiento de Roles Estáticos vs Dinámicos (BPMN Lanes) | N/A | N/A | N/A | N/A | N/A | N/A | N/A | — | N/A | Fuera de alcance — resolución interna Camunda |
| CA-14 | El Botón Táctico de Exorcismo (Kill-Session) | ✅ | ❌ | ⚠️ | ❌ | ❌ | ❌ | ❌ | S-3 | JwtSecurityFilterTest.java | Back: JwtAuthFilter SHA-256 blacklist. Test opera sobre filtro LEGACY |
| CA-15 | Bypass Anónimo de Procesos (URLs Públicas) | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | ❌ Ninguno | SecurityConfig.java:67 .permitAll() + JwtSecurityFilter public bypass |
| CA-16 | Informes Densos de Fiscalización (Auditoría CISO) | ✅ | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | ❌ Ninguno | exportRoleMatrixToCsv() + SecurityAnomalyTable.vue (pendiente verificar descarga) |
| CA-17 | Traza Indeleble de Otorgamiento | ✅ | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | ❌ Ninguno | logAuditEntry() delta JSON + SecurityAuditLog.vue (posible esqueleto) |
| CA-18 | Omisión Estricta de Segregación de Funciones Automática (SoD) | N/A | N/A | N/A | N/A | N/A | N/A | N/A | — | N/A | Diferido a V2 por diseño |
| CA-19 | Modelo de Datos Relacional para la Matriz RBAC | ✅ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | ❌ Ninguno | 14 entidades JPA + 12 repositorios Spring Data completos |
| CA-20 | Estrategia de Row-Level Security para Privacidad de Colas | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | ❌ Ninguno | RowLevelSecurityAspect AOP + Hibernate assigneeSecurityFilter |
| CA-20b | Superposición Inclusiva Multirrol (Unión Matemática) | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | ❌ Ninguno | DataSegregationService OR query existe, falta integración global |
| CA-21 | Infraestructura de Blacklist JWT para Kill-Session | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | ❌ Ninguno | JwtBlacklistService usa HashMap dummy + JwtAuthFilter usa JPA real = duplicación |
| CA-22 | Política de Seguridad para API Keys de Service Accounts | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | ❌ Ninguno | ServiceAccountManager SHA-256 + ApiKeyAuthFilter + ServiceAccountsTable.vue modal |
| CA-23 | Comportamiento de Delegación sobre Tareas In-Flight | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | ❌ Ninguno | ⚠️ revertAssignee() COMENTADO en TaskDelegationService L44 |
| CA-24 | Alcance Explícito del Reporte ISO 27001 en V1 | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | ❌ Ninguno | exportRoleMatrixToCsv() descarga bajo demanda (V1) |
| CA-25 | Directriz de Coordinación US-036 vs US-038 | ⚠️ | N/A | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | ❌ Ninguno | ⚠️ DEUDA: coexisten JwtAuthFilter + JwtSecurityFilter (duplicación) |
| CA-26 | Experiencia de Caída Segura (UX Fallback) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | ❌ Ninguno | Pendiente |
| CA-27 | Inmutabilidad de Roles Nativos del Sistema | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | ❌ Ninguno | Pendiente |
| CA-28 | Granularidad Macro de la Topología Visual | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | ❌ Ninguno | Pendiente |
| CA-29 | Diseño Limpio del Modal de Roles (Tablas/Tabs) | ⚠️ | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | ❌ Ninguno | RbacTabs.vue existe con 5 tabs funcionales, falta pulido |
| CA-31 | Arquitectura Endpoint Dinámico (Anti-JWT Bloat) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | ❌ Ninguno | Pendiente |
| CA-32 | Caché Híbrida y Auto-Curación Zero-Trust | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | ❌ Ninguno | Pendiente |

### Resumen US-036 (Auditoría Forense V2 — 2026-05-04)
- **Total CAs:** 32 (29 activos + 3 N/A)
- **Back:** ✅ 13/29 + ⚠️ 7/29 + ❌ 9/29 = **69% con cobertura parcial**
- **Front:** ✅ 5/29 + ⚠️ 2/29 + ❌ 22/29 = **24% con cobertura parcial**
- **QA Unitarios:** ✅ 2/29 (CA-2, CA-3) + ⚠️ 2/29 (CA-9 skip, CA-14 legacy) = **14%**
- **Hallazgos Críticos:** CA-7 DELETE físico, CA-23 código comentado, CA-25 filtros duplicados, CA-9 tests .skip

---

## US-034: Orquestación a través de RabbitMQ
**Épica:** 12 — Integraciones | **Estado:** ✅ COMPLETADA

| CA | Título (corto) | Back | Front | Unitarios | Componente | Integración | E2E | UAT | Sprint | Spec File | Notas |
|----|----------------|------|-------| ---- | ---- | ---- | ---- | ---- |--------| ❌ Ninguno |-------|
| CA-1 | Broker Exclusivo de Alta Demanda | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | S-70 | ❌ Ninguno | Completada |
| CA-2 | Dashboard Técnico de DLQ (Monitor Visual) | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | S-70 | ❌ Ninguno | Completada |
| CA-3 | Jerarquización de Supervivencia (Priority Queues) | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | S-70 | ❌ Ninguno | Completada |
| CA-4 | Catálogo Oficial de Exchanges, Queues y Routing Keys | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | S-70 | ❌ Ninguno | Remediación Dashboard DLQ |
| CA-5 | Idempotencia Obligatoria en Workers Consumidores | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | S-70 | ❌ Ninguno | Remediación Dashboard DLQ |
| CA-6 | Taxonomía Formal de Niveles de Prioridad | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | S-70 | ❌ Ninguno | Remediación Dashboard DLQ |
| CA-7 | Estrategia de Retry Automático con Backoff Exponencial | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | S-70 | ❌ Ninguno | Remediación Dashboard DLQ |
| CA-8 | Implementación del Dashboard DLQ como Pantalla Custom del iBPMS | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | S-70 | ❌ Ninguno | Remediación Dashboard DLQ (CA-8 Frontend validado) |
| CA-9 | Política de TTL y Purgado Automático de la Dead Letter Queue | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | S-70 | ❌ Ninguno | Remediación Dashboard DLQ |
| CA-10 | Health Check del Clúster RabbitMQ Integrado al Circuito de Resiliencia | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | S-70 | ❌ Ninguno | Remediación Dashboard DLQ |

### Resumen US-034
- **Total CAs auditados:** 10 | **Back:** ✅ 100% | **Front:** ✅ 100% | **QA:** ✅ 100%

---

## US-038: Asignación Multi-Rol y Sincronización EntraID
**Épica:** 13 — Seguridad/RBAC | **Estado:** 🔶 EN PROGRESO (~55%) (Auditoría Forense — 2026-05-04)

| CA | Título (corto) | Back | Front | Unitarios | Componente | Integración | E2E | UAT | Sprint | Spec File | Notas |
|----|----------------|------|-------| ---- | ---- | ---- | ---- | ---- |--------| ---- |-------|
| CA-1 | Tolerancia a Fallos del Kill-Switch (Redis Fail-Open Policy) | ✅ | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | us038-multi-rol-entraid.spec.ts (skip) | Back: JwtAuthFilter:63-78 Fail-Open. Front: toggleUserStatus fallback local, no reacciona a Redis caído |
| CA-2 | Filtro de la Mochila Pesada (Anti-Token Bloat) | ✅ | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | us038-multi-rol-entraid.spec.ts (skip) | Back: JwtAuthFilter:104-109 ibpms_rol_ filter. Front: sin indicador visual de bloat |
| CA-3 | Aprovisionamiento JIT con Guardrail Claims Mínimos | ✅ | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | us038-multi-rol-entraid.spec.ts (skip) | Back: JwtAuthFilter:83-96 + AuthSyncController:51-77 (428). Front: no Modal Incompletitud para 428 |
| CA-4 | Protocolo Break-Glass con Cierre de Ciclo | ✅ | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | us038-multi-rol-entraid.spec.ts (skip) | 🐛 Back: /emergency-login funcional. FALTA cierre de ciclo (invalidación post-contingencia). Front: sin formulario Break-Glass |
| CA-5 | Resolución Aditiva de Permisos (RBAC Simple) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | us038-multi-rol-entraid.spec.ts (skip) | Back: JwtAuthFilter:104-130 fusión aditiva. Front: IdentityGovernance.vue:380-384 multi-select |
| CA-6 | Detección y Contención SoD (Juez y Parte) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | us038-multi-rol-entraid.spec.ts (skip) | Back: BpmTaskService:155-160 + SecurityAnomalyListener. Front: SecurityAnomalyTable.vue 207 líneas |
| CA-7 | Proxy Temporal de Autoridad y Exorcismo de Tareas | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | us038-multi-rol-entraid.spec.ts (skip) | Back: TaskRescueProducer + JwtAuthFilter:111-120 delegaciones. Front: IdentityGovernance.vue:174-222 |
| CA-8 | El Exorcismo de Tareas por Despido | ✅ | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | us038-multi-rol-entraid.spec.ts (skip) | Back: TaskRescueConsumer:28-53 UNCLAIM_ALL. Front: reacciona WS pero **falta botón admin trigger** |
| CA-9 | Trazabilidad Quirúrgica (Distributed Tracing V2 Ready) | ✅ | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | us038-multi-rol-entraid.spec.ts (skip) | Back: CorrelationIdFilter.java completo. Front: **Axios no propaga X-Correlation-ID** |
| CA-10 | Consolidación Transversal e Insignia de Procedencia | ✅ | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | us038-multi-rol-entraid.spec.ts (skip) | Back: useWorkdeskStore DTO tiene candidateGroup. Front: sin badge visual de procedencia |
| CA-11 | Indicador Tipográfico de Dominio en Cabecera | N/A | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | us038-multi-rol-entraid.spec.ts (skip) | Front: MainLayout.vue:349-356 topRolesTipText computed |
| CA-12 | Tablero de Resolución de Anomalías de Seguridad | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | us038-multi-rol-entraid.spec.ts (skip) | Back: Entity+Repo+Listener. Front: SecurityAnomalyTable.vue + IdentityGovernance Tab Anomalías |
| CA-13 | Postergación de Reset de Password para V2 | N/A | N/A | N/A | N/A | N/A | N/A | N/A | — | N/A | Diferido explícitamente a US-048 (ver us038_functional_analysis.md § 6) |

### Resumen US-038 (Auditoría Forense — 2026-05-04)
- **Total CAs:** 13 (11 activos + 1 N/A-Front + 1 N/A-Global)
- **Back:** ✅ 10/11 + N/A 1 = **91% funcional**
- **Front:** ✅ 5/11 + ⚠️ 6/11 = **45% con parciales**
- **QA E2E:** ❌ 0/11 (13 tests todos bajo `test.skip(true)` — 0 ejecutables)
- **Hallazgos Críticos:** CA-4 sin cierre de ciclo Break-Glass, CA-8 sin trigger admin, CA-9 sin propagación Axios Correlation-ID

---

## US-039: Formulario Genérico Base (Pantalla 7.B)
**Épica:** 2 — IDE Formularios | **Estado:** ✅ COMPLETADA

| CA | Título (corto) | Back | Front | Unitarios | Componente | Integración | E2E | UAT | Sprint | Spec File | Notas |
|----|----------------|------|-------| ---- | ---- | ---- | ---- | ---- |--------| ❌ Ninguno |-------|
| CA-1 | Inyección Explícita (Anti-Bypass) y Restricción VIP (Pre-Flight) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | ❌ Ninguno | Pendiente |
| CA-2 | Prevención de Context Bleeding (Filtro Anti-Basura BFF) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | ❌ Ninguno | Pendiente |
| CA-3 | Mutación Camaleónica de Interfaz y Botón de Pánico (Error Event) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | — | ❌ Ninguno | Pendiente |
| CA-4 | Definición del Cuerpo Editable del Formulario Genérico | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | S-72 | ❌ Ninguno | Hardening OBS-1, OBS-2 Frontend OK. QA Gatekeeper Red Stage Activo |
| CA-5 | Configuración de Whitelist Regex por Proceso | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | S-72 | ❌ Ninguno | Hardening OBS-1, OBS-2 Frontend OK. QA Gatekeeper Red Stage Activo |
| CA-6 | Catálogo Configurable de Roles VIP para Bloqueo Pre-Flight | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | S-72 | ❌ Ninguno | Hardening OBS-1, OBS-2 Frontend OK. QA Gatekeeper Red Stage Activo |
| CA-7 | Persistencia y Auto-Guardado del Formulario Genérico | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | S-72 | ❌ Ninguno | Hardening OBS-1, OBS-2 Frontend OK. QA Gatekeeper Red Stage Activo |
| CA-8 | Mapeo Explícito de Botones de Pánico a Eventos BPMN | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | S-72 | ❌ Ninguno | Hardening OBS-1, OBS-2 Frontend OK. QA Gatekeeper Red Stage Activo |

### Resumen US-039
- **Total CAs auditados:** 8 | **Back:** ✅ 5/8 (~62%) | **Front:** ✅ 5/8 (~62%) | **QA:** ✅ 5/8 (~62%)

---

## US-043: Configuración Global de SLA
**Épica:** 14 — SLA | **Estado:** ✅ COMPLETADA (con deuda técnica)

| CA | Título (corto) | Back | Front | Unitarios | Componente | Integración | E2E | UAT | Sprint | Spec File | Notas |
|----|----------------|------|-------| ---- | ---- | ---- | ---- | ---- |--------| ❌ Ninguno |-------|
| CA-1 | Inyección Arquitectónica del BusinessCalendar en Camunda Engine | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | us043-sla-config.spec.ts | Completado |
| CA-2 | Exención de Pausa para Timers Netamente Sistémicos | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | us043-sla-config.spec.ts | Completado |
| CA-3 | Recálculo Retroactivo Restringido a Batch Job (Anti-Deadlocks) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | us043-sla-config.spec.ts | Completado |
| CA-4 | Husos Horarios Estrictos en Geografías Híbridas (Timezones) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | us043-sla-config.spec.ts | Completado |
| CA-5 | Automatización de Festivos Externos con Fallback | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | us043-sla-config.spec.ts | Completado |
| CA-6 | Alertas Preventivas de Quiebre de Nivel (Early Warning) | ⚠️ | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ | — | us043-sla-config.spec.ts | **Deuda técnica pendiente** |

### Resumen US-043
- **Total CAs auditados:** 6 | **Back:** ✅ 5/6 (~83%) | **Front:** ✅ 5/6 (~83%) | **QA:** ❌ 0%

---

## US-048: Módulo Gestor Propio de Identidades (Internal IdP)
**Épica:** 13 — Seguridad/RBAC | **Estado:** ✅ COMPLETADA (Back+Front)

| CA | Título (corto) | Back | Front | Unitarios | Componente | Integración | E2E | UAT | Sprint | Spec File | Notas |
|----|----------------|------|-------| ---- | ---- | ---- | ---- | ---- |--------| ❌ Ninguno |-------|
| CA-1 | Creación Exclusiva por Administrador (V1 Centralizada) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | ❌ Ninguno | Completado |
| CA-2 | Gobernanza Estricta de Contraseñas Seguras | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | ❌ Ninguno | Completado |
| CA-3 | Destrabe Administrativo de Credenciales (Reset Manual) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | ❌ Ninguno | Completado |
| CA-4 | Fábrica de Roles Dinámicos (Role CRUD) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | ❌ Ninguno | Completado |
| CA-5 | El Botón de Emergencia (Kill Switch Activo/Inactivo) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | ❌ Ninguno | Completado |
| CA-6 | Asignación Híbrida de Múltiples Sombreros Locales | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | ❌ Ninguno | Completado |
| CA-7 | Mutación de Interfaz en Modo Híbrido EntraID | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | S-3 | ❌ Ninguno | Completado |

### Resumen US-048
- **Total CAs auditados:** 7 | **Back:** ✅ 100% | **Front:** ✅ 100% | **QA:** ❌ 0%

---

## Resumen Global de Cobertura (Actualizado 2026-04-18T15:25 — Reconciliación PO Cruzada)

| Métrica | Valor |
|---------|-------|
| **Total US en V1** | 56 |
| **US Completadas (Back+Front)** | 11 (US-000, US-001, US-003, US-005, US-028, US-034, US-036, US-038, US-039, US-043, US-048) |
| **US En Construcción (avanzadas >60%)** | 6 (US-002 ~68%, US-004 ~71%, US-025 ~60%, US-027 ~65%, US-029 ~72%, US-030 ~85%) |
| **US En Construcción (tempranas <50%)** | 2 (US-007 ~48% — IDOR remediado, US-017 ~50% — 8 CAs UX/UI pendientes) |
| **US Scaffolding (Fencing activo)** | 5 (US-008 ~10%, US-011, US-021, US-035, US-045) |
| **US Pendientes** | 32 |
| **CAs Implementados (estimado)** | ~290+ |
| **CAs Validados QA** | ~38 (~13%) |
| **Falsos Positivos Corregidos** | 5 (US-001 CA-8 · US-002 9%→68% · US-017 0%→50% · US-025 ausente · US-027 ausente) |
| **Vulnerabilidades Críticas Abiertas** | 0 (IDOR US-007 + US-027 cerrado en S6.1; Webhook legacy US-004 deprecado a 410) |
| **Principal Brecha** | 🟡 QA < 13% global. US-008 Kanban sigue mock. Data seed E2E pendiente para UI tests. |
| **E2E Sprint 6.1** | 4/7 PASS (57%) — Lotes B1+B2 PASS (Security), B3+B4+B5 FAIL (UI sin data seed) |

### Brechas Prioritarias (Post Iteración 6.1 — 2026-04-19)

| Prioridad | Brecha | US Afectadas | Acción Recomendada | Estado |
|-----------|--------|-------------|-------------------|:------:|
| ✅ CERRADO S6.1 | IDOR activo — tenantId hardcodeado | US-007, US-027 | Hotfix: role prefix `ibpms_rol_*`, tenant propagation en JwtAuthFilter, Anti-IDOR `startsWith` en RagSessionCleanerUseCase | ✅ E2E 2/2 PASS |
| ✅ CERRADO S6.1 | `EmailWebhookController` bypasea pipeline de seguridad | US-004 | Deprecado a HTTP 410 Gone | ✅ E2E 2/2 PASS |
| ✅ CERRADO S6.1 | B-20: Vinculación DMN↔BPMN no visual | US-005, US-007 | Dropdown visual + endpoint `/api/v1/dmn/definitions` | ✅ |
| ✅ CERRADO S6.1 | Login.vue sin data-testid E2E | Frontend | Añadidos 4 data-testid (break-glass-toggle, email, password, submit) | ✅ |
| ✅ CERRADO S6.1 | Debug System.out.println en SecurityContextUtils | Backend | Removidos 6 println | ✅ |
| 🔴 P0 It.6.2 | Data seed operacional para E2E UI | Backend/Infra | SQL seed: tasks, DMN definitions, Kanban cards para BD E2E | ❌ Pendiente |
| 🟠 P1 | US-008 KanbanView con mock hardcodeado | US-008, US-030 | Implementar state machine real + endpoint PATCH | ❌ Pendiente |
| 🟠 P1 | `FormBffCoreService` prefill parcialmente mock | US-029, US-017 | Conectar prefill a BD real | ❌ Pendiente |
| 🟠 P1 | CA-6 US-004: sin RabbitMQ consumer de intake | US-004 | Implementar `@RabbitListener` | ❌ Pendiente |
| 🟡 P2 | QA al 0% en US completadas | US-003, US-005, US-038, US-043, US-048 | Sprint de QA dedicado | ❌ Pendiente |
| 🟡 P2 | CAs Remediación US-007 (13-18) sin auditar | US-007 | Continuar auditoría | ❌ Pendiente |
| 🟡 P3 | Desglose CA-a-CA faltante | US-034, US-038, US-039, US-043, US-048 | Reconciliación con `git log --grep="CA-"` | ❌ Pendiente |
| 🟡 P3 | Deuda técnica US-043 CA-6 | US-043 | Plan de remediación | ❌ Pendiente |
| 🟡 P4 | OBS abiertas US-005 | US-005 | Cerrar OBS-1 (CA-68) y OBS-2 (CA-65) | ❌ Pendiente |
| 🟢 P1 | US-017 CA-19 a CA-26 UX/UI delegados | US-017 | Handoff Frontend + QA emitidos (Toast Flotante Conexión) | ⏳ Delegado |

---

> **⚡ Próxima acción recomendada (Iteración 6.2):**
> 1. **✅ P0 SEGURIDAD COMPLETADO:** Hotfix IDOR y Webhooks cerrados en Sprint 6.1.
> 2. **🔴 P0 DATA SEED:** Crear `seed-e2e.sql` con datos operacionales (AgileTask, DMN definitions, Kanban cards) para que Lotes B3-B5 pasen.
> 3. **🟠 P1 CONECTIVIDAD:** Conectar `FormBffCoreService.generateMegaDtoFormContext()` a datos reales de BD.
> 4. **🟠 P1 KANBAN:** Implementar state machine real US-008 + endpoint PATCH.
> 5. Re-ejecutar suite E2E completa para alcanzar 7/7 PASS (100%).
