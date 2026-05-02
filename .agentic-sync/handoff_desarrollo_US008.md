# 🚀 HANDOFFS DE DESARROLLO INTEGRAL — US-008 (Tablero Kanban: Cambio de Estado)

**De:** Arquitecto Líder
**Fecha:** 2026-05-02
**Rama:** `sprint-6`
**Tipo:** DESARROLLO (no auditoría)
**ADRs Obligatorios:** ADR-001 (Hexagonal), ADR-007 (No-CMMN / State Machine JPA), ADR-010 (Pirámide Testing)

---

## 1. 🏗️ Para: Agente Infra/DB

### Changesets Liquibase requeridos:

**1.1 Tabla `ibpms_time_logs` (CA-9, CA-11)**
```yaml
columns:
  - id: UUID PK DEFAULT gen_random_uuid()
  - reference_id: UUID NOT NULL (FK genérica a tarea)
  - reference_type: VARCHAR(30) NOT NULL CHECK IN ('TASK_BPMN','TASK_AGILE','TASK_GANTT')
  - started_at: TIMESTAMPTZ NOT NULL
  - stopped_at: TIMESTAMPTZ (nullable, null = timer activo)
  - duration_minutes: INT (calculado al stop)
  - user_id: VARCHAR(100) NOT NULL
  - created_at: TIMESTAMPTZ DEFAULT now()
indexes:
  - idx_timelog_ref: (reference_id, reference_type)
  - idx_timelog_user: (user_id)
```

**1.2 Tabla `ibpms_kanban_columns` (CA-8)**
```yaml
columns:
  - id: UUID PK DEFAULT gen_random_uuid()
  - board_id: UUID NOT NULL FK → ibpms_kanban_boards
  - name: VARCHAR(50) NOT NULL
  - position: INT NOT NULL
  - created_at: TIMESTAMPTZ DEFAULT now()
constraints:
  - UNIQUE (board_id, name)
```

**1.3 ALTER `ibpms_task` (CA-1)**
```yaml
- ADD COLUMN blocked_reason TEXT (nullable)
```

**1.4 Seed data: columnas default para boards existentes**
```sql
INSERT INTO ibpms_kanban_columns (board_id, name, position)
SELECT id, 'TODO', 0 FROM ibpms_kanban_boards
UNION ALL
SELECT id, 'IN_PROGRESS', 1 FROM ibpms_kanban_boards
UNION ALL  
SELECT id, 'BLOCKED', 2 FROM ibpms_kanban_boards
UNION ALL
SELECT id, 'DONE', 3 FROM ibpms_kanban_boards;
```

Gate: Reportar en `.agentic-sync/approval_request_infra_US008.md`.

---

## 2. 🛠️ Para: Agente Backend (DESARROLLO COMPLETO — 6 Fases)

### REGLA INQUEBRANTABLE (ADR-001):
- Los archivos en `domain/model/` son POJOs PUROS. CERO imports de Jakarta/JPA/Spring.
- Los servicios en `application/service/` usan PUERTOS (interfaces), NO repositorios JPA directos.
- Los adaptadores JPA en `infrastructure/persistence/` implementan los puertos y mapean Entity↔Domain.

### Fase 1: Dominio Puro (`domain/model/kanban/`)

**1.1 `KanbanState.java`** (Enum con transiciones — reemplaza `KanbanStateMachine.java` actual)
```java
package com.ibpms.poc.domain.model.kanban;

import java.util.EnumSet;
import java.util.Map;

public enum KanbanState {
    TODO, IN_PROGRESS, BLOCKED, DONE;

    private static final Map<KanbanState, EnumSet<KanbanState>> TRANSITIONS = Map.of(
        TODO, EnumSet.of(IN_PROGRESS),
        IN_PROGRESS, EnumSet.of(BLOCKED, DONE),
        BLOCKED, EnumSet.of(IN_PROGRESS),
        DONE, EnumSet.noneOf(KanbanState.class)
    );

    public boolean canTransitionTo(KanbanState target) {
        return TRANSITIONS.getOrDefault(this, EnumSet.noneOf(KanbanState.class)).contains(target);
    }

    public boolean isImmutable() {
        return this == DONE;
    }
}
```

**1.2 `KanbanTask.java`** — POJO puro (sin @Entity)
```
Campos: UUID id, UUID boardId, String title, String description, 
        KanbanState status, String assignee (single), String priority,
        ZonedDateTime slaDueDate, ZonedDateTime createdAt, ZonedDateTime updatedAt,
        String blockedReason
Métodos:
  - isImmutable() → status.isImmutable()
  - validateTransition(KanbanState newState) → lanza IllegalStateException si inválida
  - requireBlockedReason(String reason) → valida que reason no sea blank si newState==BLOCKED
```

