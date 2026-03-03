<template>
  <div class="h-full w-full bg-gray-50 flex flex-col" v-cloak>
    
    <!-- ═══════ Header ═══════ -->
    <header class="flex justify-between items-center px-6 py-3 bg-white border-b border-gray-200 shrink-0">
      <div class="flex items-center gap-4">
        <router-link to="/admin/integration/builder" class="text-gray-400 hover:text-indigo-600 transition">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path></svg>
        </router-link>
        <div>
          <h1 class="text-xl font-bold text-gray-900 flex items-center gap-2">
            Visual Data Mapper (CA-5)
          </h1>
          <p class="text-xs text-gray-500 mt-0.5">Mapea visualmente la respuesta JSON del Endpoint hacia las Variables Nativas de Camunda (iForm_Maestro).</p>
        </div>
      </div>
      <div class="flex gap-2">
        <button @click="autoMapByName" class="bg-teal-50 text-teal-700 px-3 py-1.5 border border-teal-200 rounded shadow-sm text-sm font-medium hover:bg-teal-100 transition flex items-center gap-1">
          ✨ Auto-Map (Match Nombres)
        </button>
        <button @click="saveMapping" class="bg-indigo-600 text-white px-4 py-1.5 rounded shadow text-sm font-medium hover:bg-indigo-700 transition">
          Confirmar Mapeo
        </button>
      </div>
    </header>

    <main class="flex-1 overflow-hidden p-6">
      <div class="h-full flex flex-col lg:flex-row gap-8">
        
        <!-- ========================= LEFT: Raw Response ========================= -->
        <section class="flex-1 bg-white rounded-xl shadow-sm border border-gray-200 flex flex-col overflow-hidden">
          <div class="px-5 py-3 bg-slate-900 border-b border-gray-200 flex justify-between items-center">
             <h2 class="text-xs font-bold text-white tracking-widest uppercase flex items-center gap-2">
               📥 Raw JSON Response
             </h2>
             <span class="text-[10px] text-gray-400">Origen: APIM / Sistema Legado</span>
          </div>
          
          <div class="p-4 bg-gray-50 flex-1 overflow-y-auto">
             <p class="text-[11px] text-gray-500 mb-4">Arrastra (Drag) los nodos JSON hacia la derecha (iForm Variables) para conectarlos.</p>
             
             <VueDraggable
               :list="rawJsonNodes"
               :group="{ name: 'mapping', pull: 'clone', put: false }"
               :clone="cloneNode"
               item-key="path"
               class="space-y-2"
             >
               <template #item="{ element }">
                 <div class="bg-white border border-gray-200 p-2.5 rounded shadow-sm hover:border-indigo-400 hover:shadow cursor-grab active:cursor-grabbing flex items-center justify-between group transition">
                    <div class="flex items-center gap-3">
                       <span class="text-gray-400">☷</span>
                       <span class="font-mono text-sm font-bold text-indigo-900">{{ element.path }}</span>
                    </div>
                    <span class="text-[10px] px-2 py-0.5 bg-gray-100 text-gray-500 rounded font-mono">{{ element.type }}</span>
                 </div>
               </template>
             </VueDraggable>
          </div>
        </section>

        <!-- ========================= CENTER: Visual Arrows (Decorative) ========================= -->
        <div class="hidden lg:flex flex-col items-center justify-center w-12 text-gray-300">
           <svg class="w-8 h-8 opacity-50 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"></path></svg>
           <svg class="w-8 h-8 opacity-50 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"></path></svg>
           <svg class="w-8 h-8 opacity-50 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"></path></svg>
           <span class="text-xs font-bold uppercase tracking-[0.2em] transform -rotate-90 mt-8 whitespace-nowrap">Mapping Engine</span>
        </div>

        <!-- ========================= RIGHT: Camunda Variables ========================= -->
        <section class="flex-1 bg-white rounded-xl shadow-sm border border-gray-200 flex flex-col overflow-hidden">
          <div class="px-5 py-3 bg-blue-50 border-b border-blue-200 flex justify-between items-center">
             <h2 class="text-xs font-bold text-blue-900 tracking-widest uppercase flex items-center gap-2">
               ⚙️ Camunda / iForm Variables
             </h2>
             <span class="text-[10px] bg-blue-100 text-blue-700 px-2 py-0.5 rounded font-mono border border-blue-200">Destino: Process Context</span>
          </div>

          <div class="p-4 flex-1 overflow-y-auto space-y-4 bg-white">
             
             <div v-for="camVar in camundaVariables" :key="camVar.id" class="border border-gray-200 rounded-lg p-3 bg-gray-50">
                <div class="flex justify-between items-center mb-2">
                   <div class="flex flex-col">
                      <span class="font-bold text-gray-800 text-sm">{{ camVar.label }}</span>
                      <span class="font-mono text-[10px] text-gray-500">{{ camVar.name }} ({{ camVar.type }})</span>
                   </div>
                   <div v-if="camVar.mappedNode && camVar.type === 'date'" class="flex items-center gap-1 text-[10px] bg-yellow-100 text-yellow-800 px-2 py-1 rounded border border-yellow-200 font-bold" title="Date Formatter Requerido (CA-13)">
                     ⚠️ Require Formatter
                   </div>
                </div>

                <!-- DROPZONE (VueDraggable) -->
                <VueDraggable
                  :list="camVar.mappedNode ? [camVar.mappedNode] : []"
                  :group="{ name: 'mapping', put: true }"
                  @change="(evt: any) => handleDrop(camVar.id, evt)"
                  item-key="path"
                  class="min-h-[48px] border-2 border-dashed rounded-md bg-white flex items-center justify-center transition-colors"
                  :class="camVar.mappedNode ? 'border-indigo-400 bg-indigo-50/20' : 'border-gray-300 hover:border-indigo-300 hover:bg-gray-50'"
                >
                  <template #item="{ element }">
                    <div class="w-full h-full p-2 flex items-center justify-between group">
                       <span class="font-mono text-xs font-bold text-indigo-700 px-2 py-1 bg-indigo-100 rounded break-all border border-indigo-200">
                         {{ element.path }}
                       </span>
                       <button @click.stop="clearMapping(camVar.id)" class="text-gray-400 hover:text-red-500 font-bold px-2 rounded hidden group-hover:block transition">
                         &times;
                       </button>
                    </div>
                  </template>
                  <template #footer>
                     <p v-if="!camVar.mappedNode" class="text-xs text-gray-400 pointer-events-none">
                       Suelta el nodo JSON aquí...
                     </p>
                  </template>
                </VueDraggable>
             </div>

          </div>
        </section>

      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import VueDraggable from 'vuedraggable';

