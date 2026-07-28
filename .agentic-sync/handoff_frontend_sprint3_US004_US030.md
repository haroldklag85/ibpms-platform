# Handoff Arquitectónico — 🎨 Agente Frontend + ⚙️ Backend (Parcial)
# Sprint 3: Iteraciones 5–7 (US-004 Triaje + US-030 Hub Ágil)

> **Emitido por:** `[🧠 ARQUITECTO LÍDER]` | **Fecha:** 2026-04-17
> **Sprint / Rama:** `sprint-3/feature/us004-intake-triage` y `sprint-3/feature/us030-agile-hub`
> **Protocolo Aplicado:** `.agents/skills/architect_handoff_protocol/SKILL.md`

---

## 1. Metadatos y SSOT

| Campo | Valor |
|-------|-------|
| **Sprint** | 3 — Retorno al Feature Factory |
| **User Stories** | US-004 (CAs 8,9,13,14,15,16), US-030 (14 CAs completos) |
| **SSOT** | `docs/requirements/epics/epic_A_motor_core.md` (US-004: líneas 650–731, US-030: líneas 830–956) |
| **Índice** | `docs/requirements/v1_user_stories_index.md` |
| **Dependencia Backend** | Handoff Backend Iteraciones 1-4 DEBE estar completado antes de iniciar Iteración 5 |
| **Flujo** | Backend (APIs triaje + CRUD ágil) → Frontend (P16 Intake + P10 Hub Ágil) → QA |

---

## 2. Alineación Arquitectónica y ADRs

| ADR | Impacto |
|-----|---------|
| `adr-001-hexagonal-architecture.md` | APIs de Hub Ágil como Ports/Adapters. |
| `adr_007_cmmn_vs_kanban.md` | US-030 usa Kanban puro (sin CMMN). Tablero manual, no BPMN. |
| `adr_010_testing_pyramid_governance.md` | Frontend: Happy-DOM unit tests. E2E: Playwright specs obligatorios. |

### Lineamientos Frontend
- **Stores Pinia:** Cada módulo nuevo (Intake, Agile) tiene su propio store en `frontend/src/stores/`.
- **Composables:** Lógica reutilizable en `frontend/src/composables/`.
- **Vistas:** Cada Pantalla vive en `frontend/src/views/`.
- **WebSockets:** STOMP existente para reactividad cruzada (P3 ↔ P10).

---

## 3. Rutas Exactas y Archivos a Crear

### Iteración 5: Triaje Humano (US-004 — Pantalla 16)

#### Backend (APIs de Triaje)
| Capa | Archivo | Propósito |
|------|---------|-----------|
| DB | `db/changelog/sprint3/004_create_triage_tables.sql` | DDL: `ibpms_triage_tasks` con SLA paramétrico |
| API | `api/controller/TriageTaskController.java` | `GET /api/v1/intake/triage/tasks`, `POST .../approve`, `POST .../reject` |
| Application | `application/service/TriageTaskService.java` | Crear tarea Pre-Triaje, aprobar → instanciar proceso BPMN, rechazar → motivo obligatorio |
| Application | `application/service/TriagePurgeScheduler.java` | CronJob: purga payloads rechazados > 30 días (CA-13) |

#### Frontend (Pantalla 16 — Intake)
| Capa | Archivo | Propósito |
|------|---------|-----------|
| Store | `frontend/src/stores/intakeStore.ts` | Estado reactivo: lista de tareas Pre-Triaje, filtros, paginación |
| View | `frontend/src/views/IntakeTriageView.vue` | Pantalla 16 completa |
| Component | `frontend/src/components/intake/TriageTaskCard.vue` | Tarjeta de pre-visualización del correo (CA-14) |
| Component | `frontend/src/components/intake/ApproveRejectDialog.vue` | Modal Aprobar/Rechazar con motivo obligatorio |
| Component | `frontend/src/components/intake/ProcessSelector.vue` | Dropdown de tipo de proceso BPMN (CA-15) |
| Component | `frontend/src/components/intake/SlaIndicator.vue` | Semáforo amarillo/rojo del SLA de entrada (CA-16) |
| Test | `frontend/src/tests/intakeStore.spec.ts` | Happy-DOM: flujo de aprobar/rechazar |

### Iteración 6: Hub Ágil CRUD (US-030 — Pantalla 10)

