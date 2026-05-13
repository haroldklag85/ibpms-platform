<template>
  <div v-if="visible" id="session-conflict-banner" class="fixed top-0 left-0 right-0 bg-red-600 text-white p-4 text-center z-[100] shadow-md flex justify-between items-center transition-all duration-300">
      <span class="flex-1 text-left ml-4"><strong>⚠️ Conflicto de Sesión Detectado:</strong> Esta tarea fue modificada en otra pestaña u ordenador.</span>
      <button @click="forceReload" class="bg-white text-red-600 px-4 py-2 rounded text-sm font-bold ml-4 hover:bg-gray-100 shadow">Forzar Actividad (Borrar Sesión)</button>
      <button @click="hide" class="text-white ml-4 text-xl">&times;</button>
  </div>
</template>

<script setup lang="ts">
import { useIntegrationStore } from '@/stores/useIntegrationStore';
import { ref, onMounted, onUnmounted } from 'vue';

// @Traceability: Retro-Remediación ADR-006
const integrationStore = useIntegrationStore();

const visible = ref(false);
const taskIdRef = ref<string | null>(null);

const onConflict = (e: any) => {
    visible.value = true;
    taskIdRef.value = e.detail?.taskId || null;
};

const hide = () => {
    visible.value = false;
};

const forceReload = async () => {
    if (taskIdRef.value) {
        try {
            await integrationStore.unclaimTask(taskIdRef.value); // fallback
        } catch(e) {}
    }
    window.location.reload();
};

onMounted(() => {
    window.addEventListener('session-conflict-dispatch', onConflict);
});

onUnmounted(() => {
    window.removeEventListener('session-conflict-dispatch', onConflict);
});
</script>
