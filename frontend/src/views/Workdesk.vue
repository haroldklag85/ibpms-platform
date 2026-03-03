<template>
  <div class="h-full flex flex-col relative" v-cloak>
    <!-- Overlay Cargando Global -->
    <div v-if="isLoading" class="absolute inset-0 bg-white/70 flex items-center justify-center z-50">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-ibpms-brand"></div>
    </div>

    <!-- ═══════════ Toast Notifications ═══════════ -->
    <Transition name="toast-slide">
      <div v-if="toastSuccess" class="fixed top-4 right-4 z-[100] bg-green-600 text-white px-5 py-3 rounded-lg shadow-xl flex items-center space-x-3 animate-pulse">
        <svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
        <span class="text-sm font-medium">{{ toastSuccess }}</span>
        <button @click="clearToasts" class="ml-2 text-green-200 hover:text-white">&times;</button>
      </div>
    </Transition>
    <Transition name="toast-slide">
      <div v-if="toastError" class="fixed top-4 right-4 z-[100] bg-red-600 text-white px-5 py-3 rounded-lg shadow-xl flex items-center space-x-3">
        <svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
        <span class="text-sm font-medium">{{ toastError }}</span>
        <button @click="clearToasts" class="ml-2 text-red-200 hover:text-white">&times;</button>
      </div>
    </Transition>

    <div class="flex justify-between items-center mb-6">
      <h2 class="text-2xl font-bold text-gray-800">Workdesk (Bandeja de Tareas)</h2>
      
      <!-- Filtros Rápidos y Refresh -->
      <div class="flex space-x-2">
        <button @click="loadBothQueues" class="px-4 py-2 bg-gray-100 text-gray-700 rounded shadow-sm text-sm font-medium hover:bg-gray-200" title="Refrescar">
          🔄
        </button>
        <button @click="currentTab = 'MINE'" :class="[currentTab === 'MINE' ? 'bg-ibpms text-white' : 'bg-white text-gray-700 hover:bg-gray-50']" class="px-4 py-2 border rounded shadow-sm text-sm font-medium transition">
          Mis Tareas ({{ tasks.length }})
        </button>
        <button @click="currentTab = 'CANDIDATES'" :class="[currentTab === 'CANDIDATES' ? 'bg-ibpms text-white' : 'bg-white text-gray-700 hover:bg-gray-50']" class="px-4 py-2 border rounded shadow-sm text-sm transition">
          Cola Grupal ({{ candidates.length }})
        </button>
      </div>
    </div>

    <!-- Error Bar -->
    <div v-if="error" class="bg-red-50 border-l-4 border-red-500 p-4 mb-4 rounded shadow-sm">
      <p class="text-red-700 font-medium">⚠️ Error de conexión: {{ error }}</p>
    </div>

    <!-- Grid Layout para la Bandeja Vertical -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 flex-1 overflow-hidden">
      
      <!-- Listado de Tareas (Panel Izquierdo/Central) -->
      <div class="lg:col-span-2 overflow-y-auto pr-2 space-y-4">
        
        <div v-if="(currentTab === 'MINE' && tasks.length === 0) || (currentTab === 'CANDIDATES' && candidates.length === 0)" class="text-center py-20 bg-gray-50 border-2 border-dashed rounded-lg">
           <span class="text-4xl">📭</span>
           <p class="mt-4 text-gray-500 font-medium">Bandeja Vacía. No hay tareas pendientes en esta vista.</p>
        </div>

        <!-- Renderizado Dinámico de Tareas -->
        <div v-for="task in activeList" :key="task.id" class="bg-white p-5 rounded-lg border-l-4 shadow-sm hover:shadow-md transition cursor-pointer"
             :class="{'border-l-blue-500': task.priority <= 50, 'border-l-red-500': task.priority > 50}">
          <div class="flex justify-between items-start mb-2">
            <div class="flex items-center space-x-2">
              <span v-if="task.priority > 50" class="px-2 lg:py-1 bg-red-100 text-red-800 text-xs font-bold rounded">URGENTE</span>
              <span v-else class="px-2 lg:py-1 bg-blue-100 text-blue-800 text-xs font-bold rounded">NORMAL</span>
              
              <span class="px-2 lg:py-1 bg-gray-100 text-gray-800 text-xs font-semibold rounded">
                Creado: {{ new Date(task.created).toLocaleDateString() }}
              </span>
              <span v-if="task.due" class="text-xs font-mono text-purple-600 font-bold border rounded px-1">{{ new Date(task.due).toLocaleString() }}</span>
            </div>
            <!-- Checkbox de "Bulk Action" (Futuro) -->
            <input type="checkbox" class="h-4 w-4 text-ibpms-brand rounded border-gray-300">
          </div>
          
          <h3 class="text-lg font-bold text-gray-900">{{ task.name }}</h3>
          <p class="text-gray-600 text-sm mb-4">Módulo (DefId): <span class="font-mono text-xs">{{ task.processDefinitionId }}</span></p>
          <p v-if="task.description" class="text-gray-500 text-xs italic mb-4">"{{ task.description }}"</p>
          
          <div class="flex justify-between items-center">
            <!-- ═══ Botones pestaña "Mis Tareas" ═══ -->
            <div v-if="currentTab === 'MINE'" class="flex items-center space-x-3">
              <button @click.stop="openForm(task)" class="px-4 py-2 bg-ibpms-brand text-white text-sm font-bold rounded shadow-sm hover:bg-blue-600 transition">
                Abrir Formulario
              </button>
              <button @click.stop="openUnclaimModal(task)" class="px-3 py-2 border border-orange-300 text-orange-600 text-sm font-medium rounded hover:bg-orange-50 transition inline-flex items-center space-x-1">
                <span>↩️</span> <span>Liberar</span>
              </button>
              <button @click.stop="openReassignModal(task)" class="px-3 py-2 border border-purple-300 text-purple-600 text-sm font-medium rounded hover:bg-purple-50 transition inline-flex items-center space-x-1">
                <span>🔄</span> <span>Reasignar</span>
              </button>
            </div>

            <!-- ═══ Botón pestaña "Cola Grupal" ═══ -->
            <button v-if="currentTab === 'CANDIDATES'" @click.stop="handleClaim(task.id)" class="px-4 py-2 bg-ibpms shadow text-white text-sm font-bold rounded hover:bg-gray-800 transition">
              Asignarmela (Claim)
            </button>
          </div>
        </div>

      </div>

      <!-- Panel Derecho Auxiliar (Resumen / QA Agent / IA) -->
      <div class="hidden lg:block bg-white p-6 rounded-lg shadow-sm border h-fit sticky top-0">
        <h4 class="font-bold text-gray-800 mb-4 border-b pb-2">Resumen de Bandeja</h4>
        <ul class="space-y-3 text-sm text-gray-600">
          <li class="flex justify-between"><span>Mis Tareas Totales:</span> <span class="font-bold">{{ tasks.length }}</span></li>
          <li class="flex justify-between"><span>Cola Candidata:</span> <span class="font-bold">{{ candidates.length }}</span></li>
          <li class="flex justify-between text-red-600"><span>Urgentes Totales:</span> <span class="font-bold">{{ urgentCount }}</span></li>
        </ul>
        
        <div class="mt-8 bg-gray-50 p-4 rounded border border-gray-200">
          <p class="text-xs text-gray-500 mb-2 font-mono">📡 ESTADO DE MOTOR (API)</p>
          <p class="text-sm text-gray-600">Polling: <span class="font-bold text-green-600">Online</span></p>
          <p class="text-xs text-gray-400 mt-2">Próxima Sincronización en: --s</p>
        </div>
      </div>

    </div>

    <!-- ═══════════ Modal Formulario Dinámico ═══════════ -->
    <div v-if="selectedSchema" class="fixed inset-0 bg-black/50 z-50 flex justify-end transition-opacity">
      <div class="w-full max-w-2xl bg-white h-full shadow-2xl overflow-y-auto animate-slide-in p-6">
        <div class="flex justify-between items-center mb-6 border-b pb-4">
          <h2 class="text-xl font-bold text-gray-800">Tarea en Progreso</h2>
          <button @click="selectedSchema = null" class="text-gray-500 hover:text-red-500 text-2xl font-bold">&times;</button>
        </div>
        
        <!-- Renderizador Inteligente -->
        <DynamicForm 
           :schema="selectedSchema" 
           @submit="onSolveTask" 
           @cancel="selectedSchema = null" 
        />
      </div>
    </div>

    <!-- ═══════════ Modal: Liberar Tarea (Unclaim) ═══════════ -->
    <div v-if="unclaimModalTask" class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
      <div class="bg-white rounded-xl shadow-2xl w-full max-w-md overflow-hidden">
        <div class="px-6 py-4 bg-orange-50 border-b border-orange-100 flex items-center justify-between">
          <h3 class="text-lg font-bold text-gray-900 flex items-center space-x-2">
            <span>↩️</span> <span>Liberar Tarea</span>
          </h3>
          <button @click="unclaimModalTask = null" class="text-gray-400 hover:text-gray-600 text-xl">&times;</button>
        </div>
        <div class="p-6 space-y-4">
          <p class="text-sm text-gray-600">
            Vas a devolver la tarea <span class="font-bold text-gray-900">"{{ unclaimModalTask.name }}"</span> a la cola grupal.
          </p>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Motivo (Opcional)</label>
            <textarea 
              v-model="unclaimReason" 
              rows="3" 
              placeholder="Ej. Conflicto de interés, carga laboral excesiva..."
              class="w-full rounded-md border-gray-300 shadow-sm focus:border-orange-500 focus:ring-orange-500 text-sm p-3 border resize-none"
            ></textarea>
          </div>
          <div class="flex justify-end space-x-3 pt-2">
            <button @click="unclaimModalTask = null" class="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition">
              Cancelar
            </button>
            <button @click="confirmUnclaim" :disabled="isLoading" class="px-4 py-2 text-sm font-medium text-white bg-orange-600 hover:bg-orange-700 rounded-lg shadow transition disabled:opacity-50">
              Confirmar Liberación
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ═══════════ Modal: Reasignar Tarea ═══════════ -->
    <div v-if="reassignModalTask" class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
      <div class="bg-white rounded-xl shadow-2xl w-full max-w-md overflow-hidden">
        <div class="px-6 py-4 bg-purple-50 border-b border-purple-100 flex items-center justify-between">
          <h3 class="text-lg font-bold text-gray-900 flex items-center space-x-2">
            <span>🔄</span> <span>Reasignar Tarea</span>
          </h3>
          <button @click="reassignModalTask = null" class="text-gray-400 hover:text-gray-600 text-xl">&times;</button>
        </div>
        <div class="p-6 space-y-4">
          <p class="text-sm text-gray-600">
            Selecciona al colaborador que recibirá la tarea <span class="font-bold text-gray-900">"{{ reassignModalTask.name }}"</span>.
          </p>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Asignar a:</label>
            <select 
              v-model="reassignTargetUserId" 
              class="w-full rounded-md border-gray-300 shadow-sm focus:border-purple-500 focus:ring-purple-500 text-sm p-2.5 border"
            >
              <option value="" disabled>-- Selecciona un usuario --</option>
              <option v-for="user in peerUsers" :key="user.id" :value="user.id">
                {{ user.name }} ({{ user.role }})
              </option>
            </select>
          </div>
          <div class="flex justify-end space-x-3 pt-2">
            <button @click="reassignModalTask = null" class="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition">
              Cancelar
            </button>
            <button @click="confirmReassign" :disabled="isLoading || !reassignTargetUserId" class="px-4 py-2 text-sm font-medium text-white bg-purple-600 hover:bg-purple-700 rounded-lg shadow transition disabled:opacity-50">
              Confirmar Reasignación
            </button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useTasks } from '@/composables/useTasks';
