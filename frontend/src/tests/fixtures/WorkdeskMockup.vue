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

    <!-- Mockup Dev Tools (Úsalo para simular eventos sin modificar conexión real) -->
    <div class="fixed top-2 right-1/2 translate-x-1/2 bg-white px-4 py-2 rounded-lg shadow-xl border z-[200] flex gap-2 items-center">
       <span class="text-xs font-bold text-gray-400 tracking-widest uppercase">Mockup Control:</span>
       <button @click="mockConnectionState = 'ONLINE'" class="text-[11px] px-3 py-1.5 font-bold uppercase transition rounded" :class="mockConnectionState === 'ONLINE' ? 'bg-indigo-600 text-white shadow' : 'bg-gray-100 hover:bg-gray-200'">Online</button>
       <button @click="mockConnectionState = 'OFFLINE'" class="text-[11px] px-3 py-1.5 font-bold uppercase transition rounded" :class="mockConnectionState === 'OFFLINE' ? 'bg-red-600 text-white shadow' : 'bg-red-50 hover:bg-red-100 text-red-700'">Offline</button>
       <button @click="simulateReconnect" class="text-[11px] px-3 py-1.5 font-bold uppercase bg-amber-100 text-amber-700 hover:bg-amber-200 transition rounded flex items-center gap-1"><span class="material-symbols-outlined text-[14px]">refresh</span> Reconectar</button>
    </div>

    <!-- Connection Monitor Toast -->
    <Transition name="toast-slide">
      <!-- OFFLINE STATE -->
      <div v-if="mockConnectionState === 'OFFLINE'" class="fixed bottom-6 left-6 z-[100] w-80 bg-white border border-gray-100 rounded-xl shadow-2xl p-5 flex flex-col gap-4">
         <div class="flex items-start gap-3">
            <div class="p-2 bg-red-50 rounded-full shrink-0">
               <span class="material-symbols-outlined text-red-500 text-xl">wifi_off</span>
            </div>
            <div>
               <h4 class="text-sm font-bold text-slate-900">Estado de la Red</h4>
               <p class="text-xs text-slate-500 mt-1 leading-relaxed">Sin conexión. La sincronización automática se encuentra pausada.</p>
            </div>
         </div>
         <button @click="simulateReconnect" class="w-full mt-1 py-2.5 bg-red-50 hover:bg-red-100 text-red-700 text-[13px] font-bold rounded-lg border border-red-200 transition-colors flex justify-center items-center gap-2 active:scale-95">
            Realizar Reconexión
         </button>
      </div>

      <!-- RECONNECTING STATE -->
      <div v-else-if="mockConnectionState === 'RECONNECTING'" class="fixed bottom-6 left-6 z-[100] w-[280px] bg-slate-900 rounded-xl shadow-2xl px-5 py-4 flex items-center justify-center gap-4">
         <div class="animate-spin rounded-full h-5 w-5 border-b-2 border-indigo-400"></div>
         <p class="text-sm font-bold text-white tracking-wide">Restableciendo enlace...</p>
      </div>

      <!-- RESTORED STATE -->
      <div v-else-if="mockConnectionState === 'RESTORED'" class="fixed bottom-6 left-6 z-[100] bg-emerald-500 rounded-xl shadow-2xl px-5 py-3.5 flex items-center gap-3">
         <span class="material-symbols-outlined text-white bg-emerald-600 rounded-full p-1">check</span>
         <p class="text-sm font-bold text-white">Conexión restaurada con éxito</p>
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
           <!-- CA-04: Toggle de Delegación con contextos separados -->
           <div class="inline-flex rounded-lg border border-gray-200/80 bg-white/50 backdrop-blur-sm p-0.5 shadow-sm">
             <button
               :class="[
                 'px-3 py-1.5 text-xs font-semibold rounded-md transition-all duration-200',
                 delegationMode === 'SELF'
                   ? 'bg-indigo-600 text-white shadow-sm'
                   : 'text-gray-500 hover:text-gray-700 hover:bg-gray-50'
               ]"
               @click="switchDelegationMode('SELF')"
             >
               📋 Mis Tareas
             </button>
             <button
               :class="[
                 'px-3 py-1.5 text-xs font-semibold rounded-md transition-all duration-200',
                 delegationMode === 'DELEGATED'
                   ? 'bg-amber-500 text-white shadow-sm'
                   : 'text-gray-500 hover:text-gray-700 hover:bg-gray-50'
               ]"
               @click="switchDelegationMode('DELEGATED')"
             >
               👤 Tareas de mi Asistente
             </button>
           </div>

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
        <!-- (CA-8 eliminado de aquí, ahora domina el main content) -->
      </div>
    </header>

    <!-- Error Bar -->
    <div v-if="store.isError" class="bg-red-50 border-b border-red-200 p-3 shadow-sm flex items-start flex-shrink-0">
      <span class="material-symbols-outlined text-red-500 mt-0.5 mr-3 shrink-0">error</span>
      <p class="text-red-700 font-medium text-sm">{{ store.errorMessage }}</p>
    </div>

    <!-- CA-15: Banner de Delegación Activa (solo visible en modo DELEGATED) -->
    <Transition name="slide-down">
      <div
        v-if="delegationMode === 'DELEGATED' && delegatedUserName"
        class="w-full px-6 py-2.5 flex items-center gap-3 border-b border-amber-200/60 bg-amber-50 shadow-sm shrink-0"
        role="alert"
        aria-live="polite"
      >
        <span class="text-amber-600 text-xl">⚠️</span>
        <span class="text-sm font-medium text-amber-800">
          Estás viendo el escritorio de <strong>{{ delegatedUserName }}</strong>
        </span>
        <button
          class="ml-auto text-xs font-semibold text-amber-600 hover:text-amber-800 hover:underline flex items-center gap-1"
          @click="switchDelegationMode('SELF')"
        >
          <span class="material-symbols-outlined text-sm">exit_to_app</span> Volver a mis tareas
        </button>
      </div>
    </Transition>

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

        <!-- CA-22/CA-29: Filtros Facetados (Chips) -->
        <div v-if="store.facets && store.facets.length > 0" class="flex flex-wrap items-center gap-2 px-6 py-3 bg-white border-b border-gray-200 shadow-sm z-10 shrink-0">
          <span class="text-[10px] font-bold text-gray-400 uppercase tracking-widest mr-2 flex items-center gap-1">
            <span class="material-symbols-outlined text-[14px]">category</span> Facetas
          </span>
          <button 
            v-for="facet in store.facets" 
            :key="facet.status"
            @click="applyFacetFilter(facet.status)"
            class="flex items-center gap-1.5 px-3 py-1.5 rounded border text-[11px] font-bold uppercase transition-all duration-200"
            :class="statusFilter === facet.status 
              ? 'bg-indigo-600 border-indigo-700 text-white shadow-md' 
              : 'bg-white border-gray-300 text-gray-600 hover:bg-gray-50 hover:border-gray-400'"
          >
            {{ facet.statusName || facet.status }}
            <span 
              class="px-1.5 py-0.5 rounded-sm text-[10px]"
              :class="statusFilter === facet.status ? 'bg-white/20 text-white' : 'bg-gray-100 text-gray-500'"
            >
              {{ facet.count }}
            </span>
          </button>
          
          <button 
            v-if="statusFilter" 
            @click="applyFacetFilter('')"
            class="ml-2 text-[10px] text-gray-400 hover:text-indigo-600 hover:underline font-semibold flex items-center gap-1"
          >
            <span class="material-symbols-outlined text-[14px]">close</span> Limpiar
          </button>
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
           
           <!-- CA-08: Modo Atender Siguiente Oculta Grilla -->
           <div v-if="store.forceRoutingEnabled" class="absolute inset-0 flex flex-col items-center justify-center p-8 bg-white/90 backdrop-blur z-30">
               <div class="max-w-md w-full text-center">
                 <div class="w-24 h-24 bg-indigo-50 rounded-full flex items-center justify-center mx-auto mb-6 border-4 border-indigo-100 shadow-inner">
                    <span class="material-symbols-outlined text-indigo-600 text-5xl">bolt</span>
                 </div>
                 <h2 class="text-2xl font-bold text-gray-900 mb-2">Modo Enrutamiento Forzoso</h2>
                 <p class="text-gray-500 mb-8 font-medium">El selector manual ha sido deshabilitado temporalmente.<br/>Por favor atienda la siguiente tarea crítica en cola.</p>
                 <button
                   class="w-full py-4 bg-indigo-600 hover:bg-indigo-700 active:bg-indigo-800 text-white font-bold rounded-xl shadow-lg hover:shadow-xl transition-all transform hover:-translate-y-0.5 flex items-center justify-center gap-3"
                   :disabled="store.isAttending"
                   @click="onAttendNextAction"
                 >
                   <span class="material-symbols-outlined text-2xl" :class="{ 'animate-spin': store.isAttending }">{{ store.isAttending ? 'hourglass_empty' : 'rocket_launch' }}</span>
                   <span class="text-lg">{{ store.isAttending ? 'Asignando...' : 'Atender Siguiente Tarea' }}</span>
                 </button>
               </div>
           </div>

           <!-- CA-12: Empty State Gamificado -->
           <div v-else-if="filteredItems.length === 0 && !store.isLoading" class="absolute inset-0 flex flex-col items-center justify-center">
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
                   <th class="px-4 py-3 font-bold text-center">Acciones</th>
                 </tr>
               </thead>
               <tbody>
                 <tr 
                   v-for="task in filteredItems" 
                   :key="task.unifiedId"
                   @click="mockOpenTask(task)"
                   :class="[{ 'is-ghost': (task as any)._isGhost, 'is-new': (task as any)._isNew }, 'workdesk-row border-b border-gray-100 hover:bg-indigo-50/30 cursor-pointer transition-colors group']"
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
                       <!-- CA-10: Badge de Autorización Tipográfica -->
                       <span class="px-1.5 py-0.5 bg-indigo-50 text-indigo-700 rounded text-[9px] font-bold border border-indigo-200 shrink-0">{{ task.targetRole || 'Rol Operativo' }}</span>
                       
                       <span v-if="task.variables?.isSlaAtRisk === true && getSlaStatus(task.slaExpirationDate) !== 'EXPIRED'" class="px-1.5 py-0.5 bg-amber-500 text-white rounded text-[9px] font-bold border border-amber-600 shrink-0" title="SLA en Riesgo (<20% restante)">⚠️ SLA en Riesgo</span>
                       <span v-if="task.financialImpactHigh" class="px-1.5 py-0.5 bg-red-100 text-red-700 rounded text-[9px] font-black border border-red-200 shrink-0">🔥 Impacto</span>
                     </div>
                   </td>
                   <!-- Col 2: SLA Semáforo Vivo con Iconografía Accesible (CA-11) -->
                   <td class="px-4 py-3">
                     <span :class="['px-2 py-1 rounded text-[10px] font-bold uppercase tracking-wider border flex items-center gap-1 w-fit', getSlaPillClass(task.slaExpirationDate)]">
                       <span class="text-xs">{{ getSlaIcon(task.slaExpirationDate) }}</span>
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
                   <!-- Col 6: Acciones (US-002 Task Claim) -->
                   <td class="px-4 py-3 text-center" @click.stop>
                     <button @click="onClaimTask(task)" class="px-3 py-1.5 bg-indigo-600 hover:bg-indigo-700 active:transform active:scale-95 text-white font-bold rounded-lg shadow-sm transition-all text-[11px] uppercase tracking-wider flex items-center justify-center gap-1.5 mx-auto disabled:opacity-50 min-w-[90px]" :disabled="isClaiming === (task.unifiedId || task.originalTaskId)">
                       <span v-if="isClaiming === (task.unifiedId || task.originalTaskId)" class="material-symbols-outlined text-[14px] animate-spin">refresh</span>
                       <span v-else class="material-symbols-outlined text-[14px]">pan_tool</span>
                       {{ isClaiming === (task.unifiedId || task.originalTaskId) ? 'Cargando' : 'Atender' }}
                     </button>
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
               @click="store.fetchGlobalInbox(store.pageInfo.pageNumber - 1, store.pageInfo.pageSize, searchQuery, delegationMode === 'DELEGATED' ? (delegatedUserId || undefined) : undefined, typeFilter, slaFilter, statusFilter)"
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
               @click="store.fetchGlobalInbox(store.pageInfo.pageNumber + 1, store.pageInfo.pageSize, searchQuery, delegationMode === 'DELEGATED' ? (delegatedUserId || undefined) : undefined, typeFilter, slaFilter, statusFilter)"
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

          <!-- Bloque CQRS removido arquitectónicamente. Reemplazado por el Toast inferior izquierdo -->
        </div>
      </aside>
    </main>

    <!-- VIEWER DE TAREA FAKE PARA DEMOSTRAR SKIPEO CA-21 -->
    <Transition name="toast-slide">
      <div v-if="openedTask" class="fixed inset-0 z-[100] flex items-center justify-center bg-gray-900/60 backdrop-blur-sm p-6">
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-3xl overflow-hidden flex flex-col h-[80vh]">
          <div class="px-6 py-4 bg-indigo-600 text-white flex items-center justify-between shadow-md">
            <div>
              <p class="text-[10px] font-bold uppercase tracking-widest text-indigo-200">ID: {{ openedTask.originalTaskId || openedTask.unifiedId }}</p>
              <h3 class="text-xl font-bold flex items-center gap-2">
                {{ openedTask.title || 'Formulario de Tarea' }}
              </h3>
            </div>
            <button @click="openedTask = null" class="text-indigo-200 hover:text-white transition rounded p-1"><span class="material-symbols-outlined">close</span></button>
          </div>
          <div class="p-8 flex-1 overflow-y-auto bg-gray-50">
             <div class="border-2 border-dashed border-gray-300 rounded-xl p-12 text-center text-gray-500 font-medium h-full flex flex-col items-center justify-center">
                 <span class="material-symbols-outlined text-6xl text-gray-300 mb-4">design_services</span>
                 Aquí cargaría el formulario dinámico real de la tarea (US-028/003).<br/>
                 Simulemos que el cliente no contesta y necesitas hacer skip.
             </div>
          </div>
          <div class="px-6 py-4 border-t border-gray-200 bg-white flex justify-between gap-3 shadow-inner">
             <button @click="openSkipReason" class="px-5 py-2.5 text-sm font-bold text-amber-700 bg-amber-100 hover:bg-amber-200 rounded-lg shadow-sm transition flex items-center gap-2 border border-amber-200">
                <span class="material-symbols-outlined text-[18px]">skip_next</span> Skipeo Justificado
             </button>
             <button @click="openedTask = null" class="px-6 py-2.5 text-sm font-bold text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg shadow-sm transition">
                <span class="material-symbols-outlined align-middle mr-1 text-[18px]">done_all</span> Completar Tarea
             </button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- CA-21: Modal para Skipeo Justificado -->
    <Transition name="toast-slide">
      <div v-if="showSkipModal" class="fixed inset-0 z-[110] flex items-center justify-center bg-gray-900/60 backdrop-blur-sm p-4">
        <div class="bg-white rounded-xl shadow-2xl w-full max-w-lg overflow-hidden flex flex-col border border-gray-200">
          <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between bg-amber-50">
            <h3 class="text-lg font-bold text-gray-900 flex items-center gap-2">
              <span class="material-symbols-outlined text-amber-500">skip_next</span>
              Justificar Pausa / Skipeo
            </h3>
            <button @click="closeSkipModal" class="text-gray-400 hover:text-gray-600 p-1 bg-white rounded"><span class="material-symbols-outlined">close</span></button>
          </div>
          <div class="p-6 space-y-4">
             <div class="bg-cyan-50 border border-cyan-200 text-cyan-800 p-3 rounded-lg text-[13px] flex gap-3 shadow-sm">
                <span class="material-symbols-outlined mt-0.5 text-cyan-600">info</span>
                <p>Estás a punto de saltar una tarea crítica. Esta acción quedará <strong>inmutablemente registrada en el Audit Log</strong> del sistema (NFR-OBS-01).</p>
             </div>
             <div>
               <label class="block text-sm font-bold text-gray-700 mb-1.5">Motivo de salto <span class="text-red-500">*</span></label>
               <select v-model="skipForm.reason" class="w-full bg-gray-50 border border-gray-300 rounded-lg px-3 py-2.5 text-sm text-gray-800 focus:ring-2 focus:ring-amber-500 focus:border-amber-500 outline-none transition font-medium">
                 <option value="" disabled>Seleccione un motivo...</option>
                 <option value="CLIENTE_NO_RESPONDE">El cliente no responde / No está disponible</option>
                 <option value="REQUIERE_DOCUMENTACION">Requiere documentación adicional externa</option>
                 <option value="FUERA_DE_AREA">Fuera de mi área de especialidad</option>
                 <option value="OTRO">Otro (Especificar)</option>
               </select>
             </div>
             <div v-if="skipForm.reason === 'OTRO'">
               <label class="block text-sm font-bold text-gray-700 mb-1.5">Detalle del motivo <span class="text-red-500">*</span></label>
               <textarea v-model="skipForm.detail" rows="3" class="w-full bg-gray-50 border border-gray-300 rounded-lg px-3 py-2 text-sm text-gray-800 focus:ring-2 focus:ring-amber-500 focus:border-amber-500 outline-none transition placeholder-gray-400" placeholder="Mínimo 10 caracteres explicatorios..."></textarea>
               <p v-if="skipForm.detail.length > 0 && skipForm.detail.length < 10" class="text-xs text-red-500 mt-1.5 font-medium flex items-center gap-1"><span class="material-symbols-outlined text-[14px]">error</span> El detalle debe tener al menos 10 caracteres.</p>
             </div>
          </div>
          <div class="px-6 py-4 border-t border-gray-100 bg-gray-50 flex justify-end gap-3">
             <button @click="closeSkipModal" class="px-4 py-2.5 text-sm font-bold text-gray-600 hover:text-gray-800 hover:bg-gray-200/60 rounded-lg transition" :disabled="store.isAttending">Cancelar</button>
             <button @click="submitSkip" :disabled="isSkipFormInvalid || store.isAttending" class="px-5 py-2.5 text-sm font-bold text-white bg-amber-600 hover:bg-amber-700 rounded-lg shadow disabled:opacity-50 disabled:cursor-not-allowed transition flex items-center gap-2">
                <span v-if="store.isAttending" class="material-symbols-outlined animate-spin text-[18px]">refresh</span>
                Confirmar Salto
             </button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'WorkdeskMockup' });

