<template>
  <div class="bg-white rounded-lg shadow-sm border border-red-200 overflow-hidden">
    <div class="px-4 py-5 sm:px-6 flex justify-between items-center bg-red-50 border-b border-red-200">
      <div class="flex items-center">
        <svg class="h-5 w-5 text-red-500 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
        <h3 class="text-lg leading-6 font-medium text-red-900">Incidentes Activos ({{ telemetryStore.incidents.length }})</h3>
      </div>
      
      <button 
        @click="refresh"
        class="p-2 border border-transparent rounded-md text-red-400 hover:text-red-500 hover:bg-red-100 focus:outline-none"
        title="Actualizar"
      >
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
        </svg>
      </button>
    </div>

    <div class="overflow-x-auto">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Fecha</th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tipo</th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Mensaje</th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Proceso / Actividad</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-if="telemetryStore.isLoading && telemetryStore.incidents.length === 0">
            <td colspan="4" class="px-6 py-4 text-center text-sm text-gray-500">
              <div class="flex justify-center items-center">
                <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-red-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                Cargando incidentes...
              </div>
            </td>
          </tr>
          <tr v-else-if="telemetryStore.incidents.length === 0">
            <td colspan="4" class="px-6 py-8 text-center text-sm text-gray-500">
              <span class="inline-flex items-center text-green-600">
                <svg class="h-5 w-5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                No hay incidentes reportados en este momento.
              </span>
            </td>
          </tr>
          <tr v-for="incident in telemetryStore.incidents" :key="incident.id" class="hover:bg-red-50 transition-colors">
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ formatDate(incident.incidentTimestamp) }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
              <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-red-100 text-red-800">
                {{ incident.incidentType }}
              </span>
            </td>
            <td class="px-6 py-4 text-sm text-gray-500 max-w-xs truncate" :title="incident.incidentMessage">
              {{ incident.incidentMessage }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
              <div class="flex flex-col">
                <span class="text-xs text-gray-400">P: {{ formatId(incident.processDefinitionId) }}</span>
                <span>A: {{ incident.activityId }}</span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useTelemetryStore } from '@/stores/useTelemetryStore';

const telemetryStore = useTelemetryStore();

const refresh = () => {
  telemetryStore.fetchIncidents();
};

const formatDate = (isoString: string) => {
  if (!isoString) return '-';
  const d = new Date(isoString);
  return new Intl.DateTimeFormat('es-CO', {
    month: 'short', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit'
  }).format(d);
};

const formatId = (id: string) => {
  if (!id) return '-';
  if (id.length > 20) return id.substring(0, 8) + '...' + id.substring(id.length - 4);
  return id;
};

onMounted(() => {
  telemetryStore.fetchIncidents();
});
</script>