import type { TaskDto } from '@/types/Task';
import type { FormSchema } from '@/types/FormSchema';
import DynamicForm from '@/components/forms/DynamicForm.vue';

const {
  tasks, candidates, isLoading, error,
  toastSuccess, toastError, clearToasts,
  peerUsers,
  fetchMyTasks, fetchCandidateTasks,
  claimTask, unclaimTask, reassignTask,
  fetchPeerUsers
} = useTasks();

// Form Modal State
const selectedSchema = ref<FormSchema | null>(null);

// Unclaim Modal State
const unclaimModalTask = ref<TaskDto | null>(null);
const unclaimReason = ref('');

// Reassign Modal State
const reassignModalTask = ref<TaskDto | null>(null);
const reassignTargetUserId = ref('');

// Mock Schema de Prueba (Hasta que llegue del backend por tarea)
const mockupSchema: FormSchema = {
  formId: "test_form_01",
  title: "Aprobación de Expediente",
  description: "Diligencie las variables extraídas de la solicitud.",
  fields: [
    { key: "solicitante", label: "Nombre del Solicitante", type: "string", required: true, disabled: true, defaultValue: "Pedro Pérez" },
    { key: "monto", label: "Monto Aprobado (USD)", type: "number", required: true, metadata: { min: 0, max: 10000 } },
    { key: "estado", label: "Decisión", type: "select", required: true, options: [
        { label: "Aprobar Expediente", value: "APROBADO" },
        { label: "Rechazar por Faltantes", value: "RECHAZADO" }
    ]},
    { key: "fechaFirma", label: "Fecha de Formalización", type: "date", required: true },
    { key: "esUrgente", label: "Marcar Notificación Prioritaria", type: "boolean" }
  ]
};

