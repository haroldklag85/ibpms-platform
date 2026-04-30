# Journey J-08: Resiliencia Transversal — CQRS, Circuit Breaker, DLQ y Event Sourcing

> **Journey:** J-08 — Certificación de la Infraestructura de Resiliencia
> **Actor principal:** Administrador IT / Motor Backend / Operario (validación cruzada)
> **Criticidad:** 🟠 ALTA (US-000 completada, US-034 completada, US-017 NO desarrollada → máxima deuda)
> **US Cruzadas:** US-000, US-034, US-017, US-029, US-001, US-004
> **Épicas:** Motor Core (Épica A) + Dashboards/Integraciones (Épica F)
> **Fecha:** 2026-04-19
> **Autor:** Agente PO (Antigravity)
> **Formato:** Híbrido (Manual paso-a-paso + links a `.spec.ts`)
> **Enfoque PO:** Certificar los pilares de infraestructura invisible: CQRS, Event Store, Circuit Breaker, DLQ, idempotencia

---

## Narrativa del Journey

Este Journey certifica la infraestructura de resiliencia que sostiene toda la plataforma iBPMS. A diferencia de los Journeys funcionales anteriores, J-08 valida los mecanismos invisibles al usuario final: la persistencia inmutable via Event Sourcing (US-017), la orquestación asíncrona via RabbitMQ (US-034), y los interceptores globales de error y degradación (US-000). El actor principal es el Backend mismo, con el Administrador IT como operador del Dashboard DLQ y el Operario como validador cruzado de la experiencia degradada.

```
┌───────────────────────────────────────────────────────────────────────────────┐
│ FASE 1: Event Sourcing y CQRS — Persistencia Inmutable (US-017)              │
│ FASE 2: Protección del Motor Camunda — Exclusión Topológica (US-017)         │
│ FASE 3: RabbitMQ — Priority Queues y Topología (US-034)                      │
│ FASE 4: DLQ — Dashboard, Retry y Purga (US-034)                             │
│ FASE 5: Circuit Breaker y Fallback SQL (US-034, US-000)                      │
│ FASE 6: Degradación Graceful Global — UX de Error (US-000, US-001)           │
└───────────────────────────────────────────────────────────────────────────────┘
```

---

## Precondiciones

| # | Precondición | Verificación | US Origen |
|---|-------------|-------------|-----------|
| PRE-1 | Clúster RabbitMQ operativo con exchanges y colas configuradas | `/actuator/health/rabbitmq` → 200 | US-034 |
| PRE-2 | Motor Camunda operativo | `/actuator/health/camunda` → 200 | US-000 |
| PRE-3 | PostgreSQL operativo con tabla `form_event_store` | DDL aplicada | US-017 |
| PRE-4 | Redis operativo (idempotencia, Circuit Breaker state) | `PING` → `PONG` | US-034 |
| PRE-5 | Usuario `admin_it@alpha.com` con rol `ADMIN_IT` | JWT con rol efectivo | US-036 |
| PRE-6 | Usuario `operario@alpha.com` con tarea activa asignada | JWT con `assignee` | US-002 |
| PRE-7 | Topología documentada en `docs/architecture/rabbitmq_topology.md` | Archivo existe y es consistente | US-034 CA-4 |

---

## FASE 1: Event Sourcing y CQRS — Persistencia Inmutable

### CU-J08-01: Grabación Inmutable del Evento FORM_SUBMITTED
**CA Mapeado:** US-017 CA-01, US-017 CA-06
**Estado esperado:** ❌ DEBE FALLAR (US-017 NO desarrollada)
| Paso | Actor | Acción | Resultado Esperado (POST-PARCHE) |
|:----:|-------|--------|----------------------------------|
| 1 | Operario | Completa formulario y presiona [Enviar Final] | `POST /api/v1/workbox/tasks/{id}/complete` |
| 2 | Backend | Inyecta `FORM_SUBMITTED` en tabla `form_event_store` | — |
| 3 | Verificación BD | `SELECT * FROM form_event_store WHERE task_id = X` | Registro con: `event_id` (UUID), `event_type=FORM_SUBMITTED`, `task_id`, `process_instance_id`, `user_id`, `payload_json` (JSONB), `schema_version`, `created_at`, `idempotency_key` |
| 4 | Pentester | Intenta `UPDATE form_event_store SET payload_json = ...` | ❌ Políticas de BD: tabla inmutable (append-only) |
| 5 | Pentester | Intenta `DELETE FROM form_event_store WHERE event_id = ...` | ❌ Políticas de BD: tabla inmutable |
| 6 | Verificación | Worker asíncrono de proyección | Datos aplanados en tabla analítica para Dashboard (BAM) |
**Resultado actual (sin parche):** ⚠️ US-017 no implementada. El submit guarda directamente sin Event Sourcing.
**Automatización:** `e2e/specs/j-08/event-store-form-submitted.spec.ts`

