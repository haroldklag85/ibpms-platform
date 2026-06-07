<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
    <div class="px-4 py-5 sm:px-6 flex justify-between items-center bg-gray-50 border-b border-gray-200">
      <h3 class="text-lg leading-6 font-medium text-gray-900">Instancias de Proceso</h3>
      
      <div class="flex items-center space-x-2">
        <label for="status-filter" class="text-sm font-medium text-gray-700">Estado:</label>
        <select 
          id="status-filter"
          v-model="selectedStatus" 
          @change="onFilterChange"
          class="block w-40 pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-md"
        >
          <option value="">Todos</option>
          <option value="ACTIVE">Activos</option>
          <option value="COMPLETED">Completados</option>
          <option value="SUSPENDED">Suspendidos</option>
        </select>
        <button 
          @click="refresh"
          class="p-2 border border-transparent rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100 focus:outline-none"
          title="Actualizar"
        >
          <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
          </svg>
        </button>
      </div>
    </div>

    <div class="overflow-x-auto">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID Instancia</th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Definición</th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Business Key</th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Inicio</th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Estado</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-if="telemetryStore.isLoading && telemetryStore.instances.length === 0">
            <td colspan="5" class="px-6 py-4 text-center text-sm text-gray-500">
              <div class="flex justify-center items-center">
                <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-blue-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                Cargando instancias...
              </div>
            </td>
          </tr>
          <tr v-else-if="telemetryStore.instances.length === 0">
            <td colspan="5" class="px-6 py-8 text-center text-sm text-gray-500">
              No hay instancias que coincidan con los criterios.
            </td>
          </tr>
          <tr v-for="instance in telemetryStore.instances" :key="instance.id" class="hover:bg-gray-50 transition-colors">
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ formatId(instance.id) }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ formatId(instance.processDefinitionId) }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ instance.businessKey || '-' }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ formatDate(instance.startTime) }}</td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full" :class="statusClass(instance.state)">
                {{ statusText(instance.state) }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useTelemetryStore } from '@/stores/useTelemetryStore';

const telemetryStore = useTelemetryStore();
const selectedStatus = ref('');

const onFilterChange = () => {
  telemetryStore.fetchInstances(selectedStatus.value || undefined);
};

const refresh = () => {
  telemetryStore.fetchInstances(selectedStatus.value || undefined);
};

const formatDate = (isoString: string) => {
  if (!isoString) return '-';
  const d = new Date(isoString);
  return new Intl.DateTimeFormat('es-CO', {
    year: 'numeric', month: 'short', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  }).format(d);
};

const formatId = (id: string) => {
  if (!id) return '-';
  if (id.length > 20) return id.substring(0, 8) + '...' + id.substring(id.length - 4);
  return id;
};

const statusClass = (state: string) => {
  switch (state?.toUpperCase()) {
    case 'ACTIVE': return 'bg-green-100 text-green-800';
    case 'COMPLETED': return 'bg-blue-100 text-blue-800';
    case 'SUSPENDED': return 'bg-yellow-100 text-yellow-800';
    default: return 'bg-gray-100 text-gray-800';
  }
};

const statusText = (state: string) => {
  switch (state?.toUpperCase()) {
    case 'ACTIVE': return 'Activo';
    case 'COMPLETED': return 'Completado';
    case 'SUSPENDED': return 'Suspendido';
    default: return state || 'Desconocido';
  }
};

onMounted(() => {
  telemetryStore.fetchInstances();
});
</script>
