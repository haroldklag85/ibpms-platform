<template>
  <div v-if="dmnStore.isRateLimited" class="bg-yellow-100 border-l-4 border-yellow-500 text-yellow-800 p-4 mb-4 font-mono text-sm shadow rounded flex flex-col justify-center items-center h-[120px]">
    <div class="flex items-center text-base mb-2">
      <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-yellow-800" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
      </svg>
      <span>Has superado el límite de aceleración de generación (HTTP 429).</span>
    </div>
    <div class="font-bold text-2xl text-yellow-900 tracking-tight">
      {{ dmnStore.rateLimitSeconds }} s
    </div>
  </div>
  
  <div v-if="dmnStore.requiresFallback" class="bg-blue-100 border-l-4 border-blue-500 text-blue-800 p-4 mb-4 font-sans text-sm shadow rounded">
    <div class="flex items-center">
      <span class="mr-2">⏳</span>
      <span>El LLM NLP está tardando más de lo usual (Timeout 504). Por favor, intenta de nuevo o escoge una meta más pequeña.</span>
      <button @click="dmnStore.requiresFallback = false" class="ml-auto underline font-bold hover:text-blue-900">Aceptar</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useDmnStore } from '@/stores/useDmnStore';
const dmnStore = useDmnStore();
</script>