### CU-J08-02: Idempotencia del Submit con Idempotency Key
**CA Mapeado:** US-017 CA-06, US-029 CA-12
**Estado esperado:** ❌ DEBE FALLAR
| Paso | Actor | Acción | Resultado Esperado (POST-PARCHE) |
|:----:|-------|--------|----------------------------------|
| 1 | Frontend | Genera UUID `idempotency_key` y lo incluye en header | — |
| 2 | Frontend | Submit exitoso → HTTP 200 | `FORM_SUBMITTED` con key `ABC-123` |
| 3 | Frontend (retry accidental) | Envía mismo payload con misma `idempotency_key` `ABC-123` | — |
| 4 | Backend | Detecta key duplicada en `form_event_store` | **HTTP 200** (idempotente — no graba segundo evento) |
| 5 | Verificación BD | `SELECT COUNT(*) FROM form_event_store WHERE idempotency_key = 'ABC-123'` | Exactamente 1 registro |
| 6 | Verificación | Camunda avanzó token SOLO una vez | Sin duplicación de tarea |
**Automatización:** `e2e/specs/j-08/event-store-idempotency.spec.ts`

### CU-J08-03: Auto-Claim Transaccional para Tareas de Grupo
**CA Mapeado:** US-017 CA-04, US-017 CA-13
**Estado esperado:** ❌ DEBE FALLAR
| Paso | Actor | Acción | Resultado Esperado (POST-PARCHE) |
|:----:|-------|--------|----------------------------------|
| 1 | Operario A | Abre tarea TK-500 del grupo "Abogados" (sin `assignee`) y presiona [Enviar] | — |
| 2 | Backend | Verifica: tarea sin `assignee` | Activa flujo Auto-Claim |
| 3 | Backend | Verifica: `operario_a` es miembro del `candidateGroup` | Validación CA-13 |
| 4 | Backend | Ejecuta `taskService.claim(TK-500, operario_a)` | Evento `TASK_AUTO_CLAIMED` grabado en Event Store |
| 5 | Backend | Inmediatamente después: `FORM_SUBMITTED` grabado | 2 eventos consecutivos |
| 6 | Verificación | Event Store | `TASK_AUTO_CLAIMED` con timestamp anterior a `FORM_SUBMITTED` |
| 7 | Operario B | Intenta submit simultáneo de TK-500 | — |
| 8 | Backend | `OptimisticLockingException` de Camunda | **HTTP 409 Conflict**: "Esta tarea ya fue reclamada y completada por otro operario" |
**Automatización:** `e2e/specs/j-08/event-store-auto-claim.spec.ts`

### CU-J08-04: Borradores — GET, PUT y DELETE con Lifecycle
**CA Mapeado:** US-017 CA-07, US-017 CA-16
**Estado esperado:** ❌ DEBE FALLAR
| Paso | Actor | Acción | Resultado Esperado (POST-PARCHE) |
|:----:|-------|--------|----------------------------------|
| 1 | Frontend | `PUT /api/v1/workbox/tasks/{taskId}/draft` con borrador parcial | HTTP 204 No Content |
| 2 | Verificación BD | `task_drafts WHERE task_id = X` | Borrador actualizado con `updated_at` actual |
| 3 | Frontend | `GET /api/v1/workbox/tasks/{taskId}/draft` | HTTP 200 con `{currentStep, partialData, schemaVersion, updatedAt}` |
| 4 | Frontend | Submit exitoso → `POST /complete` | — |
| 5 | Backend | Como último paso transaccional, ejecuta `DELETE /draft` | HTTP 204 — borrador eliminado |
| 6 | Verificación BD | `task_drafts WHERE task_id = X` | CERO registros (draft destruido post-submit CA-16) |
| 7 | Verificación | Borradores NO aparecen en Event Store | `form_event_store` sin tipo `FORM_DRAFT_SAVED` (CA-09) |
**Automatización:** `e2e/specs/j-08/draft-lifecycle-crud.spec.ts`

