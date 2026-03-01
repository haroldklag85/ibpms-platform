<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 flex flex-col font-sans text-gray-800 dark:text-gray-200">
    
    <!-- Public Header -->
    <header class="bg-white dark:bg-gray-800 shadow-sm border-b border-gray-200 dark:border-gray-700 h-16 flex items-center justify-between px-6 sm:px-10 shrink-0">
      <div class="flex items-center space-x-3">
        <div class="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center">
          <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path></svg>
        </div>
        <span class="text-xl font-bold tracking-tight text-gray-900 dark:text-white">GovServices Portal</span>
      </div>
      <div>
        <button class="text-sm font-medium text-blue-600 dark:text-blue-400 hover:text-blue-800 dark:hover:text-blue-300">
          Oficina Virtual
        </button>
      </div>
    </header>

    <!-- Main Content -->
    <main class="flex-1 flex flex-col items-center justify-center p-6 w-full max-w-4xl mx-auto space-y-12">
      
      <!-- Search Hero -->
      <div class="text-center w-full max-w-xl mx-auto space-y-6">
        <h1 class="text-3xl sm:text-4xl font-extrabold text-gray-900 dark:text-white tracking-tight">Rastrea tu Trámite</h1>
        <p class="text-gray-500 dark:text-gray-400 text-lg">Ingresa tu código de seguimiento (Tracking ID) para conocer el estado actual de tu solicitud.</p>
        
        <form @submit.prevent="searchTracking" class="relative mt-8 flex items-center">
          <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
            <svg class="w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
          </div>
          <input 
            v-model="trackingCode" 
            type="text" 
            class="block w-full pl-12 pr-32 py-4 border border-gray-300 rounded-xl leading-5 bg-white xl:text-lg dark:bg-gray-800 dark:border-gray-700 dark:text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 shadow-sm transition-all"
            placeholder="Ej. TRK-2024-9981A"
            required
          >
          <button 
            type="submit" 
            :disabled="isLoading"
            class="absolute right-2 top-2 bottom-2 px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white font-bold rounded-lg shadow-sm disabled:opacity-75 disabled:cursor-wait transition-colors"
          >
            <span v-if="!isLoading">Consultar</span>
            <svg v-else class="animate-spin h-5 w-5 mx-auto" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
          </button>
        </form>
      </div>

      <!-- Tracking Results (Hidden until searched) -->
      <div v-if="trackingResult" class="w-full bg-white dark:bg-gray-800 rounded-2xl shadow-xl border border-gray-100 dark:border-gray-700 overflow-hidden transform transition-all animate-fade-in-up">
        
        <!-- Headers Case Info -->
        <div class="p-6 sm:p-8 border-b border-gray-100 dark:border-gray-700 bg-gray-50/50 dark:bg-gray-800/50 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h2 class="text-xl font-bold text-gray-900 dark:text-white">Renovación de Licencia Comercial</h2>
            <p class="text-sm text-gray-500 dark:text-gray-400 mt-1 font-mono">Tracking ID: {{ trackingResult.id }}</p>
          </div>
          <div class="text-left sm:text-right">
            <span class="inline-flex items-center px-3 py-1 rounded-full text-sm font-bold bg-blue-100 text-blue-800 dark:bg-blue-900/40 dark:text-blue-300 border border-blue-200 dark:border-blue-800">
              En Revisión
            </span>
            <p class="text-xs text-gray-500 mt-2">Última actualización: Hoy, 10:30 AM</p>
          </div>
        </div>

        <!-- Progress Bar Visual -->
        <div class="p-6 sm:p-10">
          <div class="relative">
            <!-- Background Line -->
            <div class="absolute inset-0 flex items-center" aria-hidden="true">
              <div class="w-full border-t-2 border-gray-200 dark:border-gray-700"></div>
            </div>
            <!-- Progress Line -->
            <div class="absolute inset-0 flex items-center" aria-hidden="true">
              <div class="w-1/2 border-t-2 border-blue-600 transition-all duration-1000 ease-out"></div>
            </div>
            
            <!-- Milestones -->
            <div class="relative flex justify-between">
              
              <!-- Step 1: DONE -->
              <div class="flex flex-col items-center">
                <div class="h-8 w-8 rounded-full bg-blue-600 flex items-center justify-center ring-4 ring-white dark:ring-gray-800 z-10">
                  <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                </div>
                <div class="mt-4 text-center">
                  <p class="text-sm font-bold text-gray-900 dark:text-white">Iniciado</p>
                  <p class="text-xs text-gray-500 mt-1 hidden sm:block">12 Oct 2024</p>
                </div>
              </div>

              <!-- Step 2: DONE -->
              <div class="flex flex-col items-center">
                <div class="h-8 w-8 rounded-full bg-blue-600 flex items-center justify-center ring-4 ring-white dark:ring-gray-800 z-10">
                  <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                </div>
                <div class="mt-4 text-center">
                  <p class="text-sm font-bold text-gray-900 dark:text-white">Radicado</p>
                  <p class="text-xs text-gray-500 mt-1 hidden sm:block">12 Oct 2024</p>
                </div>
              </div>

              <!-- Step 3: CURRENT -->
              <div class="flex flex-col items-center">
                <div class="h-8 w-8 rounded-full bg-white border-2 border-blue-600 flex items-center justify-center ring-4 ring-white dark:bg-gray-800 dark:ring-gray-800 z-10">
                  <div class="h-2.5 w-2.5 bg-blue-600 rounded-full animate-pulse"></div>
                </div>
                <div class="mt-4 text-center">
                  <p class="text-sm font-bold text-blue-600 dark:text-blue-400">En Revisión Legal</p>
                  <p class="text-xs text-blue-500 mt-1 font-medium hidden sm:block">Fase Actual</p>
                </div>
              </div>

              <!-- Step 4: PENDING -->
              <div class="flex flex-col items-center">
                <div class="h-8 w-8 rounded-full bg-white border-2 border-gray-300 flex items-center justify-center ring-4 ring-white dark:bg-gray-800 dark:border-gray-600 dark:ring-gray-800 z-10">
                </div>
                <div class="mt-4 text-center">
                  <p class="text-sm font-medium text-gray-500 dark:text-gray-400">Resolución</p>
                </div>
              </div>

            </div>
          </div>
        </div>
        
        <!-- Detailed Timeline Message -->
        <div class="bg-blue-50 dark:bg-blue-900/20 p-6 sm:px-10 flex items-start space-x-4">
          <div class="mt-0.5"><i class="fa-solid fa-circle-info text-blue-600 dark:text-blue-400"></i></div>
          <div>
            <h4 class="text-sm font-bold text-blue-900 dark:text-blue-300">Mensaje de la Oficina:</h4>
            <p class="text-sm text-blue-800 dark:text-blue-200/80 mt-1">Su expediente ha superado el triage inicial y está siendo validado por el departamento jurídico. Recibirá una notificación al correo registrado cuando esta fase concluya o si se requieren subsanaciones.</p>
          </div>
        </div>

      </div>

      <!-- Not Found State -->
      <div v-else-if="searchAttempted && !trackingResult" class="text-center animate-fade-in-up">
         <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-red-100 text-red-600 mb-4">
            <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
         </div>
         <h3 class="text-xl font-bold text-gray-900 dark:text-white mb-2">Trámite No Encontrado</h3>
         <p class="text-gray-500 dark:text-gray-400 max-w-md mx-auto">No se encontraron expedientes activos para el código "{{ trackingCode }}". Verifica e intenta nuevamente.</p>
      </div>

    </main>
    
    <!-- Public Footer -->
    <footer class="bg-white dark:bg-gray-800 border-t border-gray-200 dark:border-gray-700 py-6 text-center shrink-0">
      <p class="text-xs text-gray-500 dark:text-gray-400">© 2026 iBPMS GovServices. Todos los derechos reservados. Motorizado por Antigravity iBPMS.</p>
    </footer>

  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const trackingCode = ref('');
const isLoading = ref(false);
const trackingResult = ref<any>(null);
const searchAttempted = ref(false);

const searchTracking = async () => {
  if (!trackingCode.value) return;
  
  isLoading.value = true;
  searchAttempted.value = true;
  trackingResult.value = null;

  try {
    // API Mock: $http.get(`/api/v1/public/tracking/${trackingCode.value}`)
    await new Promise(resolve => setTimeout(resolve, 1500));
    
    // Simulate valid tracking code ending in 'A'
    if (trackingCode.value.toUpperCase().endsWith('A')) {
      trackingResult.value = {
        id: trackingCode.value.toUpperCase(),
        status: 'En Revisión Legal'
      };
    }
  } catch (error) {
    console.error('Error fetching tracking data', error);
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in-up {
  animation: fadeInUp 0.5s ease-out forwards;
}
</style>
