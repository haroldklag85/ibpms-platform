<template>
  <div class="flex flex-col flex-shrink-0 w-80 bg-gray-50 rounded-lg shadow-inner overflow-hidden border border-gray-200">
    
    <!-- Column Header -->
    <div class="px-4 py-3 flex justify-between items-center border-b" :class="column.color">
      <h3 class="font-bold text-gray-800">{{ column.title }}</h3>
      <span class="bg-white/50 text-gray-800 text-xs font-bold px-2 py-1 rounded-full">{{ items.length }}</span>
    </div>

    <!-- Drop Zone -->
    <div class="p-3 flex-1 overflow-y-auto min-h-[500px]">
      <draggable 
        class="h-full space-y-3"
        :list="items" 
        item-key="id"
        group="kanban"
        ghost-class="opacity-50"
        @change="onChange"
      >
        <template #item="{ element }">
          <KanbanCard :item="element" />
        </template>
        <!-- Slot vacío si no hay items -->
        <template #footer v-if="items.length === 0">
           <div class="h-32 border-2 border-dashed border-gray-300 rounded flex items-center justify-center text-gray-400 text-sm">
             Arrastra un caso aquí
           </div>
        </template>
      </draggable>
    </div>

  </div>
</template>

<script setup lang="ts">
import { PropType } from 'vue';
import draggable from 'vuedraggable';
import type { KanbanColumnDef, KanbanItem } from '@/types/Kanban';
import KanbanCard from './KanbanCard.vue';

const props = defineProps({
  column: {
    type: Object as PropType<KanbanColumnDef>,
    required: true
  },
  items: {
    type: Array as PropType<KanbanItem[]>,
    required: true
  }
});

const emit = defineEmits(['itemMoved']);

const onChange = (evt: any) => {
  // Cuando VueDraggable suelta un item de otro grupo, emite un 'added'
  if (evt.added) {
    const movedItem: KanbanItem = evt.added.element;
    emit('itemMoved', { item: movedItem, newStatus: props.column.id });
  }
};
</script>
