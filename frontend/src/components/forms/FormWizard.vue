<!-- @Traceability: US-029 - CA-22 - Componente inyectado para navegación Multi-Etapa (Wizard) -->
<template>
  <div class="form-wizard w-full mb-6">
    <!-- Progress Bar -->
    <div class="flex items-center justify-between mb-4">
      <div 
        v-for="(stage, index) in stages" 
        :key="stage"
        class="flex-1 flex flex-col items-center relative"
      >
        <div 
          class="w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold border-2 z-10 transition-colors"
          :class="{
            'bg-ibpms-brand border-ibpms-brand text-white': isCurrent(stage) || isPast(index),
            'bg-white border-gray-300 text-gray-500': !isCurrent(stage) && !isPast(index),
            'ring-2 ring-red-500': hasErrors(stage)
          }"
        >
          <span v-if="isPast(index) && !hasErrors(stage)">✓</span>
          <span v-else>{{ index + 1 }}</span>
        </div>
        <div class="text-xs mt-2 font-medium" :class="isCurrent(stage) || isPast(index) ? 'text-gray-800' : 'text-gray-400'">
          {{ stage }}
        </div>
        <!-- Progress Line -->
        <div 
          v-if="index < stages.length - 1"
          class="absolute top-4 left-1/2 w-full h-1 -z-0"
          :class="isPast(index) ? 'bg-ibpms-brand' : 'bg-gray-200'"
        ></div>
      </div>
    </div>

    <!-- Form Content (Fields) -->
    <div class="wizard-content my-6">
      <slot></slot>
    </div>

    <!-- Controls (Can be rendered inside footer via slot or emitted) -->
    <div class="wizard-controls flex justify-between mt-8 border-t pt-4">
      <button 
        type="button" 
        @click="prevStep" 
        :disabled="currentIndex === 0"
        class="px-4 py-2 border rounded-md text-gray-700 hover:bg-gray-100 font-medium disabled:opacity-50 transition"
      >
        Anterior
      </button>
      <button 
        type="button" 
        @click="nextStep" 
        v-if="currentIndex < stages.length - 1"
        class="px-4 py-2 bg-indigo-600 text-white rounded-md font-bold hover:bg-indigo-700 transition"
      >
        Siguiente
      </button>
      <!-- El botón de submit final lo controla el padre o un slot, para adherirse al DoD -->
      <slot name="submit-button" v-if="currentIndex === stages.length - 1"></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps({
  stages: {
    type: Array as () => string[],
    required: true
  },
  currentStage: {
    type: String,
    required: true
  },
  errorMap: {
    type: Object as () => Record<string, boolean>,
    default: () => ({})
  }
});

const emit = defineEmits(['prev-step', 'next-step']);

const currentIndex = computed(() => props.stages.indexOf(props.currentStage));

const isCurrent = (stage: string) => stage === props.currentStage;
const isPast = (index: number) => index < currentIndex.value;
const hasErrors = (stage: string) => !!props.errorMap[stage];

const prevStep = () => {
  if (currentIndex.value > 0) {
    emit('prev-step', props.stages[currentIndex.value - 1]);
  }
};

const nextStep = () => {
  if (currentIndex.value < props.stages.length - 1) {
    emit('next-step', props.stages[currentIndex.value + 1]);
  }
};
</script>
