<template>
  <div class="h-full flex flex-col p-6 bg-gray-50 dark:bg-gray-900 overflow-hidden relative">
    
    <!-- Top Bar -->
    <div class="flex flex-col md:flex-row items-center justify-between mb-6 space-y-4 md:space-y-0">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white flex items-center">
          <svg class="w-6 h-6 mr-2 text-indigo-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path></svg>
          Área de Gantt (Planificación de Proyecto)
        </h1>
        <p class="text-sm text-gray-500 dark:text-gray-400">Pantalla 10.B - Vista WBS y Timeline Tradicional</p>
      </div>

      <div class="flex items-center space-x-3">
        <!-- RIESGO MITIGADO: Etiqueta Open Source -->
        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400">
          Powered by Frappe Gantt (OS MIT)
        </span>
        
        <!-- AC-1 & AC-2: FIJAR LÍNEA BASE -->
        <button 
          @click="fixBaseline"
          :disabled="baselineFixed || isLoading || hasUnassignedTasks"
          :class="[
            'inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition-colors',
            baselineFixed 
              ? 'bg-gray-300 text-gray-500 dark:bg-gray-700 dark:text-gray-400 cursor-not-allowed' 
              : 'text-white bg-indigo-600 hover:bg-indigo-700 flash-glow'
          ]"
        >
          <svg v-if="isLoading" class="animate-spin -ml-1 mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
          <svg v-else class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path></svg>
          {{ baselineFixed ? 'Línea Base Activa' : '🚀 FIJAR LÍNEA BASE' }}
        </button>
      </div>
    </div>

    <!-- Alert / SSE Notification -->
    <div v-if="sseNotification" class="mb-4 bg-teal-50 border border-teal-200 text-teal-800 p-3 rounded-lg flex items-center shadow-sm">
        <svg class="h-5 w-5 mr-3 text-teal-500 animate-pulse" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
        <span class="text-sm">{{ sseNotification }}</span>
    </div>

    <div v-if="successMsg" class="mb-4 bg-indigo-50 border border-indigo-200 text-indigo-800 p-3 rounded-lg flex items-center shadow-sm">
        <svg class="h-5 w-5 mr-3 text-indigo-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
        <span class="text-sm font-semibold">{{ successMsg }}</span>
    </div>

    <div v-if="errorMsg" class="mb-4 bg-red-50 border border-red-200 text-red-800 p-3 rounded-lg flex items-center shadow-sm">
        <span class="text-sm">{{ errorMsg }}</span>
    </div>

    <!-- Gantt Chart Container -->
    <div class="flex-1 bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden flex flex-col relative">
      <div v-if="isLoading" class="absolute inset-0 z-10 bg-white/50 backdrop-blur-sm flex items-center justify-center">
        <div class="text-indigo-600 font-medium">Cargando Mapeo de Ejecución...</div>
      </div>
      
      <div class="px-4 py-3 bg-gray-50 dark:bg-gray-800/80 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center text-sm">
        <span class="text-gray-600 dark:text-gray-400">Instrucciones: Doble click sobre una barra para Asignar Recursos y fijar el Presupuesto Real.</span>
        
        <div class="flex border border-gray-300 dark:border-gray-600 rounded-md overflow-hidden bg-white dark:bg-gray-700">
          <button @click="changeViewMode('Day')" :class="['px-3 py-1 cursor-pointer transition', viewMode === 'Day' ? 'bg-indigo-100 text-indigo-700 font-semibold' : 'hover:bg-gray-100 dark:hover:bg-gray-600 dark:text-white']">Día</button>
          <button @click="changeViewMode('Week')" :class="['px-3 py-1 cursor-pointer transition border-l border-gray-300 dark:border-gray-600', viewMode === 'Week' ? 'bg-indigo-100 text-indigo-700 font-semibold' : 'hover:bg-gray-100 dark:hover:bg-gray-600 dark:text-white']">Semana</button>
          <button @click="changeViewMode('Month')" :class="['px-3 py-1 cursor-pointer transition border-l border-gray-300 dark:border-gray-600', viewMode === 'Month' ? 'bg-indigo-100 text-indigo-700 font-semibold' : 'hover:bg-gray-100 dark:hover:bg-gray-600 dark:text-white']">Mes</button>
        </div>
      </div>

      <!-- El nodo DOM para montar el Frappe Gantt -->
      <div class="flex-1 overflow-auto bg-white gantt-scroll-container">
         <div id="frappe-gantt-target" class="frappe-gantt-root"></div>
      </div>
    </div>

    <!-- Drawer Lateral derecho para inyectar recursos a las tareas -->
    <ResourcePanel 
      :is-open="isDrawerOpen" 
      :task="selectedTask"
      @close="isDrawerOpen = false"
      @saved="refreshGanttState"
    />

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue';
import axios from 'axios';
import apiClient from '@/services/apiClient';
import ResourcePanel from './ResourcePanel.vue';

