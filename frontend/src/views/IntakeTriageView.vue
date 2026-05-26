<template>
  <div class="h-full w-full bg-gray-50 overflow-y-auto px-4 py-8 sm:px-6 lg:px-8">
    <div class="max-w-7xl mx-auto">
      
      <!-- Header Area -->
      <div class="md:flex md:items-center md:justify-between mb-6">
        <div class="flex-1 min-w-0">
          <h2 class="text-2xl font-bold leading-7 text-gray-900 sm:text-3xl sm:truncate">
            Inbox Pre-Triaje
          </h2>
          <p class="mt-1 text-sm text-gray-500">
            Monitor y enrutamiento manual de ingestas (Webhooks/Email) atrapadas por reglas de Zero-Trust. No exceda el SLA.
          </p>
        </div>
        <div class="mt-4 flex md:mt-0 md:ml-4">
          <button 
            type="button" 
            class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none"
            @click="refreshTasks"
            :disabled="intakeStore.isLoading"
          >
            <svg class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
            Refrescar
          </button>
        </div>
      </div>

      <!-- State: Error -->
      <div v-if="intakeStore.error" class="rounded-md bg-red-50 p-4 mb-6 relative">
        <div class="flex">
          <div class="flex-shrink-0">
             <svg class="h-5 w-5 text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <div class="ml-3">
            <h3 class="text-sm font-medium text-red-800">Error de Sistema</h3>
            <div class="mt-2 text-sm text-red-700">
              <p>{{ intakeStore.error }}</p>
            </div>
          </div>
          <button @click="intakeStore.error = null" class="absolute top-4 right-4 text-red-400 hover:text-red-500">
            &times;
          </button>
        </div>
      </div>

      <!-- State: Loading skeleton -->
      <div v-if="intakeStore.isLoading && intakeStore.tasks.length === 0" class="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
         <div v-for="i in 6" :key="i" class="bg-white p-6 rounded-lg shadow border border-gray-200 animate-pulse">
            <div class="h-4 bg-gray-200 rounded w-1/2 mb-4"></div>
            <div class="h-4 bg-gray-200 rounded w-1/4 mb-6"></div>
            <div class="h-6 bg-gray-200 rounded w-3/4 mb-2"></div>
            <div class="h-10 bg-gray-200 rounded mb-4"></div>
            <div class="flex justify-end space-x-2 mt-4 pt-4 border-t border-gray-100">
              <div class="h-8 bg-gray-200 rounded w-16"></div>
              <div class="h-8 bg-gray-200 rounded w-16"></div>
            </div>
         </div>
      </div>

      <!-- State: Empty -->
      <div v-else-if="intakeStore.tasks.length === 0" class="text-center py-16 bg-white rounded-lg border border-dashed border-gray-300">
        <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
        </svg>
        <h3 class="mt-2 text-sm font-medium text-gray-900">Bandeja Vacía</h3>
        <p class="mt-1 text-sm text-gray-500">No hay transacciones en Pre-Triaje actualmente.</p>
      </div>

      <!-- State: Grid Data -->
      <div v-else class="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
        <TriageTaskCard 
          v-for="task in intakeStore.tasks" 
          :key="task.id" 
          :task="task" 
          @action="openDialog"
        />
      </div>

    </div>

    <ApproveRejectDialog 
      :is-open="isDialogOpen"
      :mode="dialogMode"
      :task-id="activeTaskId"
      @close="closeDialog"
      @confirm-approve="onConfirmApprove"
      @confirm-reject="onConfirmReject"
    />
  </div>
</template>

<script setup lang="ts">
// @Traceability: US-004, CA-6, CA-8
import { useTimeStore } from '@/stores/timeStore';
import { ref, onMounted, onUnmounted, watch } from 'vue';
import { useIntakeTriageStore } from '@/stores/useIntakeTriageStore';
import TriageTaskCard from '@/components/intake/TriageTaskCard.vue';
import ApproveRejectDialog from '@/components/intake/ApproveRejectDialog.vue';

const intakeStore = useIntakeTriageStore();
const timeStore = useTimeStore();

const isDialogOpen = ref(false);
const dialogMode = ref<'APPROVE' | 'REJECT'>('APPROVE');
const activeTaskId = ref('');


onMounted(() => {
  intakeStore.fetchTasks();
  
  // Realizar polling suave cada 30 segundos si se desea
  watch(() => timeStore.currentTick, (tick) => {
    if (tick % 30000 < 1000) {
      // Only poll if not currently opening a modal/loading to avoid flicker
      if (!intakeStore.isLoading && !isDialogOpen.value) {
         intakeStore.fetchTasks(intakeStore.currentPage);
      }
    }
  }); // @Traceability: Retro-Remediación ADR-006
});

onUnmounted(() => {
  
});

const refreshTasks = () => {
  intakeStore.fetchTasks(0);
};

const openDialog = (type: 'APPROVE' | 'REJECT', id: string) => {
  dialogMode.value = type;
  activeTaskId.value = id;
  isDialogOpen.value = true;
};

const closeDialog = () => {
  isDialogOpen.value = false;
  activeTaskId.value = '';
};

const onConfirmApprove = async ({ taskId, processType }: { taskId: string, processType: string }) => {
  try {
    await intakeStore.approveTask(taskId, processType);
    closeDialog();
  } catch (error) {
    // Error is handled and surfaced in store, the dialog remains open to let user retry or cancel.
  }
};

const onConfirmReject = async ({ taskId, reason }: { taskId: string, reason: string }) => {
  try {
    await intakeStore.rejectTask(taskId, reason);
    closeDialog();
  } catch (error) {
    // Error handled in store.
  }
};
</script>
