<template>
  <div class="absolute inset-0 bg-white flex flex-col overflow-hidden">
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

          <label class="flex items-center cursor-pointer border-l pl-4 border-gray-300">
            <div class="relative">
              <input type="checkbox" v-model="agileStore.showCompleted" class="sr-only" />
              <div class="block bg-gray-200 w-10 h-6 rounded-full border border-gray-300"></div>
              <div class="dot absolute left-1 top-1 bg-white w-4 h-4 rounded-full transition" :class="{'transform translate-x-4 bg-indigo-600': agileStore.showCompleted}"></div>
            </div>
            <div class="ml-2 text-xs font-semibold text-gray-700" title="Mostrar Completadas">Mostrar Completadas</div>
          </label>

          <!-- CA-12: Link Saltar al Tablero -->
          <router-link :to="'/admin/projects/kanban/' + (agileStore.currentProject?.id || 'PROJ-DEFAULT')" class="ml-2 text-indigo-600 hover:text-indigo-800 text-sm font-semibold flex items-center gap-1">
            Saltar al Tablero <span class="material-symbols-outlined text-[16px]">arrow_forward</span>
          </router-link>

          <button @click="initBoard" class="ml-2 px-3 py-1.5 bg-white border border-gray-300 rounded shadow-sm text-sm font-semibold hover:bg-gray-50 flex items-center gap-1 text-slate-700">
            <span class="material-symbols-outlined text-[16px]">sync</span> Refrescar
          </button>
          
          <button v-if="agileStore.currentProject?.status !== 'CLOSED'" @click="showCloseDialog = true" class="ml-2 px-3 py-1.5 bg-red-50 border border-red-300 rounded shadow-sm text-sm font-semibold text-red-700 hover:bg-red-100 flex items-center gap-1">
            <span class="material-symbols-outlined text-[16px]">block</span> Cerrar Proyecto
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
    
    <!-- Dialog Cerrar Proyecto -->
    <div v-if="showCloseDialog" class="fixed inset-0 bg-black/50 z-[10002] flex items-center justify-center" role="dialog" aria-modal="true">
       <div class="bg-white rounded-lg shadow-xl max-w-md w-full p-6">
         <h3 class="text-lg font-bold text-gray-900 mb-2">Cerrar Proyecto</h3>
         <p class="text-sm text-gray-600 mb-6">¿Está seguro que desea cerrar el proyecto? Las tareas pendientes serán canceladas y el proyecto pasará a modo solo lectura.</p>
         <div class="flex justify-end gap-3">
           <button @click="showCloseDialog = false" class="px-4 py-2 bg-white border border-gray-300 rounded text-sm font-semibold text-gray-700 hover:bg-gray-50">Cancelar</button>
           <button @click="closeProject" class="px-4 py-2 bg-red-600 rounded text-sm font-semibold text-white hover:bg-red-700">Confirmar</button>
         </div>
       </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
import AgileBacklogList from '@/components/agile/AgileBacklogList.vue';
import AgileBoardDraggable from '@/components/agile/AgileBoardDraggable.vue';
import { useAgileStore } from '@/stores/agileStore';

const route = useRoute();
const agileStore = useAgileStore();
const showCloseDialog = ref(false);

const closeProject = async () => {
    if(!agileStore.currentProject) return;
    try {
        await axios.post(`/api/v1/projects/${agileStore.currentProject.id}/close`);
        showCloseDialog.value = false;
        initBoard();
    } catch(e: any) {
        // Ignored or handled by global interceptors
    }
};

const initBoard = () => {
    // Project ID usually parsed from route param 
    // e.g. /admin/projects/agile-hub/:projectId
    const currentProjectId = route.params.projectId as string || 'PROJ-DEFAULT';
    
    // In a real V1 it fetches real data, here we emulate the mocked initialization
    agileStore.fetchProjectBoard(currentProjectId).catch(() => {
        // Mock Payload for Standalone mode fallback:
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
    });
};

onMounted(() => {
   initBoard();
});

// CA-7: Portfolio View watcher
watch(() => agileStore.isPortfolioMode, async (newVal) => {
    if (newVal) {
        try {
            agileStore.isLoading = true;
            const res = await axios.get('/api/v1/agile/portfolio');
            // Simplified portfolio load logic
            if (res.data && res.data.backlogItems) {
                agileStore.backlogItems = res.data.backlogItems;
            }
        } catch (e) {
            console.error('Error fetching portfolio view:', e);
        } finally {
            agileStore.isLoading = false;
        }
    } else {
        initBoard();
    }
});
</script>

<style scoped>
.wbs-standalone-badge {
   color: #b91c1c; 
   letter-spacing: 0.05em;
}
</style>