### CU-J08-05: TTL de Borradores — Purga 72h por Cron Job
**CA Mapeado:** US-017 CA-07
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Sistema | Borrador de TK-500 tiene `updated_at` hace 73 horas | — |
| 2 | Sistema | Cron Job diario de purga ejecuta | — |
| 3 | Verificación BD | `task_drafts WHERE task_id = TK-500` | Registro eliminado (TTL 72h excedido) |
| 4 | Frontend | `GET /draft` para TK-500 | HTTP 404 Not Found |
| 5 | Verificación | Borradores frescos (<72h) | NO eliminados por el Cron Job |
**Automatización:** `e2e/specs/j-08/draft-ttl-purge.spec.ts`

---

## FASE 2: Protección del Motor Camunda — Exclusión Topológica

### CU-J08-06: Exclusión Topológica — Solo DTO Mínimo a Camunda
**CA Mapeado:** US-017 CA-02
**Estado esperado:** ❌ DEBE FALLAR
| Paso | Actor | Acción | Resultado Esperado (POST-PARCHE) |
|:----:|-------|--------|----------------------------------|
| 1 | Operario | Envía formulario con 50 campos (payload de 15KB) | — |
| 2 | Backend | Graba payload COMPLETO en `form_event_store` (CQRS Write Side) | ✅ Persistido |
| 3 | Backend | Notifica a Camunda: `taskService.complete()` | — |
| 4 | Verificación | Payload enviado a Camunda | Solo DTO minificado: `{aprobado: true, form_storage_id: "ABC-123"}` |
| 5 | Verificación BD Camunda | `ACT_RU_VARIABLE` | CERO textos largos ni JSONs complejos. Solo variables de gateway |
| 6 | Verificación | Tamaño de la tabla Camunda | Mínimo (<1KB por tarea vs 15KB del payload original) |
**Automatización:** `e2e/specs/j-08/camunda-topological-exclusion.spec.ts`

### CU-J08-07: Rollback Compensatorio con Evento Inmutable
**CA Mapeado:** US-017 CA-03, US-017 CA-10
**Estado esperado:** ❌ DEBE FALLAR
| Paso | Actor | Acción | Resultado Esperado (POST-PARCHE) |
|:----:|-------|--------|----------------------------------|
| 1 | Backend | `FORM_SUBMITTED` grabado exitosamente | Evento en Event Store |
| 2 | Backend | `taskService.complete()` a Camunda → Timeout (10s) | Motor caído |
| 3 | Backend | Retry #1 (1s delay) → falla | — |
| 4 | Backend | Retry #2 (2s delay) → falla | — |
| 5 | Backend | Retry #3 (4s delay) → falla | 3 intentos agotados (7s retry + 10s timeout = 17s total) |
| 6 | Backend | Ejecuta Rollback Compensatorio | Graba `FORM_SUBMIT_ROLLED_BACK` en Event Store (NO borra el original) |
| 7 | Verificación BD | Event Store | 2 eventos: `FORM_SUBMITTED` (original) + `FORM_SUBMIT_ROLLED_BACK` (compensación con `original_event_id`) |
| 8 | Frontend | Recibe HTTP 500 | Mensaje: "Motor No Disponible" (NO HTTP 202 falso positivo) |
| 9 | Worker Proyección | Proyecta datos analíticos | Excluye el `FORM_SUBMITTED` anulado de las tablas de lectura |
**Automatización:** `e2e/specs/j-08/camunda-rollback-compensatory.spec.ts`

### CU-J08-08: Referencia de Evento Visible para el Operario
**CA Mapeado:** US-017 CA-15
**Estado esperado:** ❌ DEBE FALLAR
| Paso | Actor | Acción | Resultado Esperado (POST-PARCHE) |
|:----:|-------|--------|----------------------------------|
| 1 | Operario | Submit exitoso | HTTP 200 |
| 2 | Response body | Incluye `eventReference` | Código legible: `EVT-A3F8K9` (max 12 chars) |
| 3 | Frontend | Pantalla de confirmación | "Tarea completada exitosamente. Referencia: EVT-A3F8K9" |
| 4 | Operario | Cita referencia a Soporte Técnico | Soporte busca en Event Store por referencia → encuentra evento exacto |
**Automatización:** `e2e/specs/j-08/event-reference-visible.spec.ts`

