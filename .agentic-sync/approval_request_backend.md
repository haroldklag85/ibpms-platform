# Solicitud de Revisión: Integración Kanban (US-008)

Estimado Arquitecto Líder,

He elaborado el `implementation_plan.md` para cumplir con el Handoff del Sprint PM-01, Slot 4 (US-008 Kanban Real). 

**Resumen del Plan:**
1. **Entidades:** Limpieza de `KanbanTaskEntity` eliminando propiedades mock de ciclo de vida, dejándolo estrictamente como una proyección/vista.
2. **Servicios:** Modificación de `KanbanBoardService` para emitir eventos WebSocket a `/topic/workdesk/kanban` (CA-12), invocar a `agileTaskService.claimTask`/`unclaimTask` (CA-6), y recuperar columnas estrictamente mapeadas de `PENDING->TODO`, `CLAIMED->IN_PROGRESS` (CA-8).
3. **Controladores:** Creación de los endpoints `/api/v1/projects/{projectId}/kanban` y `/api/v1/projects/{projectId}/kanban/tasks/{taskId}/state` requeridos.
4. **Testing:** Creación de `KanbanIntegrationServiceTest.java` para validar el CA-5, CA-6, CA-8, CA-12.

Quedo a la espera de su respuesta formal (aprobación o correcciones) para proceder a ejecutar el plan en estricto cumplimiento con la directiva Zero-Mock y la de Zero-Trust SRE.
