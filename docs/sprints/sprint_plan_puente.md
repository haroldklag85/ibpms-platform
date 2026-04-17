# Sprint Puente — Cierre de Brechas Core (Workdesk & Reclamo)

> **Sprint:** Puente Opcional (Fase 0)
> **Duración Máxima:** 2 - 3 días laborales
> **Prerequisito:** Code Freeze Activo / UAT Strategy (Alt B)
> **Objetivo Único:** Construir los bloques de código demostrablemente inexistentes (WebSocket, SLA y Task Claiming) para permitir que la bandeja de entrada (Workdesk) y sus subsecuentes pruebas unitarias y E2E puedan existir lógicamente. Todos los agentes ejecutores actúan en modo Bugfix.

---

## Modelo de Roles Activos

| Rol | Actor | Scope en Sprint Puente |
|-----|-------|--------------------|
| **Jefe de Equipo** | Humano (Harolt) | Autoriza pase a Sprint 1 tras validar que el código existe. |
| **Agente PO** | Agente IA | Ninguno. (Code Freeze Activo). |
| **Arquitecto Líder SW** | Agente IA Lead | Emite Handoffs, audita código estricto, no programa. |
| **Agentes Ejecutores** | Agentes IA (Back/Front)| Programan *exclusivamente* los CAs faltantes detallados. |

---

## Scope Estricto de Ejecución (Solo US-001 y US-002)

### 1. US-001: Tiempo Real y SLA (Deuda Técnica)
*Base funcional requerida para la sincronización colaborativa.*

- **Backend:** 
  - Exponer Broker WebSocket STOMP (`/topic/workdesk/{tenantId}`).
  - Disparadores pasivos de eventos `TASK_CLAIMED` y `TASK_COMPLETED`.
- **Frontend:**
  - Composable `useWorkdeskRealtime` (SockJS + stompjs) implementando "*Ghost Deletion*" sobre el Piniastore autogestionado.
  - Motor de Ticking SLA (`useSlaEngine`): Semáforo vivo degradado con `requestAnimationFrame`.

### 2. US-002: Task Claiming (Desarrollo Cero)
*Sin esta historia, el usuario no puede seleccionar tareas para llenar sus formularios CQRS.*

- **Backend:**
  - Endpoints Atómicos `POST /api/v1/tasks/{taskId}/claim` y `POST /unclaim`.
  - Bloqueo por cache distribuido Redis (SETNX) para evitar colisiones entre operadores.
- **Frontend:**
  - Botón de CTA (Llamado a la Acción) en la grilla del Workdesk: "Atender".
  - Desconexión Optimista de la grilla y navegación programática a pantalla Form.

---

## Criterios de Aceptación (Gate para Salir del Sprint Puente)

1. El Endpoint POST `/claim` existe, responde `200 OK` aislando concurrencia y empuja un evento STOMP.
2. El cliente Vue procesa el evento STOMP y oculta la tarea reclamada (o la empuja a la vista de "Mis tareas" según corresponda la delegación).
3. El frontend grafica el tiempo restante basado en la metadada del servidor sin recargar todo el data grid.

**Nula Prueba E2E:** En este sprint puente no tocaremos Playwright. Únicamente garantizamos que las piezas compilables existen.
