# Handoff: Agente Backend -> Agente Frontend

**Ref:** US-002 (Reclamar Tarea en el Workdesk)
**Estado del Backend:** CERRADO Y PROBADO UNITARIAMENTE.

## Especificaciones de la API (Para el Agente Frontend)
El Endpoint de *Reclamación de Tarea (Claim)* ya se encuentra disponible para su integración desde la SPA (Vue 3).

* **URL del Endpoint:** `POST /api/v1/tasks/{taskId}/claim`
* **Tipo:** Autenticado. (El Backend toma el autor del `SecurityContextHolder` o asume el default de mock en entornos sin OIDC, Ej. "maria.lopez").
* **Body Request:** Ninguno (`{}`).
* **Respuestas de Éxito:**
  * `200 OK`: La tarea fue asignada correctamente al usuario emisor de la petición en Camunda.
* **Respuestas de Error (Estándar Problem Details RFC 7807):**
  * `404 Not Found`: La tarea no existe o el taskId fue truncado en UI.
  * `409 Conflict`: "La tarea ya fue asignada a otro usuario." Sucedió una condición de carrera, refresquen el Workdesk.

## Instrucciones para Vue 3:
1. El botón de *Reclamar/Assign to Me* debe invocar Axios apuntando a `/api/v1/tasks/{taskId}/claim`.
2. Ante un HTTP 409, no se debe hacer crash, la UI debe mostrar una pequeña tostada emergente ("Tarea ya asignada a otra persona") y eliminar la `Card` del tablero global asíncronamente.
3. Ante un HTTP 200, la UI debe actualizar el `assignee` interno de esa tarjeta y moverla a la vista "Mis Tareas Pendientes".
