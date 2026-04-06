<template>
  <div class="flex flex-col space-y-2 mt-4 pt-4 border-t border-gray-200">
    <h4 class="text-xs font-semibold text-gray-500 uppercase tracking-wider">Acciones de Pánico</h4>
    <div class="flex flex-wrap gap-3">
      <button 
        type="button"
        @click="triggerPanic('APPROVED')"
        class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700 shadow-sm"
      >
        Aprobar
      </button>
      <button 
        type="button"
        @click="triggerPanic('RETURNED')"
        class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-amber-500 hover:bg-amber-600 shadow-sm"
      >
        Retornar
      </button>
      <button 
        type="button"
        @click="triggerPanic('CANCELLED')"
        class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-red-600 hover:bg-red-700 shadow-sm"
      >
        Cancelar Tarea
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useGenericFormStore } from '@/stores/genericFormStore'

const store = useGenericFormStore()

const triggerPanic = (action: "APPROVED" | "RETURNED" | "CANCELLED") => {
  store.panicAction = action
  store.panicJustification = '' // Reset justification
  store.showPanicModal = true
}
</script>
