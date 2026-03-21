<template>
  <Teleport to="body">
    <div v-if="isSudoVisible" class="fixed inset-0 z-[10000] flex items-center justify-center p-4">
      <!-- Backdrop -->
      <div class="absolute inset-0 bg-slate-900/80 backdrop-blur-sm" @click="handleCancel"></div>
      
      <!-- Modal Content -->
      <div class="relative bg-white rounded-xl shadow-2xl max-w-sm w-full overflow-hidden border border-slate-200">
         <div class="bg-red-50 px-6 py-4 flex items-center justify-between border-b border-red-100">
             <div class="flex items-center gap-2">
                 <span class="material-symbols-outlined text-red-600">lock_reset</span>
                 <h2 class="text-sm font-bold text-red-800 uppercase tracking-widest">Aprobación Sudo</h2>
             </div>
             <button @click="handleCancel" class="text-red-400 hover:text-red-600 focus:outline-none">
                 <span class="material-symbols-outlined text-lg">close</span>
             </button>
         </div>

         <div class="p-6">
             <p class="text-xs text-slate-600 font-medium mb-4">
                La acción `<span class="font-bold text-slate-800">{{ currentRequest?.actionName }}</span>` es destructiva o de alto privilegio.
                Confirme su identidad re-ingresando su contraseña corporativa.
             </p>

             <form @submit.prevent="handleSubmit">
                 <div class="mb-5">
                    <label class="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-2">Contraseña</label>
                    <input 
                       ref="pwdInput"
                       type="password" 
                       v-model="password" 
                       required 
                       placeholder="••••••••"
                       class="w-full border-slate-300 rounded focus:ring-red-500 focus:border-red-500 text-sm shadow-sm p-2 bg-slate-50"
                       :disabled="isLoading"
                    />
                 </div>
                 <div v-if="errorMsg" class="mb-4 text-xs font-bold text-red-600 flex items-center gap-1 bg-red-50 p-2 rounded">
                    <span class="material-symbols-outlined text-[14px]">error</span> {{ errorMsg }}
                 </div>

                 <div class="flex justify-end gap-3">
                     <button type="button" @click="handleCancel" class="px-4 py-2 text-xs font-bold text-slate-500 hover:bg-slate-100 rounded transition" :disabled="isLoading">Cancelar</button>
                     <button type="submit" class="px-5 py-2 text-xs font-bold text-white bg-red-600 hover:bg-red-700 rounded shadow-sm transition flex items-center gap-2" :disabled="isLoading || !password">
                         <span v-if="isLoading" class="material-symbols-outlined text-[14px] animate-spin">sync</span>
                         Confirmar Acción
                     </button>
                 </div>
             </form>
         </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue';
import { useSudo } from '@/composables/useSudo';

const { isSudoVisible, currentRequest, confirmSudo, cancelSudo } = useSudo();
const password = ref('');
const errorMsg = ref('');
const isLoading = ref(false);
const pwdInput = ref<HTMLInputElement | null>(null);

watch(isSudoVisible, async (newVal) => {
    if (newVal) {
        password.value = '';
        errorMsg.value = '';
        await nextTick();
        if (pwdInput.value) pwdInput.value.focus();
    }
});

const handleSubmit = async () => {
    if (!password.value) return;
    isLoading.value = true;
    errorMsg.value = '';
    
    try {
        await confirmSudo(password.value);
    } catch(e: any) {
        errorMsg.value = e.message || 'Contraseña incorrecta tras validación Sudo.';
        password.value = '';
    } finally {
        isLoading.value = false;
    }
};

const handleCancel = () => {
    if (!isLoading.value) cancelSudo();
};
</script>
