<template>
  <div class="workdesk-grid bg-white rounded-lg shadow overflow-hidden">
    <table class="min-w-full divide-y divide-gray-200">
      <thead class="bg-gray-50">
        <tr>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nombre</th>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Estado</th>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Acciones (CA-11/15)</th>
        </tr>
      </thead>
      <tbody class="bg-white divide-y divide-gray-200">
        <tr v-if="tasks.length === 0">
           <td colspan="3" class="px-6 py-4 text-center text-sm text-gray-500">No hay tareas disponibles</td>
        </tr>
        <tr v-for="task in tasks" :key="task.unifiedId" class="hover:bg-gray-50">
          <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ task.title }}</td>
          <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
            <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">
              {{ task.status }}
            </span>
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
</template>

<script setup lang="ts">
import { useWorkdeskStore } from '@/stores/useWorkdeskStore';
import { useAuthStore } from '@/stores/authStore';
import { computed } from 'vue';

const props = defineProps<{
    tasks: any[];
}>();

const workdeskStore = useWorkdeskStore();
const authStore = useAuthStore();

const currentUser = computed(() => authStore.user?.username || authStore.activeRole);

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
