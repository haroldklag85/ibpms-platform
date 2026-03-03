<template>
  <div class="h-full w-full bg-gray-50 flex flex-col" v-cloak>

    <!-- ═══════ Toast Notifications ═══════ -->
    <Transition name="toast-slide">
      <div v-if="toast.msg" :class="toast.type === 'success' ? 'bg-green-600' : 'bg-red-600'" class="fixed top-4 right-4 z-[100] text-white px-5 py-3 rounded-lg shadow-xl flex items-center space-x-3">
        <span class="text-sm font-medium">{{ toast.msg }}</span>
        <button @click="toast.msg = ''" class="ml-2 opacity-70 hover:opacity-100">&times;</button>
      </div>
    </Transition>

    <!-- ═══════ Header Toolbar ═══════ -->
    <header class="flex justify-between items-center px-6 py-3 bg-white border-b border-gray-200 shrink-0">
      <div class="flex items-center space-x-4">
        <div>
          <h1 class="text-xl font-bold text-gray-900 flex items-center gap-2">
            IDE de Formularios Vue3/Zod
            <span class="text-xs font-bold text-white px-2 py-0.5 rounded-full" :class="formPattern === 'IFORM_MAESTRO' ? 'bg-blue-600' : 'bg-green-600'">
              {{ formPattern === 'IFORM_MAESTRO' ? '🔵 iForm Maestro' : '🟢 Simple' }}
            </span>
          </h1>
          <p class="text-xs text-gray-500 mt-0.5">Editor bidireccional Vue3 Composition API + Validaciones Zod (US-003)</p>
        </div>
      </div>
      
      <div class="flex items-center gap-2">
        <!-- Generador Tests -->
        <button @click="generateTests" class="bg-gray-800 text-yellow-400 px-3 py-1.5 border border-black rounded shadow-sm text-xs font-semibold hover:bg-black transition flex gap-1.5 items-center">
          ⚡ Generar Tests Zod (CA-115)
        </button>

        <!-- Reset Dual -->
        <button @click="confirmReset" class="bg-white text-red-600 px-3 py-1.5 border border-red-200 rounded shadow-sm text-xs font-semibold hover:bg-red-50 transition flex gap-1.5 items-center">
          🗑 Reset (CA-43)
        </button>

        <!-- Submit Mock -->
        <button @click="simulateMockSubmit" class="bg-indigo-600 text-white px-4 py-1.5 rounded shadow text-xs font-semibold hover:bg-indigo-700 transition flex items-center gap-2">
          🚀 Probar (Submit Mock)
        </button>
      </div>
    </header>

    <!-- ═══════ Main Layout ═══════ -->
    <main class="flex-1 flex min-h-0 relative">
      
      <!-- Toolbox Izquierda (Componentes Lego) -->
      <aside class="w-64 bg-white border-r border-gray-200 flex flex-col">
        <div class="p-3 border-b border-gray-100 bg-gray-50">
          <h3 class="text-[11px] font-bold text-gray-400 uppercase tracking-widest flex items-center gap-2">🧩 Componentes</h3>
        </div>
        
        <div class="flex-1 overflow-y-auto p-3 space-y-6">
          <div v-for="category in toolboxCategories" :key="category.name">
            <h4 class="text-xs font-bold text-gray-800 mb-3">{{ category.name }}</h4>
            <VueDraggable
              :list="category.items"
              :group="{ name: 'components', pull: 'clone', put: false }"
              :clone="cloneComponent"
              item-key="type"
              class="space-y-2"
            >
              <template #item="{ element }">
                <div class="text-xs p-2.5 border border-gray-200 rounded-md bg-white hover:border-indigo-400 hover:shadow-sm cursor-grab flex items-center gap-2 transition">
                  <span class="text-lg w-6 text-center">{{ element.icon }}</span>
                  <div class="flex flex-col">
                    <span class="font-semibold text-gray-700">{{ element.label }}</span>
                    <span class="text-[9px] text-gray-400">{{ element.desc }}</span>
                  </div>
                </div>
              </template>
            </VueDraggable>
          </div>
        </div>
      </aside>

      <!-- Lienzo Central (Canvas Drag & Drop) -->
      <section class="flex-1 bg-gray-50/50 flex flex-col relative">
        <!-- Barra de Simulación del Stage (Solo para Maestro) -->
        <div v-if="formPattern === 'IFORM_MAESTRO'" class="absolute top-4 left-1/2 -translate-x-1/2 bg-blue-50 border border-blue-200 text-blue-800 px-4 py-2 rounded-full shadow-sm text-xs font-bold flex items-center gap-3 z-10">
          <span>Simulation Stage:</span>
          <select v-model="activeStageSim" class="bg-white border-blue-300 rounded text-xs py-0.5 focus:ring-blue-500 font-mono">
            <option value="START_EVENT">START_EVENT</option>
            <option value="ANALYSIS">ANALYSIS</option>
            <option value="DECISION">DECISION</option>
            <option value="ALL">Mostrar Todos (Ideation)</option>
          </select>
        </div>

        <div class="flex-1 overflow-y-auto p-6 md:p-8 lg:p-12">
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 min-h-full p-8 max-w-4xl mx-auto flex flex-col">
            <h2 class="text-xl font-bold text-gray-800 mb-6 border-b pb-4">{{ formTitle }}</h2>
            
            <VueDraggable
              v-model="canvasFields"
              group="components"
              item-key="id"
              class="flex-1 min-h-[300px]"
              animation="200"
              ghost-class="ghost-dropzone"
            >
              <template #item="{ element, index }">
                <div 
                  v-show="formPattern !== 'IFORM_MAESTRO' || activeStageSim === 'ALL' || element.stage === activeStageSim"
                  class="group relative border border-transparent hover:border-indigo-300 hover:bg-indigo-50/30 p-4 rounded-lg mb-4 transition"
                >
                  
                  <!-- Controles del Campo (Hover) -->
                  <div class="absolute -top-3 right-2 hidden group-hover:flex bg-white border border-gray-200 shadow-sm rounded-md overflow-hidden text-xs z-20">
                    <button @click="editField(element)" class="px-2 py-1 text-gray-600 hover:bg-gray-100 border-r border-gray-200" title="Propiedades">⚙️</button>
                    <button @click="removeField(index)" class="px-2 py-1 text-red-500 hover:bg-red-50" title="Eliminar">🗑️</button>
                  </div>

                  <!-- Badge de Stage actual (Solo Maestro) -->
                  <div v-if="formPattern === 'IFORM_MAESTRO' && activeStageSim === 'ALL'" class="absolute -top-2 left-4 bg-blue-100 text-blue-800 text-[9px] font-bold px-1.5 py-0.5 rounded shadow-sm font-mono border border-blue-200">
                    v-if stage == '{{ element.stage }}'
                  </div>

                  <!-- Renderizado Dinámico Simple -->
                  <div class="flex flex-col gap-1 mt-1">
                    <label class="text-sm font-bold text-gray-700">{{ element.label }} <span v-if="element.required" class="text-red-500">*</span></label>
                    <p v-if="element.desc" class="text-[10px] text-gray-400 mb-1">{{ element.desc }}</p>
                    
                    <input v-if="element.type === 'text'" :placeholder="element.placeholder" class="form-input text-sm rounded-md border-gray-300 focus:border-indigo-500 focus:ring-indigo-500 shadow-sm disabled:bg-gray-100" />
                    <input v-if="element.type === 'number'" type="number" :placeholder="element.placeholder" class="form-input text-sm rounded-md border-gray-300 focus:border-indigo-500 focus:ring-indigo-500 shadow-sm" />
                    <select v-if="element.type === 'select'" class="form-select text-sm rounded-md border-gray-300 focus:border-indigo-500 focus:ring-indigo-500 shadow-sm">
                      <option disabled selected>{{ element.placeholder }}</option>
                      <option v-for="opt in element.options || ['Opción 1']" :key="opt">{{ opt }}</option>
                    </select>
                    <div v-if="element.type === 'file'" class="border-2 border-dashed border-gray-300 rounded p-4 text-center text-xs text-gray-500 hover:bg-gray-50 cursor-pointer">
                      📂 {{ element.placeholder }} (Drag & Drop SGDEA)
                    </div>
                  </div>

                </div>
              </template>
              
              <template #footer>
                 <div v-if="canvasFields.length === 0" class="h-full w-full flex flex-col items-center justify-center text-gray-400 border-2 border-dashed border-gray-200 rounded-lg bg-gray-50 p-12 mt-4 hover:border-indigo-300 transition">
                   <span class="text-4xl mb-4">📥</span>
                   <p class="font-medium text-gray-500">Arrastra componentes aquí</p>
                   <p class="text-xs mt-2 text-gray-400 text-center max-w-xs">El código Vue.js se generará e inyectará en tiempo real en el IDE lateral.</p>
                 </div>
              </template>
            </VueDraggable>
          </div>
        </div>
      </section>

      <!-- Monaco IDE (Solo Lectura V1) -->
      <aside class="w-2/5 min-w-[350px] bg-[#1e1e1e] border-l border-gray-800 flex flex-col shadow-[-4px_0_15px_-3px_rgba(0,0,0,0.1)] z-20">
        
        <!-- Tabs -->
        <div class="flex bg-[#252526] text-xs font-mono font-medium text-gray-400 border-b border-[#3e3e42] shrink-0">
          <button @click="activeCodeTab = 'TEMPLATE'" :class="{ 'bg-[#1e1e1e] text-white border-t-2 border-blue-500': activeCodeTab === 'TEMPLATE' }" class="px-4 py-2 hover:bg-[#2d2d2d] transition flex items-center gap-2">
            <span class="text-emerald-400">&lt;&gt;</span> template
          </button>
          <button @click="activeCodeTab = 'SCRIPT'" :class="{ 'bg-[#1e1e1e] text-white border-t-2 border-yellow-500': activeCodeTab === 'SCRIPT' }" class="px-4 py-2 hover:bg-[#2d2d2d] transition flex items-center gap-2">
            <span class="text-blue-400">{}</span> script
          </button>
          <button @click="activeCodeTab = 'ZOD'" :class="{ 'bg-[#1e1e1e] text-white border-t-2 border-indigo-500': activeCodeTab === 'ZOD' }" class="px-4 py-2 hover:bg-[#2d2d2d] transition flex items-center gap-2">
             <span class="text-indigo-400 font-bold">Z</span> zod
          </button>
          <div class="ml-auto px-4 flex items-center group relative cursor-help">
            <span class="text-gray-500 text-sm">❕</span>
            <div class="absolute right-0 top-full mt-2 w-48 p-2 bg-gray-800 text-xs text-gray-300 rounded shadow-xl hidden group-hover:block z-50">
              El IDE es Read-Only en esta versión. El código se autogenera según los componentes en el lienzo.
            </div>
          </div>
        </div>

        <!-- Monaco Editor Container -->
        <div class="flex-1 relative">
           <VueMonacoEditor 
             v-model:value="computedCode"
             :language="activeCodeTab === 'TEMPLATE' ? 'html' : 'typescript'"
             theme="vs-dark"
             :options="monacoOptions"
             class="absolute inset-0"
           />
        </div>
      </aside>

    </main>

    <!-- ═══════ Modals ═══════ -->
    <!-- Pattern Selection Modal (On Mount if Empty) -->
    <div v-if="showPatternModal" class="fixed inset-0 bg-gray-900/60 flex items-center justify-center z-[200] p-4 backdrop-blur-sm">
      <div class="bg-white rounded-xl shadow-2xl p-6 md:p-8 max-w-2xl w-full">
        <h2 class="text-2xl font-bold text-gray-900 mb-2">Crear Nuevo Formulario (Dual-Pattern)</h2>
        <p class="text-sm text-gray-600 mb-8">Selecciona la arquitectura de este formulario según la directriz (CA-2).</p>
        
        <div class="grid md:grid-cols-2 gap-6">
          <button @click="selectPattern('SIMPLE')" class="text-left border-2 border-gray-200 hover:border-green-500 hover:bg-green-50/30 rounded-xl p-6 transition group">
            <div class="text-4xl mb-4 group-hover:scale-110 transition-transform">🟢</div>
            <h3 class="text-lg font-bold text-green-700 mb-2">Formulario Simple</h3>
            <p class="text-xs text-gray-500 leading-relaxed">Formulario estándar de una sola vista. Ideal para tareas aisladas sin ciclo de vida complejo en Camunda.</p>
          </button>
          <button @click="selectPattern('IFORM_MAESTRO')" class="text-left border-2 border-gray-200 hover:border-blue-500 hover:bg-blue-50/30 rounded-xl p-6 transition group">
            <div class="text-4xl mb-4 group-hover:scale-110 transition-transform">🔵</div>
            <h3 class="text-lg font-bold text-blue-700 mb-2">iForm Maestro</h3>
            <p class="text-xs text-gray-500 leading-relaxed">Formulario universal mutante. Viajará por todo el proceso BPMN revelando u ocultando componentes dinámicamente según la variable <code class="bg-gray-100 px-1 rounded">stage</code>.</p>
          </button>
        </div>
      </div>
    </div>

    <!-- Properties Modal (Field Editor) -->
    <div v-if="editingField" class="fixed inset-0 bg-gray-900/60 flex items-center justify-center z-[150] p-4">
      <div class="bg-white rounded-lg shadow-2xl p-6 w-full max-w-md">
        <h3 class="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">⚙️ Propiedades del Componente</h3>
        
        <div class="space-y-4">
          <div>
            <label class="block text-xs font-bold text-gray-700 mb-1">ID (Variable Name)</label>
            <input v-model="editingField.id" class="w-full text-sm border-gray-300 rounded font-mono bg-gray-50 uppercase" />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-700 mb-1">Label (Nombre Visible)</label>
            <input v-model="editingField.label" class="w-full text-sm border-gray-300 rounded" />
          </div>
          <div v-if="formPattern === 'IFORM_MAESTRO'" class="bg-blue-50 p-3 rounded border border-blue-200">
             <label class="block text-xs font-bold text-blue-800 mb-1">Stage (Etapa BPMN de aparición)</label>
             <input v-model="editingField.stage" class="w-full text-sm border-blue-300 rounded font-mono" placeholder="Ej: ANALYSIS" />
          </div>
          <div class="flex items-center gap-2 pt-2 border-t mt-4">
             <input type="checkbox" v-model="editingField.required" id="reqCheck" class="text-indigo-600 rounded" />
             <label for="reqCheck" class="text-sm font-medium text-gray-700 cursor-pointer">Campo Requerido (Agrega .min(1) a Zod)</label>
          </div>
        </div>

        <div class="mt-6 flex justify-end gap-3">
          <button @click="editingField = null" class="bg-indigo-600 text-white px-4 py-2 rounded text-sm font-semibold hover:bg-indigo-700">Guardar Cambios</button>
        </div>
      </div>
    </div>

    <!-- Test Gen / Result Modal -->
    <div v-if="showResultModal" class="fixed inset-0 bg-gray-900/60 flex items-center justify-center z-[150] p-4">
        <div class="bg-gray-900 rounded-xl max-w-2xl w-full shadow-2xl border border-gray-700 flex flex-col overflow-hidden">
            <div class="px-5 py-3 bg-gray-800 border-b border-gray-700 flex justify-between items-center text-white">
               <h3 class="font-bold flex items-center gap-2 text-sm">{{ modalTitle }}</h3>
               <button @click="showResultModal = false" class="text-gray-400 hover:text-white">&times;</button>
            </div>
            <div class="p-5 overflow-y-auto font-mono text-xs text-green-400 whitespace-pre-wrap leading-relaxed max-h-[60vh]">
{{ modalContent }}
            </div>
            <div class="px-5 py-3 bg-gray-800 border-t border-gray-700 flex justify-between">
                <button v-if="modalTitle.includes('Tests')" class="text-xs text-gray-400 hover:text-white flex items-center gap-1">📋 Copiar al Portapapeles</button>
                <div v-else></div>
                <button @click="showResultModal = false" class="bg-indigo-600 text-white px-4 py-1.5 rounded hover:bg-indigo-700 text-xs font-bold font-sans">Cerrar</button>
            </div>
        </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import VueDraggable from 'vuedraggable';
