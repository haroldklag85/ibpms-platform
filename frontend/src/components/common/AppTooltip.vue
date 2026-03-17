<template>
  <div class="relative inline-flex items-center group/tooltip justify-center">
    <!-- Icon Selector -->
    <span 
      class="inline-flex cursor-help text-[11px] font-bold items-center justify-center w-[14px] h-[14px] rounded-full transition-colors duration-200"
      :class="[
        isError ? 'bg-red-100 text-red-600 border border-red-500' : 'bg-indigo-100 text-indigo-500 hover:bg-indigo-200'
      ]"
    >
      ?
    </span>
    
    <!-- Floating Tooltip Box -->
    <div 
       class="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 w-64 p-3 rounded-lg shadow-xl opacity-0 invisible group-hover/tooltip:opacity-100 group-hover/tooltip:visible transition-all duration-200 z-[999] text-left border"
       :class="[
         isError ? 'bg-red-50 border-red-300' : 'bg-gray-800 border-gray-700'
       ]"
    >
        <div 
          class="text-[11px] leading-relaxed font-normal whitespace-normal break-words"
          :class="[
            isError ? 'text-red-900' : 'text-gray-200'
          ]"
          v-html="content"
        >
        </div>
        
        <!-- Flecha Inferior (Tail) -->
        <div 
           class="absolute top-full left-1/2 -translate-x-1/2 w-0 h-0 border-l-[6px] border-l-transparent border-r-[6px] border-r-transparent border-t-[6px]"
           :class="[
             isError ? 'border-t-red-50' : 'border-t-gray-800'
           ]"
        ></div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * AppTooltip.vue
 * V1 Core UI Component (US-003, US-005)
 * Tooltip didáctico interactivo para Onboarding Embebido, con inyección de HTML seguro y estado reactivo de peligro (Red Alert).
 */

const props = defineProps<{
  content: string;
  isError?: boolean;
}>();
</script>
