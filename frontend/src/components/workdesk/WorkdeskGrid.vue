<template>
  <div>
    <!-- Botón Atender Siguiente y Toast CA-08 -->
    <div class="mb-4 flex items-center space-x-4" v-if="workdeskStore.forceRoutingEnabled">
      <button 
        @click="handleAttendNext" 
        :disabled="workdeskStore.isAttending" 
        class="bg-blue-600 text-white px-5 py-2 rounded-lg shadow font-medium hover:bg-blue-700 disabled:opacity-50 transition flex items-center"
      >
        <svg v-if="workdeskStore.isAttending" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path>
        </svg>
        Atender Siguiente
      </button>
      <div v-if="attendToast" class="bg-indigo-100 text-indigo-800 px-4 py-2 rounded-lg text-sm border border-indigo-300 transition-opacity">
        ✅ {{ attendToast }}
      </div>
    </div>

    <div class="workdesk-grid bg-white rounded-lg shadow overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nombre</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Estado</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">SLA</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Acciones (CA-11/15)</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-if="tasks.length === 0">
             <td colspan="4" class="px-6 py-4 text-center text-sm text-gray-500">No hay tareas disponibles</td>
          </tr>
          <tr v-for="task in tasks" :key="task.unifiedId" class="hover:bg-gray-50 transition-colors">
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ task.title }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
              <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">
                {{ task.status }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
               <SlaIndicator v-if="task.slaExpirationDate" :expirationDate="task.slaExpirationDate" />
               <span v-else class="text-gray-400">N/A</span>
            </td>
          <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
            <button 
              v-if="!task.assignee || task.status === 'AVAILABLE'"
              @click="handleClaim(task.unifiedId)"
              :disabled="workdeskStore.isLoading"
              class="text-indigo-600 hover:text-indigo-900 bg-indigo-50 hover:bg-indigo-100 px-3 py-1 rounded transition-colors disabled:opacity-50"
            >
              Reclamar
            </button>
            <button 
              v-if="task.assignee === currentUser && task.status === 'ACTIVE'"
              @click="handleUnclaim(task.unifiedId)"
              :disabled="workdeskStore.isLoading"
              class="text-red-600 hover:text-red-900 bg-red-50 hover:bg-red-100 px-3 py-1 rounded ml-2 transition-colors disabled:opacity-50"
            >
              Liberar (Unclaim)
            </button>
          </td>
        </tr>
      </tbody>
    </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useWorkdeskStore } from '@/stores/useWorkdeskStore';
import { useAuthStore } from '@/stores/authStore';
import SlaIndicator from './SlaIndicator.vue';

const props = defineProps<{
    tasks: any[];
}>();

const workdeskStore = useWorkdeskStore();
const authStore = useAuthStore();

const currentUser = computed(() => authStore.user?.username || authStore.activeRole);
const attendToast = ref('');

const handleAttendNext = async () => {
    try {
        const taskAssigned = await workdeskStore.attendNext();
        attendToast.value = `¡Tarea asignada: ${taskAssigned?.title || taskAssigned?.unifiedId}!`;
        setTimeout(() => attendToast.value = '', 4000);
    } catch (e: any) {
        attendToast.value = '';
        alert(e.message || 'Error intentando asignar siguiente tarea.');
    }
};

const handleClaim = async (taskId: string) => {
    try {
        await workdeskStore.claimTask(taskId);
        // Toast or Global Notification handle success state feedback
    } catch (e) {
        // Exception caught, 403 or 409
    }
};

const handleUnclaim = async (taskId: string) => {
    const confirm = window.confirm('¿Está seguro de querer liberar esta tarea y devolverla a la cola global?');
    if (!confirm) return;

    try {
        await workdeskStore.unclaimTask(taskId);
    } catch (e) {
        // Validation managed
    }
};
</script>