// Import frappe-gantt TS
// Usualmente exporta Gantt class en ES default. Cuidado con environments SSR
import Gantt from 'frappe-gantt';

// Styles for the component
import 'frappe-gantt/dist/frappe-gantt.css';

const MOCK_PROJECT_ID = 'proj-123'; // Epic 10.B Contexto temporal
let ganttInstance: Gantt | null = null;
let eventSource: EventSource | null = null;

const rawTasks = ref<any[]>([]);
const isLoading = ref(true);
const baselineFixed = ref(false);
const viewMode = ref('Day');

const isDrawerOpen = ref(false);
const selectedTask = ref<any>(null);

const successMsg = ref('');
const errorMsg = ref('');
const sseNotification = ref('');

// Computed Property to ensure ZERO-TRUST on resource assignment before executing baseline
const hasUnassignedTasks = computed(() => {
  if (rawTasks.value.length === 0) return true;
  return rawTasks.value.some(t => !t.assigneeUserId);
});

// ======================
// BOOT & API CALLS
// ======================
const loadGanttTree = async () => {
  isLoading.value = true;
  try {
    const response = await apiClient.get(`/execution/projects/${MOCK_PROJECT_ID}/gantt-tree`);
    rawTasks.value = response.data.map((t: any) => ({
      id: t.id,
      name: t.name,
      start: t.start,
      end: t.end,
      progress: t.progress,
      dependencies: t.dependencies,
      custom_class: getCustomClassForStatus(t.status),
      // Metadata extra via payload:
      status: t.status,
      actualBudget: t.actualBudget,
      assigneeUserId: t.assigneeUserId
    }));
    
    await nextTick();
    renderGanttChart();
    
  } catch (err) {
    console.error(err);
    errorMsg.value = 'Failed to load Gantt Tree from DB.';
  } finally {
    isLoading.value = false;
  }
};

const getCustomClassForStatus = (status: string) => {
  switch(status) {
    case 'DONE': return 'bar-done';
    case 'IN_PROGRESS': return 'bar-in-progress';
    case 'BLOCKED': return 'bar-blocked';
    case 'PENDING': default: return 'bar-pending';
  }
};

// ======================
// FRAPPE GANTT LOGIC
// ======================
const renderGanttChart = () => {
    const container = document.getElementById('frappe-gantt-target');
    if (!container) return;

    if (ganttInstance) {
        // En frappe, cambiar data reconstruye todo pero puede perder el state, lo mejor es purgar el dom html
        container.innerHTML = ''; 
    }

    if (rawTasks.value.length === 0) return;

    ganttInstance = new Gantt('#frappe-gantt-target', rawTasks.value, {
        header_height: 50,
        column_width: 30,
        step: 24,
        view_modes: ['Quarter Day', 'Half Day', 'Day', 'Week', 'Month'],
        bar_height: 25,
        bar_corner_radius: 4,
        arrow_curve: 5,
        padding: 18,
        view_mode: viewMode.value,
        date_format: 'YYYY-MM-DD',
        custom_popup_html: function(task: any) {
             const responsable = task.assigneeUserId ? task.assigneeUserId : 'Sin Asignar';
             const budget = task.actualBudget ? `$${task.actualBudget}` : '$0.00';
             return `
               <div class="px-3 py-2 bg-white shadow-lg border border-gray-200 rounded-md text-sm">
                 <h4 class="font-bold text-gray-800 mb-1">${task.name}</h4>
                 <div class="text-xs text-gray-600">Progreso: ${task.progress}%</div>
                 <div class="text-xs text-gray-600">Costos: ${budget}</div>
                 <div class="text-xs text-gray-500 mt-1 border-t pt-1 border-gray-100">Doble click para asignar UUID</div>
               </div>
             `;
        },
        on_click: function (task: any) {
            // Un click
        },
        on_date_change: function(task: any, start: any, end: any) {
            // Drag and drop para recalendarizar
            // En V1 esto está restringido si LA LÍNEA BASE ESTÁ FIJADA
            if (baselineFixed.value) {
                errorMsg.value = 'Operación Denegada. La Línea base general ya está fijada.';
                setTimeout(() => errorMsg.value='', 3000);
                // Revert visual
                loadGanttTree(); 
                return;
            }
        },
        on_progress_change: function(task: any, progress: number) {
            if (baselineFixed.value) {
                errorMsg.value = 'El progreso solo puede ser alterado automáticamente vía Workdesk por los ejecutores.';
                setTimeout(() => errorMsg.value='', 3000);
                loadGanttTree();
            }
        },
        on_view_change: function(mode: string) {
            // internal mode changed
        }
    });

    // Event binding no oficial para doble-click
    // Frappe expone events a nivel SVG elements `.bar-group`
    const bars = container.querySelectorAll('.bar-group');
    bars.forEach((bar: Element) => {
        bar.addEventListener('dblclick', () => {
            const taskId = bar.getAttribute('data-id');
            const targetTask = rawTasks.value.find(t => t.id === taskId);
            if (targetTask) {
                selectedTask.value = targetTask;
                isDrawerOpen.value = true;
            }
        });
    });
};

