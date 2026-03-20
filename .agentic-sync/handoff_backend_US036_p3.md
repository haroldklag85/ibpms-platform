# Handoff Backend - Iteración 38 (US-036: CA-11 a CA-15)

## Propósito
Culminar el motor RBAC integrando Roles Dinámicos desde el motor de procesos Camunda, acoplando el Botón Táctico de Exorcismo (Kill-Switch extendido) y abriendo por primera vez un Bypass Anónimo de seguridad transaccional para formularios públicos.

## Criterios de Aceptación Cubiertos (Backend)
* **CA-11 y CA-12 (No-Op / Boundary Guidelines):** Reglas pasivas. No crear MFA propio, confiar 100% en EntraID. No ofuscar columnas a nivel API (Responsabilidad frontal).
* **CA-13 (Roles Dinámicos BPMN Lanes):** Modificar el enrutador de tareas o `BpmTaskService` (creado en la Iteración 36). Al consultar bandeja, cruzar los *Roles Estáticos* (BD JPA) con los *Roles Dinámicos* (Variables de proceso inyectadas por los Expression Lanes de Camunda).
* **CA-14 (Kill-Session Táctico):** Si bien en el US-048 se validaba el `isActive` en cada Request, aquí se demanda un exterminio forzado TCP/Caché. Implementar endpoint `POST /api/v1/admin/users/{id}/kill-session` que purgue activamente el JWT invalidándolo en la capa in-memory (o lista negra JPA temporal) para un Log-Out fulminante antes de que expire el TTL.
* **CA-15 (Bypass Anónimo - URLs Públicas):** Abiertura controlada del Firewall. Modificar el `SecurityWebFilterChain` (Spring Security) para que los endpoints `POST /api/v1/process/{key}/start-anonymous` sean `permitAll()`. Validar internamente que dicho proceso BPMN tenga la bandera `isPublic = true` en base de datos; si no, escupir HTTP 403.

## Tareas Java (Prioridad 1 - Tarea Bloqueante)
1. Modificar Filtros de Seguridad para crear el Bypass Anónimo (`permitAll()`).
2. Implementar Lista Negra de JWT o purga de caché para el `kill-session`.
3. Cruzar Colecciones Camunda (Static Roles + Dynamic Variables).
