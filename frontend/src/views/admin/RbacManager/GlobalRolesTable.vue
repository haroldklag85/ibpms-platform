<template>
  <div class="bg-white border text-sm text-gray-700 min-h-[300px]">

    <!-- CA-6 US-036: Acción de creación con selector de herencia piramidal -->
    <div class="flex justify-end gap-2 px-4 py-2 border-b border-gray-100 bg-gray-50/50">
      <button
        data-testid="btn-open-import-entraid"
        class="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold bg-emerald-600 text-white rounded hover:bg-emerald-700 transition-colors"
        @click="openImportModal"
      >☁️ Importar EntraID</button>
      
      <button
        data-testid="btn-open-create-role"
        class="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold bg-indigo-600 text-white rounded hover:bg-indigo-700 transition-colors"
        @click="createModalOpen = true"
      >+ Crear Rol Local</button>
    </div>

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
          <th class="p-4 font-semibold text-center">Admin</th>
        </tr>
      </thead>
      <tbody class="divide-y divide-gray-200">
        <tr v-for="rol in store.globalRoles" :key="rol.id" class="hover:bg-indigo-50/20 transition-colors">
          <td class="p-4 align-top">
            <div class="flex items-center gap-2 font-medium text-gray-900">
              <span class="w-2 h-2 rounded-full bg-indigo-500"></span>
              {{ rol.name }}
              <!-- CA-2 US-036: Badge de Guardián para el Rol Root — inmutable por diseño -->
              <span
                v-if="isRootRole(rol.name)"
                class="inline-flex items-center gap-1 rounded bg-red-100 px-2 py-0.5 text-[10px] font-bold text-red-800 border border-red-200"
                title="Rol Guardián Raíz — Inmutable por diseño de seguridad"
              >🔒 ROOT</span>
              <span v-if="rol.is_vip_restricted" class="inline-flex items-center gap-1 rounded bg-amber-100 px-2 py-0.5 text-[10px] font-bold text-amber-800 border border-amber-200" title="Rol restringido VIP">⭐ VIP</span>
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
          <td class="p-4 align-top text-center">
            <div class="flex items-center justify-center gap-1">

              <!-- CA-3 US-036: Botón de Asignación Masiva — visible solo en roles plantilla -->
              <button
                v-if="rol.isTemplate"
                data-testid="btn-mass-assign"
                :data-role-id="rol.id"
                class="inline-flex items-center gap-1 rounded bg-indigo-600 px-2 py-1 text-[11px] font-semibold text-white hover:bg-indigo-700 transition-colors"
                title="Asignación Masiva de este Rol Plantilla a múltiples usuarios"
                @click="onMassAssign(rol)"
              >
                👥 Masivo
              </button>

              <!-- CA-2 US-036: Botón Editar — oculto para ROLE_SUPER_ADMIN -->
              <button
                v-if="!isRootRole(rol.name)"
                data-testid="btn-edit-role"
                :data-role-id="rol.id"
                class="w-7 h-7 rounded-full hover:bg-indigo-50 hover:text-indigo-600 transition-colors text-gray-400"
                title="Editar rol"
                @click="onEditRole(rol)"
              >✏️</button>

              <!-- CA-2 US-036: Botón Borrar — PROHIBIDO para ROLE_SUPER_ADMIN (v-if defensivo) -->
              <button
                v-if="!isRootRole(rol.name)"
                data-testid="btn-delete-role"
                :data-role-id="rol.id"
                class="w-7 h-7 rounded-full hover:bg-red-50 hover:text-red-600 transition-colors text-gray-400"
                title="Eliminar rol"
                @click="onDeleteRole(rol)"
              >🗑️</button>

              <!-- CA-2 US-036: Indicador visual cuando el rol es Root (sin acción destructiva) -->
              <span
                v-if="isRootRole(rol.name)"
                class="text-gray-300 text-xs select-none"
                title="Este rol está protegido y no puede ser modificado ni eliminado"
              >— protegido —</span>

            </div>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- CA-6 US-036: Modal de Creación de Rol con selector de Herencia Piramidal -->
    <div
      v-if="createModalOpen"
      class="fixed inset-0 bg-black/40 flex items-center justify-center z-50"
      data-testid="modal-create-role"
    >
      <div class="bg-white rounded-xl shadow-2xl p-6 w-[420px] space-y-4">
        <h3 class="text-base font-bold text-gray-900">Crear Rol Global</h3>

        <div>
          <label class="block text-xs font-medium text-gray-600 mb-1">Nombre del Rol</label>
          <input
            v-model="newRole.name"
            type="text"
            data-testid="input-role-name"
            placeholder="ROLE_NOMBRE_DESCRIPTIVO"
            class="w-full text-sm border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-400"
          />
        </div>

        <div>
          <label class="block text-xs font-medium text-gray-600 mb-1">Descripción</label>
          <input
            v-model="newRole.description"
            type="text"
            data-testid="input-role-description"
            placeholder="Responsabilidades del rol…"
            class="w-full text-sm border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-400"
          />
        </div>

        <!-- CA-6: Selector de Rol Padre (Herencia Piramidal) -->
        <div>
          <label class="block text-xs font-medium text-gray-600 mb-1">
            Hereda permisos de (Rol Padre)
            <span class="text-gray-400 font-normal">— opcional</span>
          </label>
          <select
            v-model="newRole.parentRoleId"
            data-testid="select-parent-role"
            class="w-full text-sm border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-400"
          >
            <option value="">— Sin herencia —</option>
            <option v-for="rol in store.globalRoles" :key="rol.id" :value="rol.id">
              {{ rol.name }}
            </option>
          </select>
          <p class="text-[10px] text-gray-400 mt-1">
            El nuevo rol heredará los ProcessPermissions del rol seleccionado y de toda su cadena de ancestros.
          </p>
        </div>

        <div class="flex items-center gap-2">
          <input id="isTemplate" v-model="newRole.isTemplate" type="checkbox" class="rounded" data-testid="check-is-template" />
          <label for="isTemplate" class="text-xs text-gray-600">Es plantilla asignable masivamente</label>
        </div>

        <div class="flex gap-2 justify-end pt-2">
          <button
            class="px-3 py-1.5 text-sm text-gray-600 hover:bg-gray-100 rounded"
            @click="createModalOpen = false"
          >Cancelar</button>
          <button
            class="px-3 py-1.5 text-sm bg-indigo-600 text-white rounded hover:bg-indigo-700 font-semibold"
            :disabled="creatingRole"
            data-testid="btn-confirm-create-role"
            @click="confirmCreateRole"
          >{{ creatingRole ? 'Creando...' : 'Crear Rol' }}</button>
        </div>
      </div>
    </div>

    <!-- CA-6 US-036: Modal de Edición de Rol -->
    <div
      v-if="editModalOpen"
      class="fixed inset-0 bg-black/40 flex items-center justify-center z-50"
      data-testid="modal-edit-role"
    >
      <div class="bg-white rounded-xl shadow-2xl p-6 w-[420px] space-y-4">
        <h3 class="text-base font-bold text-gray-900">Editar Rol Global</h3>

        <div>
          <label class="block text-xs font-medium text-gray-600 mb-1">Nombre del Rol</label>
          <input
            v-model="editingRole.name"
            type="text"
            data-testid="input-edit-role-name"
            class="w-full text-sm border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-400"
          />
        </div>

        <div>
          <label class="block text-xs font-medium text-gray-600 mb-1">Descripción</label>
          <input
            v-model="editingRole.description"
            type="text"
            data-testid="input-edit-role-description"
            class="w-full text-sm border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-400"
          />
        </div>

        <div>
          <label class="block text-xs font-medium text-gray-600 mb-1">
            Hereda permisos de (Rol Padre)
          </label>
          <select
            v-model="editingRole.parentRoleId"
            data-testid="select-edit-parent-role"
            class="w-full text-sm border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-400"
          >
            <option value="">— Sin herencia —</option>
            <option v-for="rol in store.globalRoles" :key="rol.id" :value="rol.id" :disabled="rol.id === editingRole.id">
              {{ rol.name }}
            </option>
          </select>
        </div>

        <div class="flex items-center gap-2">
          <input id="editIsTemplate" v-model="editingRole.isTemplate" type="checkbox" class="rounded" data-testid="check-edit-is-template" />
          <label for="editIsTemplate" class="text-xs text-gray-600">Es plantilla asignable masivamente</label>
        </div>

        <div class="flex gap-2 justify-end pt-2">
          <button
            class="px-3 py-1.5 text-sm text-gray-600 hover:bg-gray-100 rounded"
            @click="editModalOpen = false"
          >Cancelar</button>
          <button
            class="px-3 py-1.5 text-sm bg-indigo-600 text-white rounded hover:bg-indigo-700 font-semibold"
            :disabled="updatingRole"
            data-testid="btn-confirm-edit-role"
            @click="confirmUpdateRole"
          >{{ updatingRole ? 'Actualizando...' : 'Guardar Cambios' }}</button>
        </div>
      </div>
    </div>

    <!-- Modal de confirmación Asignación Masiva (CA-3) -->
    <div
      v-if="massAssignTarget"
      class="fixed inset-0 bg-black/40 flex items-center justify-center z-50"
      data-testid="modal-mass-assign"
    >
      <div class="bg-white rounded-xl shadow-2xl p-6 w-96">
        <h3 class="text-base font-bold text-gray-900 mb-1">Asignación Masiva</h3>
        <p class="text-sm text-gray-600 mb-4">
          Asignando plantilla <span class="font-semibold text-indigo-700">{{ massAssignTarget.name }}</span>
          a {{ demoUserIds.length }} usuario(s).
        </p>
        <div class="bg-gray-50 rounded p-3 text-xs font-mono text-gray-500 mb-4 break-all">
          POST /api/v1/admin/roles/{{ massAssignTarget.id }}/assign-massively<br>
          {{ JSON.stringify(demoUserIds) }}
        </div>
        <div class="flex gap-2 justify-end">
          <button
            class="px-3 py-1.5 text-sm text-gray-600 hover:bg-gray-100 rounded"
            @click="massAssignTarget = null"
          >Cancelar</button>
          <button
            class="px-3 py-1.5 text-sm bg-indigo-600 text-white rounded hover:bg-indigo-700 font-semibold"
            :disabled="massAssigning"
            @click="confirmMassAssign"
          >{{ massAssigning ? 'Asignando...' : 'Confirmar' }}</button>
        </div>
      </div>
    </div>

    <!-- Modal de Importación EntraID (CA-1) -->
    <div
      v-if="importModalOpen"
      class="fixed inset-0 bg-black/40 flex items-center justify-center z-50"
      data-testid="modal-import-entraid"
    >
      <div class="bg-white rounded-xl shadow-2xl p-6 w-[450px] space-y-4">
        <div class="flex items-center justify-between">
          <h3 class="text-base font-bold text-gray-900">Importar Grupos EntraID</h3>
          <span class="text-[10px] bg-emerald-100 text-emerald-700 px-2 py-0.5 rounded font-bold uppercase">Azure AD Sync</span>
        </div>
        
        <p class="text-xs text-gray-500">Seleccione los grupos corporativos para mapearlos como roles locales en el iBPMS.</p>

        <div class="max-h-60 overflow-y-auto border rounded divide-y">
          <div v-for="group in entraIdGroups" :key="group.id" class="p-3 flex items-center justify-between hover:bg-gray-50">
            <div>
              <div class="text-sm font-semibold text-gray-800">{{ group.displayName }}</div>
              <div class="text-[10px] text-gray-400 font-mono">{{ group.id }}</div>
            </div>
            <button
              class="px-2 py-1 text-[11px] bg-emerald-50 text-emerald-600 border border-emerald-200 rounded hover:bg-emerald-600 hover:text-white transition-colors"
              @click="confirmImport(group)"
            >Importar</button>
          </div>
          <div v-if="entraIdGroups.length === 0" class="p-8 text-center text-gray-400 text-xs">
            No se encontraron grupos disponibles.
          </div>
        </div>

        <div class="flex justify-end pt-2">
          <button
            class="px-3 py-1.5 text-sm text-gray-600 hover:bg-gray-100 rounded"
            @click="importModalOpen = false"
          >Cerrar</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { useIntegrationStore } from '@/stores/useIntegrationStore';
