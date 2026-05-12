# 🧠→🎨 Handoff: Arquitecto Líder → Frontend
# T-19: Completar separación por Tabs usando componentes nativos de Vue (CA-22 US-002)

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** 🎨 FRONTEND
**Fecha:** 2026-05-12T22:00:00-05:00
**Sprint:** 7 — Iteración 7.1
**Prioridad:** 🔴 Alta
**Dependencia:** Ninguna

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
cat .cursorrules

# 2. Skill principal del agente receptor
cat .agents/skills/frontend_vue_composition_api/SKILL.md

# 3. Skills transversales aplicables
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. Handoff actual (este archivo)
cat .agentic-sync/T-19_Frontend_Tabs_Handoff.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar
> la anotación @Traceability o comentario `// @Traceability: US-002, CA-22`.
> Esto es INNEGOCIABLE.

## 🔬 Diagnóstico del Arquitecto

El componente actual `Workdesk.vue` expone una botonera inline para cambiar de "Mis Tareas" a "Pool Disponible", pero carece por completo de la implementación técnica requerida por CA-22 (US-002).

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Falta de Contadores | `Workdesk.vue:62-73` | Las tabs no muestran los contadores dinámicos `[N]` y `[M]` de la bandeja personal vs la cola del equipo. |
| Botonera Condicional Inexistente | `Workdesk.vue:384-389` | Se renderiza un único botón "Atender" sin importar la tab. CA-22 exige: Tab 1: [Abrir], [Liberar]. Tab 2: [Explorar], [Reclamar]. |

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Agregar Contadores Computados en Workdesk Store

**Archivo:** `frontend/src/stores/useWorkdeskStore.ts`

Necesitas crear propiedades computadas en el store para contar cuántas tareas hay asignadas al usuario activo (`PERSONAL`) y cuántas están en el pool sin asignar (`POOL`). 
Asegúrate de inyectar la trazabilidad:

```typescript
// @Traceability: US-002, CA-22 (Contadores N y M en store)
getters: {
  // Asumiendo que usas Pinia con setup o getters de options api. Si es setup, define computed vars.
  personalTaskCount: (state) => state.items.filter((t: any) => t.assignee).length,
  poolTaskCount: (state) => state.items.filter((t: any) => !t.assignee).length,
}
// O en su defecto, basarse en la paginación global si las tareas no están todas en memoria. Si las trae paginadas por vista, expone los totales de cada vista.
```
*(Nota para Frontend: Ajusta el getter dependiendo de la arquitectura de paginación del store)*

### Paso 2: Refactorizar Filtro de Tabs en `Workdesk.vue`

**Archivo:** `frontend/src/views/Workdesk.vue`

Añade los contadores visuales `[N]` y `[M]` a los botones del tab.

```html
<!-- @Traceability: US-002, CA-22 (Separación Visual con Contadores) -->
<div class="inline-flex rounded-lg border border-gray-200/80 bg-white/50 backdrop-blur-sm p-0.5 shadow-sm" data-testid="filter-pool-tabs">
  <button
    :class="['px-3 py-1.5 text-xs font-semibold rounded-md transition-all duration-200 flex items-center gap-1.5', store.activeView === 'PERSONAL' ? 'bg-indigo-600 text-white shadow-sm' : 'text-gray-500 hover:text-gray-700 hover:bg-gray-50']"
    @click="store.setActiveView('PERSONAL')"
    data-testid="tab-personal"
  >
    👤 Mi Bandeja <span class="bg-black/20 px-1.5 py-0.5 rounded text-[10px]">{{ store.personalTaskCount || 0 }}</span>
  </button>
  <button
    :class="['px-3 py-1.5 text-xs font-semibold rounded-md transition-all duration-200 flex items-center gap-1.5', store.activeView === 'POOL' ? 'bg-teal-600 text-white shadow-sm' : 'text-gray-500 hover:text-gray-700 hover:bg-gray-50']"
    @click="store.setActiveView('POOL')"
    data-testid="tab-pool"
  >
    👥 Cola del Equipo <span class="bg-black/20 px-1.5 py-0.5 rounded text-[10px]">{{ store.poolTaskCount || 0 }}</span>
  </button>
</div>
```

### Paso 3: Condicionar Botonera de Acción de Fila según Tab

**Archivo:** `frontend/src/views/Workdesk.vue`

Modifica la columna "Acciones" para respetar las reglas de negocio de CA-22.

