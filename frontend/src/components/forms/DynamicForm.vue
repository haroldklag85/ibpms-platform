<template>
  <form @submit.prevent="handleSubmit" class="bg-gray-50 border p-6 rounded-lg relative">
    
    <div class="mb-6 border-b pb-4">
      <h3 class="text-xl font-bold text-gray-800">{{ schema.title || 'Formulario de Tarea' }}</h3>
      <p v-if="schema.description" class="text-sm text-gray-500 mt-1">{{ schema.description }}</p>
    </div>

    <div class="space-y-4">
      <!-- Iterador Inteligente (Recursividad de Nivel 1 con Dual Pattern) -->
      <template v-for="field in schema.fields" :key="field.key">
        <!-- Dual Pattern Hiding -->
        <div v-show="!field.stage || currentStage === 'ALL' || field.stage === currentStage">
          <DynamicField
            :field="field"
            v-model="formData[field.key]"
            :error="zodErrors[field.key]"
          />
        </div>
      </template>
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
  },
  currentStage: {
    type: String,
    default: 'ALL'
  },
  zodSchema: {
    type: Object as PropType<any>,
    default: null
  }
});

const emit = defineEmits(['submit', 'cancel']);

const formData = ref<Record<string, any>>({});
const isSubmitting = ref(false);
const zodErrors = ref<Record<string, string>>({});
const AUTOSAVE_KEY = 'ibpms_draft_form_v1';

// Inicializar formData en caso de defaultValues o Draft Guardado
const initFormData = () => {
  const savedDraft = localStorage.getItem(AUTOSAVE_KEY);
  if (savedDraft) {
    try {
      formData.value = JSON.parse(savedDraft);
      return;
    } catch (e) { /* ignore */ }
  }

  const initialData: Record<string, any> = {};
  props.schema.fields.forEach(field => {
    if (field.defaultValue !== undefined) {
      initialData[field.key] = field.defaultValue;
    } else if (field.type === 'boolean') {
      initialData[field.key] = false;
    } else if (field.type === 'array') { // CA-25 Zod Arrays
      initialData[field.key] = [];
    } else {
      initialData[field.key] = null;
    }
  });
  formData.value = initialData;
};

// Auto-Save + Zod Live Validation Watcher
watch(formData, (newVal) => {
  localStorage.setItem(AUTOSAVE_KEY, JSON.stringify(newVal));

  if (props.zodSchema) {
    const result = props.zodSchema.safeParse(newVal);
    if (!result.success) {
      const errMap: Record<string, string> = {};
      result.error.issues.forEach((issue: any) => {
        errMap[issue.path[0]] = issue.message;
      });
      zodErrors.value = errMap;
    } else {
      zodErrors.value = {};
    }
  }
}, { deep: true });

// Si cambia el schema dinámicamente, reiniciamos el form
watch(() => props.schema, initFormData, { immediate: true, deep: true });

const handleSubmit = () => {
  if (props.zodSchema) {
    const result = props.zodSchema.safeParse(formData.value);
    if (!result.success) {
      // CA-25 Abort submit if Zod fails (like Array Min Rows)
      return; 
    }
  }

  isSubmitting.value = true;
  // Simular retraso de validación/envío
  setTimeout(() => {
    // Escupir el Payload en JSON puro hacia el orquestador
    emit('submit', JSON.parse(JSON.stringify(formData.value)));
    localStorage.removeItem(AUTOSAVE_KEY); // Clean draft
    isSubmitting.value = false;
  }, 600);
};
</script>
