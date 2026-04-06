<template>
  <div class="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
    <MetadataGrid :prefillData="store.prefillData" />
    
    <div class="mt-6 border-t pt-6">
      <h3 class="text-sm font-semibold text-gray-800 mb-4">Formulario de Tarea</h3>
      <ManagementResultSelect />
      <ObservationsField />
      <EvidenceDropzone />
    </div>

    <!-- Submit Button (Standard Path) -->
    <div class="mt-6 flex justify-end gap-3 border-t pt-4">
      <DraftSyncIndicator />
      <div class="flex-grow"></div>
      <button 
        type="button" 
        class="bg-white py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none"
        @click="store.clearDraft()"
      >
        Limpiar Borrador
      </button>
      <button 
        type="button" 
        class="bg-indigo-600 border border-transparent rounded-md shadow-sm py-2 px-4 inline-flex justify-center text-sm font-medium text-white hover:bg-indigo-700 focus:outline-none"
        :class="{'opacity-50 cursor-not-allowed': !isValid || store.isSubmitting}"
        :disabled="!isValid || store.isSubmitting"
        @click="store.submitForm()"
      >
        <span v-if="store.isSubmitting">Enviando...</span>
        <span v-else>Completar Tarea</span>
      </button>
    </div>

    <PanicButtonBar />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useGenericFormStore } from '@/stores/genericFormStore'
import MetadataGrid from './MetadataGrid.vue'
import ManagementResultSelect from './ManagementResultSelect.vue'
import ObservationsField from './ObservationsField.vue'
import EvidenceDropzone from './EvidenceDropzone.vue'
import PanicButtonBar from './PanicButtonBar.vue'
import DraftSyncIndicator from './DraftSyncIndicator.vue'
import { z } from 'zod'

const store = useGenericFormStore()

const obsSchema = z.string().min(10).max(2000)

const isValid = computed(() => {
  return obsSchema.safeParse(store.observations).success && store.result !== ''
})
</script>
