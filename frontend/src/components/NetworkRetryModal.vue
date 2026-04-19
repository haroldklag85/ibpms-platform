<template>
    <div v-if="formStore.requiresRetry" id="network-retry-modal" class="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-[100]">
        <div class="bg-white rounded-lg p-6 max-w-sm shadow-xl">
            <h3 class="text-lg font-bold text-gray-900 mb-2">Error de Conexión</h3>
            <p class="text-sm text-gray-600 mb-4">
                El servidor está tardando en responder. (Intento {{ formStore.retryCount }} de 3).
                <br/><br/>Clave de IDempotencia: <span class="text-xs text-gray-400 font-mono">{{ formStore.idempotencyKey }}</span>
            </p>
            <div class="flex justify-end space-x-3">
                <button @click="cancel" class="px-4 py-2 text-sm text-gray-600 bg-gray-100 rounded hover:bg-gray-200">Cancelar</button>
                <button @click="retry" class="px-4 py-2 text-sm text-white bg-blue-600 rounded hover:bg-blue-700">Reintentar</button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { useFormStore } from '@/stores/useFormStore';

const formStore = useFormStore();

const retry = () => {
    window.dispatchEvent(new CustomEvent('network-retry-dispatch'));
};

const cancel = () => {
    formStore.requiresRetry = false;
};
</script>
