# Handoff Frontend — Cierre de Brecha de Integración (Tasks & BulkAssign)

---

## 1. Metadatos y SSOT

| Campo | Valor |
|-------|-------|
| **Iteración** | Deuda Técnica de Integración (Iteración 5 / sprint-6) |
| **Rama Git** | `sprint-6` |
| **Deuda a Cerrar** | Corrección de rutas Spanglish (`/tareas` -> `/tasks`) y adición de `bulkAssign` |
| **Flujo de Trabajo** | Frontend → QA |

---

## 2. Contexto del Incidente

Durante la auditoría de Arquitectura, se identificó un **Integration Gap** que rompe el enrutamiento E2E del sistema de tareas:
1. `TaskService.ts` está intentando hacer peticiones a `/tareas/...` (ej. `/tareas/candidatas`, `/tareas/{taskId}/claim`). El backend expone estos contratos bajo la ruta `/tasks/...`.
2. La funcionalidad de asignación masiva (`bulkAssign`) requerida para cumplir con CA-5 y CA-14, expuesta por el backend en `/agile/projects/{projectId}/tasks/bulk-assign`, no existe en el frontend.

---

## 3. Instrucciones de Implementación

### Tarea 1: Corregir Enrutamiento "Spanglish" en `TaskService.ts`
En el archivo `frontend/src/services/TaskService.ts`:
- Reemplaza **TODAS** las ocurrencias del path `/tareas` por `/tasks`.
- Específicamente, verifica:
  - `getMyTasks`: de `/tareas` a `/tasks`
  - `getCandidateTasks`: de `/tareas/candidatas` a `/tasks/candidatas` (O verifica si backend espera solo `/tasks` con un param. Si no hay endpoint `candidatas` en Backend, mapearlo a `/tasks` enviando los params correctos. Asumiremos `/tasks/candidatas` temporalmente o la misma ruta con query param `assigned=false`). Nota: Según el estándar, deberías cambiar la ruta a `/tasks`.
  - `claimTask`: de `/tareas/${taskId}/claim` a `/tasks/${taskId}/claim`
  - `unclaimTask`: de `/tareas/${taskId}/unclaim` a `/tasks/${taskId}/unclaim`
  - `reassignTask`: de `/tareas/${taskId}/reassign` a `/tasks/${taskId}/reassign`

### Tarea 2: Implementar el Método `bulkAssign`
En `frontend/src/services/TaskService.ts`, agrega el siguiente método para consumir el controlador `AgileTaskController`:

```typescript
    /**
     * CA-5 + CA-14: Asignación masiva interactiva
     */
    static async bulkAssign(projectId: string, taskIds: string[], userId: string): Promise<void> {
        try {
            await apiClient.post(`/agile/projects/${projectId}/tasks/bulk-assign`, {
                taskIds,
                userId
            });
        } catch (error) {
            console.error('Error en asignación masiva de tareas:', error);
            throw error;
        }
    }
```

---

## 4. Criterios de Aceptación y Veredicto
- [ ] No debe existir el string `'/tareas'` en `TaskService.ts`.
- [ ] El método `bulkAssign` debe existir y apuntar a la ruta `/agile/projects/...`.
- [ ] La compilación local del frontend (`npm run build` o `vue-tsc --noEmit`) debe ser exitosa.

> ⚠️ **REGLAS DE EJECUCIÓN:** 
> - Modifica `TaskService.ts` aplicando las correcciones indicadas.
> - Ejecuta chequeos de TS y lint.
> - Notifica finalización para que QA audite.
