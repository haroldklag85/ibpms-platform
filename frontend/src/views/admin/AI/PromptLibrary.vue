<template>
  <div class="h-full w-full bg-gray-50 dark:bg-gray-900 flex flex-col p-6 overflow-hidden relative" v-cloak>
    
    <!-- ═══════ Toast Notifications ═══════ -->
    <Transition name="toast-slide">
      <div v-if="toast.msg" :class="toast.type === 'success' ? 'bg-green-600' : 'bg-red-600'" class="fixed top-4 right-4 z-[100] text-white px-5 py-3 rounded-lg shadow-xl flex items-center space-x-3">
        <span class="text-sm font-medium">{{ toast.msg }}</span>
        <button @click="toast.msg = ''" class="ml-2 opacity-70 hover:opacity-100">&times;</button>
      </div>
    </Transition>

    <header class="flex justify-between items-center mb-6 shrink-0">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
          🧠 Librería de Prompts Maestros (Pantalla 15)
        </h1>
        <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">Gestión centralizada de templates cognitivos. Acceso restringido a <code class="bg-gray-200 dark:bg-gray-700 px-1 rounded text-red-600 dark:text-red-400 font-bold">role: prompt_engineer</code> (CA-7).</p>
      </div>
      
      <div class="flex gap-2 relative">
         <button @click="createNewPrompt" class="bg-indigo-600 text-white px-4 py-2 rounded-md shadow text-sm font-medium hover:bg-indigo-700 transition flex items-center gap-2">
            <span>+</span> Nuevo Template
         </button>
      </div>
    </header>

    <main class="flex-1 flex gap-6 min-h-0">
      
      <!-- Panel Izquierdo: Lista de Prompts -->
      <section class="w-1/3 min-w-[300px] bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-xl overflow-hidden flex flex-col shadow-sm">
        <div class="p-4 border-b border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800/50">
           <input type="text" v-model="searchQuery" placeholder="Buscar prompt por nombre o tag..." class="w-full text-sm border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-md shadow-sm focus:border-indigo-500 focus:ring-indigo-500" />
        </div>
        
        <div class="flex-1 overflow-y-auto p-2 space-y-2">
           <div 
             v-for="prompt in filteredPrompts" 
             :key="prompt.id"
             @click="selectPrompt(prompt)"
             :class="selectedPrompt?.id === prompt.id ? 'bg-indigo-50 border-indigo-200 dark:bg-indigo-900/20 dark:border-indigo-800' : 'bg-white border-transparent hover:bg-gray-50 dark:bg-gray-800 dark:hover:bg-gray-700 dark:border-gray-700'"
             class="p-3 rounded-lg border cursor-pointer transition flex flex-col gap-2"
           >
              <div class="flex justify-between items-start">
                 <h3 class="text-sm font-bold text-gray-900 dark:text-white truncate pr-2">{{ prompt.name }}</h3>
                 <span class="text-[10px] font-mono bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-300 px-1.5 py-0.5 rounded shrink-0">v{{ prompt.version }}</span>
              </div>
              <p class="text-xs text-gray-500 dark:text-gray-400 line-clamp-2">{{ prompt.description }}</p>
              <div class="flex gap-1 mt-1">
                 <span v-for="tag in prompt.tags" :key="tag" class="text-[9px] uppercase tracking-wider bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 px-1.5 py-0.5 rounded">
                    {{ tag }}
                 </span>
              </div>
           </div>
        </div>
      </section>

      <!-- Panel Derecho: Editor / Diff (CA-23) -->
      <section v-if="selectedPrompt" class="flex-1 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-xl overflow-hidden flex flex-col shadow-sm">
        
        <!-- Header del Editor -->
        <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center bg-gray-50 dark:bg-gray-800/50">
           <div class="flex items-center gap-4">
              <h2 class="text-lg font-bold text-gray-900 dark:text-white">{{ selectedPrompt.name }}</h2>
              <select v-model="viewMode" class="text-xs border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded font-medium shadow-sm">
                 <option value="edit">📝 Modo Edición</option>
                 <option value="diff" :disabled="!isModified">⚖️ Ver Cambios (Diff)</option>
              </select>
           </div>
           <div class="flex gap-2">
              <button 
                v-if="viewMode === 'diff'" 
                @click="rollback" 
                class="text-xs px-3 py-1.5 font-bold text-red-600 bg-red-50 hover:bg-red-100 border border-red-200 rounded transition flex items-center gap-1"
               >
                 ⏪ Rollback a Producción
              </button>
              <button 
                @click="savePrompt" 
                :disabled="!isModified"
                class="text-xs px-4 py-1.5 font-bold text-white bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed rounded shadow transition"
              >
                 💾 Guardar Nueva Versión (v{{ selectedPrompt.version + 1 }})
              </button>
           </div>
        </div>

        <!-- Meta Configs -->
        <div v-show="viewMode === 'edit'" class="px-6 py-4 grid grid-cols-2 gap-4 border-b border-gray-200 dark:border-gray-700">
           <div>
             <label class="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Modelo LLM Base</label>
             <select v-model="draftPrompt.model" class="w-full text-xs border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded shadow-sm focus:ring-indigo-500">
                <option value="gpt-4-turbo">GPT-4 Turbo (Precisión Alta)</option>
                <option value="claude-3-opus">Claude 3 Opus (Razonamiento Complejo)</option>
                <option value="gemini-1.5-pro">Gemini 1.5 Pro (Contexto Masivo 1M+)</option>
             </select>
           </div>
           <div>
             <label class="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Temperatura</label>
             <div class="flex items-center gap-3">
               <input type="range" v-model.number="draftPrompt.temperature" min="0" max="1" step="0.1" class="w-full accent-indigo-600" />
               <span class="text-xs font-mono font-bold bg-gray-100 dark:bg-gray-700 px-2 py-1 rounded w-12 text-center">{{ draftPrompt.temperature }}</span>
             </div>
           </div>
        </div>

        <!-- Área Central Monaco / Diff -->
        <div class="flex-1 flex flex-col min-h-0 bg-[#1e1e1e] relative">
           
           <div class="absolute top-2 right-4 z-10 flex gap-2">
             <span class="text-[#858585] text-[10px] font-mono tracking-widest uppercase">Variables: {{ extractVariables(draftPrompt.template).join(', ') || 'Ninguna' }}</span>
           </div>

           <!-- Modo Edición Normal -->
           <vue-monaco-editor
              v-if="viewMode === 'edit'"
              v-model:value="draftPrompt.template"
              theme="vs-dark"
              language="systemprompt"
              :options="monacoOptions"
              @mount="handleEditorMount"
              class="h-full w-full"
           />

           <!-- Modo Diff Visual (CA-23) -->
           <vue-monaco-diff-editor
              v-else
              :original="selectedPrompt.template"
              :modified="draftPrompt.template"
              theme="vs-dark"
              language="systemprompt"
              :options="{ ...monacoOptions, readOnly: true, renderSideBySide: true }"
              class="h-full w-full"
           />
        </div>
      </section>
      
      <!-- Empty State -->
      <section v-else class="flex-1 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-xl flex flex-col items-center justify-center p-12 text-center shadow-sm">
         <div class="text-6xl mb-4">🔮</div>
         <h2 class="text-xl font-bold text-gray-900 dark:text-white mb-2">Workspace Cognitivo</h2>
         <p class="text-sm text-gray-500 max-w-md">Selecciona un template del panel izquierdo para versionarlo, o crea uno nuevo. Asegúrate de declarar las variables entre dobles llaves, ej: <code class="bg-gray-100 dark:bg-gray-700 text-pink-600 px-1 rounded font-mono">{{ context }}</code></p>
      </section>

    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, shallowRef } from 'vue';
