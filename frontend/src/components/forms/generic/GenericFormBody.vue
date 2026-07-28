<template>
  <!-- @Traceability: US-039 - CA-4, CA-7 -->
  <div class="relative bg-white p-6 rounded-lg shadow-sm border border-gray-200">
    <!-- FRONT-029-11: Detección Sesión Duplicada -->
    <div v-if="isLocked" class="mb-4 p-4 bg-blue-50 border border-blue-200 rounded-md text-blue-800 flex items-center gap-3">
        <span class="text-2xl">⚠️</span>
        <div>
            <h4 class="font-bold">Formulario abierto en otra pestaña</h4>
            <p class="text-sm">Por seguridad, la edición en esta pestaña ha sido bloqueada. Continúa en la pestaña activa.</p>
        </div>
    </div>
    
    <!-- FRONT-029-10: Pre-Aviso Caducidad Borrador -->
    <div v-if="hoursRemaining !== null && !isLocked" class="mb-4 p-3 bg-yellow-50 border border-yellow-200 rounded-md text-yellow-800 text-sm flex gap-2 items-center">
        <span class="text-lg">⏰</span>
        <strong>Quedan {{ hoursRemaining }} horas antes de que el borrador se elimine del servidor.</strong>
    </div>
    
    <!-- Pointer events none if locked -->
    <div :class="{'opacity-50 pointer-events-none': isLocked}">
    <MetadataGrid :prefillData="store.prefillData" />
    
    <div class="mt-6 border-t pt-6">
      <h3 class="text-sm font-semibold text-gray-800 mb-4">Formulario de Tarea</h3>
      <ManagementResultSelect />
      <ObservationsField />
      <EvidenceDropzone />
    </div>

    <!-- FRONT-029-04: Lazy Patching Warnings (Simulated for generic form) -->
    <div v-if="missingRequiredFields.length > 0" class="mt-4 p-3 bg-red-50 border border-red-200 rounded text-red-700 text-sm flex gap-2 items-center">
       <span class="text-xl">⚠️</span>
       <div>
         <strong>Campos obligatorios faltantes detectados.</strong>
         <p>Este campo fue añadido en una versión reciente del formulario. Faltan {{ missingRequiredFields.length }} campos en sus datos.</p>
       </div>
    </div>

    <!-- Submit Button (Standard Path) -->
    <div class="mt-6 flex justify-end gap-3 border-t pt-4">
      <DraftSyncIndicator />
      <div class="flex-grow"></div>
      <button 
        type="button" 
        class="bg-white py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none"
        @click="store.clearDraft()"
        :disabled="feedback.phase.value !== 'idle'"
      >
        Limpiar Borrador
      </button>
      <button 
        type="button" 
        class="bg-indigo-600 border border-transparent rounded-md shadow-sm py-2 px-4 inline-flex justify-center text-sm font-medium text-white hover:bg-indigo-700 focus:outline-none disabled:opacity-50 disabled:cursor-not-allowed"
        :disabled="!isValid || store.isSubmitting || missingRequiredFields.length > 0 || feedback.phase.value !== 'idle'"
        @click="onConfirmClick"
      >
        <span v-if="store.isSubmitting || feedback.phase.value === 'validating' || feedback.phase.value === 'saving'" class="flex items-center gap-2">
            <svg class="animate-spin h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
            Enviando...
        </span>
        <span v-else>Completar Tarea</span>
      </button>
    </div>
    <div v-if="showInlineError" class="mt-2 text-right text-xs text-red-600 font-bold">
      {{ validationErrorText }}
    </div>
    <div v-if="feedback.phase.value === 'error'" class="mt-2 text-right text-xs text-red-600 font-bold">
      {{ feedback.errorMessage }}
    </div>

    <PanicButtonBar />

    </div>
    <!-- OVERLAYS FRONT-029-01 & FRONT-029-02 -->
    <div v-if="feedback.phase.value === 'validating' || feedback.phase.value === 'saving'" class="absolute inset-0 bg-black/30 backdrop-blur-sm z-50 rounded-lg flex flex-col items-center justify-center text-white">
        <svg class="animate-spin h-12 w-12 text-white mb-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
        <p class="text-lg font-medium">{{ feedback.phase.value === 'validating' ? 'Validando datos...' : 'Guardando en el servidor...' }}</p>
    </div>

    <!-- POST SUBMIT REDIRECT FRONT-029-02 -->
    <div v-if="feedback.phase.value === 'success'" class="absolute inset-0 bg-white z-50 rounded-lg flex flex-col items-center justify-center">
        <div class="text-green-500 mb-4 animate-bounce">
            <svg class="w-20 h-20" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
        </div>
        <h2 class="text-2xl font-bold text-gray-800 mb-2">¡Tarea completada exitosamente!</h2>
        <p class="text-gray-600 mb-6">ID Tarea: {{ store.taskId }}</p>
        <button @click="goToWorkdesk" class="px-6 py-2 bg-indigo-600 text-white rounded-md font-medium hover:bg-indigo-700 transition">
            Ir al Workdesk
        </button>
        <p class="text-sm text-gray-400 mt-4">Redirigiendo automáticamente en 3 segundos...</p>
    </div>

    <!-- SCHEMA CONFLICT MODAL FRONT-029-06 -->
    <div v-if="showSchemaConflictModal" class="fixed inset-0 z-[100] flex items-center justify-center bg-black/50">
        <div class="bg-white rounded-lg shadow-xl p-6 max-w-md w-full">
            <h3 class="text-lg font-bold text-gray-900 mb-3 flex items-center gap-2">
                <span class="text-2xl">⚠️</span> Actualización de Formulario
            </h3>
            <p class="text-sm text-gray-600 mb-5">
                El formulario fue actualizado con nuevos campos obligatorios. Tus datos están seguros. Se recargará la estructura para que completes la información adicional.
            </p>
            <div class="flex justify-end">
                <button @click="resolveSchemaConflict" class="bg-indigo-600 text-white px-4 py-2 rounded font-medium hover:bg-indigo-700">
                    Aceptar y Recargar
                </button>
            </div>
        </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useSessionLock } from '@/composables/workdesk/useSessionLock'