import VueMonacoEditor from '@guolao/vue-monaco-editor';

// ── Types ────────────────────────────────────────────────────────
interface FormField {
  id: string;
  type: string;
  label: string;
  desc?: string;
  placeholder: string;
  required: boolean;
  stage?: string; // Solo para IFORM_MAESTRO
  zodType: string;
  options?: string[];
}

// ── State ────────────────────────────────────────────────────────
const formTitle = ref('Solicitud Onboarding (V1)');
const formPattern = ref<'SIMPLE' | 'IFORM_MAESTRO' | null>(null);
const showPatternModal = ref(true);

const canvasFields = ref<FormField[]>([]);
const activeStageSim = ref('ALL');

const activeCodeTab = ref<'TEMPLATE' | 'SCRIPT' | 'ZOD'>('TEMPLATE');
const editingField = ref<FormField | null>(null);

// ── Modals / Toasts ──────────────────────────────────────────────
const showResultModal = ref(false);
const modalTitle = ref('');
const modalContent = ref('');
const toast = ref<{ msg: string; type: 'success' | 'error' }>({ msg: '', type: 'success' });

const showToast = (msg: string, type: 'success' | 'error' = 'success') => {
  toast.value = { msg, type };
  setTimeout(() => { toast.value.msg = ''; }, 4000);
};

