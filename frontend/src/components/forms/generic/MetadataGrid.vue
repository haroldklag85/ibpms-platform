<!-- @Traceability: US-039 - CA-2 -->
<template>
  <div class="mb-6">
    <h3 class="text-sm font-semibold text-gray-800 mb-3 border-b pb-1">Metadatos de la Tarea</h3>
    <div v-if="!hasData" class="p-4 bg-gray-50 border border-gray-200 rounded text-sm text-gray-500 text-center">
      No hay metadatos disponibles para esta tarea.
    </div>
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div v-for="(value, key) in prefillData" :key="key" class="flex flex-col">
        <label class="text-xs font-medium text-gray-600 mb-1 capitalize flex items-center gap-1">
          <span>{{ key.replace(/_/g, ' ') }}</span>
          <span title="Campo de solo lectura">🔒</span>
        </label>
        <div class="relative group">
          <input 
            type="text" 
            readonly 
            disabled
            :value="value"
            class="block w-full px-3 py-2 border border-gray-200 rounded-md bg-gray-100 text-gray-600 text-sm cursor-not-allowed focus:outline-none"
            title="Campo de solo lectura"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  prefillData: Record<string, any>
}>()

const hasData = computed(() => {
  return props.prefillData && Object.keys(props.prefillData).length > 0
})
</script>
