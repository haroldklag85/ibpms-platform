<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 bg-[url('https://www.transparenttextures.com/patterns/clean-gray-paper.png')]">
    <div class="max-w-md w-full bg-white rounded-2xl shadow-[0_20px_50px_rgba(8,_112,_184,_0.07)] p-8 border border-gray-100 relative overflow-hidden">
      
      <!-- Decoración Top -->
      <div class="absolute top-0 left-0 w-full h-2 bg-gradient-to-r from-blue-600 to-indigo-600"></div>

      <div class="text-center mb-10">
        <h2 class="text-3xl font-black text-gray-800 tracking-tight flex items-center justify-center gap-2">
           <span class="text-blue-600">🏢</span> Docketing
        </h2>
        <p class="text-gray-500 mt-2 text-sm font-medium">Plataforma iBPMS Corporativa</p>
      </div>

      <!-- VISTA 1: SSO FEDERADO (Flujo Normal) -->
      <div v-if="!isBreakGlass" class="space-y-6">
         <button 
            @click="triggerAzureSSO" 
            class="w-full flex items-center justify-center gap-3 py-3.5 px-4 border border-gray-300 rounded-xl shadow-sm text-sm font-bold text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all hover:shadow-md"
         >
            <svg class="w-5 h-5" viewBox="0 0 21 21" xmlns="http://www.w3.org/2000/svg">
                <path d="M10 0v10H0V0h10zm11 0v10H11V0h10zM10 11v10H0V11h10zm11 0v10H11V11h10z" fill="#00a4ef"/>
                <path d="M10 0v10H0V0h10z" fill="#f25022"/>
                <path d="M21 0v10H11V0h10z" fill="#7fba00"/>
                <path d="M10 11v10H0V11h10z" fill="#00a4ef"/>
                <path d="M21 11v10H11V11h10z" fill="#ffb900"/>
            </svg>
            Ingresar con Microsoft Entra ID
         </button>

         <div class="relative py-4">
            <div class="absolute inset-0 flex items-center">
               <div class="w-full border-t border-gray-200"></div>
            </div>
            <div class="relative flex justify-center text-xs">
               <span class="px-2 bg-white text-gray-400 uppercase tracking-widest font-bold">Opciones de Acceso</span>
            </div>
         </div>

         <div class="text-center mt-2 mb-6 p-3 bg-blue-50/50 rounded-lg border border-blue-100/50">
             <p class="text-[11px] text-blue-800 font-medium">
                ¿Problemas de federación o Contraseña Olvidada? <br/>
                <span class="font-bold">Contacte a Mesa de Ayuda IT del Grupo Corporativo.</span>
             </p>
         </div>

         <div class="text-center">
             <button data-testid="break-glass-toggle" @click="enableBreakGlass" class="text-[10px] font-bold text-gray-400 hover:text-red-600 transition tracking-widest uppercase underline decoration-gray-300 hover:decoration-red-300 underline-offset-4">
                 ⚠️ Break-Glass Recovery (IT Only)
             </button>
         </div>
      </div>

      <!-- VISTA 2: BREAK-GLASS LOGIN (CA-4 - Emergencia Local) -->
      <div v-else class="space-y-6 animate-fade-in">
         <div class="bg-red-50 border border-red-100 rounded-lg p-3 text-center mb-6">
             <span class="text-red-600 font-bold text-[10px] uppercase tracking-widest flex items-center justify-center gap-1">
                 <span class="material-symbols-outlined text-[14px]">emergency</span> Modo Break-Glass Activo
             </span>
             <p class="text-xs text-red-500 mt-1 font-medium leading-tight">Uso exclusivo para fallos de Federación SAML/OIDC. Su IP será auditada.</p>
         </div>

         <!-- Banner de Error Diferenciado (Break-Glass) -->
         <div 
             v-if="loginError" 
             data-testid="login-error-banner"
             class="rounded-lg p-3 text-center mb-4 border animate-fade-in"
             :class="{
                 'bg-amber-50 border-amber-200': loginError.code === 'USER_NOT_FOUND',
                 'bg-red-50 border-red-200': loginError.code === 'INVALID_PASSWORD',
                 'bg-gray-100 border-gray-300': loginError.code === 'ACCOUNT_DISABLED',
                 'bg-yellow-50 border-yellow-200': loginError.code === 'MISSING_FIELDS',
                 'bg-red-100 border-red-300': loginError.code === 'UNKNOWN'
             }"
         >
             <div class="flex items-center justify-center gap-2">
                 <span class="material-symbols-outlined text-[18px]"
                     :class="{
                         'text-amber-600': loginError.code === 'USER_NOT_FOUND',
                         'text-red-600': loginError.code === 'INVALID_PASSWORD' || loginError.code === 'UNKNOWN',
                         'text-gray-600': loginError.code === 'ACCOUNT_DISABLED',
                         'text-yellow-600': loginError.code === 'MISSING_FIELDS'
                     }"
                 >
                     {{ loginError.code === 'USER_NOT_FOUND' ? 'person_off' : 
                        loginError.code === 'INVALID_PASSWORD' ? 'lock' : 
                        loginError.code === 'ACCOUNT_DISABLED' ? 'block' : 
                        'error' }}
                 </span>
                 <p class="text-sm font-semibold"
                     :class="{
                         'text-amber-800': loginError.code === 'USER_NOT_FOUND',
                         'text-red-800': loginError.code === 'INVALID_PASSWORD' || loginError.code === 'UNKNOWN',
                         'text-gray-800': loginError.code === 'ACCOUNT_DISABLED',
                         'text-yellow-800': loginError.code === 'MISSING_FIELDS'
                     }"
                 >
                     {{ loginError.message }}
                 </p>
             </div>
         </div>

         <form @submit.prevent="handleEmergencyLogin" class="space-y-5">
            <div>
              <label class="block text-xs font-bold uppercase tracking-wider text-gray-600 mb-1">Usuario Táctico</label>
              <input 
                data-testid="email-input"
                v-model="email" 
                type="email" 
                required 
                placeholder="admin.local@empresa.com"
                class="block w-full px-3 py-2.5 bg-gray-50 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500 sm:text-sm font-medium transition"
              >
            </div>

            <div>
              <label class="block text-xs font-bold uppercase tracking-wider text-gray-600 mb-1">Contraseña Bóveda</label>
              <input 
                data-testid="password-input"
                v-model="password" 
                type="password" 
                required
                class="block w-full px-3 py-2.5 bg-gray-50 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500 sm:text-sm font-medium transition"
              >
            </div>

            <div class="pt-2">
              <button 
                data-testid="login-submit"
                type="submit" 
                class="w-full flex items-center justify-center gap-2 py-3 px-4 border border-transparent rounded-xl shadow-md text-sm font-bold text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 transition-all"
              >
                <span class="material-symbols-outlined text-[18px]">verified_user</span> Forzar Acceso Local
              </button>
            </div>
            
            <div class="text-center pt-2">
                <button type="button" @click="disableBreakGlass" class="text-[11px] font-bold text-gray-500 hover:text-blue-600 transition uppercase tracking-wider">
                    ← Volver al SSO Corporativo
                </button>
            </div>
         </form>
      </div>
    </div>



  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import apiClient from '@/services/apiClient';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

