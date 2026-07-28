<template>
  <div class="bg-white rounded-lg border border-gray-200">
    <!-- Header Action Bar -->
    <div class="px-5 py-4 border-b border-gray-100 flex items-center justify-between">
      <div class="flex items-center gap-3">
        <span class="material-symbols-outlined text-red-600 border border-red-200 bg-red-50 p-1.5 rounded-md">
          security
        </span>
        <div>
          <h2 class="text-base font-bold text-gray-900 border-l-2 border-red-500 pl-2">Tablero CISO: Anomalías de Seguridad</h2>
          <p class="text-xs text-gray-500 mt-0.5">Control de Auditoría y Trazabilidad de Resiliencia SRE</p>
        </div>
      </div>
      <div>
        <button 
          @click="loadAnomalies" 
          class="flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-gray-600 bg-gray-50 hover:bg-gray-100 border border-gray-200 rounded-md transition-colors"
        >
          <span class="material-symbols-outlined text-[16px]" :class="{ 'animate-spin': store.isLoading }">refresh</span>
          Actualizar CISO Board
        </button>
      </div>
    </div>

    <!-- Data Grid -->
    <div class="overflow-x-auto min-h-[300px]">
      <table class="w-full text-sm text-left">
        <thead class="text-xs text-gray-500 bg-gray-50 border-b border-gray-100 uppercase font-semibold">
          <tr>
            <th class="px-5 py-3 rounded-tl-lg">Incidente</th>
            <th class="px-5 py-3">Contexto</th>
            <th class="px-5 py-3">Trace/Correlation ID</th>
            <th class="px-5 py-3">Severidad</th>
            <th class="px-5 py-3 text-center">Estado</th>
            <th class="px-5 py-3 text-right rounded-tr-lg">Acción Forense</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-if="store.isLoading && !store.anomalies.length">
            <td colspan="6" class="px-5 py-12 text-center text-gray-400">
              <span class="material-symbols-outlined animate-spin text-3xl mb-2">sync</span>
              <p class="text-sm">Analizando trazabilidad...</p>
            </td>
          </tr>
          <tr v-else-if="filteredAnomalies.length === 0">
            <td colspan="6" class="px-5 py-12 text-center text-gray-400">
              <span class="material-symbols-outlined text-4xl mb-2 text-emerald-500 border border-emerald-200 bg-emerald-50 p-2 rounded-full">shield_lock</span>
              <p class="text-sm font-semibold text-emerald-700">Perímetro Seguro</p>
              <p class="text-xs mt-1">No se detectan anomalías de seguridad ni bloqueos en la infraestructura.</p>
            </td>
          </tr>
          <tr 
            v-for="anomaly in filteredAnomalies" 
            :key="anomaly.id" 
            class="hover:bg-slate-50 transition-colors cursor-pointer group"
          >
            <td class="px-5 py-3">
              <div class="flex items-center gap-2">
                <span class="w-2 h-2 rounded-full" :class="getSeverityDotClass(anomaly.severity)"></span>
                <span class="font-bold text-gray-800">{{ anomaly.anomalyType }}</span>
              </div>
              <p class="text-[10px] text-gray-400 mt-0.5 ml-4">{{ new Date(anomaly.detectedAt).toLocaleString() }}</p>
            </td>
            <td class="px-5 py-3">
              <div class="text-xs bg-slate-100 p-1.5 rounded text-gray-700 font-mono break-all max-w-xs border border-slate-200">
                {{ anomaly.contextSnippet }}
              </div>
            </td>
            <td class="px-5 py-3">
              <div class="text-[10px] font-mono select-all text-indigo-600 bg-indigo-50 border border-indigo-100 px-1.5 py-0.5 rounded cursor-copy" title="Copiar Correlation ID" @click="copyToClipboard(anomaly.traceCorrelationId)">
                {{ anomaly.traceCorrelationId || 'N/A' }}
              </div>
            </td>
            <td class="px-5 py-3">
              <span class="px-2 py-1 rounded text-[10px] font-bold uppercase tracking-wider border" :class="getSeverityPillClass(anomaly.severity)">
                {{ anomaly.severity }}
              </span>
            </td>
            <td class="px-5 py-3 text-center">
              <span class="px-2 py-1 rounded text-[10px] font-bold uppercase tracking-wider" :class="getStatusPillClass(anomaly.investigationStatus)">
                {{ anomaly.investigationStatus }}
              </span>
              <div v-if="anomaly.resolvedAt" class="text-[9px] text-gray-400 mt-1">
                Por {{ anomaly.resolvedBy }}
              </div>
            </td>
            <td class="px-5 py-3 text-right">
              <div class="flex items-center justify-end gap-2" v-if="anomaly.investigationStatus === 'PENDIENTE'">
                <button 
                  @click="openResolveModal(anomaly)"
                  class="px-2.5 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded text-[10px] font-bold shadow-sm transition-colors uppercase tracking-wider"
                >
                  Resolver
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <!-- Modal para Resolver -->
    <Transition name="fade">
      <div v-if="selectedAnomaly" class="fixed inset-0 z-[100] flex items-center justify-center bg-gray-900/60 backdrop-blur-sm">
        <div class="bg-white rounded-xl shadow-2xl w-full max-w-md p-6">
          <div class="flex items-center gap-3 mb-4">
             <span class="material-symbols-outlined text-amber-500 text-2xl">policy</span>
             <h3 class="text-lg font-bold text-gray-900">Resolución Forense</h3>
          </div>
          <p class="text-xs text-gray-600 mb-6 border-l-2 border-amber-300 pl-2">
            La resolución de una anomalía altera el Audit Log de Nivel 0 de forma inmutable. Seleccione el veredicto SRE.
          </p>
          
          <div class="space-y-4 mb-6">
            <label class="block">
              <input type="radio" v-model="resolutionValue" value="MITIGADA" class="mr-2 accent-indigo-600" />
              <span class="text-sm font-semibold text-gray-800">Causa Mitigada (SRE)</span>
            </label>
            <label class="block">
              <input type="radio" v-model="resolutionValue" value="FALSO_POSITIVO" class="mr-2 accent-indigo-600" />
              <span class="text-sm font-semibold text-gray-800">Falso Positivo (Ignorar)</span>
            </label>
            <label class="block">
              <input type="radio" v-model="resolutionValue" value="INVESTIGADA_ESCALADA" class="mr-2 accent-indigo-600" />
              <span class="text-sm font-semibold text-gray-800">Escalada a Nivel 3 (CSIRT)</span>
            </label>
          </div>

          <div class="flex justify-end gap-3 pt-4 border-t border-gray-100">
            <button @click="selectedAnomaly = null" class="px-4 py-2 text-sm font-bold text-gray-600 hover:bg-gray-100 rounded-md transition-colors">
              Cancelar
            </button>
            <button @click="confirmResolve" :disabled="isResolving || !resolutionValue" class="px-4 py-2 text-sm font-bold text-white bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 rounded-md shadow-sm transition-colors flex items-center gap-2">
              <span v-if="isResolving" class="material-symbols-outlined animate-spin text-[16px]">refresh</span>
              Sellar Resolución
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRbacStore } from '@/stores/rbacStore'

