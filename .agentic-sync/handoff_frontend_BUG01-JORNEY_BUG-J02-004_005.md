# 🎨 Handoff Frontend — BUG-J02-004 + BUG-J02-005 (Filtro FormKey + CSS Dropdown)

> **Iteración**: Sprint 01-devDavid  
> **US**: BUG01-JORNEY  
> **Bugs**: BUG-J02-004 (P2) + BUG-J02-005 (P3)  
> **Rama de Trabajo**: `DevDavid` (**OBLIGATORIO** — prohibido trabajar en `main`)  
> **Orden de ejecución**: 🥈 **PASO 2 de 2** (ejecutar SOLO después de que el Backend haya hecho push)  
> **SSOT**: `docs/qa/INFORME_TECNICO_QA_J02_PM01.md` → Líneas 236-237  
> **Arquitecto Líder**: Agente Orquestador (chat principal)

---

## Pre-Handoff Checklist — BUG-J02-004 + BUG-J02-005

| # | Verificación | Estado | Evidencia |
|---|---|---|---|
| 1 | US en sprint actual (Roadmap PM-IA) | ✅ | Sprint PM-01, Cadena 4 (BPMN E2E) — Bugs de certificación J-02 |
| 2 | Endpoints definidos en API_CONTRACTS.md | ✅ | `GET /api/v1/forms/active` — Usado por dropdown FormKey (verificado en código) |
| 3 | Prerrequisitos completados | ✅ | US-005 (Motor BPMN) ~97% completada |
| 4 | Matriz de cobertura actualizada | ✅ | INFORME_TECNICO_QA_J02_PM01.md — CA-39 ✅, CA-40 ⚠️ parcial |

**Resultado**: ✅ APROBADO para handoff

---

## 1. Metadatos y SSOT

| Campo | Valor |
|-------|-------|
| **Bug IDs** | BUG-J02-004, BUG-J02-005 |
| **Tipos** | Funcional (004) + Cosmético (005) |
| **Severidades** | P2 (004), P3 (005) |
| **Misión UAT Origen** | M5 — Dropdown FormKey |
| **Archivo principal afectado** | `frontend/src/views/admin/Modeler/BpmnDesigner.vue` |
| **Credenciales de prueba** | `root@ibpms.local` / `Root#Temp4Sys` |
| **Rama Git** | `DevDavid` |

---

## 2. Alineación Arquitectónica y ADRs

| ADR | Impacto |
|-----|---------|
| **ADR-002** (Vue 3 + Vite) | El componente `BpmnDesigner.vue` (4426 líneas, 222 KB) es un componente monolítico. **NO incrementes su tamaño innecesariamente.** Los cambios deben ser quirúrgicos y localizados. |
| **ADR-010** (Pirámide Testing) | Si modificas la lógica de `filteredForms`, los tests existentes en `BpmnDesigner.spec.ts` deben seguir pasando. |

**Stack confirmado**: Vue 3 / TypeScript / Vitest. Sin violaciones.

---

## 3. Causa Raíz Verificada — Rutas Exactas y Contexto Preexistente

### BUG-J02-004: Sin filtro/toggle Simple vs Maestro en dropdown FormKey

**Archivo**: `frontend/src/views/admin/Modeler/BpmnDesigner.vue`

**Lógica actual del filtro** (líneas 2847-2851):
```typescript
const filteredForms = computed(() => {
  if (processPattern.value === 'SIMPLE') return availableForms.value.filter(f => f.type === 'SIMPLE');
  if (processPattern.value === 'IFORM_MAESTRO') return availableForms.value.filter(f => f.type === 'MAESTRO');
  return availableForms.value;
});
```

**Variable `processPattern`** (línea 1628):
```typescript
const processPattern = ref<'SIMPLE' | 'IFORM_MAESTRO'>('SIMPLE');
```

**Selector de `processPattern`** (líneas 490-501):
```html
<!-- Process Pattern (CA-31 y CA-38) -->
<select v-model="processPattern" @change="updateProcessProperty('formPattern', processPattern)" 
        :disabled="elementCount > 1" class="...">
  <option value="SIMPLE">🟢 Simple (Formularios independientes)</option>
  <option value="IFORM_MAESTRO">🔵 iForm Maestro (Formulario mutante)</option>
</select>
<p v-if="elementCount > 1" class="text-[9px] text-gray-500 mt-1">🔒 Bloqueado: El lienzo no está vacío.</p>
```

