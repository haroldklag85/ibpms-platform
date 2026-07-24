# 🧠→💻 Handoff: ARQUITECTO LÍDER → FRONTEND - VUE 3
# US-029: Ejecución de Formularios (CA-20 a CA-34)

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** 💻 FRONTEND - VUE 3
**Fecha:** 2026-06-05T01:30:00Z
**Sprint:** 8 — PM-01
**Prioridad:** 🔴 Alta
**Dependencia:** Ninguna

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
cat docs/sprints/gobernanza_pm/01_ROADMAP_Y_METODOLOGIA.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar
> la anotación @Traceability o comentario `// @Traceability: US-XXX, CA-XX`.
> Esto es INNEGOCIABLE.

## 🔬 Diagnóstico del Arquitecto

La US-029 está actualmente en un ~72% de desarrollo en el frontend. Los flujos base de autoguardado y submit están implementados. Sin embargo, según el refinamiento funcional y los nuevos criterios, es necesario incorporar las experiencias avanzadas de UX y resiliencia del CA-20 al CA-34 en la vista de ejecución (Pantalla 2).

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Faltan Refinamientos UI | `frontend/src/views/TaskDetailView.vue` (o componentes asociados) | No hay barra de progreso Wizard (CA-22), advertencia de borrador (CA-26), detección de pestañas dobles (CA-30), ni estilos ReadOnly (CA-33). |

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Implementación de Pestañas Múltiples y Resiliencia (CA-30 y CA-31)

**Archivo:** `frontend/src/composables/useTaskSync.ts` (o similar, en el context del borrador de formulario)

Agrega la lógica de BroadcastChannel para advertir sobre pestañas dobles y el indicador de sincronización.

```typescript
// @Traceability: US-029, CA-30, CA-31
import { ref, onMounted, onUnmounted } from 'vue';

export function useTaskSync(taskId: string) {
  const isDuplicateTab = ref(false);
  const syncStatus = ref<'SYNCED' | 'LOCAL_ONLY' | 'SYNCING' | 'OFFLINE'>('SYNCED');
  let channel: BroadcastChannel | null = null;

  onMounted(() => {
    channel = new BroadcastChannel(`task_edit_${taskId}`);
    // Notificar que esta pestaña abrió la tarea
    channel.postMessage({ type: 'OPENED' });

    channel.onmessage = (event) => {
      if (event.data.type === 'OPENED') {
        // Alguien más la abrió después, o ya estaba abierta
        isDuplicateTab.value = true;
        // Responder que estamos activos
        channel?.postMessage({ type: 'ALREADY_ACTIVE' });
      } else if (event.data.type === 'ALREADY_ACTIVE') {
        isDuplicateTab.value = true;
      }
    };
  });

  onUnmounted(() => {
    channel?.close();
  });

  return { isDuplicateTab, syncStatus };
}
```

### Paso 2: Scroll al Error y Modal de Formulario Vacío (CA-25, CA-32)

**Archivo:** `frontend/src/components/forms/FormExecutor.vue` (o el renderizador de formulario)

```html
<!-- @Traceability: US-029, CA-32 -->
<template>
  <div>
    <!-- Renderizado del formulario -->
    
    <div class="form-actions mt-4">
      <button 
        @click="handleSubmit" 
        :disabled="isDuplicateTab || isSubmitting"
        class="btn-primary"
      >
        <span v-if="isSubmitting" class="animate-spin mr-2">↻</span>
        {{ isSubmitting ? 'Guardando en el servidor...' : 'Enviar' }}
      </button>
    </div>

    <!-- Modal de Confirmación si no hay campos obligatorios -->
    <Teleport to="body">
      <div v-if="showEmptyConfirmModal" class="fixed inset-0 bg-black/50 z-[900] flex items-center justify-center">
        <div class="bg-white p-6 rounded-lg max-w-md">
          <h3 class="text-lg font-bold mb-4">¿Estás seguro de que deseas completar esta tarea?</h3>
          <p class="mb-4">Esta acción no se puede deshacer.</p>
          <div class="flex justify-end gap-2">
            <button @click="showEmptyConfirmModal = false" class="btn-secondary">Cancelar</button>
            <button @click="executeSubmit" class="btn-primary">Sí, completar</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
// @Traceability: US-029, CA-25
const scrollToFirstError = () => {
  const firstErrorEl = document.querySelector('.has-error');
  if (firstErrorEl) {
    firstErrorEl.scrollIntoView({ behavior: 'smooth', block: 'center' });
    (firstErrorEl as HTMLElement).focus();
  }
};
</script>
```

### Paso 3: Estilos ReadOnly y Feedback Visual de Envío (CA-20, CA-33)

**Archivo:** `frontend/src/assets/css/forms.css` o dentro de los `<style>` del componente base de inputs.

```css
/* @Traceability: US-029, CA-33 */
.input-readonly {
  background-color: #F5F5F5;
  border: 1px solid #e5e7eb;
  cursor: not-allowed;
  position: relative;
}

.input-readonly::before {
  content: "🔒";
  position: absolute;
  left: 8px;
  top: 50%;
  transform: translateY(-50%);
}
```

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Prevención de pestaña doble activa | Abrir la tarea en 2 pestañas muestra el warning en la segunda bloqueando botones. |
| 2 | Scroll automático a error | Al fallar validación de Zod, la UI hace scroll al primer campo rojo. |
| 3 | Modal preventivo para form opcional | Si el schema no tiene campos `required`, presionar enviar muestra modal Z-900. |
| 4 | Build exitoso | `npm run build` pasa sin errores de TypeScript/Linting. |

## 🚦 SECUENCIA DE EJECUCIÓN

1. Implementar `useTaskSync` para control de pestañas y estado de sync.
2. Integrar scroll suave al encontrar errores de validación.
3. Agregar el Modal de confirmación (Teleport Z-900) para formularios sin campos obligatorios.
4. Aplicar estilos read-only estandarizados (`bg-[#F5F5F5]` y cursor `not-allowed`).
5. Ejecutar Build UI: `npm run build`
6. Commit: `git add . && git commit -m "feat(frontend): implementar CA-20 a CA-34 para US-029" && git push origin sprint-8/pm-01/us-029-form-exec`

## 📋 Instrucciones para Copiar y Pegar

```text
Asume el rol de 💻 FRONTEND - VUE 3.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat .agents/skills/frontend_build_audit/SKILL.md
3. cat .agents/skills/clean_code_standards/SKILL.md
4. cat .agents/skills/zero_mock_enforcement/SKILL.md
5. cat .agentic-sync/handoff_FRONTEND_US029_CA20_CA34.md

TU MISIÓN:

1. Implementar la prevención de doble pestaña y el estado de sincronización (useTaskSync).
2. Agregar scroll suave al primer error de Zod y el modal Teleport Z-900 para forms vacíos.
3. Actualizar los estilos para campos `readOnly` con candado visual.
4. Build/Compile: Ejecutar auditoría SRE del frontend (npm run build).
5. Commit: git add . && git commit -m "feat(frontend): implementar CA-20 a CA-34 para US-029" && git push origin sprint-8/pm-01/us-029-form-exec

REGLAS INQUEBRANTABLES:
- Prohibido el uso de mocks en los llamados API.
- Todo CSS/Tailwind inyectado debe alinearse al diseño corporativo.
- Mantener cobertura de tests unitarios (si aplica).
```
