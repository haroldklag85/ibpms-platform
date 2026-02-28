<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-100">
    <div class="max-w-md w-full bg-white rounded-lg shadow-md p-8">
      <div class="text-center mb-8">
        <h2 class="text-3xl font-bold text-ibpms">iBPMS Platform</h2>
        <p class="text-gray-500 mt-2">Inicia sesión con tu cuenta corporativa</p>
      </div>

      <form @submit.prevent="handleLogin" class="space-y-6">
        <div>
          <label class="block text-sm font-medium text-gray-700">Correo Electrónico</label>
          <input 
            v-model="email" 
            type="email" 
            required 
            placeholder="usuario@empresa.com"
            class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-ibpms-brand focus:border-ibpms-brand sm:text-sm"
          >
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700">Contraseña</label>
          <input 
            v-model="password" 
            type="password" 
            required
            class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-ibpms-brand focus:border-ibpms-brand sm:text-sm"
          >
        </div>

        <div>
          <button 
            type="submit" 
            class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-ibpms-brand hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-ibpms-brand transition"
          >
            Ingresar
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const email = ref('');
const password = ref('');

const handleLogin = () => {
  // Simulación de Auth y JWT temporal
  console.log('Login attempt:', email.value);
  localStorage.setItem('ibpms_token', 'mock_jwt_token_12345');
  router.push('/');
};
</script>
