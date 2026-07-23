<template>
  <div v-if="isOpen" class="fixed inset-0 z-50 overflow-y-auto" aria-labelledby="modal-title" role="dialog" aria-modal="true">
    <div class="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
      
      <div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" aria-hidden="true" @click="closeModal"></div>

      <!-- This element is to trick the browser into centering the modal contents. -->
      <span class="hidden sm:inline-block sm:align-middle sm:h-screen" aria-hidden="true">&#8203;</span>

      <div class="inline-block align-bottom bg-white rounded-lg px-4 pt-5 pb-4 text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full sm:p-6">
        <div>
          <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full" :class="isRejectMode ? 'bg-red-100' : 'bg-blue-100'">
            <svg v-if="isRejectMode" class="h-6 w-6 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
            <svg v-else class="h-6 w-6 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <div class="mt-3 text-center sm:mt-5">
            <h3 class="text-lg leading-6 font-medium text-gray-900" id="modal-title">
              {{ isRejectMode ? 'Rechazar Ingesta' : 'Aprobar Ingesta' }}
            </h3>
            <div class="mt-2">
              <p class="text-sm text-gray-500">
                {{ isRejectMode ? 'Por favor provea un motivo para rechazar este payload. Esta acción no se puede deshacer.' : 'Seleccione el proceso de negocio al cual enrutar este payload.' }}
              </p>
            </div>
          </div>
          
          <div class="mt-4">
            <div v-if="isRejectMode">
              <label for="rejectionReason" class="block text-sm font-medium text-gray-700">Motivo de Rechazo</label>
              <textarea 
                id="rejectionReason" 
                v-model="rejectionReason" 
                rows="3" 
                class="shadow-sm focus:ring-red-500 focus:border-red-500 mt-1 block w-full sm:text-sm border border-gray-300 rounded-md"
                placeholder="Indique el motivo..."
              ></textarea>
              <p v-if="localError" class="mt-2 text-sm text-red-600" id="reason-error">{{ localError }}</p>
            </div>
            
            <div v-else>
               <ProcessSelector v-model="selectedProcess" />
               <p v-if="localError" class="mt-2 text-sm text-red-600" id="process-error">{{ localError }}</p>
            </div>
          </div>
        </div>
        <div class="mt-5 sm:mt-6 sm:flex sm:flex-row-reverse">
          <button 
            type="button" 
            class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 text-base font-medium text-white focus:outline-none focus:ring-2 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm"
            :class="[isRejectMode ? 'bg-red-600 hover:bg-red-700 focus:ring-red-500' : 'bg-blue-600 hover:bg-blue-700 focus:ring-blue-500', isSubmitting ? 'opacity-50 cursor-not-allowed' : '']"
            @click="handleSubmit"
            :disabled="isSubmitting"
          >
            {{ isSubmitting ? 'Procesando...' : (isRejectMode ? 'Rechazar' : 'Aprobar y Enrutar') }}
          </button>
          <button 
            type="button" 
            class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm" 
            @click="closeModal"
            :disabled="isSubmitting"
          >
            Cancelar
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import ProcessSelector from './ProcessSelector.vue';

const props = defineProps<{
  isOpen: boolean;
  mode: 'APPROVE' | 'REJECT';
  taskId: string;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'confirm-approve', payload: { taskId: string, processType: string }): void;
  (e: 'confirm-reject', payload: { taskId: string, reason: string }): void;
}>();

const isRejectMode = ref(false);
const rejectionReason = ref('');
const selectedProcess = ref('');
const localError = ref('');
const isSubmitting = ref(false);

watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    isRejectMode.value = props.mode === 'REJECT';
    rejectionReason.value = '';
    selectedProcess.value = '';
    localError.value = '';
    isSubmitting.value = false;
  }
});

const closeModal = () => {
  if (!isSubmitting.value) {
    emit('close');
  }
};

const handleSubmit = () => {
  localError.value = '';
  
  if (isRejectMode.value) {
    if (rejectionReason.value.trim().length < 5) {
      localError.value = 'Debe proveer un motivo de rechazo razonable (min 5 caracteres).';
      return;
    }
    isSubmitting.value = true;
    emit('confirm-reject', { taskId: props.taskId, reason: rejectionReason.value });
  } else {
    if (!selectedProcess.value) {
      localError.value = 'Debe seleccionar un proceso de negocio.';
      return;
    }
    isSubmitting.value = true;
    emit('confirm-approve', { taskId: props.taskId, processType: selectedProcess.value });
  }
};
</script>
