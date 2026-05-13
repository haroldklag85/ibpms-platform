# 🧠→💻 Handoff: Arquitecto Líder → Agente Frontend
# T-19: Completar Separación por Tabs (Contadores y Botoneras CA-22)

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** 💻 FRONTEND - VUE3
**Fecha:** 2026-05-12T19:00:00-05:00
**Sprint:** 7 — Iteración 7.1
**Prioridad:** 🔴 Alta
**Dependencia:** Ninguna

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3, 4)
cat .cursorrules

# 2. Skill principal del agente receptor (Frontend Build & Vue 3)
cat .agents/skills/frontend_build_audit/SKILL.md

# 3. Skills transversales aplicables
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. ADRs relevantes (Dumb Components)
cat docs/architecture/adr_006_vue3_lowcode_engine.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar la anotación `@Traceability` o comentario `// @Traceability: US-002, CA-22`. Esto es INNEGOCIABLE.

---

## 🔬 Diagnóstico del Arquitecto

Tras la auditoría de la deuda técnica funcional de la US-002 (Reclamar Tarea) y US-001 (Obtener Tareas), se ha detectado que la T-19 (CA-22) no cumple con el contrato visual exigido:

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Contadores Fijos | `frontend/src/components/WorkdeskTabs.vue:8,15` | El componente inyecta texto fijo ("Mi Bandeja", "Tareas de Equipo") ignorando los getters `personalTaskCount` y `poolTaskCount` del store, violando CA-22. |
| Fuga de Contexto | `frontend/src/views/Workdesk.vue:363` | El botón "Reclamar" aparece en la vista "Mi Bandeja", y el botón "Liberar" aparece en la vista "Pool", violando la separación estricta de acciones del CA-22. |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Inyectar Contadores en las Pestañas (CA-22)

**Archivo:** `frontend/src/components/WorkdeskTabs.vue`

Actualiza el bloque `<template>` para consumir de forma reactiva los contadores numéricos directamente del `workdeskStore`.

```vue
<!-- // @Traceability: US-002, CA-22 -->
<template>
  <div class="flex border-b border-gray-200 mb-4 bg-white px-2">
    <button 
      class="py-3 px-6 transition-colors font-semibold text-sm outline-none focus:outline-none flex items-center gap-2"
      :class="workdeskStore.activeView === 'PERSONAL' ? 'border-b-[3px] border-blue-600 text-blue-600' : 'text-gray-500 hover:text-gray-800 hover:border-b-[3px] hover:border-gray-300 border-b-[3px] border-transparent'"
      @click="workdeskStore.setActiveView('PERSONAL')"
    >
      💼 Mi Bandeja 
      <span class="bg-blue-100 text-blue-800 text-xs font-medium px-2 py-0.5 rounded-full">{{ workdeskStore.personalTaskCount }}</span>
    </button>
    <button 
      class="py-3 px-6 transition-colors font-semibold text-sm outline-none focus:outline-none flex items-center gap-2"
      :class="workdeskStore.activeView === 'POOL' ? 'border-b-[3px] border-indigo-600 text-indigo-600' : 'text-gray-500 hover:text-gray-800 hover:border-b-[3px] hover:border-gray-300 border-b-[3px] border-transparent'"
      @click="workdeskStore.setActiveView('POOL')"
    >
      👥 Tareas de Equipo 
      <span class="bg-indigo-100 text-indigo-800 text-xs font-medium px-2 py-0.5 rounded-full">{{ workdeskStore.poolTaskCount }}</span>
    </button>
  </div>
</template>
```

### Paso 2: Condicionar Botones de Fila según Pestaña (CA-22)

**Archivo:** `frontend/src/views/Workdesk.vue`

Localiza los botones de acción (Reclamar, Liberar, Abrir) dentro de la iteración de tarjetas/filas. Modifica el condicional `v-if` para que dependa explícitamente de `store.activeView`.

```vue
<!-- Dentro del <template> de Workdesk.vue donde se iteran las tareas -->
<!-- // @Traceability: US-002, CA-22 -->

<!-- Botón Reclamar (Solo en POOL) -->
<button v-if="store.activeView === 'POOL' && !task.assignee" 
        @click.stop="store.claimTask(task.unifiedId)"
        class="text-indigo-600 hover:text-indigo-900 font-medium text-sm">
   Reclamar
</button>

<!-- Botón Abrir (Solo en Mi Bandeja) -->
<button v-if="store.activeView === 'PERSONAL' && task.assignee" 
        @click.stop="openTask(task)"
        class="text-blue-600 hover:text-blue-900 font-medium text-sm mr-3">
   Abrir
</button>

<!-- Botón Liberar (Solo en Mi Bandeja) -->
<button v-if="store.activeView === 'PERSONAL' && task.assignee" 
        @click.stop="store.unclaimTask(task.unifiedId)"
        class="text-red-500 hover:text-red-700 font-medium text-sm">
   Liberar
</button>
```
*(Ajusta las clases CSS exactas según la estructura actual de Workdesk.vue, respetando la directriz de condicional).*

---

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Pestañas con Contadores Visibles | Iniciar el app y verificar que `[N]` (Mi Bandeja) y `[M]` (Pool) se renderizan numéricamente en la UI. |
| 2 | Botonera Aislada | En `Workdesk.vue`, el botón "Reclamar" tiene `v-if="store.activeView === 'POOL'"`. Ejecutar `grep -n "store.activeView === 'POOL'" frontend/src/views/Workdesk.vue`. |
| 3 | Trazabilidad Inversa (Ley 3) | Inspeccionar `git diff` asegurando que el marcador `// @Traceability: US-002, CA-22` exista. |
| 4 | Build Exitoso | Ejecutar `npm run build` en el frontend, resultando en compilación exitosa (0 errores TS). |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Editar `frontend/src/components/WorkdeskTabs.vue`.
2. Editar `frontend/src/views/Workdesk.vue`.
3. Validar: `cd frontend && npm run build`
4. Commit: `git add . && git commit -m "feat(workdesk): T-19 inyectar contadores en WorkdeskTabs y aislar botoneras según CA-22" && git push origin HEAD`

---

## 📋 Instrucciones para Copiar y Pegar

```
Asume el rol de 💻 FRONTEND - VUE3.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat .agents/skills/frontend_build_audit/SKILL.md
3. cat docs/architecture/adr_006_vue3_lowcode_engine.md
4. cat .agentic-sync/T-19_Frontend_Workdesk_Tabs_Handoff.md

TU MISIÓN:

1. Modifica `frontend/src/components/WorkdeskTabs.vue` para inyectar `workdeskStore.personalTaskCount` y `workdeskStore.poolTaskCount` en la UI, tal como lo define el snippet de la Sección 4.
2. Modifica la grilla en `frontend/src/views/Workdesk.vue` para aislar los botones (Reclamar vs Liberar/Abrir) usando `v-if="store.activeView === 'X'"`.
3. Build/Compile: `cd frontend && npm run build`
4. Commit: `git add . && git commit -m "feat(workdesk): T-19 inyectar contadores CA-22" && git push origin HEAD`

REGLAS INQUEBRANTABLES:
- DEBES añadir `// @Traceability: US-002, CA-22` a cada componente modificado (LEY GLOBAL 3).
- PROHIBIDO usar `any` adicionales, usa las variables ya tipadas en el store de Pinia.
- El build final debe pasar sin errores de TypeScript.
```