const changeViewMode = (mode: string) => {
    viewMode.value = mode;
    if (ganttInstance) {
        ganttInstance.change_view_mode(mode);
    }
}

// Emitted from ResourceDrawer
const refreshGanttState = () => {
    // Volvemos a jalar el payload del mock para que tome las props guardadas (aunque en el mock lo cachea en ram por ahora no)
    // Para simplificar, cerramos y confiamos que Vue actualizó la ref interna the ResourceDrawer.
    renderGanttChart(); 
};


// ======================
// AC-1 / AC-2: FIJAR LÍNEA BASE
// ======================
const fixBaseline = async () => {
    isLoading.value = true;
    errorMsg.value = '';
    try {
        const response = await apiClient.post(`/execution/projects/${MOCK_PROJECT_ID}/baseline`);
        successMsg.value = `Big Bang! Línea base congelada [ID: ${response.data}]. Las bandejas Workdesk de Camunda de los responsables han sido activadas transaccionalmente.`;
        baselineFixed.value = true;
        setTimeout(() => successMsg.value = '', 6000);
    } catch(err) {
        errorMsg.value = 'Fallo la consolidación de la línea base operativa.';
    } finally {
        isLoading.value = false;
    }
};


// ======================
// AC-3: SERVER-SENT EVENTS (SSE)
// ======================
const setupSSE = () => {
    // Usamos el EventSource API HTML5 Puntiando al endpoint
    // En el modo Mock-First real, MSW no sporta EventSource por defecto facil.
    // Usaremos un timer simulador interno si MSW falla, pero el código es el real para backend vivo.
    
    // Fallback Mock Simulador SSE:
    setTimeout(() => {
        sseNotification.value = "Evento SSE Recibido: El Usuario 'user-001' culminó Desarrollo Backend al 100% via Pantalla 1.";
        
        // Find task and mock visually to green (DONE)
        const t3 = rawTasks.value.find(t => t.id === 'task-3');
        if(t3) {
            t3.status = 'DONE';
            t3.progress = 100;
            t3.custom_class = getCustomClassForStatus('DONE');
        }
        renderGanttChart();
        
        setTimeout(() => { sseNotification.value = ''; }, 5000);
    }, 15000); // Trigger 15 seg despues

    /* CÓDIGO PRODUCCIÓN (Oculto para Mock)
    eventSource = new EventSource(`/api/v1/execution/projects/${MOCK_PROJECT_ID}/stream`);
    
    eventSource.onopen = () => console.log('SSE AC-3 Connection Established');
    
    eventSource.addEventListener('gantt-task-update', (event) => {
        const payload = JSON.parse(event.data);
        console.log("SSE Rceibido", payload);
        sseNotification.value = `¡Progreso Actualizado! Tarea ID: ${payload.taskId} pasó a estado ${payload.status}`;
        loadGanttTree(); // Recargar árbol
        setTimeout(() => sseNotification.value = '', 4000);
    });

    eventSource.onerror = (error) => {
        console.warn('SSE Disconnected. Trying to reconnect...', error);
    };
    */
};

onMounted(() => {
    loadGanttTree();
    setupSSE();
});

onUnmounted(() => {
    if(eventSource) {
        eventSource.close();
    }
});
</script>

<style>
/* CSS HACKS PARA FRAPPE-GANTT OVERRIDE */
.frappe-gantt-root {
  min-height: 400px; /* Para asegurar overflow vertical */
}

/* Custom Status Bars Classes injected via custom_class property */
.gantt .bar-wrapper.bar-done .bar { fill: #10b981; }             /* Tailwind Emerald 500 */
.gantt .bar-wrapper.bar-in-progress .bar { fill: #3b82f6; }       /* Tailwind Blue 500 */
.gantt .bar-wrapper.bar-blocked .bar { fill: #ef4444; }           /* Tailwind Red 500 */
.gantt .bar-wrapper.bar-pending .bar { fill: #64748b; }           /* Tailwind Slate 500 */

/* Progreso solido encima */
.gantt .bar-wrapper.bar-done .bar-progress { fill: #059669; }
.gantt .bar-wrapper.bar-in-progress .bar-progress { fill: #2563eb; }
.gantt .bar-wrapper.bar-blocked .bar-progress { fill: #dc2626; }
.gantt .bar-wrapper.bar-pending .bar-progress { fill: #475569; }

/* Efecto Flash/Pulso en boton */
.flash-glow {
    animation: pulse-glow 2s infinite;
}
@keyframes pulse-glow {
  0% { box-shadow: 0 0 0 0 rgba(79, 70, 229, 0.7); }
  70% { box-shadow: 0 0 0 10px rgba(79, 70, 229, 0); }
  100% { box-shadow: 0 0 0 0 rgba(79, 70, 229, 0); }
}

/* Asegurar tipografia modern en Gantt */
.gantt { font-family: 'Inter', system-ui, sans-serif !important; }
</style>
