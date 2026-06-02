<script setup lang="ts">
import { RouterView } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import NotFound404 from '@/components/common/NotFound404.vue'
import SudoModal from '@/components/common/SudoModal.vue'
import ErrorStateGlobal from '@/components/common/ErrorStateGlobal.vue'
import ConnectionToast from '@/components/common/ConnectionToast.vue'
import IncompleteProfileModal from '@/components/common/IncompleteProfileModal.vue'
import SessionLockModal from '@/components/common/SessionLockModal.vue'
import { useConnectionStatus } from '@/composables/useConnectionStatus'
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { LocalStorageGarbageCollector } from '@/services/LocalStorageGarbageCollector'

const authStore = useAuthStore()
const router = useRouter()

useConnectionStatus()

const confirmLogout = () => {
  authStore.showLogoutConfirm = false
  authStore.logout()
  router.push('/login')
}

onMounted(() => {
  LocalStorageGarbageCollector.run()
  window.addEventListener('storage', (e) => {
      if (e.key === 'ibpms_token' && !e.newValue) {
          authStore.logout()
          router.push('/login')
      }
  })
})
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
  
  <!-- CA-33: Reconexión SSE -->
  <div v-if="authStore.isSSEDisconnected" class="h-screen w-screen bg-gray-900/90 flex flex-col items-center justify-center space-y-4 fixed inset-0 z-[9999]">
     <div class="animate-spin h-12 w-12 border-4 border-indigo-500 border-t-transparent rounded-full mb-4"></div>
     <div class="text-white font-bold text-xl tracking-widest uppercase">Reconectando con el servidor de seguridad...</div>
  </div>
  
  <!-- CA-3: Security by Obscurity 404 Fallback -->
  <NotFound404 v-else-if="authStore.isGlobal404" class="fixed inset-0 z-[9998]" />

  <!-- Vue Router Main Canvas -->
  <RouterView v-show="!authStore.isHydrating && !authStore.isGlobal404" />
  
  <!-- CA-11: Botón de Fuga Infranqueable (Cerrar Sesión Externo) -->
  <button 
      v-if="authStore.token && !authStore.isHydrating"
      @click="authStore.showLogoutConfirm = true" 
      class="fixed bottom-4 right-4 z-[10001] bg-[#1e1b4b] hover:bg-indigo-900 text-white rounded-full p-3 shadow-[0_0_15px_rgba(30,27,75,0.3)] flex items-center justify-center transition-all hover:scale-110 group"
      title="Fuga Incondicional (Terminar Sesión)"
  >
      <span class="material-symbols-outlined group-hover:animate-pulse">power_settings_new</span>
  </button>

  <!-- Modal de Confirmación de Cierre de Sesión Transversal -->
  <Transition name="fade">
    <div v-if="authStore.showLogoutConfirm" class="fixed inset-0 z-[10002] flex items-center justify-center bg-slate-900/60 backdrop-blur-sm p-4">
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md overflow-hidden border border-slate-100 flex flex-col p-6 animate-fade-in-up">
        <div class="flex items-center gap-3.5 mb-4 text-slate-800">
          <div class="w-10 h-10 rounded-full bg-amber-50 border border-amber-200 flex items-center justify-center text-amber-500 shrink-0">
            <span class="material-symbols-outlined text-[22px]">logout</span>
          </div>
          <div>
            <h3 class="text-base font-bold text-[#1e1b4b] text-left">¿Cerrar Sesión Activa?</h3>
            <p class="text-xs text-slate-400 font-medium mt-0.5 text-left">iBPMS Platform Security</p>
          </div>
        </div>
        <p class="text-xs text-slate-500 leading-relaxed mb-6 font-medium text-left">
          Estás a punto de terminar tu sesión de trabajo. Si tienes tareas en curso o formularios en proceso de edición sin guardar en la bandeja, perderás los cambios temporales.
        </p>
        <div class="flex items-center justify-end gap-3">
          <button 
            @click="authStore.showLogoutConfirm = false" 
            class="px-4 py-2 text-xs font-bold text-slate-500 hover:text-slate-700 hover:bg-slate-100 rounded-lg transition"
          >
            Cancelar
          </button>
          <button 
            @click="confirmLogout" 
            class="px-4 py-2 text-xs font-bold text-white bg-slate-800 hover:bg-slate-700 active:bg-slate-900 rounded-lg shadow transition"
          >
            Sí, Cerrar Sesión
          </button>
        </div>
      </div>
    </div>
  </Transition>

  <!-- CA-9: Transversal Sudo Modal -->
  <SudoModal />

  <!-- CA-1 & CA-3: Global Defensive UI State -->
  <ErrorStateGlobal />

  <!-- CA-20: Connection Toast -->
  <ConnectionToast />

  <!-- CA-3: Incomplete Profile JIT Modal -->
  <IncompleteProfileModal />

  <!-- CA-27: Glassmorphism Soft-Lock -->
  <SessionLockModal />
</template>
