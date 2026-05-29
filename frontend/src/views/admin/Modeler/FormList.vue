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

          <button @click="router.push('/admin/modeler/forms/designer')" class="px-4 py-2 bg-indigo-600 text-white font-semibold rounded-lg hover:bg-indigo-700 shadow-sm transition flex items-center gap-2 whitespace-nowrap">
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
              <th scope="col" class="px-6 py-4 text-left text-xs font-bold text-gray-500 uppercase tracking-wider">Tipo</th>
              <th scope="col" class="px-6 py-4 text-left text-xs font-bold text-gray-500 uppercase tracking-wider">Versión Activa</th>
              <th scope="col" class="px-6 py-4 text-left text-xs font-bold text-gray-500 uppercase tracking-wider">Última Modificación</th>
              <th scope="col" class="px-6 py-4 text-center text-xs font-bold text-gray-500 uppercase tracking-wider">Acciones de Gobernanza</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
             <tr v-if="isLoading">
               <td colspan="6" class="p-8 text-center text-gray-400 font-medium animate-pulse">Cargando diccionario...</td>
             </tr>
             <tr v-else-if="forms.length === 0">
               <td colspan="6" class="p-8 text-center text-gray-500 font-medium">Bóveda vacía. No existen formularios.</td>
             </tr>
             <tr v-for="form in forms" :key="form.id" @click="router.push(`/admin/modeler/forms/designer?id=${form.id}`)" class="hover:bg-indigo-50/50 transition-colors cursor-pointer group">
               <td class="px-6 py-4 whitespace-nowrap">
                  <div class="font-mono text-sm font-semibold text-indigo-700">{{ form.id }}</div>
                  <div class="text-xs text-gray-400 mt-0.5">Autor: {{ form.author || 'Sistema' }}</div>
               </td>
               <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm font-bold text-gray-900">{{ form.name }}</div>
                  <div class="text-[11px] text-gray-500 mt-0.5 uppercase tracking-wide">{{ form.description || 'Sin descripción' }}</div>
               </td>
               <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-700 font-medium">
                  <span class="px-2.5 py-1 inline-flex text-[11px] leading-4 font-bold rounded-md" 
                        :class="form.pattern === 'IFORM_MAESTRO' || form.pattern === 'iForm Maestro' ? 'bg-blue-50 text-blue-700 border border-blue-200' : 'bg-green-50 text-green-700 border border-green-200'">
                    {{ form.pattern === 'IFORM_MAESTRO' || form.pattern === 'iForm Maestro' ? 'iForm Maestro' : 'Simple' }}
                  </span>
               </td>
               <td class="px-6 py-4 whitespace-nowrap">
                  <span class="px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full bg-emerald-100 text-emerald-800 border border-emerald-200 shadow-sm">
                    Versión {{ form.version || '1.0' }}
                  </span>
               </td>
               <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 font-mono">
                  {{ form.updatedAt ? form.updatedAt : '---' }}
               </td>
               <td class="px-6 py-4 whitespace-nowrap text-center space-x-3">
                  <button @click.stop="router.push(`/admin/modeler/forms/designer?id=${form.id}`)" class="text-indigo-600 hover:text-indigo-900 font-medium text-sm transition-colors" title="Editar Arquitectura">
                    <span class="material-symbols-outlined text-[20px] align-middle">edit</span>
                  </button>
                  <button @click.stop="router.push(`/admin/modeler/forms/designer?id=${form.id}&showHistory=true`)" class="text-amber-600 hover:text-amber-800 font-medium text-sm transition-colors history-btn" title="Historial de Versiones">
                    <span class="material-symbols-outlined text-[20px] align-middle">history</span>
                  </button>
                  <button @click.stop="confirmDelete(form.id)" class="text-red-500 hover:text-red-700 font-medium text-sm transition-colors delete-btn" title="Eliminar Registro">
                    <span class="material-symbols-outlined text-[20px] align-middle">delete</span>
                  </button>
               </td>
             </tr>
          </tbody>
        </table>
      </div>

    </div>
  </div>

  <!-- MODAL DE CONFIRMACIÓN DE BORRADO (LEY 5) -->
  <div v-if="showDeleteModal" class="delete-confirm-modal fixed inset-0 bg-gray-900/60 flex items-center justify-center z-[1000] p-4 backdrop-blur-sm">
    <div id="deleteConfirmModal" class="bg-white rounded-xl shadow-2xl p-6 max-w-md w-full border border-gray-100">
      <div class="flex items-center gap-3 text-red-600 mb-4">
        <span class="material-symbols-outlined text-3xl">warning</span>
        <h3 class="text-lg font-bold">Advertencia de Integridad</h3>
      </div>
      <p class="text-sm text-gray-600 mb-6">
        ¿Está seguro de eliminar el formulario <strong>[{{ formIdToDelete }}]</strong> de la bóveda? Esta acción no se puede deshacer.
      </p>
      <div class="flex justify-end gap-3">
        <button @click="showDeleteModal = false" class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 text-sm font-semibold transition">
          Cancelar
        </button>
        <button @click="executeDelete" class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 text-sm font-semibold shadow-sm transition">
          Sí, Confirmar
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useIntegrationStore } from '@/stores/useIntegrationStore';
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

// @Traceability: US-003 - CA-86
const integrationStore = useIntegrationStore();
const router = useRouter();

const forms = ref<any[]>([]);
const isLoading = ref(true);
const alertMsg = ref('');
const alertType = ref<'success' | 'error'>('success');
const showDeleteModal = ref(false);
const formIdToDelete = ref<string | null>(null);

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
        const response = await integrationStore.get(`/api/v1/forms${queryParam}`);
        forms.value = response.data || [];
    } catch (error) {
        showAlert('Error recuperando diccionario de formularios.', 'error');
    } finally {
        isLoading.value = false;
    }
};

const confirmDelete = (id: string) => {
    formIdToDelete.value = id;
    showDeleteModal.value = true;
};

const executeDelete = async () => {
    const id = formIdToDelete.value;
    showDeleteModal.value = false;
    if (!id) return;
    
    try {
        await integrationStore.delete(`/api/v1/forms/${id}`);
        showAlert(`El formulario ${id} fue disipado de la bóveda.`, 'success');
        fetchForms();
    } catch (error: any) {
        // GAP 5 - Mitigación In-Flight
        if (error.response?.status === 409) {
            showAlert(`⛔ Conflicto Físico (Http 409): Operación Abortada. El formulario [${id}] está anclado a instanciaciones In-Flight activas.`, 'error');
        } else {
            showAlert(`Vulnerabilidad detectada borrando el formulario. ${error.message}`, 'error');
        }
    } finally {
        formIdToDelete.value = null;
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
