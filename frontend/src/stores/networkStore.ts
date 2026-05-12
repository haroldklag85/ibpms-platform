// @Traceability: US-017, CA-24
import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useNetworkStore = defineStore('network', () => {
  const isOffline = ref(!navigator.onLine);
  const isSaving = ref(false);
  
  window.addEventListener('offline', () => isOffline.value = true);
  window.addEventListener('online', () => {
     isOffline.value = false;
  });

  return { isOffline, isSaving };
});
