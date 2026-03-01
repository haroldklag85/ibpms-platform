<template>
  <div class="h-full w-full bg-gray-50 flex flex-col pt-4 px-6 gap-4">
    <header class="flex justify-between items-center shrink-0">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Taller de Reglas de Negocio (DMN) Asistido por IA</h1>
        <p class="text-sm text-gray-500">Diseñador y Autogenerador NLP de Reglas DMN (US-007).</p>
      </div>
      <div class="flex items-center gap-3">
         <button @click="deployDMN" :disabled="isDeploying" class="bg-indigo-600 text-white px-4 py-2 rounded-md shadow text-sm font-medium hover:bg-indigo-700 disabled:opacity-50 flex gap-2 items-center">
            <svg v-if="!isDeploying" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path></svg>
            <svg v-else class="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path></svg>
            Desplegar Tabla de Reglas
         </button>
      </div>
    </header>

    <main class="flex-1 flex gap-4 min-h-0 relative">

      <!-- Panel IA Asistente NLP -->
      <aside class="w-80 border border-indigo-100 bg-indigo-50/30 rounded-xl shadow-sm flex flex-col p-4 overflow-y-auto shrink-0">
          <div class="flex items-center gap-2 text-indigo-800 font-bold mb-4">
              <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM9.555 7.168A1 1 0 008 8v4a1 1 0 001.555.832l3-2a1 1 0 000-1.664l-3-2z" clip-rule="evenodd"></path></svg>
              AI DMN Copilot (NLP)
          </div>
          
          <p class="text-xs text-gray-600 mb-4 leading-relaxed">
             Describe tu regla de negocio en lenguaje natural. El agente IA (`ibpms-daas`) construirá la matriz por ti.
          </p>
          
          <textarea v-model="prompt" rows="5" placeholder="Ej: Si el nivel_riesgo es ALTO y el score es menor a 500, entonces accion='Rechazar'. De lo contrario, 'Aprobar'." class="w-full text-sm border-gray-300 rounded shadow-inner p-2 focus:ring-indigo-500 focus:border-indigo-500 mb-4 bg-white"></textarea>
          
          <button @click="generateRule" :disabled="isGenerating || !prompt" class="w-full bg-slate-900 hover:bg-black text-white px-3 py-2 rounded text-sm font-semibold flex items-center justify-center gap-2 mb-6 disabled:opacity-50 transition">
             <svg v-if="isGenerating" class="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path></svg>
             <span v-else>✨ Traducir a DMN</span>
          </button>

          <div v-if="lastAction" class="p-3 bg-white border border-emerald-200 rounded text-xs text-emerald-800 break-words font-mono">
              {{ lastAction }}
          </div>
      </aside>

      <!-- Lienzo Vue Central para DMN-JS -->
      <section class="flex-1 bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden flex flex-col relative-canvas">
          <div v-if="!hasData" class="flex-1 flex flex-col items-center justify-center text-gray-400 p-8 text-center bg-gray-50/50">
             <svg class="w-16 h-16 mb-4 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"></path></svg>
             <p class="font-medium">El lienzo DMN está vacío.</p>
             <p class="text-xs mt-2">Usa el asistente de la izquierda o importa un archivo XML.</p>
          </div>
          
          <!-- Mockup Render de la Tabla DMN Generada -->
          <div v-else class="flex-1 p-6 overflow-auto">
              <h3 class="text-sm font-bold text-gray-700 mb-4 uppercase tracking-wider">Decision Table: Evaluación de Riesgo</h3>
              <div class="overflow-x-auto border border-gray-300 rounded">
                <table class="min-w-full divide-y divide-gray-300 text-sm">
                  <thead class="bg-gray-100">
                    <tr>
                      <th scope="col" class="py-2.5 pl-4 pr-3 text-left font-semibold text-gray-900 border-r border-gray-300 bg-blue-50/50">Input 1 <br><span class="text-xs text-indigo-600 font-mono">nivel_riesgo</span></th>
                      <th scope="col" class="py-2.5 pl-4 pr-3 text-left font-semibold text-gray-900 border-r border-gray-300 bg-blue-50/50">Input 2 <br><span class="text-xs text-indigo-600 font-mono">score</span></th>
                      <th scope="col" class="py-2.5 pl-4 pr-3 text-left font-semibold text-gray-900 bg-emerald-50/50">Output 1 <br><span class="text-xs text-emerald-600 font-mono">accion</span></th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-gray-200 bg-white">
                    <tr>
                      <td class="whitespace-nowrap px-3 py-3 text-gray-500 font-mono border-r">"ALTO"</td>
                      <td class="whitespace-nowrap px-3 py-3 text-gray-500 font-mono border-r">&lt; 500</td>
                      <td class="whitespace-nowrap px-3 py-3 text-gray-900 font-bold bg-red-50">"Rechazar"</td>
                    </tr>
                    <tr>
                      <td class="whitespace-nowrap px-3 py-3 text-gray-500 font-mono border-r">-</td>
                      <td class="whitespace-nowrap px-3 py-3 text-gray-500 font-mono border-r">&gt;= 500</td>
                      <td class="whitespace-nowrap px-3 py-3 text-gray-900 font-bold bg-green-50">"Aprobar"</td>
                    </tr>
                  </tbody>
                </table>
              </div>
          </div>
      </section>

    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const prompt = ref('')
const isGenerating = ref(false)
const hasData = ref(false)
const lastAction = ref('')
const isDeploying = ref(false)

const generateRule = () => {
    isGenerating.value = true
    setTimeout(() => {
        isGenerating.value = false
        hasData.value = true
        lastAction.value = `[POST /api/v1/ai/dmn/translate] Generación Exitosa. DMN-JS Container Updated.`
    }, 1200)
}

const deployDMN = () => {
   isDeploying.value = true
   setTimeout(() => {
      isDeploying.value = false
      alert("[Mock Backend] Tabla DMN desplegada correctamente en DaaS para su motor de evaluación.")
   }, 1000)
}
</script>

<style scoped>
.relative-canvas { position: relative; }
</style>
