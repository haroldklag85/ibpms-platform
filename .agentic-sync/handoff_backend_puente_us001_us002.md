# 🤝 Handoff de Arquitectura: Sprint Puente Backend (Fase 0)

> **Destinatario:** Agente de Backend / Java Spring Boot
> **Alcance:** Historias US-001 (WebSockets) y US-002 (Task Claiming)
> **Directiva:** Code Freeze Activo. Resolver estas deudas técnicas estrictamente con TDD en mente.

---

## Bloque 1: US-001 STOMP WebSockets (Tiempo Real)

**Requisito de Negocio:** Cuando un operador reclama una tarea, todos los demás operadores del mismo grupo/tenant deben ver cómo la tarea desaparece de sus pantallas instantáneamente sin recargar la página (*Ghost Deletion*).

**Arquitectura a implementar:**
1. Crear clase `WebsocketConfig` con anotación `@EnableWebSocketMessageBroker`.
2. Registrar el endpoint `stomp-endpoint` (ej. `/api/v1/ws`) permitiendo CORS nativo o vía config de Spring.
3. Habilitar un SimpleBroker en la ruta prefijo `/topic`.
4. Crear un servicio `WorkdeskNotificationService` con `SimpMessagingTemplate` que provea un método `notifyTaskClaimed(String tenantId, String taskId, String claimedBy)`. Este método emitirá el mensaje STOMP al tópico `/topic/workdesk/{tenantId}`.

> [!WARNING]
> Dado el enfoque de Segregación de Responsabilidad, la seguridad (JWT) del WebSocket debe configurarse permitiendo el protocolo handshake, o ignorando la validación estricta solo para la suscripción temporal mientras se asienta el pipeline E2E.

---

## Bloque 2: US-002 Task Claiming (Reclamo de Tarea)

**Requisito de Negocio:** Prevenir condiciones de carrera (Race Conditions) si el Operador A y el Operador B hacen clic el mismo milisegundo en el botón "Atender" de la misma tarea.

**Arquitectura a implementar:**
1. Crear el DTO y el controlador `TaskClaimController` mapeado bajo `/api/v1/tasks`.
2. **Endpoint POST `/claim`:** 
   - Debe recibir la solicitud y antes de ir a BD, intentar adquirir un candado unívoco (Distributed Lock) usando **Redis** (Plantilla de Spring Boot Data Redis - `SETNX`).
   - Si Redis rechaza el Lock (Falso), responder inmediatamente `HTTP 409 Conflict`.
   - Si Redis acepta el Lock, actualizar en BDD `assigneeId` al usuario actual (JWT Security context mockeado o proveniente de OIDC).
   - Inyectar el `WorkdeskNotificationService` y disparar la notificación por WebSocket.
3. **Endpoint POST `/unclaim`:**
   - Liberar la tarea poniéndola en null y emitiendo un re-ingreso al broker.

## Firmas de Recepción
- [ ] Leído y analizado.
- [ ] Endpoints desplegados e inyectados.
- [ ] Commit y Push a reporsitorio de rama local.
