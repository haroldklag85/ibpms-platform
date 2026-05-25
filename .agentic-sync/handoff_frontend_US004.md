# 🧠→💻 Handoff: Arquitecto Líder → Frontend - VUE3
# US-004: Pantalla de Triaje Humano (Dumb Components)

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** 💻 FRONTEND - VUE3
**Fecha:** 2026-05-25T13:20:00-05:00
**Sprint:** 7 — Iteración 1
**Prioridad:** 🔴 Alta
**Dependencia:** Ninguna (Se puede mockear el Pinia Store mientras Backend avanza)

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
cat .cursorrules

# 2. Skill principal del agente receptor
cat .agents/skills/frontend_build_audit/SKILL.md

# 3. Skills transversales aplicables
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. ADRs relevantes
cat docs/architecture/ADR-006-Dumb-Components.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar
> la anotación @Traceability o comentario `// @Traceability: US-XXX, CA-XX`.
> Esto es INNEGOCIABLE.

## 🔬 Diagnóstico del Arquitecto

Actualmente no existe una interfaz visual para la gestión manual del Pre-Triaje Humano (CA-8, CA-9), bloqueando la capacidad de los administradores para procesar cargas útiles de webhook dudosas.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Ausencia UI | `frontend/src/views/intake/` | No existe la vista `IntakeTriageView.vue` para listar correos/webhooks retenidos. |
| Ausencia Store | `frontend/src/stores/` | No existe la lógica de gestión (Dumb Component ADR-006) en Pinia para esta bandeja. |

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Crear el Store de Pinia (Zero-Mock V2 / Dumb Component)

**Archivo:** `frontend/src/stores/useIntakeTriageStore.ts`

```typescript
import { defineStore } from 'pinia';
import { ref } from 'vue';
import apiClient from '@/services/apiClient';
// @Traceability: US-004, CA-8

export const useIntakeTriageStore = defineStore('intakeTriage', () => {
    const items = ref([]);
    const loading = ref(false);

    const fetchTriageItems = async () => {
        loading.value = true;
        try {
            const { data } = await apiClient.get('/intake/triage');
            items.value = data;
        } finally {
            loading.value = false;
        }
    };

    const processItem = async (id: string, action: 'APPROVE' | 'REJECT') => {
        await apiClient.post(`/intake/triage/${id}/process`, { action });
        await fetchTriageItems(); // Reload
    };

    return { items, loading, fetchTriageItems, processItem };
});
```

### Paso 2: Crear la Vista Dumb Component

**Archivo:** `frontend/src/views/intake/IntakeTriageView.vue`

El componente NO debe tener lógica de red, todo se delega al store.

```vue
<template>
  <div class="intake-triage-view">
    <!-- @Traceability: US-004, CA-9 -->
    <h2>Bandeja de Triaje Humano</h2>
    <div v-if="loading">Cargando...</div>
    <ul v-else>
      <li v-for="item in items" :key="item.id">
         {{ item.subject }} 
         <button @click="processItem(item.id, 'APPROVE')">Aprobar</button>
         <button @click="processItem(item.id, 'REJECT')">Rechazar</button>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { storeToRefs } from 'pinia';
import { useIntakeTriageStore } from '@/stores/useIntakeTriageStore';

const store = useIntakeTriageStore();
const { items, loading } = storeToRefs(store);
const { fetchTriageItems, processItem } = store;

onMounted(() => {
    fetchTriageItems();
});
</script>
```

### Paso 3: Registrar la ruta en Vue Router

**Archivo:** `frontend/src/router/index.ts`
Agrega la ruta `/intake/triage` asociada al componente `IntakeTriageView.vue`.

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Store creado y reactivo | `useIntakeTriageStore.ts` usa Pinia |
| 2 | Dumb Component implementado | `IntakeTriageView.vue` delega las peticiones al store |
| 3 | Trazabilidad inyectada | `grep -r "@Traceability.*US-004" frontend/src/views/intake` retorna resultados |
| 4 | Build exitoso | Comando `npm run build` o `npx vite build` compila sin errores TypeScript. |

## 🚦 SECUENCIA DE EJECUCIÓN

1. Crear `useIntakeTriageStore.ts`.
2. Crear `IntakeTriageView.vue`.
3. Actualizar el router.
4. Ejecutar Build: `npm run build`
5. Commit: `git add . && git commit -m "feat(ui): triage human view implementation for US-004" && git push`

## 📋 Instrucciones para Copiar y Pegar

```text
Asume el rol de 💻 FRONTEND - VUE3.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat .agents/skills/frontend_build_audit/SKILL.md
3. cat .agents/skills/clean_code_standards/SKILL.md
4. cat .agents/skills/zero_mock_enforcement/SKILL.md
5. cat docs/architecture/ADR-006-Dumb-Components.md
6. cat C:\Users\HaroltAndrésGómezAgu\.gemini\antigravity\brain\70f5a9fc-0715-4dbd-b999-23a6c6833584\artifacts\handoff_frontend_US004.md

TU MISIÓN:

1. Implementa el store Pinia `useIntakeTriageStore.ts` y la vista `IntakeTriageView.vue` según los snippets del handoff.
2. Registra la ruta en el router.
3. Build/Compile: npm run build
4. Commit: git add . && git commit -m "feat(ui): triage human view implementation for US-004" && git push

REGLAS INQUEBRANTABLES:
- DEBES inyectar @Traceability en la nueva vista y en el store.
- PROHIBIDO poner llamadas axios directas dentro del componente `.vue`. Todo a través del store (ADR-006).
- PROHIBIDO usar alert() o confirm(), si se necesita UI modal crear un nuevo componente.
```
