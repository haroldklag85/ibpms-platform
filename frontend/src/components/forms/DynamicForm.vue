<template>
  <form @submit.prevent="handleSubmit" class="bg-gray-50 border p-6 rounded-lg relative">
    
    <div class="mb-6 border-b pb-4">
      <h3 class="text-xl font-bold text-gray-800">{{ schema.title || 'Formulario de Tarea' }}</h3>
      <p v-if="schema.description" class="text-sm text-gray-500 mt-1">{{ schema.description }}</p>
    </div>

    <!-- @Traceability: US-029 - CA-22 - Integración de FormWizard -->
    <FormWizard 
      v-if="stages.length > 1"
      :stages="stages"
      :currentStage="internalStage"
      :errorMap="wizardErrorStatus"
      @next-step="handleNextStep"
      @prev-step="handlePrevStep"
    >
      <template #submit-button>
        <div class="flex space-x-3">
          <button type="button" @click="$emit('cancel')" class="px-4 py-2 border rounded-md text-gray-700 hover:bg-gray-100 font-medium transition">
            Cancelar
          </button>
          <button type="submit" :disabled="isSubmitting" class="px-5 py-2 bg-ibpms-brand text-white rounded-md font-bold shadow-sm hover:bg-blue-600 disabled:opacity-50 transition flex items-center">
            <span v-if="isSubmitting" class="animate-spin h-4 w-4 mr-2 border-b-2 border-white rounded-full"></span>
            Completar Tarea
          </button>
        </div>
      </template>
    </FormWizard>

    <div class="space-y-4">
      <!-- Iterador Inteligente (Recursividad de Nivel 1 con Dual Pattern) -->
      <template v-for="field in schema.fields" :key="field.key">
        <!-- Dual Pattern Hiding -->
        <div v-show="!field.stage || internalStage === 'ALL' || field.stage === internalStage">
          <DynamicField
            :field="field"
            v-model="formStore.formData[field.key]"
            :error="zodErrors[field.key]"
          />
        </div>
      </template>
    </div>

    <div v-if="stages.length <= 1" class="mt-8 pt-4 border-t flex justify-end space-x-3">
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
import { ref, watch, PropType, computed, reactive, onMounted } from 'vue';
import type { FormSchema } from '@/types/FormSchema';
import DynamicField from './DynamicField.vue';
import FormWizard from './FormWizard.vue';
import { z } from 'zod';
import { useWizardValidation } from '@/composables/useWizardValidation';
import { useFormStore } from '@/stores/useFormStore';

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

const formStore = useFormStore();
const isSubmitting = ref(false);
const zodErrors = ref<Record<string, string>>({});
const AUTOSAVE_KEY = 'ibpms_draft_form_v1';

const stages = computed(() => {
  const stgs: string[] = [];
  props.schema.fields.forEach(f => {
    if (f.stage && !stgs.includes(f.stage)) stgs.push(f.stage);
  });
  return stgs.length > 0 ? stgs : ['ALL'];
});

const internalStage = ref(props.currentStage === 'ALL' ? stages.value[0] : props.currentStage);

watch(() => props.currentStage, (val) => {
  if (val !== 'ALL') internalStage.value = val;
});

const reactiveConfigs = reactive<Record<string, z.ZodSchema>>({});

const updateSchemaConfigs = () => {
  stages.value.forEach(stg => {
    if (props.zodSchema instanceof z.ZodObject) {
      const stageKeys = props.schema.fields.filter(f => (f.stage || 'ALL') === stg).map(f => f.key);
      const mask: Record<string, true> = {};
      stageKeys.forEach(k => mask[k] = true);
      reactiveConfigs[stg] = props.zodSchema.pick(mask);
    } else {
      reactiveConfigs[stg] = props.zodSchema || z.any();
    }
  });
};

watch(() => props.schema, updateSchemaConfigs, { deep: true, immediate: true });
watch(() => props.zodSchema, updateSchemaConfigs, { deep: true, immediate: true });

const { wizardErrors, validateStep, hasStepErrors, clearStepErrors } = useWizardValidation(reactiveConfigs);

const wizardErrorStatus = computed(() => {
  const status: Record<string, boolean> = {};
  stages.value.forEach(s => {
    status[s] = hasStepErrors(s);
  });
  return status;
});

// Inicializar formData en caso de defaultValues o Draft Guardado
const initFormData = () => {
  const savedDraft = localStorage.getItem(AUTOSAVE_KEY);
  if (savedDraft) {
    try {
      formStore.setFormData(JSON.parse(savedDraft));
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
  formStore.setFormData(initialData);
};

onMounted(() => {
  initFormData();
});

// Auto-Save + Zod Live Validation Watcher delegado al Store y Composable
watch(() => formStore.formData, (newVal) => {
  localStorage.setItem(AUTOSAVE_KEY, JSON.stringify(newVal));

  if (stages.value.length > 1) {
    // Si hay Wizard, usamos live validation del current step sin frenar navigation
    const result = reactiveConfigs[internalStage.value]?.safeParse(newVal);
    if (result && !result.success) {
      const errMap: Record<string, string> = {};
      result.error.issues.forEach((issue: any) => {
        errMap[issue.path[0]] = issue.message;
      });
      zodErrors.value = errMap;
    } else {
      zodErrors.value = {};
    }
  } else if (props.zodSchema) {
    // Single stage form
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

// Watch for schema changes dynamically
watch(() => props.schema, initFormData, { deep: true });

const handleNextStep = (nextStage: string) => {
  if (validateStep(internalStage.value, formStore.formData)) {
    zodErrors.value = {}; // clear current errors
    internalStage.value = nextStage;
  } else {
    // show errors for current step
    zodErrors.value = wizardErrors.value[internalStage.value] || {};
  }
};

const handlePrevStep = (prevStage: string) => {
  zodErrors.value = {};
  internalStage.value = prevStage;
};

const handleSubmit = () => {
  if (stages.value.length > 1) {
    // Wizard format: Validate current last step before submit
    if (!validateStep(internalStage.value, formStore.formData)) {
      zodErrors.value = wizardErrors.value[internalStage.value] || {};
      return; // abort if last step is invalid
    }
  } else if (props.zodSchema) {
    // Normal single-page format
    const result = props.zodSchema.safeParse(formStore.formData);
    if (!result.success) {
      return; 
    }
  }

  isSubmitting.value = true;
  setTimeout(() => {
    emit('submit', JSON.parse(JSON.stringify(formStore.formData)));
    localStorage.removeItem(AUTOSAVE_KEY); // Clean draft
    isSubmitting.value = false;
  }, 600);
};
</script>

