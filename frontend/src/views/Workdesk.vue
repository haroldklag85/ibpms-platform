<template>
  <div class="h-full flex flex-col relative" v-cloak>
    <!-- Overlay Cargando Global -->
    <div v-if="store.isLoading" class="absolute inset-0 bg-white/70 flex items-center justify-center z-50 rounded-xl">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
    </div>

    <!-- ═══════════ Toast Notifications ═══════════ -->
    <Transition name="toast-slide">
      <div v-if="toastSuccess" class="fixed top-4 right-4 z-[100] bg-green-600 text-white px-5 py-3 rounded-lg shadow-xl flex items-center space-x-3 animate-pulse">
        <svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
        <span class="text-sm font-medium">{{ toastSuccess }}</span>
        <button @click="clearToasts" class="ml-2 text-green-200 hover:text-white">&times;</button>
      </div>
    </Transition>

    <div class="flex justify-between items-center mb-6">
      <div>
         <h2 class="text-2xl font-bold text-gray-900 dark:text-white">Bandeja Unificada (Hybrid Workdesk)</h2>
         <p class="text-sm text-gray-500 dark:text-gray-400">Epic 1 (US-001) - Vista consolidada de Procesos y Agilidad</p>
      </div>
      
      <!-- Filtros Rápidos y Refresh -->
      <div class="flex items-center space-x-3">
        <button @click="loadData" class="p-2 bg-white border border-gray-300 dark:bg-gray-800 dark:border-gray-600 rounded shadow-sm hover:bg-gray-50 dark:hover:bg-gray-700 transition" title="Refrescar Inbox">
           <svg class="w-5 h-5 text-gray-600 dark:text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path></svg>
        </button>
      </div>
    </div>

    <!-- Error Bar -->
    <div v-if="store.isError" class="bg-red-50 border border-red-200 p-4 mb-4 rounded-lg shadow-sm flex items-start">
      <svg class="w-5 h-5 text-red-500 mt-0.5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
      <p class="text-red-700 font-medium text-sm">{{ store.errorMessage }}</p>
    </div>

    <!-- Grid Layout para la Bandeja Vertical -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 flex-1 overflow-hidden">
      
      <!-- Listado de Tareas (Panel Izquierdo/Central) -->
      <div class="lg:col-span-2 overflow-y-auto pr-2 space-y-4 pb-10">
        
        <div v-if="store.items.length === 0 && !store.isLoading" class="text-center py-20 bg-white dark:bg-gray-800 border-2 border-dashed border-gray-300 dark:border-gray-700 rounded-xl">
           <div class="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-indigo-50 dark:bg-indigo-900/30">
               <svg class="h-8 w-8 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" /></svg>
           </div>
           <p class="mt-4 text-gray-500 dark:text-gray-400 font-medium">Bandeja Vacía. Eres libre.</p>
        </div>

        <!-- Renderizado Dinámico de Tareas CQRS -->
        <div 
           v-for="task in store.items" 
           :key="task.unifiedId" 
           class="bg-white dark:bg-gray-800 p-5 rounded-xl border-l-4 shadow-sm hover:shadow-md transition cursor-pointer flex flex-col sm:flex-row gap-4"
           :class="getSlaBorderColor(task.slaExpirationDate)"
        >
          <!-- Indicador de Sistema (Camunda vs Kanban) -->
          <div class="flex-shrink-0 flex items-center justify-center w-12 h-12 rounded-full border border-gray-100 dark:border-gray-700 bg-gray-50 dark:bg-gray-900 shadow-inner">
             <span v-if="task.sourceSystem === 'BPMN'" title="Camunda BPM" class="text-xl">⚡</span>
             <span v-else-if="task.sourceSystem === 'KANBAN'" title="Agile Kanban" class="text-xl">📋</span>
             <span v-else class="text-xl">📦</span>
          </div>

          <div class="flex-1">
            <div class="flex flex-wrap justify-between items-start gap-2 mb-2">
              <div class="flex items-center space-x-2 flex-wrap gap-y-2">
                
                <!-- Tag Source System -->
                <span :class="[
                  'px-2 py-0.5 text-xs font-bold rounded-md uppercase tracking-wide',
                  task.sourceSystem === 'BPMN' ? 'bg-purple-100 text-purple-800 dark:bg-purple-900/30 dark:text-purple-400' : 'bg-cyan-100 text-cyan-800 dark:bg-cyan-900/30 dark:text-cyan-400'
                ]">
                  {{ task.sourceSystem }}
                </span>
                
                <!-- SLA SEMAFORO -->
                <span :class="['px-2 py-0.5 text-xs font-bold rounded-md flex items-center space-x-1', getSlaBadgeClass(task.slaExpirationDate)]">
                   <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                   <span>{{ getSlaRelativeTime(task.slaExpirationDate) }}</span>
                </span>
                
              </div>
              <span class="text-xs text-gray-500 font-mono bg-gray-100 dark:bg-gray-700 px-2 py-1 rounded">ID: {{ task.originalTaskId }}</span>
            </div>
            
            <h3 class="text-lg font-bold text-gray-900 dark:text-white mt-1">{{ task.title }}</h3>
            <p class="text-gray-500 dark:text-gray-400 text-sm mt-1">
              Asignado a: <span class="font-semibold text-gray-700 dark:text-gray-300">{{ task.assignee || 'Sin Asignar (Grupal)' }}</span>
            </p>
            
            <div class="flex items-center mt-4">
               <button @click.stop="mockOpenTask(task)" class="px-4 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-medium rounded-lg shadow-sm transition">
                 Atender Tarea
               </button>
            </div>
          </div>
        </div>

        <!-- Paginador -->
        <div v-if="store.pageInfo.totalElements > store.pageInfo.pageSize" class="flex justify-between items-center bg-white dark:bg-gray-800 p-4 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700">
           <span class="text-sm text-gray-500 dark:text-gray-400">Total: {{ store.pageInfo.totalElements }} (Pág. {{ store.pageInfo.pageNumber + 1 }})</span>
           <div class="flex space-x-2">
              <button 
                 :disabled="store.pageInfo.pageNumber === 0" 
                 @click="store.fetchGlobalInbox(store.pageInfo.pageNumber - 1, store.pageInfo.pageSize)"
                 class="px-3 py-1 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 disabled:opacity-50 text-sm font-medium text-gray-700 dark:text-gray-300 transition"
              >Anterior</button>
              <button 
                 :disabled="(store.pageInfo.pageNumber + 1) * store.pageInfo.pageSize >= store.pageInfo.totalElements" 
                 @click="store.fetchGlobalInbox(store.pageInfo.pageNumber + 1, store.pageInfo.pageSize)"
                 class="px-3 py-1 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 disabled:opacity-50 text-sm font-medium text-gray-700 dark:text-gray-300 transition"
              >Siguiente</button>
           </div>
        </div>

      </div>

      <!-- Panel Derecho Auxiliar (Métricas Globales) -->
      <div class="hidden lg:block bg-white dark:bg-gray-800 p-6 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 h-fit sticky top-0">
        <h4 class="font-bold text-gray-800 dark:text-white mb-4 border-b border-gray-200 dark:border-gray-700 pb-2">Resumen Operativo</h4>
        <ul class="space-y-4 text-sm text-gray-600 dark:text-gray-400">
          <li class="flex justify-between items-center">
            <span>Volumen Total Global:</span> 
            <span class="font-bold text-gray-900 dark:text-white bg-gray-100 dark:bg-gray-700 px-2 py-0.5 rounded">{{ store.pageInfo.totalElements }}</span>
          </li>
          <li class="flex justify-between items-center text-red-600 dark:text-red-400">
            <span>Tareas Vencidas (SLA):</span> 
            <span class="font-bold bg-red-100 dark:bg-red-900/30 px-2 py-0.5 rounded">{{ countExpiredSLA() }}</span>
          </li>
          <li class="flex justify-between items-center text-yellow-600 dark:text-yellow-400">
            <span>Por expirar (< 24h):</span> 
            <span class="font-bold bg-yellow-100 dark:bg-yellow-900/30 px-2 py-0.5 rounded">{{ countWarningSLA() }}</span>
          </li>
        </ul>
        
        <div class="mt-8 bg-indigo-50 dark:bg-indigo-900/20 p-4 rounded-lg border border-indigo-100 dark:border-indigo-800">
          <p class="text-xs text-indigo-800 dark:text-indigo-300 mb-2 font-bold tracking-wider">📡 STATUS MODELO CQRS</p>
          <p class="text-sm text-indigo-900 dark:text-indigo-200">Sincronización Eventual: <span class="font-bold text-green-600 dark:text-green-400 animate-pulse">Online</span></p>
          <p class="text-xs text-indigo-500 mt-2">BPMN y KANBAN convergidos.</p>
        </div>
      </div>

    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useWorkdeskStore } from '@/stores/useWorkdeskStore';

