<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <div class="md:flex md:items-center md:justify-between mb-8">
      <div class="flex-1 min-w-0">
        <h2 class="text-2xl font-bold leading-7 text-gray-900 sm:text-3xl sm:truncate flex items-center">
          <svg class="h-8 w-8 text-blue-600 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
          </svg>
          Telemetría y Monitoreo BPMN
        </h2>
        <p class="mt-1 text-sm text-gray-500">
          Supervisión de la actividad del motor (Business Activity Monitoring) e incidentes técnicos.
        </p>
      </div>
      <div class="mt-4 flex md:mt-0 md:ml-4">
        <button
          @click="refreshAll"
          type="button"
          class="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors"
        >
          Actualizar Todo
        </button>
      </div>
    </div>

    <!-- Error Banner -->
    <div v-if="telemetryStore.error" class="rounded-md bg-red-50 p-4 mb-8">
      <div class="flex">
        <div class="flex-shrink-0">
          <svg class="h-5 w-5 text-red-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
          </svg>
        </div>
        <div class="ml-3">
          <h3 class="text-sm font-medium text-red-800">Error de conexión</h3>
          <div class="mt-2 text-sm text-red-700">
            <p>{{ telemetryStore.error }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Secciones Principales -->
    <div class="space-y-8">
      <!-- Panel de Incidentes (Prioridad Alta) -->
      <IncidentPanel />

      <!-- Tabla de Instancias -->
      <InstanceTable />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useTelemetryStore } from '@/stores/useTelemetryStore';
import InstanceTable from '@/components/monitoring/InstanceTable.vue';
import IncidentPanel from '@/components/monitoring/IncidentPanel.vue';

const telemetryStore = useTelemetryStore();

const refreshAll = () => {
  telemetryStore.fetchInstances();
  telemetryStore.fetchIncidents();
};
</script>