---

## FASE 3: RabbitMQ — Priority Queues y Topología

### CU-J08-09: Topología de Exchanges y Colas — Catálogo Oficial
**CA Mapeado:** US-034 CA-1, US-034 CA-4
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Verificación Infra | Exchange `ibpms.exchange.topic` (tipo Topic) existe | Configurado como punto de entrada único |
| 2 | Verificación | Colas principales existen | `ibpms.notifications.email`, `ibpms.ai.generation`, `ibpms.integrations.webhook`, `ibpms.bpmn.events` |
| 3 | Verificación | Dead Letter Exchange/Queue existe | `ibpms.exchange.dlx` → `ibpms.dlq.global` |
| 4 | Verificación | Routing Keys siguen convención | `notifications.p1.send`, `ai.p3.generate`, etc. |
| 5 | Verificación Docs | `docs/architecture/rabbitmq_topology.md` | Documento consistente con la topología real |
| 6 | Verificación | CERO colas ad-hoc sin documentar | Todas las colas existentes están en el catálogo |
**Automatización:** `e2e/specs/j-08/rabbitmq-topology-catalog.spec.ts`

### CU-J08-10: Priority Queues — Jerarquización P1 > P2 > P3
**CA Mapeado:** US-034 CA-3, US-034 CA-6
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Sistema | Inyecta 3 mensajes simultáneos: P3 (AI batch), P2 (email), P1 (aprobación financiera) | — |
| 2 | Worker | Consume mensajes | Orden: P1 primero, luego P2, luego P3 |
| 3 | Verificación | Header `x-priority` | Cada mensaje lleva su nivel correcto |
| 4 | Verificación | Prefetch count | P1=1 (atómico), P2=10, P3=50 |
| 5 | Verificación | Mensajes sin `x-priority` | Default a P2 |
**Automatización:** `e2e/specs/j-08/rabbitmq-priority-queues.spec.ts`

### CU-J08-11: Idempotencia Obligatoria en Workers Consumidores
**CA Mapeado:** US-034 CA-5
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Productor | Publica mensaje con `x-idempotency-key: UUID-AAA` | — |
| 2 | Worker | Consume y procesa exitosamente | `ibpms_processed_messages` registra `UUID-AAA` |
| 3 | Admin IT | Desde DLQ, reintenta el mismo mensaje (con mismo `UUID-AAA`) | — |
| 4 | Worker | Consulta `ibpms_processed_messages` → key existe | ACK silencioso — NO reprocesa |
| 5 | Verificación | Efecto colateral (ej: email enviado) | Solo 1 email enviado (no 2) |
| 6 | Verificación | Scheduled Job de purga (24h) | Registros >72h eliminados automáticamente |
**Automatización:** `e2e/specs/j-08/rabbitmq-idempotency-workers.spec.ts`

### CU-J08-12: Retry Automático con Backoff Exponencial antes de DLQ
**CA Mapeado:** US-034 CA-7
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Worker | Falla al procesar mensaje (IOException transitoria) | — |
| 2 | RabbitMQ | Retry #1: inmediato (0ms) → falla | — |
| 3 | RabbitMQ | Retry #2: delay 5 segundos → falla | — |
| 4 | RabbitMQ | Retry #3: delay 30 segundos → falla | — |
| 5 | RabbitMQ | Retry #4 (final): delay 2 minutos → falla | — |
| 6 | RabbitMQ | Enruta a DLX → `ibpms.dlq.global` | — |
| 7 | Verificación DLQ | Mensaje en DLQ con headers | `x-original-queue`, `x-first-death-reason`, `x-delivery-count: 4`, `x-last-error-message` |
| 8 | Worker | Error permanente (ValidationException) | DLQ directa SIN reintentos (error no transitorio) |
**Automatización:** `e2e/specs/j-08/rabbitmq-retry-backoff.spec.ts`

---

## FASE 4: DLQ — Dashboard, Retry y Purga

