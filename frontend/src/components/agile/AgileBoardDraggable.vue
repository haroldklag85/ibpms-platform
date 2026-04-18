<template>
  <div class="h-full flex flex-col bg-slate-100 overflow-hidden w-full">
    
    <!-- Sprint Header (Active/Planned Sprint) -->
    <div class="p-4 bg-white border-b border-slate-200 flex justify-between items-center shadow-sm z-10 shrink-0">
      <div>
        <h2 class="text-lg font-bold text-slate-800">{{ activeSprint?.name || 'Seleccione un Sprint' }}</h2>
        <p class="text-xs text-slate-500 mt-1" v-if="activeSprint">{{ activeSprint.startDate }} - {{ activeSprint.endDate }}</p>
      </div>
      <div>
         <span 
            v-if="activeSprint"
            class="px-3 py-1 text-xs font-bold rounded-full"
            :class="activeSprint.status === 'ACTIVE' ? 'bg-indigo-100 text-indigo-700' : 'bg-slate-100 text-slate-700'"
         >
            {{ activeSprint.status }}
         </span>
      </div>
    </div>

    <!-- Kanban Columns -->
    <div v-if="activeSprint" class="flex-1 overflow-x-auto p-6 flex gap-6 items-start">
       <!-- Columna To Do -->
       <div class="w-80 shrink-0 bg-slate-200/50 rounded-xl flex flex-col max-h-full">
         <h4 class="px-4 py-3 font-semibold text-slate-700 text-sm border-b border-slate-300">TODO</h4>
         <draggable 
           class="flex-1 p-3 overflow-y-auto space-y-3 min-h-[200px]"
           :list="sprintItems"
           group="agile-items"
           item-key="id"
           @change="onDragChange"
         >
           <template #item="{ element }">
             <div class="bg-white border text-sm font-medium border-slate-200 rounded shadow-sm p-3 cursor-grab hover:border-indigo-300 transition-colors">
               <div class="flex justify-between items-start mb-2">
                 <span class="text-xs font-bold px-1.5 py-0.5 rounded" :class="typeBadgeClass(element.type)">{{ element.type }}</span>
                 <span class="text-[10px] text-slate-400 font-mono">#{{ element.id.slice(-4) }}</span>
               </div>
               <p class="text-slate-800 leading-snug mb-3">
                 {{ element.title }}
               </p>
               <AgileTagCreator :item-id="element.id" :current-tags="element.tags" />
               <div class="mt-3 border-t border-slate-100 pt-2 flex justify-between items-center">
                 <AssigneeMultiSelect :item-id="element.id" :current-assignees="element.assignees" />
                 <span v-if="element.storyPoints" class="bg-indigo-50 text-indigo-700 rounded-full w-6 h-6 flex items-center justify-center text-xs font-bold">{{ element.storyPoints }}</span>
               </div>
             </div>
           </template>
         </draggable>
       </div>
    </div>

    <!-- Empty State / No Sprints -->
    <div v-else class="flex-1 flex flex-col items-center justify-center text-slate-500">
       <span class="material-symbols-outlined text-[48px] mb-4 text-slate-300">view_kanban</span>
       <h4 class="text-lg font-medium text-slate-600">No hay Sprint Activo</h4>
       <p class="text-sm mt-1">Cree un sprint en la configuración del Hub para comenzar a planificar.</p>
    </div>

  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import draggable from 'vuedraggable';
import { useAgileStore } from '@/stores/agileStore';
import { ItemType } from '@/types/agile';
import AgileTagCreator from './AgileTagCreator.vue';
import AssigneeMultiSelect from './AssigneeMultiSelect.vue';

const agileStore = useAgileStore();

// Usamos el unico sprint como mock, en prod sería el activo o seleccionado
const activeSprint = computed(() => {
  return agileStore.sprints && agileStore.sprints.length > 0 ? agileStore.sprints[0] : null;
});

const sprintItems = computed(() => {
  if (!activeSprint.value) return [];
  // For UI MVP, we show all sprint items in this single column wrapper,
  // Full Kanban demands sorting by element.status (TO_DO, PROGRESS)
  return agileStore.backlogItems.filter(item => item.sprintId === activeSprint.value!.id);
});

const onDragChange = (evt: any) => {
  if (evt.added && activeSprint.value) {
    const item = evt.added.element;
    // Bind to this sprint
    agileStore.moveItemToSprint(item.id, activeSprint.value.id);
  }
};

const typeBadgeClass = (type: ItemType) => {
  switch(type) {
    case ItemType.EPIC: return 'bg-purple-100 text-purple-800';
    case ItemType.STORY: return 'bg-green-100 text-green-800';
    case ItemType.BUG: return 'bg-red-100 text-red-800';
    default: return 'bg-gray-100 text-gray-800';
  }
};
</script>

<style scoped>
.sortable-ghost {
  opacity: 0.5;
  border: 1px dashed #6366f1;
}
</style>