const store = useWorkdeskStore();
const toastSuccess = ref('');

const loadData = async () => {
    await store.fetchGlobalInbox(0, 50);
};

const mockOpenTask = (task: any) => {
    toastSuccess.value = `Work in progress: Abriendo tarea ${task.unifiedId}...`;
    setTimeout(() => { toastSuccess.value = ''; }, 3000);
}

const clearToasts = () => {
    toastSuccess.value = '';
}

// ==========================================
// Logica Semáforo SLA (Ejecutada sobre el Front AC-1)
// ==========================================
const getSlaStatus = (isoString?: string) => {
    if(!isoString) return 'OK';
    
    const flag = new Date(isoString).getTime();
    const now = Date.now();
    const diffHours = (flag - now) / (1000 * 60 * 60);

    if(diffHours < 0) return 'EXPIRED';
    if(diffHours <= 24) return 'WARNING';
    return 'OK';
};

const getSlaBorderColor = (isoString?: string) => {
    const st = getSlaStatus(isoString);
    if(st === 'EXPIRED') return 'border-l-red-500';
    if(st === 'WARNING') return 'border-l-yellow-400';
    return 'border-l-green-500 dark:border-l-green-500/70 border-l-gray-300';
};

const getSlaBadgeClass = (isoString?: string) => {
    const st = getSlaStatus(isoString);
    if(st === 'EXPIRED') return 'bg-red-100 text-red-800 dark:bg-red-900/40 dark:text-red-400 animate-pulse';
    if(st === 'WARNING') return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/40 dark:text-yellow-400';
    return 'bg-green-100 text-green-800 dark:bg-green-900/40 dark:text-green-400';
};

const getSlaRelativeTime = (isoString?: string) => {
    if(!isoString) return 'Sin SLA Expiración';
    
    const flag = new Date(isoString).getTime();
    const now = Date.now();
    const diffHours = (flag - now) / (1000 * 60 * 60);
    const diffDays = diffHours / 24;

    if (diffHours < 0) return `Vencido hace ${Math.abs(Math.round(diffHours))} hrs`;
    if (diffHours < 24) return `Vence en ${Math.round(diffHours)} hrs`;
    return `Vence en ${Math.round(diffDays)} días`;
};

// Summary Logic
const countExpiredSLA = () => {
    return store.items.filter(i => getSlaStatus(i.slaExpirationDate) === 'EXPIRED').length;
};
const countWarningSLA = () => {
    return store.items.filter(i => getSlaStatus(i.slaExpirationDate) === 'WARNING').length;
};

onMounted(() => {
   loadData();
});
</script>

<style scoped>
[v-cloak] {
  display: none;
}

.toast-slide-enter-active,
.toast-slide-leave-active {
  transition: all 0.3s ease;
}
.toast-slide-enter-from {
  opacity: 0;
  transform: translateX(100px);
}
.toast-slide-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}
</style>
