<template>
  <div class="h-full flex flex-col relative bg-gray-50 font-['Inter']" v-cloak>
    <!-- Overlay Cargando Global -->
    <div v-if="store.isLoading" class="absolute inset-0 bg-white/70 flex items-center justify-center z-50 rounded-xl">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
    </div>

    <!-- Toast Notifications -->
    <Transition name="toast-slide">
      <div v-if="toastSuccess" class="fixed top-4 right-4 z-[100] bg-green-600 text-white px-5 py-3 rounded-lg shadow-xl flex items-center space-x-3 animate-pulse">
        <span class="material-symbols-outlined text-white text-xl">check_circle</span>
        <span class="text-sm font-medium">{{ toastSuccess }}</span>
        <button @click="clearToasts" class="ml-2 text-green-200 hover:text-white">&times;</button>
      </div>
    </Transition>

    <!-- Header Stitch Style -->
    <header class="min-h-[4rem] bg-white border-b border-gray-200 flex flex-wrap items-center justify-between px-6 z-30 flex-shrink-0 gap-4 py-3 xl:py-0">
      <div class="flex items-center gap-6">
        <div class="flex items-center gap-2">
          <span class="material-symbols-outlined text-indigo-600 text-2xl">balance</span>
          <h1 class="text-lg font-bold text-[#1e1b4b]">Bandeja Unificada <span class="text-gray-400 font-normal ml-1">Workdesk</span></h1>
        </div>
        
        <!-- Contenedor general de Filtros -->
        <div class="flex items-center gap-2">
           <!-- Delegación de Bandejas (Gap CA-4) -->
           <select 
              v-model="delegationFilter" 
              @change="loadData"
              class="bg-indigo-50 border border-indigo-100 text-indigo-600 text-sm rounded-md focus:ring-indigo-500 focus:border-indigo-500 block p-2 font-semibold hover:bg-indigo-100 cursor-pointer outline-none"
           >
             <option value="">Mis Tareas</option>
             <option value="TEAM">Tareas del Equipo</option>
           </select>

           <!-- Filtro Tipo (Procesos vs Proyectos) -->
           <select 
              v-model="typeFilter"
              @change="loadData"
              class="bg-white border border-gray-200 text-gray-600 text-sm rounded-md focus:ring-indigo-500 focus:border-indigo-500 block p-2 hover:bg-gray-50 cursor-pointer outline-none transition-colors"
           >
             <option value="">Todos los Tipos</option>
             <option value="BPMN">Procesos (BPMN)</option>
             <option value="KANBAN">Proyectos (Kanban)</option>
           </select>

           <!-- Filtro Nivel de SLA -->
           <select 
              v-model="slaFilter"
              @change="loadData"
              class="bg-white border border-gray-200 text-gray-600 text-sm rounded-md focus:ring-indigo-500 focus:border-indigo-500 block p-2 hover:bg-gray-50 cursor-pointer outline-none transition-colors"
           >
             <option value="">Cualquier Nivel SLA</option>
             <option value="EXPIRED">Vencido</option>
             <option value="WARNING">Urgente</option>
             <option value="OK">Normal</option>
           </select>
        </div>
      </div>

      <div class="flex-1 max-w-2xl px-2 xl:px-8 flex items-center gap-3">
        <!-- Búsqueda (Gap CA-2) -->
        <div class="relative flex-1 group">
          <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-xl font-light">search</span>
          <input 
            v-model="searchQuery"
            @input="onSearchInput"
            class="w-full bg-gray-50 border border-gray-200 rounded-lg py-1.5 pl-10 pr-4 text-sm focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 transition-all outline-none" 
            placeholder="Buscar por ID, título o asignado..." type="search"
          />
        </div>
        <button @click="loadData" class="flex items-center gap-2 px-3 py-1.5 text-sm font-medium text-gray-600 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 group" title="Refrescar Inbox">
          <span class="material-symbols-outlined text-lg group-hover:rotate-180 transition-transform duration-500">sync</span>
        </button>
      </div>

      <div class="flex items-center gap-4">
        <!-- CA-8 (Feature Toggle Oculto) -->
        <button v-if="FEATURE_FORCE_QUEUE" @click="attendNextTask" class="px-3 py-1.5 bg-indigo-600 text-white shadow-sm text-sm font-medium hover:bg-indigo-700 rounded transition hidden sm:inline-block">
          Atender Siguiente
        </button>
      </div>
    </header>

    <!-- Error Bar -->
    <div v-if="store.isError" class="bg-red-50 border-b border-red-200 p-3 shadow-sm flex items-start flex-shrink-0">
      <span class="material-symbols-outlined text-red-500 mt-0.5 mr-3 shrink-0">error</span>
      <p class="text-red-700 font-medium text-sm">{{ store.errorMessage }}</p>
    </div>

    <!-- CA-07/CA-18: Banner de Degradación BPMN -->
    <Transition name="toast-slide">
      <div v-if="store.isDegraded" class="bg-amber-50 border-b border-amber-300 p-3 shadow-sm flex items-center flex-shrink-0 gap-3">
        <span class="material-symbols-outlined text-amber-600 text-xl animate-pulse shrink-0">warning</span>
        <div>
          <p class="text-amber-800 font-bold text-sm">Sincronización BPMN degradada temporalmente</p>
          <p class="text-amber-600 text-xs">Las tareas de procesos automatizados podrían no estar actualizadas. Las tareas Kanban operan con normalidad.</p>
        </div>
      </div>
    </Transition>

    <!-- Main Content 75/25 Split -->
    <main class="flex-1 flex overflow-hidden flex-col md:flex-row">
      <!-- 75% Cards -->
      <section :class="isMetricsPanelOpen ? 'lg:w-3/4 border-r border-gray-200' : 'w-full'" class="w-full flex flex-col bg-gray-50 overflow-hidden transition-all duration-300 relative">
        
        <!-- CA-7: Componentes Dinámicos Aditivos (Protegido por Suspense Boundary) -->
        <div v-if="dynamicComponents.length > 0" class="w-full shrink-0 flex flex-col max-h-[45vh] overflow-y-auto border-b-4 border-slate-300 shadow-md">
           <div class="sticky top-0 bg-slate-800 px-4 py-2 flex items-center justify-between z-10 border-b border-slate-700 shadow-sm">
               <span class="text-[10px] font-black tracking-widest text-indigo-400 uppercase flex items-center gap-2">
                 <span class="material-symbols-outlined text-[14px]">extension</span> Módulo Aditivo (RBAC Inject)
               </span>
           </div>
           <div class="relative bg-white flex-1 overflow-auto rounded-b-lg min-h-[160px]">
               <Suspense>
                  <div class="flex flex-col">
                     <component v-for="(Comp, idx) in dynamicComponents" :key="'comp-'+idx" :is="Comp" />
                  </div>
                  <template #fallback>
                     <div class="flex items-center justify-center h-40 text-xs font-bold text-slate-400 animate-pulse">
                        <span class="material-symbols-outlined mr-2 animate-spin">refresh</span> Acoplando Widgets...
                     </div>
                  </template>
               </Suspense>
           </div>
        </div>

        <div class="h-12 bg-white border-b border-gray-200 px-6 flex items-center justify-between flex-shrink-0 shadow-sm z-20">
          <div class="flex items-center gap-4">
             <button @click="isMetricsPanelOpen = !isMetricsPanelOpen" class="p-1 rounded text-gray-400 hover:text-indigo-600 hover:bg-indigo-50 transition -ml-2" :title="isMetricsPanelOpen ? 'Ocultar Resumen Panel Derecho' : 'Mostrar Resumen'">
                <span class="material-symbols-outlined text-xl">{{ isMetricsPanelOpen ? 'dock_to_right' : 'dock_to_left' }}</span>
             </button>
             <span class="text-xs font-medium text-gray-500 flex items-center gap-1">
                <span class="material-symbols-outlined text-sm">filter_alt</span>
                Mostrando: <span class="font-bold text-indigo-700 bg-indigo-50 px-2 py-0.5 rounded">{{ filteredItems.length }}</span> resultados locales
             </span>
          </div>
          <div class="text-[11px] font-medium text-gray-400">
              Total Global: {{ store.pageInfo.totalElements }}
          </div>
        </div>

        <div class="flex-1 overflow-y-auto p-card-p no-scrollbar relative min-h-0">
           
           <!-- CA-12: Empty State Gamificado -->
           <div v-if="filteredItems.length === 0 && !store.isLoading" class="absolute inset-0 flex flex-col items-center justify-center">
             <div class="mx-auto flex h-24 w-24 items-center justify-center rounded-full bg-gradient-to-br from-emerald-100 to-green-50 border-2 border-emerald-200 shadow-lg">
               <span class="material-symbols-outlined text-emerald-500 text-5xl">celebration</span>
             </div>
             <h3 class="mt-6 text-lg font-bold text-emerald-700">🎉 ¡Bandeja Vacía!</h3>
             <p class="mt-2 text-gray-500 font-medium tracking-wide text-sm max-w-sm text-center">
               Has resuelto todas tus tareas pendientes. Excelente desempeño operativo.
             </p>
             <p class="mt-1 text-[10px] text-gray-400 uppercase tracking-widest font-semibold">Última sincronización: {{ new Date().toLocaleTimeString() }}</p>
           </div>
           
           <!-- CA-03: Data Grid Universal 5 Columnas -->
           <div v-else class="overflow-x-auto">
             <table class="w-full text-sm text-left">
               <thead class="text-[10px] uppercase tracking-wider text-gray-400 border-b border-gray-200 bg-gray-50/50">
                 <tr>
                   <th class="px-4 py-3 font-bold">Nombre</th>
                   <th class="px-4 py-3 font-bold">SLA</th>
                   <th class="px-4 py-3 font-bold">Estado</th>
                   <th class="px-4 py-3 font-bold hidden md:table-cell">Avance</th>
                   <th class="px-4 py-3 font-bold hidden md:table-cell">Recurso</th>
                 </tr>
               </thead>
               <tbody>
                 <tr 
                   v-for="task in filteredItems" 
                   :key="task.unifiedId"
                   @click="mockOpenTask(task)"
                   class="border-b border-gray-100 hover:bg-indigo-50/30 cursor-pointer transition-colors group"
                 >
                   <!-- Col 1: Nombre + Badge Tipo + Badge Impacto -->
                   <td class="px-4 py-3">
                     <div class="flex items-center gap-2">
                       <span class="material-symbols-outlined text-lg" :class="task.sourceSystem === 'BPMN' ? 'text-indigo-600' : 'text-cyan-600'">
                         {{ task.sourceSystem === 'BPMN' ? 'bolt' : 'account_tree' }}
                       </span>
                       <div class="flex flex-col min-w-0">
                         <span class="font-semibold text-[#1e1b4b] truncate max-w-[280px] group-hover:text-indigo-600 transition-colors">{{ task.title }}</span>
                         <span class="text-[10px] font-mono text-gray-400">{{ task.originalTaskId }}</span>
                       </div>
                       <span v-if="task.financialImpactHigh" class="px-1.5 py-0.5 bg-red-100 text-red-700 rounded text-[9px] font-black border border-red-200 shrink-0">🔥 Impacto</span>
                     </div>
                   </td>
                   <!-- Col 2: SLA Semáforo Vivo -->
                   <td class="px-4 py-3">
                     <span :class="['px-2 py-1 rounded text-[10px] font-bold uppercase tracking-wider border', getSlaPillClass(task.slaExpirationDate)]">
                       {{ getSlaRelativeTime(task.slaExpirationDate) }}
                     </span>
                   </td>
                   <!-- Col 3: Estado -->
                   <td class="px-4 py-3">
                     <span class="px-2 py-1 bg-gray-100/80 text-gray-600 rounded text-[10px] font-bold uppercase border border-gray-200 border-dashed">{{ task.status }}</span>
                   </td>
                   <!-- Col 4: Avance (CA-23) - Oculta en móvil -->
                   <td class="px-4 py-3 hidden md:table-cell">
                     <div v-if="task.progressPercent != null" class="flex items-center gap-2">
                       <div class="flex-1 bg-gray-200 rounded-full h-2 max-w-[120px]">
                         <div class="bg-indigo-600 h-2 rounded-full transition-all duration-500" :style="{ width: task.progressPercent + '%' }"></div>
                       </div>
                       <span class="text-[10px] font-bold text-gray-500 w-8 text-right">{{ task.progressPercent }}%</span>
                     </div>
                     <span v-else class="text-[10px] text-gray-400 italic">N/D</span>
                   </td>
                   <!-- Col 5: Recurso Asignado - Oculta en móvil (CA-12 degradación responsive) -->
                   <td class="px-4 py-3 hidden md:table-cell">
                     <div class="flex items-center gap-2">
                       <div v-if="task.assignee" class="w-6 h-6 rounded-full bg-indigo-600 text-white flex items-center justify-center font-bold text-[9px] ring-1 ring-indigo-100 uppercase shrink-0">
                         {{ task.assignee.substring(0,2) }}
                       </div>
                       <span class="text-xs text-gray-600 truncate max-w-[100px]">{{ task.assignee || 'Sin Asignar' }}</span>
                     </div>
                   </td>
                 </tr>
               </tbody>
             </table>
           </div>
        </div>

        <!-- Pagination Stitch Footer -->
        <div v-if="store.pageInfo.totalElements > store.pageInfo.pageSize" class="h-14 bg-white border-t border-gray-200 px-6 flex items-center justify-between flex-shrink-0">
          <p class="text-[11px] text-gray-500 font-medium tracking-wide">Página {{ store.pageInfo.pageNumber + 1 }}</p>
          <div class="flex items-center gap-2">
            <button 
               :disabled="store.pageInfo.pageNumber === 0" 
               @click="store.fetchGlobalInbox(store.pageInfo.pageNumber - 1, store.pageInfo.pageSize, searchQuery, delegationFilter)"
               class="p-1 text-gray-400 hover:text-indigo-600 disabled:opacity-30 disabled:hover:text-gray-400 transition"
            >
              <span class="material-symbols-outlined">chevron_left</span>
            </button>
            <div class="flex items-center gap-1">
              <span class="w-7 h-7 flex items-center justify-center text-xs font-bold rounded bg-indigo-600 text-white shadow-sm">
                {{ store.pageInfo.pageNumber + 1 }}
              </span>
            </div>
            <button 
               :disabled="(store.pageInfo.pageNumber + 1) * store.pageInfo.pageSize >= store.pageInfo.totalElements" 
               @click="store.fetchGlobalInbox(store.pageInfo.pageNumber + 1, store.pageInfo.pageSize, searchQuery, delegationFilter)"
               class="p-1 text-gray-400 hover:text-indigo-600 disabled:opacity-30 disabled:hover:text-gray-400 transition"
            >
              <span class="material-symbols-outlined">chevron_right</span>
            </button>
          </div>
        </div>
      </section>

      <!-- 25% Sidebar Metrics -->
      <aside v-if="isMetricsPanelOpen" class="hidden lg:block w-1/4 bg-white p-8 overflow-y-auto no-scrollbar relative z-10 shrink-0 transition-all duration-300">
        <div class="space-y-10">
          <div>
            <h2 class="text-xs font-bold text-gray-400 uppercase tracking-[0.2em] mb-8">Resumen Operativo</h2>
            <div class="space-y-8">
              <div class="flex items-center gap-4">
                <div class="relative w-14 h-14 rounded-full flex items-center justify-center bg-indigo-50 border border-indigo-100">
                   <span class="text-base font-bold text-indigo-700">{{ store.pageInfo.totalElements }}</span>
                </div>
                <div>
                  <p class="text-sm font-bold text-gray-900">Total Tareas</p>
                  <p class="text-[10px] text-gray-500 uppercase tracking-tighter font-semibold">Bandeja Activa</p>
                </div>
              </div>
              <div class="flex items-center gap-4">
                <div class="relative w-14 h-14 rounded-full flex items-center justify-center bg-red-50 border border-red-100">
                   <span class="text-base font-bold text-red-600">{{ countExpiredSLA() }}</span>
                   <div v-if="countExpiredSLA() > 0" class="absolute top-0 right-0 w-3.5 h-3.5 bg-red-500 rounded-full border-2 border-white animate-pulse"></div>
                </div>
                <div>
                  <p class="text-sm font-bold text-red-600 uppercase">Vencidas</p>
                  <p class="text-[10px] text-red-400 uppercase tracking-tighter font-semibold">Crítico - SLA Cumplido</p>
                </div>
              </div>
              <div class="flex items-center gap-4">
                <div class="relative w-14 h-14 rounded-full flex items-center justify-center bg-yellow-50 border border-yellow-100">
                   <span class="text-base font-bold text-yellow-600">{{ countWarningSLA() }}</span>
                </div>
                <div>
                  <p class="text-sm font-bold text-gray-900">Por Expirar</p>
                  <p class="text-[10px] text-yellow-600 uppercase tracking-tighter font-semibold">&lt; 24 Horas</p>
                </div>
              </div>
            </div>
          </div>

          <div class="pt-8 border-t border-gray-100">
             <div class="mt-2 bg-slate-50 p-5 rounded-xl border border-slate-200">
               <div class="flex items-center gap-2 mb-3">
                 <span class="material-symbols-outlined text-indigo-500 text-lg">public</span>
                 <p class="text-xs text-indigo-800 font-bold uppercase tracking-widest">CQRS Engine</p>
               </div>
               <p class="text-sm text-slate-800 font-medium tracking-tight">Sync Eventual: <br/>
                 <span v-if="!store.isError && store.stompConnected" class="font-bold text-emerald-600 flex items-center mt-2 gap-1.5 bg-emerald-50 px-2 py-1 rounded w-fit text-xs border border-emerald-100 animate-pulse">
                    <span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span> ONLINE
                 </span>
                 <span v-else class="font-bold text-red-600 flex items-center mt-2 gap-1.5 bg-red-50 px-2 py-1 rounded w-fit text-xs border border-red-200 animate-pulse" title="Conexión Rechazada o STOMP Caído">
                    <span class="w-1.5 h-1.5 rounded-full bg-red-500"></span> OFFLINE
                 </span>
               </p>
             </div>
          </div>
        </div>
      </aside>
    </main>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'Workdesk' });

