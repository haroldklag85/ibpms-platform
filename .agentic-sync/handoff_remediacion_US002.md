# Handoff de Remediación — US-002: Reclamar Tarea (Claim Task)
# Fecha: 2026-05-02
# Origen: Auditoría Arquitectónica Forense (14 GAPs detectados)
# Gobierno: ADR-001 (Hexagonal), ADR-010 (Testing Pyramid)

## Secuencia de Ejecución
```
Fase 1: Infra/DB (sin dependencias)
Fase 2: Backend (espera Infra/DB)
Fase 3: Frontend (espera Backend)
Fase 4: QA (espera Backend + Frontend)
```

---

## Sección 1: Infra/DB

### Changeset requerido
- Extender tabla `claim_audit_log`:
  - ADD COLUMN `user_id` VARCHAR(100) — actor genérico (reemplaza semántica de solo `supervisor_id`)
  - ADD COLUMN `previous_assignee` VARCHAR(100) nullable
  - ADD COLUMN `reason` TEXT nullable
  - ADD COLUMN `message` TEXT nullable — nota peer-to-peer (CA-16)
  - RENAME COLUMN `supervisor_id` → mantener por retrocompatibilidad, el nuevo `user_id` lo reemplaza lógicamente
- Crear tabla `ibpms_orphaned_attachments`:
  - id UUID PK
  - task_id VARCHAR(100) NOT NULL
  - file_reference VARCHAR(500) NOT NULL
  - uploaded_by VARCHAR(100) NOT NULL
  - orphaned_at TIMESTAMPTZ DEFAULT now()
  - purged BOOLEAN DEFAULT false
  - INDEX (orphaned_at, purged)
- Registrar changeset en `db.changelog-master.yaml`

---

## Sección 2: Backend (9 GAPs)

### GAP-001: Separar ClaimAuditLog del dominio JPA
- **Actual:** `domain/model/audit/ClaimAuditLog.java` tiene `@Entity`, `@Table`, `@Column`
- **Acción:**
  1. Convertir `ClaimAuditLog.java` en POJO puro (quitar todas las anotaciones JPA)
  2. Agregar campos: `userId`, `previousAssignee`, `reason`, `message`, `actionType` (enum string)
  3. Crear `infrastructure/jpa/entity/ClaimAuditLogEntity.java` con las anotaciones JPA
  4. Crear `application/port/out/ClaimAuditPort.java`:
     ```java
     public interface ClaimAuditPort {
         void save(ClaimAuditLog log);
         List<ClaimAuditLog> findByTaskId(UUID taskId);
     }
     ```
  5. Crear `infrastructure/persistence/ClaimAuditJpaAdapter.java` que implemente `ClaimAuditPort` y mapee Entity↔Domain

### GAP-002: Desacoplar LiberarTareaService de JPA
- **Actual:** `LiberarTareaService.java` importa `TaskAuditLogEntity` y `TaskAuditLogRepository`
- **Acción:**
  1. Reemplazar `TaskAuditLogRepository` por `ClaimAuditPort`
  2. Eliminar imports de `infrastructure.jpa.entity` y `infrastructure.jpa.repository`
  3. Construir un `ClaimAuditLog` de dominio con actionType=`RELEASED` y persistir via puerto

### GAP-003: Desacoplar ClaimAuditService de Repository JPA
- **Actual:** `ClaimAuditService.java` importa `ClaimAuditLogRepository`
- **Acción:**
  1. Reemplazar `ClaimAuditLogRepository` por `ClaimAuditPort`
  2. Refactorizar `auditForceUnclaim()` para usar el puerto
  3. Agregar método `auditClaim(taskId, userId, actionType, reason, previousAssignee)` genérico

### GAP-004: Crear endpoint bulk-claim
- **Acción:**
  1. En el controlador unificado (ver GAP-011), crear:
     ```
     POST /api/v1/tasks/bulk-claim
     Body: { "taskIds": ["id1", "id2", ...] }  (max 20 — hard limit)
     Response 200: { "claimed": [...], "conflicts": [{ "taskId": "...", "reason": "Already claimed by María" }] }
     ```
  2. En el servicio, iterar cada taskId con el mismo mecanismo atómico de `findByIdForUpdate`. Capturar excepciones individualmente y acumular resultado parcial.
  3. Al finalizar el batch, emitir UN SOLO WebSocket `BULK_REMOVE` con todos los taskIds reclamados exitosamente (CA-23).

