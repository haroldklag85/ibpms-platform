<template>
  <div class="bg-white p-3 rounded shadow-sm border border-gray-200 cursor-move hover:shadow-md transition group">
    <div class="flex justify-between items-start mb-2">
      <span class="text-xs font-bold px-2 py-1 rounded" :class="priorityClass">
        {{ priorityLabel }}
      </span>
      <span v-if="item.assignee" class="text-xs bg-gray-100 text-gray-600 px-2 py-1 rounded-full font-medium" title="Asignado a">
        👤 {{ item.assignee }}
      </span>
    </div>
    
    <h4 class="text-sm font-bold text-gray-800 mb-2">{{ item.title }}</h4>
    <p class="text-xs text-gray-500 font-mono">ID: {{ item.id }}</p>

    <!-- Acciones Ocultas mostradas al hover -->
    <div class="mt-3 pt-2 border-t flex space-x-2 opacity-0 group-hover:opacity-100 transition-opacity">
      <button class="text-xs text-ibpms-brand hover:underline font-medium">✏️ Abrir</button>
      <button class="text-xs text-red-500 hover:underline font-medium">🗑️ Descartar</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, PropType } from 'vue';
import type { KanbanItem } from '@/types/Kanban';

const props = defineProps({
  item: {
    type: Object as PropType<KanbanItem>,
    required: true
  }
});

const priorityClass = computed(() => {
  if (!props.item.priority) return 'bg-gray-100 text-gray-700';
  return props.item.priority > 50 ? 'bg-red-100 text-red-700' : 'bg-blue-100 text-blue-700';
});

const priorityLabel = computed(() => {
  if (!props.item.priority) return 'NORM';
  return props.item.priority > 50 ? 'URG' : 'NORM';
});
</script>
