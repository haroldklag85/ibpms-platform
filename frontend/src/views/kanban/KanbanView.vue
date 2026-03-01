<template>
  <div class="h-full flex flex-col pt-2 bg-white">
    <div class="flex justify-between items-center mb-6 px-6">
      <h2 class="text-2xl font-bold text-gray-800">Tablero Kanban Interactívo</h2>
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

    <!-- Área de Arrastre con Desplazamiento Horizontal (Tablero) -->
    <div v-else class="flex-1 overflow-x-auto overflow-y-hidden pb-4 px-6">
      <div class="flex space-x-6 h-full items-start">
        
        <KanbanColumn 
          v-for="col in board.columns" 
          :key="col.id" 
          :column="col"
          :items="getItemsForColumn(col.id)"
          @itemMoved="handleItemMove"
        />

      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import type { KanbanBoard, KanbanItem } from '@/types/Kanban';
import KanbanColumn from '@/components/kanban/KanbanColumn.vue';

const isLoading = ref(true);
const syncStatus = ref('');

// Tablero de Prueba (Mock para este Sprint).
// Próximamente se obtendrá vía Axios (KanbanService)
const board = ref<KanbanBoard>({
  boardId: "board_operaciones",
  columns: [
    { id: "TODO", title: "Por Hacer", color: "bg-gray-100" },
    { id: "DOING", title: "En Progreso", color: "bg-blue-50" },
    { id: "DONE", title: "Finalizado", color: "bg-green-50" }
  ],
  items: [
    { id: "T-001", title: "Revisar Nómina Enero", status: "TODO", priority: 10, assignee: "Pedro P." },
    { id: "T-002", title: "Auditoría Legal Incidente", status: "DOING", priority: 80, assignee: "Carlos R." },
    { id: "T-003", title: "Carga de Documentos", status: "DOING", priority: 40, assignee: "Ana L." },
    { id: "T-004", title: "Envío Tarjeta Crédito", status: "DONE", priority: 20 }
  ]
});

const getItemsForColumn = (columnId: string) => {
  return board.value.items.filter(i => i.status === columnId);
};

// Evento emitido cuando un Item es soltado (Dropped) en una Columna Diferente.
const handleItemMove = async ({ item, newStatus }: { item: KanbanItem, newStatus: string }) => {
  // 1. Mostrar estado de sincronización.
  syncStatus.value = `Sincronizando Muvimiento T-${item.id}...`;
  
  // 2. Simulamos la llamada Axios que actualiza esto en Backend (PATCH /api/v1/kanban/items/{id}/status)
  setTimeout(() => {
    // 3. Modificamos el estado Local Reactivo si el Service responde OK.
    const targetItem = board.value.items.find(i => i.id === item.id);
    if(targetItem) {
      targetItem.status = newStatus;
    }
    syncStatus.value = `Guardado OK`;
    setTimeout(() => syncStatus.value = '', 2000);
  }, 500);
};

const loadBoard = () => {
  isLoading.value = true;
  setTimeout(() => {
    isLoading.value = false;
  }, 600);
};

onMounted(() => {
  loadBoard();
});
</script>
