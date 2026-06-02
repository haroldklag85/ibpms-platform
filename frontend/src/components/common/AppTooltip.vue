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
           class="absolute w-0 h-0"
           :style="arrowStyle"
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

// @Traceability: US-005, CA-05
const isHovered = ref(false);
const triggerRef = ref<HTMLElement | null>(null);
const tooltipStyle = ref({ top: '0px', left: '0px', transform: 'translate(-50%, -100%)' });
const arrowStyle = ref<Record<string, string>>({});

const show = () => {
  if (triggerRef.value) {
    const rect = triggerRef.value.getBoundingClientRect();
    const scrollY = window.scrollY;
    const scrollX = window.scrollX;
    const viewportWidth = typeof window !== 'undefined' ? window.innerWidth : 1200;
    
    // Default centering
    let shiftPercent = 50;
    const tooltipWidth = 256; // Matches w-64
    const halfWidth = tooltipWidth / 2;
    const triggerCenter = rect.left + rect.width / 2;

    // Check right overflow
    if (triggerCenter + halfWidth > viewportWidth - 10) {
      const availableSpaceOnRight = viewportWidth - 10 - triggerCenter;
      shiftPercent = Math.max(10, Math.min(90, 100 - (availableSpaceOnRight / tooltipWidth) * 100));
    }
    // Check left overflow
    else if (triggerCenter - halfWidth < 10) {
      const availableSpaceOnLeft = triggerCenter - 10;
      shiftPercent = Math.max(10, Math.min(90, (availableSpaceOnLeft / tooltipWidth) * 100));
    }

    tooltipStyle.value = {
      top: `${rect.top - 10 + scrollY}px`,
      left: `${triggerCenter + scrollX}px`,
      transform: `translate(-${shiftPercent}%, -100%)`
    };

    arrowStyle.value = {
      top: '100%',
      left: `${shiftPercent}%`,
      transform: 'translateX(-50%)',
      borderLeft: '6px solid transparent',
      borderRight: '6px solid transparent',
      borderTop: `6px solid ${props.isError ? '#fef2f2' : '#1f2937'}`
    };
  }
  isHovered.value = true;
};

const hide = () => {
  isHovered.value = false;
};
</script>
