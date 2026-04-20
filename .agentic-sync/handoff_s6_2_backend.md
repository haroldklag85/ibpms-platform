# 🔧 Handoff Backend — Sprint 6 / Iteración 6.2

> **Iteración:** Sprint 6 — Iteración 6.2 (Data Seed E2E + J-04 v2 + Kanban MVP)  
> **Rama de trabajo:** `sprint-6/uat-certification` (git pull origin sprint-6/uat-certification primero)  
> **US objetivo:** US-001 (Workdesk), US-002 (Claim/Skip), US-008 (Kanban), US-029 (Formulario)  
> **Flujo:** Backend → Frontend → QA  
> **SSOT de referencia:** `docs/requirements/v1_user_stories_index.md` → épicas A y B  
> **Plan aprobado:** `docs/sprints/sprint_plan_s6.md` §Iteración 6.2  
> **Casos de uso refinados:** `docs/uat/casos_uso_uat_j04.md` (v2 — 45 escenarios)  
> **Autor:** Arquitecto Líder SW  
> **Fecha:** 2026-04-19

---

## 1. Metadatos y SSOT

| Parámetro | Valor |
|-----------|-------|
| **Sprint** | 6 — Iteración 6.2 |
| **Rama Git** | `sprint-6/uat-certification` |
| **US** | US-001, US-002, US-008, US-029 |
| **Bloques de trabajo** | B1: Data Seed E2E (P0), B2: Delegación real (P1), B3: Skipeo endpoint (P1), B4: BFF viewer real (P1), B5: Kanban state machine (P1), B6: Force Routing toggle (P2) |
| **Exclusiones** | US-017 (CQRS) excluida — D-01. No tocar `FormEventEntity` ni `form_event_store`. |
| **Prerequisito** | Iteración 6.1 SELLADA ✅ — commit `ca20cdb5` |

**Fuentes de verdad:**
- `docs/uat/casos_uso_uat_j04.md` (v2) → Escenarios completos
- `docs/sprints/sprint_plan_s6.md` → Plan del Sprint
- `.agentic-sync/coverage_matrix.md` → Matriz de cobertura

---

## 2. Alineación Arquitectónica

| ADR | Impacto |
|-----|---------|
| `adr-001-hexagonal-architecture.md` | Nuevos controllers en `infrastructure/web/`. Services en `application/service/`. Entities en `infrastructure/jpa/entity/`. |
| `adr_007_cmmn_vs_kanban.md` | Kanban usa POJOs puros en dominio. JPA entities en infraestructura. MapStruct para conversión. |
| `adr_010_testing_pyramid_governance.md` | Unit tests obligatorios para cada nuevo endpoint (JUnit/Mockito). |

**Principios Zero-Trust:**
- TODO endpoint DEBE usar `SecurityContextUtils.getTenantId()` — NUNCA hardcodes.
- Skipeo y Delegación DEBEN registrar audit trail inmutable.

---

## 3. Bloques de Trabajo

### B1: Data Seed E2E (🔴 P0 — Bloqueante Absoluto)

**Problema:** Los specs E2E de It. 6.1 (B3-B5) fallaron porque la BD está vacía post-Docker Compose. Sin datos operacionales, Playwright no encuentra elementos para interactuar.

**Entregable:** Archivo `backend/ibpms-core/src/main/resources/seed-e2e.sql`

**Contenido obligatorio del SQL seed:**

```sql
-- 1. USUARIOS E2E (4 actores de J-04 v2)
-- analista_n1 → grupo Adjusters, ROLE_OPERARIO
-- perito_a → grupo Adjusters, ROLE_OPERARIO, skill: peritaje
-- perito_b → grupo Adjusters, ROLE_OPERARIO, skill: peritaje
-- director_1 → grupo Directors, ROLE_SUPERVISOR
-- Passwords: BCrypt de "Test123!" (hash: $2a$10$ estándar)
-- tenant_id: 'tenant_alpha' para todos

-- 2. RELACIÓN DE DELEGACIÓN (PRE-04 del J-04)
-- director_1 es jefe de analista_n1 → tabla user_delegation o campo delegatedAssistantId

-- 3. TAREAS WORKDESK (precondición PRE-01 del J-04)
-- ≥ 4 tareas status=PENDING en cola Adjusters (sin assignee)
-- Con SLA variados: 1 verde (>50%), 1 amarillo (15-50%), 1 rojo (<15%), 1 gris (vencido)
-- Esto valida CU-J04-04 (4 niveles semáforo SLA simultáneos)

-- 4. TAREAS KANBAN (PRE-08 del J-04)
-- ≥ 3 tareas en estado TODO para Kanban Board
-- 1 tarea en DONE (para validar inmutabilidad CA-2)
-- Columnas: TODO, IN_PROGRESS, BLOCKED, DONE

-- 5. DMN DEPLOYMENT (para re-run B4 It. 6.1)
-- 1 tabla DMN publicada (key='risk_assessment')
-- Si Camunda requiere deploy vía API, documentar en globalSetup

-- 6. FEATURE TOGGLE (CU-J04-23)
-- forceRouting = false (default)
```

