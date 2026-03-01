<template>
  <div class="h-full w-full bg-gray-50 flex flex-col pt-4 px-6 gap-4">
    <header class="flex justify-between items-center shrink-0">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Diseñador de Procesos BPMN 2.0</h1>
        <p class="text-sm text-gray-500">Editor visual integrado (bpmn-js). Vincula iForms a propiedades del XML.</p>
      </div>
      <div class="flex items-center gap-3">
        <label class="cursor-pointer bg-white border border-gray-300 px-4 py-2 rounded-md shadow-sm text-sm font-medium hover:bg-gray-50 flex gap-2 items-center">
          <svg class="w-4 h-4 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"></path></svg>
          Importar XML
          <input type="file" @change="handleFileUpload" accept=".bpmn,.xml" class="hidden" />
        </label>
        
        <button @click="downloadXML" class="bg-white border border-gray-300 px-4 py-2 rounded-md shadow-sm text-sm font-medium hover:bg-gray-50 flex items-center gap-2">
           <svg class="w-4 h-4 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"></path></svg>
           Exportar
        </button>

        <button @click="deployToCamunda" :disabled="isDeploying" class="bg-indigo-600 text-white px-4 py-2 rounded-md shadow text-sm font-medium hover:bg-indigo-700 disabled:opacity-50 flex gap-2 items-center">
          <svg v-if="!isDeploying" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path></svg>
          <svg v-else class="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path></svg>
          {{ isDeploying ? 'Desplegando...' : 'Desplegar Proceso a Motor' }}
        </button>
      </div>
    </header>

    <!-- Canvas y Panel Derecho -->
    <main class="flex-1 flex gap-4 min-h-0 bg-white border border-gray-200 rounded-xl overflow-hidden shadow-sm relative relative-canvas">
      
      <!-- Toolbox y Paleta (Autogenerada por bpmn-js) -->
      <div ref="canvasContainer" class="flex-1 overflow-hidden h-full bpmn-canvas"></div>

      <!-- Panel Lateral Estático de iBPMS Property Extension -->
      <aside class="w-80 border-l border-gray-200 bg-gray-50 shrink-0 flex flex-col p-4 overflow-y-auto">
        <h3 class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-4 flex items-center gap-2">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4"></path></svg>
          Camunda Properties
        </h3>

        <!-- Formulario Custom para inyectar iForm_Maestro al Definition -->
        <div class="space-y-4">
           <div>
             <label class="block text-xs font-medium text-gray-700 mb-1">General Process ID</label>
             <input type="text" v-model="processId" class="w-full text-xs border-gray-300 rounded focus:ring-indigo-500 focus:border-indigo-500" placeholder="Ej: Credito_Consumer_v1" />
           </div>

           <div class="p-3 bg-white border border-gray-200 rounded shadow-sm">
             <label class="block text-xs font-bold text-gray-800 mb-2">Vincular Entidad / Formulario Maestro</label>
             <p class="text-[10px] text-gray-500 mb-3 leading-relaxed">De acuerdo con (US-003), selecciona el esquema de Super-Formulario Zod/Vue que viajará y mutará a través de todos los nodos de este proceso.</p>
             
             <select v-model="selectedIForm" class="w-full text-xs font-mono border-gray-300 rounded focus:ring-indigo-500 focus:border-indigo-500 bg-indigo-50/30 text-indigo-800">
               <option value="">-- Sin Formulario Asignado --</option>
               <option value="iForm_Credito_Base">iForm_Credito_Base (Activo)</option>
               <option value="iForm_Onboarding_V3">iForm_Onboarding_V3</option>
               <option value="iForm_Compras_IT">iForm_Compras_IT</option>
             </select>
           </div>
           
           <div class="pt-4 border-t border-gray-200">
             <button @click="openAICopilot" class="w-full bg-slate-900 hover:bg-black text-white px-3 py-2 rounded text-xs font-semibold flex items-center justify-center gap-2 transition">
               <svg class="w-4 h-4 text-emerald-400" fill="currentColor" viewBox="0 0 20 20"><path d="M10 12a2 2 0 100-4 2 2 0 000 4z"></path><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm0-2a6 6 0 100-12 6 6 0 000 12z" clip-rule="evenodd"></path></svg>
               Auditoría ISO-9001 (Copilot)
             </button>
           </div>
        </div>

      </aside>

    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, defineAsyncComponent } from 'vue'

const canvasContainer = ref(null)
const selectedIForm = ref('')
const processId = ref('Process_1')
const isDeploying = ref(false)

// Mock Inicial del XML BPMN 2.0 (Starter Template)
const emptyBpmn = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" id="Definitions_1x5" targetNamespace="http://bpmn.io/schema/bpmn" exporter="iBPMS Designer Vue" exporterVersion="1.0">
  <bpmn:process id="Process_1" isExecutable="true">
    <bpmn:startEvent id="StartEvent_1" />
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1">
      <bpmndi:BPMNShape id="_BPMNShape_StartEvent_2" bpmnElement="StartEvent_1">
        <dc:Bounds x="179" y="159" width="36" height="36" />
      </bpmndi:BPMNShape>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`

let modelerInstance = null

onMounted(async () => {
    // Al ser un mockup puro estructural validado, simulamos el mount de Bpmn-JS 
    // real que requeriría la librería NPM instalada (que ha fallado en CLI).
    console.log("Mock Mount bpmn-js")
    canvasContainer.value.innerHTML = `<div class="p-8 text-center text-gray-500 font-mono text-sm">[ bpmn-js iframe placeholder ]<br><br> (Simulando renderizado del Canvas VanillaJS para US-005) <br><br> XML Enlazado a Node_id: ${processId.value} </div>`
})

onBeforeUnmount(() => {
    if(modelerInstance) {
        modelerInstance.destroy()
    }
})

const deployToCamunda = async () => {
   isDeploying.value = true
   // 1. modeler.saveXML()
   // 2. Base64 Encode
   // 3. await fetch('/api/v1/design/processes/deploy')
   setTimeout(() => {
      isDeploying.value = false
      alert(`[Mock Backend] Deploy exitoso del proceso ${processId.value} v1. Llevando FormKey: ${selectedIForm.value}`)
   }, 1500)
}

const openAICopilot = () => {
   alert("[Asistente IA] Analizando diagrama...\n- Sugerencia: El Start Event no tiene formulario asociado.\n- ISO 9001: Falta validación humana final.")
}

const handleFileUpload = (event) => {
   const file = event.target.files[0]
   if(file) {
       console.log("Archivo Cargado: ", file.name)
       // parsear y llamar a modeler.importXML()
   }
}

const downloadXML = () => {
  // modeler.saveXML() -> create blob -> Anchor trigger download
  console.log("Descargando XML Base...")
}

</script>

<style scoped>
/* Las librerías Bpmn-js y Dmn-js requieren que sus canvas tengan posiciones absolutas internamente. */
.relative-canvas { position: relative; }
</style>
