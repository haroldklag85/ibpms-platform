# Handoff Bloque 3: Backend -> Frontend (Service Delivery)

**Ref:** US-024, US-025, US-026
**Estado:** Endpoints Codificados y Capa Anti-Corrupción Terminada.

El Agente de Backend ha finalizado la construcción de los endpoints para el intake y consultas 360. A continuación el detalle técnico para que integren Axios en el Frontend:

## 1. Instanciación Manual (Plan B - Intake) - US-024
*(Pantalla 16 - Solo perfiles Intake Admin)*
* **Endpoint:** `POST /api/v1/service-delivery/manual-start`
* **Auth:** Requerida (`Role_Admin_Intake`).
* **Payload:** `ManualStartDTO` (Incluye `definitionKey`, `businessKey`, `type` y `initialVariables`).
* **Respuesta:**
  * `201 Created`: Devuelve JSON con `{ "caseId": "uuid...", "status": "STARTED" }`.
  * `403 Forbidden`: Si el token no tiene el ROL indicado.

## 2. Vistas 360 del Cliente - US-025
*(Pantalla 17 - Consolidado)*
* **Endpoint:** `GET /api/v1/customers/{crmId}/cases360`
* **Auth:** Requerida.
* **Respuesta (JSON):** 
  ```json
  {
    "crmId": "101010",
    "totalCases": 2,
    "cases": [
       {
         "processInstanceId": "...",
         "definitionKey": "...",
         "businessKey": "...",
         "state": "ACTIVE",
         "startTime": "2026-03-01T10:00:00Z"
       }
    ]
  }
  ```
*(Nota: El Backend ahora cruza directamente el HistoryService de Camunda por la variable `crmId` para agrupar).*

## 3. Trazabilidad Pública B2C/B2B - US-026
*(Pantalla 18 - Tracking Externo)*
* **Endpoint:** `GET /api/v1/tracking/{trackingCode}`
* **Auth:** ANÓNIMO (Open).
* **Comportamiento:**
  * Enviar el ticket o consecutivo (`businessKey`) en la URL.
  * *NO expone* IDs del motor Camunda, aplica Anti-Corrupción devolviendo solo un `startedAt`, `isCompleted` y el `statusDescription` amigable de alto nivel.
  * Si el código no existe devuelve `404 Not Found`.