### CU-J08-13: Dashboard DLQ — Visualización Consolidada
**CA Mapeado:** US-034 CA-2, US-034 CA-8
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Admin IT | Navega al Dashboard DLQ (componente Vue custom del iBPMS) | Pantalla accesible desde navegación principal |
| 2 | Verificación | `GET /api/v1/admin/queues/dlq/summary` | `{total: 45, by_queue: [{name: "ibpms.integrations.webhook", count: 30}, ...], oldest_message: "2026-04-17T10:00:00Z"}` |
| 3 | Verificación UI | Tabla con agrupación por cola de origen | Columnas: Cola Origen, Cantidad, Mensaje Más Antiguo, Último Error |
| 4 | Verificación RBAC | Operario intenta acceder | **HTTP 403 Forbidden**: solo `ADMIN_IT` |
| 5 | Verificación | Dashboard es pantalla iBPMS, NO enlace al Management UI de RabbitMQ | Componente Vue integrado |
**Automatización:** `e2e/specs/j-08/dlq-dashboard-summary.spec.ts`

### CU-J08-14: DLQ — Reintentar Mensajes con Confirmación Modal
**CA Mapeado:** US-034 CA-8
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Admin IT | Presiona [Reintentar Mensajes] en Dashboard DLQ | Modal: "Se reintentarán 45 mensajes. Los Workers deben ser idempotentes (CA-5)." |
| 2 | Admin IT | Presiona [Confirmar] | `POST /api/v1/admin/queues/dlq/retry` |
| 3 | Backend | Mueve 45 mensajes de DLQ a sus colas originales | — |
| 4 | Verificación | Workers procesan (idempotencia protege contra duplicados CA-5) | — |
| 5 | Verificación Auditoría | `ibpms_audit_log` | `{action: "DLQ_RETRY", user: "admin_it@alpha.com", message_count: 45, timestamp}` |
**Automatización:** `e2e/specs/j-08/dlq-retry-messages.spec.ts`

### CU-J08-15: DLQ — Purga con Sudo-Mode Obligatorio
**CA Mapeado:** US-034 CA-8, US-038
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Admin IT | Presiona [Purgar Cola] | Modal Sudo-Mode: "Ingrese su contraseña + justificación (mín. 20 chars)" |
| 2 | Admin IT | Ingresa password correcto + "Mensajes de integración obsoletos tras migración a v2" | — |
| 3 | Admin IT | Presiona [Confirmar Purga] | `DELETE /api/v1/admin/queues/dlq/purge` → HTTP 200 |
| 4 | Verificación | DLQ vacía | Total: 0 mensajes |
| 5 | Verificación Auditoría | `ibpms_audit_log` | `{action: "DLQ_PURGE", justification: "Mensajes de integración...", user, message_count, timestamp}` |
| 6 | Admin IT (sin Sudo) | Intenta `DELETE /api/v1/admin/queues/dlq/purge` sin header `X-Sudo-Password` | **HTTP 403**: "Se requiere Sudo-Mode" |
**Automatización:** `e2e/specs/j-08/dlq-purge-sudo-mode.spec.ts`

### CU-J08-16: DLQ — TTL 30 Días con Archivado Automático
**CA Mapeado:** US-034 CA-9
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Sistema | Mensaje en DLQ con edad de 29 días | — |
| 2 | DlqArchiveJob | Se ejecuta diariamente → detecta mensaje con TTL <48h | — |
| 3 | DlqArchiveJob | Copia a `ibpms_dlq_archive`: `message_id`, `original_queue`, `headers_json`, `body_summary` (1KB máx), `archived_at` | — |
| 4 | Sistema | Mensaje supera 30 días (TTL) | RabbitMQ purga automáticamente |
| 5 | Verificación | `ibpms_dlq_archive` | Registro de archivo preservado para forense |
| 6 | Verificación | Archivos con >180 días | Purgados por `LocalStorageGarbageCollector` |
**Automatización:** `e2e/specs/j-08/dlq-ttl-archive.spec.ts`

---

## FASE 5: Circuit Breaker y Fallback SQL