**Dropdown FormKey — UserTask** (líneas 522-535):
```html
<!-- FormKey -->
<div class="p-3 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded shadow-sm">
  <label class="block text-xs font-bold text-gray-800 dark:text-gray-200 mb-2 flex items-center justify-between">
    📝 FormKey (User Task)
    <AppTooltip :content="bpmnTooltips.FORM_KEY" />
  </label>
  <p class="text-[10px] text-gray-500 dark:text-gray-400 mb-2">Formulario renderizado en Workdesk</p>
  <select v-model="selectedFormKey" @change="syncElementProperties('camunda:formKey', selectedFormKey)" 
          class="w-full text-xs font-mono border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded p-2 border bg-indigo-50/30 dark:bg-indigo-900/20 text-indigo-800 dark:text-indigo-300">
    <option value="">-- Sin FormKey --</option>
    <option v-for="form in filteredForms" :key="form.key" :value="form.key">
      {{ form.type === 'MAESTRO' ? '🔵' : '🟢' }} {{ form.name }} ({{ form.key }})
    </option>
  </select>
</div>
```

**Dropdown FormKey — StartEvent** (líneas 590-603): Estructura idéntica al UserTask.

**Problema reportado por Harold**: "No se cuenta con filtro, como en la primera selección se eligió Simple solo aparece Simple, aun existiendo formularios maestros".

**Decisión del humano (Harold)**: Mantener la lógica existente del `processPattern` pero agregar un **filtro adicional** junto al dropdown FormKey para acceso rápido a formularios Simple y Maestro. El filtro adicional mejora la UX sin modificar el comportamiento del patrón de proceso.

### BUG-J02-005: Estilos CSS del dropdown FormKey

**Problema**: Harold reportó que "se encuentra el formulario pero los estilos deben ser corregidos". El `<select>` del FormKey usa clases Tailwind inline sin un estilo coherente con el resto del design system del panel de propiedades BPMN.

**Áreas afectadas**:
- Línea 529: `<select>` del FormKey UserTask
- Línea 597: `<select>` del FormKey StartEvent

---

## 4. Snippets Prescriptivos — Solución Detallada

### 4.1 BUG-J02-004: Agregar filtro visual de tipo de formulario

**Crear una nueva ref** (junto a `filteredForms`, ~línea 2847):
```typescript
// BUG-J02-004: Filtro visual adicional para acceso rápido Simple/Maestro
const formTypeFilter = ref<'ALL' | 'SIMPLE' | 'MAESTRO'>('ALL');
```

**Modificar el computed `filteredForms`** (líneas 2847-2851) para combinar AMBOS filtros:
```typescript
const filteredForms = computed(() => {
  let forms = availableForms.value;
  
  // Filtro 1: Por patrón del proceso (lógica existente — mantener)
  if (processPattern.value === 'SIMPLE') {
    forms = forms.filter(f => f.type === 'SIMPLE');
  } else if (processPattern.value === 'IFORM_MAESTRO') {
    forms = forms.filter(f => f.type === 'MAESTRO');
  }
  
  // Filtro 2: Filtro visual adicional del usuario (BUG-J02-004)
  if (formTypeFilter.value === 'SIMPLE') {
    forms = forms.filter(f => f.type === 'SIMPLE');
  } else if (formTypeFilter.value === 'MAESTRO') {
    forms = forms.filter(f => f.type === 'MAESTRO');
  }
  
  return forms;
});
```

**Agregar toggle visual ANTES del `<select>` del FormKey** (tanto en la sección UserTask como en StartEvent).

El toggle debe insertarse ENTRE el `<p>` descriptivo y el `<select>` del dropdown. Debe tener 3 botones: "Todos", "🟢 Simple", "🔵 Maestro".

**Template HTML para el toggle** (insertar justo antes de `<select v-model="selectedFormKey"...`):
```html
<!-- BUG-J02-004: Filtro rápido de tipo de formulario -->
<div class="flex gap-1 mb-2">
  <button 
    v-for="filterOpt in [
      { value: 'ALL', label: 'Todos', icon: '📋' },
      { value: 'SIMPLE', label: 'Simple', icon: '🟢' },
      { value: 'MAESTRO', label: 'Maestro', icon: '🔵' }
    ]" 
    :key="filterOpt.value"
    @click="formTypeFilter = filterOpt.value"
    :class="[
      'px-2 py-1 text-[10px] font-medium rounded-md border transition-all duration-150',
      formTypeFilter === filterOpt.value
        ? 'bg-indigo-600 text-white border-indigo-600 shadow-sm'
        : 'bg-white dark:bg-gray-700 text-gray-600 dark:text-gray-300 border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-600'
    ]"
    type="button"
  >
    {{ filterOpt.icon }} {{ filterOpt.label }}
  </button>
</div>
```

**IMPORTANTE**: Este mismo bloque se debe insertar en DOS lugares:
1. **Sección UserTask** (después de línea 528, antes de línea 529)
2. **Sección StartEvent** (después de línea 596, antes de línea 597)

