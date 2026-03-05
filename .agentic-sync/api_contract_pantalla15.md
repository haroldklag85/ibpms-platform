# Contrato API: Pantalla 15 (CRUD Buzones Azure KV)

Este documento es el Hand-off oficial del Agente Backend hacia el Agente Frontend (Vue 3). Garantiza la integración fluida con la arquitectura Zero-Trust.

## Endpoints Disponibles

### 1. `POST /api/v1/admin/mailboxes/test-connection`
**Descripción:** Efectúa un ping en VIVO contra MS Graph usando las credenciales crudas ingresadas en la UI para validar si Microsoft responde un token a la app. OBLIGATORIO antes del Guardar.
- **Request Body (JSON):**
```json
{
  "tenantId": "string (UUID)",
  "clientId": "string (UUID)",
  "rawClientSecret": "string (Texto Plano temporal desde el UI)",
  "protocol": "GRAPH"
}
```
- **Response `200 OK`**: `{ "status": "VALIDATED", "message": "Microsoft Graph Access Granted" }`
- **Response `400 Bad Request`**: (Excepción `ConnectionValidationException` mapeada).
```json
{
  "error": "ConnectionValidationException",
  "message": "Fallo al autenticar contra Microsoft Graph. Verifique las credenciales."
}
```

### 2. `POST /api/v1/admin/mailboxes`
**Descripción:** Guarda la configuración en DB. El backend captura el `rawClientSecret` inyectado, lo empuja silenciosamente a *Azure Key Vault*, obtiene un `vaultId`, lo escribe a su tabla, y desecha el raw.
- **Request Body (JSON):**
```json
{
  "alias": "string (alias corporativo ej. SAC Legal)",
  "emailAddress": "string",
  "tenantId": "string",
  "clientId": "string",
  "rawClientSecret": "string (secreto fresco para guardar el Secret en el Vault)",
  "protocol": "GRAPH",
  "defaultBpmnProcessId": "string (CA-3: Default Process for Route Fallback)",
  "isActive": true
}
```
- **Response `201 Created`**: Devuelve la entidad persistida (con el `keyVaultReferenceId` expuesto, borrando la propiedad de raw secret por motivos obvios).

### 3. `PUT /api/v1/admin/mailboxes/{id}/status`
**Descripción:** Operación "Emergency Toggle" (CA-8) de pausa de succión.
- **Request Body:** `{ "isActive": false }`
- **Response `200 OK`**: Cambia el estado bloqueando al CRON Job Polling Worker.

### Reglas de Negocio a aplicar en Vue Frontend
1. El botón Guardar de la UI **debe** permanecer `disabled=true` hasta que endpoint `test-connection` retorne un estricto `200`. Si se modifica un char en el Tenant o Secret, debe volver a bloquearse.
2. Si un usuario intenta enviar config IMAP manual, debe estallar localmente sin llamar al backend. IMAP ha sido depreciado.
