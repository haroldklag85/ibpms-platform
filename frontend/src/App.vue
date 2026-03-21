<script setup lang="ts">
import { RouterView } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import NotFound404 from '@/components/common/NotFound404.vue'
import SudoModal from '@/components/common/SudoModal.vue'

const authStore = useAuthStore()
</script>

<template>
  <!-- CA-2: Skeleton Loader Transversal FOUC -->
  <div v-if="authStore.isHydrating" class="h-screen w-screen bg-slate-50 flex flex-col items-center justify-center space-y-4 fixed inset-0 z-[9999]">
     <div class="animate-pulse flex flex-col items-center">
         <div class="h-16 w-16 bg-indigo-200/50 rounded-full mb-6"></div>
         <div class="h-3 w-48 bg-gray-300 rounded-full mb-3"></div>
         <div class="h-2 w-32 bg-gray-200 rounded-full"></div>
         <div class="mt-8 text-xs font-bold tracking-widest text-indigo-400 uppercase">Validando Identidad y Permisos IAM...</div>
     </div>
  </div>
  
  <!-- CA-3: Security by Obscurity 404 Fallback -->
  <NotFound404 v-else-if="authStore.isGlobal404" class="fixed inset-0 z-[9998]" />

  <!-- Vue Router Main Canvas -->
  <RouterView v-show="!authStore.isHydrating && !authStore.isGlobal404" />
  
  <!-- CA-11: Botón de Fuga Infranqueable (Cerrar Sesión Externo) -->
  <button 
      v-if="authStore.token && !authStore.isHydrating"
      @click="authStore.logout" 
      class="fixed bottom-4 right-4 z-[10001] bg-red-600 hover:bg-red-700 text-white rounded-full p-3 shadow-[0_0_15px_rgba(220,38,38,0.5)] flex items-center justify-center transition-all hover:scale-110 group"
      title="Fuga Incondicional (Terminar Sesión)"
  >
      <span class="material-symbols-outlined group-hover:animate-pulse">power_settings_new</span>
  </button>

  <!-- CA-9: Transversal Sudo Modal -->
  <SudoModal />
</template>