### GAP-005: Unificar endpoint release
- **Acción:**
  1. Crear `POST /api/v1/tasks/{taskId}/release` reemplazando `/unclaim`
  2. Body: `{ "message": "string (max 500 chars, opcional)" }`
  3. Response 200: `{ "taskId": "...", "releasedAt": "ISO8601" }`
  4. Prohibir envío de `partialPayload` a Camunda (CA-7: Amnesia Transaccional).
  5. En el servicio: `procesoBpmPort.liberarTarea(taskId, null)` — sin variables parciales.
  6. Si message presente, persistir en `ClaimAuditLog` con actionType=`RELEASED` y campo `message`.
  7. Emitir WebSocket `ADD` al canal grupal del tenant (CA-12).

### GAP-006: Validar team_id en force-unclaim
- **Acción:**
  1. En `WorkboxTaskController.forceUnclaim()` (o controlador unificado):
     - Extraer `teamId` del supervisor autenticado (JWT claim o `DataSegregationService`)
     - Extraer `teamId` de la tarea (consultar en BD)
     - Si `supervisor.teamId != task.teamId` → retornar `HTTP 403 Forbidden` con mensaje: "No tiene permisos para gestionar tareas de este equipo."
  2. Registrar intentos denegados en audit log con result=`DENIED`
  3. Response 200: `{ "taskId": "...", "previousAssignee": "...", "forcedBy": "...", "timestamp": "ISO8601" }`

### GAP-007: Exponer endpoint audit-trail
- **Acción:**
  1. Crear `GET /api/v1/tasks/{taskId}/audit-trail`
  2. Response 200: `{ "entries": [{ "action": "CLAIMED", "userId": "...", "userName": "...", "timestamp": "...", "reason": "..." }] }`
  3. Delegar a `ClaimAuditPort.findByTaskId()`

### GAP-008: Crear GhostJobScheduler
- **Acción:**
  1. Crear `application/service/GhostJobScheduler.java`
  2. `@Scheduled(fixedRate = 900000)` — cada 15 minutos
  3. Lógica:
     - Consultar tareas con `assignee != null` y `last_activity_at < (now - ghostTimeout)`
     - `ghostTimeout` default = 4 horas (configurable por tenant en `ClaimProperties`)
     - Para cada tarea inactiva: ejecutar auto-unclaim + purgar LocalStorage (notificar frontend via WS) + registrar audit `AUTO_UNCLAIMED`
  4. Pre-aviso al 75%: al detectar tarea al 75% del umbral, emitir WebSocket `GHOST_WARNING` hacia la bandeja personal del operario
  5. Extensión de tiempo: crear endpoint `POST /api/v1/tasks/{taskId}/extend-timeout` que reinicie el contador. Máx 2 extensiones consecutivas. Registrar audit `TIMEOUT_EXTENDED`.

### GAP-009: Amnesia Transaccional y archivos orphaned
- **Acción:**
  1. En la lógica de `liberar()`, PROHIBIR envío de variables a Camunda:
     ```java
     procesoBpmPort.liberarTarea(taskId, null); // NUNCA enviar partialPayload
     ```
  2. Marcar archivos adjuntos subidos durante la sesión como `orphaned` en tabla `ibpms_orphaned_attachments`
  3. Crear `OrphanedAttachmentCleanupJob.java` con `@Scheduled(cron = "0 0 3 * * ?")` que purga registros con `orphaned_at < (now - 24h)`

### GAP-010: Extender audit log con action_types
- **Acción:** Registrar todos los tipos del CA-20:
  - `CLAIMED` — reclamo individual
  - `BULK_CLAIMED` — reclamo masivo
  - `RELEASED` — liberación voluntaria + message
  - `FORCE_UNCLAIMED` — despojo por supervisor + reason
  - `AUTO_UNCLAIMED` — por inactividad
  - `TIMEOUT_EXTENDED` — extensión de tiempo

