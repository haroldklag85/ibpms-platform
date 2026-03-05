<template>
  <div class="h-full flex flex-col p-6 space-y-6 bg-gray-50 dark:bg-gray-900">
    <!-- Header -->
    <div class="flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Admin: Orígenes SAC (Mailboxes)</h1>
        <p class="text-sm text-gray-500 dark:text-gray-400">Configuración de Canales Inbound (MS Graph API)</p>
      </div>
      <button 
        @click="openModal"
        class="inline-flex items-center px-4 py-2 border border-transparent rounded-lg shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors"
      >
        <svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path></svg>
        Añadir Buzón
      </button>
    </div>

    <!-- IMAP Warning Banner (Master Prompt Requirement) -->
    <div class="bg-amber-50 border-l-4 border-amber-400 p-4 rounded-md">
      <div class="flex">
        <div class="flex-shrink-0">
          <svg class="h-5 w-5 text-amber-400" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd" /></svg>
        </div>
        <div class="ml-3">
          <p class="text-sm text-amber-700">
            <strong>Arquitectura V1 Strict:</strong> El protocolo <strong>IMAP</strong> está <span class="font-bold underline">Deprecado</span> por Microsoft. Esta interfaz fuerza exclusivamente conexiones modernas OAuth 2.0 (Microsoft Graph API). Las credenciales Secret son inyectadas en Azure Key Vault.
          </p>
        </div>
      </div>
    </div>

    <!-- Error/Sucess Alerts -->
    <div v-if="successMsg" class="bg-green-50 border border-green-200 text-green-800 rounded-lg p-4 flex items-center transition-opacity">
      <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
      {{ successMsg }}
    </div>
    <div v-if="errorMsg" class="bg-red-50 border border-red-200 text-red-800 rounded-lg p-4 flex items-center transition-opacity">
      <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
      {{ errorMsg }}
    </div>

    <!-- Mailboxes Grid -->
    <div class="bg-white dark:bg-gray-800 shadow-sm rounded-xl border border-gray-200 dark:border-gray-700 overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
        <thead class="bg-gray-50 dark:bg-gray-800/50">
          <tr>
             <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Alias / ID</th>
             <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tenant (AAD)</th>
             <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Default Process ID</th>
             <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Estado de Polling (Emergency Toggle)</th>
          </tr>
        </thead>
        <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
          <tr v-if="isLoading">
            <td colspan="4" class="px-6 py-4 text-center text-sm text-gray-500 dark:text-gray-400">Cargando buzones...</td>
          </tr>
          <tr v-else-if="mailboxes.length === 0">
            <td colspan="4" class="px-6 py-4 text-center text-sm text-gray-500 dark:text-gray-400">No hay buzones configurados.</td>
          </tr>
          <tr v-for="mbox in mailboxes" :key="mbox.id" class="hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors">
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="flex items-center">
                <div class="flex-shrink-0 h-10 w-10 bg-blue-100 dark:bg-blue-900/30 rounded-full flex items-center justify-center text-blue-600 dark:text-blue-400">
                   <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path></svg>
                </div>
                <div class="ml-4">
                  <div class="text-sm font-medium text-gray-900 dark:text-white">{{ mbox.alias }}</div>
                  <div class="text-xs text-gray-500 dark:text-gray-400">Procolo: {{ mbox.protocol }}</div>
                </div>
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
               <div class="text-sm text-gray-900 dark:text-gray-300">{{ mbox.tenantId }}</div>
               <div class="text-xs text-gray-500 font-mono">{{ truncate(mbox.clientId) }}</div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
               <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-indigo-100 text-indigo-800 dark:bg-indigo-900/30 dark:text-indigo-400">
                 {{ mbox.defaultBpmnProcessId }}
               </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
               <!-- Emergency Toggle CA-8 -->
               <label class="relative inline-flex items-center cursor-pointer">
                 <input type="checkbox" :checked="mbox.active" @change="toggleMailboxStatus(mbox)" class="sr-only peer">
                 <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 dark:peer-focus:ring-blue-800 rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-blue-600"></div>
                 <span class="ml-3 text-sm font-medium text-gray-900 dark:text-gray-300">
                   {{ mbox.active ? 'Escuchando' : 'PAUSA EMERGENCIA' }}
                 </span>
               </label>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal Form CRUD -->
    <div v-if="isModalOpen" class="fixed inset-0 z-50 overflow-y-auto" aria-labelledby="modal-title" role="dialog" aria-modal="true">
      <div class="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
        
        <div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" aria-hidden="true" @click="closeModal"></div>

        <span class="hidden sm:inline-block sm:align-middle sm:h-screen" aria-hidden="true">&#8203;</span>

        <div class="inline-block align-bottom bg-white dark:bg-gray-800 rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-xl sm:w-full border border-gray-200 dark:border-gray-700">
          <div class="bg-white dark:bg-gray-800 px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
            <div class="sm:flex sm:items-start">
              <div class="mt-3 text-center sm:mt-0 sm:mx-4 sm:text-left w-full">
                <h3 class="text-lg leading-6 font-medium text-gray-900 dark:text-white" id="modal-title">
                  Configurar Nuevo Buzón OAuth 2.0
                </h3>
                <div class="mt-4 space-y-4">
                  <!-- Form Fields -->
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Alias del Canal</label>
                    <input v-model="form.alias" type="text" placeholder="Ej: Soporte VIP" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white">
                  </div>
                  <div class="grid grid-cols-2 gap-4">
                     <div>
                       <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Tenant ID (AAD)</label>
                       <input v-model="form.tenantId" type="text" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white">
                     </div>
                     <div>
                       <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Protocolo</label>
                       <!-- Deshabilitado forzando GRAPH -->
                       <input value="GRAPH" disabled type="text" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm bg-gray-100 dark:bg-gray-900 text-gray-500 cursor-not-allowed">
                     </div>
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Client ID (App ID)</label>
                    <input v-model="form.clientId" type="text" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white">
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Raw Client Secret</label>
                    <input v-model="form.rawClientSecret" type="password" placeholder="Key Vault blindará esto en Backend" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white">
                  </div>
                  <div class="pt-2">
                     <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 text-amber-600 dark:text-amber-500 mb-1">
                       <svg class="w-4 h-4 inline mr-1" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M11.3 1.046A1 1 0 0112 2v5h4a1 1 0 01.82 1.573l-7 10A1 1 0 018 18v-5H4a1 1 0 01-.82-1.573l7-10a1 1 0 011.12-.38z" clip-rule="evenodd" /></svg>
                       Fallback Process ID (RAG Failure)
                     </label>
                     <input v-model="form.defaultBpmnProcessId" type="text" placeholder="bpm_incidente_generico" class="block w-full rounded-md border-amber-300 shadow-sm focus:border-amber-500 focus:ring-amber-500 dark:bg-gray-700 dark:border-amber-600 dark:text-white">
                  </div>
                </div>
                
                <!-- Connection Testing Box -->
                <div class="mt-6 p-4 bg-gray-50 dark:bg-gray-900 rounded-lg border border-gray-200 dark:border-gray-700">
                   <div class="flex items-center justify-between">
                      <span class="text-sm text-gray-600 dark:text-gray-400">Paso 1: Validar MS Graph Live</span>
                      <button @click="testLiveConnection" :disabled="isTesting" class="px-3 py-1.5 bg-indigo-100 hover:bg-indigo-200 text-indigo-700 rounded text-sm font-medium transition-colors disabled:opacity-50 inline-flex items-center">
                         <svg v-if="isTesting" class="animate-spin -ml-1 mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
                         [ 🧪 Probar Conexión ]
                      </button>
                   </div>
                   <div v-if="connectionStatus" :class="['mt-2 text-xs font-mono p-2 rounded', connectionStatus.success ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800']">
                      > {{ connectionStatus.msg }}
                   </div>
                </div>

              </div>
            </div>
          </div>
          <div class="bg-gray-50 dark:bg-gray-800/80 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse border-t border-gray-200 dark:border-gray-700">
            <!-- UX Rule: Disabled until 200 OK -->
            <button 
              @click="submitMailbox" 
              :disabled="!connectionStatus?.success || isSaving"
              class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-blue-600 text-base font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 sm:ml-3 sm:w-auto sm:text-sm disabled:opacity-50 disabled:cursor-not-allowed transition-all"
            >
              <svg v-if="isSaving" class="animate-spin -ml-1 mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
              Guardar Configuración
            </button>
            <button @click="closeModal" type="button" class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm dark:bg-gray-700 dark:text-gray-300 dark:border-gray-600 dark:hover:bg-gray-600">
              Cancelar
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import apiClient from '@/services/apiClient';

