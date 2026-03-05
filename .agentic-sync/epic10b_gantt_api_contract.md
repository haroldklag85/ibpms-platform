# 📑 API Contract: Pantalla 10.B (Ejecución y Gantt)

## Backend Handoff Status
- **Component**: `ibpms-core` (Project Domain)
- **Status**: ✅ Repositorios, Endpoints SSE, y Transaccionalidad embebida desarrollados.
- **Ready for Frontend**: YES.

---

## 🚀 Endpoints Disponibles

**Base Path:** `/api/v1/execution/projects`

### 1. `GET /{id}/gantt-tree`
Retorna el árbol de ejecución de tareas (instancias) vinculadas a un proyecto.
**Response (200 OK):**
```json
[
  {
    "id": "uuid-task-execution",
    "projectId": "uuid-project-X",
    "wbsTaskTemplateId": "uuid-template-T1",
    "status": "PENDING",
    "assigneeUserId": null,
    "actualBudget": null,
    "startDatePlan": "2026-04-01T08:00:00",
    "endDatePlan": "2026-04-05T18:00:00"
  }
]
```

### 2. `PUT /tasks/{taskId}/assign`
Asignación de responsables y costos reales para una tarea particular desde el *ResourcePanel*.
**Payload:**
```json
{
  "assigneeUserId": "user-uuid",
  "actualBudget": 5000.00
}
```
**Response:** `204 No Content`

### 3. `POST /{id}/baseline`
**(AC-2 Big Bang Transaccional):** Congela el proyecto de teórico a ejecutivo y dispara la primera tarea local en Camunda (`RuntimeService.startProcessInstanceByKey()`).
**Response (200 OK - TEXT):** Retorna el UUID de la nueva línea base. Ejem: `"uuid-baseline-xyz-123"`

### 4. `GET /{id}/stream` (SSE Server-Sent Events)
**(AC-3 Suscripción Reactiva):** Endpoint `text/event-stream` unidireccional para el Gantt.
El Motor de Gantt Frontend debe conectarse via `EventSource('/api/v1/execution/projects/{id}/stream')`.

**Ejemplo de Payload SSE Emitido Event "gantt-task-update":**
```json
{
  "taskId": "uuid-task-execution",
  "status": "DONE"
}
```

---

## 🔒 Reglas de UX & Rendimiento (Handoff a Frontend)
1. El Gantt UI implementado en P10.B DEBE ser una librería OS libre de licencias de costo por usuario (ej. `frappe-gantt` o equivalente MIT).
2. Es prohibido abrir un WebSocket bidireccional: la librería OS debe empalmar con la interfaz `EventSource` pasiva conectada a `/stream`.
3. Hasta que no se dispare y retorne de forma exitosa el POST a `/baseline`, todos los nodos en el Gantt permanecen visualmente en estado Planificado.
