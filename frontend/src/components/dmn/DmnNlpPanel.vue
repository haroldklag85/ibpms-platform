<template>
  <div class="dmn-nlp-panel bg-white p-6 rounded-lg shadow-md border mb-6">
    <h3 class="text-lg font-bold mb-4">Prompt NLP a DMN (Copilot)</h3>
    <textarea 
      v-model="prompt" 
      class="w-full border rounded-md p-3 mb-4 focus:ring-2 focus:ring-indigo-500 disabled:opacity-50"
      rows="4"
      placeholder="Ej: Si la antiguedad es mayor a 5 años o el salario > 3000 -> APROBADO"
      :disabled="dmnStore.isGenerating"
    ></textarea>
    
    <button 
      @click="handleGenerate"
      :disabled="!prompt || dmnStore.isGenerating"
      class="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700 disabled:opacity-50 flex items-center"
    >
      <span v-if="dmnStore.isGenerating" class="mr-2">⏳ Generando...</span>
      <span v-else>✨ Generar Reglas DMN</span>
    </button>
    
    <!-- ERROR -->
    <div v-if="dmnStore.generationError" class="mt-4 p-3 bg-red-100 text-red-600 rounded">
      {{ dmnStore.generationError }}
    </div>

    <!-- SUCCESS XML RENDER -->
    <div v-if="dmnStore.generatedXml" class="mt-6 border-t pt-4">
      <div class="flex justify-between items-center mb-2">
        <h4 class="font-bold">XML DMN Resultante:</h4>
        
        <!-- Indicador de Confianza Visual -->
        <span class="px-3 py-1 rounded-full text-xs font-bold text-white shadow-sm" :class="confidenceClass">
          Confianza: {{ dmnStore.confidence }}%
        </span>
      </div>
      <pre class="bg-gray-900 text-green-400 p-4 rounded text-xs overflow-x-auto"><code>{{ dmnStore.generatedXml }}</code></pre>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useDmnStore } from '@/stores/useDmnStore';

const dmnStore = useDmnStore();
const prompt = ref('');

const handleGenerate = async () => {
    if (!prompt.value) return;
    try {
        await dmnStore.generateFromPrompt(prompt.value);
    } catch(e) {
        // Exception captured by store's generationError property
    }
};

const confidenceClass = computed(() => {
    const val = dmnStore.confidence;
    if (val >= 80) return 'bg-green-500';
    if (val >= 50) return 'bg-yellow-500 text-yellow-900';
    return 'bg-red-500';
});
</script>