#### Backend (APIs del Hub)
| Capa | Archivo | Propósito |
|------|---------|-----------|
| DB | `db/changelog/sprint3/005_create_agile_hub_tables.sql` | DDL: `ibpms_agile_projects`, `ibpms_agile_tasks`, `ibpms_agile_task_assignees`, `ibpms_agile_tags` |
| Domain | `domain/model/AgileProject.java` | Entidad proyecto ágil (tipo KANBAN_CONTINUOUS, status ACTIVE/CLOSED) |
| Domain | `domain/model/AgileTask.java` | Entidad tarjeta: título, descripción, esfuerzo, posición, estado |
| Domain | `domain/model/AgileTag.java` | Entidad etiqueta con color ad-hoc |
| API | `api/controller/AgileProjectController.java` | CRUD de proyectos ágiles |
| API | `api/controller/AgileTaskController.java` | CRUD completo de tarjetas + reorden + asignación masiva |
| Application | `application/service/AgileTaskService.java` | Lógica CRUD con auditoría forense (CA-4), validación RBAC (CA-11) |

#### Frontend (Pantalla 10 — Hub Ágil)
| Capa | Archivo | Propósito |
|------|---------|-----------|
| Store | `frontend/src/stores/agileStore.ts` | Estado reactivo: proyecto activo, lista de tareas, filtros, drag state |
| View | `frontend/src/views/AgileHubView.vue` | Pantalla 10 completa |
| Component | `frontend/src/components/agile/AgileBacklogList.vue` | Lista vertical estilo Jira/Linear (CA-12) |
| Component | `frontend/src/components/agile/AgileTaskRow.vue` | Fila de tarjeta: título, responsables, tag, estado |
| Component | `frontend/src/components/agile/AgileTaskSlidePanel.vue` | Panel lateral Canvas para crear/editar tarjeta (CA-3) |
| Component | `frontend/src/components/agile/AgileTagCreator.vue` | Creador de etiquetas con color picker ad-hoc |
| Component | `frontend/src/components/agile/AssigneeMultiSelect.vue` | Combo multi-select de responsables del proyecto (CA-5) |
| Composable | `frontend/src/composables/useAgileDragDrop.ts` | Lógica de drag & drop vertical con persistencia de posición (CA-6) |
| Test | `frontend/src/tests/agileStore.spec.ts` | Happy-DOM: CRUD tarjetas, reorden, filtros |
| Test | `frontend/src/tests/useAgileDragDrop.spec.ts` | Happy-DOM: cálculo de posiciones |

### Iteración 7: Hub Ágil UX Avanzada (US-030)

#### Backend (APIs complementarias)
| Capa | Archivo | Propósito |
|------|---------|-----------|
| API | `api/controller/AgileProjectClosureController.java` | `POST /api/v1/agile/projects/{id}/close` — cascada de cancelación (CA-10) |
| Application | `application/service/SlaChangeLogService.java` | Bitácora de modificaciones SLA (CA-9) |

#### Frontend (UX avanzada)
| Capa | Archivo | Propósito |
|------|---------|-----------|
| Component | `frontend/src/components/agile/PortfolioViewToggle.vue` | Switch Vista Proyecto / Vista Portafolio (CA-7) |
| Component | `frontend/src/components/agile/CompletedTasksToggle.vue` | Toggle "Mostrar Completadas" plegable (CA-8) |
| Component | `frontend/src/components/agile/StaleBadge.vue` | Badge "🕐 Inactivo X días" con borde ámbar (CA-13) |
| Component | `frontend/src/components/agile/BulkAssignBar.vue` | Barra de selección múltiple + acción masiva (CA-14) |
| Component | `frontend/src/components/agile/SearchFilterBar.vue` | Búsqueda rápida: Estado, Asignado, Etiqueta (CA-12) |
| Composable | `frontend/src/composables/useVirtualScroll.ts` | Virtualización del DOM para scroll infinito (CA-12, CA-14) |
| Test | `frontend/src/tests/AgileHubView.spec.ts` | Happy-DOM: renderizado, filtros, scroll, stale badge |

---

## 4. Snippets Prescriptivos

