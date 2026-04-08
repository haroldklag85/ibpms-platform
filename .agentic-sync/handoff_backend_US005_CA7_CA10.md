# Contrato de Arquitectura Backend (Iteración 17 | US-005: CA-7 al CA-10)

**Rol:** Desarrollador Backend Java/Spring Boot.
**Objetivo:** Desarrollar el motor evaluador de "Migration Plans" de Camunda, garantizando el bloqueo estricto si existen disrupciones topológicas y blindando la API contra parcheo manual de variables.

## 📋 Contexto y Criterios de Aceptación:
Nos encargamos del gobierno de transición de diagramas BPMN (Instancias V1 siendo empujadas a flujos V2).

*   **Endpoint Evaluador - Bloqueo Topológico (CA-9):**
    *   Crea `GET /api/v1/design/processes/{processDefinitionKey}/instances/migratable?sourceVersion=1&targetVersion=2`.
    *   Este endpoint lista las instancias activas de la `sourceVersion`. Por cada una, el Backend debe evaluar semánticamente si se puede mapear (MigrationPlan). Si un Token está detenido en una `UserTask` que fue **borrada** en la `targetVersion`, el token es huérfano. 
    *   Debe retornar un DTO indicando: `[{ instanceId: "123", isMigratable: false, reason: "El nodo actual no existe en la topología destino" }]`.
*   **Endpoint Ejecutor y Grandfathering (CA-7 y CA-10):**
    *   Crea `POST /api/v1/design/processes/migrate`.
    *   **CA-7:** El motor JAMÁS migra instancias automáticamente cuando se hace Deploy. Solo lo hace cuando se llama a este POST de forma explícita con un lote de IDs.
    *   **CA-10 (Anti Data-Patching):** El Payload `MigrationRequestDTO` **DEBE TENER EL RECHAZO TÉCNICO E IMPLÍCITO** (E.g `@JsonIgnoreProperties(ignoreUnknown = true)`) a cualquier Map de `variables`. Solo aceptará la lista de `instanceIds` a migrar.

## 📐 Reglas de Desarrollo:
1. Usaremos DTOs simulando la lógica real de *Camunda Migration API*. Construye los controladores y servicios (`ProcessMigrationService.java`) con lógica defensiva.
2. Loguea exhaustivamente cada nodo desconectado en el proceso de evaluación CA-9.

## 🛑 MANDATO LOCAL GATEKEEPER (REGLA DE ORO)
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` a principal.
Congela tu Motor Evaluador en un stash cuando superes la compilación local:
`git add . && git commit -m "chore(sync): save progress" && git push origin HEAD`

Informa textualmente la confirmación del guardado.
