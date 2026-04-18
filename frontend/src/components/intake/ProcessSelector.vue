<template>
  <div class="w-full">
    <label class="block text-sm font-medium text-gray-700 mb-1">Business Process</label>
    <select 
      v-model="modelValue" 
      class="w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-md shadow-sm"
      @change="$emit('update:modelValue', modelValue)"
    >
      <option disabled value="">Seleccione un proceso BPMN</option>
      <option v-for="process in processOptions" :key="process.id" :value="process.id">
        {{ process.name }}
      </option>
    </select>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const props = defineProps<{
  modelValue: string;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
}>();

// Typically this would come from a store or API, 
// hardcoded for demonstration/POC based on CA needs
const processOptions = ref([
  { id: 'ALTA_CLIENTE', name: 'Alta de Cliente Corporativo' },
  { id: 'PROCESO_QUEJA', name: 'Gestión de PQRS' },
  { id: 'ACTUALIZACION_DATOS', name: 'Actualización de Datos' }
]);

const modelValue = ref(props.modelValue);
</script>
