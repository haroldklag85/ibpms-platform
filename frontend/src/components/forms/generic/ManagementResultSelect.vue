<template>
  <div class="mb-5">
    <label for="managementResult" class="block text-sm font-medium text-gray-700 mb-1">
      Resultado de la Gestión <span class="text-red-500">*</span>
    </label>
    <select
      id="managementResult"
      v-model="store.result"
      class="mt-1 block w-full rounded-md border-gray-300 py-2 pl-3 pr-10 text-base focus:border-indigo-500 focus:outline-none focus:ring-indigo-500 sm:text-sm"
    >
      <option value="" disabled>Seleccione un resultado...</option>
      <option v-for="option in store.allowedResults" :key="option" :value="option">
        {{ option }}
      </option>
    </select>
  </div>
</template>

<script setup lang="ts">
import { useGenericFormStore } from '@/stores/genericFormStore'

const store = useGenericFormStore()
</script>