// ── Toolbox Categories ───────────────────────────────────────────
const toolboxCategories = [
  {
    name: "Texto",
    items: [
      { icon: 'Ab', label: 'Input Text', desc: 'Validación Regex', type: 'text', placeholder: 'Ej: Juan Pérez', required: true, zodType: 'string' },
      { icon: '📝', label: 'Long Text', desc: 'Textarea (2+ filas)', type: 'text', placeholder: 'Comentarios...', required: false, zodType: 'string' },
    ]
  },
  {
    name: "Numérico & Fechas",
    items: [
      { icon: '#', label: 'Number Field', desc: 'Zod min/max', type: 'number', placeholder: '0.00', required: true, zodType: 'number' },
      { icon: '📅', label: 'Date Picker', desc: 'Zod date (YYYY-MM-DD)', type: 'text', placeholder: 'Seleccionar Fecha', required: false, zodType: 'string' },
    ]
  },
  {
    name: "Selección",
    items: [
      { icon: '≡', label: 'Dropdown', desc: 'Typeahead / API', type: 'select', placeholder: '-- Seleccione --', required: true, zodType: 'string', options: ['Opción A', 'Opción B'] },
    ]
  },
  {
    name: "Avanzados",
    items: [
      { icon: '📎', label: 'File Upload', desc: 'SGDEA Vault Embed', type: 'file', placeholder: 'Arrastra PDF aquí', required: false, zodType: 'any' },
      { icon: '✍️', label: 'Firma Digital', desc: 'Canvas a Base64', type: 'text', placeholder: 'Firma autógrafa', required: true, zodType: 'string' },
    ]
  }
];

