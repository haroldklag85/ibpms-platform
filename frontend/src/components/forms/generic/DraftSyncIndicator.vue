<template>
  <div class="flex items-center text-xs font-medium space-x-2">
    <!-- Local Draft Exists -->
    <template v-if="store.syncState === 'LOCAL_ONLY'">
      <span class="relative flex h-3 w-3">
        <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-amber-400 opacity-75"></span>
        <span class="relative inline-flex rounded-full h-3 w-3 bg-amber-500"></span>
      </span>
      <span class="text-amber-600">Borrador en Navegador</span>
    </template>
    
    <!-- Syncing to remote -->
    <template v-else-if="store.syncState === 'SAVING'">
      <span class="relative flex h-3 w-3">
        <span class="animate-spin h-3 w-3 border-2 border-indigo-500 border-t-transparent rounded-full"></span>
      </span>
      <span class="text-indigo-600">Guardando...</span>
    </template>

    <!-- Synced to remote -->
    <template v-else-if="store.syncState === 'SYNCED'">
      <span class="relative flex h-3 w-3">
        <span class="relative inline-flex rounded-full h-3 w-3 bg-green-500"></span>
      </span>
      <span class="text-green-600">Sincronizado</span>
    </template>

    <!-- Sync error -->
    <template v-else-if="store.syncState === 'ERROR'">
      <span class="relative flex h-3 w-3">
        <span class="relative inline-flex rounded-full h-3 w-3 bg-red-500"></span>
      </span>
      <span class="text-red-600">Error de Sincronización</span>
    </template>
  </div>
</template>

<script setup lang="ts">
import { useGenericFormStore } from '@/stores/genericFormStore'

const store = useGenericFormStore()
</script>