### DDL — Hub Ágil (Iteración 6)
```sql
-- Liquibase Changelog: sprint3/005_create_agile_hub_tables.sql
-- changeset architect:sprint3-005-agile-hub

CREATE TABLE ibpms_agile_projects (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(200)  NOT NULL,
    description     TEXT,
    methodology     VARCHAR(20)   NOT NULL DEFAULT 'KANBAN_CONTINUOUS', -- Solo KANBAN en V1 (CA-1)
    status          VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE | CLOSED
    created_by      VARCHAR(255)  NOT NULL,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    closed_at       TIMESTAMPTZ,
    closed_by       VARCHAR(255),
    max_active_tasks INTEGER      NOT NULL DEFAULT 500  -- Límite rígido V1 (CA-11)
);

CREATE TABLE ibpms_agile_tasks (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id      UUID         NOT NULL REFERENCES ibpms_agile_projects(id),
    title           VARCHAR(300) NOT NULL,
    description     TEXT,          -- Sanitizado contra XSS (CA-11)
    effort_estimated NUMERIC(8,2),  -- Horas (CA-3)
    effort_actual   NUMERIC(8,2),  -- Horas reales (CA-3)
    notes           TEXT,
    status          VARCHAR(20)  NOT NULL DEFAULT 'TODO', -- TODO | IN_PROGRESS | DONE | CANCELLED
    position        INTEGER      NOT NULL DEFAULT 0,      -- Orden vertical (CA-6)
    sla_deadline    TIMESTAMPTZ,
    last_activity_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),  -- Para detección de "Ticket Rancio" (CA-13)
    created_by      VARCHAR(255) NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE ibpms_agile_task_assignees (
    task_id         UUID         NOT NULL REFERENCES ibpms_agile_tasks(id) ON DELETE CASCADE,
    user_id         VARCHAR(255) NOT NULL,
    assigned_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    PRIMARY KEY (task_id, user_id)
);

CREATE TABLE ibpms_agile_tags (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id      UUID         NOT NULL REFERENCES ibpms_agile_projects(id),
    name            VARCHAR(50)  NOT NULL,
    color_hex       VARCHAR(7)   NOT NULL DEFAULT '#6366f1',  -- Color ad-hoc (CA-3, CA-12)
    created_by      VARCHAR(255) NOT NULL,
    CONSTRAINT uq_tag_per_project UNIQUE (project_id, name)
);

CREATE TABLE ibpms_agile_task_tags (
    task_id         UUID NOT NULL REFERENCES ibpms_agile_tasks(id) ON DELETE CASCADE,
    tag_id          UUID NOT NULL REFERENCES ibpms_agile_tags(id),
    PRIMARY KEY (task_id, tag_id)
);

CREATE TABLE ibpms_agile_sla_changelog (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id         UUID         NOT NULL REFERENCES ibpms_agile_tasks(id),
    previous_value  TIMESTAMPTZ,
    new_value       TIMESTAMPTZ,
    changed_by      VARCHAR(255) NOT NULL,
    changed_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_agt_project ON ibpms_agile_tasks(project_id, status);
CREATE INDEX idx_agt_position ON ibpms_agile_tasks(project_id, position);
CREATE INDEX idx_agt_activity ON ibpms_agile_tasks(last_activity_at);
```

### Endpoint Contract — AgileTaskController (Iteración 6)
```java
@RestController
@RequestMapping("/api/v1/agile/projects/{projectId}/tasks")
public class AgileTaskController {

    // CA-3: Crear tarjeta
    @PostMapping
    public ResponseEntity<AgileTaskDto> createTask(
        @PathVariable UUID projectId,
        @Valid @RequestBody CreateAgileTaskRequest request) { ... }

    // CA-3: Listar tarjetas (campos ligeros para CA-14)
    @GetMapping
    public ResponseEntity<Page<AgileTaskSummaryDto>> listTasks(
        @PathVariable UUID projectId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String assignee,
        @RequestParam(required = false) UUID tagId,
        @RequestParam(required = false) String search,
        Pageable pageable) { ... }

    // CA-3: Detalle completo (carga pesada bajo demanda — CA-14)
    @GetMapping("/{taskId}")
    public ResponseEntity<AgileTaskDetailDto> getTaskDetail(
        @PathVariable UUID projectId,
        @PathVariable UUID taskId) { ... }

    // CA-3: Editar tarjeta
    @PatchMapping("/{taskId}")
    public ResponseEntity<AgileTaskDto> updateTask(
        @PathVariable UUID taskId,
        @Valid @RequestBody UpdateAgileTaskRequest request) { ... }

    // CA-4: Eliminar con auditoría forense
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
        @PathVariable UUID taskId) { ... }

    // CA-6: Reordenar por drag & drop
    @PatchMapping("/reorder")
    public ResponseEntity<Void> reorderTasks(
        @PathVariable UUID projectId,
        @Valid @RequestBody ReorderRequest request) { ... }

    // CA-5 + CA-14: Asignación masiva
    @PostMapping("/bulk-assign")
    public ResponseEntity<Void> bulkAssign(
        @PathVariable UUID projectId,
        @Valid @RequestBody BulkAssignRequest request) { ... }
}
```

