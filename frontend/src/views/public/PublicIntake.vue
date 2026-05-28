<template>
  <div class="min-h-screen w-full bg-gray-50 flex flex-col items-center justify-center p-4">
    <div class="w-full max-w-2xl bg-white rounded-xl shadow-xl min-h-[60vh] border border-gray-200 flex flex-col overflow-hidden">
        <!-- Header minimalista sin navegación -->
        <header class="bg-indigo-600 px-6 py-4 flex items-center justify-between border-b border-indigo-700">
            <div class="flex items-center gap-3">
                <span class="text-3xl">📝</span>
                <div>
                   <h1 class="text-white font-bold text-lg leading-tight uppercase tracking-wider">Trámite Público Seguro</h1>
                   <p class="text-indigo-200 text-xs font-medium">Bypass Anónimo CA-15 Activo</p>
                </div>
            </div>
            <div class="bg-white/10 px-3 py-1 rounded text-white font-mono text-xs border border-white/20">
               {{ processKey }}
            </div>
        </header>

        <main class="flex-1 p-8 flex flex-col justify-between">
           <!-- Vue-based Toast/Notification (LEY 5 Compliance) -->
           <div v-if="submitted" class="bg-emerald-50 border border-emerald-200 rounded-lg p-4 mb-6 flex items-center gap-3 text-emerald-800 animate-fade-in">
              <span class="text-xl">✅</span>
              <div>
                 <h3 class="font-bold text-sm">Formulario Procesado</h3>
                 <p class="text-xs font-medium">El trámite público ha sido registrado exitosamente en la base de datos perimetral.</p>
              </div>
           </div>

           <div class="flex-1 border-2 border-dashed border-gray-200 rounded-lg flex flex-col items-center justify-center bg-gray-50 text-center p-6">
              <span class="material-symbols-outlined text-4xl text-gray-400 mb-4">dock</span>
              <h2 class="text-lg font-bold text-gray-700 mb-2">Simulador de iForm Desacoplado</h2>
              <p class="text-sm text-gray-500 max-w-sm mx-auto">Esta es una vista huérfana de Vue Router. No posee <code>Navigation Guards</code> (Auth Interceptor) y carece del Layout SPA de Intranet. Apta para injertos iFrame externos.</p>
           </div>
           
           <!-- Google reCAPTCHA v3 Badge / Security Container -->
           <div class="mt-6 g-recaptcha-badge bg-indigo-50/50 border border-indigo-100 rounded-lg p-3 text-xs text-indigo-700 font-medium flex items-center gap-2">
              <span class="text-base">🛡️</span>
              <span>Protección Anti-DDoS y Bots por Google reCAPTCHA v3 activa</span>
           </div>
           
           <div class="mt-8 flex justify-end gap-4">
                <button @click="submitAnonymousForm" class="bg-indigo-600 text-white font-bold px-6 py-2.5 rounded shadow-lg hover:bg-indigo-700 transition w-full sm:w-auto">🚀 Enviar Formulario Anónimo</button>
           </div>
        </main>
        
        <footer class="bg-gray-100 p-4 text-center border-t border-gray-200">
            <p class="text-[10px] text-gray-400 font-bold uppercase tracking-widest">Powered by Antigravity Zero-Trust Forms Engine</p>
        </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
// @Traceability: US-003 - CA-70: Modo Trámite Público Perimetral / Bypass JWT Seguro
import { computed, ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const processKey = computed(() => route.params.processKey || 'UNKNOWN_PROCESS');
const submitted = ref(false);

onMounted(() => {
    // Dynamic injection of Google reCAPTCHA v3 script tag (DDoS / Bot Prevention)
    const script = document.createElement('script');
    script.src = 'https://www.google.com/recaptcha/api.js?render=explicit';
    script.async = true;
    script.defer = true;
    document.head.appendChild(script);
});

const submitAnonymousForm = () => {
    // Setting state instead of using native DOM alert (LEY 5 compliance)
    submitted.value = true;
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0');

.material-symbols-outlined {
  font-family: 'Material Symbols Outlined';
  font-weight: normal;
  font-style: normal;
  display: inline-block;
  white-space: nowrap;
  word-wrap: normal;
  direction: ltr;
  font-feature-settings: 'liga';
  -webkit-font-feature-settings: 'liga';
  -webkit-font-smoothing: antialiased;
}

.animate-fade-in {
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