import { VueMonacoEditor, VueMonacoDiffEditor } from '@guolao/vue-monaco-editor';
import * as monaco from 'monaco-editor';

interface PromptTemplate {
  id: string;
  name: string;
  description: string;
  version: number;
  tags: string[];
  model: string;
  temperature: number;
  template: string;
}

// ── Mocks ──
const prompts = ref<PromptTemplate[]>([
  {
    id: 'PRM-001',
    name: 'Análisis de Riesgo Crediticio',
    description: 'Extrae métricas financieras clave de estados de cuenta y dicta una matriz de riesgo inicial.',
    version: 4,
    tags: ['Risk', 'Extraction', 'JSON'],
    model: 'gpt-4-turbo',
    temperature: 0.2,
    template: `You are an expert underwriter. Analyze the following financial context:\n\n{{ document_context }}\n\nExtract the debt-to-income ratio, total liabilities, and output strictly as a JSON matching schema:\n{{ output_schema }}`
  },
  {
    id: 'PRM-002',
    name: 'Redactor Comercial (Email)',
    description: 'Genera correos persuasivos basados en la trazabilidad del caso y las decisiones tomadas en el Task actual.',
    version: 1,
    tags: ['Comms', 'Empathetic'],
    model: 'claude-3-opus',
    temperature: 0.7,
    template: `Write an empathetic email to the customer explaining the following decision:\n\nDecision: {{ task_decision }}\nTone: {{ tone_override }}\n\nEnsure no internal API details or technical errors are mentioned.`
  },
  {
    id: 'PRM-003',
    name: 'Clasificador de PQRS (Intake)',
    description: 'Enruta peticiones libres hacia el pool de candidatos correspondiente (Legal, Soporte, Facturación).',
    version: 12,
    tags: ['Routing', 'Zero-Shot'],
    model: 'gemini-1.5-pro',
    temperature: 0.0,
    template: `Categorize the following customer complaint into precisely one of these queues: [LEGAL, SUPPORT, BILLING, UNKNOWN].\n\nComplaint Body:\n{{ email_body }}\n\nReturn merely the exact uppercase queue name without punctuation.`
  }
]);

