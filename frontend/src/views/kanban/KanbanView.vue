<template>
  <div class="h-full flex flex-col pt-2 bg-white">
    <div class="flex justify-between items-center mb-6 px-6">
      <div class="flex items-center space-x-3">
         <h2 class="text-2xl font-bold text-gray-800">Tablero Kanban Interactivo</h2>
         <span v-if="isReadonly" class="px-2 py-1 bg-gray-200 text-gray-600 text-xs font-bold rounded">Modo Lectura</span>
      </div>
      <div class="flex items-center space-x-3">
        <button @click="loadBoard" class="px-4 py-2 bg-ibpms text-white rounded text-sm hover:bg-gray-800 shadow-sm transition">
          🔄 Recargar Tablero
        </button>
        <span v-if="syncStatus" class="text-xs text-ibpms-brand font-medium animate-pulse">{{ syncStatus }}</span>
      </div>
    </div>

    <div v-if="isLoading" class="flex-1 flex justify-center items-center">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-ibpms-brand"></div>
    </div>

    <div v-else class="flex-1 overflow-x-auto overflow-y-hidden pb-4 px-6">
      <div class="flex space-x-6 h-full items-start">
        
        <KanbanColumn 
          v-for="col in kanbanStore.columns" 
          :key="col.id" 
          :column="col"
          :items="col.items"
          :disabled="isReadonly"
          @itemMoved="handleItemMove"
        />

      </div>
    </div>

    <!-- Modal para Bloqueos -->
    <div v-if="showBlockModal" class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center">
       <div class="bg-white p-6 rounded shadow-xl w-96">
          <h3 class="text-lg font-bold text-red-600 mb-2">Bloquear Tarea</h3>
          <p class="text-sm text-gray-600 mb-4">Por favor, especifica el Motivo de Bloqueo para la tarea <strong>{{ taskToBlock?.title }}</strong>.</p>
          <textarea v-model="blockReasonInput" rows="3" class="w-full border rounded p-2 text-sm mb-4" placeholder="Ej: Faltan documentos del cliente..."></textarea>
          <div class="flex justify-end space-x-2">
             <button @click="cancelBlock" class="px-4 py-2 bg-gray-200 text-gray-700 rounded text-sm">Cancelar</button>
             <button @click="confirmBlock" :disabled="!blockReasonInput.trim()" class="px-4 py-2 bg-red-600 text-white rounded text-sm disabled:opacity-50">Bloquear</button>
          </div>
       </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useKanbanStore } from '@/stores/kanbanStore';
import KanbanColumn from '@/components/kanban/KanbanColumn.vue';
import { useAuthStore } from '@/stores/authStore';

const kanbanStore = useKanbanStore();
const authStore = useAuthStore();

const syncStatus = ref('');
const isReadonly = ref(false); // Lógica temporal. TODO: Ligar a RBAC real si se requiere

// Block Modal State
const showBlockModal = ref(false);
const taskToBlock = ref<any>(null);
const blockReasonInput = ref('');

const isLoading = computed(() => kanbanStore.loading);

const handleItemMove = async ({ item, newStatus }: { item: any, newStatus: string }) => {
  if (isReadonly.value) return;

  if (newStatus === 'BLOCKED') {
     taskToBlock.value = item;
     blockReasonInput.value = '';
     showBlockModal.value = true;
     return;
  }

  try {
    syncStatus.value = `Guardando ${item.id}...`;
    await kanbanStore.moveTask(item.id, newStatus);
    syncStatus.value = `OK`;
  } catch(error) {
    syncStatus.value = `Error`;
  } finally {
    setTimeout(() => syncStatus.value = '', 2000);
  }
};

const confirmBlock = async () => {
    if (!taskToBlock.value || !blockReasonInput.value.trim()) return;
    
    try {
        syncStatus.value = `Bloqueando ${taskToBlock.value.id}...`;
        await kanbanStore.moveTask(taskToBlock.value.id, 'BLOCKED', blockReasonInput.value.trim());
        syncStatus.value = `Bloqueado OK`;
    } catch(error) {
        syncStatus.value = `Error al bloquear`;
    } finally {
        showBlockModal.value = false;
        taskToBlock.value = null;
        setTimeout(() => syncStatus.value = '', 2000);
    }
};

const cancelBlock = () => {
    showBlockModal.value = false;
    taskToBlock.value = null;
    kanbanStore.fetchBoard(); // rollback visually
};

const loadBoard = async () => {
  await kanbanStore.fetchBoard();
};

onMounted(() => {
  loadBoard();
});
</script>