// ── Draggable Clone Hook ─────────────────────────────────────────
let idCounter = 1;
const cloneComponent = (original: any) => {
  const cloned = JSON.parse(JSON.stringify(original));
  cloned.id = `FIELD_${idCounter++}`;
  cloned.stage = 'START_EVENT'; // Default
  return cloned;
};

// ── Actions ──────────────────────────────────────────────────────
const selectPattern = (pattern: 'SIMPLE' | 'IFORM_MAESTRO') => {
  formPattern.value = pattern;
  showPatternModal.value = false;
  showToast(`Patrón ${pattern === 'SIMPLE' ? 'Simple' : 'iForm Maestro'} seleccionado.`);
};

const confirmReset = () => {
  if (confirm('¿Estás seguro de restablecer el formulario (CA-43)? Se perderán todos los datos actuales.')) {
    canvasFields.value = [];
    idCounter = 1;
    showPatternModal.value = true;
  }
};

const removeField = (index: number) => {
  canvasFields.value.splice(index, 1);
};

const editField = (field: FormField) => {
  editingField.value = field;
};

// ── Monaco Editor Setup ──────────────────────────────────────────
const monacoOptions = {
  readOnly: true,
  minimap: { enabled: false },
  wordWrap: 'on',
  fontSize: 13,
  lineHeight: 22,
  scrollBeyondLastLine: false,
  padding: { top: 16 }
};

