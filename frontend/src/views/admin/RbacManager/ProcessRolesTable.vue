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
            <div class="space-y-4">
              <!-- CA-4 & CA-6: Matriz Iniciador vs Ejecutor con Herencia -->
              <table v-if="rol.processPermissions && rol.processPermissions.length > 0" class="w-full text-xs border rounded-lg overflow-hidden">
                <thead class="bg-gray-50 text-gray-500 font-bold uppercase text-[9px]">
                  <tr>
                    <th class="px-2 py-1 text-left">Rol/Grupo</th>
                    <th class="px-2 py-1 text-center">Iniciar</th>
                    <th class="px-2 py-1 text-center">Ejecutar</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-gray-100 bg-white">
                  <tr v-for="permission in rol.processPermissions" :key="permission.id" class="hover:bg-gray-50/50">
                    <td class="px-2 py-2">
                      <div class="flex flex-col">
                        <span class="font-medium text-gray-700">{{ permission.roleName || 'Grupo' }}</span>
                        <!-- CA-06: Indicador de Herencia Piramidal -->
                        <span 
                          v-if="permission.sourceRoleId && permission.sourceRoleId !== rol.id"
                          class="text-[9px] text-indigo-500 font-bold flex items-center gap-0.5"
                          title="Permiso heredado del ancestro"
                        >
                          ↳ Heredado
                        </span>
                        <span v-else class="text-[9px] text-emerald-500 font-medium">Propio</span>
                      </div>
                    </td>
                    <td class="px-2 py-2 text-center">
                      <input
                        type="checkbox"
                        class="rounded text-indigo-600 focus:ring-indigo-500 disabled:opacity-50"
                        :checked="permission.canInitiateProcess"
                        :disabled="permission.sourceRoleId && permission.sourceRoleId !== rol.id"
                        @change="togglePermission(rol, permission, 'canInitiateProcess')"
                      />
                    </td>
                    <td class="px-2 py-2 text-center">
                      <input
                        type="checkbox"
                        class="rounded text-orange-600 focus:ring-orange-500 disabled:opacity-50"
                        :checked="permission.canExecuteTasks"
                        :disabled="permission.sourceRoleId && permission.sourceRoleId !== rol.id"
                        @change="togglePermission(rol, permission, 'canExecuteTasks')"
                      />
                    </td>
                  </tr>
                </tbody>
              </table>
              
              <div v-if="!rol.processPermissions || rol.processPermissions.length === 0" class="text-[11px] text-amber-600 bg-amber-50 p-2 rounded border border-amber-100 italic">
                Sin matriz de permisos definida para este carril.
              </div>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { useRbacStore } from '@/stores/rbacStore'

const store = useRbacStore()

/** CA-4: Toggle granular permission and persist to backend */
async function togglePermission(role, permission, field) {
  const oldValue = permission[field]
  permission[field] = !oldValue // Optimistic UI
  
  try {
    await store.updateProcessPermission(role.id, {
      ...permission,
      [field]: !oldValue
    })
  } catch (err) {
    permission[field] = oldValue // Rollback on error
    alert('Error al actualizar permisos: ' + err.message)
  }
}
</script>
