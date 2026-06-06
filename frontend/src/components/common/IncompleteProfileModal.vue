<template>
  <Teleport to="body">
    <div v-if="isOpen" class="fixed inset-0 bg-gray-900/90 flex flex-col items-center justify-center z-[500] p-4 backdrop-blur-md">
      <div class="bg-white rounded-2xl shadow-2xl p-8 max-w-md w-full border border-gray-200 flex flex-col items-center relative overflow-hidden">
        <div class="w-16 h-16 bg-amber-100 rounded-full flex items-center justify-center mb-5 border-4 border-amber-50">
          <span class="material-symbols-outlined text-amber-500 text-3xl">how_to_reg</span>
        </div>
        <h2 class="text-2xl font-black text-gray-800 tracking-tight text-center mb-2">Completar Perfil</h2>
        <p class="text-sm text-gray-500 text-center mb-8 font-medium">Hemos recibido tus credenciales, pero tu cuenta carece de información obligatoria para operar la bandeja iBPMS.</p>
        <form @submit.prevent="submitProfile" class="w-full space-y-5">
          <div>
            <label class="block text-xs font-bold text-gray-700 uppercase tracking-wider mb-1">Sucursal de Origen <span class="text-red-500">*</span></label>
            <select v-model="form.branchId" required class="w-full border-gray-300 rounded-lg shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm p-3 border font-medium text-gray-700 bg-gray-50">
              <option value="" disabled>Seleccione Oficina Matriz...</option>
              <option value="BOG_101">Bogotá Principal</option>
              <option value="MED_201">Medellín Operaciones</option>
              <option value="CAL_301">Cali Administrativa</option>
            </select>
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-700 uppercase tracking-wider mb-1">Teléfono Corporativo <span class="text-red-500">*</span></label>
            <input v-model="form.phone" type="tel" required placeholder="+57 320 000 0000" class="w-full border-gray-300 rounded-lg shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm p-3 border font-medium bg-gray-50" />
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
</template>

<script setup lang="ts">
import { useIntegrationStore } from '@/stores/useIntegrationStore';
import { ref, onMounted, onUnmounted } from 'vue';
import { useAuthStore } from '@/stores/authStore';
import { useRouter } from 'vue-router';

// @Traceability: Retro-Remediación ADR-006
const integrationStore = useIntegrationStore();

const isOpen = ref(false);
const form = ref({ branchId: '', phone: '' });
const tempToken = ref('');
const router = useRouter();

const handle428 = (e: Event) => {
    const customEvent = e as CustomEvent;
    isOpen.value = true;
    if (customEvent.detail && customEvent.detail.tempToken) {
        tempToken.value = customEvent.detail.tempToken;
    }
};

onMounted(() => {
    window.addEventListener('jit-428-dispatch', handle428);
});

onUnmounted(() => {
    window.removeEventListener('jit-428-dispatch', handle428);
});

const submitProfile = async () => {
    try {
        const authStore = useAuthStore();
        const success = await authStore.syncProfile(tempToken.value, form.value);
        if (success) {
            isOpen.value = false;
            router.push('/');
        }
    } catch (e) {
        alert('Error sincronizando perfil.');
    }
};
</script>