**Instrucción Docker Compose:** Añadir el mount del seed en `docker-compose.e2e.yml`:
```yaml
services:
  postgres:
    volumes:
      - ./backend/ibpms-core/src/main/resources/seed-e2e.sql:/docker-entrypoint-initdb.d/02-seed.sql
```

> [!CAUTION]
> Si las entidades JPA usan Liquibase para crear tablas, el seed SQL DEBE ejecutarse DESPUÉS del schema (por eso `02-seed.sql`, no `01-`). Verifica el orden de ejecución.

---

### B2: Delegación Real (🟠 P1 — B-J04-03)

**Brecha:** CU-J04-20 a 22 requieren que el Director vea las tareas del Analista N1 vía delegación. Actualmente `assistantId` es placeholder hardcoded `101edfe`.

**Archivos a crear/modificar:**

| Archivo | Acción | Ubicación |
|---------|--------|-----------|
| `UserDelegation.java` | [NEW] Entity JPA | `infrastructure/jpa/entity/` |
| `UserDelegationRepository.java` | [NEW] Spring Data JPA | `infrastructure/jpa/` |
| `DelegationService.java` | [NEW] Application service | `application/service/` |
| `WorkboxTaskController.java` | [MODIFY] Endpoint delegación | `infrastructure/web/` |

**Detalle:**
- Entity `UserDelegation`: `{id, supervisorId, assistantId, tenantId, createdAt}`
- Service: `findDelegatedAssistantId(supervisorId, tenantId)` → retorna assistantId o null
- Endpoint existente `GET /api/v1/tasks` acepta `?delegatedUser={userId}` → debe consultar la relación real
- **Validación anti-IDOR (CU-J04-NEG-04):** Si el caller NO tiene relación jerárquica → HTTP 403

---

### B3: Skipeo con Endpoint Real (🟠 P1 — B-J04-07)

**Brecha:** CU-J04-25 a 28 requieren skipeo con 4 motivos y audit trail. Actualmente `skipAndNext()` en el frontend NO tiene endpoint backend.

**Archivos a crear:**

| Archivo | Acción | Ubicación |
|---------|--------|-----------|
| `TaskSkipController.java` | [NEW] Controller | `infrastructure/web/` |
| `SkipAuditService.java` | [NEW] Service | `application/service/` |
| `SkipAuditEntity.java` | [NEW] Entity JPA | `infrastructure/jpa/entity/` |
| `SkipAuditRepository.java` | [NEW] Spring Data JPA | `infrastructure/jpa/` |

**Endpoint:**
```
POST /api/v1/tasks/{taskId}/skip
Body: { "reason": "CLIENT_NO_RESPONSE" | "REQUIRES_DOCUMENTATION" | "OUT_OF_AREA" | "OTHER", "detail": "opcional ≥10 chars si reason=OTHER" }
Response 200: { "skippedTaskId": "...", "nextTaskId": "...", "nextTaskName": "..." }
```

**Lógica:**
1. Registrar skipeo inmutable: `{userId, taskId, reason, detail, tenantId, timestamp}`
2. Liberar la tarea actual (unclaim)
3. Asignar la siguiente tarea más urgente por SLA al mismo usuario → `AgileTaskService.findNextBySlaPriority(tenantId, group)`
4. Retornar datos de la nueva tarea asignada

**4 motivos (enum `SkipReason`):**
- `CLIENT_NO_RESPONSE` — "El cliente no responde / No está disponible"
- `REQUIRES_DOCUMENTATION` — "Requiere documentación adicional externa"
- `OUT_OF_AREA` — "Fuera de mi área de especialidad"
- `OTHER` — "Otro (Especificar)" → `detail` obligatorio ≥10 chars

---

