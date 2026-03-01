<template>
  <div class="bg-white border text-sm text-gray-700 min-h-[300px]">
    <div v-if="store.isLoading" class="p-8 text-center text-gray-500">
      <svg class="animate-spin h-6 w-6 text-indigo-600 mx-auto mb-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
      Sincronizando Active Directory...
    </div>

    <table v-else class="w-full text-left border-collapse">
      <thead>
        <tr class="bg-gray-100/50 text-gray-500 uppercase text-xs tracking-wider">
          <th class="p-4 font-semibold w-1/4">Nombre de Rol Global</th>
          <th class="p-4 font-semibold w-2/4">Descripción y Scope</th>
          <th class="p-4 font-semibold w-1/4">Miembros</th>
          <th class="p-4 font-semibold w-16 text-center">Admin</th>
        </tr>
      </thead>
      <tbody class="divide-y divide-gray-200">
        <tr v-for="rol in store.globalRoles" :key="rol.id" class="hover:bg-indigo-50/20 transition-colors">
          <td class="p-4 align-top">
            <div class="flex items-center gap-2 font-medium text-gray-900">
              <span class="w-2 h-2 rounded-full bg-indigo-500"></span>
              {{ rol.name }}
            </div>
          </td>
          <td class="p-4 align-top text-gray-600 whitespace-pre-line leading-relaxed">
            {{ rol.description }}
          </td>
          <td class="p-4 align-top">
            <div class="space-y-1">
              <div v-for="user in rol.members" :key="user.id" class="flex gap-2 items-center bg-gray-50 px-2 py-1 rounded w-fit border border-gray-100">
                 <div class="w-5 h-5 bg-gradient-to-tr from-indigo-500 to-purple-500 rounded-full flex items-center justify-center text-white text-[10px] uppercase font-bold">{{ user.email.charAt(0) }}</div>
                 <span class="text-xs">{{ user.email.split('@')[0] }}</span>
              </div>
            </div>
            <button class="text-indigo-600 hover:text-indigo-800 text-xs font-semibold mt-2 flex items-center gap-1">
              + Añadir Miembro
            </button>
          </td>
          <td class="p-4 align-top text-center text-gray-400">
            <button class="hover:text-indigo-600 w-8 h-8 rounded-full hover:bg-indigo-50 transition-colors">⋮</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { useRbacStore } from '@/stores/rbacStore'
const store = useRbacStore()
</script>