interface Mailbox {
  id: string;
  alias: string;
  protocol: string;
  tenantId: string;
  clientId: string;
  defaultBpmnProcessId: string;
  active: boolean;
  createdAt?: string;
}

const mailboxes = ref<Mailbox[]>([]);
const isLoading = ref(false);
const isModalOpen = ref(false);
const isTesting = ref(false);
const isSaving = ref(false);

const successMsg = ref('');
const errorMsg = ref('');

const connectionStatus = ref<{success: boolean, msg: string} | null>(null);

const form = reactive({
  alias: '',
  tenantId: '',
  clientId: '',
  rawClientSecret: '',
  defaultBpmnProcessId: ''
});

const loadMailboxes = async () => {
    isLoading.value = true;
    try {
        const response = await apiClient.get('/mailboxes');
        mailboxes.value = response.data;
    } catch (e) {
        console.error(e);
        errorMsg.value = 'Fallo la carga de buzones';
    } finally {
        isLoading.value = false;
    }
};

const openModal = () => {
    form.alias = '';
    form.tenantId = '';
    form.clientId = '';
    form.rawClientSecret = '';
    form.defaultBpmnProcessId = '';
    connectionStatus.value = null;
    isModalOpen.value = true;
};

const closeModal = () => {
    isModalOpen.value = false;
};

