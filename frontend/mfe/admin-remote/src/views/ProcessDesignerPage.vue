<template>
  <div class="flex h-screen bg-background">
    <!-- Panel Izquierdo: Chat de Inteligencia Artificial (Taller DMN) -->
    <aside class="w-80 border-r border-border bg-surface p-4 flex flex-col">
      <h2 class="text-lg font-bold text-text mb-4">IA DMN Taller</h2>
      <div class="flex-1 overflow-auto bg-gray-50 border border-border rounded p-2 mb-4 text-sm text-text-muted">
        Prompt: <em>"Si el demandante es 'Banco' rechazar automáticamente..."</em>
      </div>
      <button class="bg-primary hover:bg-blue-600 text-white font-bold py-2 px-4 rounded w-full transition">
        Generar Reglas DMN
      </button>
    </aside>

    <!-- Panel Central: Lienzo de Modelado (bpmn-js y dmn-js) -->
    <main class="flex-1 relative flex flex-col">
      <header class="bg-surface border-b border-border p-4 flex justify-between items-center">
        <h1 class="text-xl font-bold">Process Designer V1</h1>
        <div class="space-x-2">
          <button class="px-4 py-2 border border-border rounded text-text font-medium hover:bg-gray-50">BPMN</button>
          <button class="px-4 py-2 bg-primary text-white rounded font-medium hover:bg-blue-600">DMN</button>
          <button class="px-4 py-2 bg-success text-white rounded font-medium hover:bg-green-600">Deploy a Camunda</button>
        </div>
      </header>
      <!-- Contenedor del Canvas de Modeler -->
      <div ref="canvas" class="flex-1 bg-white relative" id="canvas">
        <div class="absolute inset-0 flex items-center justify-center text-text-muted">
           Lienzo listo para BPMN-JS / DMN-JS...
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
// Las librerías BpmnModeler y DmnModeler serán inicializadas aquí
const canvas = ref<HTMLElement | null>(null)
onMounted(() => {
  console.log('Canvas de Diseño Inicializado')
})
</script>