// Node shape from JSON
interface JsonNode {
  path: string;
  type: string;
}

// Camunda destination shape
interface CamundaVariable {
  id: string;
  name: string;
  label: string;
  type: 'string' | 'number' | 'boolean' | 'date';
  mappedNode: JsonNode | null;
}

// Dummy tree data extracted from previous ConnectorBuilder postman stage.
const rawJsonNodes = ref<JsonNode[]>([
  { path: 'transactionId', type: 'string' },
  { path: 'status', type: 'string' },
  { path: 'data.approved', type: 'boolean' },
  { path: 'data.amount', type: 'string' },
  { path: 'data.settlementDate', type: 'string' },
  { path: 'timestamp', type: 'number' }
]);

// Dummy Camunda variables extracted from selected iForm_Maestro
const camundaVariables = ref<CamundaVariable[]>([
  { id: 'v1', name: 'aprobacionCredit', label: '¿Crédito Aprobado?', type: 'boolean', mappedNode: null },
  { id: 'v2', name: 'montoOtorgado', label: 'Monto Total Desembolsado', type: 'number', mappedNode: null },
  { id: 'v3', name: 'referenciaApim', label: 'Trx Request ID', type: 'string', mappedNode: null },
  { id: 'v4', name: 'fechaLiquidacion', label: 'Fecha Liquidada (Date Picker)', type: 'date', mappedNode: null }
]);

// Handlers
const cloneNode = (element: JsonNode) => {
  return { ...element };
};

const handleDrop = (camVarId: string, evt: any) => {
  if (evt.added) {
    const v = camundaVariables.value.find(c => c.id === camVarId);
    if (v) {
      v.mappedNode = evt.added.element;
    }
  }
};

const clearMapping = (camVarId: string) => {
  const v = camundaVariables.value.find(c => c.id === camVarId);
  if (v) v.mappedNode = null;
};

const autoMapByName = () => {
    // Basic AI/Heuristic auto-mapping for demo
    const v1 = camundaVariables.value.find(c => c.name === 'aprobacionCredit');
    if (v1 && !v1.mappedNode) v1.mappedNode = { path: 'data.approved', type: 'boolean' };
    
    const v2 = camundaVariables.value.find(c => c.name === 'montoOtorgado');
    if (v2 && !v2.mappedNode) v2.mappedNode = { path: 'data.amount', type: 'string' };

    const v3 = camundaVariables.value.find(c => c.name === 'referenciaApim');
    if (v3 && !v3.mappedNode) v3.mappedNode = { path: 'transactionId', type: 'string' };
};

const saveMapping = () => {
  const payload = camundaVariables.value
    .filter(c => c.mappedNode)
    .map(c => ({
       target: c.name,
       source: c.mappedNode!.path
    }));
  
  console.log("Saving Mapping Structure:", payload);
  alert("Mapeo guardado exitosamente. Ahora el APIM inyectará estas variables en Camunda.");
};
</script>

<style scoped>
/* Scrollbar adjustments */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 4px;
}
</style>
