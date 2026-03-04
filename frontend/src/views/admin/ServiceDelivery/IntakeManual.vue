<template>
  <div class="max-w-4xl mx-auto p-6 space-y-8">
    <div class="flex items-center justify-between border-b pb-4">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Service Delivery: Intake Manual</h1>
        <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">Ingreso de casos Plan B / Soporte Operativo</p>
      </div>
      <div class="flex gap-3 items-center">
        <!-- Mock toggle for Role -->
        <label class="flex items-center gap-2 text-xs bg-gray-100 px-3 py-1 rounded border">
          <input type="checkbox" v-model="isSacLeader" class="rounded text-blue-600 focus:ring-blue-500">
          Rol Emulado: SAC_Leader
        </label>
        <div class="bg-blue-100 text-blue-800 px-3 py-1 rounded-full text-xs font-semibold tracking-wide">
          Role_Admin_Intake
        </div>
      </div>
    </div>

    <!-- Alert Success -->
    <div v-if="successMessage" class="bg-green-50 border border-green-200 text-green-800 rounded-lg p-4 flex items-center">
      <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
      {{ successMessage }}
    </div>

    <form @submit.prevent="submitIntake" class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6 space-y-6">
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- Cliente -->
        <div class="space-y-2">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">ID Cliente / CRM ID *</label>
          <input 
            v-model="form.customerId" 
            type="text" 
            required
            class="w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white p-2.5 border"
            placeholder="Ej. CRM-99214"
          >
        </div>

        <!-- Tipo de Trámite -->
        <div class="space-y-2">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Tipo de Servicio *</label>
          <select 
            v-model="form.serviceType" 
            required
            class="w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white p-2.5 border"
          >
            <option value="" disabled>Seleccione servicio...</option>
            <option value="onboarding">Onboarding Corporativo</option>
            <option value="support">Soporte Nivel 2</option>
            <option value="billing">Reclamo Facturación</option>
          </select>
        </div>

        <!-- Prioridad -->
        <div class="space-y-2">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Nivel de Prioridad (SLA)</label>
          <select 
            v-model="form.priority" 
            class="w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white p-2.5 border"
          >
            <option value="low">Baja (48h)</option>
            <option value="normal">Normal (24h)</option>
            <option value="high">Alta (4h)</option>
            <option value="critical">Crítica (1h)</option>
          </select>
        </div>

        <!-- Archivo Adjunto -->
        <div class="space-y-2">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Documento Soporte (Opcional)</label>
          <input 
            type="file" 
            @change="handleFileUpload"
            class="w-full text-sm text-gray-500 file:mr-4 file:py-2.5 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100 dark:file:bg-gray-700 dark:file:text-blue-400"
          >
        </div>
      </div>

      <!-- Descripción -->
      <div class="space-y-2">
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Descripción del Caso *</label>
        <textarea 
          v-model="form.description" 
          rows="4" 
          required
          class="w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white p-2.5 border"
          placeholder="Detalla los pormenores del trámite a ingresar manualmente..."
        ></textarea>
      </div>

      <!-- Actions -->
      <div class="pt-4 flex justify-end items-center space-x-4 border-t border-gray-200 dark:border-gray-700">
        <button type="button" class="text-sm font-medium text-gray-600 hover:text-gray-900 dark:text-gray-400 dark:hover:text-white">
          Cancelar
        </button>
        <button 
          type="submit" 
          :disabled="isSubmitting"
          class="px-5 py-2.5 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg shadow-sm disabled:opacity-50 disabled:cursor-not-allowed flex items-center transition-colors"
        >
          <svg v-if="isSubmitting" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          Iniciar Trámite
        </button>
      </div>
    </form>

    <!-- 1. Pantalla 16 (IntakeManual.vue) - Modal de Lectura Plana (CA-10) -->
    <div class="mt-8 border-t pt-8">
       <h2 class="text-xl font-bold text-gray-800 mb-4">Cola de Correos Pendientes de Radicación</h2>
       <div class="bg-white border rounded-lg overflow-hidden shadow-sm">
          <table class="min-w-full divide-y divide-gray-200">
             <thead class="bg-gray-50">
               <tr>
                 <th class="px-4 py-3 text-left text-xs font-bold text-gray-500 uppercase">Origen</th>
                 <th class="px-4 py-3 text-left text-xs font-bold text-gray-500 uppercase">Asunto</th>
                 <th class="px-4 py-3 text-right text-xs font-bold text-gray-500 uppercase">Acción</th>
               </tr>
             </thead>
             <tbody class="divide-y divide-gray-200">
               <tr v-for="mail in mockEmails" :key="mail.id" class="hover:bg-gray-50">
                 <td class="px-4 py-3 text-sm text-gray-900">{{ mail.sender }}</td>
                 <td class="px-4 py-3 text-sm text-gray-500 truncate max-w-xs">{{ mail.subject }}</td>
                 <td class="px-4 py-3 text-right">
                    <button v-if="!isSacLeader" @click="openReadonlyMail(mail)" class="text-indigo-600 hover:text-indigo-900 text-xs font-bold bg-indigo-50 px-3 py-1.5 rounded transition inline-flex items-center gap-1 border border-indigo-100">
                      🔍 Ver Correo
                    </button>
                    <span v-else class="text-xs text-gray-400 italic">Acceso Total Disponible</span>
                 </td>
               </tr>
             </tbody>
          </table>
       </div>
    </div>

    <!-- Modal (Overlay) Lectura Plana -->
    <div v-if="readonlyMailModal" class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-xl w-full max-w-2xl overflow-hidden animate-slide-in">
        <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-900 flex justify-between items-center">
          <h3 class="text-lg font-bold text-gray-800 dark:text-white flex items-center gap-2">
            📧 Vista Restringida de Correo (Texto Plano)
          </h3>
          <button @click="readonlyMailModal = false" class="text-gray-400 hover:text-gray-600 text-xl font-bold">&times;</button>
        </div>
        <div class="p-6 space-y-4">
           <div v-if="selectedMail">
              <div class="mb-4">
                <p class="text-xs font-bold text-gray-500 uppercase tracking-wider">Asunto</p>
                <p class="text-base font-semibold text-gray-900">{{ selectedMail.subject }}</p>
              </div>
              <div class="mb-4">
                <p class="text-xs font-bold text-gray-500 uppercase tracking-wider">Remitente</p>
                <p class="text-sm text-gray-700">{{ selectedMail.sender }}</p>
              </div>
              <div>
                <p class="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">Cuerpo del Mensaje</p>
                <div class="bg-gray-50 border border-gray-200 rounded p-4 text-sm text-gray-700 whitespace-pre-wrap font-mono min-h-[150px]">
                  {{ selectedMail.body }}
                </div>
              </div>
              <p class="text-xs text-red-500 font-bold mt-4 flex items-center gap-1">
                 ⚠️ Anexos bloqueados por Políticas de DLP. Visualización modo solo lectura (Non-Leader).
              </p>
           </div>
        </div>
        <div class="px-6 py-4 border-t border-gray-200 bg-gray-50 flex justify-end">
           <button @click="readonlyMailModal = false" class="px-4 py-2 bg-gray-200 text-gray-800 text-sm font-bold rounded hover:bg-gray-300 transition">Cerrar</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';

