<template>
  <div class="p-6 bg-white rounded shadow">
    <h2 class="text-lg font-bold mb-4">Configurar Webhook APIM (Outbound)</h2>
    <form @submit.prevent="saveWebhook">
      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700">URL del APIM Externo</label>
        <input 
          v-model="webhookUrl" 
          type="url" 
          required 
          class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
          placeholder="https://api.tu-apim.com/v1/legacy-soap"
          data-test="url-input"
        />
        <p v-if="error" class="mt-2 text-sm text-red-600" data-test="error-message">{{ error }}</p>
      </div>
      <button 
        type="submit" 
        class="bg-indigo-600 text-white px-4 py-2 rounded focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
        data-test="save-btn"
      >
        Guardar Configuración
      </button>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const webhookUrl = ref('');
const error = ref('');

const saveWebhook = () => {
  error.value = '';
  // SSRF Guardrail en la UI (Fast-fail)
  try {
    const parsed = new URL(webhookUrl.value);
    const host = parsed.hostname.toLowerCase();
    
    if (host === 'localhost' || host === '127.0.0.1') {
      error.value = 'SSRF Blocked: No se permiten URLs apuntando a Localhost o loopbacks (127.x.x.x).';
      return;
    }
    if (host === '169.254.169.254') {
      error.value = 'SSRF Blocked: Intento de extracción de metadatos Cloud (AWS/Azure) bloqueado.';
      return;
    }
    if (host === '0.0.0.0') {
      error.value = 'SSRF Blocked: Enrutamiento wildcard bloqueado.';
      return;
    }

    // Success payload
    console.log("Configuración guardada hacia:", webhookUrl.value);
  } catch (e) {
    error.value = 'URL inválida.';
  }
};
</script>