import { ref, watch, onMounted, onUnmounted, defineAsyncComponent, computed } from 'vue';
import { useWorkdeskStore } from '@/stores/useWorkdeskStore';
import { useAuthStore } from '@/stores/authStore';

const store = useWorkdeskStore();
const toastSuccess = ref('');

// CA-12: Anti Empty Last Page
watch(() => store.items.length, (newLen) => {
  if (newLen === 0 && store.pageInfo.pageNumber > 0) {
    store.fetchGlobalInbox(0, store.pageInfo.pageSize, searchQuery.value, delegationFilter.value, typeFilter.value, slaFilter.value);
  }
});

// ==========================================
// Toggle del Panel Lateral Derecho
// ==========================================
const isMetricsPanelOpen = ref(true);

// ==========================================
// Gap CA-8: Toggle oculto Anti Cherry-Picking
// ==========================================
const FEATURE_FORCE_QUEUE = (import.meta as any).env.VITE_FEATURE_FORCE_QUEUE === 'true' || false;

// ── CA-7: Inyección Dinámica ──
const authStore = useAuthStore();
const AdminMetricsWidget = defineAsyncComponent(() => import('@/views/admin/Analytics/DashboardBAM.vue'));

const dynamicComponents = computed(() => {
    const list = [];
    if (authStore.hasAnyRole(['ROLE_SUPER_ADMIN', 'Global Admin'])) {
        list.push(AdminMetricsWidget);
    }
    return list;
});

