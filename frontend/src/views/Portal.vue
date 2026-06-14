<!-- @Traceability: US-005 - ADR-001 -->
<template>
  <div class="max-w-7xl mx-auto">
    <!-- Procesos Frecuentes Quick Links -->
    <div v-if="frequentProcesses.length > 0" class="mb-6 flex items-center space-x-4 bg-white p-3 rounded-lg border shadow-sm">
      <span class="font-bold text-gray-700">⭐ Mis Procesos Frecuentes:</span>
      <button v-for="fp in frequentProcesses" :key="fp.id" class="px-3 py-1.5 bg-gray-50 border rounded shadow-sm hover:bg-gray-100 text-sm font-medium text-gray-700 transition">
        {{ fp.icon || '📌' }} {{ fp.name }}
      </button>
    </div>
    
    <div class="bg-blue-50 border-l-4 border-ibpms-brand p-5 mb-8 rounded-r-lg shadow-sm">
      <div class="flex items-center">
        <span class="text-3xl mr-4">👋</span>
        <div>
          <p class="text-lg text-blue-900 font-bold">
            Buenos días, @{{ authStore.user?.username || 'Usuario' }}. ¿Qué necesitas iniciar hoy?
          </p>
          <p class="text-sm text-blue-700 mt-1">Selecciona un proceso de tu catálogo a continuación.</p>
        </div>
      </div>
    </div>

    <!-- Catálogo de Procesos Dinámico -->
    <div class="mb-8">
      <h3 class="text-lg font-semibold text-gray-700 mb-4 border-b pb-2">Catálogo de Procesos</h3>
      <div v-if="loading" class="text-sm text-gray-500">Cargando procesos...</div>
      <div v-else-if="processDefinitions.length === 0" class="text-sm text-gray-500">No hay procesos disponibles.</div>
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        
        <div v-for="process in processDefinitions" :key="process.id" class="bg-white rounded-lg shadow-sm border p-6 hover:shadow-md transition">
          <div class="flex items-center mb-4">
            <span class="text-3xl mr-3">🚀</span>
            <h4 class="text-lg font-bold text-ibpms">{{ process.name || process.key }}</h4>
          </div>
          <p class="text-gray-600 text-sm mb-6">{{ process.description || 'Proceso BPMN disponible para instanciar.' }}</p>
          <button class="w-full bg-gray-100 hover:bg-gray-200 text-ibpms font-medium py-2 px-4 rounded transition">
            Iniciar Proceso
          </button>
        </div>
      </div>
    </div>

    <!-- Módulo de Operador -->
    <div v-if="authStore.hasAnyRole(['ROLE_OPERADOR'])" class="mb-8">
      <h3 class="text-lg font-semibold text-gray-700 mb-4 border-b pb-2">Mis Tareas Pendientes</h3>
      <div class="bg-yellow-50 border border-yellow-200 p-6 rounded-lg">
        <h4 class="text-yellow-900 font-bold mb-2">Bandeja de Entrada</h4>
        <p class="text-sm text-yellow-700 mb-4">Tienes tareas urgentes por revisar.</p>
        <button @click="router.push('/workdesk')" class="bg-yellow-600 text-white hover:bg-yellow-700 font-medium py-2 px-4 rounded transition shadow-sm">
          Ir a Bandeja
        </button>
      </div>
    </div>

    <!-- Módulo de Administración -->
    <div v-if="authStore.hasAnyRole(['ROLE_SUPER_ADMIN', 'ROLE_SYSTEM_ADMIN'])" class="mb-8">
      <h3 class="text-lg font-semibold text-gray-700 mb-4 border-b pb-2">Panel Administrativo</h3>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div class="bg-indigo-50 border border-indigo-200 p-6 rounded-lg">
          <h4 class="text-indigo-900 font-bold mb-2">Administración del Sistema</h4>
          <p class="text-sm text-indigo-700 mb-4">Acceso a configuraciones globales, gestión de usuarios y auditoría.</p>
          <button @click="router.push('/admin')" class="bg-indigo-600 text-white hover:bg-indigo-700 font-medium py-2 px-4 rounded transition shadow-sm">
            Ir a Configuración Global
          </button>
        </div>
        <div class="bg-red-50 border border-red-200 p-6 rounded-lg">
          <h4 class="text-red-900 font-bold mb-2">Auditoría Rápida</h4>
          <p class="text-sm text-red-700 mb-4">Revisa eventos de seguridad recientes.</p>
          <button @click="router.push('/admin/incidents')" class="bg-red-600 text-white hover:bg-red-700 font-medium py-2 px-4 rounded transition shadow-sm">
            Ver Logs
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { useIntegrationStore } from '@/stores/useIntegrationStore';
import { ref, onMounted } from 'vue';
import { useAuthStore } from '@/stores/authStore';
import { useRouter } from 'vue-router';

// @Traceability: Retro-Remediación ADR-006
const integrationStore = useIntegrationStore();

const authStore = useAuthStore();
const router = useRouter();
const processDefinitions = ref<any[]>([]);
const frequentProcesses = ref<any[]>([]);
const loading = ref(true);

onMounted(async () => {
    try {
        // @Traceability: US-005, CA-14
        const { data } = await integrationStore.get('/design/processes/catalog?status=ACTIVE');
        processDefinitions.value = data || [];
    } catch (e) {
        console.error('Error fetching processes', e);
    } finally {
        loading.value = false;
    }
});
</script>
