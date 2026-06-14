<template>
  <span 
    class="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full border transition-colors duration-300"
    :class="trafficColor"
    :title="expirationDate"
  >
    <span v-if="isExpired">⚠️ Expirado</span>
    <span v-else-if="isAtRisk">⏰ Riesgo</span>
    <span v-else>✅ A tiempo</span>
  </span>
</template>

<script setup lang="ts">
import { useSlaTrafficLight } from '@/composables/useSlaTrafficLight';

const props = defineProps<{
    expirationDate: string | null;
}>();

const { trafficColor, isAtRisk, isExpired } = useSlaTrafficLight(props.expirationDate);
</script>
