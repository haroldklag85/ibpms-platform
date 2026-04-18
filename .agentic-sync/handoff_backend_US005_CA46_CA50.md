# Contrato de Arquitectura Backend (Iteración 25 | US-005: CA-46, CA-49, CA-50)

**Rol:** Desarrollador Backend Java/Spring Boot.
**Objetivo:** Suministrar los insumos de metadatos (Modelos de Contrato) para que el Frontend ensamble la matriz de mapeo visual tipado, y engordar el analizador de vuelos.

## 📋 Contexto y Criterios de Implementación:

### Tarea 1: API de Esquemas del Hub (CA-49)
*   En el `IntegrationHubController`, crea un Endpoint GET `/api/v1/integrations/connectors/{id}/schema`.
*   Retorna la estructura que espera la API objetivo (Ej: Si id = "o365_mail", retorna `[{field: "to", type: "String"}, {field: "body", type: "String"}, {field: "isHtml", type: "Boolean"}]`).

### Tarea 2: Mock API del Diccionario Zod de Proceso (CA-50)
*   En el `BpmnDesignController` (o un nuevo `ProcessVariableController`), expide `GET /api/v1/design/processes/{key}/variables`.
*   Retorna un arreglo Mock de variables detectadas en el formulario del proceso: `[{name: "cliente_email", type: "String"}, {name: "monto", type: "Number"}, {name: "aprobado", type: "Boolean"}]`.

### Tarea 3: Refactor Pre-Flight Analyzer (CA-46)
*   Modifica el Pre-Flight Analyzer Endpoint. Mapea el XML buscando `bpmn:messageEventDefinition` en eventos intermedios.
*   Si encuentra un evento de mensaje y NO tiene un conector asignado (`camunda:delegateExpression` o `camunda:connector`), devuelve una `Warning` (Severidad Amarilla): `"MessageEvent sin conector API asociado. Considere crear el conector en el Hub y migrar a Service Task."`

*(Nota: CA-47 y CA-48 pertenecen exclusivamente al scope Frontend).*

## 🛑 MANDATO LOCAL GATEKEEPER
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` a principal.
Desplegar lógicas, y archivar en stash locutado al comandante:
`git add . && git commit -m "chore(sync): save progress" && git push origin HEAD`

Informa textualmente la confirmación del guardado.
