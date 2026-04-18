<template>
  <div class="bg-white overflow-hidden shadow rounded-lg hover:shadow-md transition-shadow duration-300 border border-gray-200">
    <div class="px-4 py-5 sm:p-6 flex flex-col h-full">
      
      <!-- Card Header: Sender and SLA -->
      <div class="flex justify-between items-start mb-4">
        <div>
          <h3 class="text-sm font-medium text-gray-500 truncate" :title="task.sender">
            {{ task.sender }}
          </h3>
          <p class="text-xs text-gray-400 mt-1">
            {{ formatArrivalDate(task.receivedAt) }}
          </p>
        </div>
        <SlaIndicator :creation-date="task.receivedAt" :sla-deadline="task.slaDeadline" />
      </div>

      <!-- Card Body: Subject & Preview -->
      <div class="flex-grow border-t border-b border-gray-100 py-4 my-2">
        <h4 class="text-lg font-semibold text-gray-900 mb-2 truncate" :title="task.subject">
          {{ task.subject || 'Sin Asunto' }}
        </h4>
        <p class="text-sm text-gray-600 line-clamp-3">
          {{ task.payloadPreview }}
        </p>
        <div class="mt-3 flex items-center space-x-2">
          <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-indigo-100 text-indigo-800">
            Source: {{ task.source }}
          </span>
          <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-gray-100 text-gray-800">
            ID: {{ shortId }}
          </span>
        </div>
      </div>

      <!-- Card Footer: Fast Actions -->
      <div class="mt-auto pt-4 flex justify-end space-x-3">
        <button 
          type="button" 
          class="inline-flex items-center px-3 py-1.5 border border-transparent text-xs font-medium rounded shadow-sm text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500"
          @click="$emit('action', 'REJECT', task.id)"
        >
          Rechazar
        </button>
        <button 
          type="button" 
          class="inline-flex items-center px-3 py-1.5 border border-transparent text-xs font-medium rounded shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
          @click="$emit('action', 'APPROVE', task.id)"
        >
          Aprobar
        </button>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { PreTriageTask } from '@/types/intake';
import SlaIndicator from './SlaIndicator.vue';

const props = defineProps<{
  task: PreTriageTask;
}>();

defineEmits<{
  (e: 'action', type: 'APPROVE' | 'REJECT', id: string): void;
}>();

const shortId = computed(() => {
  return props.task.id.slice(-6);
});

const formatArrivalDate = (isoDate: string) => {
  // Simple format for demo purposes. 
  try {
    return new Intl.DateTimeFormat('es-CO', { 
      year: 'numeric', month: 'short', day: 'numeric',
      hour: '2-digit', minute: '2-digit'
    }).format(new Date(isoDate));
  } catch (e) {
    return isoDate;
  }
};
</script>
