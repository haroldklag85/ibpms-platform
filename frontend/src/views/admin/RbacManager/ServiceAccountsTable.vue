<template>
  <div class="h-full flex flex-col pb-4">
    <div class="flex justify-between items-center mb-4 mt-2">
      <h2 class="text-lg font-medium text-gray-900">Credenciales de Sistemas (API Keys)</h2>
      <button @click="showCreateModal = true" class="px-3 py-1.5 bg-purple-600 text-white rounded shadow-sm hover:bg-purple-700 text-sm font-medium">
        + Nueva API Key
      </button>
    </div>

    <!-- Table -->
    <div class="overflow-hidden shadow ring-1 ring-black ring-opacity-5 md:rounded-lg">
      <table class="min-w-full divide-y divide-gray-300">
        <thead class="bg-gray-50">
          <tr>
            <th scope="col" class="py-3.5 pl-4 pr-3 text-left text-sm font-semibold text-gray-900">ID / Nombre</th>
            <th scope="col" class="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">Key (Masked)</th>
            <th scope="col" class="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">Estado</th>
            <th scope="col" class="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">Vencimiento</th>
            <th scope="col" class="relative py-3.5 pl-3 pr-4 sm:pr-6">
              <span class="sr-only">Acciones</span>
            </th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200 bg-white">
          <tr v-if="isLoading">
            <td colspan="5" class="py-4 text-center text-sm text-gray-500">Cargando credenciales...</td>
          </tr>
          <tr v-else-if="serviceAccounts.length === 0">
            <td colspan="5" class="py-4 text-center text-sm text-gray-500">No hay API Keys configuradas.</td>
          </tr>
          <tr v-else v-for="account in serviceAccounts" :key="account.id">
            <td class="py-4 pl-4 pr-3 text-sm font-medium text-gray-900">
              {{ account.name }}
            </td>
            <td class="px-3 py-4 text-sm text-gray-500 font-mono">
              {{ account.maskedKey || 'sk-live-****' }}
            </td>
            <td class="px-3 py-4 text-sm">
              <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-green-100 text-green-800" v-if="account.status === 'ACTIVE'">ACTIVA</span>
              <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-red-100 text-red-800" v-else>EXPIRADA/REVOCADA</span>
            </td>
            <td class="px-3 py-4 text-sm text-gray-500 flex items-center gap-2">
              <span :class="getExpirationColorClass(account.expirationDate)" class="w-3 h-3 rounded-full border border-gray-200 shadow-sm block"></span>
              {{ formatDate(account.expirationDate) }}
            </td>
            <td class="relative py-4 pl-3 pr-4 text-right text-sm font-medium sm:pr-6">
              <button class="text-red-600 hover:text-red-900" @click="revokeKey(account.id)">Revocar</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal Generate Key -->
    <div v-if="showCreateModal" class="relative z-10" aria-labelledby="modal-title" role="dialog" aria-modal="true">
      <div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity"></div>
      <div class="fixed inset-0 z-10 overflow-y-auto">
        <div class="flex min-h-full items-end justify-center p-4 text-center sm:items-center sm:p-0">
          <div class="relative transform overflow-hidden rounded-lg bg-white text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg">
            
            <!-- Phase 1: Form Name -->
            <div v-if="!generatedKeyData" class="px-4 pb-4 pt-5 sm:p-6 sm:pb-4">
              <h3 class="text-base font-semibold leading-6 text-gray-900 mb-2">Crear nueva API Key</h3>
              <p class="text-sm text-gray-500 mb-4">Esta llave tendrá una vigencia estándar permitiendo el acceso system-to-system.</p>
              <div>
                <label for="keyName" class="block text-sm font-medium leading-6 text-gray-900">Nombre del Servicio (Ref)</label>
                <div class="mt-2">
                  <input type="text" v-model="newKeyName" id="keyName" class="block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-purple-600 sm:text-sm sm:leading-6">
                </div>
              </div>
            </div>
            <div v-if="!generatedKeyData" class="bg-gray-50 px-4 py-3 sm:flex sm:flex-row-reverse sm:px-6">
              <button type="button" @click="generateKey" :disabled="isGenerating || !newKeyName" class="inline-flex w-full justify-center rounded-md bg-purple-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-purple-500 sm:ml-3 sm:w-auto disabled:opacity-50">Generar</button>
              <button type="button" @click="showCreateModal = false" class="mt-3 inline-flex w-full justify-center rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50 sm:mt-0 sm:w-auto">Cancelar</button>
            </div>

            <!-- Phase 2: Generated Key View -->
            <div v-else class="px-4 pb-4 pt-5 sm:p-6 sm:pb-4">
              <div class="mx-auto flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-full bg-green-100 sm:mx-0 sm:h-10 sm:w-10 mb-4">
                <svg class="h-6 w-6 text-green-600" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M4.5 12.75l6 6 9-13.5" />
                </svg>
              </div>
              <h3 class="text-base font-semibold leading-6 text-gray-900 mb-2">API Key Generada Exitosamente</h3>
              
              <div class="bg-red-50 border-l-4 border-red-500 p-4 mb-4">
                <p class="text-sm text-red-700 font-bold">
                  Guarda esta clave ahora. Por motivos de seguridad, no volverá a mostrarse en texto plano.
                </p>
              </div>

              <div class="mt-2 flex items-center justify-between bg-gray-100 p-3 rounded-md border border-gray-300">
                <code class="text-sm break-all text-gray-800">{{ generatedKeyData.token }}</code>
              </div>
            </div>
            
            <div v-if="generatedKeyData" class="bg-gray-50 px-4 py-3 sm:flex sm:flex-row-reverse sm:px-6 gap-2">
               <button type="button" @click="copyToClipboard" class="inline-flex w-full justify-center items-center gap-1 rounded-md bg-indigo-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 sm:w-auto">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 5H6a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2v-1M8 5a2 2 0 002 2h2a2 2 0 002-2M8 5a2 2 0 012-2h2a2 2 0 012 2"></path></svg>
                Copiar al portapapeles
              </button>
              <button type="button" @click="closeModal" class="inline-flex w-full justify-center rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50 sm:w-auto">Cerrar</button>
            </div>

          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import apiClient from '@/services/apiClient'

