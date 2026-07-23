<template>
  <div class="task-form-submit">
    <!-- CA-29: Soft-Undo Banner -->
    <div v-if="formStore.isUndoAvailable" class="bg-yellow-50 border-l-4 border-yellow-400 p-4 mb-4 rounded shadow flex justify-between items-center transition-all">
      <div>
        <p class="text-sm font-medium text-yellow-700">Formulario enviado exitosamente.</p>
        <p class="text-xs text-yellow-600">Tienes {{ formStore.undoTimeLeft }} segundos para deshacer el envío.</p>
      </div>
      <button @click="doUndo" class="px-4 py-2 bg-yellow-500 text-white text-sm font-bold rounded hover:bg-yellow-600">
        Deshacer Envío (Soft-Undo)
      </button>
    </div>

    <!-- CA-21: Validation Error Toast/Banner -->
    <div v-if="Object.keys(formStore.validationErrors).length > 0" class="bg-red-50 border-l-4 border-red-500 p-4 mb-4 rounded">
      <h3 class="text-red-800 font-bold mb-2">Errores de Validación:</h3>
      <ul class="list-disc pl-5">
        <li v-for="(msg, field) in formStore.validationErrors" :key="field" class="text-sm text-red-600">
          <strong>{{ field }}</strong>: {{ msg }}
        </li>
      </ul>
    </div>

    <!-- CA-25: Load Spinner during submit -->
    <button 
      @click="handleSubmit" 
      :disabled="formStore.isSubmitting || formStore.isUndoAvailable"
      class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
    >
      <svg v-if="formStore.isSubmitting && !formStore.isUndoAvailable" class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
      </svg>
      {{ (formStore.isSubmitting && !formStore.isUndoAvailable) ? 'Procesando...' : 'Completar Tarea' }}
    </button>
  </div>
</template>

<script setup lang="ts">
import { useFormStore } from '@/stores/useFormStore';
import { z } from 'zod';

const props = defineProps<{
  taskId: string;
  schema: z.ZodTypeAny;
}>();

const formStore = useFormStore();

const handleSubmit = async () => {
    // 1. Zod Isomorphic Validation
    const isValid = formStore.validateForm(props.schema);
    if (!isValid) {
        // Validation handled reactively by store
        return;
    }

    try {
        // Enviar con soporte de Soft-Undo
        await formStore.submitForm(props.taskId, formStore.formData, true);
        // Toast emitido silenciosamente o gestionado por App.vue global
    } catch (e: any) {
        // Si hay error duro (403, 500)
    }
};

const doUndo = () => {
    formStore.softUndo();
};
</script>