import { ref, watch, onMounted, onUnmounted, defineAsyncComponent, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useWorkdeskStore } from '@/stores/useWorkdeskStore';
import { useAuthStore } from '@/stores/authStore';
import { useTimeStore } from '@/stores/timeStore';

const router = useRouter();
const store = useWorkdeskStore();
const timeStore = useTimeStore();
const toastSuccess = ref('');

// ==========================================
// MOCKUP CONTROL LOGIC
// ==========================================
const mockConnectionState = ref<'ONLINE' | 'OFFLINE' | 'RECONNECTING' | 'RESTORED'>('ONLINE');

const simulateReconnect = () => {
   mockConnectionState.value = 'RECONNECTING';
   setTimeout(() => {
      mockConnectionState.value = 'RESTORED';
      setTimeout(() => {
         mockConnectionState.value = 'ONLINE';
      }, 2500); // flash duration
   }, 2000); // retry delay simulation
};
// ==========================================

// CA-12: Anti Empty Last Page
watch(() => store.items.length, (newLen) => {
  if (newLen === 0 && store.pageInfo.pageNumber > 0) {
    const delegatedId = delegationMode.value === 'DELEGATED' ? delegatedUserId.value : undefined;
    store.fetchGlobalInbox(0, store.pageInfo.pageSize, searchQuery.value, delegatedId || undefined, typeFilter.value, slaFilter.value, statusFilter.value);
  }
});