const isSubmitting = ref(false);
const successMessage = ref('');
const form = reactive({
  customerId: '',
  serviceType: '',
  priority: 'normal',
  description: '',
  file: null as File | null
});

const handleFileUpload = (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files.length > 0) {
    form.file = target.files[0];
  }
};

const submitIntake = async () => {
  isSubmitting.value = true;
  successMessage.value = '';
  
  try {
    // Simulating API Call: $http.post('/api/v1/service-delivery/manual-start')
    await new Promise(resolve => setTimeout(resolve, 1500));
    
    console.log('Intake payload submitted:', { ...form });
    successMessage.value = `Trámite Iniciado Exitosamente. Caso asignado ID: CASE-${Math.floor(Math.random() * 10000)}`;
    
    // Reset form after success
    form.customerId = '';
    form.serviceType = '';
    form.description = '';
    form.file = null;
  } catch (error) {
    console.error('Error in intake:', error);
  } finally {
    isSubmitting.value = false;
  }
};

// ==========================================
// Modal Lectura Plana (CA-10) Lógica
// ==========================================
const readonlyMailModal = ref(false);
const isSacLeader = ref(false); // Mock de rol SAC_Leader
const selectedMail = ref<null | { id: string, sender: string, subject: string, body: string }>(null);

const mockEmails = ref([
  { id: 'MAIL-1', sender: 'cliente1@empresa.com', subject: 'Solicitud de Crédito Corporativo', body: 'Adjunto mis extractos bancarios para la solicitud del crédito. Quedo atento.' },
  { id: 'MAIL-2', sender: 'soporte@external.com', subject: 'Falla en acceso al portal Docusign', body: 'No puedo ver los expedientes desde ayer. Arroja HTTP 500.' }
]);

const openReadonlyMail = (mail: any) => {
  selectedMail.value = mail;
  readonlyMailModal.value = true;
};

</script>

<style scoped>
.animate-slide-in {
  animation: slideInDown 0.3s ease-out forwards;
}
@keyframes slideInDown {
  from { transform: translateY(-20px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}
</style>
