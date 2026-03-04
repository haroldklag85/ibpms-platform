<template>
  <div class="h-full flex flex-col p-6 space-y-6 bg-gray-50 dark:bg-gray-900">
    
    <!-- Header -->
    <div class="flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Business Activity Monitoring (BAM)</h1>
        <p class="text-sm text-gray-500 dark:text-gray-400">Salud del proceso, rendimiento de SLAs e impacto de IA</p>
      </div>
      <div class="flex items-center space-x-3">
        <select class="rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 dark:bg-gray-800 dark:border-gray-700 dark:text-white text-sm py-2 px-3 border">
          <option>Últimos 30 Días</option>
          <option>Este Año</option>
          <option>Histórico Total</option>
        </select>
        <button 
          @click="fetchMetrics" 
          :disabled="isLoading"
          class="inline-flex items-center px-4 py-2 border border-gray-300 dark:border-gray-600 shadow-sm text-sm font-medium rounded-lg text-gray-700 dark:text-gray-200 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 transition-colors"
        >
          <svg v-if="isLoading" class="animate-spin -ml-1 mr-2 h-4 w-4 text-blue-600 dark:text-blue-400" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <svg v-else class="mr-2 h-4 w-4 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path></svg>
          Actualizar
        </button>
      </div>
    </div>

    <!-- Skeletons (Loading State) -->
    <div v-if="isLoading && !processHealth" class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6 animate-pulse">
      <div v-for="i in 4" :key="i" class="bg-white dark:bg-gray-800 h-32 rounded-xl border border-gray-200 dark:border-gray-700"></div>
    </div>

    <template v-else-if="processHealth && aiMetrics">
      
      <!-- Seccion 1: Process Health (US-009) -->
      <div>
         <h2 class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-4 border-b border-gray-200 dark:border-gray-700 pb-2">Rendimiento Operativo (BPMN Core)</h2>
         <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6">
           
           <!-- Active Cases -->
           <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6 flex items-center space-x-4">
             <div class="p-3 bg-blue-100 text-blue-600 dark:bg-blue-900/30 dark:text-blue-400 rounded-lg">
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path></svg>
             </div>
             <div>
               <p class="text-sm font-medium text-gray-500 dark:text-gray-400">Casos Activos</p>
               <p class="text-3xl font-bold text-gray-900 dark:text-white">{{ processHealth.activeCases }}</p>
             </div>
           </div>

           <!-- Completed Cases -->
           <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6 flex items-center space-x-4">
             <div class="p-3 bg-green-100 text-green-600 dark:bg-green-900/30 dark:text-green-400 rounded-lg">
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
             </div>
             <div>
               <p class="text-sm font-medium text-gray-500 dark:text-gray-400">Casos Históricos</p>
               <p class="text-3xl font-bold text-gray-900 dark:text-white">{{ processHealth.completedCases.toLocaleString() }}</p>
             </div>
           </div>

           <!-- Active Tasks -->
           <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6 flex items-center space-x-4">
             <div class="p-3 bg-yellow-100 text-yellow-600 dark:bg-yellow-900/30 dark:text-yellow-400 rounded-lg">
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path></svg>
             </div>
             <div>
               <p class="text-sm font-medium text-gray-500 dark:text-gray-400">Tareas Pendientes</p>
               <p class="text-3xl font-bold text-gray-900 dark:text-white">{{ processHealth.activeTasks }}</p>
             </div>
           </div>

           <!-- Overdue Tasks -->
           <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6 flex items-center space-x-4 relative overflow-hidden">
             <div class="absolute right-0 top-0 h-full w-2 bg-red-500"></div>
             <div class="p-3 bg-red-100 text-red-600 dark:bg-red-900/30 dark:text-red-400 rounded-lg">
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
             </div>
             <div>
               <p class="text-sm font-medium text-gray-500 dark:text-gray-400">SLA Incumplido</p>
               <p class="text-3xl font-bold text-red-600 dark:text-red-400">{{ processHealth.overdueTasks }}</p>
             </div>
           </div>

         </div>
      </div>

      <!-- Seccion 2: Métricas de IA y Copiloto (US-018) & CSS Charts -->
      <div class="grid grid-cols-1 xl:grid-cols-3 gap-6">
        
        <!-- Tarjetas AI -->
        <div class="xl:col-span-1 space-y-6">
          <h2 class="text-xs font-bold text-purple-600 dark:text-purple-400 uppercase tracking-wider mb-2 border-b border-gray-200 dark:border-gray-700 pb-2">Inteligencia Artificial (NLP)</h2>
          
          <div class="bg-gradient-to-br from-purple-600 to-indigo-700 rounded-xl shadow-lg p-6 text-white relative overflow-hidden">
            <!-- Decorative AI Pattern -->
            <svg class="absolute right-0 bottom-0 opacity-10 w-32 h-32 transform translate-x-4 translate-y-4" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/></svg>
            
            <p class="text-sm font-medium text-purple-100">Certeza de IA Global</p>
            <div class="flex items-baseline space-x-2 mt-1">
              <span class="text-4xl font-extrabold">{{ (aiMetrics.averageSimilarityScore * 100).toFixed(1) }}%</span>
              <span class="text-sm text-green-300 font-medium">↑ 2.1%</span>
            </div>
            <div class="mt-4 w-full bg-purple-900/50 rounded-full h-2">
              <div class="bg-green-400 h-2 rounded-full" :style="{ width: `${aiMetrics.averageSimilarityScore * 100}%` }"></div>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-4text-center flex flex-col items-center justify-center p-4">
              <span class="text-xs text-gray-500 font-medium uppercase tracking-wider mb-1">DMN Generados</span>
              <span class="text-2xl font-bold tracking-tight text-gray-900 dark:text-white">{{ aiMetrics.generatedDmns }}</span>
            </div>
            <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-4 text-center flex flex-col items-center justify-center p-4">
               <span class="text-xs text-gray-500 font-medium uppercase tracking-wider mb-1">Correos Triados</span>
               <span class="text-2xl font-bold tracking-tight text-gray-900 dark:text-white">{{ aiMetrics.autoRoutedEmails }}</span>
            </div>
          </div>
          
          <div class="bg-indigo-50 dark:bg-indigo-900/20 rounded-xl border border-indigo-100 dark:border-indigo-800 p-4 flex items-start space-x-3">
             <svg class="h-5 w-5 text-indigo-500 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path></svg>
             <div>
               <p class="text-sm font-bold text-indigo-900 dark:text-indigo-300">Total Eventos IA: {{ aiMetrics.totalAiEvents.toLocaleString() }}</p>
               <p class="text-xs text-indigo-700 dark:text-indigo-400 mt-1">El modelo Llama3 procesó {{ aiMetrics.totalAiEvents }} transacciones este mes, ahorrando un estimado de 420 hrs humanas.</p>
             </div>
          </div>

        </div>

        <!-- Dashboard Visual Chart (CSS Based) -->
        <div class="xl:col-span-2 space-y-6 flex flex-col">
          <h2 class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-2 border-b border-gray-200 dark:border-gray-700 pb-2">Distribución de Tareas</h2>
          
          <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6 flex-1 flex flex-col">
            <div class="flex justify-between items-center mb-6">
              <h3 class="text-sm font-bold text-gray-700 dark:text-gray-300">Carga Operativa vs IA</h3>
              <div class="flex space-x-4 text-xs">
                <div class="flex items-center"><span class="w-3 h-3 rounded bg-blue-500 mr-2"></span>Casos Radicados</div>
                <div class="flex items-center"><span class="w-3 h-3 rounded bg-purple-500 mr-2"></span>Rutas de IA</div>
              </div>
            </div>

            <!-- CSS Bar Chart (Mocked data illustration) -->
            <div class="flex-1 flex items-end space-x-2 sm:space-x-6 h-64 mt-4 px-2">
              
              <div v-for="month in ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun']" :key="month" class="flex-1 flex flex-col items-center justify-end h-full">
                <div class="w-full flex justify-center items-end space-x-1 sm:space-x-2 h-full pb-2 border-b border-gray-200 dark:border-gray-700 relative group">
                  
                  <!-- Tooltips -->
                  <div class="absolute -top-10 bg-gray-900 text-white text-xs rounded py-1 px-2 opacity-0 group-hover:opacity-100 transition-opacity z-10 whitespace-nowrappointer-events-none">
                    Total: {{ Math.floor(Math.random() * 500) + 100 }}
                  </div>

                  <!-- Bar 1 (Casos) -->
                  <div 
                    class="w-full max-w-[2rem] bg-blue-500 rounded-t-sm hover:bg-blue-400 transition-all duration-500"
                    :style="{ height: `${Math.floor(Math.random() * 60) + 30}%` }"
                  ></div>
                  <!-- Bar 2 (IA) -->
                  <div 
                    class="w-full max-w-[2rem] bg-purple-500 rounded-t-sm hover:bg-purple-400 transition-all duration-500"
                    :style="{ height: `${Math.floor(Math.random() * 40) + 10}%` }"
                  ></div>
                </div>
                <!-- Label -->
                <span class="text-xs text-gray-500 mt-2 font-medium">{{ month }}</span>
              </div>

            </div>
          </div>
        </div>

      </div>

    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { api } from '@/services/apiClient';
const isLoading = ref(true);
const processHealth = ref<any>(null);
const aiMetrics = ref<any>(null);

const fetchMetrics = async () => {
  isLoading.value = true;
  
  try {
    const [healthRes, aiRes] = await Promise.all([
      api.getProcessHealth(),
      api.getAiMetrics()
    ]);
    
    processHealth.value = healthRes.data;
    aiMetrics.value = aiRes.data;

  } catch (err) {
    console.error("Hubo un error cargando las métricas de BAM", err);
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  fetchMetrics();
});
</script>
