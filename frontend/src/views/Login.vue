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
             <button @click="enableBreakGlass" class="text-[10px] font-bold text-gray-400 hover:text-red-600 transition tracking-widest uppercase underline decoration-gray-300 hover:decoration-red-300 underline-offset-4">
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

         <form @submit.prevent="handleEmergencyLogin" class="space-y-5">
            <div>
              <label class="block text-xs font-bold uppercase tracking-wider text-gray-600 mb-1">Usuario Táctico</label>
              <input 
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
                v-model="password" 
                type="password" 
                required
                class="block w-full px-3 py-2.5 bg-gray-50 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500 sm:text-sm font-medium transition"
              >
            </div>

            <div class="pt-2">
              <button 
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

    <!-- MODAL JIT 428 (CA-3 - Perfil Incompleto) -->
    <Teleport to="body">
       <div v-if="showJitModal" class="fixed inset-0 bg-gray-900/90 flex flex-col items-center justify-center z-[500] p-4 backdrop-blur-md">
          <div class="bg-white rounded-2xl shadow-2xl p-8 max-w-md w-full border border-gray-200 flex flex-col items-center relative overflow-hidden">
             
             <div class="w-16 h-16 bg-amber-100 rounded-full flex items-center justify-center mb-5 border-4 border-amber-50">
                 <span class="material-symbols-outlined text-amber-500 text-3xl">how_to_reg</span>
             </div>

             <h2 class="text-2xl font-black text-gray-800 tracking-tight text-center mb-2">Completar Perfil</h2>
             <p class="text-sm text-gray-500 text-center mb-8 font-medium">Hemos recibido tus credenciales de Azure AD, pero tu cuenta carece de información obligatoria para operar la bandeja iBPMS.</p>

             <form @submit.prevent="submitJitProfile" class="w-full space-y-5">
                 <div v-if="missingClaims.includes('Sucursal_ID')">
                    <label class="block text-xs font-bold text-gray-700 uppercase tracking-wider mb-1">Sucursal de Origen <span class="text-red-500">*</span></label>
                    <select v-model="jitData.branchId" required class="w-full border-gray-300 rounded-lg shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm p-3 border font-medium text-gray-700 bg-gray-50">
                       <option value="" disabled>Seleccione Oficina Matriz...</option>
                       <option value="BOG_101">Bogotá Principal</option>
                       <option value="MED_201">Medellín Operaciones</option>
                       <option value="CAL_301">Cali Administrativa</option>
                    </select>
                 </div>

                 <div v-if="missingClaims.includes('Telefono')">
                    <label class="block text-xs font-bold text-gray-700 uppercase tracking-wider mb-1">Teléfono Corporativo <span class="text-red-500">*</span></label>
                    <input v-model="jitData.phone" type="tel" required placeholder="+57 320 000 0000" class="w-full border-gray-300 rounded-lg shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm p-3 border font-medium bg-gray-50" />
                 </div>

                 <div class="bg-blue-50 border border-blue-100 p-3 rounded-lg flex gap-3 items-start mt-6">
                     <span class="material-symbols-outlined text-blue-500 text-[18px] mt-0.5">info</span>
                     <p class="text-[11px] text-blue-800 font-medium leading-tight">Al completar estos datos, se insertarán en tu expediente de Identidad para futuras sesiones.</p>
                 </div>

                 <button type="submit" class="w-full bg-gray-900 text-white font-bold py-3.5 rounded-xl shadow-lg hover:bg-black transition uppercase tracking-widest text-sm mt-4 flex justify-center items-center gap-2">
                     Sincronizar Perfil y Continuar <span class="material-symbols-outlined text-[18px]">arrow_forward</span>
                 </button>
             </form>

          </div>
       </div>
    </Teleport>

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

// JIT CA-3 States
const showJitModal = ref(false);
const missingClaims = ref<string[]>([]);
const jitData = ref({ branchId: '', phone: '' });
const federatedTempToken = ref('');

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
    router.replace({ query: {} });
    email.value = '';
    password.value = '';
};

// ===============================================
// VISTA 1: FLUJO AZURE SSO (OIDC / SAML) CA-3
// ===============================================
const triggerAzureSSO = async () => {
    // Simulamos el Callback de Azure redirigiendo a nuestro Backend (/api/v1/auth/callback)
    console.log('[SSO EntraID] - Invocando Federation Flow...');
    
    // MOCK DEL BACKEND RESPONDIENDO HTTP 428 PRECONDITION REQUIRED
    // Usualmente esto se da si el AuthStore emite la advertencia post-redirección
    const isProfileIncomplete = true; // Forzamos el Fallback JIT para auditoría
    
    if (isProfileIncomplete) {
        console.warn('[HTTP 428 Interceptor] - Perfil incompleto detectado. Abriendo Guardrail JIT.');
        federatedTempToken.value = 'TEMP_JWT_123';
        missingClaims.value = ['Sucursal_ID', 'Telefono'];
        showJitModal.value = true;
    } else {
        // Flujo Feliz
        authStore.login('VALID_AZURE_JWT_999');
        router.push('/');
    }
};

const submitJitProfile = async () => {
    try {
        // Simulamos PUT /api/v1/auth/sync
        /* 
        await apiClient.put('/api/v1/auth/sync', { 
            tempToken: federatedTempToken.value, 
            claims: jitData.value 
        });
        */
        console.log('[JIT SYNC OK] - Datos inyectados: ', jitData.value);
        
        // El Servidor responde con un JWT validado y full-claims.
        showJitModal.value = false;
        authStore.login('VALID_AZURE_JWT_REPAIRED_999');
        router.push('/');
    } catch (e) {
        alert('Error sincronizando perfil.');
    }
};

// ===============================================
// VISTA 2: BREAK-GLASS EMERGENCY LOGIN CA-4
// ===============================================
const handleEmergencyLogin = async () => {
    try {
        console.log(`[BREAK-GLASS] Forzando POST /api/v1/auth/emergency-login para ${email.value}`);
        // Simulamos POST al endpoint secreto
        // const response = await apiClient.post('/api/v1/auth/emergency-login', { username: email.value, password: password.value });
        
        authStore.login('EMERGENCY_LOCAL_JWT_666');
        router.push('/');
    } catch (e) {
        alert('Credenciales de bóveda rechazadas o IP denegada.');
    }
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0');
.animate-fade-in { animation: fadeIn 0.4s ease-out forwards; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(5px); } to { opacity: 1; transform: translateY(0); } }
</style>
