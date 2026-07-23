<template>
  <div
    v-if="isVisible"
    class="bg-red-600/95 text-white fixed bottom-4 right-4 z-50 rounded-xl shadow-2xl p-4 min-w-[360px]"
    data-testid="fatal-toast"
  >
    <div class="flex items-center gap-3">
      <span class="material-symbols-outlined text-2xl">error</span>
      <div class="flex flex-col">
        <span class="font-bold">{{ t('errors.fatalServer') }}</span>
        <span class="text-sm opacity-90 mt-1 break-all font-mono">TraceId: {{ traceId }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
const isVisible = ref(false);
const traceId = ref('');

const handleGlobalError = (event: Event) => {
  const customEvent = event as CustomEvent;
  if (customEvent.detail?.type === 'SERVER_ERROR') {
    isVisible.value = true;
    traceId.value = customEvent.detail.traceId || 'N/A';
  }
};

onMounted(() => {
  window.addEventListener('global-error-dispatch', handleGlobalError);
});

onUnmounted(() => {
  window.removeEventListener('global-error-dispatch', handleGlobalError);
});
</script>