const truncate = (val: string) => {
    if (!val) return '';
    return val.substring(0, 8) + '...';
};

const testLiveConnection = async () => {
    if(!form.tenantId || !form.clientId || !form.rawClientSecret) {
        connectionStatus.value = { success: false, msg: 'Error: Complete Tenant, Client ID y Secret' };
        return;
    }
    isTesting.value = true;
    connectionStatus.value = null;
    try {
        const payload = {
            tenantId: form.tenantId,
            clientId: form.clientId,
            rawClientSecret: form.rawClientSecret
        };
        const response = await apiClient.post('/mailboxes/test-connection', payload);
        connectionStatus.value = { success: true, msg: response.data.message || '200 OK: Validated with Graph' };
    } catch (e: any) {
        connectionStatus.value = { 
            success: false, 
            msg: e.response?.data?.message || '400 Bad Request: MS Graph Credenciales Inválidas' 
        };
    } finally {
        isTesting.value = false;
    }
};

const submitMailbox = async () => {
    isSaving.value = true;
    try {
        const payload = {
            alias: form.alias,
            protocol: 'GRAPH',
            tenantId: form.tenantId,
            clientId: form.clientId,
            rawClientSecret: form.rawClientSecret,
            defaultBpmnProcessId: form.defaultBpmnProcessId,
            active: true
        };
        await apiClient.post('/mailboxes', payload);
        successMsg.value = `El Buzón "${form.alias}" fue guardado y el Secret inyectado al Azure Key Vault.`;
        setTimeout(() => successMsg.value = '', 5000);
        closeModal();
        await loadMailboxes();
    } catch (e) {
        errorMsg.value = 'Failed to save mailbox configuration';
        setTimeout(() => errorMsg.value = '', 5000);
    } finally {
        isSaving.value = false;
    }
};

const toggleMailboxStatus = async (mbox: Mailbox) => {
    const originalState = mbox.active;
    mbox.active = !mbox.active;
    try {
        await apiClient.patch(`/mailboxes/${mbox.id}/status`, { active: mbox.active });
        if(!mbox.active) {
             errorMsg.value = `¡ALERTA! Buzón ${mbox.alias} en PAUSA de Emergencia. Correos represados.`;
             setTimeout(() => errorMsg.value = '', 6000);
        } else {
             successMsg.value = `Buzón ${mbox.alias} ha resumido la escucha.`;
             setTimeout(() => successMsg.value = '', 6000);
        }
    } catch (e) {
        mbox.active = originalState; // rollback on fail
        errorMsg.value = 'Error al cambiar estado del polling.';
        setTimeout(() => errorMsg.value = '', 5000);
    }
};

onMounted(() => {
    loadMailboxes();
});
</script>