const store = useRbacStore()
const isResolving = ref(false)
const selectedAnomaly = ref(null)
const resolutionValue = ref('')

const loadAnomalies = async () => {
    await store.fetchAnomalies()
}

onMounted(() => {
    loadAnomalies()
})

const filteredAnomalies = computed(() => {
    return store.anomalies.slice().sort((a, b) => new Date(b.detectedAt) - new Date(a.detectedAt))
})

const openResolveModal = (anomaly) => {
    selectedAnomaly.value = anomaly
    resolutionValue.value = 'MITIGADA'
}

const confirmResolve = async () => {
    if (!selectedAnomaly.value || !resolutionValue.value) return
    isResolving.value = true
    try {
        await store.resolveAnomaly(selectedAnomaly.value.id, resolutionValue.value)
        selectedAnomaly.value = null
    } catch (err) {
        alert("Ocurrió un error al resolver la anomalía. Consulte la consola.")
    } finally {
        isResolving.value = false
    }
}

const copyToClipboard = (text) => {
    if(text) navigator.clipboard.writeText(text)
}

const getSeverityDotClass = (sev) => {
    if (sev === 'CRITICA') return 'bg-red-500'
    if (sev === 'ALTA') return 'bg-orange-500'
    if (sev === 'MEDIA') return 'bg-amber-400'
    return 'bg-blue-400'
}

const getSeverityPillClass = (sev) => {
    if (sev === 'CRITICA') return 'bg-red-50 text-red-700 border-red-200'
    if (sev === 'ALTA') return 'bg-orange-50 text-orange-700 border-orange-200'
    if (sev === 'MEDIA') return 'bg-amber-50 text-amber-700 border-amber-200'
    return 'bg-blue-50 text-blue-700 border-blue-200'
}

const getStatusPillClass = (status) => {
    if (status === 'PENDIENTE') return 'bg-rose-50 text-rose-700 border border-rose-200'
    if (status === 'FALSO_POSITIVO') return 'bg-slate-100 text-slate-500 border border-slate-200 line-through'
    return 'bg-emerald-50 text-emerald-700 border border-emerald-200'
}
</script>
