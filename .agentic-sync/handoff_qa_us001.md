---
author: Lead Architect
target: QA Developer Agent (Testing)
epic: US-001 Hybrid Workdesk
status: OPEN
---

# Hand-off Técnico: Quality Assurance (US-001 Remediation)

Con los inminentes desarrollos que aplicarán los agentes de Backend y Frontend para abordar los Gaps de CA-2 al CA-8, requieres proveer una infraestructura de automatización de QA (Vitest + Playwright) y unit testing (JUnit + Mockito) que pruebe las nuevas interacciones sin regresiones sobre CA-1 (Paginación).

### Specifications & Assertions (A Desarrollar)

#### 1. Backend: Mocking del Webhook & Websockets
- **Acción:** Instaurar suites en `ibpms-core` bajo `/src/test/java`
- **Assertion:** 
  - Validar que al completar una tarea desde un mock Camunda REST API (HTTP 200), el `KanbanTaskSyncListener` arroja un evento de `/topic/workdesk.updates` con Payload `ACTION: REMOVE`.
  - Asegurar que el CQRS `/workdesk/global-inbox?search=X&delegatedToId=Y` retorna la página serializada con base en un escenario pre-inyectado en la H2 DB Memory local.

#### 2. Frontend: Pruebas de DOM (Data Grid)
- **Acción:** Implementar pruebas de componente en `ibpms-platform/frontend/src/views/__tests__/Workdesk.spec.ts`.
- **Assertion:**
  - Montar el Componente con `@vue/test-utils` e inyectar un Pinia Mock Store de 10 tareas.
  - Testear la existencia del `<input type="search">` y certificar que altera la llamada del store tras 500ms al tipear.
  - Asegurarse de que exista la renderización `<table>` e `<th>` en vez de los div-cards pasados.

#### 3. Frontend: Pruebas Ticking SLA (Timers)
- **Assertion:**
  - Avance de Timer nativo mockeado (Ej: `vi.advanceTimersByTime(60000)`).
  - Validar que el `<span class="bg-green-100">` mute re-encaminando a `bg-yellow-100` reactivamente pasados X milisegundos in-memory.

*Inicia tus operaciones SOLO cuando los Agentes Backend y Frontend hayan commiteado sus PRs o cerrado sus correspondientes Hand-offs.*
