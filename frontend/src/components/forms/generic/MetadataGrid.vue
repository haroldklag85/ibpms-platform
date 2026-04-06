<template>
  <div class="mb-6">
    <h3 class="text-sm font-semibold text-gray-800 mb-3 border-b pb-1">Metadatos de la Tarea</h3>
    <div v-if="!hasData" class="p-4 bg-gray-50 border border-gray-200 rounded text-sm text-gray-500 text-center">
      No hay metadatos disponibles para esta tarea.
    </div>
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div v-for="(value, key) in prefillData" :key="key" class="flex flex-col">
        <label class="text-xs font-medium text-gray-600 mb-1 capitalize">{{ key.replace(/_/g, ' ') }}</label>
        <div class="relative group">
          <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <svg class="h-4 w-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
            </svg>
          </div>
          <input 
            type="text" 
            readonly 
            disabled
            :value="value"
            class="block w-full pl-9 pr-3 py-2 border border-gray-300 rounded-md bg-[#F5F5F5] text-gray-500 text-sm cursor-not-allowed focus:outline-none"
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
