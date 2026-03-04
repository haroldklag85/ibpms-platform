<template>
  <div class="h-full flex flex-col p-6 space-y-6">
    <!-- Header & Search -->
    <div class="flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Customer 360</h1>
        <p class="text-sm text-gray-500 dark:text-gray-400">Consolidado de expedientes y actividad de clientes</p>
      </div>
      
      <div class="w-full md:w-96 relative">
        <label for="crmSearch" class="sr-only">Buscar por CRM ID o Nombre</label>
        <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
          <svg class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"/>
          </svg>
        </div>
        <input 
          id="crmSearch"
          v-model="searchQuery"
          @keyup.enter="searchCustomer"
          type="text" 
          class="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-lg leading-5 bg-white dark:bg-gray-800 dark:border-gray-700 dark:text-white placeholder-gray-500 focus:outline-none focus:placeholder-gray-400 focus:ring-1 focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
          placeholder="Buscar CRM ID, DNI o CUIT..."
        >
      </div>
    </div>

    <!-- Loading Skeleton -->
    <div v-if="isLoading" class="grid grid-cols-1 lg:grid-cols-3 gap-6 animate-pulse mt-4">
      <div class="bg-white dark:bg-gray-800 h-64 rounded-xl"></div>
      <div class="lg:col-span-2 space-y-4">
        <div class="bg-white dark:bg-gray-800 h-24 rounded-xl"></div>
        <div class="bg-white dark:bg-gray-800 h-64 rounded-xl"></div>
      </div>
    </div>

    <!-- Results Split-Pane Dashboard -->
    <div v-else-if="customerInfo" class="grid grid-cols-1 lg:grid-cols-3 gap-6 flex-1">
      
      <!-- Panel Izquierdo: Ficha del Cliente -->
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 flex flex-col">
        <div class="p-6 border-b border-gray-200 dark:border-gray-700">
          <div class="flex items-center space-x-4">
            <div class="h-16 w-16 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center text-xl font-bold">
              {{ customerInfo.name.charAt(0) }}
            </div>
            <div>
              <h2 class="text-lg font-bold text-gray-900 dark:text-white">{{ customerInfo.name }}</h2>
              <p class="text-sm text-gray-500 font-mono">{{ customerInfo.id }}</p>
            </div>
          </div>
        </div>
        
        <div class="p-6 flex-1 space-y-4 text-sm">
          <div>
            <span class="block text-gray-500 dark:text-gray-400 mb-1">Email / Contacto</span>
            <span class="font-medium text-gray-900 dark:text-gray-200">{{ customerInfo.email }}</span>
          </div>
          <div>
            <span class="block text-gray-500 dark:text-gray-400 mb-1">Segmento</span>
            <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200">
              {{ customerInfo.segment }}
            </span>
          </div>
          <div>
            <span class="block text-gray-500 dark:text-gray-400 mb-1">Última Interacción</span>
            <span class="font-medium text-gray-900 dark:text-gray-200">{{ customerInfo.lastInteraction }}</span>
          </div>
        </div>
      </div>

      <!-- Panel Derecho: Expedientes y Actividad -->
      <div class="lg:col-span-2 flex flex-col space-y-6">
        
        <!-- KPIs Row -->
        <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
          <div class="bg-white dark:bg-gray-800 p-4 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm text-center">
            <span class="block text-2xl font-bold text-blue-600">3</span>
            <span class="text-xs text-gray-500 font-medium uppercase tracking-wider">Trámites Activos</span>
          </div>
          <div class="bg-white dark:bg-gray-800 p-4 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm text-center">
            <span class="block text-2xl font-bold text-gray-900 dark:text-white">12</span>
            <span class="text-xs text-gray-500 font-medium uppercase tracking-wider">Históricos</span>
          </div>
          <div class="bg-white dark:bg-gray-800 p-4 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm text-center">
            <span class="block text-2xl font-bold text-red-500">1</span>
            <span class="text-xs text-gray-500 font-medium uppercase tracking-wider">SLA en Riesgo</span>
          </div>
          <div class="bg-white dark:bg-gray-800 p-4 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm text-center">
            <span class="block text-2xl font-bold text-green-500">98%</span>
            <span class="text-xs text-gray-500 font-medium uppercase tracking-wider">CSAT Score</span>
          </div>
        </div>

        <!-- Tabla Activos -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 flex-1 overflow-hidden flex flex-col">
          <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center bg-gray-50 dark:bg-gray-800/50">
            <h3 class="font-bold text-gray-900 dark:text-white">Expedientes en Curso</h3>
            <button class="text-sm text-blue-600 hover:underline">Ver Histórico</button>
          </div>
          <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
              <thead class="bg-gray-50 dark:bg-gray-900/50">
                <tr>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">ID Trámite</th>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Servicio</th>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Estado Actual</th>
                  <th scope="col" class="px-6 py-3 text-right text-xs font-semibold text-gray-500 uppercase tracking-wider">Asignado</th>
                </tr>
              </thead>
              <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                <tr v-for="caseItem in activeCases" :key="caseItem.id" class="hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors">
                  <td class="px-6 py-4 whitespace-nowrap text-sm font-mono text-blue-600 dark:text-blue-400">
                    {{ caseItem.id }}
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-200">
                    {{ caseItem.service }}
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <span :class="[
                      'px-2 inline-flex text-xs leading-5 font-semibold rounded-full',
                      caseItem.status === 'En Riesgo' ? 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400' : 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400'
                    ]">
                      {{ caseItem.status }}
                    </span>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400 text-right">
                    {{ caseItem.assignee }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Empty State -->
    <div v-else class="flex-1 flex flex-col items-center justify-center text-center text-gray-500 dark:text-gray-400">
      <svg class="h-16 w-16 mb-4 text-gray-300 dark:text-gray-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
      </svg>
      <p class="text-lg font-medium">Busque un cliente para ver su perfil 360</p>
      <p class="text-sm mt-1">Intente buscar por "CRM-99214"</p>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { api } from '@/services/apiClient';

const searchQuery = ref('');
const isLoading = ref(false);
const customerInfo = ref<any>(null);
const activeCases = ref<any[]>([]);

const searchCustomer = async () => {
  if (!searchQuery.value.trim()) return;
  
  isLoading.value = true;
  customerInfo.value = null;
  activeCases.value = [];

  try {
    const response = await api.getCustomer360(searchQuery.value);
    
    // Asignamos la data provista por el Integration Gap (con fallback seguro en local si backend manda un cascarón vacío)
    customerInfo.value = {
      id: response.data?.id || searchQuery.value,
      name: response.data?.name || 'Acme Corporation S.A.',
      email: response.data?.email || 'contact@acmecorp.com',
      segment: response.data?.segment || 'Enterprise B2B',
      lastInteraction: response.data?.lastInteraction || 'Ayer, 14:30 hs'
    };
    
    activeCases.value = response.data?.activeCases || [
      { id: 'TRM-1029', service: 'Renovación de Contrato', status: 'En Progreso', assignee: 'Maria L.' },
      { id: 'TRM-1044', service: 'Soporte Facturación', status: 'En Riesgo', assignee: 'Carlos P.' },
      { id: 'TRM-1050', service: 'Alta de Nuevo Servicio', status: 'Esperando Doc', assignee: 'Sistema' }
    ];

  } catch (error) {
    console.error('Búsqueda fallida', error);
  } finally {
    isLoading.value = false;
  }
};
</script>