// ==========================================
// Toggle del Panel Lateral Derecho
// ==========================================
const isMetricsPanelOpen = ref(true);

// ==========================================
// Búsqueda & Delegación & Filtros Dinámicos (Gaps CA-2, CA-4)
// ==========================================
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
// CA-04: Estado del modo de delegación
const delegationMode = ref<'SELF' | 'DELEGATED'>('SELF');
const delegatedUserId = ref<string | null>(null);
const delegatedUserName = ref<string | null>(null);

const typeFilter = ref('');
const slaFilter = ref('');
const statusFilter = ref('');

let searchTimeout: ReturnType<typeof setTimeout> | null = null;

// CA-04/CA-15: Cambiar modo de delegación
const switchDelegationMode = async (mode: 'SELF' | 'DELEGATED') => {
  if (mode === delegationMode.value) return;

  delegationMode.value = mode;

  if (mode === 'DELEGATED') {
    // CA-15: Enviar request con el assistantId del usuario configurado
    // V1: El assistantId se obtiene del perfil del ejecutivo logueado
    // Nota: El authStore base tiene properties o any prop.
    const auth = authStore;
    const assistantId = (auth as any).delegatedAssistantId || '101edfe'; // UUID placeholder si no existe

    if (!assistantId) {
      console.warn('CA-04: No se encontró asistente configurado para delegación');
      delegationMode.value = 'SELF';
      return;
    }

    delegatedUserId.value = assistantId;

    try {
      // CA-15: El Backend valida la jerarquía y retorna 403 si es IDOR
      await store.fetchGlobalInbox(0, 50, searchQuery.value, assistantId, typeFilter.value, slaFilter.value, statusFilter.value);

      // Si la respuesta incluye delegationContext, extraer nombre
      delegatedUserName.value = store.lastDelegationContext?.delegatedUserDisplayName || assistantId;
    } catch (error: any) {
      if (error.response?.status === 403) {
        // CA-15: Bloqueo IDOR — revertir al modo propio
        console.error('CA-15: Delegación denegada por el servidor (403 Forbidden)');
        delegationMode.value = 'SELF';
        delegatedUserId.value = null;
        delegatedUserName.value = null;
        alert('No tiene permisos para ver el escritorio de este usuario.');
      }
    }
  } else {
    // Volver a "Mis Tareas"
    delegatedUserId.value = null;
    delegatedUserName.value = null;
    await loadData(); // Recargar con el contexto propio
  }
};

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

