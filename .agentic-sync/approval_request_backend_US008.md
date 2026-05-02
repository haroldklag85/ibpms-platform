# Approval Request - Backend Kanban Board (US-008)
**Epic:** Kanban & Agile Hub
**User Story:** US-008
**Date:** 2026-05-02
**Agent:** Antigravity (Backend Role)
**Status:** READY FOR REVIEW

## Resumen del Desarrollo
Se ha implementado el módulo backend Kanban puro siguiendo el ADR-001 (Arquitectura Hexagonal Estricta), separando el dominio (`KanbanTask`, `KanbanState`, `TimeLogEntry`) en POJOs inmutables, estableciendo casos de uso puros y creando adaptadores JPA (`KanbanTaskJpaAdapter`, `TimeLogJpaAdapter`). 

## Archivos Creados/Modificados

### Fase 1: Dominio Puro (`domain/model/kanban/`)
- `KanbanState.java`: Máquina de estado enum para Kanban, controlando reglas de transición y la inmutabilidad de la columna `DONE`.
- `KanbanTask.java`: POJO de dominio Kanban, con reglas de negocio intrínsecas (ej. `requireBlockedReason()`).
- `KanbanColumn.java`: POJO que representa columnas de trabajo en el Hub Kanban.
- `TimeLogEntry.java`: Value object append-only que encapsula lógica de tiempo (durationMinutes).

### Fase 2: Puertos (`application/port/`)
- `KanbanTaskPort.java`, `KanbanColumnPort.java`, `TimeLogPort.java`
- `MoveKanbanTaskUseCase.java` y `TrackTimeUseCase.java` para control de inversiones de dependencia.
- Creado método `commit` en `AuditLogUseCase` para auditoría manual con Javers.

### Fase 3: Servicios (`application/service/`)
- `KanbanTaskService.java`: Controla el flujo de `moveTask()`. Comprueba reglas de la máquina de estado, graba en base de datos, llama a WebSocket y dispara auditoría en Javers.
- `TimeTrackingService.java`: Provee métodos `startTimer()` y `stopTimer()` validando un único Timer por usuario en base al estado activo.
- `KanbanColumnService.java`: Expone la lógica de creación (máximo 7) y eliminación de columnas.
- `AuditLogService.java`: Actualizado para soportar llamadas explícitas de tracking desde el servicio (vía `commit`).

### Fase 4: Adaptadores JPA y REST (`infrastructure/`)
- `KanbanTaskEntity.java`: Modificada agregando `@Column blocked_reason`.
- Creadas `KanbanColumnEntity.java` y `TimeLogEntity.java` para soportar mapeo JPA.
- Creados repositorios `KanbanColumnRepository.java` y `TimeLogRepository.java`.
- Creados adaptadores hexagonales: `KanbanTaskJpaAdapter`, `KanbanColumnJpaAdapter`, `TimeLogJpaAdapter`.
- `KanbanTaskApiController.java`: Controladores para interactuar con columnas y estados (vía PATCH).
- `TimeTrackingController.java`: Controlador para registrar tiempos operando estricto sobre Append-Only (rechazando PUT/DELETE vía `405 Method Not Allowed`).

### Fase 5: Tests (OBLIGATORIOS)
- **Dominio Unitarios**: Creados `KanbanStateTest`, `KanbanTaskTest`, y `TimeLogEntryTest`.
- **Servicios Mockito**: Creados `KanbanTaskServiceTest`, `KanbanColumnServiceTest`, y `TimeTrackingServiceTest` asegurando que las interacciones del puerto de salida y la mensajería STOMP sean correctas.
- **Tests de Integración (E2E)**: Creados `KanbanStateTransitionIT` y `TimeTrackingIT` interactuando con Testcontainers PostgreSQL + Security JWT, comprobando fallas esperadas y status codes REST.

## Resultados de Compilación y Calidad
✅ **Build & Test Status**: `BUILD SUCCESS` (Verificado con `mvn clean test`).  
✅ **Arquitectura**: 100% CMMN-Free y Arquitectura Hexagonal.  
✅ **Seguridad**: Validación de estado JWT activa en controllers. Append-only para base de datos de auditoría de tiempos.  
