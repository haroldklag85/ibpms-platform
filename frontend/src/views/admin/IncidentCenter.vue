<template>
  <div class="h-full w-full bg-gray-50 flex flex-col p-6 overflow-hidden">
    
    <header class="mb-6 flex items-center justify-between shrink-0">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 flex items-center gap-2">
          🚨 Centro de Incidentes (Morgue de Tokens)
        </h1>
        <p class="text-sm text-gray-500 mt-1">
          Nivel 3 DRP: Gestión reactiva de tokens trabados por fallos técnicos o sistémicos (CA-13).
        </p>
      </div>
      <div>
        <button @click="fetchIncidents" class="bg-white border border-gray-300 text-gray-700 font-medium px-4 py-2 rounded shadow hover:bg-gray-50 transition flex items-center gap-2 text-sm">
          <span>↻</span> Refrescar Tablero
        </button>
      </div>
    </header>

    <div class="flex-1 overflow-y-auto bg-white rounded-xl shadow-sm border border-gray-200">
      <div v-if="loading" class="flex flex-col items-center justify-center h-64 text-red-500">
        <svg class="animate-spin w-8 h-8 mb-4" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path></svg>
        <span class="font-bold">Escaneando Morgue de Tokens...</span>
      </div>

      <table v-else class="w-full text-left border-collapse min-w-max">
        <thead class="bg-red-50/50 sticky top-0 z-10 shadow-sm border-b border-red-100">
          <tr>
            <th class="py-3 px-4 font-bold text-xs text-red-800 uppercase tracking-widest">ID Instancia</th>
            <th class="py-3 px-4 font-bold text-xs text-red-800 uppercase tracking-widest">Proceso</th>
            <th class="py-3 px-4 font-bold text-xs text-red-800 uppercase tracking-widest">Actividad Fallida</th>
            <th class="py-3 px-4 font-bold text-xs text-red-800 uppercase tracking-widest">Diagnóstico (Error)</th>
            <th class="py-3 px-4 font-bold text-xs text-red-800 uppercase tracking-widest text-right">Acciones (Nivel 3)</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="inc in incidents" :key="inc.id" class="border-b border-gray-100 hover:bg-red-50/30 transition group">
            <td class="py-4 px-4 font-mono text-xs text-gray-700">#{{ inc.id.split('-')[0] }}...</td>
            <td class="py-4 px-4 text-sm font-semibold text-gray-900">{{ inc.processName }} <span class="text-xs text-gray-400 font-normal ml-1">v{{ inc.version }}</span></td>
            <td class="py-4 px-4 text-sm">
              <span class="bg-gray-100 text-gray-800 px-2 py-1 rounded text-xs border border-gray-200">{{ inc.failedActivity }}</span>
            </td>
            <td class="py-4 px-4 text-sm text-red-600 font-mono text-[10px] max-w-xs truncate" :title="inc.errorMessage">
              {{ inc.errorMessage }}
            </td>
            <td class="py-4 px-4 text-right space-x-2">
              <button @click="retryIncident(inc.id)" :disabled="processingId === inc.id" class="bg-blue-50 text-blue-700 border border-blue-200 px-3 py-1.5 rounded text-xs font-bold hover:bg-blue-100 transition disabled:opacity-50">
                🔄 Retry (Electrochoque)
              </button>
              <button @click="abortIncident(inc.id)" :disabled="processingId === inc.id" class="bg-red-50 text-red-700 border border-red-200 px-3 py-1.5 rounded text-xs font-bold hover:bg-red-100 transition disabled:opacity-50">
                💀 Abortar Caso
              </button>
            </td>
          </tr>
          <tr v-if="incidents.length === 0">
            <td colspan="5" class="py-12 text-center">
              <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-green-50 text-green-500 mb-4">
                <span class="text-2xl">✨</span>
              </div>
              <h3 class="text-lg font-bold text-gray-900">Morgue Vacía</h3>
              <p class="text-gray-500 text-sm mt-1">El ecosistema está sano. No hay tokens atascados.</p>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { api } from '@/services/apiClient';

interface Incident {
  id: string;
  processName: string;
  version: number;
  failedActivity: string;
  errorMessage: string;
}

const loading = ref(true);
const incidents = ref<Incident[]>([]);
const processingId = ref<string | null>(null);

const fetchIncidents = async () => {
  loading.value = true;
  try {
    const { data } = await api.getIncidents();
    incidents.value = data || [];
  } catch (err) {
    console.error('Failed to fetch incidents', err);
    // Mock local DRP data si no hay Backend
    incidents.value = [
      { id: 'inc-99a1b-123', processName: 'Crédito de Consumo', version: 2, failedActivity: 'ServiceTask_SendEmail', errorMessage: 'java.net.ConnectException: Connection refused' },
      { id: 'inc-88c2d-456', processName: 'Onboarding Proveedores', version: 1, failedActivity: 'CallActivity_Compliance', errorMessage: 'org.camunda.bpm.engine.ScriptEvaluationException: Unable to evaluate script' }
    ];
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchIncidents();
});

const retryIncident = async (id: string) => {
  if (!confirm(`¿Estás seguro de inyectar Electrochoque (Retry Job) al token ${id}?`)) return;
  processingId.value = id;
  try {
    await api.retryIncident(id);
    alert(`⚡ Retry ejecutado exitosamente en ${id}`);
    await fetchIncidents();
  } catch (err) {
    alert(`Error al ejecutar Retry: El engine no pudo superar la falla nativa.`);
  } finally {
    processingId.value = null;
  }
};

const abortIncident = async (id: string) => {
  if (!confirm(`🛑 PELIGRO: ¿Estás completamente seguro de Abortar (Matar) el token ${id}? Esta acción destruirá la instancia BPMN para siempre.`)) return;
  processingId.value = id;
  try {
    await api.abortIncident(id);
    alert(`💀 Instancia ${id} abortada y purgada exitosamente.`);
    await fetchIncidents();
  } catch (err) {
    alert(`Error al abortar la instancia.`);
  } finally {
    processingId.value = null;
  }
};
</script>