// ── Generators (Auto-sync) ───────────────────────────────────────
const computedCode = computed(() => {
  if (activeCodeTab.value === 'TEMPLATE') {
    let tpl = `<template>\n  <form @submit.prevent="submitTask" class="space-y-6">`;
    if (canvasFields.value.length === 0) {
      tpl += `\n    <!-- Arrastra componentes al lienzo -->`;
    } else {
      for (const field of canvasFields.value) {
        tpl += `\n    `;
        if (formPattern.value === 'IFORM_MAESTRO') {
          tpl += `<div v-if="stage === '${field.stage}'" class="field-${field.id.toLowerCase()}">\n      `;
        } else {
          tpl += `<div class="field-${field.id.toLowerCase()}">\n      `;
        }
        tpl += `<label class="block text-sm font-medium text-gray-700">${field.label}${field.required ? '*' : ''}</label>\n      `;
        
        // Component logic (simplified)
        if (field.type === 'text' || field.type === 'number') {
          tpl += `<input type="${field.type}" v-model="formData.${field.id}" placeholder="${field.placeholder}" class="form-input mt-1 w-full rounded-md border-gray-300" />`;
        } else if (field.type === 'select') {
           tpl += `<select v-model="formData.${field.id}" class="form-select mt-1 w-full rounded-md border-gray-300">\n        <!-- Options -->\n      </select>`;
        } else {
           tpl += `<!-- Custom Component: ${field.type} -->`;
        }
        
        tpl += `\n      <span v-if="errors.${field.id}" class="text-red-500 text-xs">{{ errors.${field.id} }}</span>\n    </div>`;
      }
    }
    tpl += `\n    <button type="submit" class="w-full bg-blue-600 text-white py-2 rounded">Enviar</button>\n  </form>\n</template>`;
    return tpl;
  } 
  
  if (activeCodeTab.value === 'SCRIPT') {
    let scr = `<script setup lang="ts">\nimport { ref, inject } from 'vue';\nimport { z } from 'zod';\nimport { taskSchema } from './schema.zod.ts';\n\n`;
    if (formPattern.value === 'IFORM_MAESTRO') {
      scr += `// IFORM_MAESTRO: Inyección de Etapa BPMN actual (Dual-Pattern CA-2)\nconst stage = inject('camunda_process_stage', 'START_EVENT');\n\n`;
    }
    
    scr += `const formData = ref({\n`;
    for (const field of canvasFields.value) {
      const def = field.type === 'number' ? 'null' : "''";
      scr += `  ${field.id}: ${def},\n`;
    }
    scr += `});\n\nconst errors = ref<Record<string, string>>({});\n\nconst submitTask = async () => {\n  errors.value = {}; // Reset\n  const result = taskSchema.safeParse(formData.value);\n  if (!result.success) {\n    result.error.issues.forEach(issue => {\n      errors.value[issue.path[0]] = issue.message;\n    });\n    return;\n  }\n  \n  // Envío a Camunda\n  // await apiClient.post('/tasks/complete', { variables: result.data });\n};\n<\/script>`;
    return scr;
  }

  if (activeCodeTab.value === 'ZOD') {
    let zc = `import { z } from 'zod';\n\nexport const taskSchema = z.object({\n`;
    for (const field of canvasFields.value) {
      zc += `  ${field.id}: z.${field.zodType}()${field.required ? '.min(1, "Campo requerido")' : '.optional()'}, // [${field.stage || 'GLOBAL'}]\n`;
    }
    zc += `});\n\nexport type TaskSchemaPayload = z.infer<typeof taskSchema>;`;
    return zc;
  }

  return '';
});