const applyFacetFilter = (status: string) => {
    statusFilter.value = status;
    loadData();
};

const loadData = async () => {
    const delegatedId = delegationMode.value === 'DELEGATED' ? delegatedUserId.value : undefined;
    await store.fetchGlobalInbox(0, store.pageInfo?.pageSize || 50, searchQuery.value, delegatedId || undefined, typeFilter.value, slaFilter.value, statusFilter.value);
};

// ==========================================
// CA-08/CA-16: Modo Attend Next y Tarea Abierta
// ==========================================
const openedTask = ref<any>(null);

const onAttendNextAction = async () => {
    try {
        const item = await store.attendNext();
        toastSuccess.value = `¡Tarea Asignada Atómicamente!`;
        setTimeout(() => { toastSuccess.value = ''; }, 3000);
        openedTask.value = item;
    } catch (err: any) {
        store.errorMessage = err.message || 'Error asignando siguiente tarea crítica.';
        store.isError = true;
    }
}

const isClaiming = ref<string | null>(null);

const onClaimTask = async (task: any) => {
    const taskIdString = task.unifiedId || task.originalTaskId;
    isClaiming.value = taskIdString;
    try {
        await store.claimTask(taskIdString);
        toastSuccess.value = 'Tarea atendida con éxito.';
        setTimeout(() => { toastSuccess.value = ''; }, 3000);
        // US-002: Enrutamiento programático a FormGen (usamos vista FormDesigner mock)
        router.push({ name: 'FormDesigner' });
    } catch (err: any) {
        console.error(err);
        if (err.response?.status === 409) {
            store.errorMessage = 'Alguien ya ha tomado esta tarea.';
        } else {
            store.errorMessage = err.response?.data?.message || 'Error al intentar atender la tarea.';
        }
        store.isError = true;
    } finally {
        isClaiming.value = null;
    }
}