interface ServiceAccount {
  id: string;
  name: string;
  status: 'ACTIVE' | 'REVOKED' | 'EXPIRED';
  expirationDate: string;
  maskedKey: string;
}

const serviceAccounts = ref<ServiceAccount[]>([])
const isLoading = ref(true)

const showCreateModal = ref(false)
const newKeyName = ref('')
const isGenerating = ref(false)
const generatedKeyData = ref<{ token: string, account: ServiceAccount } | null>(null)

const fetchAccounts = async () => {
  isLoading.value = true
  try {
    const res = await apiClient.get('/admin/service-accounts')
    if (res.data && Array.isArray(res.data)) {
        serviceAccounts.value = res.data
    }
  } catch (error) {
    console.error('Error fetching service accounts', error)
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  fetchAccounts()
})

const getExpirationColorClass = (expirationDate: string) => {
  if (!expirationDate) return 'bg-gray-400'
  const expTime = new Date(expirationDate).getTime()
  const now = Date.now()
  const diffDays = Math.ceil((expTime - now) / (1000 * 60 * 60 * 24))
  
  if (diffDays <= 0) return 'bg-red-500' // Expirada
  if (diffDays <= 30) return 'bg-yellow-400' // < 30 dias amarillas
  return 'bg-green-500' // Segura
}

const formatDate = (dateString: string) => {
  if (!dateString) return 'N/A'
  const d = new Date(dateString)
  return d.toLocaleDateString()
}

const generateKey = async () => {
  if (!newKeyName.value) return
  isGenerating.value = true
  try {
    const res = await apiClient.post('/admin/service-accounts', { name: newKeyName.value })
    if (res.data) {
        generatedKeyData.value = {
        token: res.data.token,
        account: res.data.account
        }
        // Update local table with asterisk masked one
        if (res.data.account) {
            serviceAccounts.value.push(res.data.account)
        }
    }
  } catch (error) {
    console.error('Failed to generate key', error)
  } finally {
    isGenerating.value = false
  }
}

const copyToClipboard = async () => {
  if (generatedKeyData.value?.token) {
    try {
      await navigator.clipboard.writeText(generatedKeyData.value.token)
    } catch (e) {
      console.error('Clipboard update failed:', e)
    }
  }
}

const closeModal = () => {
  showCreateModal.value = false
  generatedKeyData.value = null
  newKeyName.value = ''
}

const revokeKey = async (id: string) => {
  try {
    await apiClient.delete(`/admin/service-accounts/${id}`)
    fetchAccounts()
  } catch (e) {
    console.error('Failed to revoke', e)
  }
}
</script>
