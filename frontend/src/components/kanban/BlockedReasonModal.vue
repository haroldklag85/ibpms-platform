<template>
  <div v-if="show" class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center">
    <div class="bg-white p-6 rounded shadow-xl w-96">
      <h3 class="text-lg font-bold text-red-600 mb-2">Bloquear Tarea</h3>
      <p class="text-sm text-gray-600 mb-4">
        Por favor, especifica el Motivo de Bloqueo para la tarea <strong>{{ taskTitle }}</strong>.
      </p>
      <textarea 
        v-model="blockReasonInput" 
        rows="3" 
        class="w-full border rounded p-2 text-sm mb-4" 
        placeholder="Describe el motivo del bloqueo..." 
        data-testid="block-reason-input"
      ></textarea>
      <div class="flex justify-end space-x-2">
        <button @click="onCancel" class="px-4 py-2 bg-gray-200 text-gray-700 rounded text-sm hover:bg-gray-300" data-testid="cancel-block">
          Cancelar
        </button>
        <button @click="onConfirm" :disabled="!blockReasonInput.trim()" class="px-4 py-2 bg-red-600 text-white rounded text-sm hover:bg-red-700 disabled:opacity-50" data-testid="confirm-block">
          Confirmar Bloqueo
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';

const props = defineProps({
  show: Boolean,
  taskTitle: String
});

const emit = defineEmits(['confirm', 'cancel']);
const blockReasonInput = ref('');

watch(() => props.show, (newVal) => {
  if (newVal) {
    blockReasonInput.value = '';
  }
});

const onConfirm = () => {
  if (blockReasonInput.value.trim()) {
    emit('confirm', blockReasonInput.value.trim());
  }
};

const onCancel = () => {
  emit('cancel');
};
</script>
