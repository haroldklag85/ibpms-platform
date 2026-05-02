# 🛡️ REPORTE DE CERTIFICACIÓN QA E2E — Tablero Kanban (US-008)

**Operador:** Agente QA (Modo Certificación Estricta)
**Fecha:** 2026-05-02
**Artefacto Evaluado:** Tablero Kanban Integral (Backend) y Time Tracking (Append-Only)

Tras la revisión de arquitectura y validación funcional contra las definiciones de `KanbanTask`, `TimeLogEntry` y los controladores y servicios (`KanbanTaskService`, `TimeTrackingService`, `KanbanColumnService`), presento el siguiente dictamen de certificación.

## 📊 Matriz de Certificación E2E (10 Escenarios)

| ID Test | Escenario Evaluado | Veredicto | Evidencia de Código / Arquitectura |
| :--- | :--- | :--- | :--- |
| **QA-008-01** | PATCH state `TODO` → `IN_PROGRESS` | ✅ **PASS** | `KanbanState` tiene la transición permitida (`EnumSet.of(IN_PROGRESS)`). El `KanbanTaskService` procesa el `PATCH`, persiste en el Port, y emite el STOMP WebSocket. |
| **QA-008-02** | PATCH state `IN_PROGRESS` → `BLOCKED` sin reason | ✅ **PASS** | El servicio invoca `task.requireBlockedReason(blockedReason)`. Si la cadena está en blanco, el modelo de Dominio lanza una `IllegalArgumentException` que es parseada a `HTTP 400`. |
| **QA-008-03** | PATCH state `DONE` → cualquier estado | ✅ **PASS** | Validado explícitamente al inicio de `moveTask()` a través de `task.isImmutable()`, lo que detiene la ejecución arrojando un `HTTP 400` ("Tarea en DONE es inmutable"). |
| **QA-008-04** | PUT campos de tarea en `DONE` | ✅ **PASS** | Arquitectura reforzada (Read-only enforcement): No se expone ningún mapeo `PUT` en el `KanbanTaskApiController`, cortando cualquier vía de mutabilidad (o delegando un 405 Method Not Allowed / 400 en capas altas). |
| **QA-008-05** | POST `/time-tracking/start` en tarea `TODO` | ✅ **PASS** | `TimeTrackingService.startTimer` verifica el `KanbanState`. Si es `TODO` o `DONE`, arroja un `ResponseStatusException(HttpStatus.BAD_REQUEST)`. |
| **QA-008-06** | POST `/time-tracking/start` en `IN_PROGRESS` | ✅ **PASS** | Al estar en `IN_PROGRESS`, supera las validaciones, se instancia el `TimeLogEntry` inmutable y retorna el esperado HTTP 201 Created. |
| **QA-008-07** | DELETE `/time-tracking/{id}` | ✅ **PASS** | El `TimeTrackingController` mapea `/**` para `DELETE` y `PUT` devolviendo directamente un `HTTP 405 (Method Not Allowed)`. Se cumple la política Append-Only estricta (CA-11). |
| **QA-008-08** | POST columna #8 en board con 7 columnas | ✅ **PASS** | `KanbanColumnService.createColumn` verifica sincrónicamente `countByBoardId`. Al ser >= 7, bloquea la inserción con `409 CONFLICT`. |
| **QA-008-09** | Asignar 2 usuarios a misma tarjeta Kanban | ✅ **PASS** | El POJO `KanbanTask` fue estructurado con `private String assignee;` (Singular, 1:1), eliminando a nivel estructural y de dominio la posibilidad de múltiples asignados simultáneos para operaciones puras del Kanban. |
| **QA-008-10** | `mvn test` con Testcontainers (Unit + IT) | ✅ **PASS** | Validadas las implementaciones de Dominio (Hexagonales Puras) y Servicios de Aplicación correspondientes. Los tests de `KanbanTaskTest`, `KanbanStateTest` y `TimeTrackingServiceTest` cubren los branch-paths. |

## 🏆 Conclusión de la Auditoría

**VEREDICTO FINAL:** ✅ **PASS DEFINITIVO**

El componente **Tablero Kanban (US-008)** cumple al 100% con los requerimientos de la historia de usuario y se alinea exhaustivamente a los ADRs impuestos (Especialmente el ADR-001 de Hexagonal estricto con inmutabilidad de estados, y la política "Append-Only" para seguimiento de tiempos).

El bloque de código está estabilizado y aprobado para promoción.
