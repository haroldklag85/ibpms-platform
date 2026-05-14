<template>
  <div class="bg-red-50/50 border border-red-200 rounded-2xl p-6 shadow-sm">
    <div class="flex items-center gap-3 mb-6">
      <div class="w-10 h-10 bg-red-600 rounded-lg flex items-center justify-center text-white shadow-lg shadow-red-200">
        <span class="material-symbols-outlined">emergency_home</span>
      </div>
      <div>
        <h3 class="text-lg font-black text-red-900 leading-none">Acceso Break-Glass</h3>
        <p class="text-[11px] text-red-600 font-bold uppercase tracking-widest mt-1">Protocolo de Emergencia Local</p>
      </div>
    </div>

    <form @submit.prevent="handleEmergencyLogin" class="space-y-4">
      <div>
        <label class="block text-[10px] font-black text-red-800 uppercase tracking-widest mb-1.5 ml-1">Email Administrador</label>
        <div class="relative">
          <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-red-400 text-[20px]">person</span>
          <input 
            v-model="emergencyForm.email"
            type="email" 
            required 
            placeholder="admin@local"
            class="w-full bg-white border-2 border-red-100 rounded-xl py-3 pl-10 pr-4 text-sm font-bold text-gray-900 focus:border-red-500 focus:ring-0 transition-all placeholder:text-red-200"
          />
        </div>
      </div>

      <div>
        <label class="block text-[10px] font-black text-red-800 uppercase tracking-widest mb-1.5 ml-1">Clave de Seguridad</label>
        <div class="relative">
          <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-red-400 text-[20px]">key</span>
          <input 
            v-model="emergencyForm.password"
            type="password" 
            required 
            placeholder="••••••••"
            class="w-full bg-white border-2 border-red-100 rounded-xl py-3 pl-10 pr-4 text-sm font-bold text-gray-900 focus:border-red-500 focus:ring-0 transition-all placeholder:text-red-200"
          />
        </div>
      </div>

      <div class="pt-2">
        <label class="block text-[10px] font-black text-red-800 uppercase tracking-widest mb-1.5 ml-1">Justificación del Incidente <span class="text-red-600">*</span></label>
        <textarea 
          v-model="emergencyForm.justification"
          required 
          rows="3"
          placeholder="Describa el motivo de la activación del protocolo (Ej: Caída masiva de EntraID / Redis Outage)..."
          class="w-full bg-white border-2 border-red-100 rounded-xl p-3 text-xs font-medium text-gray-900 focus:border-red-500 focus:ring-0 transition-all placeholder:text-red-200 resize-none"
        ></textarea>
        <p class="text-[9px] text-red-500 font-bold mt-1.5 flex items-center gap-1">
          <span class="material-symbols-outlined text-[12px]">gavel</span>
          Este registro será auditado bajo normativa ISO 27001.
        </p>
      </div>

      <button 
        type="submit" 
        :disabled="loading"
        class="w-full bg-red-600 hover:bg-red-700 disabled:bg-gray-400 text-white font-black py-4 rounded-xl shadow-xl shadow-red-200 flex items-center justify-center gap-3 transition-all hover:scale-[1.02] active:scale-[0.98] mt-6"
      >
        <span v-if="loading" class="animate-spin material-symbols-outlined text-[20px]">progress_activity</span>
        <span v-else class="material-symbols-outlined text-[20px]">local_fire_department</span>
        {{ loading ? 'VERIFICANDO PROTOCOLO...' : 'ACTIVAR ACCESO DE EMERGENCIA' }}
      </button>
    </form>

    <div v-if="error" class="mt-4 p-3 bg-red-100 border-l-4 border-red-600 rounded flex items-start gap-3">
       <span class="material-symbols-outlined text-red-600 text-[18px]">error</span>
       <p class="text-[11px] text-red-800 font-bold leading-tight">{{ error }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useAuthStore } from '@/stores/authStore';
import { useRouter } from 'vue-router';
import apiClient from '@/services/apiClient';

const authStore = useAuthStore();
const router = useRouter();
const loading = ref(false);
const error = ref('');

const emergencyForm = ref({
  email: '',
  password: '',
  justification: ''
});

const handleEmergencyLogin = async () => {
  loading.value = true;
  error.value = '';
  
  try {
    const res = await apiClient.post('/api/v1/auth/emergency/login', {
      email: emergencyForm.value.email,
      password: emergencyForm.value.password,
      justification: emergencyForm.value.justification
    });

    if (res.data && res.data.token) {
      authStore.login(res.data.token);
      router.push('/');
    } else {
      error.value = 'Respuesta de servidor inválida.';
    }
  } catch (err: any) {
    console.error('Error Break-Glass:', err);
    error.value = err.response?.data?.message || 'Fallo de autenticación en protocolo Break-Glass. Verifique credenciales.';
  } finally {
    loading.value = false;
  }
};
</script>
