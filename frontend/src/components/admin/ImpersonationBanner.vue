<template>
  <div
    v-if="authStore.isImpersonating"
    class="fixed top-0 left-0 right-0 z-[60] bg-amber-400 text-amber-900 h-10 flex items-center justify-between px-4 text-sm font-medium shadow-md"
    data-testid="impersonation-banner"
  >
    <div class="flex items-center gap-2">
      <span class="material-symbols-outlined text-lg">admin_panel_settings</span>
      <span>{{ t('impersonation.banner') }} [{{ authStore.impersonatedBy }}]</span>
      <span class="ml-4 font-mono font-bold bg-amber-500 px-2 py-0.5 rounded text-xs">
        {{ formattedTime }}
      </span>
    </div>
    <button
      @click="handleExit"
      class="bg-amber-600 hover:bg-amber-700 text-white px-3 py-1 rounded-lg text-xs flex items-center gap-1 transition-colors"
    >
      <span class="material-symbols-outlined text-[14px]">logout</span>
      {{ t('impersonation.exit') }}
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useAuthStore } from '@/stores/authStore';
import { useI18n } from 'vue-i18n';

const authStore = useAuthStore();
const { t } = useI18n();

const timeLeft = ref(0);
let interval: any;

const updateTime = () => {
  if (!authStore.impersonationExpiresAt) return;
  const now = Date.now();
  const diff = Math.max(0, Math.floor((authStore.impersonationExpiresAt - now) / 1000));
  timeLeft.value = diff;
  if (diff === 0 && authStore.isImpersonating) {
    handleExit();
  }
};

const formattedTime = computed(() => {
  const m = Math.floor(timeLeft.value / 60).toString().padStart(2, '0');
  const s = (timeLeft.value % 60).toString().padStart(2, '0');
  return `${m}:${s}`;
});

const handleExit = () => {
  authStore.exitImpersonation();
};

onMounted(() => {
  updateTime();
  interval = setInterval(updateTime, 1000);
});

onUnmounted(() => {
  clearInterval(interval);
});
</script>
