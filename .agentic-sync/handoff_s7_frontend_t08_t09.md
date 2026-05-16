# 🧠→🎨 Handoff: Arquitecto Líder → Frontend
# T-08 y T-09: Remediación de Deuda Funcional (ADR-006 y ADR-010)

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** 🎨 FRONTEND
**Fecha:** 2026-05-12T11:34:28-05:00
**Sprint:** 7 — Iteración 7.1
**Prioridad:** 🔴 Alta
**Dependencia:** Backend KanbanController (Ya existe y está operativo)

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (Leyes Globales)
cat .cursorrules

# 2. Skill principal del agente receptor
cat .agents/skills/frontend_build_audit/SKILL.md

# 3. Skills transversales aplicables
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. ADRs relevantes
cat docs/architecture/adr_006_vue3_lowcode_engine.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar la anotación @Traceability o comentario `// @Traceability: US-XXX, CA-XX`. Esto es INNEGOCIABLE.

---

## 🔬 Diagnóstico del Arquitecto

El Workdesk y el Kanban Board presentan severas deudas funcionales que comprometen la escalabilidad (DOM-Thrashing) y las pruebas integrales (Mock-Lock).

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| DOM-Thrashing / Fuga de memoria | `src/views/Workdesk.vue` (Líneas 835, 912) | Se detecta el uso (o intención) de `setInterval` locales por tarjeta en lugar de delegar en el `timeStore.ts` (Violación ADR-006 / US-001 CA-11). |
| Mock Lock | `src/stores/kanbanStore.ts` | Uso de arrays estáticos (Mocks) en el estado. No consume `KanbanController.java` (Violación ADR-010 Zero-Mock / US-008). |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Migrar SLA a Global Heartbeat

**Archivo:** `frontend/src/stores/timeStore.ts`

Construir/Asegurar que el store central provea el Heartbeat usando `requestAnimationFrame` y NUNCA `setInterval`.

```typescript
import { defineStore } from 'pinia';
// @Traceability: US-001, CA-11

export const useTimeStore = defineStore('timeStore', {
  state: () => ({
    currentTick: Date.now(),
  }),
  actions: {
    startEngine() {
      const loop = () => {
        this.currentTick = Date.now();
        requestAnimationFrame(loop);
      };
      requestAnimationFrame(loop);
    }
  }
});
```

### Paso 2: Limpiar KanbanStore e Integrar API REST

**Archivo:** `frontend/src/stores/kanbanStore.ts`

Eliminar arrays "quemados" y consumir la API REST real.

```typescript
import { defineStore } from 'pinia';
import apiClient from '@/services/apiClient';
// @Traceability: US-008, CA-12

export const useKanbanStore = defineStore('kanban', {
  state: () => ({
    tasks: [],
  }),
  actions: {
    async fetchTasks(boardId: string) {
      const response = await apiClient.get(`/api/v1/kanban/boards/${boardId}/tasks`);
      this.tasks = response.data;
    },
    async moveTask(taskId: string, newStatus: string, reason?: string) {
      const payload: any = { newStatus };
      if (newStatus === 'BLOCKED') payload.reason = reason;
      await apiClient.patch(`/api/v1/kanban/tasks/${taskId}/move`, payload);
      // Recargar tareas para reflejar la base de datos
    }
  }
});
```

---

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Cero `setInterval` crudos en Workdesk | `grep "setInterval" frontend/src/views/Workdesk.vue` → 0 resultados |
| 2 | Consumo Real de Kanban | `grep "api/v1/kanban" frontend/src/stores/kanbanStore.ts` → Retorna coincidencias |
| 3 | Trazabilidad Inyectada | `grep "@Traceability: US-008" frontend/src/stores/kanbanStore.ts` → Retorna coincidencias |
| 4 | Build exitoso | `npm run build` dentro de `frontend` termina con PASS |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Modificar `frontend/src/stores/timeStore.ts` y limpiar `Workdesk.vue`.
2. Modificar `frontend/src/stores/kanbanStore.ts`.
3. Validar build: `cd frontend && npm run build`
4. Commit: `git add . && git commit -m "refactor(frontend): migrar SLA a requestAnimationFrame y purgar mocks Kanban (T-08, T-09)" && git push`

---

## 📋 Instrucciones para Copiar y Pegar

```text
Asume el rol de 🎨 FRONTEND.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat .agents/skills/frontend_build_audit/SKILL.md
3. cat .agents/skills/clean_code_standards/SKILL.md
4. cat .agents/skills/zero_mock_enforcement/SKILL.md
5. cat docs/architecture/adr_006_vue3_lowcode_engine.md
6. cat .agentic-sync/handoffs/handoff_s7_frontend_t08_t09.md

TU MISIÓN:

1. Ejecutar el Paso 1 de las Instrucciones Quirúrgicas del handoff (Implementar requestAnimationFrame en timeStore).
2. Ejecutar el Paso 2 de las Instrucciones Quirúrgicas del handoff (Integrar kanbanStore.ts con Axios /api/v1/kanban).
3. Build/Compile: `cd frontend && npm run build`
4. Commit: `git add . && git commit -m "refactor(frontend): migrar SLA a requestAnimationFrame y purgar mocks Kanban (T-08, T-09)" && git push`

REGLAS INQUEBRANTABLES:
- DEBES incluir "// @Traceability: US-001, CA-11" o "// @Traceability: US-008, CA-12" en todo archivo modificado (LEY GLOBAL 3).
- PROHIBIDO usar `setInterval` dentro de Workdesk.vue o componentes hijos.
- PROHIBIDO crear flujos felices mockeados en `kanbanStore.ts`. DEBES apuntar al endpoint real (ADR-010).
```