// ── Modals Triggers ──────────────────────────────────────────────
const generateTests = () => {
    modalTitle.value = "🧪 Vitest/Jest Generator (CA-115)";
    modalContent.value = `import { describe, it, expect } from 'vitest';
import { taskSchema } from './schema.zod.ts';

describe('${formTitle.value} - Zod Validation', () => {
   
   it('Debería retornar Error si el payload viene vacío (simulando 400 Bad Request)', () => {
      const payload = {}; 
      const result = taskSchema.safeParse(payload);
      expect(result.success).toBe(false);
   });

${canvasFields.value.length > 0 ? `   it('Debería aprobar el envío correcto de ${canvasFields.value[0].label}', () => {
      const payload = {
${canvasFields.value.map(f => `         ${f.id}: ${f.type==='number' ? 100 : "'test_value'"},`).join('\n')}
      };
      const result = taskSchema.safeParse(payload);
      expect(result.success).toBe(true);
   });` : '   // Arrastra componentes para generar tests automáticos basados en tus inputs.'}
});`;
    showResultModal.value = true;
};

const simulateMockSubmit = () => {
    modalTitle.value = "🚀 Submit Mock Execute (CA-29)";
    if(canvasFields.value.length === 0) {
        modalContent.value = "[MOCK ENGINE] ⚠️ No hay campos obligatorios. Payload vacío permitido.";
    } else {
        modalContent.value = `[WORKDESK ENGINE] Enviando Formulario a Cola de Tareas...
POST -> /api/v1/workbox/tasks/{TAREA_ID}/complete

1. Ejecutando parse Zod Local (Vue):
=> FALLIDO (Validando variables requeridas vacías).

2. Simulando Payload forzado sin client-validation a Backend:
{ 
   "variables": {} 
}

[BACKEND HTTP 400 RESPONSE]:
{
  "instance": "com.ibpms.core.zod.ValidationException",
  "detail": "Violación de contrato iForm_Maestro",
  "issues": [
${canvasFields.value.filter(f => f.required).map(f => `    { "field": "${f.id}", "rule": "required" }`).join(',\n')}
  ]
}`;
    }
    showResultModal.value = true;
};
</script>

<style>
/* Ghost class for VueDraggable */
.ghost-dropzone {
  opacity: 0.5;
  background: #f0fdf4 !important; /* Tailwind green-50 */
  border: 2px dashed #22c55e !important; /* Tailwind green-500 */
}

/* Base custom Scrollbars for IDEs/Canvas */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 4px;
}
.dark ::-webkit-scrollbar-thumb,
aside[class*="bg-[#1e1e1e]"] ::-webkit-scrollbar-thumb {
  background-color: #4b5563;
}
</style>