import { ref, reactive } from 'vue'
import { useRbacStore } from '@/stores/rbacStore'
import { RoleFactories } from '@/tests/factories'

// @Traceability: Retro-Remediación ADR-006
const integrationStore = useIntegrationStore();

const store = useRbacStore()

// CA-2 US-036: Constante canónica del Rol Guardián (sincronizada con RoleService.ROOT_ROLE)
const ROOT_ROLE_NAME = 'ROLE_SUPER_ADMIN'

/** Determina si un nombre de rol es el Guardián Raíz inmutable */
function isRootRole(name) {
  return name === ROOT_ROLE_NAME
}

// --- CA-1: EntraID Import ---
const importModalOpen = ref(false)
const entraIdGroups = ref([])

async function openImportModal() {
  importModalOpen.value = true
  entraIdGroups.value = await store.fetchEntraIdGroups()
}

async function confirmImport(group) {
  try {
    await store.importRole(group)
    alert(`✅ Grupo ${group.displayName} importado con éxito.`)
  } catch (err) {
    alert('Error al importar: ' + err.message)
  }
}

// --- CA-2: Delete ---
function onDeleteRole(rol) {
  if (!confirm(`¿Eliminar el rol "${rol.name}"? Esta acción es irreversible.`)) return
  integrationStore.delete(`/admin/roles/${rol.id}`)
    .then(() => store.fetchRoles())
    .catch(err => alert(`Error al eliminar: ${err?.response?.data?.message ?? err.message}`))
}

