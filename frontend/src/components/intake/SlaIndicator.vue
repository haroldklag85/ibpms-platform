<!-- @Traceability: Remediación Deuda Técnica - CA-11 / ADR-006 (Pinia Centralizado) -->
<template>
  <div class="inline-flex items-center space-x-2 px-2 py-1 rounded text-xs font-semibold" :class="colorClasses">
    <span class="w-2 h-2 rounded-full animate-pulse" :class="dotClasses"></span>
    <span>{{ label }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useTimeStore } from '@/stores/timeStore';
import { useSlaEngine, UrgencyType } from '@/composables/useSlaEngine';

const props = defineProps<{
  creationDate: string;
  slaDeadline: string;
}>();

const timeStore = useTimeStore();

const { calculateUrgency } = useSlaEngine();

const urgency = computed(() => {
  return calculateUrgency(props.slaDeadline, props.creationDate, timeStore.currentTick);
});

const colorClasses = computed(() => {
  switch (urgency.value) {
    case UrgencyType.BLACK: return 'bg-gray-800 text-white';
    case UrgencyType.RED: return 'bg-red-100 text-red-800';
    case UrgencyType.ORANGE: return 'bg-orange-100 text-orange-800';
    case UrgencyType.YELLOW: return 'bg-yellow-100 text-yellow-800';
    case UrgencyType.GREEN: return 'bg-green-100 text-green-800';
    default: return 'bg-gray-100 text-gray-800';
  }
});

const dotClasses = computed(() => {
  switch (urgency.value) {
    case UrgencyType.BLACK: return 'bg-gray-400';
    case UrgencyType.RED: return 'bg-red-500';
    case UrgencyType.ORANGE: return 'bg-orange-500';
    case UrgencyType.YELLOW: return 'bg-yellow-500';
    case UrgencyType.GREEN: return 'bg-green-500';
    default: return 'bg-gray-400';
  }
});

const label = computed(() => {
  switch (urgency.value) {
    case UrgencyType.BLACK: return 'Breached';
    case UrgencyType.RED: return 'Critical (< 15%)';
    case UrgencyType.ORANGE: return 'Warning (< 30%)';
    case UrgencyType.YELLOW: return 'At Risk (< 70%)';
    case UrgencyType.GREEN: return 'On Track';
    default: return 'Unknown';
  }
});

</script>
