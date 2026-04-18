<template>
  <div class="mb-5">
    <label for="observations" class="block text-sm font-medium text-gray-700 mb-1">
      Observaciones / Notas del Operario <span class="text-red-500">*</span>
    </label>
    <textarea
      id="observations"
      v-model="store.observations"
      rows="4"
      class="block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
      :class="{ 'border-red-300': store.observations && (store.observations.length < 10 || store.observations.length > 2000) }"
      placeholder="Ingrese sus observaciones aquí (mínimo 10 caracteres)..."
    ></textarea>
    <div class="mt-1 flex justify-between text-xs">
      <span v-if="store.observations && store.observations.length < 10" class="text-red-500">Mínimo 10 caracteres permitidos.</span>
      <span v-else-if="store.observations && store.observations.length > 2000" class="text-red-500">Máximo 2000 caracteres permitidos.</span>
      <span v-else class="text-gray-500">&nbsp;</span>
      
      <span :class="{'text-red-500 font-bold': store.observations.length > 2000 || (store.observations.length > 0 && store.observations.length < 10), 'text-gray-500': store.observations.length <= 2000 && store.observations.length >= 10 || store.observations.length === 0}">
        {{ store.observations.length }} / 2000
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useGenericFormStore } from '@/stores/genericFormStore'

const store = useGenericFormStore()
</script>
