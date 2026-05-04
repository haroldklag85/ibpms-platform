<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const isGlobalError = ref(false)
const globalErrorData = ref<{code: number|string, message: string} | null>(null)

const isOptimisticLock = ref(false)

const handleGlobalError = (event: Event) => {
    const customEvent = event as CustomEvent
    isGlobalError.value = true
    globalErrorData.value = customEvent.detail
}

const handleOptimisticLock = () => {
    isOptimisticLock.value = true
}

const reloadApp = () => {
    window.location.reload()
}

onMounted(() => {
    window.addEventListener('global-error-dispatch', handleGlobalError)
    window.addEventListener('optimistic-lock-dispatch', handleOptimisticLock)
})

onUnmounted(() => {
    window.removeEventListener('global-error-dispatch', handleGlobalError)
    window.removeEventListener('optimistic-lock-dispatch', handleOptimisticLock)
})
</script>

<template>
  <Teleport to="body">
    <!-- Overlay Colapso del Servidor / Red -->
    <div v-if="isGlobalError" class="fixed inset-0 z-[99999] flex flex-col items-center justify-center bg-gray-900/95 backdrop-blur-sm text-white font-sans">
      <span class="material-symbols-outlined text-[80px] text-red-500 mb-6 animate-pulse">warning</span>
      <h1 class="text-3xl font-bold text-red-500 mb-4">ALERTA DEL SISTEMA: NIVEL 0</h1>
      <p class="text-lg text-gray-200 max-w-2xl text-center mb-2">
        {{ globalErrorData?.message || t('errors.fatalServer') }}
      </p>
      <p class="text-sm font-mono text-gray-400 mb-8 mt-2 bg-black/50 px-4 py-2 rounded">
        Código de Error: {{ globalErrorData?.code || 'Desconocido' }}
      </p>
      <button @click="reloadApp" class="px-6 py-3 bg-red-600 hover:bg-red-700 text-white font-bold rounded-lg shadow-[0_0_15px_rgba(220,38,38,0.5)] transition-all transform hover:scale-105 flex items-center">
        <span class="material-symbols-outlined mr-2">refresh</span> REINICIAR CONTEXTO
      </button>
    </div>

    <!-- Overlay Concurrencia Optimista (CA-3) -->
    <div v-if="isOptimisticLock && !isGlobalError" class="fixed inset-0 z-[99999] flex flex-col items-center justify-center bg-amber-900/90 backdrop-blur-sm text-white font-sans">
      <span class="material-symbols-outlined text-[80px] text-amber-400 mb-6 animate-spin-slow">sync_problem</span>
      <h1 class="text-3xl font-bold text-amber-400 mb-4">CONFLICTO DE CONCURRENCIA</h1>
      <p class="text-lg text-gray-200 max-w-2xl text-center mb-8 px-4 leading-relaxed">
        <strong>Datos Oxidados:</strong> El registro que intentabas guardar ha sido modificado simultáneamente por otro operador en la red. <br/><br/>
        Para prevenir corrupción transaccional, tu operación fue rechazada.
      </p>
      <button @click="reloadApp" class="px-6 py-3 bg-amber-500 hover:bg-amber-600 text-black font-bold rounded-lg shadow-lg transition-all transform hover:scale-105 flex items-center">
        <span class="material-symbols-outlined mr-2">sync</span> SINCRONIZAR DATOS
      </button>
    </div>
  </Teleport>
</template>

<style scoped>
.animate-spin-slow {
  animation: spin 3s linear infinite;
}
</style>
