# Handoff Backend - Iteración 36 (US-036: CA-1 a CA-5)

## Propósito
Desplegar el Motor de Autorización RBAC en Java. Se establecerá la granularidad de permisos sobre procesos, la figura del Super Admin inmutable, y la segregación de datos (Row-Level Security) para la visualización de colas de Camunda.

## Criterios de Aceptación Cubiertos (Backend)
* **CA-1 (Hibridación EntraID/Local):** Continuar soportando el esquema híbrido del IdP. Los Roles pueden originarse vía MS Graph o persistirse localmente en JPA.
* **CA-2 (Root Admin Inmutable):** Crear un `DataSeeder` (o script Flyway) que inyecte un usuario `[Super_Administrador]` (`ROLE_SUPER_ADMIN`) al arrancar. Los endpoints de `DELETE /users/{id}` o `PUT /users/{id}/deactivate` deben rechazar (Throw Exception 403) si el ID coincide con este Root Admin.
* **CA-3 (Clonación por Plantilla):** Estructura transaccional para agrupar Permisos individuales (`PermissionEntity`) dentro de `RoleEntity`. Permitir asignación masiva mediante endpoints Batch.
* **CA-4 (Segregación Iniciador vs Ejecutor):** Crear la entidad `ProcessPermission` (o atributos equivalentes) atada al Rol, con booleanos estrictos: `can_initiate_process` y `can_execute_tasks`.
* **CA-5 (Privacidad de Colas - Row Level Security):** **[CRÍTICO]** Interceptar las peticiones del Workdesk hacia Camunda. Todo Query de `/tasks` debe venir pre-filtrado forzosamente añadiendo `taskAssignee = currentUser` o validando suscripciones a colas púbicas, impidiendo que escaneen toda la BD.

## Tareas Java (Prioridad 1 - Tarea Bloqueante)
1. Modificar esquema de Base de Datos para soportar Permisos Granulares atados a Roles.
2. Codificar la inmutabilidad del Super Administrador.
3. Inyectar el Filtro de Seguridad a nivel de Datos (RLS) en los repositorios o clientes de Camunda.
