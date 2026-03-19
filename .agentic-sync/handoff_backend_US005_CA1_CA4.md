# Contrato de Arquitectura Backend (Iteración 15 - US-005: CA-1 al CA-4)

**Rol:** Desarrollador Backend Java/Spring Boot.
**Objetivo:** Desarrollar el Endpoint Core de Despliegue BPMN y el Motor de Analizador Semántico "Pre-Flight" para proteger a Camunda de XMLs inválidos o incompletos.

## 📋 Contexto y Criterios de Aceptación (Alcance Estricto):
En esta Iteración 15 saltamos al corazón orquestador de la plataforma: El diseño de Procesos BPMN. Debes construir la API que recibe el lienzo dibujado por el usuario y dictaminar si es ejecutable o no.

*   **CA-1 (Despliegue General):** Crea el endpoint `POST /api/v1/design/processes/deploy`. Debe recibir `multipart/form-data` con un archivo `.bpmn` (BPMN 2.0 XML). Si el archivo es válido y superó las pruebas semánticas, debes retornar HTTP 201 Created simulando (o ejecutando) el despliegue al Engine Camunda.
*   **CA-2 (Control de Diagrama Roto):** Implementa el parseador XML (sugiero `camunda-bpmn-model-api`). Si el proceso carece de un *End Event*, el sistema debe arrojar un HTTP 422 (Unprocessable Entity) con un payload estructural de errores: `[{ node: "Process_1", message: "Falta End Event" }]`.
*   **CA-3 (Análisis Semántico Complejo):** El motor "Pre-Flight" debe iterar los nodos del XML. 
    1. Si hay `ServiceTask`, verificar que exista la propiedad de ejecución (ej. `camunda:delegateExpression` o `class`). 
    2. Si hay `UserTask`, verificar que tenga `camunda:formKey`. 
    3. Si hay `ExclusiveGateway`, arrojar advertencia si no tiene Flujo por Defecto (`default` property). Si algo falla, sumar a la lista de Errores 422.
*   **CA-4 (Validación Estricta de Start Event):** El `StartEvent` ES OBLIGATORIO que posea un `camunda:formKey` para permitir instancias manuales. Caso contrario, suma a los errores 422.

## 📐 Reglas de Desarrollo:
1. Usa el API oficial de Camunda para parseo si está en el `pom.xml` (`org.camunda.bpm.model:camunda-bpmn-model`), o en su defecto un DocumentBuilder XML seguro contra XXE (XML External Entities).
2. Es preferible que el controlador retorne un DTO robusto como `DeploymentValidationResponse` con listas separadas de `errors` y `warnings`.

## 🛑 MANDATO LOCAL GATEKEEPER (REGLA DE ORO)
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` a principal.
Congela tu Motor Semántico Pre-flight en un stash cuando superes la compilación local:
`git stash save "temp-backend-US005-ca1-ca4"`

Informa textualmente la confirmación del guardado.
