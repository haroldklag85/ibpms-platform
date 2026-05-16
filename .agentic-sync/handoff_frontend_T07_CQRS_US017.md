# 🧠→🎨 Handoff: Arquitecto Líder → Frontend - Vue
# T-07: UX de CQRS, Latencia y Monitoreo de Red (US-017)

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** 🎨 FRONTEND - VUE
**Fecha:** 2026-05-12T09:30:00-05:00
**Sprint:** 7 — Sprint 7.1
**Prioridad:** 🔴 Alta
**Dependencia:** Ninguna (Se puede mockear localmente con Axios Interceptors, pero preferir integración real).

---

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
cat docs/architecture/adr_014_observabilidad.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar la anotación `// @Traceability: US-XXX, CA-XX`. Esto es INNEGOCIABLE.

---

## 🔬 Diagnóstico del Arquitecto

La plataforma carece de feedback visual adecuado durante bloqueos o lentitud en envíos a Camunda, lo que puede causar doble clics y estados huérfanos.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Feedback Bloqueante Inexistente | `src/components/common/` | No hay indicador no intrusivo de "Guardando..." (CA-19). |
| Monitoreo de Red Ausente | `src/stores/` | No se gestiona globalmente si el usuario pierde conexión (CA-22, CA-25). |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: State Manager para Red y Latencia

**Archivo:** `ibpms-platform/frontend/src/stores/networkStore.ts`

Crea un Pinia Store centralizado.

```typescript
// @Traceability: US-017, CA-24
import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useNetworkStore = defineStore('network', () => {
  const isOffline = ref(!navigator.onLine);
  const isSaving = ref(false);
  
  window.addEventListener('offline', () => isOffline.value = true);
  window.addEventListener('online', () => {
     isOffline.value = false;
     // Disparar toast success por 3 segundos...
  });

  return { isOffline, isSaving };
});
```

### Paso 2: Toast de Conexión y Latencia

**Archivo:** `ibpms-platform/frontend/src/components/common/CQRSConnectionToast.vue`

Un componente totalmente desacoplado (Dumb Component).

```html
<!-- @Traceability: US-017, CA-19, CA-20, CA-22 -->
<template>
  <div v-if="networkStore.isOffline || showSaving" class="fixed bottom-4 left-4 z-50 p-3 bg-gray-800 text-white rounded shadow pointer-events-none transition-opacity">
    <span v-if="networkStore.isOffline">🔴 Trabajando sin conexión</span>
    <span v-else-if="showSaving">⏳ Guardando cambios...</span>
  </div>
</template>

<script setup lang="ts">
import { useNetworkStore } from '@/stores/networkStore';
import { ref, watch } from 'vue';

const networkStore = useNetworkStore();
const showSaving = ref(false);

// Lógica de Debounce: Mostrar showSaving solo si networkStore.isSaving lleva en true > 5 segundos.
</script>
```

### Paso 3: Incluir en el Layout Base

**Archivo:** `ibpms-platform/frontend/src/layouts/AppLayout.vue` (o similar).
Agrega `<CQRSConnectionToast />`.

---

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Renderizado offline | Apagar la red del navegador muestra "Trabajando sin conexión" |
| 2 | Elemento no bloqueante | El div incluye `pointer-events-none`. |
| 3 | Trazabilidad Inversa | `grep -r "@Traceability: US-017" src/components/` -> 1 resultado. |
| 4 | Build Frontend | `npm run build` genera la carpeta `dist` sin advertencias críticas de TS. |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Crea `src/stores/networkStore.ts`.
2. Crea `src/components/common/CQRSConnectionToast.vue`.
3. Inyéctalo en el Layout.
4. Ejecuta lint: `cd ibpms-platform/frontend && npm run lint`
5. Ejecuta build: `npm run build`
6. Commit: `git add . && git commit -m "feat(ui): add network and CQRS latency monitor toast US-017" && git push`

---

## 📋 Instrucciones para Copiar y Pegar

```
Asume el rol de 🎨 FRONTEND - VUE.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat ibpms-platform/.cursorrules
2. cat ibpms-platform/.agents/skills/frontend_build_audit/SKILL.md
3. cat ibpms-platform/.agents/skills/clean_code_standards/SKILL.md
4. cat ibpms-platform/docs/architecture/adr_014_observabilidad.md
5. cat ibpms-platform/.agentic-sync/handoff_frontend_T07_CQRS_US017.md

TU MISIÓN:

1. Crea `networkStore.ts` en Pinia para manejar el estado online/offline y `isSaving`.
2. Crea `CQRSConnectionToast.vue` para mostrar el mensaje de "Trabajando sin conexión" o "Guardando..." en la esquina inferior izquierda.
3. El mensaje "Guardando..." debe tener un debounce, mostrarse SOLO si `isSaving` lleva true > 5s.
4. Build/Compile: `cd ibpms-platform/frontend && npm run build`
5. Commit: `git add . && git commit -m "feat(ui): implement cqrs latency and network toast US-017" && git push`

REGLAS INQUEBRANTABLES:
- DEBES usar `<script setup lang="ts">` y TypeScript estricto.
- DEBES incluir comentarios `// @Traceability: US-017, CA-19` en el componente creado.
- PROHIBIDO usar alertas nativas (`alert()`); el Toast NO DEBE bloquear clics (`pointer-events-none`).
```