**1.3 `KanbanColumn.java`** — POJO puro
```
Campos: UUID id, UUID boardId, String name, int position
```

**1.4 `TimeLogEntry.java`** — Value Object Append-Only
```
Campos: UUID id, UUID referenceId, String referenceType (TASK_BPMN/TASK_AGILE/TASK_GANTT),
        ZonedDateTime startedAt, ZonedDateTime stoppedAt, Integer durationMinutes,
        String userId, ZonedDateTime createdAt
Regla: Sin setters para startedAt, userId, referenceId después de construcción (inmutabilidad parcial).
Método: stop(ZonedDateTime now) → calcula durationMinutes = ChronoUnit.MINUTES.between(startedAt, now), seta stoppedAt.
```

### Fase 2: Puertos (`application/port/`)

**2.1 `out/KanbanTaskPort.java`**
```java
public interface KanbanTaskPort {
    KanbanTask save(KanbanTask task);
    Optional<KanbanTask> findById(UUID id);
    List<KanbanTask> findByBoardId(UUID boardId);
}
```

**2.2 `out/KanbanColumnPort.java`**
```java
public interface KanbanColumnPort {
    KanbanColumn save(KanbanColumn column);
    List<KanbanColumn> findByBoardId(UUID boardId);
    long countByBoardId(UUID boardId);
    void deleteById(UUID id);
}
```

**2.3 `out/TimeLogPort.java`**
```java
public interface TimeLogPort {
    TimeLogEntry save(TimeLogEntry entry);
    List<TimeLogEntry> findByReferenceId(UUID referenceId);
    Optional<TimeLogEntry> findActiveByUserAndReference(String userId, UUID referenceId);
}
```

**2.4 `in/MoveKanbanTaskUseCase.java`**
```java
public interface MoveKanbanTaskUseCase {
    void moveTask(UUID taskId, String newState, String blockedReason, String userId);
}
```

**2.5 `in/TrackTimeUseCase.java`**
```java
public interface TrackTimeUseCase {
    TimeLogEntry startTimer(UUID referenceId, String referenceType, String userId);
    TimeLogEntry stopTimer(UUID logId, String userId);
}
```

### Fase 3: Servicios de Aplicación (`application/service/`)

**3.1 `KanbanTaskService.java`** — Implementa `MoveKanbanTaskUseCase`
```
Lógica moveTask():
1. Cargar task vía KanbanTaskPort.findById(taskId) → 404 si no existe.
2. Validar task.isImmutable() → 400 "Tarea en DONE es inmutable".
3. Parsear newState → KanbanState.valueOf(newState).
4. Validar task.validateTransition(newState) → 400 si inválida.
5. Si newState == BLOCKED → task.requireBlockedReason(blockedReason) → 400 si vacío.
6. Mutar estado en el POJO domain.
7. Persistir via KanbanTaskPort.save(task).
8. Emitir WebSocket STOMP a /topic/kanban/{boardId}/tasks con evento TASK_STATE_CHANGED.
9. Escribir audit log (AuditLogService existente).
```

**3.2 `TimeTrackingService.java`** — Implementa `TrackTimeUseCase`
```
Lógica startTimer():
1. Cargar tarea (via KanbanTaskPort u otro).
2. Validar estado: rechazar si TODO o DONE (CA-3: timer gobernado por columna).
3. Verificar que no existe timer activo (stoppedAt==null) para el mismo user+reference → 409.
4. Crear TimeLogEntry con startedAt=now, stoppedAt=null.
5. Persistir vía TimeLogPort.save().

Lógica stopTimer():
1. Cargar TimeLogEntry → 404.
2. Validar que pertenece al userId → 403.
3. Invocar entry.stop(now) → calcula duración.
4. Persistir vía TimeLogPort.save().
```

**3.3 `KanbanColumnService.java`**
```
Lógica createColumn():
1. Contar columnas del board → si >= 7, lanzar 409 CONFLICT (hard-limit CA-8).
2. Validar nombre único por board.
3. Asignar position = count + 1.
4. Persistir vía KanbanColumnPort.save().

Lógica deleteColumn():
1. Validar que la columna no tenga tareas asignadas → 409 si tiene.
2. Eliminar vía KanbanColumnPort.deleteById().
```

### Fase 4: Adaptadores de Infraestructura (`infrastructure/`)

**4.1 `persistence/KanbanTaskJpaAdapter.java`** — Implementa KanbanTaskPort
- Inyecta KanbanTaskRepository (JPA). 
- Mapea KanbanTaskEntity ↔ KanbanTask (domain) usando factory methods.

