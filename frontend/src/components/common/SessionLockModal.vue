<template>
  <div
    v-if="isVisible"
    class="fixed inset-0 z-[100] backdrop-blur-xl bg-black/40 flex items-center justify-center p-4"
    data-testid="session-lock-modal"
  >
    <div class="bg-white rounded-2xl shadow-2xl p-8 max-w-sm w-full text-center">
      <div class="flex justify-center mb-6">
        <!-- Logo iBPMS placeholder -->
        <div class="w-16 h-16 rounded-full bg-indigo-100 flex items-center justify-center">
          <span class="material-symbols-outlined text-indigo-600 text-3xl">lock</span>
        </div>
      </div>
      
      <h2 class="text-xl font-bold text-slate-800 mb-2">{{ t('errors.sessionExpired') }}</h2>
      <p class="text-sm text-slate-500 mb-6">
        {{ t('errors.sessionExpiredDesc') }}
      </p>

      <form @submit.prevent="handleReconnect">
        <div class="mb-4 text-left">
          <label class="block text-xs font-medium text-slate-600 mb-1">{{ t('common.password') }}</label>
          <input
            v-model="password"
            type="password"
            required
            class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none text-sm"
            placeholder="••••••••"
            data-testid="session-lock-password"
          />
          <span v-if="errorMsg" class="text-xs text-red-500 mt-1 block">{{ errorMsg }}</span>
        </div>
        
        <button
          type="submit"
          class="w-full py-2 bg-indigo-600 text-white rounded-lg font-medium hover:bg-indigo-700 transition-colors focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
          :disabled="isLoading"
          data-testid="session-lock-submit"
        >
          <span v-if="isLoading" class="material-symbols-outlined animate-spin align-middle text-sm">sync</span>
          <span v-else>{{ t('errors.reconnect') }}</span>
        </button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { useAuthStore } from '@/stores/authStore';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
const authStore = useAuthStore();
const isVisible = ref(false);
const password = ref('');
const isLoading = ref(false);
const errorMsg = ref('');

const handleGlobalError = (event: Event) => {
  const customEvent = event as CustomEvent;
  if (customEvent.detail?.type === 'SESSION_EXPIRED') {
    isVisible.value = true;
    password.value = '';
    errorMsg.value = '';
  }
};

onMounted(() => {
  window.addEventListener('global-error-dispatch', handleGlobalError);
});

onUnmounted(() => {
  window.removeEventListener('global-error-dispatch', handleGlobalError);
});

const handleReconnect = async () => {
  if (!password.value) return;
  
  isLoading.value = true;
  errorMsg.value = '';
  
  try {
    await authStore.hydrateAuth();
    isVisible.value = false;
  } catch (error) {
    authStore.logout();
    errorMsg.value = t('errors.invalidCredentials');
  } finally {
    isLoading.value = false;
  }
};
</script>
