# 📑 API Contract: Configuración Administrativa de Buzones SAC (Pantalla 15)

## Backend Handoff Status
- **Component**: `ibpms-core` (Monolith)
- **Status**: ✅ BUILD SUCCESS, JUnit Tests Passed.
- **Ready for Frontend**: YES.

---

## 🚀 Endpoints Disponibles

**Base Path:** `/api/v1/admin/mailboxes`

### 1. `GET /` (Listar Buzones)
Retorna la lista de buzones configurados.
**Response (200 OK):**
```json
[
  {
    "id": "uuid",
    "alias": "reclamos@empresa.com",
    "tenantId": "t-1234",
    "clientId": "c-5678",
    "protocol": "GRAPH",
    "isActive": true,
    "defaultBpmnProcessId": "proceso_reclamo_v1",
    "createdAt": "2026-03-04T10:00:00"
  }
]
```

### 2. `POST /test` (Probar Conexión - Ping en Vivo)
Realiza la validación en vivo contra Microsoft Graph API. **El Frontend debe habilitar el botón "Guardar" SOLO si esto retorna 200.**
**Payload:**
```json
{
  "alias": "reclamos@empresa.com",
  "tenantId": "t-1234",
  "clientId": "c-5678",
  "clientSecret": "supersecreto",
  "protocol": "GRAPH",
  "defaultBpmnProcessId": "proceso_reclamo_v1"
}
```
**Response (200 OK):** `"Conexión MS Graph exitosa"`
**Error (400 Bad Request):** `"ConnectionValidationException: Credenciales rechazadas..."`

### 3. `POST /` (Crear / Guardar Buzón)
Guarda la configuración formalmente. El secreto viajará al KeyVault en Backend, nunca se guarda en texto plano en DB. Ejecuta el ping de validación internamente antes de insertar.
**Payload:** Igual a `POST /test`.
**Response (201 Created):** Retorna el objeto `SacMailboxDTO` (con el ID generado y sin el secret).

### 4. `PATCH /{id}/status?active=true|false` (Toggle de Emergencia)
Pausa temporalmente la succión de correos para el polling concurrente.
**Response (204 No Content)**

---

## 🔒 Reglas de Seguridad (Handoff a Frontend)
1. Frontend **no debe** permitir el puerto IMAP. El select form debe forzar OAuth2 / Microsoft Graph.
2. Botón `Guardar` arranca deshabilitado (`disabled=true`).
3. El frontend invocará `POST /test`. Si arroja 200, el botón `Guardar` se habilita.
4. Las pruebas en Vite (`vitest`) deberán certificar este comportamiento de validación asíncrona en la UI.
