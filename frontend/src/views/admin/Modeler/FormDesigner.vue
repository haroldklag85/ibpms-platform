<template>
  <div class="h-full w-full bg-gray-50 flex flex-col pt-4 px-6 gap-4">
    <header class="flex justify-between items-center shrink-0">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">IDE de Formularios Pro-Code (iForms)</h1>
        <p class="text-sm text-gray-500">Editor visual bidireccional Vue3/Zod. Diseña esquemas estables para casos BPMN de múltiples etapas (US-003).</p>
      </div>
      <div class="flex items-center gap-3">
        <button @click="generateTests" class="bg-gray-800 text-yellow-400 px-4 py-2 border border-black rounded-md shadow-sm text-sm font-medium hover:bg-black transition flex gap-2 items-center">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path></svg>
          [⚡ Generador de Tests] (US-028)
        </button>

        <button @click="simulateMockSubmit" class="bg-indigo-600 text-white px-4 py-2 rounded-md shadow text-sm font-medium hover:bg-indigo-700 flex gap-2 items-center">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
          Probar / Submit Mock (US-029)
        </button>
      </div>
    </header>

    <main class="flex-1 flex gap-4 min-h-0 relative">
      <!-- Toolbox Izquierda -->
      <aside class="w-64 bg-white border border-gray-200 rounded-xl shadow-sm p-4 overflow-y-auto hidden md:block">
        <h3 class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-4">Inputs</h3>
        <div class="space-y-2">
           <div class="text-sm p-2 border border-gray-200 rounded bg-gray-50 hover:bg-indigo-50 hover:border-indigo-300 cursor-move flex items-center gap-2">
             <span class="text-gray-500">Ab</span> Input Text
           </div>
           <div class="text-sm p-2 border border-gray-200 rounded bg-gray-50 hover:bg-indigo-50 hover:border-indigo-300 cursor-move flex items-center gap-2">
             <span class="text-gray-500">#</span> Number Field
           </div>
           <div class="text-sm p-2 border border-gray-200 rounded bg-gray-50 hover:bg-indigo-50 hover:border-indigo-300 cursor-move flex items-center gap-2">
             <span class="text-gray-500">≡</span> Dropdown (Select)
           </div>
           <div class="text-sm p-2 border border-gray-200 rounded bg-gray-50 hover:bg-indigo-50 hover:border-indigo-300 cursor-move flex items-center gap-2">
             <span class="text-gray-500">📎</span> File Upload (PDF)
           </div>
        </div>
      </aside>

      <!-- Lienzo Central de Componentes Vue Renderizados -->
      <section class="flex-1 bg-white border border-gray-200 rounded-xl shadow-sm p-6 overflow-y-auto">
         <h2 class="text-lg font-medium text-gray-800 border-b pb-2 mb-4">Previsualización (Render Output)</h2>
         <p class="text-sm text-gray-500 mb-6 italic">Arrastra campos aquí. Se enlazan a Current_Stage para mostrar/ocultar.</p>

         <div class="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center text-gray-400 bg-gray-50/50 mb-6">
            <span v-if="fields.length === 0">Zona de Trabajo: Dropzone para iForm</span>
            <div v-else class="space-y-4 text-left">
               <!-- Mock de campos arrastrados -->
               <div v-for="(field, index) in fields" :key="index" class="p-3 border border-indigo-100 bg-indigo-50 rounded group relative">
                  <label class="block text-xs font-semibold text-gray-700 mb-1">{{ field.label }} <span v-if="field.required" class="text-red-500">*</span></label>
                  <input :type="field.type" :placeholder="field.placeholder" class="w-full text-sm border-gray-300 rounded focus:ring-indigo-500 focus:border-indigo-500 bg-white disabled:bg-gray-100" />
                  
                  <button @click="removeField(index)" class="absolute top-2 right-2 text-red-400 hover:text-red-700 hidden group-hover:block">
                     <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                  </button>
               </div>
            </div>
         </div>

         <!-- Botón para inyectar mock fields en ausencia de Drag n Drop nativo -->
         <button @click="addMockField" class="text-sm text-indigo-600 hover:text-indigo-800 font-medium">
             + Agregar Campo Numérico ("Monto Aprobado")
         </button>

      </section>

      <!-- Monaco IDE - Vista Bidireccional de Código -->
      <aside class="w-1/3 min-w-[300px] bg-[#1e1e1e] border border-gray-800 rounded-xl shadow-sm text-gray-300 flex flex-col overflow-hidden">
        <div class="bg-black/40 px-4 py-2 text-xs font-mono border-b border-gray-700 flex justify-between items-center text-gray-400">
           <span>schema.zod.ts</span>
           <span class="text-emerald-500 animate-pulse">● Live Sync</span>
        </div>
        <div class="p-4 overflow-y-auto flex-1 font-mono text-xs leading-relaxed">
           <pre><span class="text-blue-400">import</span> { z } <span class="text-blue-400">from</span> <span class="text-orange-300">'zod'</span>;

