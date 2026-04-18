# 🤝 Handoff de Arquitectura: Sprint Puente Frontend (Fase 0)

> **Destinatario:** Agente de Frontend / Vue 3 / Pinia
> **Alcance:** Historias US-001 (WebSockets/SLA) y US-002 (Task Claim UI)
> **Directiva:** Code Freeze Activo. Resolver deudas en la UX para alistar a Playwright.

---

## Bloque 1: US-001 Tiempo Real (Ghost Deletion)

**Requisito de Negocio:** La pantalla debe ser reactiva a los eventos del servidor para prevenir colisiones visuales entre agentes operativos.

**Arquitectura a implementar:**
1. Instalar o utilizar `@stomp/stompjs` y `sockjs-client` (O WebSocket HTML5 nativo).
2. Crear un composable reactivo Vue `useWorkdeskRealtime.ts` que se auto-suscriba al tópico `/topic/workdesk/{tenantId}` usando el token de sesión activo.
3. Proveer un callback de eventos que, al escuchar `TASK_CLAIMED`, invoque el store interno (`useWorkdeskStore.taskArray.filter...`) removiendo visualmente la caja o registro de la tarea de forma atómica.

---

## Bloque 2: US-001 Reloj de Ticking (SLA Engine)

**Arquitectura a implementar:**
1. Crear un store de Pinia separado `useSlaEngine.ts` que calcule temporalidades sin golpear a la CPU pesadamente.
2. Implementar un bucle matemático usando `requestAnimationFrame` en donde mida `SLA_Limite - Date.now()`.
3. Exponer métodos reactivos `isUrgent(taskId)`, `isWarning(taskId)`, que sean consumidos por el grid del Workdesk para cambiar colores a Rojo (SLA Vencido o < 25%) y Amarillo.

---

## Bloque 3: US-002 Task Claim UI (Atender Tarea)

**Arquitectura a implementar:**
1. Dibujar visiblemente en el Data Grid del Workdesk (o sus Details/Cards) el **CTA "Atender"**.
2. Al hacer clic, invocar el endpoint Axios `POST /api/v1/tasks/{taskId}/claim`.
3. Enmascarar con estado `Loading` u "Optimistic UI". Si la API responde OK, enrutar programáticamente (`vue-router`) a la vista de detalle del Formulario Genérico (FormGen). Si falla con 409 Conflict, lanzar un Toast de error indicando "Alguien ya ha tomado esta tarea".

## Firmas de Recepción
- [ ] Leído y analizado.
- [ ] Patrones de estado implementados en Pinia.
- [ ] Mockup visual listo para inspección E2E Playwright.
