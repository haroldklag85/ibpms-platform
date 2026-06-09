# CONTRATO DE DELEGACIÓN ARQUITECTÓNICA (HANDOFF FRONTEND - BUGS SECUNDARIOS J02)

## 1. Metadatos del Handoff
- **Iteración / Sprint:** Sprint 7 (Cierre de Deuda Técnica UAT)
- **Historia de Usuario:** US-003
- **IDs de Bugs:** BUG-S7-002, BUG-S7-003
- **Agente Especialista:** Frontend Agent (Vue3 + Pinia)
- **Arquitecto Delegante:** Arquitecto Líder (IA)
- **Nivel de Severidad:** P2 (Media) y P3 (Baja)

## 2. Descripción del Problema
Para cerrar exitosamente el Journey J-02 y garantizar la experiencia del usuario (UX) en la pantalla de Modelado de Formularios, se deben resolver las siguientes dos brechas secundarias reportadas:

1. **BUG-S7-002 (P2): Opciones de Radio Button Inmutables.**
   - Actualmente, el componente de Radio Button (y Select) no tiene un editor manual en el panel derecho de Propiedades (`FormDesigner.vue`). Las opciones ("Opción 1", "Opción 2") quedan rígidas. 
   - El Select permite subir un CSV, pero el usuario necesita poder escribir opciones a mano rápidamente.
2. **BUG-S7-003 (P3): Título del Canvas Fijo.**
   - El `formTitle` se inyecta desde Pinia (`useFormDesignerStore.ts`) con el valor predeterminado "Solicitud Onboarding (V1)" y se muestra en la UI como texto duro `{{ formTitle }}` dentro de un `<h2>`. Al no ser un input, el humano no puede renombrar el formulario.

## 3. Plan de Solución Arquitectónica (ACCIONES REQUERIDAS)

Abre `frontend/src/views/admin/Modeler/FormDesigner.vue` y realiza **únicamente** estas dos modificaciones:

### A. Reparar BUG-S7-003 (Input para formTitle)
Ubica el `<h2>` que renderiza el título del formulario (alrededor de la línea 154):
```html
<h2 class="text-xl font-bold text-gray-800 mb-6 border-b pb-4 font-sans">{{ formTitle }}</h2>
```
Reemplázalo por un input enlazado reactivamente a `formTitle` (el cual ya está destructurado con `storeToRefs` en el script setup):
```html
<input v-model="formTitle" class="text-xl font-bold text-gray-800 mb-6 border-b pb-4 font-sans w-full bg-transparent outline-none hover:bg-gray-50 focus:bg-white focus:ring-2 focus:ring-indigo-200 transition-colors cursor-text" title="Clic para editar el nombre del formulario" />
```

### B. Reparar BUG-S7-002 (Textarea para opciones de Radio/Select)
Ubica la zona del panel derecho donde se editan las propiedades. Específicamente, busca el bloque donde está la subida de CSV para `select` (alrededor de la línea 480). Justo encima de ese bloque, agrega un `textarea` manual para las opciones:
```html
<div v-if="['select', 'radio'].includes(editingField.type)" class="mb-4">
   <label class="block text-xs font-bold text-gray-700 mb-1">Opciones (Una por línea)</label>
   <textarea :value="(editingField.options || []).join('\n')" @input="e => editingField.options = (e.target.value || '').split('\n').filter(o => o.trim())" rows="4" class="w-full text-sm border-gray-300 rounded" placeholder="Opción 1&#10;Opción 2&#10;Opción 3"></textarea>
</div>
```

## 4. Política Anti-Amnesia (OBLIGATORIA)
- Altera solo las líneas indicadas. No elimines código que no sea necesario.
- Compila con `npm run build` para asegurar que el parsing de los eventos del `@input` no arroje error de tipado (TypeScript).

## 5. Salida Esperada
- `git commit -am "fix(UX): permitir edicion titulo form y opciones radio/select (BUG-S7-002, BUG-S7-003)"`
- `git push` a la rama `sprint-7/bugfix-uat`.
- Responder al Arquitecto Líder que los bugs secundarios han sido parchados para cerrar la deuda técnica del sprint.