// ==========================================
// Búsqueda & Delegación & Filtros Dinámicos (Gaps CA-2, CA-4)
// ==========================================
const searchQuery = ref('');
const delegationFilter = ref('');
const typeFilter = ref('');
const slaFilter = ref('');

let searchTimeout: ReturnType<typeof setTimeout> | null = null;

// Reactivity CA-5 Zero Frontend Filtering logic - Direct pass-through
const filteredItems = computed(() => {
    return store.items;
});

const onSearchInput = () => {
    if(searchTimeout) clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        loadData();
    }, 500); // 500ms Debouncer
};

const attendNextTask = () => {
   alert("Asignación ciega forzada.");
}

const loadData = async () => {
    await store.fetchGlobalInbox(0, store.pageInfo?.pageSize || 50, searchQuery.value, delegationFilter.value, typeFilter.value, slaFilter.value);
};

const mockOpenTask = (task: any) => {
    toastSuccess.value = `Work in progress: Abriendo Tarea ${task.unifiedId}...`;
    setTimeout(() => { toastSuccess.value = ''; }, 3000);
}

const clearToasts = () => {
    toastSuccess.value = '';
}

// ==========================================
// SLA Ticking Engine (Gap CA-5 Vivo)
// ==========================================
const currentTick = ref(Date.now());
let timerId: ReturnType<typeof setInterval> | null = null;

