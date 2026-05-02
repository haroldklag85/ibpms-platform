<template>
  <div class="h-full flex flex-col pt-2 bg-white" data-testid="kanban-board">
    <div class="flex justify-between items-center mb-6 px-6">
      <div class="flex items-center space-x-3">
         <h2 class="text-2xl font-bold text-gray-800">Tablero Kanban Interactivo</h2>
         <span v-if="isReadonly" class="px-2 py-1 bg-gray-200 text-gray-600 text-xs font-bold rounded">Modo Lectura</span>
      </div>
      <div class="flex items-center space-x-3">
        <button v-if="canManageColumns" @click="showAddColModal = true" class="px-4 py-2 bg-indigo-600 text-white rounded text-sm hover:bg-indigo-700 shadow-sm transition">
          + Agregar Columna
        </button>
        <button @click="loadBoard" class="px-4 py-2 bg-ibpms text-white rounded text-sm hover:bg-gray-800 shadow-sm transition">
          🔄 Recargar Tablero
        </button>
        <div class="sync-indicator text-sm font-medium" data-testid="kanban-sync-status">
          <span v-if="syncStatus === 'saving'" class="text-yellow-600 animate-pulse">⏳ Guardando...</span>
          <span v-else-if="syncStatus === 'ok'" class="text-green-600">✅ OK</span>
          <span v-else-if="syncStatus === 'error'" class="text-red-600">❌ Error</span>
        </div>
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

    <!-- Modals -->
    <BlockedReasonModal 
      :show="showBlockModal" 
      :taskTitle="taskToBlock?.title" 
      @confirm="confirmBlock" 
      @cancel="cancelBlock" 
    />
    
    <AddColumnModal
      :show="showAddColModal"
      @confirm="confirmAddColumn"
      @cancel="showAddColModal = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import { useKanbanStore } from '@/stores/kanbanStore';
import KanbanColumn from '@/components/kanban/KanbanColumn.vue';
import { useAuthStore } from '@/stores/authStore';
import BlockedReasonModal from '@/components/kanban/BlockedReasonModal.vue';
import AddColumnModal from '@/components/kanban/AddColumnModal.vue';

const route = useRoute();
const kanbanStore = useKanbanStore();
const authStore = useAuthStore();

const boardId = computed(() => (route.params.projectId as string) || 'default-board');

const syncStatus = ref('');
const isReadonly = ref(false); // Lógica temporal. TODO: Ligar a RBAC real si se requiere

// Auth for columns
const canManageColumns = computed(() => {
  const roles = authStore.user?.roles || [];
  return roles.includes('SUPERVISOR') || roles.includes('SUPER_ADMIN');
});

// Block Modal State
const showBlockModal = ref(false);
const taskToBlock = ref<any>(null);

// Add Col Modal State
const showAddColModal = ref(false);

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
    syncStatus.value = 'saving';
    await kanbanStore.moveTask(item.id, newStatus);
    syncStatus.value = 'ok';
  } catch(error: any) {
    syncStatus.value = 'error';
    if (error.response?.status === 403) {
      alert('Esta tarea está completada y no puede modificarse');
    } else if (error.response?.status === 400) {
      alert('Transición de estado no válida');
    }
  } finally {
    setTimeout(() => syncStatus.value = '', 2000);
  }
};

const confirmBlock = async (reason: string) => {
    if (!taskToBlock.value) return;
    
    try {
        syncStatus.value = 'saving';
        await kanbanStore.moveTask(taskToBlock.value.id, 'BLOCKED', reason);
        syncStatus.value = 'ok';
    } catch(error) {
        syncStatus.value = 'error';
    } finally {
        showBlockModal.value = false;
        taskToBlock.value = null;
        setTimeout(() => syncStatus.value = '', 2000);
    }
};

const cancelBlock = () => {
    showBlockModal.value = false;
    taskToBlock.value = null;
    kanbanStore.fetchBoard(boardId.value); // rollback visually
};

const confirmAddColumn = async (name: string) => {
  try {
    syncStatus.value = 'saving';
    await kanbanStore.addColumn(boardId.value, name);
    syncStatus.value = 'ok';
    showAddColModal.value = false;
  } catch (error) {
    syncStatus.value = 'error';
    alert(kanbanStore.error || 'Error al agregar columna');
  } finally {
    setTimeout(() => syncStatus.value = '', 2000);
  }
};

const loadBoard = async () => {
  await kanbanStore.fetchBoard(boardId.value);
};

onMounted(() => {
  loadBoard();
});
</script>
