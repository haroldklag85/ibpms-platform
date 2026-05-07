<template>
  <div v-if="show" class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center">
    <div class="bg-white p-6 rounded shadow-xl w-96">
      <div class="flex justify-between items-center mb-4">
        <h3 class="text-lg font-bold text-gray-800">Agregar Nueva Columna</h3>
        <span class="text-xs font-bold px-2 py-1 bg-gray-200 text-gray-700 rounded">{{ currentCount }}/7 columnas</span>
      </div>
      
      <p class="text-sm text-gray-600 mb-4">
        Ingresa el nombre para la nueva columna del tablero.
      </p>
      
      <input 
        v-model="columnNameInput" 
        type="text" 
        class="w-full border rounded p-2 text-sm mb-2" 
        placeholder="Nombre de la columna..." 
        data-testid="column-name-input"
        @keyup.enter="onConfirm"
      />
      <p v-if="isLimitReached" class="text-xs text-red-600 mb-4">Se ha alcanzado el límite de 7 columnas.</p>
      <p v-else-if="nameError" class="text-xs text-red-600 mb-4">{{ nameError }}</p>
      
      <div class="flex justify-end space-x-2 mt-4">
        <button @click="onCancel" class="px-4 py-2 bg-gray-200 text-gray-700 rounded text-sm hover:bg-gray-300" data-testid="cancel-add-col">
          Cancelar
        </button>
        <button 
          @click="onConfirm" 
          :disabled="!columnNameInput.trim() || isLimitReached" 
          class="px-4 py-2 bg-indigo-600 text-white rounded text-sm hover:bg-indigo-700 disabled:opacity-50" 
          data-testid="confirm-add-col"
        >
          Guardar Columna
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import { useKanbanStore } from '@/stores/kanbanStore';

const props = defineProps({
  show: Boolean
});

const emit = defineEmits(['confirm', 'cancel']);
const columnNameInput = ref('');
const nameError = ref('');

const store = useKanbanStore();
const currentCount = computed(() => store.columns.length);
const isLimitReached = computed(() => store.columns.length >= 7);

watch(() => props.show, (newVal) => {
  if (newVal) {
    columnNameInput.value = '';
    nameError.value = '';
  }
});

const onConfirm = () => {
  if (isLimitReached.value) return;
  const name = columnNameInput.value.trim();
  if (!name) return;
  
  if (store.columns.find(c => c.name.toLowerCase() === name.toLowerCase() || c.title?.toLowerCase() === name.toLowerCase())) {
    nameError.value = 'El nombre de la columna ya existe.';
    return;
  }
  
  emit('confirm', name);
};

const onCancel = () => {
  emit('cancel');
};
</script>
