<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden flex flex-col h-full">
    
    <!-- Barra de Herramientas -->
    <div class="p-4 border-b bg-gray-50 flex justify-between items-center">
      <div class="flex items-center space-x-3">
        <h3 class="font-bold text-gray-800">Control de Acceso (RBAC)</h3>
        <span class="text-xs bg-gray-200 text-gray-700 px-2 py-1 rounded">Total: {{ users.length }}</span>
      </div>
      <div class="flex space-x-2">
        <input type="text" placeholder="👤 Buscar usuario..." class="px-3 py-1.5 text-sm border rounded focus:ring-ibpms-brand border-gray-300 w-64" />
        <button class="px-3 py-1.5 bg-gray-100 border text-gray-700 text-sm font-medium rounded hover:bg-gray-200">
           🔄 Sincronizar LDAP
        </button>
      </div>
    </div>

    <!-- Tabla Data Grid -->
    <div class="overflow-y-auto flex-1">
      <table class="w-full text-left text-sm text-gray-600">
        <thead class="bg-gray-50 text-gray-700 uppercase text-xs font-semibold sticky top-0 shadow-sm border-b">
          <tr>
            <th class="px-4 py-3">Operario</th>
            <th class="px-4 py-3 w-48">Departamento</th>
            <th class="px-4 py-3">Matriz de Roles (Grupos)</th>
            <th class="px-4 py-3 text-center w-24">Estado</th>
            <th class="px-4 py-3 text-right">Ajustes</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.userId" class="border-b transition hover:bg-blue-50/30">
            
            <!-- Perfil -->
            <td class="px-4 py-3">
               <div class="flex items-center space-x-3">
                  <div class="w-8 h-8 rounded-full bg-ibpms text-white flex justify-center items-center font-bold text-xs">
                     {{ user.fullName.charAt(0) }}
                  </div>
                  <div>
                    <p class="font-medium text-gray-900">{{ user.fullName }}</p>
                    <p class="text-xs text-ibpms-brand">{{ user.userId }}</p>
                  </div>
               </div>
            </td>
            
            <td class="px-4 py-3 text-xs font-medium text-gray-500 uppercase">{{ user.department }}</td>
            
            <!-- Roles Assignados -->
            <td class="px-4 py-3">
               <div class="flex flex-wrap gap-1.5">
                  <span 
                    v-for="role in user.assignedRoles" 
                    :key="role"
                    class="px-2 py-0.5 text-xs font-bold rounded border bg-blue-50 text-blue-700 border-blue-200 font-mono"
                  >
                     🛡️ {{ role }}
                  </span>
                  
                  <!-- Añadir Rol Button -->
                  <button @click="openRoleEditor(user)" class="px-2 py-0.5 text-xs font-bold rounded border border-dashed border-gray-400 text-gray-400 hover:text-ibpms-brand hover:border-ibpms-brand transition">
                    + Asignar
                  </button>
               </div>
            </td>
            
            <!-- Estado -->
            <td class="px-4 py-3 text-center">
               <span class="w-2.5 h-2.5 rounded-full inline-block" :class="user.isActive ? 'bg-green-500' : 'bg-red-500'"></span>
            </td>

            <!-- Acciones -->
            <td class="px-4 py-3 text-right">
              <button @click="openRoleEditor(user)" class="text-gray-400 hover:text-ibpms-brand transition text-base">⚙️</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { PropType } from 'vue';
import type { UserProfile } from '@/types/Security';

const props = defineProps({
  users: {
    type: Array as PropType<UserProfile[]>,
    default: () => []
  }
});

const emit = defineEmits(['edit-roles']);

const openRoleEditor = (user: UserProfile) => {
  emit('edit-roles', user);
};
</script>