// ── State ──
const searchQuery = ref('');
const selectedPrompt = ref<PromptTemplate | null>(null);
const draftPrompt = ref<PromptTemplate>({} as PromptTemplate);
const viewMode = ref<'edit' | 'diff'>('edit');
const toast = ref<{ msg: string; type: 'success' | 'error' }>({ msg: '', type: 'success' });

// ── Computed ──
const filteredPrompts = computed(() => {
  const q = searchQuery.value.toLowerCase();
  return prompts.value.filter(p => 
    p.name.toLowerCase().includes(q) || 
    p.tags.some(t => t.toLowerCase().includes(q))
  );
});

const isModified = computed(() => {
  if (!selectedPrompt.value) return false;
  return draftPrompt.value.template !== selectedPrompt.value.template ||
         draftPrompt.value.temperature !== selectedPrompt.value.temperature ||
         draftPrompt.value.model !== selectedPrompt.value.model;
});

// ── Monaco Setup ──
const monacoOptions = {
  automaticLayout: true,
  minimap: { enabled: false },
  wordWrap: "on",
  fontSize: 14,
  fontFamily: "'Fira Code', 'Cascadia Code', Consolas, monospace",
  lineHeight: 24,
  scrollBeyondLastLine: false,
  folding: false,
  renderWhitespace: "selection"
};

const handleEditorMount = (editor: monaco.editor.IStandaloneCodeEditor, m: typeof monaco) => {
  // Register basic custom syntax highlighter for system prompts
  m.languages.register({ id: 'systemprompt' });
  m.languages.setMonarchTokensProvider('systemprompt', {
    tokenizer: {
      root: [
        [/\{\{[^}]+\}\}/, 'custom-variable'], // Match {{ variable }}
        [/\[[A-Z_]+\]/, 'custom-constant'],   // Match [CONSTANT]
      ]
    }
  });
  
  m.editor.defineTheme('vs-dark-prompt', {
    base: 'vs-dark',
    inherit: true,
    rules: [
      { token: 'custom-variable', foreground: '56B6C2', fontStyle: 'bold' },
      { token: 'custom-constant', foreground: 'D19A66', fontStyle: 'bold' }
    ],
    colors: {}
  });
  m.editor.setTheme('vs-dark-prompt');
};

// ── Methods ──
const showToast = (msg: string, type: 'success' | 'error' = 'success') => {
  toast.value = { msg, type };
  setTimeout(() => { toast.value.msg = ''; }, 5000);
};

const selectPrompt = (prompt: PromptTemplate) => {
  if (isModified.value && !confirm('Tienes cambios sin guardar. ¿Descartar?')) return;
  selectedPrompt.value = prompt;
  draftPrompt.value = JSON.parse(JSON.stringify(prompt)); // Deep clone
  viewMode.value = 'edit';
};

const createNewPrompt = () => {
  const newRef: PromptTemplate = {
    id: `PRM-00${prompts.value.length + 1}`,
    name: 'Nuevo Template Cognitivo',
    description: 'Descripción pendiente...',
    version: 1,
    tags: ['Draft'],
    model: 'gpt-4-turbo',
    temperature: 0.5,
    template: ' Escribe el prompt del sistema aquí...\n Usa {{ variable_name }} para inyectar contexto de Camunda.'
  };
  prompts.value.unshift(newRef);
  selectPrompt(newRef);
};

const rollback = () => {
  if (!selectedPrompt.value) return;
  draftPrompt.value = JSON.parse(JSON.stringify(selectedPrompt.value));
  viewMode.value = 'edit';
  showToast('Cambios revertidos a la versión productiva.');
};

const savePrompt = () => {
  if (!selectedPrompt.value) return;
  
  // Update mock database
  const targetIndex = prompts.value.findIndex(p => p.id === selectedPrompt.value!.id);
  if (targetIndex !== -1) {
    prompts.value[targetIndex] = {
      ...draftPrompt.value,
      version: selectedPrompt.value.version + 1
    };
  }
  
  showToast(`Versión ${prompts.value[targetIndex].version} guardada exitosamente y desplegada.`);
  selectPrompt(prompts.value[targetIndex]); // Re-select to reset dirty state
};

// Util: Extrae variables {{ nombre }}
const extractVariables = (templateStr: string): string[] => {
  if (!templateStr) return [];
  const regex = /\{\{\s*([^}]+)\s*\}\}/g;
  const matches = [...templateStr.matchAll(regex)];
  return [...new Set(matches.map(m => m[1].trim()))];
};
</script>

<style scoped>
.toast-slide-enter-active,
.toast-slide-leave-active {
  transition: all 0.3s ease;
}
.toast-slide-enter-from {
  opacity: 0;
  transform: translateX(100%);
}
.toast-slide-leave-to {
  opacity: 0;
  transform: translateX(100%) translateY(-20px);
}
</style>
