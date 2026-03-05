<template>
  <div class="space-y-4">
    <!-- Fases -->
    <div 
      v-for="phase in store.template?.phases" 
      :key="phase.id"
      class="border border-gray-200 dark:border-gray-700 rounded-lg overflow-hidden bg-white dark:bg-gray-800"
    >
      <!-- Phase Header -->
      <div class="bg-gray-100 dark:bg-gray-900 px-4 py-2 font-bold text-gray-800 dark:text-gray-200 border-b border-gray-200 dark:border-gray-700">
        {{ phase.name }}
      </div>

      <!-- Hitos (Milestones) -->
      <div class="p-4 space-y-4">
        <div 
          v-for="milestone in phase.milestones" 
          :key="milestone.id"
          class="border border-indigo-100 dark:border-indigo-900/50 rounded-md p-3 bg-indigo-50/30 dark:bg-indigo-900/10"
        >
          <div class="flex items-center mb-3">
             <span class="font-semibold text-indigo-800 dark:text-indigo-300 text-sm mr-2">{{ milestone.name }}</span>
             <!-- Red Flag Stage Gate -->
             <svg v-if="milestone.isStageGate" class="w-4 h-4 text-red-500" title="Stage Gate (Aprobación Hard Obligatoria)" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M3 6a3 3 0 013-3h10a1 1 0 01.8 1.6L14.25 8l2.55 3.4A1 1 0 0116 13H6a1 1 0 00-1 1v3a1 1 0 11-2 0V6z" clip-rule="evenodd" /></svg>
          </div>

          <!-- Draggable Zone for Tasks -->
          <!-- Vuedraggable is used to re-order tasks within the milestone natively -->
          <draggable 
            v-model="milestone.tasks" 
            item-key="id"
            ghost-class="ghost-task"
            :disabled="store.isPublished"
            class="space-y-2 min-h-[40px]"
          >
            <template #item="{ element: task }">
              <div 
                @click="store.selectTask(task.id)"
                :class="[
                  'p-3 rounded border text-sm flex justify-between items-center transition-all cursor-pointer group',
                  store.selectedTask?.id === task.id 
                    ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20 ring-1 ring-blue-500' // Selected
                    : 'border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 hover:border-blue-300 dark:hover:border-blue-700',
                  (!task.formKey && !store.isPublished) ? 'border-amber-400 bg-amber-50 dark:bg-amber-900/20' : '' // Alert Missing Form Key
                ]"
              >
                <div class="flex items-center">
                  <!-- Drag Handle SVG -->
                  <svg v-if="!store.isPublished" class="w-4 h-4 mr-2 text-gray-400 opacity-50 group-hover:opacity-100 cursor-move" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 8h16M4 16h16"></path></svg>
                  <span class="font-medium text-gray-800 dark:text-gray-200">{{ task.name }}</span>
                </div>
                
                <div class="flex items-center space-x-3 text-xs">
                   <span class="text-gray-500 dark:text-gray-400">{{ task.estimatedHours }}h</span>
                   <span v-if="task.formKey" class="text-green-600 dark:text-green-400 font-mono px-1.5 py-0.5 bg-green-100 dark:bg-green-900/30 rounded" title="Formulario Asignado">✓ f_key</span>
                   <span v-else class="text-amber-600 dark:text-amber-400 font-bold px-1.5 py-0.5 bg-amber-100 dark:bg-amber-900/30 rounded animate-pulse" title="Falta Form Key">⚠️ Bloqueante</span>
                </div>
              </div>
            </template>
          </draggable>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useProjectTemplateStore } from '@/stores/useProjectTemplateStore';
import draggable from 'vuedraggable';

const store = useProjectTemplateStore();
</script>

<style scoped>
.ghost-task {
  opacity: 0.5;
  background-color: #f3f4f6; /* Tailwind gray-100 */
  border: 1px dashed #9ca3af; /* Tailwind gray-400 */
}
</style>
