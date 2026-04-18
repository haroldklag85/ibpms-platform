<template>
  <div class="h-full flex flex-col bg-slate-50 border-r border-slate-200 w-80 shrink-0">
    <div class="p-4 border-b border-slate-200 bg-white flex justify-between items-center z-10 shrink-0">
      <h3 class="text-sm font-semibold text-slate-800">Backlog Global</h3>
      <span class="bg-indigo-100 text-indigo-700 px-2 py-0.5 rounded-full text-xs font-bold shadow-inner">{{ processedOrphanedItems.length }}</span>
    </div>
    <div class="px-3 py-2 bg-slate-100 border-b border-slate-200 shrink-0">
       <input 
          type="text" 
          v-model="searchDebounced"
          @input="onSearchInput"
          placeholder="Filtrar tickets..."
          class="w-full text-sm px-2 py-1.5 border border-slate-300 rounded shadow-sm focus:ring-1 focus:ring-indigo-500"
       />
    </div>

    <!-- CA-12: Virtualized DOM Rendering para +10000 Tickets -->
    <div class="flex-1 overflow-hidden relative">
      <RecycleScroller
        class="h-full absolute inset-0 scroller-padding"
        :items="processedOrphanedItems"
        :item-size="130"
        key-field="id"
        v-slot="{ item }"
      >
        <div class="px-3 py-1.5 h-full">
           <div class="bg-white border border-slate-200 rounded shadow-sm p-3 hover:border-indigo-400 hover:shadow transition-all relative group h-[118px] flex flex-col justify-between">
              <!-- CA-13: Stale Ticket Aura -->
              <div v-if="isStale(item)" class="absolute -top-1 -right-1 w-3 h-3 bg-amber-400 rounded-full shadow-sm animate-pulse" title="Ticket Rancio (>30 días inactivo)"></div>
              
              <div class="flex justify-between items-start mb-1">
                 <span class="text-[10px] font-bold px-1.5 rounded" :class="typeBadgeClass(item.type)">
                   {{ item.type }}
                 </span>
                 <span class="text-[10px] text-slate-400 font-mono">#{{ item.id.split('-').pop() }}</span>
              </div>
  
              <p class="text-[13px] text-slate-800 font-medium leading-tight mb-2 line-clamp-2" :title="item.title">
                 {{ item.title }}
              </p>
  
              <div class="mt-auto flex justify-between items-center border-t border-slate-100 pt-2">
                 <AssigneeMultiSelect :item-id="item.id" :current-assignees="item.assignees" />
                 <button @click="moveToActiveSprint(item.id)" class="opacity-0 group-hover:opacity-100 transition-opacity bg-indigo-50 hover:bg-indigo-100 text-indigo-700 text-[10px] px-2 py-1 rounded font-semibold border border-indigo-200">
                    Enviar al Sprint =>
                 </button>
              </div>
           </div>
        </div>
      </RecycleScroller>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useAgileStore } from '@/stores/agileStore';
import { ItemType, BacklogItem } from '@/types/agile';
import AssigneeMultiSelect from './AssigneeMultiSelect.vue';
import lodash from 'lodash-es';

const agileStore = useAgileStore();
const searchRaw = ref('');
const searchDebounced = ref('');

// CA-12: Debounced Filter
const onSearchInput = lodash.debounce((e: Event) => {
   searchRaw.value = (e.target as HTMLInputElement).value.toLowerCase();
}, 300);

const processedOrphanedItems = computed(() => {
  const filtered = agileStore.filteredBacklogItems.filter(i => !i.sprintId);
  if (!searchRaw.value) return filtered;
  return filtered.filter(i => i.title.toLowerCase().includes(searchRaw.value) || i.id.toLowerCase().includes(searchRaw.value));
});

// CA-13: Stale Ticket Logic
const isStale = (item: BacklogItem) => {
   if (!item || !item.updatedAt) return false;
   const diffTime = Math.abs(Date.now() - new Date(item.updatedAt).getTime());
   const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)); 
   return diffDays > 30;
};

// Sustituto de Drag & Drop para dominios virtualizados (Arquitectura aceptada en Handoff)
const moveToActiveSprint = (itemId: string) => {
   const activeSprint = agileStore.sprints[0];
   if (activeSprint) {
     agileStore.moveItemToSprint(itemId, activeSprint.id);
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
.scroller-padding {
  padding-bottom: 2rem;
}
/* Requerido para virtual-scroller height */
.vue-recycle-scroller {
  height: 100%;
}
</style>
