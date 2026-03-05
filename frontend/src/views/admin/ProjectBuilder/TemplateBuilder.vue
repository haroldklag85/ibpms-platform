<template>
  <div class="h-full flex flex-col p-6 bg-gray-50 dark:bg-gray-900 overflow-hidden">
    
    <!-- Top Header & Actions -->
    <div class="flex flex-col md:flex-row items-center justify-between mb-6 space-y-4 md:space-y-0">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white flex items-center">
          <svg class="w-6 h-6 mr-2 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path></svg>
          Constructor de Plantillas (WBS)
        </h1>
        <p class="text-sm text-gray-500 dark:text-gray-400">
          {{ store.template?.name || 'Cargando...' }} 
          <span v-if="store.isPublished" class="ml-2 inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400">READ ONLY (PUBLISHED)</span>
        </p>
      </div>

      <div class="flex items-center space-x-3">
        <button 
          @click="saveTemplate"
          :disabled="store.isLoading || store.isPublished"
          class="inline-flex items-center px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 dark:bg-gray-800 dark:text-gray-300 dark:border-gray-600 transition-colors"
        >
          Guardar Draft
        </button>

        <!-- AC-1 UX Defensiva: Solo habilita si isPublishable (100% form_key) -->
        <button 
          @click="publishTemplate"
          :disabled="!store.isPublishable || store.isPublished || store.isLoading"
          :class="[
            'inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2 transition-colors',
            store.isPublishable && !store.isPublished 
              ? 'text-white bg-blue-600 hover:bg-blue-700 focus:ring-blue-500 trigger-pulse' 
              : 'bg-gray-300 text-gray-500 dark:bg-gray-700 dark:text-gray-400 cursor-not-allowed'
          ]"
        >
          <!-- Candado Abierto / Cerrado visual hint -->
          <svg v-if="!store.isPublishable" class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path></svg>
          <svg v-else class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 11V7a4 4 0 118 0m-4 8v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2z"></path></svg>
          {{ store.isPublished ? 'PUBLICADO' : '[ PUBLICAR PLANTILLA ]' }}
        </button>
      </div>
    </div>

    <!-- Error/Success Alerts -->
    <div v-if="successMsg" class="mb-4 bg-green-50 text-green-800 p-3 rounded-md text-sm border border-green-200 shadow-sm animate-pulse flex items-center">
      <svg class="h-5 w-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
      {{ successMsg }}
    </div>
    <div v-if="errorMsg" class="mb-4 bg-red-50 text-red-800 p-3 rounded-md text-sm border border-red-200 flex items-center">
       <svg class="h-5 w-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
      {{ errorMsg }}
    </div>

    <!-- Split Screen Workspace -->
    <div class="flex-1 flex flex-col md:flex-row gap-6 overflow-hidden">
      
      <!-- Left: WBS Tree View -->
      <div class="w-full md:w-2/3 bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 overflow-y-auto flex flex-col">
        <div class="px-4 py-3 bg-gray-50 dark:bg-gray-800/80 border-b border-gray-200 dark:border-gray-700 sticky top-0 z-10 flex justify-between items-center">
          <span class="font-semibold text-gray-700 dark:text-gray-300">Work Breakdown Structure (WBS)</span>
          <span v-if="!store.isPublishable && !store.isPublished" class="text-xs text-amber-600 dark:text-amber-400 font-medium">⚠️ Faltan Form Keys</span>
        </div>
        <div class="p-4 flex-1">
          <WbsTreeView v-if="store.template" />
          <div v-else class="flex h-full items-center justify-center text-gray-400">
             Cargando topología...
          </div>
        </div>
      </div>

      <!-- Right: Property Inspector -->
      <div class="w-full md:w-1/3 bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 overflow-y-auto flex flex-col">
        <div class="px-4 py-3 bg-gray-50 dark:bg-gray-800/80 border-b border-gray-200 dark:border-gray-700 sticky top-0 z-10">
          <span class="font-semibold text-gray-700 dark:text-gray-300">Inspector de Propiedades</span>
        </div>
        <div class="p-4 flex-1 bg-gray-50/50 dark:bg-gray-900/50">
          <PropertyInspector />
        </div>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useProjectTemplateStore } from '@/stores/useProjectTemplateStore';
import WbsTreeView from './WbsTreeView.vue';
import PropertyInspector from './PropertyInspector.vue';

const store = useProjectTemplateStore();

const successMsg = ref('');
const errorMsg = ref('');

const TEMPLATE_ID_MOCK = 'tpl-001';

onMounted(async () => {
    await store.loadTemplate(TEMPLATE_ID_MOCK);
    
    // Auto-select first task if exists
    if (store.template?.phases?.[0]?.milestones?.[0]?.tasks?.[0]) {
       store.selectTask(store.template.phases[0].milestones[0].tasks[0].id);
    }
});

const saveTemplate = async () => {
    errorMsg.value = '';
    try {
        await store.deepSave();
        successMsg.value = 'Draft guardado (Deep Save Topológico) exitosamente.';
        setTimeout(() => successMsg.value = '', 4000);
    } catch(e) {
        errorMsg.value = 'Fallo la validación TopSort del Backend. ¿Dependencia circular?';
    }
};

const publishTemplate = async () => {
    errorMsg.value = '';
    try {
        await store.publishTemplate();
        successMsg.value = '¡Plantilla COMPILADA y PUBLICADA! Lista para P9.';
        setTimeout(() => successMsg.value = '', 6000);
    } catch(e: any) {
        errorMsg.value = e.response?.data?.message || 'Error de Integridad al Publicar. Revisa Form Keys.';
    }
};
</script>

<style scoped>
.trigger-pulse {
  animation: safe-pulse 2s infinite;
}
@keyframes safe-pulse {
  0% { box-shadow: 0 0 0 0 rgba(37, 99, 235, 0.4); }
  70% { box-shadow: 0 0 0 6px rgba(37, 99, 235, 0); }
  100% { box-shadow: 0 0 0 0 rgba(37, 99, 235, 0); }
}
</style>