// UI States
const isBreakGlass = ref(false);
const email = ref('');
const password = ref('');
const loginError = ref<{ code: string; message: string } | null>(null);

// ===============================================
// INICIALIZACIÓN (Comprobación de Rutas de Emergencia)
// ===============================================
onMounted(() => {
    if (route.query.emergency === 'true') {
        isBreakGlass.value = true;
    }
});

const enableBreakGlass = () => {
    isBreakGlass.value = true;
    router.replace({ query: { emergency: 'true' } });
};

const disableBreakGlass = () => {
    isBreakGlass.value = false;
    loginError.value = null;
    router.replace({ query: {} });
    email.value = '';
    password.value = '';
};

// ===============================================
// VISTA 1: FLUJO AZURE SSO (OIDC / SAML) CA-3
// ===============================================
const triggerAzureSSO = async () => {
    // @Traceability: US-036 - CA-11 Respeto ciego al Autenticador Perimetral (EntraID MFA)
    console.log('Redirigiendo a https://login.microsoftonline.com/ o endpoint interno...');
    window.location.href = '/api/v1/auth/sso/azure';
};

// ===============================================
// VISTA 2: BREAK-GLASS EMERGENCY LOGIN CA-4
// ===============================================
const handleEmergencyLogin = async () => {
    loginError.value = null; // Limpiar error previo
    try {
        console.log(`[BREAK-GLASS] Forzando POST /auth/emergency-login para ${email.value}`);
        const response = await apiClient.post('/auth/emergency-login', { 
            email: email.value, 
            password: password.value 
        });
        const { token } = response.data;
        authStore.login(token);
        router.push('/workdesk');
    } catch (e: any) {
        const responseData = e?.response?.data;
        const code = responseData?.code || 'UNKNOWN';
        const message = responseData?.message;

        switch (code) {
            case 'USER_NOT_FOUND':
                loginError.value = {
                    code,
                    message: message || 'No existe una cuenta asociada al correo proporcionado.'
                };
                break;
            case 'INVALID_PASSWORD':
                loginError.value = {
                    code,
                    message: message || 'La contraseña proporcionada es incorrecta.'
                };
                break;
            case 'ACCOUNT_DISABLED':
                loginError.value = {
                    code,
                    message: message || 'La cuenta se encuentra deshabilitada. Contacte al administrador.'
                };
                break;
            case 'MISSING_FIELDS':
                loginError.value = {
                    code,
                    message: message || 'Debe ingresar correo y contraseña.'
                };
                break;
            default:
                loginError.value = {
                    code: 'UNKNOWN',
                    message: 'Error de conexión con el servidor. Verifique que el backend esté activo.'
                };
        }
    }
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0');
.animate-fade-in { animation: fadeIn 0.4s ease-out forwards; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(5px); } to { opacity: 1; transform: translateY(0); } }
</style>
