<template>
  <div class="p-6 bg-gray-50 min-h-screen">
    <div class="max-w-7xl mx-auto space-y-6">
      <!-- HEADER -->
      <header class="flex justify-between items-center bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex-col sm:flex-row gap-4">
        <div>
          <h1 class="text-2xl font-bold text-gray-800 flex items-center gap-2">
            <span class="material-symbols-outlined text-indigo-600">list_alt</span>
            Gestor de Formularios
          </h1>
          <p class="text-sm text-gray-500 mt-1">Diccionario central de metadatos Zero-Code</p>
        </div>
        
        <div class="flex items-center gap-4 w-full sm:w-auto">
          <!-- SEARCH FIELD (Server-Side) -->
          <div class="relative w-full sm:w-64">
            <span class="absolute inset-y-0 left-0 flex items-center pl-3 text-gray-400">
              <span class="material-symbols-outlined text-[18px]">search</span>
            </span>
            <input type="text" v-model="searchQuery" @input="onSearchInput" placeholder="Buscar por Nombre / API..." class="w-full pl-9 pr-4 py-2 bg-gray-50 hover:bg-white border border-gray-300 focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 rounded-lg text-sm transition-all outline-none">
          </div>

          <button @click="$router.push('/admin/modeler/forms/designer')" class="px-4 py-2 bg-indigo-600 text-white font-semibold rounded-lg hover:bg-indigo-700 shadow-sm transition flex items-center gap-2 whitespace-nowrap">
            <span class="material-symbols-outlined text-sm">add</span> Crear Nuevo
          </button>
        </div>
      </header>

      <!-- ALERTS -->
      <div v-if="alertMsg" :class="alertType === 'error' ? 'bg-red-50 border-red-200 text-red-700' : 'bg-emerald-50 border-emerald-200 text-emerald-700'" class="p-4 rounded-lg flex items-center gap-3 border shadow-sm transition-all">
        <span class="material-symbols-outlined">{{ alertType === 'error' ? 'error' : 'check_circle' }}</span>
        <span class="font-medium text-sm">{{ alertMsg }}</span>
        <button @click="alertMsg = ''" class="ml-auto hover:opacity-75"><span class="material-symbols-outlined text-sm">close</span></button>
      </div>

      <!-- DATAGRID -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th scope="col" class="px-6 py-4 text-left text-xs font-bold text-gray-500 uppercase tracking-wider">Identificador (URI)</th>
              <th scope="col" class="px-6 py-4 text-left text-xs font-bold text-gray-500 uppercase tracking-wider">Nombre del Formulario</th>
              <th scope="col" class="px-6 py-4 text-left text-xs font-bold text-gray-500 uppercase tracking-wider">Versión Activa</th>
              <th scope="col" class="px-6 py-4 text-center text-xs font-bold text-gray-500 uppercase tracking-wider">Acciones de Gobernanza</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
             <tr v-if="isLoading">
               <td colspan="4" class="p-8 text-center text-gray-400 font-medium animate-pulse">Cargando diccionario...</td>
             </tr>
             <tr v-else-if="forms.length === 0">
               <td colspan="4" class="p-8 text-center text-gray-500 font-medium">Bóveda vacía. No existen formularios.</td>
             </tr>
             <tr v-for="form in forms" :key="form.id" @click="$router.push(`/admin/modeler/forms/designer?id=${form.id}`)" class="hover:bg-indigo-50/50 transition-colors cursor-pointer group">
               <td class="px-6 py-4 whitespace-nowrap">
                  <div class="font-mono text-sm font-semibold text-indigo-700">{{ form.id }}</div>
                  <div class="text-xs text-gray-400 mt-0.5">Autor: {{ form.author || 'Sistema' }}</div>
               </td>
               <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm font-bold text-gray-900">{{ form.name }}</div>
                  <div class="text-[11px] text-gray-500 mt-0.5 uppercase tracking-wide">{{ form.description || 'Sin descripción' }}</div>
               </td>
               <td class="px-6 py-4 whitespace-nowrap">
                  <span class="px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full bg-emerald-100 text-emerald-800 border border-emerald-200 shadow-sm">
                    Versión {{ form.version || '1.0' }}
                  </span>
               </td>
               <td class="px-6 py-4 whitespace-nowrap text-center space-x-3">
                  <button @click.stop="$router.push(`/admin/modeler/forms/designer?id=${form.id}`)" class="text-indigo-600 hover:text-indigo-900 font-medium text-sm transition-colors" title="Editar Arquitectura">
                    <span class="material-symbols-outlined text-[20px] align-middle">edit</span>
                  </button>
                  <button @click.stop="deleteForm(form.id)" class="text-red-500 hover:text-red-700 font-medium text-sm transition-colors" title="Eliminar Registro">
                    <span class="material-symbols-outlined text-[20px] align-middle">delete</span>
                  </button>
               </td>
             </tr>
          </tbody>
        </table>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import apiClient from '@/services/apiClient';

const forms = ref<any[]>([]);
const isLoading = ref(true);
const alertMsg = ref('');
const alertType = ref<'success' | 'error'>('success');

// Search Bar State
const searchQuery = ref('');
let searchTimeout: any = null;

const onSearchInput = () => {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        fetchForms();
    }, 400); // Debounce de 400ms Server-Side Search
};

const fetchForms = async () => {
    isLoading.value = true;
    try {
        const queryParam = searchQuery.value ? `?search=${encodeURIComponent(searchQuery.value)}` : '';
        const response = await apiClient.get(`/api/v1/forms${queryParam}`);
        forms.value = response.data || [];
    } catch (error) {
        showAlert('Error recuperando diccionario de formularios.', 'error');
    } finally {
        isLoading.value = false;
    }
};

const deleteForm = async (id: string) => {
    if (!confirm(`¿Advertencia de Integridad: Está seguro de eliminar el formulario [${id}]?`)) return;
    
    try {
        await apiClient.delete(`/api/v1/forms/${id}`);
        showAlert(`El formulario ${id} fue disipado de la bóveda.`, 'success');
        fetchForms();
    } catch (error: any) {
        // GAP 5 - Mitigación In-Flight
        if (error.response?.status === 409) {
            showAlert(`⛔ Conflicto Físico (Http 409): Operación Abortada. El formulario [${id}] está anclado a instanciaciones In-Flight activas.`, 'error');
        } else {
            showAlert(`Vulnerabilidad detectada borrando el formulario. ${error.message}`, 'error');
        }
    }
};

const showAlert = (msg: string, type: 'success' | 'error') => {
    alertMsg.value = msg;
    alertType.value = type;
    setTimeout(() => { alertMsg.value = ''; }, 6000);
};

onMounted(() => {
    fetchForms();
});
</script>
