<template>
  <div class="space-y-4 bg-surface rounded p-4 border border-border">
    <h3 class="font-bold text-lg text-text border-b border-border pb-2">Datos del Formulario</h3>
    <form @submit.prevent="submitForm" class="space-y-4" v-if="schema">
      <!-- Iterador de campos JSON -->
      <div v-for="field in schema.fields" :key="field.key" class="flex flex-col space-y-1">
        <label :for="field.key" class="text-sm font-medium text-text-muted">
          {{ field.label }}
          <span v-if="field.required" class="text-danger">*</span>
        </label>
        
        <!-- Input Text -->
        <input v-if="field.type === 'string'"
               :id="field.key"
               v-model="formData[field.key]"
               :required="field.required"
               class="px-3 py-2 border border-border rounded shadow-sm focus:outline-none focus:ring-1 focus:ring-primary text-text"
               type="text" />
               
        <!-- Select / Enums -->
        <select v-else-if="field.type === 'enum'"
                :id="field.key"
                v-model="formData[field.key]"
                :required="field.required"
                class="px-3 py-2 border border-border rounded shadow-sm focus:outline-none focus:ring-1 focus:ring-primary text-text">
          <option v-for="option in field.options" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>
        
        <!-- Booleans -->
        <div v-else-if="field.type === 'boolean'" class="flex items-center space-x-2">
          <input :id="field.key" type="checkbox" v-model="formData[field.key]" class="h-4 w-4 text-primary rounded border-gray-300" />
          <span class="text-sm text-text-muted">Confirmar {{ field.label }}</span>
        </div>
      </div>
      
      <div class="pt-4 flex justify-end space-x-3">
         <button type="button" @click="$emit('cancel')" class="px-4 py-2 border border-border rounded font-semibold text-text hover:bg-gray-50 transition">
           Cancelar
         </button>
         <button type="submit" class="px-4 py-2 bg-primary text-white font-semibold rounded shadow hover:bg-blue-600 transition">
           Completar Tarea
         </button>
      </div>
    </form>
    <div v-else class="text-center text-text-muted italic p-4">
      Cargando estructura dinñamica del formulario...
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps({
  schema: Object,
  initialData: Object
})

const emit = defineEmits(['submit', 'cancel'])
const formData = ref<Record<string, any>>({})

// Watcher para inicializar datos si los envian
watch(() => props.initialData, (newVal) => {
  if (newVal) {
    formData.value = { ...newVal }
  }
}, { immediate: true })

const submitForm = () => {
  emit('submit', formData.value)
}
</script>
