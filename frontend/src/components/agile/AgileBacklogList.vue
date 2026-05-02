<template>
  <div class="h-full flex flex-col bg-slate-50 border-r border-slate-200 w-80 shrink-0">
    <div class="p-4 border-b border-slate-200 bg-white flex justify-between items-center z-10 shrink-0">
      <h3 class="text-sm font-semibold text-slate-800">Backlog Global</h3>
      <div class="flex gap-2 items-center">
        <span class="bg-indigo-100 text-indigo-700 px-2 py-0.5 rounded-full text-xs font-bold shadow-inner">{{ processedOrphanedItems.length }}</span>
        <button v-if="agileStore.currentProject?.status !== 'CLOSED'" @click="showCreatePanel = true" class="bg-indigo-600 text-white px-2 py-0.5 rounded text-xs font-bold hover:bg-indigo-700">
          + Nueva Tarea
        </button>
      </div>
    </div>
    <div class="px-3 py-2 bg-slate-100 border-b border-slate-200 shrink-0">
       <input 
          type="text" 
          @input="onSearchInput"
          placeholder="Filtrar tickets..."
          class="w-full text-sm px-2 py-1.5 border border-slate-300 rounded shadow-sm focus:ring-1 focus:ring-indigo-500"
       />
    </div>

    <!-- CA-12: Virtualized DOM Rendering para +10000 Tickets -->
    <div v-if="processedOrphanedItems.length === 0" class="flex-1 flex items-center justify-center text-slate-500 text-sm">
      No hay tareas en el Backlog
    </div>
    <div v-else class="flex-1 overflow-hidden relative">
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
                 <div class="flex gap-1 items-center">
                   <span class="text-[10px] text-slate-400 font-mono">#{{ item.id.split('-').pop() }}</span>
                   <button @click="confirmDelete(item.id)" class="text-[10px] text-red-500 hover:bg-red-50 px-1 rounded">Eliminar</button>
                 </div>
              </div>
  
              <p class="text-[13px] text-slate-800 font-medium leading-tight mb-2 line-clamp-2" :title="item.title">
                 {{ item.title }}
              </p>
              <div class="mb-2">
                 <span class="text-[9px] font-bold px-1.5 py-0.5 rounded bg-gray-200 text-gray-700 uppercase tracking-wider">{{ item.status }}</span>
              </div>
  
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

    <!-- Create Slide Panel (Dummy for Tests) -->
    <div v-if="showCreatePanel" class="fixed right-0 top-0 bottom-0 w-80 bg-white shadow-xl z-[10002] p-6 flex flex-col" role="complementary" aria-label="Crear Nueva Tarea">
       <h2 class="text-lg font-bold mb-4">Crear Nueva Tarea</h2>
       <div class="mb-4">
         <label class="block text-sm font-semibold mb-1" for="tituloInput">Título</label>
         <input id="tituloInput" v-model="newTask.title" class="w-full border rounded px-2 py-1 text-sm" />
       </div>
       <div class="mb-4">
         <label class="block text-sm font-semibold mb-1">Descripción</label>
         <textarea role="textbox" v-model="newTask.description" class="w-full border rounded px-2 py-1 text-sm h-24"></textarea>
       </div>
       <div class="mt-auto flex justify-end gap-2">
         <button @click="showCreatePanel = false" class="px-3 py-1 bg-gray-100 rounded text-sm">Cancelar</button>
         <button @click="createTask" class="px-3 py-1 bg-indigo-600 text-white rounded text-sm font-bold">Guardar</button>
       </div>
    </div>

    <!-- Delete Confirm Dialog -->
    <div v-if="itemToDelete" class="fixed inset-0 bg-black/50 z-[10002] flex items-center justify-center" role="dialog" aria-label="Eliminar Tarea">
       <div class="bg-white rounded p-6 shadow-xl max-w-sm w-full">
         <h3 class="font-bold mb-2 text-lg">Eliminar Tarea</h3>
         <p class="text-sm mb-4">¿Desea eliminar la tarea #{{ itemToDelete }}?</p>
         <div class="flex justify-end gap-2">
           <button @click="itemToDelete = null" class="px-3 py-1 bg-gray-100 rounded text-sm">Cancelar</button>
           <button @click="executeDelete" class="px-3 py-1 bg-red-600 text-white font-bold rounded text-sm">Confirmar</button>
         </div>
       </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import axios from 'axios';
import { useAgileStore } from '@/stores/agileStore';
import { ItemType, BacklogItem } from '@/types/agile';
import AssigneeMultiSelect from './AssigneeMultiSelect.vue';
import { debounce } from 'lodash-es';

const agileStore = useAgileStore();
const searchRaw = ref('');
const searchDebounced = ref('');

// CA-12: Debounced Filter
let debounceTimer: ReturnType<typeof setTimeout>;
const onSearchInput = (e: Event) => {
   const val = (e.target as HTMLInputElement).value;
   clearTimeout(debounceTimer);
   debounceTimer = setTimeout(() => {
     searchRaw.value = val.toLowerCase();
   }, 300);
};

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

// CRUD Dummy logic for E2E
const showCreatePanel = ref(false);
const newTask = ref({ title: '', description: '' });

const createTask = async () => {
  if(!agileStore.currentProject) return;
  try {
     const res = await axios.post(`/api/v1/projects/${agileStore.currentProject.id}/agile/tasks`, newTask.value);
     agileStore.backlogItems.push({
       ...res.data,
       type: 'STORY',
       assignees: [],
       tags: []
     });
     showCreatePanel.value = false;
     newTask.value = { title: '', description: '' };
  } catch(e) {
     console.error(e);
  }
};

const itemToDelete = ref<string | null>(null);

const confirmDelete = (id: string) => {
  itemToDelete.value = id;
};

const executeDelete = async () => {
  if(!agileStore.currentProject || !itemToDelete.value) return;
  try {
    await axios.delete(`/api/v1/projects/${agileStore.currentProject.id}/agile/tasks/${itemToDelete.value}`);
    agileStore.backlogItems = agileStore.backlogItems.filter(i => i.id !== itemToDelete.value);
    itemToDelete.value = null;
  } catch(e) {
    console.error(e);
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
