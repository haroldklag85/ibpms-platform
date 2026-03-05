<template>
  <div v-if="task" class="flex flex-col h-full space-y-6">
    <div>
      <h3 class="text-lg font-bold text-gray-900 dark:text-white">{{ task.name }}</h3>
      <p class="text-xs text-gray-500 font-mono mt-1">ID: {{ task.id }}</p>
    </div>

    <!-- Edit Form -->
    <div class="space-y-4 flex-1">
      
      <!-- Esfuerzo / Horas -->
      <div>
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">
          Esfuerzo Planificado (Horas)
        </label>
        <div class="mt-1">
          <input 
            type="number" 
            :value="task.estimatedHours"
            @input="updateField('estimatedHours', $event)"
            :disabled="store.isPublished"
            class="shadow-sm focus:ring-blue-500 focus:border-blue-500 block w-full sm:text-sm border-gray-300 rounded-md dark:bg-gray-700 dark:border-gray-600 dark:text-white disabled:bg-gray-100 disabled:cursor-not-allowed transition-colors"
          >
        </div>
      </div>

      <!-- Form Key (Crítico para UX Defensiva) -->
      <div>
         <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">
           Form Key (Camunda) <span class="text-red-500">*</span>
         </label>
         <div class="mt-1">
           <input 
             type="text" 
             :value="task.formKey"
             @input="updateField('formKey', $event)"
             :disabled="store.isPublished"
             placeholder="Ej: ext_app_form_1"
             :class="[
               'shadow-sm focus:ring-blue-500 focus:border-blue-500 block w-full sm:text-sm rounded-md transition-colors',
               store.isPublished ? 'bg-gray-100 cursor-not-allowed dark:bg-gray-900' : 'dark:bg-gray-700 dark:text-white',
               !task.formKey ? 'border-red-300 dark:border-red-500 focus:border-red-500 focus:ring-red-500 bg-red-50 dark:bg-red-900/20' : 'border-gray-300 dark:border-gray-600'
             ]"
           >
         </div>
         <!-- Instrucción UX -->
         <p v-if="!task.formKey && !store.isPublished" class="text-xs text-red-600 dark:text-red-400 mt-2 font-medium animate-pulse">
           ⚠️ Requerido. La plantilla no podrá publicarse mientras existan tareas sin formulario.
         </p>
         <p v-else class="text-xs text-gray-500 dark:text-gray-400 mt-2">
           Identificador único del Formulario en iForms.
         </p>
      </div>
      
    </div>

    <!-- Alert si está Publicado -->
    <div v-if="store.isPublished" class="mt-auto bg-blue-50 border border-blue-200 p-4 rounded-md">
       <div class="flex">
         <div class="flex-shrink-0">
           <svg class="h-5 w-5 text-blue-400" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd" /></svg>
         </div>
         <div class="ml-3">
           <p class="text-sm text-blue-700">
             Esta plantilla está en modo <strong>Read-Only</strong> por estar en Producción.
           </p>
         </div>
       </div>
    </div>
  </div>

  <!-- Empty State -->
  <div v-else class="h-full flex flex-col items-center justify-center text-center p-6 empty-state">
    <svg class="mx-auto h-12 w-12 text-gray-400 dark:text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 15l-2 5L9 9l11 4-5 2zm0 0l5 5M7.188 2.239l.777 2.897M5.136 7.965l-2.898-.777M13.95 4.05l-2.122 2.122m-5.657 5.656l-2.12 2.122"></path></svg>
    <h3 class="mt-2 text-sm font-medium text-gray-900 dark:text-gray-100">Cero propiedades seleccionadas</h3>
    <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
      Seleccione una tarea a la izquierda en el panel del <span class="font-semibold">WBS</span> para inspeccionar sus atributos.
    </p>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useProjectTemplateStore } from '@/stores/useProjectTemplateStore';

const store = useProjectTemplateStore();

const task = computed(() => store.selectedTask);

// Handler custom para evitar mutar props directamente, pero si actualizamos el store
const updateField = (field: 'estimatedHours' | 'formKey', event: Event) => {
    const target = event.target as HTMLInputElement;
    let val: any = target.value;
    
    if (field === 'estimatedHours') {
        val = Number(val);
    }

    if (field === 'formKey' && val.trim() === '') {
        val = null;
    }

    store.updateSelectedTask({ [field]: val });
};
</script>

<style scoped>
/* Transicion Empty state a Formulario */
.empty-state {
    animation: fade-in 0.3s ease-in-out;
}
@keyframes fade-in {
    from { opacity: 0; transform: translateY(5px); }
    to { opacity: 1; transform: translateY(0); }
}
</style>