const openForm = (_task: TaskDto) => {
  selectedSchema.value = mockupSchema;
};

const onSolveTask = async (payload: any) => {
  console.log("📝 JSON PAYLOAD GENERADO POR EL MOTOR:", payload);
  alert("Tarea enviada al backend exitosamente. Revisa la consola (F12) para ver el Payload JSON resultante.");
  selectedSchema.value = null;
  await fetchMyTasks();
};

// ── Modal Openers ──────────────────────────────────────────
const openUnclaimModal = (task: TaskDto) => {
  unclaimReason.value = '';
  unclaimModalTask.value = task;
};

const openReassignModal = async (task: TaskDto) => {
  reassignTargetUserId.value = '';
  reassignModalTask.value = task;
  await fetchPeerUsers(); // Cargar usuarios disponibles
};

// ── Modal Actions ──────────────────────────────────────────
const confirmUnclaim = async () => {
  if (!unclaimModalTask.value) return;
  try {
    await unclaimTask(unclaimModalTask.value.id, unclaimReason.value || undefined);
    unclaimModalTask.value = null;
    await fetchCandidateTasks(); // Refresca cola grupal
  } catch {
    // Error ya manejado por toast en composable
  }
};

const confirmReassign = async () => {
  if (!reassignModalTask.value || !reassignTargetUserId.value) return;
  try {
    await reassignTask(reassignModalTask.value.id, reassignTargetUserId.value);
    reassignModalTask.value = null;
  } catch {
    // Error ya manejado por toast en composable
  }
};

// Tabs UI State
const currentTab = ref<'MINE' | 'CANDIDATES'>('MINE');

const activeList = computed(() => {
  return currentTab.value === 'MINE' ? tasks.value : candidates.value;
});

const urgentCount = computed(() => {
   return tasks.value.filter((t: TaskDto) => t.priority > 50).length + candidates.value.filter((t: TaskDto) => t.priority > 50).length;
});

const loadBothQueues = async () => {
  await Promise.allSettled([
    fetchMyTasks(),
    fetchCandidateTasks()
  ]);
};

const handleClaim = async (taskId: string) => {
  try {
    await claimTask(taskId);
    await fetchMyTasks();
    currentTab.value = 'MINE'; 
  } catch {
    // Error ya manejado por toast en composable
  }
};

onMounted(() => {
  loadBothQueues();
});
</script>

<style scoped>
[v-cloak] {
  display: none;
}

/* Toast slide transition */
.toast-slide-enter-active,
.toast-slide-leave-active {
  transition: all 0.3s ease;
}
.toast-slide-enter-from {
  opacity: 0;
  transform: translateX(100px);
}
.toast-slide-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}
</style>
