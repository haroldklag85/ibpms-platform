---
author: Lead Architect
target: Backend Developer Agent (Java)
epic: US-001 Hybrid Workdesk
status: OPEN
---

# Hand-off Técnico: Backend Remediation (US-001)

Se requiere cerrar gaps en la vista maestra CQRS (`ibpms_workdesk_projection`). Se requiere enfoque **Strict Vertical Slice** (Entity Specification -> Controller).

### Misiones y Criterios de Aceptación (A Desarrollar)

#### 1. Repositorio y Filtro Dinámico (Gap CA-2 y CA-4)
- **Archivos:** `WorkdeskProjectionRepository.java` y `WorkdeskQueryController.java`
- **Comportamiento:**
  - El Controller `getGlobalInbox` actualmente solo recibe `Pageable`. Debe ser modificado para aceptar opcionalmente:
    - `@RequestParam(required = false) String search` (Keyword search bidireccional sobre Título y ID Original).
    - `@RequestParam(required = false) String delegatedUserId` (ID paramétrico para usurpar la vista).
  - Dado el requerimiento dinámico, usa Spring Data JPA `@Query` manuales tolerantes a Null (`WHERE (:search IS NULL OR w.title ILIKE %:search%)`) o implementa JPA Specification Builder (predicados).

#### 2. Soporte a Websockets - Eliminación Fantasma (Gap CA-6)
- **Archivos:** `WebSocketConfig.java` (Crearlo si no existe en Spring Boot), `KanbanTaskSyncListener.java`.
- **Comportamiento:**
  - Importar e instanciar `spring-boot-starter-websocket`.
  - Habilitar Message Broker Simple STOMP en `/ws-endpoint` y tópicos en `/topic/workdesk.updates`.
  - Cuando Camunda, Azure Service Bus o el Job interno asignen (Claim) la tarea a un usuario, mandar un JSON via `SimpMessagingTemplate.convertAndSend("/topic/workdesk.updates", payload)` con la directiva "TASK_CLAIMED" para que todos los demás clientes (Frontend) oculten forzosamente el UUID de su lista.

#### 3. Soporte de Tolerancia a Fallas en Controlador CQRS (Gap CA-7)
- Proveer captura de excepciones `@ExceptionHandler` o bloques en el `WorkdeskQueryController` para que, si el Sync de Camunda o DB Relacional crashea, la API jamás retorne un 500, sino una lista paginada vacía o un DTO envoltorio con el Warning paramétrico `{"degraded": true}` (Garantizando UI Amable).

*Finaliza la tarea con `mvn clean compile` para descartar fallos.*
