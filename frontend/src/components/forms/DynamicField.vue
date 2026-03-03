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

    <!-- TYPE: TYPEAHEAD (CA-24) -->
    <div v-else-if="field.type === 'typeahead'" class="relative">
      <input
        :id="field.key"
        type="text"
        :value="modelValue"
        @input="handleTypeaheadInput"
        @focus="typeaheadOpen = true"
        :placeholder="field.metadata?.placeholder || 'Buscar...'"
        :required="field.required"
        :disabled="field.disabled"
        class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-ibpms-brand focus:border-ibpms-brand"
        autocomplete="off"
      />
      <ul v-if="typeaheadOpen && filteredTypeaheadOptions.length > 0" class="absolute z-10 mt-1 w-full bg-white shadow-lg max-h-48 rounded-md py-1 text-base ring-1 ring-black ring-opacity-5 overflow-auto focus:outline-none sm:text-sm">
        <li v-for="opt in filteredTypeaheadOptions" :key="opt.value" @click="selectTypeahead(opt.value)" class="cursor-pointer select-none relative py-2 pl-3 pr-9 hover:bg-indigo-600 hover:text-white">
          {{ opt.label }}
        </li>
      </ul>
    </div>

    <!-- TYPE: GPS SCANNER (CA-45) -->
    <div v-else-if="field.type === 'gps'" class="flex items-center gap-3">
      <input 
        :id="field.key"
        type="text"
        :value="modelValue"
        readonly
        placeholder="Coordenadas GPS"
        class="flex-1 px-3 py-2 border border-gray-300 rounded-md shadow-sm bg-gray-50 text-gray-500"
      />
      <button type="button" @click="requestGeolocation" class="px-3 py-2 bg-indigo-100 text-indigo-700 font-bold rounded-md hover:bg-indigo-200 transition text-sm flex items-center gap-1">
        📍 Obtener GPS
      </button>
    </div>

    <!-- Fallback / Error -->
    <div v-else class="p-2 bg-red-50 text-red-600 text-sm border border-red-200 rounded">
      Tipo de campo no soportado: <strong>{{ field.type }}</strong>
    </div>

    <!-- Zod Live Error Span -->
    <span v-if="error" class="block text-xs text-red-600 mt-1 font-medium transition animate-fade-in data-test-zod-error">
      {{ error }}
    </span>

  </div>
</template>

<script setup lang="ts">
import { PropType, ref, computed } from 'vue';
import type { FormField } from '@/types/FormSchema';

const props = defineProps({
  field: {
    type: Object as PropType<FormField>,
    required: true
  },
  modelValue: {
    type: [String, Number, Boolean, Object, Array],
    default: null
  },
  error: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['update:modelValue']);

const updateValue = (val: any) => {
  emit('update:modelValue', val);
};

// --- Typeahead CA-24 Logic ---
const typeaheadOpen = ref(false);
const filteredTypeaheadOptions = computed(() => {
  if (!props.field.options) return [];
  const search = String(props.modelValue || '').toLowerCase();
  return props.field.options.filter(opt => opt.label.toLowerCase().includes(search));
});

const handleTypeaheadInput = (e: Event) => {
  const val = (e.target as HTMLInputElement).value;
  updateValue(val);
  typeaheadOpen.value = true;
};

const selectTypeahead = (val: string | number) => {
  updateValue(val);
  typeaheadOpen.value = false;
};

// --- GPS Sensor Mocking CA-45 ---
const requestGeolocation = () => {
  if ("geolocation" in navigator) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        updateValue(`${position.coords.latitude}, ${position.coords.longitude}`);
      },
      (error) => {
        alert("Error de GPS: " + error.message);
      }
    );
  } else {
    alert("Geolocalización no soportada.");
  }
};
</script>
