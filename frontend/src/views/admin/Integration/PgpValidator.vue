<template>
  <div class="p-6 bg-white rounded shadow">
    <h2 class="text-lg font-bold mb-4">PGP Payload Validator (Integración Segura)</h2>
    <div class="flex flex-col gap-4">
      
      <div>
        <label class="block text-sm font-medium text-gray-700">Payload Original (JSON)</label>
        <textarea 
          v-model="rawPayload" 
          rows="4"
          class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm font-mono"
          data-test="raw-json-input"
        ></textarea>
      </div>

      <div>
        <label class="block text-sm font-medium text-gray-700">Llave Pública Destinatario (ASCII Armor)</label>
        <textarea 
          v-model="publicKey" 
          rows="4"
          class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm font-mono"
          data-test="pub-key-input"
        ></textarea>
      </div>

      <button 
        @click="simulateEncryption" 
        class="bg-blue-600 text-white px-4 py-2 rounded self-start"
        data-test="encrypt-btn"
      >
        Probar Encripción PGP
      </button>

      <div v-if="encryptedResult" class="mt-4 p-4 bg-gray-900 text-green-400 rounded overflow-x-auto text-xs font-mono" data-test="encrypted-output">
        <pre>{{ encryptedResult }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const rawPayload = ref('');
const publicKey = ref('');
const encryptedResult = ref('');

const simulateEncryption = () => {
  if (!rawPayload.value || !publicKey.value) return;
  
  // Fake the BouncyCastle encryption to validate UI behavior.
  // En el mundo real, Vue llamaría a la API del backend de CryptographyService.
  // Aquí devolvemos el Mock del Armor Block
  encryptedResult.value = `-----BEGIN PGP MESSAGE-----
Version: BCPG v1.78

hQEMAww13z3/ZqKBAQgAx+s4N...
-----END PGP MESSAGE-----`;
};
</script>