### CU-J08-17: Circuit Breaker RabbitMQ — Falla y Recuperación
**CA Mapeado:** US-034 CA-10
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Sistema | `/actuator/health/rabbitmq` → falla #1 | Estado: CLOSED (aún operativo) |
| 2 | Sistema (15s después) | Health check → falla #2 | Estado: CLOSED |
| 3 | Sistema (30s después) | Health check → falla #3 | Estado: **OPEN** (Circuit Breaker activado) |
| 4 | Productores | Intentan publicar mensajes | Buffer local en memoria (max 1000, FIFO) |
| 5 | RabbitMQ | Vuelve en 3 minutos | — |
| 6 | Health check | Detecta RabbitMQ restaurado | Estado: HALF-OPEN → CLOSED |
| 7 | Sistema | Drena buffer automáticamente | 1000 mensajes reenviados a RabbitMQ |
| 8 | Verificación | CERO mensajes perdidos | Todos procesados post-recuperación |
**Automatización:** `e2e/specs/j-08/circuit-breaker-recovery.spec.ts`

### CU-J08-18: Circuit Breaker — Fallback SQL de Emergencia (>5 minutos)
**CA Mapeado:** US-034 CA-10
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Sistema | Circuit Breaker en estado OPEN durante 5+ minutos | Buffer local lleno |
| 2 | Sistema | Timeout de buffer excedido | — |
| 3 | Backend | Persiste mensajes en `ibpms_queue_fallback` | Tabla SQL de emergencia |
| 4 | Sistema | Dispara alerta crítica | Notificación a SysAdmin: "RabbitMQ Offline — N mensajes en fallback SQL de emergencia" |
| 5 | RabbitMQ | Vuelve después de 15 minutos | — |
| 6 | Sistema | Job de drenaje lee `ibpms_queue_fallback` | Mensajes reenviados a RabbitMQ |
| 7 | Verificación | Tabla `ibpms_queue_fallback` | Vacía post-drenaje |
**Automatización:** `e2e/specs/j-08/circuit-breaker-fallback-sql.spec.ts`

---

## FASE 6: Degradación Graceful Global — UX de Error

### CU-J08-19: Degradación Elegante ante Falla BPMN (Workdesk)
**CA Mapeado:** US-001 CA-7, US-000 CA-1
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Sistema | Motor Camunda caído (HTTP 503) | — |
| 2 | Operario | Carga Workdesk (Pantalla 1) | — |
| 3 | Frontend | Interceptor global captura 503 de Camunda | — |
| 4 | Workdesk | Carga exitosamente tareas Kanban (no dependen de Camunda) | Tareas ágiles visibles |
| 5 | Workdesk | Toast/Banner amable | "Sincronización de Procesos (BPMN) degradada temporalmente. Estamos trabajando para solucionarlo" |
| 6 | Verificación | NO hay pantalla blanca | Workdesk funcional con datos parciales |
| 7 | Camunda vuelve | Operario recarga (F5) o WebSocket re-sincroniza | Tareas BPMN reaparecen |
**Automatización:** `e2e/specs/j-08/graceful-degradation-workdesk.spec.ts`

### CU-J08-20: Trazabilidad de Rechazos Históricos en BFF
**CA Mapeado:** US-017 CA-05, US-017 CA-11
**Estado esperado:** ❌ DEBE FALLAR
| Paso | Actor | Acción | Resultado Esperado (POST-PARCHE) |
|:----:|-------|--------|----------------------------------|
| 1 | QA Analyst | Rechaza tarea TK-700 con motivo: "Adjuntos incompletos — faltan reportes financieros Q4" | `FORM_REJECTED` en Event Store |
| 2 | Operario | Abre tarea TK-700 devuelta | `GET /api/v1/workbox/tasks/{id}/form-context` |
| 3 | BFF Response | Incluye `rejectionLogs[]` obligatoriamente | Array con: `rejectedBy`, `rejectedAt`, `reason`, `stageName`, `taskId` (CA-11) |
| 4 | Frontend | Renderiza Alert principal | "❌ Devuelta por: QA Analyst | Motivo: Adjuntos incompletos..." |
| 5 | Verificación | Si 2+ rechazos previos | Sección plegable con historial completo (más reciente primero) |
| 6 | Verificación | Nombres de revisores NO anonimizados | Trazabilidad prevalece en V1 |
**Automatización:** `e2e/specs/j-08/rejection-logs-bff.spec.ts`

