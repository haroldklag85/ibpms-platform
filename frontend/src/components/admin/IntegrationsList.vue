<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
    <div class="p-4 border-b bg-gray-50 flex justify-between items-center">
      <h3 class="font-bold text-gray-800">Directorio de Webhooks Activos</h3>
      <button @click="$emit('add-new')" class="px-4 py-2 bg-ibpms-brand text-white text-sm font-medium rounded shadow hover:bg-blue-600 transition">
        + Nuevo Webhook
      </button>
    </div>

    <!-- Tabla Data Grid -->
    <div class="overflow-x-auto">
      <table class="w-full text-left text-sm text-gray-600">
        <thead class="bg-gray-100 text-gray-700 uppercase text-xs font-semibold">
          <tr>
            <th class="px-4 py-3">Nombre</th>
            <th class="px-4 py-3">Dirección</th>
            <th class="px-4 py-3">Proceso Asignado</th>
            <th class="px-4 py-3">URL / Evento</th>
            <th class="px-4 py-3 text-center">Estado</th>
            <th class="px-4 py-3 text-right">Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="items.length === 0">
             <td colspan="6" class="text-center py-8 text-gray-500 italic">No hay integraciones configuradas actualmente.</td>
          </tr>
          
          <tr v-for="hook in items" :key="hook.id" class="border-b hover:bg-gray-50 transition border-gray-100">
            <td class="px-4 py-3 font-medium text-gray-900">{{ hook.name }}</td>
            <td class="px-4 py-3">
               <span 
                 class="px-2 py-1 text-xs font-bold rounded"
                 :class="hook.direction === 'INBOUND' ? 'bg-purple-100 text-purple-700' : 'bg-orange-100 text-orange-700'"
               >
                 {{ hook.direction }}
               </span>
            </td>
            <td class="px-4 py-3 font-mono text-xs">{{ hook.processDefinitionKey || 'GLOBAL' }}</td>
            <td class="px-4 py-3 font-mono text-xs text-ibpms-brand truncate max-w-xs" :title="hook.targetUrl">
               {{ hook.targetUrl || hook.triggerEvent || 'N/A' }}
            </td>
            <td class="px-4 py-3 text-center">
               <span class="w-2 h-2 rounded-full inline-block mr-1" :class="hook.status === 'ACTIVE' ? 'bg-green-500' : 'bg-red-500'"></span>
               {{ hook.status }}
            </td>
            <td class="px-4 py-3 text-right space-x-2">
              <button @click="copyToken(hook.secretToken)" title="Copiar Token" class="text-gray-400 hover:text-ibpms-brand transition text-base">📋</button>
              <button class="text-gray-400 hover:text-red-500 transition text-base" title="Deshabilitar">🛑</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { PropType } from 'vue';
import type { WebhookConfig } from '@/types/Integration';

const props = defineProps({
  items: {
    type: Array as PropType<WebhookConfig[]>,
    default: () => []
  }
});

const emit = defineEmits(['add-new']);

const copyToken = async (token: string) => {
  try {
    if(navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(token);
      alert('Token/Secreto copiado al portapapeles de manera segura.');
    } else {
      // Fallback
      prompt('Copia manualmente:', token);
    }
  } catch (err) {
    console.error('API Clipboard falló', err);
  }
};
</script>
