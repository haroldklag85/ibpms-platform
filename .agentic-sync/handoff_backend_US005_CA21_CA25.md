# Contrato de Arquitectura Backend (Iteración 20 | US-005: CA-21 a CA-25)

**Rol:** Desarrollador Backend Java/Spring Boot.
**Objetivo:** Habilitar endpoints de exploración del repositorio (RepositoryService Camunda) y construir el Security Filter defensivo para Despliegues.

## 📋 Contexto y Criterios de Implementación:

### Tarea 1: Escudo RBAC para el Despliegue (CA-21)
El Frontend deshabilitará el botón, pero el Backend nunca confía en el cliente.
*   Ve a `BpmnDesignController.java` y localiza tu endpoint `POST /deploy`.
*   Asume/Mockea la lectura del header `X-Mock-Role`. Si el valor es diferente a `BPMN_Release_Manager`, lanza inmediatamente una Excepción/Status `HTTP 403 Forbidden` (`"Acceso Denegado. Se requiere el rol BPMN_Release_Manager para comisionar modelos en Producción"`).

### Tarea 2: Catálogo de Modelos Base (CA-23)
*   Crea el Endpoint `GET /api/v1/design/processes`.
*   Dentro, utiliza `repositoryService.createProcessDefinitionQuery().latestVersion().list()`.
*   Devuelve un DTO Arreglo conteniendo `key`, `name`, `version`, y `deployDate` (Puedes mockear fechas si el query simple no lo trae mapeado directamente).

### Tarea 3: Endpoint de Extracción XML (CA-23 Click)
*   Crea el Endpoint `GET /api/v1/design/processes/{key}/xml`.
*   Usa el `repositoryService.getProcessModel(processDefinitionId)` de Camunda para retornar la cadena XML pura textual del modelo asociado a la Key, permitiendo al frontend pintarlo en el Lienzo de inmediato.

*Nota: Los criterios CA-22, CA-24 y CA-25 son puramente de Interfaz de Usuario (Zoom, Paleta) y no requieren integración de Spring Boot.*

## 🛑 MANDATO LOCAL GATEKEEPER
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` a principal.
Compila tus Controladores, enciende la protección 403 y envuelve todo en un stash:
`git add . && git commit -m "chore(sync): save progress" && git push origin HEAD`

Informa textualmente la confirmación del guardado.
