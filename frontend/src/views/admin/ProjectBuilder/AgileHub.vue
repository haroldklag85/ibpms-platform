<template>
  <div class="h-full bg-white flex flex-col overflow-hidden">
    <!-- Header -->
    <div class="h-16 shrink-0 border-b border-gray-200 bg-white flex justify-between items-center px-6">
       <div>
         <div class="flex items-center gap-2">
           <div class="w-8 h-8 rounded shrink-0 bg-indigo-600 flex justify-center items-center">
             <span class="material-symbols-outlined text-white text-[18px]">account_tree</span>
           </div>
           <div>
             <h1 class="text-xl font-bold text-gray-900 leading-tight">
               {{ agileStore.currentProject?.name || 'Agile Hub Workspace' }}
             </h1>
             <p class="text-xs text-gray-500 font-medium tracking-wide wbs-standalone-badge">
               LOCKED: STANDALONE MODE (NO WBS)
             </p>
           </div>
         </div>
       </div>
       <div class="flex gap-4 items-center">
          <!-- CA-7: Portfolio Toggle -->
          <label class="flex items-center cursor-pointer">
            <div class="relative">
              <input type="checkbox" v-model="agileStore.isPortfolioMode" class="sr-only" />
              <div class="block bg-gray-200 w-10 h-6 rounded-full border border-gray-300"></div>
              <div class="dot absolute left-1 top-1 bg-white w-4 h-4 rounded-full transition" :class="{'transform translate-x-4 bg-indigo-600': agileStore.isPortfolioMode}"></div>
            </div>
            <div class="ml-2 text-xs font-semibold text-gray-700">Portafolio</div>
          </label>

          <!-- CA-8: Smart Archive Toggle -->
          <label class="flex items-center cursor-pointer border-l pl-4 border-gray-300">
            <div class="relative">
              <input type="checkbox" v-model="agileStore.isArchiveSimulated" class="sr-only" />
              <div class="block bg-gray-200 w-10 h-6 rounded-full border border-gray-300"></div>
              <div class="dot absolute left-1 top-1 bg-white w-4 h-4 rounded-full transition" :class="{'transform translate-x-4 bg-indigo-600': agileStore.isArchiveSimulated}"></div>
            </div>
            <div class="ml-2 text-xs font-semibold text-gray-700" title="Ocultar Inactivos">Smart Archive</div>
          </label>

          <button @click="initBoard" class="ml-2 px-3 py-1.5 bg-white border border-gray-300 rounded shadow-sm text-sm font-semibold hover:bg-gray-50 flex items-center gap-1 text-slate-700">
            <span class="material-symbols-outlined text-[16px]">sync</span> Refrescar
          </button>
       </div>
    </div>

    <!-- Spinner General -->
    <div v-if="agileStore.isLoading && agileStore.backlogItems.length === 0" class="flex-1 flex justify-center items-center">
       <span class="material-symbols-outlined animate-spin text-4xl text-indigo-500">sync</span>
    </div>

    <!-- Layout Dividido (Side-by-side) -->
    <div v-else class="flex-1 flex overflow-hidden">
       <!-- Columna Izquierda: Backlog Pila -->
       <AgileBacklogList />
       
       <!-- Columna Derecha: Board Kano/Sprint -->
       <AgileBoardDraggable class="flex-1" />
    </div>

  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useRoute } from 'vue-router';
import AgileBacklogList from '@/components/agile/AgileBacklogList.vue';
import AgileBoardDraggable from '@/components/agile/AgileBoardDraggable.vue';
import { useAgileStore } from '@/stores/agileStore';

const route = useRoute();
const agileStore = useAgileStore();

const initBoard = () => {
    // Project ID usually parsed from route param 
    // e.g. /admin/projects/agile-hub/:projectId
    const currentProjectId = route.params.projectId as string || 'PROJ-DEFAULT';
    
    // In a real V1 it fetches real data, here we emulate the mocked initialization
    // agileStore.fetchProjectBoard(currentProjectId);

    // Mock Payload for Standalone mode:
    agileStore.$patch({
       currentProject: { id: currentProjectId, key: 'CRM', name: 'CRM Modernization' },
       sprints: [
         { id: 'sprint-1', projectId: currentProjectId, name: 'Sprint 1', startDate: '2026-04-18', endDate: '2026-05-02', status: 'ACTIVE' }
       ],
       backlogItems: [
         { id: 'item-101', title: 'Implementar SSO via Azure AD', type: 'STORY', status: 'TO_DO',
           storyPoints: 8, sprintId: null, tags: [], assignees: [], wbsReferenceId: undefined },
         { id: 'item-102', title: 'Corregir Fuga de Memoria en Redis', type: 'BUG', status: 'TO_DO',
           storyPoints: 3, sprintId: null, tags: [{id:'tg0', label:'Backend', color:'#ef4444'}], assignees: [] },
         { id: 'item-201', title: 'Diseño Base del Agile Hub UI', type: 'STORY', status: 'IN_PROGRESS',
           storyPoints: 5, sprintId: 'sprint-1', tags: [], assignees: [{userId:'u-1', name:'Alfonso Gómez', email:'alfonso@ibpms.corp'}] }
       ],
       isLoading: false
    });
};

onMounted(() => {
   initBoard();
});
</script>

<style scoped>
.wbs-standalone-badge {
   color: #b91c1c; 
   letter-spacing: 0.05em;
}
</style>
