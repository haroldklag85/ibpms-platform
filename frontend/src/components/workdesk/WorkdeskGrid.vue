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

    <!-- CA-02 Bulk Claim UI -->
    <div v-if="workdeskStore.activeView === 'POOL' && selectedTasks.length > 0" class="mb-4 bg-teal-50 border border-teal-200 rounded-lg p-3 flex justify-between items-center shadow-sm">
      <span class="text-sm text-teal-800 font-semibold">{{ selectedTasks.length }} tareas seleccionadas</span>
      <button 
        @click="handleBulkClaim" 
        :disabled="workdeskStore.isLoading"
        class="bg-teal-600 text-white px-4 py-1.5 rounded shadow text-sm font-bold hover:bg-teal-700 disabled:opacity-50 transition"
      >
        Reclamar Seleccionadas
      </button>
    </div>

    <div class="workdesk-grid bg-white rounded-lg shadow overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th v-if="workdeskStore.activeView === 'POOL'" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-10">
              <!-- Select All Checkbox omitted for simplicity, individual select added below -->
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nombre</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Estado</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">SLA</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Acciones (CA-11/15)</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-if="tasks.length === 0">
             <td :colspan="workdeskStore.activeView === 'POOL' ? 5 : 4" class="px-6 py-4 text-center text-sm text-gray-500">No hay tareas disponibles</td>
          </tr>
          <tr v-for="task in tasks" :key="task.unifiedId" class="hover:bg-gray-50 transition-colors cursor-pointer" @dblclick="selectedPreviewId = task.unifiedId">
            <td v-if="workdeskStore.activeView === 'POOL'" class="px-6 py-4 whitespace-nowrap">
               <input type="checkbox" :value="task.unifiedId" v-model="selectedTasks" class="rounded text-teal-600 focus:ring-teal-500" @click.stop />
            </td>
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
              @click="selectedPreviewId = task.unifiedId"
              :disabled="workdeskStore.isLoading"
              class="text-indigo-600 hover:text-indigo-900 bg-indigo-50 hover:bg-indigo-100 px-3 py-1 rounded transition-colors disabled:opacity-50 flex items-center gap-1"
            >
              <span class="text-xs">👁</span> Ver Detalle
            </button>
            <button 
              v-if="task.assignee === currentUser && task.status === 'ACTIVE'"
              @click="promptUnclaim(task.unifiedId)"
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

    <!-- CA-5 Task Preview Modal -->
    <TaskPreviewModal 
      :taskId="selectedPreviewId" 
      :readOnly="workdeskStore.activeView === 'POOL'"
      @close="selectedPreviewId = null" 
    />

    <!-- CA-7 Custom Unclaim Confirm Modal -->
    <Teleport to="body">
      <div v-if="unclaimTargetId" class="fixed inset-0 bg-gray-900/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
        <div class="bg-white rounded-lg shadow-xl max-w-sm w-full p-6 text-center border border-gray-100">
            <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100 mb-4">
              <span class="text-2xl">⚠️</span>
            </div>
            <h3 class="text-lg leading-6 font-bold text-gray-900 mb-2">¿Liberar Tarea?</h3>
            <p class="text-sm text-gray-500 mb-4">
              Si liberas esta tarea, perderás todo el progreso no guardado y volverá a la cola global. ¿Estás seguro?
            </p>
            <div class="mb-6 text-left">
              <label class="block text-xs font-bold text-gray-700 mb-1">Motivo (Opcional):</label>
              <textarea v-model="unclaimReason" placeholder="Justificación de liberación..." class="w-full border border-gray-300 rounded p-2 text-sm focus:ring-red-500 focus:border-red-500 outline-none" rows="2"></textarea>
            </div>
            <div class="flex justify-center gap-3">
               <button @click="unclaimTargetId = null" class="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 transition font-medium text-sm">Cancelar</button>
               <button @click="confirmUnclaim" class="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 transition font-bold shadow-sm text-sm">Sí, liberar</button>
            </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useWorkdeskStore } from '@/stores/useWorkdeskStore';
import { useAuthStore } from '@/stores/authStore';
import SlaIndicator from './SlaIndicator.vue';
import TaskPreviewModal from './TaskPreviewModal.vue';

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

const selectedPreviewId = ref<string | null>(null);
const unclaimTargetId = ref<string | null>(null);
const unclaimReason = ref('');
const selectedTasks = ref<string[]>([]);

const promptUnclaim = (taskId: string) => {
    unclaimTargetId.value = taskId;
    unclaimReason.value = '';
};

const confirmUnclaim = async () => {
    if (!unclaimTargetId.value) return;
    try {
        await workdeskStore.unclaimTask(unclaimTargetId.value, unclaimReason.value);
    } catch (e) {
        // Validation managed
    } finally {
        unclaimTargetId.value = null;
    }
};

const handleBulkClaim = async () => {
    if (selectedTasks.value.length === 0) return;
    try {
        await workdeskStore.bulkClaimTasks(selectedTasks.value);
        selectedTasks.value = [];
        // Optimistic UI change to Personal tab
        workdeskStore.setActiveView('PERSONAL');
    } catch (e) {
        alert("Error reclamando tareas");
    }
};
</script>
