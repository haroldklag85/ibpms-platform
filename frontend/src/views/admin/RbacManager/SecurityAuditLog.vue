<template>
  <div class="bg-gray-900 rounded-lg p-4 font-mono text-xs text-green-400 overflow-hidden flex flex-col h-32 mt-4 shrink-0 shadow-inner">
    <div class="text-gray-400 mb-2 font-sans text-[10px] tracking-widest uppercase flex justify-between">
      <span>Audit Trail Inmutable (Security Logs)</span>
      <span class="animate-pulse">Live Tailing</span>
    </div>
    <div class="overflow-y-auto space-y-1.5 pr-2 custom-scrollbar">
      <div v-for="(log, idx) in store.auditLogs" :key="idx" class="flex gap-3 items-start border-l border-green-800 pl-2">
        <span class="text-gray-500 shrink-0">[{{ log.timestamp }}]</span>
        <span class="leading-relaxed">{{ log.message }}</span>
      </div>
      <div class="flex gap-3 items-start border-l border-transparent pl-2 italic opacity-50">
        <span class="text-gray-500 shrink-0">~</span>
        <span>Esperando eventos de seguridad...</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRbacStore } from '@/stores/rbacStore'
const store = useRbacStore()
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: #111827; 
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #374151; 
  border-radius: 4px;
}
</style>