### CU-J08-21: SLA de Latencia — Endpoint /complete en ≤5 Segundos
**CA Mapeado:** US-017 CA-17
**Estado esperado:** ❌ DEBE FALLAR
| Paso | Actor | Acción | Resultado Esperado (POST-PARCHE) |
|:----:|-------|--------|----------------------------------|
| 1 | Operario | Presiona [Enviar Final] | Spinner visible (US-029 CA-20) |
| 2 | Backend | Ciclo completo: validación → Event Store → Camunda → response | — |
| 3 | Verificación | Tiempo de respuesta | ≤5 segundos en condiciones normales |
| 4 | Verificación | En peor caso (3 retries de Camunda) | ≤17 segundos antes de HTTP 500 |
| 5 | Verificación | Si procesamiento >5s SIN error Camunda | Log de alerta para monitoreo proactivo |
| 6 | Verificación | Respuesta siempre síncrona | HTTP 200 o 5xx — NUNCA HTTP 202 (falso positivo prohibido) |
**Automatización:** `e2e/specs/j-08/sla-latency-complete.spec.ts`

### CU-J08-22: Cifrado At-Rest de Datos PII en Event Store
**CA Mapeado:** US-017 CA-12
**Estado esperado:** ❌ DEBE FALLAR
| Paso | Actor | Acción | Resultado Esperado (POST-PARCHE) |
|:----:|-------|--------|----------------------------------|
| 1 | Operario | Envía formulario con campo PII: cédula=80123456 | — |
| 2 | Backend | Detecta campo marcado como PII en schema Zod | — |
| 3 | Backend | Cifra campo con AES-256 ANTES de escribir `payload_json` | — |
| 4 | Verificación BD | `SELECT payload_json FROM form_event_store WHERE event_id = X` | Campo cédula es texto cifrado, NO texto plano |
| 5 | Verificación | Llave de cifrado gestionada por Azure Key Vault | Diferente de la llave del LocalStorage (US-029 CA-11) |
| 6 | Worker Proyección | Descifra al proyectar a tablas analíticas | Solo para roles `AUDITOR`/`ADMIN_IT` |
**Automatización:** `e2e/specs/j-08/event-store-pii-encryption.spec.ts`

---

## Escenarios Negativos

### CU-J08-NEG-01: Rate-Limiting en Endpoints de Borradores
**CA Mapeado:** US-017 CA-14
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Frontend | Envía 7 `PUT /draft` en 60 segundos (límite: 6/min) | — |
| 2 | Requests 1-6 | HTTP 204 cada uno | Borrador actualizado |
| 3 | Request 7 | **HTTP 429 Too Many Requests** con `Retry-After: 10` | — |
| 4 | Frontend | Atrapa 429 silenciosamente (sin error al operario) | Reintenta en próximo ciclo Debounce |

### CU-J08-NEG-02: Implicit Locking en Borradores — Solo Assignee Puede Guardar
**CA Mapeado:** US-017 CA-07
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario B (no assignee) | `PUT /api/v1/workbox/tasks/{taskId}/draft` con JWT de Operario B | — |
| 2 | Backend | Verifica `assignee` de tarea vs `userId` del JWT | No coinciden |
| 3 | Response | **HTTP 403 Forbidden**: "No tiene permisos para guardar borradores de esta tarea" | — |

### CU-J08-NEG-03: Archivado Anual del Event Store
**CA Mapeado:** US-017 CA-18
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Sistema | Eventos con `created_at` > 12 meses | — |
| 2 | Job mensual | Mueve eventos a `form_event_store_archive` | Tablespace optimizado para lecturas infrecuentes |
| 3 | Verificación | Eventos archivados siguen siendo inmutables y consultables | Append-only preservado |
| 4 | Verificación | Tablas de proyección analítica (Query Side) | NO archivadas (activas para dashboards) |

### CU-J08-NEG-04: ShedLock — Prevención de Solapamiento de Cron Jobs
**CA Mapeado:** US-034 CA-7, US-017 (Worker)
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Servidor | Job de proyección #1 en estado `RUNNING` (tomó 26 horas) | — |
| 2 | Scheduler | Intenta lanzar Job #2 al día siguiente | — |
| 3 | ShedLock | Detecta Database Lock activo | Job #2 ABORTADO silenciosamente (Skip) |
| 4 | Sistema | Emite alerta al SysAdmin: "Job solapado detectado — verificar rendimiento" | — |
| 5 | Verificación | CERO crash por OOM | Servidor estable |

---

## Matriz de Trazabilidad

