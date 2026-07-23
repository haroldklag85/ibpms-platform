# 🏗️ Handoff Arquitectónico: Hotfix BUG-S7-005 y BUG-S7-006

## 1. Metadatos y SSOT (Single Source of Truth)
- **Iteración/Sprint:** `sprint-7/bugfix-uat`
- **User Story:** US-003
- **Criterios de Aceptación (CAs):** CA-41 (Restricciones en Grillas Repetibles), CA-69 (Simulador Multi-Rol / Etapas).
- **SSOT:** `docs/requirements/epics/epic_B_formularios_bpmn.md` y `docs/uat/casos_uso_uat_j02.md`.
- **Flujo de Trabajo:** Frontend.

## 2. Alineación Arquitectónica y ADRs
- **Validación de ADRs:** Cumplimiento estricto de Zero-Trust UI y LEY GLOBAL 0. No se debe tolerar la compilación sin validación.
- **Lineamientos Transversales:** La arquitectura de UI basada en VueDraggable exige que la recursividad de componentes anidados (como `field_array`) se replique de manera determinista al igual que se ha hecho para `container`, `tabs` o `accordion`. El simulador de etapas debe ser dinámico (Server-Driven UI) y no basarse en valores quemados (hardcoded).
- **Trazabilidad:** La UI y el Fuzzer deben ser coherentes con el Payload esperado por el motor Zod (arrays anidados para los field_arrays).

## 3. Rutas Exactas y Contexto Preexistente
**Archivo 1:** `frontend/src/views/admin/Modeler/FormDesigner.vue`
- **Contexto:**
  - L143-148: Opciones hardcodeadas `<option value="ANALYSIS">ANALYSIS</option>`... Deben reemplazarse por una computada dinámica `availableStages`.
  - L226+: El template de iteración `<template #item="{ element, index }">` renderiza `container`, `tabs`, `accordion`, pero no tiene soporte para `element.type === 'field_array'`.
  
**Archivo 2:** `frontend/src/stores/useFormDesignerStore.ts`
- **Contexto:**
  - El método `generateMockPath` aplana los campos usando `flatFields` y genera un JSON objeto en un solo nivel, lo cual rompe la validación para `field_array` que espera un array de objetos (`[{}]`). 
  - Falta agregar la computada `availableStages` para ser consumida por el componente Vue.

## 4. Snippets Prescriptivos (El "Qué" y el "Cómo")

### A. Para `FormDesigner.vue` (Dropdown de Stages):
```html
<select v-model="activeStageSim" class="bg-white border-blue-300 rounded text-xs py-0.5 focus:ring-blue-500 font-mono">
  <option value="START_EVENT">START_EVENT</option>
  <option v-for="stage in availableStages" :key="stage" :value="stage">{{ stage }}</option>
  <option value="ALL">Mostrar Todos (Ideation)</option>
</select>
```

### B. Para `useFormDesignerStore.ts` (Computed Stages):
```typescript
const availableStages = computed(() => {
  const stages = new Set<string>();
  const flatF = flatFields(canvasFields.value);
  flatF.forEach(f => {
    if (f.stage && f.stage !== 'START_EVENT' && f.stage !== 'ALL') {
      stages.add(f.stage);
    }
  });
  return Array.from(stages);
});
// Exponer availableStages en el return del store
```

### C. Para `FormDesigner.vue` (Canvas Drag & Drop field_array):
Inyectar un bloque `v-if="element.type === 'field_array'"` idéntico en estructura visual al bloque `container`, pero con estilos indicativos de tabla/grilla:
```html
<div v-if="element.type === 'field_array'" class="border border-emerald-200 bg-emerald-50/50 rounded-lg p-4 mt-2 min-h-[120px]">
  <h4 class="text-[10px] font-bold text-emerald-700 uppercase mb-2">📋 Grilla Repetible: {{ element.label }}</h4>
  <VueDraggable
     v-model="element.children"
     :group="{ name: 'form-builder', pull: true, put: true }"
     item-key="id"
     class="min-h-[100px] transition-all"
     :class="{'border-2 border-dashed border-gray-300 bg-gray-50 flex flex-col items-center justify-center': !element.children || element.children.length === 0}"
     animation="200"
     ghost-class="ghost-dropzone"
  >
     <template #item="{ element: child, index: childIdx }">
       <!-- Replicar exactamente la tarjeta interna usada en 'container' (sub-nivel visual) -->
       <div v-show="evaluateMockVis(child)" class="...">
         <!-- Botonera de propiedades, basura, etc. -->
         <!-- Inputs anidados -->
       </div>
     </template>
     <template #footer>
        <div v-if="!element.children || element.children.length === 0" class="text-gray-400 font-bold text-xs pointer-events-none mt-2">Arrastre componentes aquí para conformar las columnas de la Grilla</div>
     </template>
  </VueDraggable>
</div>
```

### D. Para `useFormDesignerStore.ts` (Mock Path Recursivo):
Reescribir `generateMockPath` (sin usar `flatFields` para no romper la estructura jerárquica):
```typescript
const generateMockPath = (type: string, fuzzerPayloadRef: any) => {
  const buildMock = (fields: any[]): any => {
    let mock: any = {};
    fields.forEach(f => {
      if (f.type.startsWith('button_') || f.type === 'tabs' || f.type === 'accordion') return;
      
      const key = f.camundaVariable || f.id;
      
      if (f.type === 'container') {
        const childMocks = buildMock(f.children || []);
        Object.assign(mock, childMocks); // Flatten objects from container
      } else if (f.type === 'field_array') {
        // Zod validation for arrays requires an array of objects
        mock[key] = [buildMock(f.children || [])];
      } else {
        if (type === 'happy') {
          if (f.type === 'number' || f.type === 'timer') mock[key] = 42;
          else if (f.type === 'checkbox') mock[key] = true;
          else if (f.type === 'email') mock[key] = 'test@example.com';
          else if (f.type === 'url') mock[key] = 'https://example.com';
          else if (f.isMultiple) mock[key] = ['Option1'];
          else if (f.type === 'file' || f.type === 'signature') mock[key] = '550e8400-e29b-41d4-a716-446655440000';
          else mock[key] = 'Dummy Data';
        } else {
          mock[key] = null;
        }
      }
    });
    return mock;
  };
  
  fuzzerPayloadRef.value = JSON.stringify(buildMock(canvasFields.value), null, 2);
};
```

## 5. Matriz de QA y Testing Atómico
Se debe realizar validación atómica mediante Vitest comprobando que la función `generateMockPath` crea correctamente la estructura Array para `field_array`.
- **Script:** `frontend/src/stores/__tests__/useFormDesignerStore.spec.ts`

| Test Name | CA Evaluado | Aserción Esperada |
| --- | --- | --- |
| `MockPath_Returns_Array_For_FieldArray` | CA-41 / BUG-S7-006 | La función `generateMockPath('happy')` retorna un JSON que contiene la variable del `field_array` cuyo valor es un array de objetos con las propiedades mock de los hijos `[{...}]`. |
| `AvailableStages_Computed_Removes_Duplicates` | CA-69 / BUG-S7-005 | `availableStages.value` retorna un Set de valores únicos (Ej: `['INSPECTION', 'VALUATION']`) omitiendo `START_EVENT` y `ALL`. |

## 6. Mensaje de Despacho (Comunicación al Agente Especialista)
**Para Agente Frontend:**
> "Build obligatorio: Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`. Debes aplicar las correciones en `FormDesigner.vue` y `useFormDesignerStore.ts` especificadas en `.agentic-sync/handoff_BUG_S7_005_006.md`. Una vez terminado, compila y notifica el éxito."
