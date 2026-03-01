<template>
  <div class="bg-white border text-sm text-gray-700 min-h-[300px]">
    
    <div class="p-4 bg-emerald-50/40 border-b border-emerald-100 flex items-start gap-4 text-emerald-800 text-xs">
      <svg class="w-6 h-6 shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
      <div>
        <p class="font-semibold mb-1">Roles Inferidos por Motor BPMN (Sprint 22 Deploy Hook)</p>
        <p class="text-emerald-700/80">
          Estos roles son extraídos automáticamente cada vez que desplegamos un archivo BPMN 2.0 en Camunda y lee los carriles "Lanes". 
          No puedes borrarlos desde aquí, pero sí puedes autorizar qué Grupos de Directorio Activo pueden ejecutarlos.
        </p>
      </div>
    </div>

    <div v-if="store.isLoading" class="p-8 text-center text-gray-500">Cargando BPMN Roles...</div>

    <table v-else class="w-full text-left border-collapse">
      <thead>
        <tr class="bg-gray-100/50 text-gray-500 uppercase text-xs tracking-wider">
          <th class="p-4 font-semibold w-1/4">Identificador de Carril</th>
          <th class="p-4 font-semibold w-2/4">Origen (Proceso Camunda)</th>
          <th class="p-4 font-semibold w-1/4">Grupos/Miembros Autorizados</th>
        </tr>
      </thead>
      <tbody class="divide-y divide-gray-200">
        <tr v-for="rol in store.processRoles" :key="rol.id" class="hover:bg-emerald-50/20 transition-colors">
          <td class="p-4 align-top">
            <div class="flex items-center gap-2 font-medium text-gray-900">
              <span class="w-2 h-2 rounded-full bg-emerald-500"></span>
              {{ rol.laneId }}
            </div>
            <div class="text-[10px] text-gray-400 font-mono mt-1 w-full truncate" :title="rol.name">{{ rol.name }}</div>
          </td>
          <td class="p-4 align-top">
            <div class="flex flex-col items-start gap-1">
              <span class="bg-slate-100 border border-slate-200 text-slate-700 font-mono px-2 py-0.5 rounded text-xs flex gap-1.5 items-center">
                 <svg class="w-3.5 h-3.5 text-orange-500" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 2a4 4 0 00-4 4v1H5a1 1 0 00-.994.89l-1 9A1 1 0 004 18h12a1 1 0 00.994-1.11l-1-9A1 1 0 0015 7h-1V6a4 4 0 00-4-4zm2 5V6a2 2 0 10-4 0v1h4zm-6 3a1 1 0 112 0 1 1 0 01-2 0zm7-1a1 1 0 100 2 1 1 0 000-2z" clip-rule="evenodd"></path></svg>
                 Def ID: {{ rol.processDefinitionId }}
              </span>
              <span class="text-xs text-gray-500 leading-relaxed max-w-sm">{{ rol.description }}</span>
            </div>
          </td>
          <td class="p-4 align-top">
            <div class="space-y-1">
              <div v-if="rol.members.length === 0" class="text-xs text-red-500 bg-red-50 border border-red-100 p-2 rounded flex items-center gap-1.5 font-medium">
                <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path></svg>
                Orphan Lane: Nadie puede ejecutarlo.
              </div>
              <div v-for="user in rol.members" :key="user.id" class="flex gap-2 items-center bg-gray-50 px-2 py-1 rounded w-fit border border-gray-100">
                 <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path></svg>
                 <span class="text-xs text-gray-700 font-medium">{{ user.email }}</span>
              </div>
            </div>
            <button class="text-emerald-600 hover:text-emerald-800 text-xs font-semibold mt-2 flex items-center gap-1">
              + Escanear Mapeo LDAP
            </button>
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