### Vue Store — agileStore.ts (Iteración 6)
```typescript
// frontend/src/stores/agileStore.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { AgileTask, AgileProject, AgileTag } from '@/types/agile'
import { agileApi } from '@/services/agileApi'

export const useAgileStore = defineStore('agile', () => {
  // State
  const currentProject = ref<AgileProject | null>(null)
  const tasks = ref<AgileTask[]>([])
  const tags = ref<AgileTag[]>([])
  const isLoading = ref(false)
  const showCompleted = ref(false)    // CA-8: toggle completadas
  const viewMode = ref<'project' | 'portfolio'>('project') // CA-7

  // Computed
  const visibleTasks = computed(() => {
    let filtered = tasks.value
    if (!showCompleted.value) {
      filtered = filtered.filter(t => t.status !== 'DONE' && t.status !== 'CANCELLED')
    }
    return filtered.sort((a, b) => a.position - b.position)
  })

  // CA-13: Tareas rancias (>15 días sin actividad)
  const staleTasks = computed(() =>
    tasks.value.filter(t => {
      const daysSinceActivity = Math.floor(
        (Date.now() - new Date(t.lastActivityAt).getTime()) / (1000 * 60 * 60 * 24)
      )
      return daysSinceActivity > 15 && t.status !== 'DONE'
    })
  )

  // Actions
  async function fetchTasks(projectId: string) { /* CA-14: campos ligeros */ }
  async function createTask(projectId: string, data: CreateTaskPayload) { /* CA-3 */ }
  async function updateTask(taskId: string, data: Partial<AgileTask>) { /* CA-3 */ }
  async function deleteTask(taskId: string) { /* CA-4 */ }
  async function reorderTasks(projectId: string, orderedIds: string[]) { /* CA-6 */ }
  async function bulkAssign(projectId: string, taskIds: string[], userId: string) { /* CA-14 */ }

  return {
    currentProject, tasks, tags, isLoading, showCompleted, viewMode,
    visibleTasks, staleTasks,
    fetchTasks, createTask, updateTask, deleteTask, reorderTasks, bulkAssign
  }
})
```

---

## 5. Matriz de QA y Testing Atómico (Frontend)

| Test Name | US | CA Evaluados | Aserción Esperada |
|-----------|:--:|:------------:|-------------------|
| `intakeStore.approve.spec.ts` | 004 | CA-14,15 | Aprobar → API call con processType seleccionado; sin processType → error validación |
| `intakeStore.reject.spec.ts` | 004 | CA-14 | Rechazar sin motivo → bloqueado; con motivo → API call exitoso |
| `SlaIndicator.spec.ts` | 004 | CA-16 | >80% tiempo → amarillo; >100% → rojo; <80% → verde |
| `agileStore.crud.spec.ts` | 030 | CA-3,4 | createTask → tarea en lista; deleteTask → tarea removida; updateTask → campos actualizados |
| `agileStore.filters.spec.ts` | 030 | CA-8,12 | showCompleted.toggle → incluye/excluye DONE; filtro por tag → solo tarjetas con ese tag |
| `useAgileDragDrop.spec.ts` | 030 | CA-6 | Mover tarea de posición 3 a 1 → recalcula posiciones de todo; persiste orden |
| `staleBadge.spec.ts` | 030 | CA-13 | lastActivityAt > 15 días → badge visible "🕐 Inactivo 18 días"; actividad reciente → sin badge |
| `bulkAssign.spec.ts` | 030 | CA-14 | Seleccionar 5 tareas + asignar → 1 sola API call consolidada; sin selección → botón disabled |
| `portfolioView.spec.ts` | 030 | CA-7 | Modo portafolio → muestra tareas de todos los proyectos agrupadas |

---

## 6. Mensaje de Despacho

> **Para el Agente Frontend:**
> Lee los Criterios de Aceptación en `docs/requirements/epics/epic_A_motor_core.md` (US-004: líneas 650–731 para CAs de triaje, US-030: líneas 830–956 para el Hub Ágil). Las APIs las provee el Backend según el handoff `handoff_backend_sprint3_US017_US004.md`. La US-030 se construye SIN el botón "Usar Plantilla WBS" (solo "Iniciar vacío") — la integración con US-006 está diferida y documentada como CA-6 en US-006.
>
> **Build obligatorio:** Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.

> **Para el Agente Backend (Iteraciones 5-6-7 parcial):**
> Las APIs del Hub Ágil y del Triaje Humano se construyen EN PARALELO al Frontend. Los DDL y contratos de endpoint están prescritos arriba.
>
> **Compilación obligatoria:** Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.