### B4: BFF Viewer Tarea Real (🟠 P1 — B-J04-02)

**Brecha:** `FormBffCoreService.generateMegaDtoFormContext()` retorna datos parcialmente mock para el prefill.

**Archivo:** `application/service/bff/FormBffCoreService.java`

**Cambio:** Conectar `GET /api/v1/tasks/{taskId}/form-data` a:
1. `formDefinitionRepository` para obtener el schema JSON del formulario vinculado al task
2. Variables de proceso Camunda (`runtimeService.getVariables(executionId)`) para prefill
3. Historial de formulario previo si existe (draft)

> [!NOTE]
> Si las variables de Camunda no están disponibles en el contexto E2E, retornar el schema vacío (sin prefill) para que al menos el formulario renderice. NEG-01 y NEG-02 validan formulario sin datos.

---

### B5: Kanban State Machine MVP (🟠 P1 — US-008)

**Brecha:** `KanbanView.vue` usa mocks hardcodeados. No hay endpoint de transición ni validación de estados.

**Archivos a crear/modificar:**

| Archivo | Acción |
|---------|--------|
| `KanbanStateController.java` | [NEW] `PATCH /api/v1/kanban/{taskId}/state` |
| `KanbanStateMachine.java` | [NEW] Validación de transiciones |
| `KanbanBoardService.java` | [MODIFY] Integrar state machine |

**State Machine (transiciones válidas):**
```
TODO → IN_PROGRESS ✅
IN_PROGRESS → BLOCKED ✅ (requiere blockReason)
IN_PROGRESS → DONE ✅
BLOCKED → IN_PROGRESS ✅ (desbloqueo)
DONE → * ❌ (inmutable — HTTP 403)
TODO → DONE ❌ (prohibido saltar)
TODO → BLOCKED ❌ (prohibido)
```

**Endpoint:**
```
PATCH /api/v1/kanban/{taskId}/state
Body: { "newState": "IN_PROGRESS" | "BLOCKED" | "DONE", "blockReason": "opcional" }
Response 200: { "taskId", "previousState", "newState", "updatedAt" }
Response 400: Transición inválida
Response 403: Tarea en DONE (inmutable)
```

**Endpoint Board:**
```
GET /api/v1/kanban/board?tenantId={auto}
Response 200: { columns: [{name, tasks: [{id, title, state, assignee, sla}]}] }
```

---

### B6: Force Routing Toggle (🟡 P2 — B-J04-04)

**Brecha:** CU-J04-23 a 24 requieren un feature toggle `forceRouting`.

**Implementación mínima:**

| Archivo | Acción |
|---------|--------|
| `FeatureToggleController.java` | [NEW] `GET/PUT /api/v1/admin/toggles/{key}` |
| `FeatureToggleEntity.java` | [NEW] Entity `{key, value, tenantId}` |

**Endpoint:**
```
GET /api/v1/admin/toggles/forceRouting → { "key": "forceRouting", "value": false }
PUT /api/v1/admin/toggles/forceRouting → Body: { "value": true } → 200 OK
```

---

## 4. Convenciones de Commit

```
feat(backend): T-1 seed-e2e.sql — data seed para J-04 v2 E2E
feat(backend): T-3 B-J04-03 — delegación real UserDelegation
feat(backend): T-4 B-J04-07 — endpoint skipAndNext + SkipAuditService
feat(backend): T-5 B-J04-02 — BFF viewer tarea datos reales
feat(backend): T-6 US-008 — KanbanStateMachine + PATCH endpoint
feat(backend): T-8 B-J04-04 — forceRouting toggle admin API
```

---

## 5. Validación Pre-Push

```bash
cd backend && ./gradlew test --tests "*Kanban*" --tests "*Skip*" --tests "*Delegation*" --tests "*Toggle*"
```

Todos los tests DEBEN pasar antes del push. Si algún test existente falla, NO modificar el test — investigar la causa.

---

## 6. Exclusiones Explícitas

- **NO tocar** `FormEventEntity.java` ni nada relacionado con US-017 (CQRS Event Sourcing)
- **NO tocar** `BpmnCopilotController.java` ni `RagSessionCleanerUseCase.java` — ya remediados en It. 6.1
- **NO tocar** `EmailWebhookController.java` — ya deprecado a 410 Gone en It. 6.1
- **NO implementar** WebSocket para Kanban ni `<UniversalSlaTimer>` — fuera del scope minimal