**Exponer la nueva ref** en la sección `defineExpose` (~línea 4225):
```typescript
// Agregar junto a las demás refs expuestas:
formTypeFilter,
```

### 4.2 BUG-J02-005: Refactorizar estilos CSS del dropdown FormKey

**Reemplazar las clases del `<select>`** en las líneas 529 y 597.

**Clases ACTUALES** (ambas líneas):
```
w-full text-xs font-mono border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded p-2 border bg-indigo-50/30 dark:bg-indigo-900/20 text-indigo-800 dark:text-indigo-300
```

**Clases NUEVAS** (aplicar a AMBOS selects):
```
w-full text-xs font-mono rounded-lg p-2.5 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white shadow-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 dark:focus:ring-indigo-400 dark:focus:border-indigo-400 transition-colors duration-150 appearance-none cursor-pointer hover:border-indigo-400 dark:hover:border-indigo-500
```

**Mejoras**:
- `rounded-lg` → Bordes más redondeados, consistente con el design system
- `p-2.5` → Padding ligeramente mayor para mejor touch target
- `bg-white dark:bg-gray-700` → Fondo sólido en lugar de semi-transparente
- `text-gray-900 dark:text-white` → Contraste completo
- `shadow-sm` → Sombra sutil para profundidad
- `focus:ring-2 focus:ring-indigo-500` → Ring de foco visible para accesibilidad
- `transition-colors duration-150` → Transición suave
- `hover:border-indigo-400` → Hover state visible
- `appearance-none cursor-pointer` → Mejor UX

---

## 5. Matriz de QA (Validación Cruzada)

| Test | Bug Evaluado | Aserción Esperada |
|------|-------------|-------------------|
| `Toggle de filtro se renderiza junto al dropdown FormKey en UserTask` | BUG-J02-004 | 3 botones visibles: Todos, Simple, Maestro |
| `Toggle de filtro se renderiza junto al dropdown FormKey en StartEvent` | BUG-J02-004 | 3 botones visibles: Todos, Simple, Maestro |
| `Al seleccionar 'Maestro' en el toggle, solo se muestran formularios tipo MAESTRO` | BUG-J02-004 | Opciones del select filtradas correctamente |
| `Al seleccionar 'Simple' en el toggle, solo se muestran formularios tipo SIMPLE` | BUG-J02-004 | Opciones del select filtradas correctamente |
| `Al seleccionar 'Todos', se muestran todos los formularios disponibles` | BUG-J02-004 | Todas las opciones visibles |
| `La lógica existente de processPattern sigue funcionando` | Regresión | Si processPattern=SIMPLE, el toggle 'Maestro' no muestra formularios maestros |
| `Los tests existentes en BpmnDesigner.spec.ts siguen pasando` | Regresión | `npm run test -- BpmnDesigner.spec.ts` → PASS |
| `El select del FormKey tiene estilos actualizados` | BUG-J02-005 | Classes CSS incluyen `rounded-lg`, `shadow-sm`, `focus:ring-2` |

---

## 6. Directivas Operativas

> **📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA:**
> El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Antes de iniciar tu trabajo, ejecuta el protocolo de pre-validación:
> 1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
> 2. Si no responde, arráncalo con: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
> 3. Verifica los servicios Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`) y RabbitMQ (`5672`) deben estar `Up (healthy)`.
> **PROHIBIDO** levantar el backend vía Docker o modificar el `docker-compose.yml`.

> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

> **Build obligatorio:** Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.

> **NFR/QA Strategy:** Desarrollar sobre la arquitectura en `docs/architecture/arquitecturar.md`. Preservar la arquitectura del proyecto. No alucinar, no imaginar, no salir del contexto dado. Referenciar BUG-J02-004 y BUG-J02-005 en toda documentación de la solución.

---

## INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN

1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_FRONTEND.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_FRONTEND.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION` y programa la solución.
6. **ANTES del commit final**, actualiza `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` explicando tu trabajo en lenguaje sencillo y amigable para un CEO (QUÉ hiciste y PARA QUÉ sirve).
7. Finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama `DevDavid`. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).

> 🧠 **POLÍTICA ANTIAMNESIA:** Antes de codificar, LEE OBLIGATORIAMENTE:
> 1. `docs/architecture/arquitecturar.md` — Arquitectura Core
> 2. `docs/qa/INFORME_TECNICO_QA_J02_PM01.md` — Descripción exacta de los bugs (líneas 236-237)
> 3. `docs/sprints/gobernanza_pm/GUIA_ARQUITECTO_LIDER.md` — Directrices estratégicas
