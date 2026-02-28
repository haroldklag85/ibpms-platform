<template>
  <div class="h-full flex flex-col relative" v-cloak>
    <!-- Overlay Cargando Global -->
    <div v-if="isLoading" class="absolute inset-0 bg-white/70 flex items-center justify-center z-50">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-ibpms-brand"></div>
    </div>

    <div class="flex justify-between items-center mb-6">
      <h2 class="text-2xl font-bold text-gray-800">Workdesk (Bandeja de Tareas)</h2>
      
      <!-- Filtros Rápidos y Refresh -->
      <div class="flex space-x-2">
        <button @click="loadBothQueues" class="px-4 py-2 bg-gray-100 text-gray-700 rounded shadow-sm text-sm font-medium hover:bg-gray-200" title="Refrescar">
          🔄
        </button>
        <button @click="currentTab = 'MINE'" :class="[currentTab === 'MINE' ? 'bg-ibpms text-white' : 'bg-white text-gray-700 hover:bg-gray-50']" class="px-4 py-2 border rounded shadow-sm text-sm font-medium transition">
          Mis Tareas ({{ tasks.length }})
        </button>
        <button @click="currentTab = 'CANDIDATES'" :class="[currentTab === 'CANDIDATES' ? 'bg-ibpms text-white' : 'bg-white text-gray-700 hover:bg-gray-50']" class="px-4 py-2 border rounded shadow-sm text-sm transition">
          Cola Grupal ({{ candidates.length }})
        </button>
      </div>
    </div>

    <!-- Error Bar -->
    <div v-if="error" class="bg-red-50 border-l-4 border-red-500 p-4 mb-4 rounded shadow-sm">
      <p class="text-red-700 font-medium">⚠️ Error de conexión: {{ error }}</p>
    </div>

    <!-- Grid Layout para la Bandeja Vertical -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 flex-1 overflow-hidden">
      
      <!-- Listado de Tareas (Panel Izquierdo/Central) -->
      <div class="lg:col-span-2 overflow-y-auto pr-2 space-y-4">
        
        <div v-if="(currentTab === 'MINE' && tasks.length === 0) || (currentTab === 'CANDIDATES' && candidates.length === 0)" class="text-center py-20 bg-gray-50 border-2 border-dashed rounded-lg">
           <span class="text-4xl">📭</span>
           <p class="mt-4 text-gray-500 font-medium">Bandeja Vacía. No hay tareas pendientes en esta vista.</p>
        </div>

        <!-- Renderizado Dinámico de Tareas -->
        <div v-for="task in activeList" :key="task.id" class="bg-white p-5 rounded-lg border-l-4 shadow-sm hover:shadow-md transition cursor-pointer"
             :class="{'border-l-blue-500': task.priority <= 50, 'border-l-red-500': task.priority > 50}">
          <div class="flex justify-between items-start mb-2">
            <div class="flex items-center space-x-2">
              <span v-if="task.priority > 50" class="px-2 lg:py-1 bg-red-100 text-red-800 text-xs font-bold rounded">URGENTE</span>
              <span v-else class="px-2 lg:py-1 bg-blue-100 text-blue-800 text-xs font-bold rounded">NORMAL</span>
              
              <span class="px-2 lg:py-1 bg-gray-100 text-gray-800 text-xs font-semibold rounded">
                Creado: {{ new Date(task.created).toLocaleDateString() }}
              </span>
              <span v-if="task.due" class="text-xs font-mono text-purple-600 font-bold border rounded px-1">{{ new Date(task.due).toLocaleString() }}</span>
            </div>
            <!-- Checkbox de "Bulk Action" (Futuro) -->
            <input type="checkbox" class="h-4 w-4 text-ibpms-brand rounded border-gray-300">
          </div>
          
          <h3 class="text-lg font-bold text-gray-900">{{ task.name }}</h3>
          <p class="text-gray-600 text-sm mb-4">Módulo (DefId): <span class="font-mono text-xs">{{ task.processDefinitionId }}</span></p>
          <p v-if="task.description" class="text-gray-500 text-xs italic mb-4">"{{ task.description }}"</p>
          
          <div class="flex justify-between items-center">
            <button v-if="currentTab === 'MINE'" class="px-4 py-2 bg-ibpms-brand text-white text-sm font-bold rounded shadow-sm hover:bg-blue-600 transition">
              Abrir Formulario
            </button>
            
            <button v-if="currentTab === 'CANDIDATES'" @click.stop="handleClaim(task.id)" class="px-4 py-2 bg-ibpms shadow text-white text-sm font-bold rounded hover:bg-gray-800 transition">
              Asignarmela (Claim)
            </button>

            <button v-if="currentTab === 'MINE'" class="text-gray-500 text-sm font-medium hover:underline text-red-500">
              ↩️ Liberar Grupo
            </button>
          </div>
        </div>

      </div>

      <!-- Panel Derecho Auxiliar (Resumen / QA Agent / IA) -->
      <div class="hidden lg:block bg-white p-6 rounded-lg shadow-sm border h-fit sticky top-0">
        <h4 class="font-bold text-gray-800 mb-4 border-b pb-2">Resumen de Bandeja</h4>
        <ul class="space-y-3 text-sm text-gray-600">
          <li class="flex justify-between"><span>Mis Tareas Totales:</span> <span class="font-bold">{{ tasks.length }}</span></li>
          <li class="flex justify-between"><span>Cola Candidata:</span> <span class="font-bold">{{ candidates.length }}</span></li>
          <li class="flex justify-between text-red-600"><span>Urgentes Totales:</span> <span class="font-bold">{{ urgentCount }}</span></li>
        </ul>
        
        <div class="mt-8 bg-gray-50 p-4 rounded border border-gray-200">
          <p class="text-xs text-gray-500 mb-2 font-mono">📡 ESTADO DE MOTOR (API)</p>
          <p class="text-sm text-gray-600">Polling: <span class="font-bold text-green-600">Online</span></p>
          <p class="text-xs text-gray-400 mt-2">Próxima Sincronización en: --s</p>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useTasks } from '@/composables/useTasks';
import type { TaskDto } from '@/types/Task';

const { tasks, candidates, isLoading, error, fetchMyTasks, fetchCandidateTasks, claimTask } = useTasks();

// Tabs UI State
const currentTab = ref<'MINE' | 'CANDIDATES'>('MINE');

// Computed array based on tab clicked
const activeList = computed(() => {
  return currentTab.value === 'MINE' ? tasks.value : candidates.value;
});

const urgentCount = computed(() => {
   return tasks.value.filter((t: TaskDto) => t.priority > 50).length + candidates.value.filter((t: TaskDto) => t.priority > 50).length;
});

const loadBothQueues = async () => {
  // Disparamos ambas queries en paralelo
  await Promise.allSettled([
    fetchMyTasks(),
    fetchCandidateTasks()
  ]);
};

const handleClaim = async (taskId: string) => {
  try {
    await claimTask(taskId);
    // Refresh the "Mine" queue so it shows up immediately
    await fetchMyTasks();
    currentTab.value = 'MINE'; 
  } catch(err) {
    // Errores ya atajados por el Composable
  }
};

onMounted(() => {
  loadBothQueues();
});
</scr