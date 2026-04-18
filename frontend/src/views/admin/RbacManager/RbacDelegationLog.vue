<template>
  <div class="p-4 space-y-6">

    <!-- Formulario de Nueva Delegación -->
    <div class="bg-white border border-gray-200 rounded-lg p-5 shadow-sm">
      <h3 class="text-sm font-semibold text-gray-800 mb-4 flex items-center gap-2">
        <svg class="w-4 h-4 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
        </svg>
        Nueva Cesión Temporal de Poder — CA-9 US-036
      </h3>

      <form @submit.prevent="submitDelegation" novalidate class="space-y-4">

        <!-- Selector de Receptor -->
        <div>
          <label class="block text-xs font-medium text-gray-600 mb-1">Usuario Receptor (Sustituto)</label>
          <select
            v-model="form.recipientId"
            data-testid="select-recipient"
            class="w-full text-sm border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-400"
            :class="{ 'border-red-400': errors.recipientId }"
          >
            <option value="">— Seleccione un usuario —</option>
            <option v-for="user in availableUsers" :key="user.id" :value="user.id">
              {{ user.email }}
            </option>
          </select>
          <p v-if="errors.recipientId" class="text-xs text-red-500 mt-1" data-testid="error-recipient">
            {{ errors.recipientId }}
          </p>
        </div>

        <!-- Fechas -->
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-xs font-medium text-gray-600 mb-1">Fecha de Inicio</label>
            <input
              v-model="form.startDate"
              type="datetime-local"
              data-testid="input-start-date"
              class="w-full text-sm border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-400"
              :class="{ 'border-red-400': errors.startDate }"
            />
            <p v-if="errors.startDate" class="text-xs text-red-500 mt-1" data-testid="error-start-date">
              {{ errors.startDate }}
            </p>
          </div>
          <div>
            <label class="block text-xs font-medium text-gray-600 mb-1">Fecha de Fin</label>
            <input
              v-model="form.endDate"
              type="datetime-local"
              data-testid="input-end-date"
              class="w-full text-sm border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-400"
              :class="{ 'border-red-400': errors.endDate }"
            />
            <p v-if="errors.endDate" class="text-xs text-red-500 mt-1" data-testid="error-end-date">
              {{ errors.endDate }}
            </p>
          </div>
        </div>

        <!-- Motivo -->
        <div>
          <label class="block text-xs font-medium text-gray-600 mb-1">Motivo (opcional)</label>
          <input
            v-model="form.reason"
            type="text"
            maxlength="500"
            data-testid="input-reason"
            placeholder="Ej: Vacaciones del 21 al 30 de abril"
            class="w-full text-sm border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-400"
          />
        </div>

        <!-- Alerta de error global -->
        <div
          v-if="globalError"
          data-testid="alert-global-error"
          class="rounded bg-red-50 border border-red-200 px-3 py-2 text-xs text-red-700"
        >
          {{ globalError }}
        </div>

        <!-- Botón submit -->
        <div class="flex justify-end">
          <button
            type="submit"
            data-testid="btn-submit-delegation"
            :disabled="submitting"
            class="inline-flex items-center gap-2 px-4 py-2 text-sm font-semibold bg-indigo-600 text-white rounded hover:bg-indigo-700 transition-colors disabled:opacity-50"
          >
            <svg v-if="submitting" class="animate-spin h-4 w-4" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path>
            </svg>
            {{ submitting ? 'Creando...' : 'Crear Delegación' }}
          </button>
        </div>
      </form>
    </div>

    <!-- Toast de éxito -->
    <div
      v-if="successMsg"
      data-testid="toast-success"
      class="rounded bg-green-50 border border-green-200 px-4 py-3 text-sm text-green-800 flex items-center gap-2"
    >
      <svg class="w-4 h-4 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
      </svg>
      {{ successMsg }}
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import apiClient from '@/services/apiClient'

// ID del usuario autenticado actual (en producción viene del store de sesión)
// Para el demostrador se usa un valor mock; reemplazar por useAuthStore().currentUser.id
const CURRENT_USER_ID = '00000000-0000-0000-0000-000000000001'

const availableUsers = ref([])
const submitting = ref(false)
const successMsg = ref('')
const globalError = ref('')

const form = reactive({
  recipientId: '',
  startDate: '',
  endDate: '',
  reason: '',
})

const errors = reactive({
  recipientId: '',
  startDate: '',
  endDate: '',
})

onMounted(async () => {
  try {
    const { data } = await apiClient.get('/admin/users')
    availableUsers.value = data.filter(u => u.id !== CURRENT_USER_ID)
  } catch {
    // En demo, silenciamos errores de carga de usuarios
  }
})

/**
 * CA-9 US-036 — Validación reactiva del formulario de delegación.
 * Bloquea el submit si:
 *   - No se seleccionó receptor
 *   - startDate es retroactiva (anterior a "ahora")
 *   - endDate no es posterior a startDate (fechas invertidas)
 */
function validate() {
  errors.recipientId = ''
  errors.startDate = ''
  errors.endDate = ''

  let valid = true

  if (!form.recipientId) {
    errors.recipientId = 'Debe seleccionar un usuario receptor.'
    valid = false
  }

  if (!form.startDate) {
    errors.startDate = 'La fecha de inicio es obligatoria.'
    valid = false
  } else {
    const start = new Date(form.startDate)
    const now = new Date()
    // Margen de 60 segundos de tolerancia (igual que el backend)
    if (start < new Date(now.getTime() - 60_000)) {
      errors.startDate = 'La fecha de inicio no puede ser retroactiva.'
      valid = false
    }
  }

  if (!form.endDate) {
    errors.endDate = 'La fecha de fin es obligatoria.'
    valid = false
  } else if (form.startDate && new Date(form.startDate) >= new Date(form.endDate)) {
    errors.endDate = 'La fecha de fin debe ser posterior a la fecha de inicio.'
    valid = false
  }

  return valid
}

async function submitDelegation() {
  globalError.value = ''
  successMsg.value = ''

  if (!validate()) return

  submitting.value = true
  try {
    await apiClient.post(`/admin/users/${CURRENT_USER_ID}/delegate`, {
      recipientId: form.recipientId,
      startDate: new Date(form.startDate).toISOString().slice(0, 19),
      endDate: new Date(form.endDate).toISOString().slice(0, 19),
      reason: form.reason || null,
    })

    successMsg.value = 'Delegación creada exitosamente. El receptor dispondrá de los privilegios durante la ventana temporal.'
    Object.assign(form, { recipientId: '', startDate: '', endDate: '', reason: '' })
  } catch (err) {
    globalError.value = err?.response?.data?.message ?? 'Error al crear la delegación. Verifique los datos e intente de nuevo.'
  } finally {
    submitting.value = false
  }
}
</script>