const getSlaStatus = (isoString?: string) => {
    if(!isoString) return 'OK';
    const flag = new Date(isoString).getTime();
    const diffHours = (flag - currentTick.value) / (1000 * 60 * 60);

    if(diffHours < 0) return 'EXPIRED';
    if(diffHours <= 24) return 'WARNING';
    return 'OK';
};

const getSlaPillClass = (isoString?: string) => {
    const st = getSlaStatus(isoString);
    if(st === 'EXPIRED') return 'bg-red-50 text-red-700 border-red-200/60';
    if(st === 'WARNING') return 'bg-yellow-50 text-yellow-700 border-yellow-200/60';
    return 'bg-emerald-50 text-emerald-700 border-emerald-200/60';
};

const getSlaRelativeTime = (isoString?: string) => {
    if(!isoString) return 'Sin SLA Expiración';
    
    // Reactivamente depende de currentTick.value
    const flag = new Date(isoString).getTime();
    const diffHours = (flag - currentTick.value) / (1000 * 60 * 60);
    const diffDays = diffHours / 24;

    if (diffHours < 0) return `Vencido hace ${Math.abs(Math.round(diffHours))} hrs`;
    if (diffHours < 24) return `Vence en ${Math.round(diffHours)} hrs`;
    return `Vence en ${Math.round(diffDays)} días`;
};

