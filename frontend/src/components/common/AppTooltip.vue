<template>
  <span 
    ref="triggerRef"
    class="relative inline-flex items-center justify-center cursor-help text-[11px] font-bold w-[14px] h-[14px] rounded-full transition-colors duration-200"
    :class="isError ? 'bg-red-100 text-red-600 border border-red-500' : 'bg-indigo-100 text-indigo-500 hover:bg-indigo-200'"
    @mouseenter="show"
    @mouseleave="hide"
  >
    ?
  </span>
  <Teleport to="body">
    <div 
       v-show="isHovered"
       :style="tooltipStyle"
       class="absolute z-[1000] w-64 p-3 rounded-lg shadow-2xl text-left border pointer-events-none transition-opacity duration-200"
       :class="isError ? 'bg-red-50 border-red-300' : 'bg-gray-800 border-gray-700'"
    >
        <div 
          class="text-[11px] leading-relaxed font-normal whitespace-normal break-words"
          :class="isError ? 'text-red-900' : 'text-gray-200'"
          v-html="content"
        >
        </div>
        <!-- Flecha Inferior (Tail) -->
        <div 
           class="absolute top-full left-1/2 -translate-x-1/2 w-0 h-0 border-l-[6px] border-l-transparent border-r-[6px] border-r-transparent border-t-[6px]"
           :class="isError ? 'border-t-red-50' : 'border-t-gray-800'"
        ></div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref } from 'vue';

/**
 * AppTooltip.vue
 * V1 Core UI Component (US-003, US-005)
 * Tooltip didáctico interactivo para Onboarding Embebido, con inyección de HTML seguro y estado reactivo de peligro (Red Alert).
 * CA-03: Utiliza Teleport para renderizar globalmente evadiendo z-indexes colapsados (Mónaco IDE).
 */

const props = defineProps<{
  content: string;
  isError?: boolean;
}>();

const isHovered = ref(false);
const triggerRef = ref<HTMLElement | null>(null);
const tooltipStyle = ref({ top: '0px', left: '0px', transform: 'translate(-50%, -100%)' });

const show = () => {
  if (triggerRef.value) {
    const rect = triggerRef.value.getBoundingClientRect();
    tooltipStyle.value = {
      top: `${rect.top - 10 + window.scrollY}px`,
      left: `${rect.left + rect.width / 2 + window.scrollX}px`,
      transform: 'translate(-50%, -100%)'
    };
  }
  isHovered.value = true;
};

const hide = () => {
  isHovered.value = false;
};
</script>
