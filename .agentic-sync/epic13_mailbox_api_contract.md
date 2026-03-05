# Backend Handoff: Epic 13 (SAC Mailbox configuration)
**Target:** Frontend Agent
**From:** Backend Agent
**Status:** BUILD SUCCESS

Hola Frontend. He finalizado la lógica de Azure Key Vault, las pruebas en vivo contra Microsoft Graph, el Polling Job con Redis y el Soft-Delete Listener preventivo.
Quedas autorizado para iniciar la construcción de la Pantalla 15 (`SacConfigManager.vue`) operando estrictamente sobre estos Endpoints (Mock-First u OpenAPI Proxy).

## API Contract (Endpoints & DTOs)

### 1. Test Connection (Graph OAuth)
- **POST** `/api/v1/mailboxes/test-connection`
- **Request Payload:**
```json
{
  "tenantId": "string",
  "clientId": "string",
  "rawClientSecret": "string"
}
```
- **Response `200 OK`:** `{ "status": "SUCCESS", "message": "Conexión a MS Graph validada." }`
- **Response `400 Bad Request`:** (Ej. `ConnectionValidationException` - Invalid Secret)

### 2. Save Mailbox Configuration
- **POST** `/api/v1/mailboxes`
- **Request Payload:**
```json
{
  "alias": "string (unique)",
  "protocol": "GRAPH", 
  "tenantId": "string",
  "clientId": "string",
  "rawClientSecret": "string",
  "defaultBpmnProcessId": "string",
  "active": true
}
```
*(Nota: El backend enmascarará el `rawClientSecret` inyectándolo en Azure Key Vault de forma transparente. El FE envía el string plano solo en el POST de creación o actualización, y nunca debe recuperarlo en GET).*
- **Response `201 Created`:** Retorna id.

### 3. List Mailboxes
- **GET** `/api/v1/mailboxes`
- **Response `200 OK`:**
```json
[
  {
    "id": "uuid",
    "alias": "Soporte Nivel 1",
    "protocol": "GRAPH",
    "tenantId": "org.onmicrosoft.com",
    "clientId": "appid...",
    "defaultBpmnProcessId": "process_support_triage",
    "active": true,
    "createdAt": "2026-03-04T10:00:00Z"
  }
]
```

### 4. Toggle Emergency Pause (CA-8)
- **PATCH** `/api/v1/mailboxes/{id}/status`
- **Request Payload:** `{ "active": false }`
- **Response `200 OK`:** `{ "id": "uuid", "active": false }`

Acatando la directiva de la Épica, **no envíes `protocol=IMAP`** en la creación. Construye el banner de deprecación y enlaza el botón "Guardar" a la promesa del endpoint de `test-connection`.
Quedo a tu disposición.
