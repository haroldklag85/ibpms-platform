<template>
  <form @submit.prevent="handleSubmit" class="bg-gray-50 border p-6 rounded-lg relative">
    
    <div class="mb-6 border-b pb-4">
      <h3 class="text-xl font-bold text-gray-800">{{ schema.title || 'Formulario de Tarea' }}</h3>
      <p v-if="schema.description" class="text-sm text-gray-500 mt-1">{{ schema.description }}</p>
    </div>

    <div class="space-y-2">
      <!-- Iterador Inteligente (Recursividad de Nivel 1) -->
      <DynamicField
        v-for="field in schema.fields"
        :key="field.key"
        :field="field"
        v-model="formData[field.key]"
      />
    </div>

    <div class="mt-8 pt-4 border-t flex justify-end space-x-3">
      <button type="button" @click="$emit('cancel')" class="px-4 py-2 border rounded-md text-gray-700 hover:bg-gray-100 font-medium transition">
        Cancelar
      </button>
      <button type="submit" :disabled="isSubmitting" class="px-5 py-2 bg-ibpms-brand text-white rounded-md font-bold shadow-sm hover:bg-blue-600 disabled:opacity-50 transition flex items-center">
        <span v-if="isSubmitting" class="animate-spin h-4 w-4 mr-2 border-b-2 border-white rounded-full"></span>
        Completar Tarea
      </button>
    </div>

  </form>
</template>

<script setup lang="ts">
import { ref, watch, PropType } from 'vue';
import type { FormSchema } from '@/types/FormSchema';
import DynamicField from './DynamicField.vue';

const props = defineProps({
  schema: {
    type: Object as PropType<FormSchema>,
    required: true
  }
});

const emit = defineEmits(['submit', 'cancel']);

// Objeto Central Reactivo para recolectar las respuestas
const formData = ref<Record<string, any>>({});
const isSubmitting = ref(false);

// Inicializar formData en caso de defaultValues
const initFormData = () => {
  const initialData: Record<string, any> = {};
  props.schema.fields.forEach(field => {
    if (field.defaultValue !== undefined) {
      initialData[field.key] = field.defaultValue;
    } else if (field.type === 'boolean') {
      initialData[field.key] = false;
    } else {
      initialData[field.key] = null;
    }
  });
  formData.value = initialData;
};

// Si cambia el schema dinámicamente, reiniciamos el form
watch(() => props.schema, initFormData, { immediate: true, deep: true });

const handleSubmit = () => {
  isSubmitting.value = true;
  // Simular retraso de validación/envío
  setTimeout(() => {
    // Escupir el Payload en JSON puro hacia el orquestador
    emit('submit', JSON.parse(JSON.stringify(formData.value)));
    isSubmitting.value = false;
  }, 600);
};
</script>
