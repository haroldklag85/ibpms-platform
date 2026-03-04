<template>
  <div class="h-full flex flex-col p-6 space-y-6 bg-gray-50 dark:bg-gray-900">
    <!-- Header -->
    <div class="flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Constructor de Proyectos WBS</h1>
        <p class="text-sm text-gray-500 dark:text-gray-400">Diseña plantillas de proyectos iterativos con Fases y Tareas</p>
      </div>
      <button 
        @click="submitWbs"
        :disabled="store.isSaving || !isValid"
        class="inline-flex items-center px-4 py-2 border border-transparent rounded-lg shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 transition-colors"
      >
        <svg v-if="store.isSaving" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
        <svg v-else class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
        Guardar Plantilla
      </button>
    </div>

    <!-- Alert Success -->
    <div v-if="successMessage" class="bg-green-50 border border-green-200 text-green-800 rounded-lg p-4 flex items-center animate-pulse">
      <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
      {{ successMessage }}
    </div>

    <!-- Main Workspace -->
    <div class="flex-1 grid grid-cols-1 xl:grid-cols-4 gap-6 overflow-hidden">
      
      <!-- Panel de Configuración Global -->
      <div class="xl:col-span-1 bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6 flex flex-col space-y-6">
        <div>
          <h2 class="text-lg font-bold text-gray-900 dark:text-white mb-4">Detalles del Proyecto</h2>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Título de Plantilla *</label>
              <input 
                v-model="store.draft.title" 
                type="text" 
                placeholder="Ej. Implementación Core CRM"
                class="w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white p-2.5 border"
              >
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Descripción</label>
              <textarea 
                v-model="store.draft.description" 
                rows="4"
                placeholder="Descripción del alcance macro de esta plantilla..."
                class="w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white p-2.5 border resize-none"
              ></textarea>
            </div>
          </div>
        </div>

        <div class="pt-4 border-t border-gray-200 dark:border-gray-700 mt-auto">
          <div class="flex justify-between text-sm text-gray-500 dark:text-gray-400 mb-2">
            <span>Fases Definidas:</span>
            <span class="font-bold text-gray-900 dark:text-white">{{ store.draft.phases.length }}</span>
          </div>
          <div class="flex justify-between text-sm text-gray-500 dark:text-gray-400">
            <span>Tareas Totales:</span>
            <span class="font-bold text-gray-900 dark:text-white">{{ totalTasks }}</span>
          </div>
        </div>
      </div>

      <!-- Lienzo WBS (Fases y Tareas) -->
      <div class="xl:col-span-3 bg-gray-100 dark:bg-gray-900/50 rounded-xl border border-gray-200 dark:border-gray-700 p-6 overflow-y-auto">
        <div class="space-y-6">
          
          <!-- Iteración de Fases -->
          <div 
            v-for="(phase, index) in store.draft.phases" 
            :key="phase.id"
            class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden"
          >
            <!-- Cabecera de Fase -->
            <div class="px-5 py-4 bg-gray-50 dark:bg-gray-800/80 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between group">
              <div class="flex items-center space-x-3 flex-1">
                <div class="cursor-grab text-gray-400 hover:text-gray-600 dark:hover:text-gray-300">
                   <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path></svg>
                </div>
                <div class="flex items-center space-x-2 w-full max-w-sm">
                  <span class="text-xs font-bold text-blue-600 dark:text-blue-400 uppercase tracking-wider">Fase {{ index + 1 }}</span>
                  <input 
                    v-model="phase.name" 
                    type="text" 
                    placeholder="Nombre de la Fase"
                    class="bg-transparent text-lg font-bold text-gray-900 dark:text-white border-0 border-b border-transparent hover:border-gray-300 dark:hover:border-gray-600 focus:border-blue-500 focus:ring-0 p-1 w-full"
                  >
                </div>
              </div>
              <button @click="store.removePhase(phase.id)" class="text-gray-400 hover:text-red-500 opacity-0 group-hover:opacity-100 transition-opacity">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
              </button>
            </div>

            <!-- Lista de Tareas -->
            <div class="p-5 space-y-3">
              <div 
                v-for="task in phase.tasks" 
                :key="task.id"
                class="flex items-center space-x-3 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg p-3 group hover:border-blue-300 dark:hover:border-blue-700 transition-colors"
               >
                 <div class="cursor-grab text-gray-400 hover:text-gray-600 dark:hover:text-gray-300">
                   <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 16 16"><path d="M7 2a1 1 0 11-2 0 1 1 0 012 0zm3 0a1 1 0 11-2 0 1 1 0 012 0zM7 5a1 1 0 11-2 0 1 1 0 012 0zm3 0a1 1 0 11-2 0 1 1 0 012 0zM7 8a1 1 0 11-2 0 1 1 0 012 0zm3 0a1 1 0 11-2 0 1 1 0 012 0zM7 11a1 1 0 11-2 0 1 1 0 012 0zm3 0a1 1 0 11-2 0 1 1 0 012 0zM7 14a1 1 0 11-2 0 1 1 0 012 0zm3 0a1 1 0 11-2 0 1 1 0 012 0z"/></svg>
                 </div>
                 <div class="flex-1 flex flex-col sm:flex-row sm:items-center gap-3">
                    <input 
                      v-model="task.name" 
                      type="text" 
                      placeholder="Nombre de la Tarea"
                      class="flex-1 rounded border-gray-300 text-sm focus:border-blue-500 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white p-2 border"
                    >
                    <div class="flex items-center space-x-2">
                       <span class="text-xs text-gray-500 dark:text-gray-400">Duración:</span>
                       <input 
                         v-model.number="task.estimatedHours" 
                         type="number" 
                         min="1"
                         class="w-20 rounded border-gray-300 text-sm focus:border-blue-500 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white p-2 border"
                         placeholder="Hrs"
                       >
                    </div>
                 </div>
                 <button @click="store.removeTaskFromPhase(phase.id, task.id)" class="text-gray-400 hover:text-red-500 opacity-0 group-hover:opacity-100 transition-opacity">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
                 </button>
              </div>

              <!-- Empty State Tareas -->
              <div v-if="phase.tasks.length === 0" class="text-center py-4 text-sm text-gray-500 dark:text-gray-400 border-2 border-dashed border-gray-200 dark:border-gray-700 rounded-lg">
                No hay tareas en esta fase. Arrastre aquí o haga clic en "Afegir Tarea".
              </div>

              <button @click="store.addTaskToPhase(phase.id)" class="text-sm font-medium text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-300 inline-flex items-center mt-2 p-1 rounded hover:bg-blue-50 dark:hover:bg-blue-900/30 transition-colors">
                <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path></svg>
                Agregar Tarea
              </button>
            </div>
          </div>
          
          <!-- Botón Agregar Fase -->
          <button @click="store.addPhase" class="w-full py-4 border-2 border-dashed border-gray-300 dark:border-gray-600 rounded-xl text-gray-500 dark:text-gray-400 hover:border-blue-500 hover:text-blue-600 dark:hover:text-blue-400 dark:hover:border-blue-400 flex flex-col items-center justify-center transition-colors group bg-white/50 dark:bg-gray-800/50 hover:bg-blue-50 dark:hover:bg-blue-900/20">
            <svg class="w-8 h-8 mb-2 text-gray-400 group-hover:text-blue-500 transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path></svg>
            <span class="font-medium text-sm">Añadir Nueva Fase</span>
          </button>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useProjectBuilderStore } from '@/stores/projectBuilder';

