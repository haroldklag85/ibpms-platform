<template>
  <div v-if="store.showPanicModal" class="relative z-50">
    <div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity"></div>

    <div class="fixed inset-0 z-10 w-screen overflow-y-auto">
      <div class="flex min-h-full justify-center p-4 text-center items-center sm:p-0">
        <div class="relative transform overflow-hidden rounded-lg bg-white text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg">
          <div class="bg-white px-4 pb-4 pt-5 sm:p-6 sm:pb-4">
            <div class="sm:flex sm:items-start">
              <div class="mx-auto flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-full sm:mx-0 sm:h-10 sm:w-10" :class="iconBgClass">
                <svg class="h-6 w-6" :class="iconColorClass" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
              </div>
              <div class="mt-3 text-center sm:ml-4 sm:mt-0 sm:text-left w-full">
                <h3 class="text-base font-semibold leading-6 text-gray-900" id="modal-title">Justificación Obligatoria: {{ translatedAction }}</h3>
                <div class="mt-2">
                  <p class="text-sm text-gray-500 mb-3">
                    Estás a punto de forzar el estado de la tarea a <span class="font-bold">{{ translatedAction }}</span>. Por favor ingresa una justificación mínima de 20 caracteres computados.
                  </p>
                  <textarea
                    v-model="store.panicJustification"
                    rows="3"
                    class="block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
                    :class="{ 'border-red-300': store.panicJustification.length > 0 && store.panicJustification.length < 20 }"
                    placeholder="Escribe la justificación aquí..."
                  ></textarea>
                  <p v-if="store.panicJustification.length > 0 && store.panicJustification.length < 20" class="mt-1 text-xs text-red-500">Mínimo 20 caracteres (actual: {{ store.panicJustification.length }}).</p>
                </div>
              </div>
            </div>
          </div>
          <div class="bg-gray-50 px-4 py-3 sm:flex sm:flex-row-reverse sm:px-6">
            <button 
              type="button" 
              class="inline-flex w-full justify-center rounded-md px-3 py-2 text-sm font-semibold text-white shadow-sm sm:ml-3 sm:w-auto focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2"
              :class="buttonClass"
              :disabled="!isValid"
              @click="confirmAction"
            >
              Confirmar {{ translatedAction }}
            </button>
            <button 
              type="button" 
              class="mt-3 inline-flex w-full justify-center rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50 sm:mt-0 sm:w-auto"
              @click="cancel"
            >
              Cancelar
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useGenericFormStore } from '@/stores/genericFormStore'
import { z } from 'zod'

const store = useGenericFormStore()

const justificationSchema = z.string().min(20)

const isValid = computed(() => {
  return justificationSchema.safeParse(store.panicJustification).success
})

const translatedAction = computed(() => {
  switch (store.panicAction) {
    case 'APPROVED': return 'Aprobar'
    case 'RETURNED': return 'Retornar'
    case 'CANCELLED': return 'Cancelar'
    default: return ''
  }
})

const iconBgClass = computed(() => {
  switch (store.panicAction) {
    case 'APPROVED': return 'bg-green-100'
    case 'RETURNED': return 'bg-amber-100'
    case 'CANCELLED': return 'bg-red-100'
    default: return 'bg-gray-100'
  }
})

const iconColorClass = computed(() => {
  switch (store.panicAction) {
    case 'APPROVED': return 'text-green-600'
    case 'RETURNED': return 'text-amber-600'
    case 'CANCELLED': return 'text-red-600'
    default: return 'text-gray-600'
  }
})

const buttonClass = computed(() => {
  if (!isValid.value) return 'bg-gray-400 cursor-not-allowed'
  switch (store.panicAction) {
    case 'APPROVED': return 'bg-green-600 hover:bg-green-500 focus-visible:outline-green-600'
    case 'RETURNED': return 'bg-amber-600 hover:bg-amber-500 focus-visible:outline-amber-600'
    case 'CANCELLED': return 'bg-red-600 hover:bg-red-500 focus-visible:outline-red-600'
    default: return 'bg-blue-600 hover:bg-blue-500'
  }
})

const confirmAction = async () => {
  if (isValid.value) {
    await store.submitForm()
    store.showPanicModal = false
    // Emitir evento para volver a la bandeja si fuera necesario
  }
}

const cancel = () => {
  store.showPanicModal = false
  store.panicAction = null
  store.panicJustification = ''
}
</script>
