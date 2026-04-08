# Contrato de Arquitectura Backend (Iteración 22 | US-005: CA-32 a CA-34)

**Rol:** Desarrollador Backend Java/Spring Boot.
**Objetivo:** Desarrollar los controladores de cambio de estado del repositorio (Suspensión/Archivado en BD/Camunda) y fabricar un enrutador primario para aprobación de pases.

## 📋 Contexto y Criterios de Implementación:

*(Nota: CA-31, CA-33 y CA-35 son puramente interactividad y marcadores XML gestionados por el UI/XML-Parser).*

### Tarea 1: Suspender/Archivar Diagramas Seguros (CA-32)
*   Añade el Controller/Endpoint `POST /api/v1/design/processes/{key}/archive`.
*   **Lógica Defensiva (Regla de Oro):** Invoca el `runtimeService` de Camunda y cuenta las instancias de ejecución vivas asociadas a la Key `processDefinitionKey` (`.count()`).
*   **Bloqueo:** Si `count > 0`, debes abortar y devolver un `HTTP 409 Conflict` (Ej: `"No se puede archivar. Existen X instancias vivas. Se requiere anulación o migración total."`).
*   **Aprobación:** Si `count == 0`, usa la API `repositoryService.suspendProcessDefinitionByKey(key)` de Camunda, emulando el Archiveo para que no vuelva a ser instanciable. (O actualiza el bit de `status="ARCHIVADO"` en tu tabla custom DTO devuelta por el Explorador de Catálogo).

### Tarea 2: Bandeja de Solicitud de Despliegue (CA-34)
*   Añade el Endpoint Mock: `POST /api/v1/design/processes/{key}/request-deploy`.
*   Este Endpoint recibe temporalmente el XML del Borrador en Base de Datos (Marcado con State "PENDIENTE_APROBACION").
*   Como Workdesk Mock: Crea una Tarea de Sistema o UserTask dentro de Camunda (o insértala en BD de tablas Mock) asignada al Grupo `BPMN_Release_Manager` informando: `"Aprobar despliegue de diagrama [Nombre]"`.

## 🛑 MANDATO LOCAL GATEKEEPER
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` a principal.
Agrega las lógicas del repositorio, refina los Endpoints en tu DesignController y empaca en un stash puro:
`git add . && git commit -m "chore(sync): save progress" && git push origin HEAD`

Informa textualmente la confirmación del guardado.