const mockOpenTask = (task: any) => {
    openedTask.value = task;
}

// ==========================================
// CA-21: Lógica del Modal de Skipeo Justificado
// ==========================================
const showSkipModal = ref(false);
const skipForm = ref({ reason: '', detail: '' });

const isSkipFormInvalid = computed(() => {
    if (!skipForm.value.reason) return true;
    if (skipForm.value.reason === 'OTRO' && skipForm.value.detail.trim().length < 10) return true;
    return false;
});

const openSkipReason = () => {
    skipForm.value = { reason: '', detail: '' };
    showSkipModal.value = true;
}

const closeSkipModal = () => {
    if (store.isAttending) return;
    showSkipModal.value = false;
}

const submitSkip = async () => {
    if (!openedTask.value) return;
    try {
        const newItem = await store.skipAndNext(
            openedTask.value.unifiedId || openedTask.value.originalTaskId, 
            skipForm.value.reason, 
            skipForm.value.detail
        );
        toastSuccess.value = `Skipeo registrado. Nueva Tarea Asignada.`;
        setTimeout(() => { toastSuccess.value = ''; }, 3000);
        
        // Cerrar modal y abrir la nueva tarea en el fake viewer
        showSkipModal.value = false;
        openedTask.value = newItem;
    } catch (err: any) {
        store.errorMessage = err.message || 'Error realizando skipeo justificado.';
        store.isError = true;
        showSkipModal.value = false;
    }
}