```html
<!-- @Traceability: US-002, CA-22 (Botonera Condicional según Tab Activa) -->
<td class="px-4 py-3 text-center" @click.stop>
  <!-- TAB: MI BANDEJA -->
  <div v-if="store.activeView === 'PERSONAL'" class="flex items-center justify-center gap-2">
    <button @click="openTaskDetails(task)" class="px-2 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded shadow-sm transition text-[10px] uppercase flex items-center gap-1" data-testid="btn-open-task">
      <span class="material-symbols-outlined text-[14px]">open_in_new</span> Abrir
    </button>
    <button @click="onReleaseTask(task)" class="px-2 py-1.5 bg-gray-500 hover:bg-gray-600 text-white font-bold rounded shadow-sm transition text-[10px] uppercase flex items-center gap-1" data-testid="btn-release-task">
      <span class="material-symbols-outlined text-[14px]">undo</span> Liberar
    </button>
  </div>
  
  <!-- TAB: COLA DEL EQUIPO -->
  <div v-if="store.activeView === 'POOL'" class="flex items-center justify-center gap-2">
    <button @click="openTaskDetails(task)" class="px-2 py-1.5 bg-white border border-gray-300 text-gray-700 hover:bg-gray-50 font-bold rounded shadow-sm transition text-[10px] uppercase flex items-center gap-1" data-testid="btn-explore-task">
      <span class="material-symbols-outlined text-[14px]">visibility</span> Explorar
    </button>
    <button @click="onClaimTask(task)" :disabled="isClaiming === (task.unifiedId || task.originalTaskId)" class="px-2 py-1.5 bg-teal-600 hover:bg-teal-700 text-white font-bold rounded shadow-sm transition text-[10px] uppercase flex items-center gap-1" :data-testid="'claim-button-' + (task.unifiedId || task.originalTaskId)">
      <span v-if="isClaiming === (task.unifiedId || task.originalTaskId)" class="material-symbols-outlined text-[14px] animate-spin">refresh</span>
      <span v-else class="material-symbols-outlined text-[14px]">pan_tool</span>
      Reclamar
    </button>
  </div>
</td>
```
*(Asegúrate de agregar la función `onReleaseTask` en el script setup que invoque la acción respectiva en el store `store.releaseTask(id)` o un placeholder log console si el endpoint aún no existe)*

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Renderizado condicional de botones | `grep -C 3 "v-if=\"store.activeView === 'PERSONAL'\"" frontend/src/views/Workdesk.vue` retorna resultados. |
| 2 | Contadores integrados | Inspect visual en los botones Tab muestra `{{ store.personalTaskCount }}` y `{{ store.poolTaskCount }}`. |
| 3 | Trazabilidad inyectada | `grep "@Traceability: US-002, CA-22" frontend/src/views/Workdesk.vue` devuelve resultados. |
| 4 | Build exitoso | `cd frontend && npm run build` finaliza sin errores de lint/ts. |

## 🚦 SECUENCIA DE EJECUCIÓN

1. Modificar `useWorkdeskStore.ts` con los contadores.
2. Modificar `Workdesk.vue` actualizando los tabs y la columna de acciones.
3. Compilar el frontend para validar Typescript: `npm run build` dentro de `frontend/`
4. Commit: `git add . && git commit -m "feat(workdesk): CA-22 separación visual de tabs y botonera condicional" && git push`

## 📋 Instrucciones para Copiar y Pegar

```text
Asume el rol de 🎨 FRONTEND.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat .agentic-sync/T-19_Frontend_Tabs_Handoff.md

TU MISIÓN:

1. Implementar la separación visual de Tareas Personales vs Pool (CA-22, US-002).
2. Agregar propiedades computadas en el store de Workdesk (`personalTaskCount`, `poolTaskCount`).
3. Modificar `Workdesk.vue` para mostrar el contador [N] y [M] en las pestañas.
4. Renderizar condicionalmente los botones (Abrir/Liberar vs Explorar/Reclamar) según la tab activa.
5. Build/Compile: `cd frontend && npm run build`
6. Commit: `git add . && git commit -m "feat(workdesk): CA-22 UI tabs isolation" && git push`

REGLAS INQUEBRANTABLES:
- Inyectar // @Traceability: US-002, CA-22 en toda línea lógica nueva.
- Asegurarse de que los estilos Tailwind sigan la paleta de colores del proyecto (Indigo para personal, Teal para Pool).
- El botón de Liberar (Release) debe invocar una función o al menos un alert placeholder protegido por @Traceability si falta endpoint.
```
