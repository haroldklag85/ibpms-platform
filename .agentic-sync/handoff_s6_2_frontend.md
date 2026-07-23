# 🎨 Handoff Frontend — Sprint 6 / Iteración 6.2

> **Iteración:** Sprint 6 — Iteración 6.2 (Kanban MVP Real + KanbanView Remediación)  
> **Rama de trabajo:** `sprint-6/uat-certification` (git pull origin sprint-6/uat-certification primero)  
> **US objetivo:** US-008 (Kanban)  
> **Flujo:** Backend (T-6: Kanban state machine) → **Frontend** → QA  
> **SSOT de referencia:** `docs/uat/casos_uso_uat_j04.md` (v2) → Fase 7: CU-J04-29 a 32  
> **Plan aprobado:** `docs/sprints/sprint_plan_s6.md` §Iteración 6.2  
> **Autor:** Arquitecto Líder SW  
> **Fecha:** 2026-04-19

---

## 1. Metadatos y SSOT

| Parámetro | Valor |
|-----------|-------|
| **Sprint** | 6 — Iteración 6.2 |
| **Rama Git** | `sprint-6/uat-certification` |
| **US** | US-008 (Kanban) |
| **Tarea** | T-7: KanbanView.vue → eliminar mocks, conectar API real, modal blocked, Done readonly |
| **Dependencia** | Esperar push de Backend T-6 (PATCH endpoint + Board endpoint) |
| **Prerequisito** | It. 6.1 SELLADA ✅ |

**Fuentes de verdad:**
- `docs/uat/casos_uso_uat_j04.md` (v2) → CU-J04-29 a 32 + CU-J04-NEG-06
- `.agentic-sync/coverage_matrix.md` §US-008

---

## 2. Archivo a Modificar

### `frontend/src/views/KanbanView.vue`

**Estado actual (auditado 2026-04-18):**
- `loadBoard()` usa `setTimeout` con datos mock hardcodeados (4 tareas estáticas)
- No hay llamada a API real
- No hay modal de bloqueo
- No hay validación de inmutabilidad DONE
- Estado: ~10% Scaffolding

---

## 3. Cambios Requeridos

### 3.1 Eliminar Mock Data

**ANTES (eliminar):**
```javascript
const loadBoard = () => {
  setTimeout(() => {
    columns.value = [
      { name: 'TODO', tasks: [...mockTasks] },
      // ... hardcoded
    ]
  }, 500)
}
```

**DESPUÉS (conectar API):**
```javascript
const loadBoard = async () => {
  try {
    const { data } = await apiClient.get('/api/v1/kanban/board')
    columns.value = data.columns
  } catch (error) {
    handleError(error) // US-000 degradación graceful
  }
}
```

### 3.2 Conectar `moveTask` a API Real

**ANTES:**
```javascript
const moveTask = (taskId, newState) => {
  // Solo mueve en memoria local
}
```

**DESPUÉS:**
```javascript
const moveTask = async (taskId, newState, blockReason = null) => {
  syncStatus.value = 'saving' // "Guardando..."
  try {
    await apiClient.patch(`/api/v1/kanban/${taskId}/state`, {
      newState,
      blockReason
    })
    syncStatus.value = 'ok' // "OK"
    await loadBoard() // Refresco
  } catch (error) {
    syncStatus.value = 'error'
    if (error.response?.status === 403) {
      toast.error('Esta tarea está completada y no puede modificarse')
    } else if (error.response?.status === 400) {
      toast.error('Transición de estado no válida')
    }
  }
}
```

### 3.3 Modal de Bloqueo (CA-1)

Cuando el usuario arrastra una tarea a la columna **BLOCKED**, se DEBE abrir un modal:

```vue
<template>
  <!-- Modal Bloqueo -->
  <div v-if="showBlockModal" class="modal-overlay">
    <div class="modal-card">
      <h3>⛔ Motivo de Bloqueo</h3>
      <p>Por favor, especifica el motivo de bloqueo:</p>
      <textarea
        v-model="blockReasonInput"
        placeholder="Describe el motivo del bloqueo..."
        rows="3"
        data-testid="block-reason-input"
      />
      <div class="modal-actions">
        <button @click="cancelBlock" data-testid="cancel-block">Cancelar</button>
        <button
          @click="confirmBlock"
          :disabled="!blockReasonInput.trim()"
          data-testid="confirm-block"
        >
          Bloquear
        </button>
      </div>
    </div>
  </div>
</template>
```

**Lógica del drag handler:**
```javascript
const onDrop = (taskId, targetColumn) => {
  if (targetColumn === 'BLOCKED') {
    pendingBlockTaskId.value = taskId
    showBlockModal.value = true
    return // No mover hasta confirmar
  }
  moveTask(taskId, targetColumn)
}

const confirmBlock = () => {
  moveTask(pendingBlockTaskId.value, 'BLOCKED', blockReasonInput.value)
  showBlockModal.value = false
  blockReasonInput.value = ''
}
```

### 3.4 Done Readonly (CA-2)

Las tarjetas en columna **DONE** NO deben ser arrastrables:

```vue
<div
  v-for="task in column.tasks"
  :key="task.id"
  :draggable="column.name !== 'DONE'"
  :class="{ 'done-readonly': column.name === 'DONE' }"
  :data-testid="`kanban-card-${task.id}`"
>
```

```css
.done-readonly {
  opacity: 0.7;
  cursor: default;
  pointer-events: none; /* Previene drag */
}
```

### 3.5 Indicador de Sincronización

```vue
<div class="sync-indicator" data-testid="kanban-sync-status">
  <span v-if="syncStatus === 'saving'">⏳ Guardando...</span>
  <span v-else-if="syncStatus === 'ok'">✅ OK</span>
  <span v-else-if="syncStatus === 'error'">❌ Error</span>
</div>
```

### 3.6 Data-testid Obligatorios (para Playwright)

| Selector | Elemento |
|----------|----------|
| `kanban-board` | Contenedor principal del board |
| `kanban-column-{name}` | Cada columna (TODO, IN_PROGRESS, BLOCKED, DONE) |
| `kanban-card-{taskId}` | Cada tarjeta |
| `kanban-sync-status` | Indicador de sincronización |
| `block-reason-input` | Textarea del modal de bloqueo |
| `confirm-block` | Botón confirmar bloqueo |
| `cancel-block` | Botón cancelar bloqueo |

---

## 4. Convención de Commit

```
feat(frontend): T-7 US-008 KanbanView real API + modal blocked + Done readonly
```

---

## 5. Exclusiones

- **NO implementar** WebSocket para sincronización Kanban en tiempo real — fuera del scope
- **NO implementar** `<UniversalSlaTimer>` — diferido a It. 6.5
- **NO modificar** `Login.vue` — ya remediado en It. 6.1
- **NO usar** `page.route()` mocks en ningún test