const store = useProjectBuilderStore();
const successMessage = ref('');

// Para simplicidad en UX, iniciamos con 1 fase vacía si no hay nada
if (store.draft.phases.length === 0) {
  store.addPhase();
}

const totalTasks = computed(() => {
  return store.draft.phases.reduce((acc, phase) => acc + phase.tasks.length, 0);
});

const isValid = computed(() => {
  if (!store.draft.title.trim()) return false;
  if (store.draft.phases.length === 0) return false;
  return store.draft.phases.every(p => p.name.trim() !== '' && p.tasks.length > 0 && p.tasks.every(t => t.name.trim() !== ''));
});

const submitWbs = async () => {
  if (!isValid.value) return;
  
  successMessage.value = '';
  try {
    await store.saveProjectTemplate();
    
    successMessage.value = `¡Plantilla "${store.draft.title}" guardada exitosamente en Arquitectura!`;
    console.log("WBS Payload Generated:", JSON.parse(JSON.stringify(store.draft)));
    
    // Limpieza post save
    store.draft.title = '';
    store.draft.description = '';
    store.draft.phases = [];
    store.addPhase();
    
    // Auto descartar alert
    setTimeout(() => { successMessage.value = ''; }, 5000);
  } catch (error) {
    console.error(error);
  }
};
</script>
