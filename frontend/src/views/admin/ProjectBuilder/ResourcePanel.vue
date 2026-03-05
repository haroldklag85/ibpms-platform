<template>
  <div v-if="isOpen" class="fixed inset-0 z-50 overflow-hidden">
    <!-- Backdrop oscuro -->
    <div 
      class="absolute inset-0 bg-gray-500 bg-opacity-75 transition-opacity" 
      @click="closePanel"
      aria-hidden="true"
    ></div>

    <!-- Drawer Right -->
    <div class="fixed inset-y-0 right-0 flex max-w-full pl-10">
      <div class="w-screen max-w-md transform transition-transform duration-300 ease-in-out">
        <div class="h-full flex flex-col bg-white dark:bg-gray-800 shadow-xl overflow-y-auto">
          
          <!-- Header -->
          <div class="px-6 py-4 bg-indigo-600 dark:bg-indigo-900 flex justify-between items-center text-white">
            <h2 class="text-lg font-bold">Asignación de Recursos</h2>
            <button @click="closePanel" class="text-indigo-200 hover:text-white transition-colors">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
            </button>
          </div>

          <!-- Body -->
          <div class="p-6 flex-1 flex flex-col space-y-6">
            
            <div class="bg-indigo-50 dark:bg-indigo-900/30 p-4 rounded-lg border border-indigo-100 dark:border-indigo-800">
              <h3 class="text-sm font-semibold text-indigo-800 dark:text-indigo-300">Detalles de la Tarea</h3>
              <p class="text-xl font-bold text-gray-900 dark:text-white mt-1">{{ task?.name }}</p>
              <div class="flex items-center space-x-4 mt-2 text-sm text-gray-500 dark:text-gray-400">
                <span class="flex items-center">
                   <svg class="w-4 h-4 mr-1 text-indigo-500" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clip-rule="evenodd" /></svg>
                   {{ task?.start }} -> {{ task?.end }}
                </span>
                <span class="px-2 py-0.5 rounded-full text-xs font-semibold" :class="statusColor(task?.status)">
                  {{ task?.status }}
                </span>
              </div>
            </div>

            <div v-if="successMsg" class="bg-green-50 border border-green-200 text-green-800 p-3 rounded-lg flex items-center text-sm">
                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                {{ successMsg }}
            </div>

            <!-- Formularios -->
            <div class="space-y-4">
              
              <!-- Asignado -->
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  Especialista / Obrero
                </label>
                <select 
                  v-model="form.assigneeUserId" 
                  class="block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white p-2.5 border"
                >
                  <option value="" disabled>-- Seleccione Responsable --</option>
                  <option v-for="user in users" :key="user.id" :value="user.id">
                    {{ user.name }} ({{ user.role }})
                  </option>
                </select>
              </div>

              <!-- Costo / Presupuesto -->
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  Presupuesto Real Ejecutado ($)
                </label>
                <div class="relative rounded-md shadow-sm">
                  <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <span class="text-gray-500 sm:text-sm">$</span>
                  </div>
                  <input 
                    type="number" 
                    v-model.number="form.actualBudget" 
                    placeholder="0.00" 
                    class="focus:ring-indigo-500 focus:border-indigo-500 block w-full pl-7 sm:text-sm border-gray-300 rounded-md p-2.5 dark:bg-gray-700 dark:border-gray-600 border dark:text-white"
                  >
                </div>
              </div>
            </div>
            
          </div>

          <!-- Footer Actions -->
          <div class="px-6 py-4 bg-gray-50 dark:bg-gray-800/80 border-t border-gray-200 dark:border-gray-700 flex justify-end space-x-3">
            <button 
              @click="closePanel"
              class="px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none dark:bg-gray-700 dark:text-gray-300 dark:border-gray-600 dark:hover:bg-gray-600 transition-colors"
            >
              Cancelar
            </button>
            <button 
              @click="saveResource"
              :disabled="isSaving"
              class="inline-flex justify-center items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 transition-colors"
            >
              <svg v-if="isSaving" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              Guardar Cambios
            </button>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue';
import apiClient from '@/services/apiClient';

const props = defineProps<{
  isOpen: boolean;
  task: any; // Gantt Task Object
}>();

const emit = defineEmits(['close', 'saved']);

const isSaving = ref(false);
const successMsg = ref('');

const users = ref<any[]>([]);

const form = reactive({
  assigneeUserId: '',
  actualBudget: null as number | null
});

const loadUsers = async () => {
    try {
        const response = await apiClient.get('/users/peers');
        users.value = response.data;
    } catch (e) {
        console.error("No se pudo cargar el LDAP", e);
    }
}

onMounted(() => {
    loadUsers();
});

watch(() => props.task, (newTask) => {
    if(newTask) {
        // Hydrate from Gantt Task custom properties we passed during render
        form.assigneeUserId = newTask.assigneeUserId || '';
        form.actualBudget = newTask.actualBudget || null;
        successMsg.value = '';
    }
}, { deep: true });

const closePanel = () => {
  emit('close');
};

const saveResource = async () => {
  if (!props.task) return;
  isSaving.value = true;
  successMsg.value = '';
  
  try {
    const payload = {
        assigneeUserId: form.assigneeUserId,
        actualBudget: form.actualBudget
    };
    
    // Epic 10.B PUT /assign endpoint
    await apiClient.put(`/execution/projects/tasks/${props.task.id}/assign`, payload);
    successMsg.value = 'Asignación Guardada Correctamente.';
    
    // Simulate updating the parent Gantt Task
    props.task.assigneeUserId = form.assigneeUserId;
    props.task.actualBudget = form.actualBudget;

    // Disparar update visual al Gantt
    setTimeout(() => {
        emit('saved', { ...form, taskId: props.task.id });
        closePanel();
        isSaving.value = false;
    }, 800);

  } catch (error) {
    console.error("Error al guardar recurso", error);
    isSaving.value = false;
  }
};

const statusColor = (status: string) => {
    switch (status) {
        case 'DONE': return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400';
        case 'IN_PROGRESS': return 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400';
        case 'BLOCKED': return 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400';
        case 'PENDING': default: return 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300';
    }
};
</script>
