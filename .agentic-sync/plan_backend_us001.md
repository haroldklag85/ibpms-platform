# Plan de Implementación Backend - US-001 (Hybrid Workdesk CQRS Remediation)

## 1. Análisis del Requerimiento
Evaluación técnica del contrato `handoff_backend_us001.md`:
Es necesario tapar tres brechas fundamentales en la vista CQRS del Workdesk: Búsqueda dinámica con JPQL, adopción de comunicación Full-Duplex (STOMP WebSockets) para notificaciones de "Reclamo de Tarea", y protección contra fallas en cascada (Circuit-breaking parcial en el controlador).

## 2. Cambios Arquitectónicos Propuestos

### A. Gestión de Dependencias
- Modificar `pom.xml` para integrar `spring-boot-starter-websocket`.

### B. Setup de Websockets (CA-6)
- Crear `com.ibpms.poc.infrastructure.config.WebSocketConfig.java` habilitando Broker (`/topic`) y Endpoint STOMP (`/ws-endpoint`).
- Modificar Event Listeners o Casos de Uso (ej., `reclamarTarea`) para inyectar `SimpMessagingTemplate` y despachar `{ "event": "TASK_CLAIMED", "taskId": "..." }` a `/topic/workdesk.updates`.

### C. Repositorio CQRS (CA-2 y CA-4)
- Modificar `WorkdeskProjectionRepository.java` agregando:
  ```java
  @Query("SELECT w FROM WorkdeskProjectionEntity w WHERE " +
         "(:search IS NULL OR LOWER(w.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(w.originalTaskId) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
         "(:delegatedUserId IS NULL OR w.assignee = :delegatedUserId)")
  Page<WorkdeskProjectionEntity> findDynamicInbox(@Param("search") String search, @Param("delegatedUserId") String delegatedUserId, Pageable pageable);
  ```

### D. Capa Controladora y Tolerancia a Fallos (CA-7)
- Crear/Refactorizar `WorkdeskResponseDTO.java` como Wrapper Paginado, incluyendo la propiedad `boolean degraded`.
- Refactorizar `WorkdeskQueryController.java` (`/api/v1/workdesk/global-inbox`):
  - Añadir de forma opcional `search` y `delegatedUserId`.
  - Envolver la invocación al Service/Repository en un bloque `try-catch`. En caso de `PersistenceException` o fallas similares, atrapar el error, loguearlo, y retornar `HTTP 200 OK` con un `WorkdeskResponseDTO(degraded = true, content = emptyList())`.

## 3. Plan de Verificación
- Ejecutar `mvn clean compile` para certificar que la compilación es exitosa.
- Comprobar que Spring levanta el contexto de WebSockets sin conflictos en beans inyectados.

---
**Solicitud de Aprobación Documentada:** Este plan está sujeto a revisión restrictiva. Se aguarda la directiva formal del Arquitecto Líder.