**4.2 `persistence/TimeLogJpaAdapter.java`** — Implementa TimeLogPort
- Crea `TimeLogEntity.java` en `infrastructure/jpa/entity/`.
- Crea `TimeLogRepository.java` en `infrastructure/jpa/repository/`.

**4.3 `persistence/KanbanColumnJpaAdapter.java`** — Implementa KanbanColumnPort

**4.4 `web/KanbanTaskApiController.java`** — Controller REST consolidado
```
@RestController
@RequestMapping("/api/v1/kanban")
Endpoints:
  PATCH /{taskId}/state → @PreAuthorize OPERARIO/SUPERVISOR/SUPER_ADMIN
    Body: { "newState": "IN_PROGRESS", "blockedReason": "..." }
    Delega a MoveKanbanTaskUseCase.moveTask()
  
  GET /boards/{boardId}/tasks → Listar tareas por board
  GET /boards/{boardId}/columns → Listar columnas
  POST /boards/{boardId}/columns → Crear columna (SUPERVISOR/SUPER_ADMIN)
  DELETE /boards/{boardId}/columns/{colId} → Eliminar columna (SUPERVISOR/SUPER_ADMIN)
```

**4.5 `web/TimeTrackingController.java`** — Append-Only REST
```
@RestController
@RequestMapping("/api/v1/time-tracking")
Endpoints:
  POST /start → Body: { referenceId, referenceType } → 201
  POST /stop/{logId} → 200
  GET /task/{taskId} → Listar logs de una tarea
  
  NO exponer PUT ni DELETE (CA-11: Inmutabilidad)
  Si alguien intenta DELETE → retornar 405 Method Not Allowed
```

### Fase 5: Tests (ADR-010 — OBLIGATORIOS)

**5.1 Tests Unitarios (src/test/java/.../domain/)**

`KanbanStateTest.java`:
```java
// Test: TODO → IN_PROGRESS = válida
// Test: TODO → BLOCKED = inválida
// Test: TODO → DONE = inválida
// Test: IN_PROGRESS → BLOCKED = válida
// Test: IN_PROGRESS → DONE = válida
// Test: BLOCKED → IN_PROGRESS = válida
// Test: BLOCKED → DONE = inválida
// Test: DONE → cualquier = inválida (inmutable)
// Test: DONE.isImmutable() = true
// Test: TODO.isImmutable() = false
```

`KanbanTaskTest.java`:
```java
// Test: isImmutable() retorna true si status == DONE
// Test: validateTransition() lanza excepción para transición inválida
// Test: requireBlockedReason() lanza excepción si reason es blank
// Test: requireBlockedReason() acepta reason no vacío
```

`TimeLogEntryTest.java`:
```java
// Test: stop() calcula duración correctamente
// Test: stop() seta stoppedAt
// Test: constructor inicializa startedAt y userId correctamente
```

**5.2 Tests Unitarios de Servicio (Mockito)**

`KanbanTaskServiceTest.java`:
```java
// @ExtendWith(MockitoExtension.class)
// Mock: KanbanTaskPort, SimpMessagingTemplate, AuditLogService
// Test: moveTask valid → port.save() invocado + STOMP emitido
// Test: moveTask inválida → excepción, port.save() NO invocado
// Test: moveTask DONE → excepción "inmutable"
// Test: moveTask BLOCKED sin reason → excepción
```

`TimeTrackingServiceTest.java`:
```java
// Mock: TimeLogPort, KanbanTaskPort
// Test: startTimer en TODO → rechazado
// Test: startTimer en DONE → rechazado
// Test: startTimer en IN_PROGRESS → log creado
// Test: startTimer con timer activo → 409
// Test: stopTimer → duración calculada
```

`KanbanColumnServiceTest.java`:
```java
// Mock: KanbanColumnPort
// Test: createColumn cuando count < 7 → ok
// Test: createColumn cuando count >= 7 → 409
// Test: deleteColumn con tareas → rechazado
```

**5.3 Tests de Integración (Testcontainers + REST Assured)**

`KanbanStateTransitionIT.java`:
```java
// @SpringBootTest(webEnvironment = RANDOM_PORT)
// Testcontainers: PostgreSQL
// JWT real via JwtTokenProvider
// Test E2E: crear board → crear task → PATCH /state TODO→IN_PROGRESS → 200 + DB verificado
// Test E2E: PATCH DONE→TODO → 400
// Test E2E: PATCH BLOCKED sin reason → 400
```

`TimeTrackingIT.java`:
```java
// Test E2E: POST /start en IN_PROGRESS → 201
// Test E2E: POST /start en TODO → 400
// Test E2E: POST /stop → 200 con duración
// Test E2E: DELETE → 405
```