// --- CA-6: Create Role with parentRole ---
const createModalOpen = ref(false)
const creatingRole = ref(false)
const newRole = reactive({ name: '', description: '', parentRoleId: '', isTemplate: false })

async function confirmCreateRole() {
  if (!newRole.name.trim()) return alert('El nombre del rol es obligatorio.')
  creatingRole.value = true
  try {
    const payload = {
      name: newRole.name.trim(),
      description: newRole.description.trim(),
      isTemplate: newRole.isTemplate,
      source: 'LOCAL',
      parentRole: newRole.parentRoleId ? { id: newRole.parentRoleId } : null,
    }
    await integrationStore.post('/admin/roles/', payload)
    createModalOpen.value = false
    Object.assign(newRole, { name: '', description: '', parentRoleId: '', isTemplate: false })
    store.fetchRoles()
  } catch (err) {
    alert(`Error al crear rol: ${err?.response?.data?.message ?? err.message}`)
  } finally {
    creatingRole.value = false
  }
}

// --- CA-2 & CA-6: Edit Role ---
const editModalOpen = ref(false)
const updatingRole = ref(false)
const editingRole = reactive({ id: null, name: '', description: '', parentRoleId: '', isTemplate: false })

function onEditRole(rol) {
  editingRole.id = rol.id
  editingRole.name = rol.name
  editingRole.description = rol.description
  editingRole.parentRoleId = rol.parentRole?.id || ''
  editingRole.isTemplate = !!rol.isTemplate
  editModalOpen.value = true
}

