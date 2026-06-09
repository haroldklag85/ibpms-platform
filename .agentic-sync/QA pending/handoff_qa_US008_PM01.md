# Handoff Técnico para Agente QA / DevOps
## Sprint: 8 (PM-01) | Slot: 4 | US-008: Kanban Real

### 1. Contexto de la Tarea
El equipo de Backend y Frontend ha completado la integración real ("Zero-Mock") de la funcionalidad Kanban para IBPMS. Se ha eliminado por completo la dependencia a Mocks y a entidades paralelas de Kanban, centralizando la verdad en `WorkdeskProjectionEntity`.

### 2. Qué cambió
- **Backend:** 
  - Eliminado `KanbanTaskEntity`.
  - Endpoint PATCH y GET configurados para interactuar directo con SSOT.
  - Implementación de Rollback y concurrencia ante Conflict 409.
  - WebSocket `/topic/workdesk/kanban` activado.
- **Frontend:**
  - Optimistic UI implementado.
  - Rollback en errores 409, 403, 400.
  - Test en Vitest aprobados. 
  - Eliminación de arrays hardcodeados.

### 3. Tareas a Ejecutar por el Agente QA
Con base en la Política `QA_E2E_VALIDATION_AUDIT`:
1. Validar las transiciones de Kanban con pruebas Playwright E2E.
2. Certificar funcionamiento del Web Socket.
3. Asegurar que las regresiones sobre US-030 (Hub Ágil) no existan.
4. Proveer evidencia audiovisual (Trace, Screenshots) en la carpeta de reportes.
5. Si detecta bugs, rechazar el handoff y retornar al Frontend o Backend según corresponda.

### 4. Entorno
- Rama a certificar: `sprint-8/pm-01/us-008-kanban-real`
- URL de test: `/kanban` y `/workdesk`

---
> **Nota del Arquitecto:** "Queda estrictamente prohibido simular peticiones con interceptores. Playwright debe correr contra el backend dockerizado o local."
