# Solicitud de Revisión: Estabilización Backend y Zero-Mock (Sprint 6.2)

Arquitecto Líder, he analizado el requerimiento BUG-S6-004 y BUG-S6-005 documentado en el handoff. El plan propuesto para mitigar los cuellos de botella e implementar el catálogo es el siguiente:

**1. Endpoint de Usuarios (Zero-Mock)**
He detectado que la lógica y seguridad base ya existe en `UserAdminController` y `UserService`. Implementaré un nuevo `UserController.java` en la ruta pública `/api/v1/users` que delega a `userService.listAll()`. El DTO retornado (`UserResponseDTO`) es seguro: expone ID, username, email, y roles, excluyendo cualquier rastro del hash BCrypt o datos transaccionales, cumpliendo con la regla Zero-Trust.

**2. Optimización Concurrencia (Delegaciones)**
En `AgileTaskService.java`, identifiqué un patrón de N+1 oculto y vulnerabilidad a deadlocks en `bulkAssign`, donde se itera un array de `UUID` y se hace `getTaskForUpdate` iterativo. Para sanear esto:
- Modificaré la rutina para **ordenar lexicográficamente la lista de Task IDs** antes del ciclo. Esto asegura que transacciones concurrentes adquieran locks en el mismo orden, eludiendo la condición circular del motor JPA/Postgres.
- Envolveré la captura en bloques try-catch detectando `OptimisticLockingFailureException`, registrando un Warning claro para auditoría en caso de que una tarea ya esté pisada.

**3. Optimizaciones en la Bandeja Workdesk**
Añadiré una migración SQL en Liquibase (`34-sprint6-bugs-fix.sql`) agregando el índice `idx_workdesk_search` a `ibpms_workdesk_projection(tenant_id, assignee)` para liquidar los timeouts persistentes durante los full table scans.

Ruego revisión para avanzar a la ejecución.
