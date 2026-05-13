<!-- // @Traceability: US-002, CA-22 -->
<template>
  <div class="flex border-b border-gray-200 mb-4 bg-white px-2">
    <button 
      class="py-3 px-6 transition-colors font-semibold text-sm outline-none focus:outline-none flex items-center gap-2"
      :class="workdeskStore.activeView === 'PERSONAL' ? 'border-b-[3px] border-blue-600 text-blue-600' : 'text-gray-500 hover:text-gray-800 hover:border-b-[3px] hover:border-gray-300 border-b-[3px] border-transparent'"
      @click="workdeskStore.setActiveView('PERSONAL')"
    >
      💼 Mi Bandeja 
      <span class="bg-blue-100 text-blue-800 text-xs font-medium px-2 py-0.5 rounded-full">{{ workdeskStore.personalTaskCount || 0 }}</span>
    </button>
    <button 
      class="py-3 px-6 transition-colors font-semibold text-sm outline-none focus:outline-none flex items-center gap-2"
      :class="workdeskStore.activeView === 'POOL' ? 'border-b-[3px] border-indigo-600 text-indigo-600' : 'text-gray-500 hover:text-gray-800 hover:border-b-[3px] hover:border-gray-300 border-b-[3px] border-transparent'"
      @click="workdeskStore.setActiveView('POOL')"
    >
      👥 Tareas de Equipo 
      <span class="bg-indigo-100 text-indigo-800 text-xs font-medium px-2 py-0.5 rounded-full">{{ workdeskStore.poolTaskCount || 0 }}</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import { useWorkdeskStore } from '@/stores/useWorkdeskStore';
const workdeskStore = useWorkdeskStore();
</script>