### Gate de Cierre Backend:
- `mvn clean test` ALL GREEN (unitarios + integración).
- Reportar en `.agentic-sync/approval_request_backend_US008.md`.

---

## 3. 🎨 Para: Agente Frontend (DESPUÉS de Backend)

### 3.1 `stores/kanbanStore.ts` (Pinia)
```typescript
// Estado: board, columns, tasks, activeTimers
// Actions:
//   fetchBoard(boardId) → GET /api/v1/kanban/boards/{boardId}/tasks
//   moveTask(taskId, newState, blockedReason?) → PATCH /api/v1/kanban/{taskId}/state
//     Optimistic UI: mover tarjeta localmente primero, revertir si falla
//   startTimer(taskId) → POST /api/v1/time-tracking/start
//   stopTimer(logId) → POST /api/v1/time-tracking/stop/{logId}
//   fetchColumns(boardId) → GET /api/v1/kanban/boards/{boardId}/columns
//   addColumn(boardId, name) → POST /api/v1/kanban/boards/{boardId}/columns
//   removeColumn(boardId, colId) → DELETE /api/v1/kanban/boards/{boardId}/columns/{colId}
// WebSocket: suscribir a /topic/kanban/{boardId}/tasks
```

### 3.2 `components/common/UniversalSlaTimer.vue` (CA-10)
- Props: `taskId`, `referenceType`, `currentState`, `slaDueDate`
- Comportamiento por columna (CA-3):
  - TODO: Timer oculto y bloqueado
  - IN_PROGRESS: Timer habilitado (Play/Stop visible)
  - BLOCKED: Timer habilitado (sigue contando)
  - DONE: Timer bloqueado y apagado
- Muestra SLA countdown (slaDueDate - now) con colores: verde (>50%), ámbar (20-50%), rojo (<20%)
- Botón Play/Stop que invoca kanbanStore.startTimer() / stopTimer()

### 3.3 Refactorizar `views/kanban/KanbanView.vue`
- Consumir `kanbanStore` en lugar de datos mockeados.
- Implementar drag & drop real con `@vueuse/core` useDraggable o `vue-draggable-next`.
- Al soltar tarjeta en nueva columna → invocar `kanbanStore.moveTask()`.
- Renderizar columnas dinámicas desde `kanbanStore.columns`.

### 3.4 Refactorizar `components/kanban/KanbanCard.vue`
- Integrar `<UniversalSlaTimer :taskId="task.id" :currentState="task.status" :slaDueDate="task.slaDueDate" referenceType="TASK_AGILE" />`
- Mostrar badge de assignee.
- Si `task.status === 'BLOCKED'` → mostrar chip con blockedReason.

### 3.5 Crear `components/kanban/BlockedReasonModal.vue`
- Se abre automáticamente cuando el usuario arrastra a BLOCKED.
- Input textarea obligatorio + botón "Confirmar Bloqueo".
- Si cancela → revertir el drag.

### 3.6 Crear `components/kanban/AddColumnModal.vue`
- Solo visible para SUPERVISOR/SUPER_ADMIN.
- Input nombre + validación única.
- Muestra contador: "X/7 columnas".

### Gate de Cierre Frontend:
- `npm run build` sin errores.
- Reportar en `.agentic-sync/approval_request_frontend_US008.md`.

---

## 4. 🧪 Para: Agente QA (DESPUÉS de Backend + Frontend)

### Escenarios de Certificación:

| ID | Escenario | Criterio |
|----|-----------|----------|
| QA-008-01 | PATCH state TODO→IN_PROGRESS | HTTP 200. Tarea reflejada en nueva columna. |
| QA-008-02 | PATCH state IN_PROGRESS→BLOCKED sin reason | HTTP 400. |
| QA-008-03 | PATCH state DONE→cualquier | HTTP 400 (inmutable). |
| QA-008-04 | PUT campos de tarea en DONE | HTTP 400 (read-only). |
| QA-008-05 | POST /time-tracking/start en tarea TODO | HTTP 400. |
| QA-008-06 | POST /time-tracking/start en IN_PROGRESS | HTTP 201. |
| QA-008-07 | DELETE /time-tracking/{id} | HTTP 405 Method Not Allowed. |
| QA-008-08 | POST columna #8 en board con 7 | HTTP 409 Conflict. |
| QA-008-09 | Asignar 2 usuarios a misma tarjeta Kanban | HTTP 400 (Single-Assignee). |
| QA-008-10 | `mvn test` con Testcontainers | Todos los unit + IT pasan. |

Gate: Reportar en `.agentic-sync/approval_request_qa_US008.md`.