| Escenario | US Principal | CAs Cubiertos | Fase | Estado Esperado |
|-----------|:-----------:|:------------:|:----:|:--------------:|
| CU-J08-01 | US-017 | CA-01, CA-06 | Event Store | ❌ FALLA |
| CU-J08-02 | US-017 | CA-06, US-029 CA-12 | Event Store | ❌ FALLA |
| CU-J08-03 | US-017 | CA-04, CA-13 | Event Store | ❌ FALLA |
| CU-J08-04 | US-017 | CA-07, CA-09, CA-16 | Event Store | ❌ FALLA |
| CU-J08-05 | US-017 | CA-07 | Event Store | ❌ FALLA |
| CU-J08-06 | US-017 | CA-02 | Camunda | ❌ FALLA |
| CU-J08-07 | US-017 | CA-03, CA-10 | Camunda | ❌ FALLA |
| CU-J08-08 | US-017 | CA-15 | Camunda | ❌ FALLA |
| CU-J08-09 | US-034 | CA-1, CA-4 | RabbitMQ | ✅ PASA |
| CU-J08-10 | US-034 | CA-3, CA-6 | RabbitMQ | ✅ PASA |
| CU-J08-11 | US-034 | CA-5 | RabbitMQ | ✅ PASA |
| CU-J08-12 | US-034 | CA-7 | RabbitMQ | ✅ PASA |
| CU-J08-13 | US-034 | CA-2, CA-8 | DLQ | ✅ PASA |
| CU-J08-14 | US-034 | CA-8 | DLQ | ✅ PASA |
| CU-J08-15 | US-034 | CA-8, US-038 | DLQ | ✅ PASA |
| CU-J08-16 | US-034 | CA-9 | DLQ | ✅ PASA |
| CU-J08-17 | US-034 | CA-10 | Circuit Breaker | ✅ PASA |
| CU-J08-18 | US-034 | CA-10 | Circuit Breaker | ✅ PASA |
| CU-J08-19 | US-001/000 | CA-7, CA-1 | Degradación | ✅ PASA |
| CU-J08-20 | US-017 | CA-05, CA-11 | Degradación | ❌ FALLA |
| CU-J08-21 | US-017 | CA-17 | SLA | ❌ FALLA |
| CU-J08-22 | US-017 | CA-12 | PII | ❌ FALLA |
| CU-J08-NEG-01 | US-017 | CA-14 | Negativo | ❌ FALLA |
| CU-J08-NEG-02 | US-017 | CA-07 | Negativo | ❌ FALLA |
| CU-J08-NEG-03 | US-017 | CA-18 | Negativo | ❌ FALLA |
| CU-J08-NEG-04 | US-034 | CA-7 | Negativo | ✅ PASA |

---

## Resumen de Cobertura J-08

| US | CAs Cubiertos | Total CAs US | % Cubierto en J-08 |
|----|:------------:|:----------:|:-------------------:|
| US-017 | CA-01 a CA-07, CA-09 a CA-18 | 18 | **94%** (17 CAs) |
| US-034 | CA-1 a CA-10 | 10 | **100%** (10 CAs) |
| US-000 | CA-1, CA-7 | 4 | **50%** (2 CAs — resto cubierto en J-SEC) |
| US-001 | CA-7 | 27 | **4%** (1 CA — resto cubierto en J-04) |

---

## Brechas Críticas Descubiertas (Pre-Ejecución)

| # | Brecha | Severidad | US | Escenario | Acción Requerida |
|---|--------|:---------:|:--:|-----------|-----------------|
| B-16 | US-017 COMPLETAMENTE SIN DESARROLLAR | 🔴 P0 | US-017 | CU-J08-01 a 08, 20-22 | Implementar Event Store, CQRS Write/Read, Auto-Claim, Rollback Saga |
| B-17 | Tabla `form_event_store` no existe | 🔴 P0 | US-017 | CU-J08-01 | DDL pendiente: 9 columnas definidas en CA-06 |
| B-18 | Tabla `task_drafts` no existe | 🟠 P1 | US-017 | CU-J08-04 | DDL pendiente: drafts con TTL 72h |
| B-19 | Rollback Compensatorio no implementado | 🔴 P0 | US-017 | CU-J08-07 | Implementar Saga reversa con evento `FORM_SUBMIT_ROLLED_BACK` |
| B-20 | Cifrado AES-256 at-rest para PII no implementado | 🟠 P1 | US-017 | CU-J08-22 | Integrar Azure Key Vault para cifrado de `payload_json` |