async function confirmUpdateRole() {
  if (!editingRole.name.trim()) return alert('El nombre del rol es obligatorio.')
  updatingRole.value = true
  try {
    const payload = {
      name: editingRole.name.trim(),
      description: editingRole.description.trim(),
      isTemplate: editingRole.isTemplate,
      parentRole: editingRole.parentRoleId ? { id: editingRole.parentRoleId } : null,
    }
    await store.updateRole(editingRole.id, payload)
    editModalOpen.value = false
  } catch (err) {
    alert(`Error al actualizar rol: ${err?.response?.data?.message ?? err.message}`)
  } finally {
    updatingRole.value = false
  }
}

// --- CA-3: Mass Assign ---
const massAssignTarget = ref(null)
const massAssigning = ref(false)
// IDs obtenidos del factory de tests
const demoUserIds = RoleFactories.demoUserIds

function onMassAssign(rol) {
  massAssignTarget.value = rol
}

async function confirmMassAssign() {
  if (!massAssignTarget.value) return
  massAssigning.value = true
  try {
    const { data } = await integrationStore.post(
      `/admin/roles/${massAssignTarget.value.id}/assign-massively`,
      demoUserIds
    )
    alert(`✅ Asignación completada: ${data.assigned} usuario(s) asignados.`)
    massAssignTarget.value = null
    store.fetchRoles()
  } catch (err) {
    alert(`Error en asignación masiva: ${err?.response?.data?.message ?? err.message}`)
  } finally {
    massAssigning.value = false
  }
}
</script>