// ==========================================
// Summary Metrics Logic
// ==========================================
const countExpiredSLA = () => {
    return store.items.filter(i => getSlaStatus(i.slaExpirationDate) === 'EXPIRED').length;
};
const countWarningSLA = () => {
    return store.items.filter(i => getSlaStatus(i.slaExpirationDate) === 'WARNING').length;
};

onMounted(() => {
   loadData();
   // Gap CA-5: Montar Engine con 60s
   timerId = setInterval(() => {
       currentTick.value = Date.now();
   }, 60000);

   // Gap CA-6: Iniciar conexión WebSocket (Ghost Deletion)
   // NOTA: Store maneja el fallback WS local.
   store.initWebSocket();
});

onUnmounted(() => {
    // Purgar para prevenir leaks
    if(timerId) clearInterval(timerId);
    if(searchTimeout) clearTimeout(searchTimeout);

    // Gap CA-6: Desconectar WebSocket
    store.disconnectWebSocket();
});

</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0');
</style>

<style scoped>
[v-cloak] {
  display: none;
}

.material-symbols-outlined {
  font-family: 'Material Symbols Outlined';
  font-weight: normal;
  font-style: normal;
  display: inline-block;
  white-space: nowrap;
  word-wrap: normal;
  direction: ltr;
  font-feature-settings: 'liga';
  -webkit-font-feature-settings: 'liga';
  -webkit-font-smoothing: antialiased;
}

.no-scrollbar::-webkit-scrollbar {
  display: none;
}
.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
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