const clearToasts = () => {
    toastSuccess.value = '';
}

// ==========================================
// CA-24: Umbrales deterministas basados en % del tiempo restante
// ==========================================
const SLA_THRESHOLDS = {
    GREEN_ABOVE: 0.50,   // > 50% restante → Verde
    YELLOW_ABOVE: 0.15,  // > 15% restante → Amarillo
    // < 15% → Rojo
    // 0% → Vencida
};

const getSlaStatus = (isoString?: string): 'OK' | 'WARNING' | 'EXPIRED' | 'CRITICAL' => {
    if (!isoString) return 'OK'; // Sin SLA = no hay presión

    const deadline = new Date(isoString).getTime();
    const now = timeStore.currentTick; // CA-05/CA-11: Reactivo vía Heartbeat Store
    const diff = deadline - now;

    if (diff <= 0) return 'EXPIRED'; // ⚫ Vencida (0%)

    // CA-24: Necesitamos el "total del SLA" para calcular el porcentaje.
    const totalSlaWindow = 48 * 60 * 60 * 1000; // 48h ventana base V1
    const percentRemaining = Math.min(diff / totalSlaWindow, 1.0);

    if (percentRemaining > SLA_THRESHOLDS.GREEN_ABOVE) return 'OK';        // 🟢
    if (percentRemaining > SLA_THRESHOLDS.YELLOW_ABOVE) return 'WARNING';  // 🟡
    return 'CRITICAL'; // 🔴
};