import { useRouter } from 'vue-router'
import { useGenericFormStore } from '@/stores/genericFormStore'
import { useWorkdeskStore } from '@/stores/useWorkdeskStore'
import { useSubmitFeedback } from '@/composables/workdesk/useSubmitFeedback'
import MetadataGrid from './MetadataGrid.vue'
import ManagementResultSelect from './ManagementResultSelect.vue'
import ObservationsField from './ObservationsField.vue'
import EvidenceDropzone from './EvidenceDropzone.vue'
import PanicButtonBar from './PanicButtonBar.vue'
import DraftSyncIndicator from './DraftSyncIndicator.vue'
import { z } from 'zod'

const store = useGenericFormStore()
const workdeskStore = useWorkdeskStore()
const feedback = useSubmitFeedback()
const router = useRouter()
const { isLocked } = useSessionLock(store.taskId)

// FRONT-029-12: Anti-Envío Accidental (beforeunload)
const handleBeforeUnload = (e: BeforeUnloadEvent) => {
    if (store.syncState !== 'SYNCED' && store.observations.length > 0) {
        e.preventDefault()
        e.returnValue = ''
    }
}
onMounted(() => {
    window.addEventListener('beforeunload', handleBeforeUnload)
})
onUnmounted(() => {
    window.removeEventListener('beforeunload', handleBeforeUnload)
})


// @Traceability: US-039 - CA-32 (Validación cruzada Zod) - REMEDIATED
const formSchema = z.object({
  result: z.string().min(1, "El resultado de gestión es obligatorio."),
  observations: z.string().min(10, "Las observaciones deben tener al menos 10 caracteres.").max(2000, "Máximo 2000 caracteres.")
}).superRefine((data, ctx) => {
  if (data.result === 'RECHAZADO' && data.observations.length < 50) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      message: 'Para rechazar la tarea se requiere una justificación detallada (mínimo 50 caracteres).',
      path: ['observations']
    });
  }
});

const showInlineError = ref(false)
const showSchemaConflictModal = ref(false)

// FRONT-029-04: Mock lazy patching detection
// FRONT-029-10: Pre-Aviso Caducidad
const hoursRemaining = computed(() => {
    if (!store.prefillData?.draftExpiresAt) return null;
    const expires = new Date(store.prefillData.draftExpiresAt).getTime();
    const now = new Date().getTime();
    const diffHours = (expires - now) / (1000 * 60 * 60);
    return diffHours > 0 && diffHours < 24 ? Math.ceil(diffHours) : null;
})

const missingRequiredFields = computed(() => {
    // If this was a dynamic form, we would compare Zod schema with prefillData.
    // For generic form we just verify if prefillData requires something we don't have.
    const missing = [];
    if (store.prefillData && store.prefillData._newRequiredField && !store.prefillData._newRequiredField_filled) {
        missing.push('_newRequiredField');
    }
    return missing;
})

const validationResult = computed(() => {
  return formSchema.safeParse({
    result: store.result,
    observations: store.observations
  })
})

const isValid = computed(() => {
  return validationResult.value.success
})

const validationErrorText = computed(() => {
  if (!validationResult.value.success && validationResult.value.error) {
    return 'Error: ' + validationResult.value.error.errors[0].message;
  }
  return 'Error: El formulario es inválido.';
})

const onConfirmClick = async () => {
  if (!isValid.value || missingRequiredFields.value.length > 0) {
    showInlineError.value = true
    nextTick(() => {
      document.querySelector('.border-red-500, .text-red-600')?.scrollIntoView({ behavior: 'smooth', block: 'center' })
    })
    return
  }
  showInlineError.value = false
  
  feedback.startSubmit()
  
  try {
      const success = await store.submitForm()
      if (success) {
          feedback.setSuccess()
          
          // RYOW: Purge local storage and pinia items
          localStorage.removeItem('generic_draft_' + store.taskId)
          const idx = workdeskStore.items.findIndex(i => i.taskId === store.taskId || i.unifiedId === store.taskId)
          if (idx !== -1) {
              workdeskStore.items.splice(idx, 1)
          }

          setTimeout(() => {
              if (feedback.phase.value === 'success') {
                  goToWorkdesk()
              }
          }, 3000)
      } else {
          // If we are here and it returned false, could be panic action error or upload error
          feedback.setError("Ocurrió un error de validación o no todos los archivos han subido.")
      }
  } catch (err: any) {
    if (err.response?.status === 409 || err.response?.data?.error === 'SchemaVersionConflict') {
        showSchemaConflictModal.value = true
        feedback.reset()
    } else {
        feedback.setError(err.response?.data?.message || err.message || 'Error desconocido al completar.')
    }
}
}

const resolveSchemaConflict = async () => {
    showSchemaConflictModal.value = false
    // Recargar BFF (mock: we just fetch context again)
    await store.init(store.taskId)
    // LocalStorage preserves draft which is merged back
}

const goToWorkdesk = () => {
    router.push('/workdesk')
}
</script>
