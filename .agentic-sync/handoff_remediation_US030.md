# 🚀 HANDOFFS DE REMEDIACIÓN INTEGRAL — US-030 (Proyecto Ágil)

**De:** Arquitecto Líder
**Fecha:** 2026-05-02
**Rama:** `sprint-6`

---

## 1. 🛠️ Para: Agente Backend (Prioridad: CRÍTICA — 7 GAPs)

**Rol:** Ingeniero Backend (Spring Boot & Arquitectura Hexagonal)

### GAP 1: [ARQ-030-01] CRUD AgileTask (CA-3, CA-4) — CRITICAL
No existe controlador dedicado para operaciones sobre tareas ágiles individuales.
- **Acción:** Crea `AgileTaskController.java` en `infrastructure/web/` con los siguientes endpoints:
  - `POST /api/v1/agile/projects/{projectId}/tasks` — Crear tarea. Valida que el proyecto no supere 500 tareas activas (CA-11 `maxActiveTasks`). Campos: title (obligatorio), description, effortEstimated, assigneeIds, tags, notes.
  - `PUT /api/v1/agile/projects/{projectId}/tasks/{taskId}` — Editar tarea (título, descripción, esfuerzo, asignados, notas).
  - `DELETE /api/v1/agile/projects/{projectId}/tasks/{taskId}` — Hard-delete. ANTES del borrado, registrar en tabla de auditoría: taskId, title, deletedBy, deletedAt.
  - `GET /api/v1/agile/projects/{projectId}/tasks` — Listar tareas con filtro por status (default: excluir DONE) y por assigneeId.
- **Acción:** Crea `AgileTaskService.java` en `application/service/` que orqueste la lógica de negocio.

### GAP 2: [ARQ-030-02] Vista Portafolio (CA-7) — HIGH
- **Acción:** Agrega endpoint `GET /api/v1/agile/portfolio` que devuelva todas las tareas agrupadas por proyecto, filtradas por los proyectos donde el usuario autenticado es Líder/Scrum Master. Devolver campos ligeros.

### GAP 3: [ARQ-030-03] Seguridad (CA-11) — HIGH
- **Acción:** Cambia `@PreAuthorize` en `AgileProjectController` y el nuevo `AgileTaskController` para que las operaciones de escritura (POST/PUT/DELETE) solo permitan `SUPERVISOR` o `SUPER_ADMIN` (Scrum Master / Líder). Los `OPERARIO` quedan en solo lectura (GET).
- **Acción:** Antes de persistir `description` y `notes`, invoca `FormFieldCleanserService.cleanseInput()` o un sanitizador XSS equivalente (Jsoup o similar).
- **Acción:** En el endpoint POST de creación de tarea, valida: `long activeCount = taskRepo.countByProjectIdAndStatusNotIn(projectId, List.of("DONE","CANCELLED","DELETED")); if (activeCount >= project.getMaxActiveTasks()) throw 409 CONFLICT`.

### GAP 4: [ARQ-030-04] Reorder Drag&Drop (CA-6) — MEDIUM
- **Acción:** Agrega endpoint `PATCH /api/v1/agile/projects/{projectId}/tasks/reorder` que reciba una lista de `{taskId, newPosition}` y actualice los campos `position` en batch.

### GAP 5: [ARQ-030-05] Filtro DONE (CA-8) — MEDIUM
- **Acción:** En el endpoint GET de tareas, el query default debe excluir `status = 'DONE'`. Agregar query param `?includeCompleted=true` para activar el toggle.

### GAP 6: [ARQ-030-06] Ticket Rancio (CA-13) — MEDIUM
- **Acción:** En el DTO de respuesta de cada tarea, calcular y devolver `daysInactive = ChronoUnit.DAYS.between(task.getLastActivityAt(), ZonedDateTime.now())`. El frontend usará este valor para pintar el badge si `daysInactive >= 15`.

### GAP 7: [ARQ-030-07] Batch Multi-Select (CA-14) — MEDIUM
- **Acción:** Agrega endpoint `PATCH /api/v1/agile/projects/{projectId}/tasks/bulk-assign` que reciba `{taskIds: [...], assigneeId: "..."}` y ejecute un UPDATE masivo consolidado.