const getSlaPillClass = (isoString?: string) => {
    const st = getSlaStatus(isoString);
    if (st === 'EXPIRED') return 'bg-gray-200 text-gray-700 border-gray-300';         // ⚫
    if (st === 'CRITICAL') return 'bg-red-50 text-red-700 border-red-200/60';         // 🔴
    if (st === 'WARNING') return 'bg-yellow-50 text-yellow-700 border-yellow-200/60'; // 🟡
    return 'bg-emerald-50 text-emerald-700 border-emerald-200/60';                    // 🟢
};

// CA-11: Iconografía accesible para daltónicos (SVG inline / Emojis)
const getSlaIcon = (isoString?: string): string => {
    const st = getSlaStatus(isoString);
    if (st === 'EXPIRED') return '⚫';  // Vencida
    if (st === 'CRITICAL') return '⚡'; // Rojo (Urgente)
    if (st === 'WARNING') return '⏳';  // Amarillo (Por vencer)
    return '✔️';                         // Verde (Al día)
};

const getSlaRelativeTime = (isoString?: string) => {
    if(!isoString) return 'Sin SLA Expiración';
    
    // Reactivamente depende de timeStore.currentTick
    const flag = new Date(isoString).getTime();
    const diffHours = (flag - timeStore.currentTick) / (1000 * 60 * 60);
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

// CA-31: Umbral de inactividad para auto-refresco (5 minutos)
const INACTIVITY_THRESHOLD_MS = 5 * 60 * 1000;
let visibilityCleanup: (() => void) | null = null;

onMounted(async () => {
    await store.checkForceRouting(); // CA-08: Verificar Feature Toggle
    loadData();

    // CA-05/CA-11: Arrancar Heartbeat Store en vez de setInterval
    timeStore.startEngine();

    // CA-31: Listener de visibilitychange para auto-refresco pasivo
    const onVisibilityReturn = async () => {
        if (document.visibilityState === 'visible') {
            // CA-25: El timeStore ya recalcula `currentTick` inmediatamente
            // CA-31: Si inactividad > 5 min → refresco silencioso de datos
            if (timeStore.getInactivityMs() > INACTIVITY_THRESHOLD_MS) {
                await loadData();
            }
        }
    };
    document.addEventListener('visibilitychange', onVisibilityReturn);
    visibilityCleanup = () => document.removeEventListener('visibilitychange', onVisibilityReturn);

    // CA-6: Iniciar conexión WebSocket (Ghost Deletion)
    store.initWebSocket();
});

onUnmounted(() => {
    // CA-11: Detener Heartbeat Engine
    timeStore.stopEngine();

    // CA-31: Limpiar listener
    if (visibilityCleanup) visibilityCleanup();

    if(searchTimeout) clearTimeout(searchTimeout);
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

/* CA-15: Animación suave del Banner de delegación */
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
}
.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* CA-13: Transición Ghost Deletion */
.workdesk-row {
    transition: opacity 0.8s ease-out, transform 0.8s ease-out;
}
.workdesk-row.is-ghost {
    opacity: 0;
    transform: translateX(-20px);
    pointer-events: none;
}
/* CA-26: Fade-in para tarjetas nuevas */
.workdesk-row.is-new {
    animation: fadeIn 0.5s ease-in;
}
@keyframes fadeIn {
    from { opacity: 0; transform: translateY(10px); }
    to { opacity: 1; transform: translateY(0); }
}
</style>
