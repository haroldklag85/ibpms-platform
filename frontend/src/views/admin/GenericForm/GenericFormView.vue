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
      <GenericFormBody />
    </div>

    <PanicJustificationModal />

  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useGenericFormStore } from '@/stores/genericFormStore'

import GenericFormBody from '@/components/forms/generic/GenericFormBody.vue'
import PanicJustificationModal from '@/components/forms/generic/PanicJustificationModal.vue'

const route = useRoute()
const store = useGenericFormStore()

onMounted(async () => {
  const taskIdParam = route.params.taskId as string || 'TEST-TASK-001'
  await store.init(taskIdParam)
})
</script>
