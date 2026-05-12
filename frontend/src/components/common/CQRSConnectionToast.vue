<!-- @Traceability: US-017, CA-19, CA-20, CA-22 -->
<template>
  <div v-if="networkStore.isOffline || showSaving" class="fixed bottom-4 left-4 z-50 p-3 bg-gray-800 text-white rounded shadow pointer-events-none transition-opacity flex items-center gap-2">
    <span v-if="networkStore.isOffline" class="flex items-center gap-2 text-sm font-semibold">
      <span class="material-symbols-outlined text-red-500">wifi_off</span>
      Trabajando sin conexión
    </span>
    <span v-else-if="showSaving" class="flex items-center gap-2 text-sm font-semibold">
      <span class="material-symbols-outlined text-amber-500 animate-spin">refresh</span>
      Guardando...
    </span>
  </div>
</template>

<script setup lang="ts">
import { useNetworkStore } from '@/stores/networkStore';
import { ref, watch } from 'vue';

const networkStore = useNetworkStore();
const showSaving = ref(false);

let timeoutId: ReturnType<typeof setTimeout> | null = null;

watch(() => networkStore.isSaving, (newVal) => {
    if (newVal) {
        if (!timeoutId) {
            timeoutId = setTimeout(() => {
                showSaving.value = true;
            }, 5000);
        }
    } else {
        if (timeoutId) {
            clearTimeout(timeoutId);
            timeoutId = null;
        }
        showSaving.value = false;
    }
});
</script>
