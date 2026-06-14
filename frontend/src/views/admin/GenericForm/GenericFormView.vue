<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    
    <div class="md:flex md:items-center md:justify-between mb-6">
      <div class="flex-1 min-w-0">
        <h2 class="text-2xl font-bold leading-7 text-gray-900 sm:text-3xl sm:truncate">
          Gestión de Tarea - {{ store.taskId }}
        </h2>
        <p class="mt-1 text-sm text-gray-500">
          Complete la información requerida o use las opciones de pánico.
        </p>
      </div>
    </div>

    <div v-if="store.isContextLoading" class="flex justify-center p-12">
      <svg class="animate-spin h-8 w-8 text-indigo-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
    </div>

    <div v-else>
      <!-- REM-039-C: Banner de Restauración de Borrador (Patrón CA-85) -->
      <div v-if="store.showDraftBanner" class="mb-4 bg-amber-50 border border-amber-200 rounded-lg p-4 flex items-center justify-between">
        <div class="flex items-center gap-2">
          <svg class="h-5 w-5 text-amber-600 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z" />
          </svg>
          <p class="text-sm text-amber-800">
            Se detectó un borrador no enviado. ¿Desea restaurarlo?
          </p>
        </div>
        <div class="flex gap-2 flex-shrink-0">
          <button @click="store.restoreDraft()" class="text-sm bg-amber-600 text-white px-3 py-1.5 rounded-md hover:bg-amber-700 font-medium transition-colors">Restaurar</button>
          <button @click="store.dismissDraft()" class="text-sm bg-white text-gray-600 px-3 py-1.5 rounded-md border border-gray-300 hover:bg-gray-50 font-medium transition-colors">Descartar</button>
        </div>
      </div>

      <GenericFormBody />
    </div>

    <PanicJustificationModal />

  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, onBeforeRouteLeave } from 'vue-router'
import { useGenericFormStore } from '@/stores/genericFormStore'

import GenericFormBody from '@/components/forms/generic/GenericFormBody.vue'
import PanicJustificationModal from '@/components/forms/generic/PanicJustificationModal.vue'

const route = useRoute()
const store = useGenericFormStore()

onBeforeRouteLeave((to, from, next) => {
  if (store.observations || store.result) {
    const answer = window.confirm('Tiene cambios sin guardar. ¿Desea salir sin guardar?')
    if (!answer) return next(false)
  }
  next()
})

onMounted(async () => {
  const taskIdParam = route.params.taskId as string || 'TEST-TASK-001'
  await store.init(taskIdParam)
})
</script>