### GAP-011: Consolidar controladores
- **Acción:**
  1. Eliminar `api/controller/TaskClaimController.java` (paquete legacy `api.controller`)
  2. Consolidar toda la lógica de claim en un único controlador: `infrastructure/web/TaskClaimApiController.java` bajo `/api/v1/tasks/`
  3. Endpoints finales:
     - `POST /api/v1/tasks/{taskId}/claim`
     - `POST /api/v1/tasks/bulk-claim`
     - `POST /api/v1/tasks/{taskId}/release`
     - `POST /api/v1/tasks/{taskId}/force-unclaim` (@PreAuthorize SUPERVISOR)
     - `POST /api/v1/tasks/{taskId}/extend-timeout`
     - `GET  /api/v1/tasks/{taskId}/audit-trail`

### GAP-014: Tests obligatorios
- **Tests Unitarios de Dominio:**
  - `ClaimAuditLogTest.java` — verificar POJO puro, sin JPA
- **Tests Unitarios de Servicio (Mockito):**
  - `BulkClaimServiceTest.java` — 3 tests: batch OK, batch parcial, batch limit 20 exceeded
  - `GhostJobSchedulerTest.java` — 2 tests: tareas inactivas detectadas, pre-aviso emitido
- **Tests de Integración (Testcontainers + REST Assured):**
  - `ClaimIntegrationIT.java`:
    1. Claim concurrente: 2 threads, uno 200, otro 409
    2. Release con message → audit registrado
    3. Force-unclaim sin team_id match → 403
  - `BulkClaimIT.java`:
    1. bulk-claim 5 tareas, 2 ya tomadas → `{ claimed: 3, conflicts: 2 }`
    2. bulk-claim 25 tareas → 400 (excede hard limit 20)

---

## Sección 3: Frontend (2 GAPs)

### GAP-012: Modo Solo Lectura (CA-5, CA-18)
- En `TaskPreviewModal.vue` o componente equivalente del Workdesk:
  1. Al hacer doble clic en tarea de Cola de Grupo: abrir formulario en modo `readOnly=true`
  2. NO alterar `assignee` en BD — solo lectura visual
  3. Mostrar botón [Reclamar] dentro del preview
  4. Suscribirse al WebSocket: si llega `REMOVE` con ese taskId → mostrar banner "⚠️ Esta tarea fue reclamada por otro compañero" y deshabilitar botón [Reclamar] (gris + candado)

### GAP-013: Optimistic UI + Rollback (CA-10, CA-21)
- En `useWorkdeskStore.ts` o `useTasks.ts`:
  1. Al reclamar, mover la tarea visualmente a "Mi Bandeja" de inmediato (Optimistic UI)
  2. Mostrar ícono ⟳ "Confirmando con el servidor..."
  3. Si el POST falla, ejecutar retry con backoff: 3 intentos (2s, 4s, 8s)
  4. Si los 3 reintentos fallan:
     - Rollback visual: devolver tarea a "Cola del Equipo"
     - Mostrar Modal: "No pudimos confirmar tu reclamo porque la conexión no se restableció"
  5. NUNCA rollback silencioso (CA-21 punto 4)

---

## Sección 4: QA (Certificación)

### Matriz de Escenarios (12 tests)

| ID | Escenario | HTTP Esperado |
|----|-----------|---------------|
| QA-002-01 | POST `/claim` individual → tarea se asigna | 200 |
| QA-002-02 | POST `/claim` concurrente → segundo recibe | 409 |
| QA-002-03 | POST `/bulk-claim` 5 tareas, 2 conflictos | 200 con claimed:3, conflicts:2 |
| QA-002-04 | POST `/bulk-claim` 25 tareas (excede limit) | 400 |
| QA-002-05 | POST `/release` con message | 200 + audit tiene RELEASED + message |
| QA-002-06 | POST `/force-unclaim` supervisor mismo team | 200 + audit FORCE_UNCLAIMED |
| QA-002-07 | POST `/force-unclaim` supervisor OTRO team | 403 |
| QA-002-08 | GET `/audit-trail` muestra timeline completo | 200 con entries[] |
| QA-002-09 | ClaimAuditLog en dominio es POJO puro (sin @Entity) | Verificar imports |
| QA-002-10 | LiberarTareaService NO importa nada de infrastructure | Verificar imports |
| QA-002-11 | POST `/release` NO envía variables a Camunda | Verificar código L27 de LiberarTareaService |
| QA-002-12 | Tests Testcontainers (ClaimIntegrationIT + BulkClaimIT) pasan verde | mvn test |

Reportar en `.agentic-sync/approval_request_qa_US002.md` con PASS/FAIL por escenario.
