# Handoff Backend - Iteración 37 (US-036: CA-6 a CA-10)

## Propósito
Completar la Fase 2 de la Matriz RBAC. Esta iteración añade profundidad a la gestión de accesos implementando Herencia Piramidal de Roles, Delegación Temporal (Vacaciones), Aprovisionamiento Automático (Just-in-Time) y Service Accounts (Robots M2M).

## Criterios de Aceptación Cubiertos (Backend)
* **CA-6 (Herencia Piramidal):** Modificar `RoleEntity` para soportar recursividad (ej. `parentRole_id`). Durante la autorización, si un usuario tiene Rol A que hereda de Rol B, debe obtener los permisos combinados.
* **CA-7 (Soft-Delete Inmutable):** Asegurar que el endpoint `DELETE` sobre un usuario lance una Excepción o fuerce un UPDATE (`isActive = false`). Nunca hacer borrado físico `DELETE FROM users` para no corromper la tabla de auditoría BPMN.
* **CA-8 (Ciudadano Interno):** En el filtro de Autenticación, si un Identity Provider SSO envía un JWT de un empleado nuevo que no existe en BD, insertarlo automáticamente (`Just-in-Time Provisioning`) con el rol por defecto `[Ciudadano_Interno]` (mínimo nivel).
* **CA-9 (Delegación Autónoma Temporal):** Crear `DelegationEntity` (`delegator_id`, `substitute_id`, `start_date`, `end_date`). Modificar la evaluación de permisos para que, si el rango de fecha está activo, el `substitute` herede los roles de `delegator`.
* **CA-10 (Robots M2M):** Crear CRUD para `ServiceAccountEntity`. Generar y devolver (por única vez) un API Key plano genérico (`SecureRandom`), pero almacenar su Hash SHA-256. Atar el API Key a un `RoleEntity` para que el Robot opere de igual a igual en Camunda.

## Tareas Java (Prioridad 1 - Tarea Bloqueante)
1. Codificar la entidad `DelegationEntity` y `ServiceAccountEntity`.
2. Modificar el interceptor de Seguridad y el *Token Service* para inyectar Roles Heredados y Roles de Delegación Temporal activa.
3. Asegurar aprovisionamiento JIT.
