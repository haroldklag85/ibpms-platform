<template>
  <div :class="mode === 'print' ? 'print-mode p-4 bg-white' : 'audit-mode p-6 bg-gray-50 rounded-xl shadow-inner border border-gray-200'">
      
      <!-- Encabezado contextual según modo -->
      <div v-if="mode === 'audit'" class="mb-6 pb-4 border-b border-gray-300 flex justify-between items-center">
          <div>
              <h2 class="text-xl font-bold text-gray-800 flex items-center gap-2">
                  <span class="text-indigo-600">🛡️</span> Vista de Auditoría (CA-93)
              </h2>
              <p class="text-sm text-gray-500 mt-1">Snapshot inmutable. Cualquier intento de inyección es trackeado.</p>
          </div>
          <div v-if="metadata" class="text-right text-xs bg-indigo-50 p-2 rounded border border-indigo-100">
              <p><strong>Actor:</strong> {{ metadata.user }}</p>
              <p><strong>Trace:</strong> {{ metadata.traceId }}</p>
              <p><strong>Fecha:</strong> {{ new Date(metadata.timestamp).toLocaleString() }}</p>
          </div>
      </div>

      <div v-else-if="mode === 'print'" class="mb-4 pb-2 border-b border-gray-400">
          <h2 class="text-2xl font-bold text-black uppercase tracking-wide">Reporte de Expediente</h2>
      </div>

      <!-- Renderizador estricto de solo lectura -->
      <div class="space-y-6">
          <WorkdeskReadOnlyField 
             v-for="(node, idx) in schema" 
             :key="node.id || idx"
             :node="node"
             :formData="formData"
             :mode="mode"
          />
      </div>
  </div>
</template>

<script setup lang="ts">
import WorkdeskReadOnlyField from './WorkdeskReadOnlyField.vue';

defineProps<{
   schema: any[],
   formData: Record<string, any>,
   mode: 'audit' | 'print',
   metadata?: {
       user: string,
       traceId: string,
       timestamp: number
   }
}>();
</script>

<style scoped>
@media print {
   .print-mode {
       page-break-inside: avoid;
   }
}
</style>
