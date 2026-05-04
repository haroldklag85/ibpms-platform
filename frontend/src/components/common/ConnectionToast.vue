<template>
  <transition name="fade-slide">
    <div 
      v-if="store.isVisible" 
      class="connection-toast fixed bottom-6 left-6 z-[9990] max-w-[320px] pointer-events-auto rounded-lg shadow-lg border p-4 flex items-center space-x-3 transition-colors duration-300"
      :class="store.currentColor"
    >
      <span class="material-symbols-outlined text-2xl flex-shrink-0" :class="{ 'animate-spin': store.status === 'RECONNECTING' }">
        {{ store.currentIcon }}
      </span>
      <div class="flex flex-col">
        <span class="text-sm font-semibold tracking-wide">
          <span v-if="store.status === 'RECONNECTING'">{{ t('errors.reconnect') }}</span>
          <span v-else>{{ store.currentLabel }}</span>
        </span>
        <span v-if="store.status === 'DEGRADED'" class="text-xs mt-1 opacity-80">
          Sus datos están seguros.
        </span>
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { useConnectionStore } from '@/stores/connectionStore';
import { watch } from 'vue';
import { useI18n } from 'vue-i18n';

const store = useConnectionStore();
const { t } = useI18n();

watch(() => store.status, (newStatus) => {
  if (newStatus === 'RESTORED') {
    setTimeout(() => {
      store.setStatus('ONLINE');
    }, 3000);
  }
}, { immediate: true });
</script>

<style scoped>
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.5s ease;
}
.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>
