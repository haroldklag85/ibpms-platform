<template>
  <div class="h-full w-full bg-gray-50 flex flex-col p-6 space-y-4">
    
    <!-- Encabezado de la Pantalla -->
    <header class="flex justify-between items-start">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 flex items-center gap-2">
          <svg class="w-6 h-6 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"></path></svg>
          Gobierno de Identidad y Accesos (RBAC)
        </h1>
        <p class="text-sm text-gray-500 mt-1">Configuración de Seguridad Dual: Roles manuales e infiriendo del BPMN.</p>
      </div>
      <div class="flex gap-3">
        <button class="px-4 py-2 border border-gray-300 text-gray-700 bg-white rounded-md shadow-sm hover:bg-gray-50 text-sm font-medium flex items-center gap-2">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"></path></svg>
          Sync AD / EntraID
        </button>
        <button class="px-4 py-2 bg-indigo-600 text-white rounded-md shadow-sm hover:bg-indigo-700 text-sm font-medium">
          + Crear Rol Global
        </button>
      </div>
    </header>

    <!-- Componente Principal de Pestañas y Tablas -->
    <RbacTabs class="flex-1 min-h-0" />

    <!-- Auditoría Inferior Estática -->
    <SecurityAuditLog />

  </div>
</template>

<script setup>
import RbacTabs from './RbacTabs.vue'
import SecurityAuditLog from './SecurityAuditLog.vue'
import { useRbacStore } from '@/stores/rbacStore'
import { onMounted } from 'vue'

const store = useRbacStore()

onMounted(() => {
  store.fetchRoles()
})
</script>
