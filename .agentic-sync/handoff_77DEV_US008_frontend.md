# 🏗️ Handoff Arquitectónico Frontend — US-008: Kanban Zero-Mock (Híbrido)

## 1. Metadatos y SSOT
- **Sprint/Iteración**: Sprint PM-01, Slot 4
- **User Story**: US-008 (Vista Kanban)
- **Branch de trabajo**: `sprint-8/pm-01/us-008-kanban-real`
- **SSOT**: `docs/requirements/epics/epic_B_workdesk.md`
- **Flujo de Trabajo**: Backend -> Frontend -> QA

## 2. Alineación Arquitectónica y ADRs
- **ADR-010 (Zero-Mock)**: Prohibido usar datos hardcodeados o mockAdapters. La UI debe consumir las tareas reales del backend que combinan el estado del tablero con los atributos reales de Workdesk.
- **Escalabilidad y Columnas Dinámicas**: El frontend no debe asumir que solo existen 4 columnas (`TODO`, `IN_PROGRESS`, `BLOCKED`, `DONE`). Las columnas son devueltas por el backend, permitiendo a los usuarios corporativos definir flujos custom.
- **Optimistic UI y Rollback**: En caso de conflicto de red o que la tarea haya sido reclamada por otro usuario (`409 Conflict`), la UI debe realizar un rollback visual de la tarjeta a su columna original usando el store de Pinia.

## 3. Rutas Exactas y Contexto Preexistente
- `frontend/src/stores/kanbanStore.ts`: Actualmente tiene métodos hardcodeados para buscar `columns` y `tasks` por separado en endpoints mock.
- `frontend/src/views/kanban/KanbanView.vue`: Vista principal que delega la recarga y renderiza el tablero.
- `frontend/src/components/kanban/KanbanColumn.vue`: Renderiza cada columna.

## 4. Snippets Prescriptivos (El "Qué" y el "Cómo")

### A. Refactorización de `kanbanStore.ts`
El backend devolverá los datos combinados en `/api/v1/kanban/boards/${boardId}/columns`.
Modificar `fetchBoard` para que mapee el `originalTaskId` (que viene del backend) y los atributos reales:
```typescript
async fetchBoard(boardId: string) {
    this.boardId = boardId;
    this.loading = true;
    this.error = null;
    try {
        // Ahora el backend devuelve todo agrupado y mezclado con WorkdeskProjectionEntity
        const res = await apiClient.get(`/api/v1/kanban/boards/${boardId}/columns`);
        
        this.columns = res.data.columns.map((c: any) => ({
            id: c.id || c.name,
            name: c.name,
            title: c.name, // O mapear a un display name amigable
            items: c.tasks.map((t: any) => ({
                id: t.kanbanTaskId, 
                originalTaskId: t.originalTaskId, // El ID de la tarea real de Workdesk
                title: t.title,
                status: t.state,
                assignee: t.assignee,
                slaDueDate: t.slaExpirationDate,
                blockedReason: t.blockedReason
            }))
        }));
        
        this.initWebSocket();
    } catch (error: any) {
        this.error = "Error al conectar con el servidor.";
    } finally {
        this.loading = false;
    }
}
```

### B. Modificación de `moveTask` para Escalabilidad
Al mover una tarea, el backend puede crear dinámicamente el estado si es custom. La Optimistic UI debe tolerarlo y si falla, hacer rollback.
```typescript
async moveTask(kanbanTaskId: string, newStatus: string, blockedReason?: string) {
    // Lógica existente de Optimistic UI para mover la tarjeta en los arrays locales
    // ...
    const payload: any = { newStatus };
    if (blockedReason) payload.reason = blockedReason;
    
    // Injectar el assignee actual para la delegación de Workdesk si pasa a IN_PROGRESS
    const authStore = useAuthStore();
    payload.assignee = authStore.user?.username;

    try {
        await apiClient.patch(`/api/v1/kanban/tasks/${kanbanTaskId}/move`, payload);
    } catch(error: any) {
        console.warn("Fallo en Optimistic UI, revirtiendo estado...", error);
        // Rollback visual obligatorio
        // ...
        
        if (error.response?.status === 409) {
            this.error = "La tarjeta fue reclamada o modificada por otro usuario. Se sincronizará el tablero.";
        }
        throw error;
    }
}
```

### C. Integración con `TaskPreviewModal`
Asegurarse de que al hacer clic en una tarjeta en `KanbanColumn.vue`, el ID pasado al modal sea el `originalTaskId` (Workdesk), NO el `kanbanTaskId` interno.
```vue
<!-- KanbanView.vue o KanbanColumn.vue -->
<TaskPreviewModal 
    v-if="selectedTask" 
    :taskId="selectedTask.originalTaskId" 
    :readOnly="isReadonly"
    @close="selectedTask = null" 
/>
```

## 5. Matriz de QA y Testing Atómico
**Script sugerido**: `kanbanStore.spec.ts` (Vitest)
| Test Name | CA Evaluado | Aserción Esperada |
|-----------|-------------|-------------------|
| `moveTask_optimisticRollback_on_409` | CA-21 (Workdesk) | Si el PATCH devuelve 409, la tarea vuelve a la columna original localmente. |
| `fetchBoard_maps_originalTaskId` | ADR-010 | El modelo de la tienda mapea `originalTaskId` correctamente para usarlo en modales. |
| `dynamic_columns_rendering` | Escalabilidad | Si el payload devuelve 7 columnas arbitrarias, vue-draggable renderiza 7 columnas. |

## 6. Mensaje de Despacho (Comunicación al Agente Especialista)
> "Build obligatorio: Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`."