<span class="text-blue-400">export const</span> iFormSchema = z.<span class="text-yellow-200">object</span>({
<span v-if="fields.length === 0" class="text-gray-500">  // Mueve componentes al lienzo...</span><span v-else v-for="field in fields" :key="'code-'+field.id" class="text-gray-300">  {{ field.id }}: z.<span class="text-yellow-200">{{ field.zodType }}</span>(){{ field.required ? '' : '.optional()' }},
</span>});</pre>
        </div>
      </aside>

    </main>

    <!-- Modal de Resultados Mock -->
    <div v-if="showModal" class="fixed inset-0 bg-gray-900/50 flex items-center justify-center z-50">
        <div class="bg-white rounded-lg p-6 max-w-lg w-full shadow-2xl">
            <h3 class="text-lg font-bold text-gray-900 mb-2">{{ modalTitle }}</h3>
            <div class="bg-gray-100 p-3 rounded font-mono text-xs text-gray-800 whitespace-pre-wrap max-h-60 overflow-y-auto mb-4 border border-gray-200">
                {{ modalContent }}
            </div>
            <div class="flex justify-end">
                <button @click="showModal = false" class="bg-indigo-600 text-white px-4 py-2 rounded shadow hover:bg-indigo-700 text-sm font-medium">Cerrar</button>
            </div>
        </div>
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue'

const fields = ref([])
const showModal = ref(false)
const modalTitle = ref('')
const modalContent = ref('')

const addMockField = () => {
   if(fields.value.length === 0) {
      fields.value.push({
         id: 'monto_aprobado',
         label: 'Monto Aprobado',
         type: 'number',
         placeholder: 'Ej: 1500',
         required: true,
         zodType: 'number().positive'
      })
   } else {
       fields.value.push({
         id: 'comentarios',
         label: 'Comentarios Adicionales',
         type: 'text',
         placeholder: 'Opcional',
         required: false,
         zodType: 'string'
      })
   }
}

const removeField = (idx) => {
    fields.value.splice(idx, 1)
}

const generateTests = () => {
    modalTitle.value = "Generación de Test Suites Zod (.spec.ts) (US-028)"
    modalContent.value = `import { describe, it, expect } from 'vitest';
import { iFormSchema } from './form-schema';

describe('iForm_Maestro Schema Validation', () => {
   it('debería retornar HTTP 400 ValidationFailed si falta un campo requerido', () => {
      const payload = {}; // Payload roto
      const result = iFormSchema.safeParse(payload);
      expect(result.success).toBe(false);
      expect(result.error.issues[0].message).toBe("Required");
   });

   ${fields.value.length > 0 ? `it('debería aprobar un payload válido con ${fields.value[0].id}', () => {
      const result = iFormSchema.safeParse({ ${fields.value[0].id}: 100 });
      expect(result.success).toBe(true);
   });` : '// Agrega campos al schema para generar aserciones concretas.'}
});`
    showModal.value = true
}

const simulateMockSubmit = () => {
    modalTitle.value = "Ejecución Mock de Envío (Submit Task) (US-029)"
    if(fields.value.length === 0) {
        modalContent.value = "⚠️ No hay campos en el formulario para validar."
    } else {
        modalContent.value = `Enviando POST a /api/v1/workbox/tasks/TK-100/complete

Payload enviado intencionalmente VACÍO { variables: {} } para probar evaluación estricta...

Resultado en Frontend: HTTP 400 Bad Request
Excepción Zod atrapada.

{
  "error": "ValidationFailed",
  "fields": [
    { "field": "${fields.value[0].id}", "message": "Required" }
  ]
}`
    }
    showModal.value = true
}
</script>