### Gate de Cierre
- `mvn clean compile` sin errores.
- Reportar en `.agentic-sync/approval_request_backend.md`.

---

## 2. 🎨 Para: Agente Frontend (Prioridad: ALTA — 6 puntos)

**Rol:** Ingeniero Frontend (Vue3 / Composition API)

### F-030-01: Panel CRUD de Tarea (CA-3)
- En `AgileHub.vue` / `AgileBacklogList.vue`, implementa el botón "+ Nueva Tarea" que deslice un panel lateral (slide-panel) con los campos: Título, Descripción (editor rich-text), Esfuerzo Estimado, Responsables (combo multi-select via `AssigneeMultiSelect.vue`), Etiquetas (via `AgileTagCreator.vue`), Notas.
- Conectar al nuevo endpoint `POST /api/v1/agile/projects/{projectId}/tasks`.

### F-030-02: Eliminación con Confirmación (CA-4)
- Botón de eliminación por tarjeta con diálogo de confirmación simple.
- Conectar al endpoint `DELETE`.

### F-030-03: Filtro de Completadas (CA-8)
- Toggle "Mostrar Completadas" en barra superior. Default: ocultas.
- Cuando activado, sección plegable al final de la lista con las tareas DONE.

### F-030-04: Badge Ticket Rancio (CA-13)
- Si `daysInactive >= 15`, mostrar borde lateral izquierdo ámbar, badge "🕐 Inactivo X días", fondo cálido sutil.

### F-030-05: Selector de Vista Portafolio (CA-7)
- Toggle en barra superior: "Vista Proyecto" (default) vs "Vista Portafolio".
- Vista Portafolio consume `GET /api/v1/agile/portfolio` y agrupa tarjetas por proyecto.

### F-030-06: Link "Saltar al Tablero" (CA-12)
- En esquina superior derecha: link "Saltar al Tablero →" que navegue a la Pantalla 3 (KanbanView) del proyecto actual.

### Gate de Cierre
- `npm run build` sin errores.
- Reportar en `.agentic-sync/approval_request_frontend.md`.

---

## 3. 🏗️ Para: Agente Infra/DB (Prioridad: VERIFICATIVA)

**Rol:** Ingeniero DevOps y Base de Datos

### Verificaciones requeridas:
1. Confirma que las tablas existen en migración Liquibase:
   - `ibpms_agile_projects`
   - `ibpms_agile_tasks`
   - `ibpms_agile_task_assignees` (tabla join)
   - `ibpms_agile_sla_changelog`
2. Verifica que la columna `position` (INTEGER) existe en `ibpms_agile_tasks`.
3. Verifica que la columna `last_activity_at` (TIMESTAMPTZ) existe en `ibpms_agile_tasks`.
4. Si alguna tabla falta, crea el changeset de Liquibase.
5. Reportar en `.agentic-sync/approval_request_infra.md`.

---

## 4. 🧪 Para: Agente QA (Prioridad: BLOQUEADA hasta Backend + Frontend)

**Rol:** Ingeniero de Calidad

### Escenarios de certificación:
| ID | Escenario | Criterio |
|----|-----------|----------|
| QA-030-01 | POST crear tarea con campos válidos | HTTP 201, tarea visible en GET. |
| QA-030-02 | POST crear tarea #501 en proyecto con 500 activas | HTTP 409 Conflict. |
| QA-030-03 | DELETE tarea existente | Tarea desaparece de GET. Registro en audit log. |
| QA-030-04 | POST cerrar proyecto en cascada | Todas las tareas no-DONE pasan a CANCELLED. Proyecto queda CLOSED y read-only. |
| QA-030-05 | PUT con XSS en description (`<script>alert(1)</script>`) | El script debe sanitizarse antes de persistir. |
| QA-030-06 | GET tareas default vs `?includeCompleted=true` | Default excluye DONE. Con flag las incluye. |
| QA-030-07 | PATCH reorder con nueva posición | Las posiciones se actualizan y el GET refleja el nuevo orden. |

Reportar en `.agentic-sync/approval_request_qa.md`.
