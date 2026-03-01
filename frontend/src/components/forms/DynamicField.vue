<template>
  <div class="mb-4">
    <!-- Label -->
    <label v-if="field.label" :for="field.key" class="block text-sm font-medium text-gray-700 mb-1">
      {{ field.label }}
      <span v-if="field.required" class="text-red-500">*</span>
    </label>

    <!-- TYPE: STRING / TEXT -->
    <input 
      v-if="field.type === 'string'"
      :id="field.key"
      type="text"
      :value="modelValue"
      @input="updateValue(($event.target as HTMLInputElement).value)"
      :placeholder="field.metadata?.placeholder || ''"
      :required="field.required"
      :disabled="field.disabled"
      class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-ibpms-brand focus:border-ibpms-brand disabled:bg-gray-100 disabled:text-gray-500"
    />

    <!-- TYPE: NUMBER -->
    <input 
      v-else-if="field.type === 'number'"
      :id="field.key"
      type="number"
      :value="modelValue"
      @input="updateValue(Number(($event.target as HTMLInputElement).value))"
      :min="field.metadata?.min"
      :max="field.metadata?.max"
      :required="field.required"
      :disabled="field.disabled"
      class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-ibpms-brand focus:border-ibpms-brand"
    />

    <!-- TYPE: SELECT -->
    <select 
      v-else-if="field.type === 'select'"
      :id="field.key"
      :value="modelValue"
      @change="updateValue(($event.target as HTMLSelectElement).value)"
      :required="field.required"
      :disabled="field.disabled"
      class="w-full px-3 py-2 border border-gray-300 bg-white rounded-md shadow-sm focus:outline-none focus:ring-ibpms-brand focus:border-ibpms-brand disabled:bg-gray-100"
    >
      <option value="" disabled selected>Seleccione una opción</option>
      <option v-for="opt in field.options" :key="opt.value" :value="opt.value">
        {{ opt.label }}
      </option>
    </select>

    <!-- TYPE: BOOLEAN / CHECKBOX -->
    <div v-else-if="field.type === 'boolean'" class="flex items-center mt-2">
      <input 
        :id="field.key"
        type="checkbox"
        :checked="!!modelValue"
        @change="updateValue(($event.target as HTMLInputElement).checked)"
        :required="field.required"
        :disabled="field.disabled"
        class="h-4 w-4 text-ibpms-brand focus:ring-ibpms-brand border-gray-300 rounded"
      />
      <label :for="field.key" class="ml-2 block text-sm text-gray-900">
        Confirmar (Sí / No)
      </label>
    </div>

    <!-- TYPE: DATE -->
    <input 
      v-else-if="field.type === 'date'"
      :id="field.key"
      type="date"
      :value="modelValue"
      @input="updateValue(($event.target as HTMLInputElement).value)"
      :required="field.required"
      :disabled="field.disabled"
      class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-ibpms-brand focus:border-ibpms-brand"
    />

    <!-- Fallback / Error -->
    <div v-else class="p-2 bg-red-50 text-red-600 text-sm border border-red-200 rounded">
      Tipo de campo no soportado: <strong>{{ field.type }}</strong>
    </div>

  </div>
</template>

<script setup lang="ts">
import { PropType } from 'vue';
import type { FormField } from '@/types/FormSchema';

const props = defineProps({
  field: {
    type: Object as PropType<FormField>,
    required: true
  },
  modelValue: {
    type: [String, Number, Boolean, Object, Array],
    default: null
  }
});

const emit = defineEmits(['update:modelValue']);

const updateValue = (val: any) => {
  emit('update:modelValue', val);
};
</script>
