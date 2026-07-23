<template>
  <!-- @Traceability: US-007 — Panel de Catálogo de Procesos para Iniciar Casos BPMN -->
  <Transition name="panel-slide">
    <div
      v-if="isOpen"
      class="fixed inset-0 z-[90] flex justify-end"
      data-testid="process-catalog-overlay"
    >
      <!-- Backdrop -->
      <div
        class="absolute inset-0 bg-gray-900/50 backdrop-blur-sm"
        @click="$emit('close')"
      ></div>

      <!-- Panel lateral -->
      <div class="relative w-full max-w-lg bg-white shadow-2xl flex flex-col h-full animate-slide-in-right">
        <!-- Header -->
        <div class="px-6 py-4 bg-gradient-to-r from-indigo-600 to-indigo-700 text-white flex items-center justify-between shadow-md">
          <div class="flex items-center gap-3">
            <span class="material-symbols-outlined text-2xl">rocket_launch</span>
            <div>
              <h2 class="text-lg font-bold">Iniciar Nuevo Caso</h2>
              <p class="text-indigo-200 text-xs font-medium">Seleccione un proceso BPMN para iniciar</p>
            </div>
          </div>
          <button
            @click="$emit('close')"
            class="text-indigo-200 hover:text-white transition rounded-lg p-1.5 hover:bg-white/10"
            data-testid="btn-close-catalog"
          >
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>

        <!-- Loading -->
        <div v-if="processStore.isLoadingCatalog" class="flex-1 flex items-center justify-center">
          <div class="flex flex-col items-center gap-4">
            <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-600"></div>
            <p class="text-sm text-gray-500 font-medium">Cargando procesos disponibles...</p>
          </div>
        </div>

        <!-- Error -->
        <div v-else-if="processStore.hasError" class="flex-1 flex items-center justify-center p-6">
          <div class="text-center max-w-sm">
            <span class="material-symbols-outlined text-5xl text-red-400 mb-4">error</span>
            <p class="text-red-700 font-bold text-sm mb-2">Error al cargar el catálogo</p>
            <p class="text-red-500 text-xs mb-4">{{ processStore.error }}</p>
            <button
              @click="processStore.fetchCatalog()"
              class="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-lg text-sm transition"
            >
              Reintentar
            </button>
          </div>
        </div>

        <!-- Empty State -->
        <div v-else-if="processStore.activeProcesses.length === 0" class="flex-1 flex items-center justify-center p-6">
          <div class="text-center max-w-sm">
            <span class="material-symbols-outlined text-5xl text-gray-300 mb-4">inventory_2</span>
            <p class="text-gray-600 font-bold text-sm">No hay procesos disponibles</p>
            <p class="text-gray-400 text-xs mt-2">Despliegue una definición BPMN desde el Modelador para verla aquí.</p>
          </div>
        </div>

        <!-- Lista de procesos -->
        <div v-else class="flex-1 overflow-y-auto p-4 space-y-3">
          <div
            v-for="process in processStore.activeProcesses"
            :key="process.key"
            class="bg-white border border-gray-200 rounded-xl p-4 hover:border-indigo-300 hover:shadow-md transition-all duration-200 group"
            :data-testid="'process-card-' + process.key"
          >
            <div class="flex items-start justify-between gap-3">
              <div class="flex items-center gap-3 min-w-0">
                <div class="w-10 h-10 rounded-lg bg-indigo-50 border border-indigo-100 flex items-center justify-center shrink-0 group-hover:bg-indigo-100 transition">
                  <span class="material-symbols-outlined text-indigo-600 text-xl">schema</span>
                </div>
                <div class="min-w-0">
                  <p class="font-bold text-gray-900 text-sm truncate" :title="process.name">
                    {{ process.name || process.key }}
                  </p>
                  <p class="text-[10px] font-mono text-gray-400 mt-0.5">{{ process.key }}</p>
                </div>
              </div>
              <div class="flex flex-col items-end gap-1.5 shrink-0">
                <span class="px-2 py-0.5 bg-emerald-50 text-emerald-700 rounded text-[9px] font-bold border border-emerald-200">
                  v{{ process.version }}
                </span>
                <span class="text-[9px] text-gray-400 font-medium">
                  {{ formatDeployDate(process.deployDate) }}
                </span>
              </div>
            </div>
            <div class="mt-3 flex items-center justify-between">
              <span
                v-if="process.formPattern"
                class="text-[10px] text-gray-400 font-medium flex items-center gap-1"
              >
                <span class="material-symbols-outlined text-[12px]">description</span>
                {{ process.formPattern }}
              </span>
              <span v-else></span>
              <button
                @click="onStartProcess(process)"
                :disabled="processStore.isStartingProcess"
                class="px-3 py-1.5 bg-indigo-600 hover:bg-indigo-700 active:bg-indigo-800 text-white font-bold rounded-lg text-xs transition-all shadow-sm hover:shadow flex items-center gap-1.5 disabled:opacity-50 disabled:cursor-not-allowed"
                :data-testid="'btn-start-' + process.key"
              >
                <span
                  v-if="processStore.isStartingProcess && startingKey === process.key"
                  class="material-symbols-outlined text-[14px] animate-spin"
                >refresh</span>
                <span v-else class="material-symbols-outlined text-[14px]">play_arrow</span>
                {{ processStore.isStartingProcess && startingKey === process.key ? 'Iniciando...' : 'Iniciar Caso' }}
              </button>
            </div>
          </div>
        </div>

        <!-- Footer: Resultado del último inicio -->
        <Transition name="toast-slide">
          <div
            v-if="showSuccessResult"
            class="px-6 py-4 bg-emerald-50 border-t-2 border-emerald-300 shrink-0"
            data-testid="start-success-banner"
          >
            <div class="flex items-start gap-3">
              <span class="material-symbols-outlined text-emerald-600 text-2xl shrink-0">check_circle</span>
              <div class="min-w-0">
                <p class="text-emerald-800 font-bold text-sm">¡Caso iniciado exitosamente!</p>
                <p class="text-emerald-600 text-xs mt-1 font-mono truncate" :title="processStore.lastStartResult?.processInstanceId">
                  ID: {{ processStore.lastStartResult?.processInstanceId }}
                </p>
                <p class="text-emerald-500 text-[10px] mt-0.5">
                  Iniciado por: {{ processStore.lastStartResult?.startedBy }}
                </p>
              </div>
            </div>
          </div>
        </Transition>

        <!-- Footer: Error de inicio -->
        <Transition name="toast-slide">
          <div
            v-if="processStore.hasError && processStore.isStartingProcess === false && lastAction === 'start'"
            class="px-6 py-4 bg-red-50 border-t-2 border-red-300 shrink-0"
            data-testid="start-error-banner"
          >
            <div class="flex items-start gap-3">
              <span class="material-symbols-outlined text-red-500 text-2xl shrink-0">error</span>
              <div class="min-w-0">
                <p class="text-red-800 font-bold text-sm">Error al iniciar el proceso</p>
                <p class="text-red-600 text-xs mt-1">{{ processStore.error }}</p>
              </div>
              <button @click="processStore.clearError()" class="text-red-400 hover:text-red-600 shrink-0">
                <span class="material-symbols-outlined text-[16px]">close</span>
              </button>
            </div>
          </div>
        </Transition>
      </div>
    </div>
  </Transition>

  <!-- Modal de Confirmación (NO usa alert/confirm nativo — .cursorrules §5) -->
  <Transition name="toast-slide">
    <div
      v-if="showConfirmModal"
      class="fixed inset-0 z-[100] flex items-center justify-center bg-gray-900/60 backdrop-blur-sm p-4"
      data-testid="confirm-start-modal"
    >
      <div class="bg-white rounded-xl shadow-2xl w-full max-w-md overflow-hidden border border-gray-200">
        <div class="px-6 py-4 border-b border-gray-100 bg-indigo-50">
          <h3 class="text-lg font-bold text-gray-900 flex items-center gap-2">
            <span class="material-symbols-outlined text-indigo-600">rocket_launch</span>
            Confirmar Inicio de Caso
          </h3>
        </div>
        <div class="p-6 space-y-4">
          <div class="bg-gray-50 rounded-lg p-4 border border-gray-200">
            <p class="text-xs text-gray-400 uppercase font-bold tracking-widest mb-1">Proceso</p>
            <p class="text-sm font-bold text-gray-900">{{ confirmProcess?.name || confirmProcess?.key }}</p>
            <p class="text-[10px] font-mono text-gray-400 mt-1">{{ confirmProcess?.key }} v{{ confirmProcess?.version }}</p>
          </div>
          <div class="bg-blue-50 border border-blue-200 text-blue-800 p-3 rounded-lg text-[13px] flex gap-3">
            <span class="material-symbols-outlined text-blue-600 mt-0.5 shrink-0">info</span>
            <p>Se creará una nueva instancia de este proceso. Las tareas generadas aparecerán en la Bandeja Unificada.</p>
          </div>
        </div>
        <div class="px-6 py-4 border-t border-gray-100 bg-gray-50 flex justify-end gap-3">
          <button
            @click="cancelStart"
            class="px-4 py-2.5 text-sm font-bold text-gray-600 hover:text-gray-800 hover:bg-gray-200/60 rounded-lg transition"
            :disabled="processStore.isStartingProcess"
          >
            Cancelar
          </button>
          <button
            @click="confirmStart"
            :disabled="processStore.isStartingProcess"
            class="px-5 py-2.5 text-sm font-bold text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg shadow disabled:opacity-50 disabled:cursor-not-allowed transition flex items-center gap-2"
            data-testid="btn-confirm-start"
          >
            <span v-if="processStore.isStartingProcess" class="material-symbols-outlined animate-spin text-[18px]">refresh</span>
            <span v-else class="material-symbols-outlined text-[18px]">play_arrow</span>
            {{ processStore.isStartingProcess ? 'Iniciando...' : 'Iniciar Caso' }}
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
/**
 * @component ProcessCatalogPanel
 * @description Panel lateral para listar procesos BPMN y permitir iniciar nuevos casos.
 * @traceability US-007 — Ejecución BPMN
 */
defineOptions({ name: 'ProcessCatalogPanel' });

import { ref, watch } from 'vue';
import { useProcessStore } from '@/stores/useProcessStore';
import type { ProcessCatalogItem } from '@/types/Process';

const props = defineProps<{
  isOpen: boolean;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'process-started', processInstanceId: string): void;
}>();

const processStore = useProcessStore();

const showConfirmModal = ref(false);
const confirmProcess = ref<ProcessCatalogItem | null>(null);
const startingKey = ref<string | null>(null);
const showSuccessResult = ref(false);
const lastAction = ref<'start' | 'catalog' | null>(null);

/** Carga el catálogo cuando el panel se abre */
watch(() => props.isOpen, (isOpen) => {
  if (isOpen) {
    processStore.clearError();
    processStore.clearLastResult();
    showSuccessResult.value = false;
    lastAction.value = 'catalog';
    processStore.fetchCatalog();
  }
});

/** Abre el modal de confirmación antes de iniciar */
const onStartProcess = (process: ProcessCatalogItem): void => {
  confirmProcess.value = process;
  showConfirmModal.value = true;
  processStore.clearError();
};

/** Confirma e inicia el proceso */
const confirmStart = async (): Promise<void> => {
  if (!confirmProcess.value) return;

  startingKey.value = confirmProcess.value.key;
  lastAction.value = 'start';
  showSuccessResult.value = false;

  const result = await processStore.startProcess({
    processDefinitionKey: confirmProcess.value.key,
    variables: {},
  });

  showConfirmModal.value = false;

  if (result) {
    showSuccessResult.value = true;
    emit('process-started', result.processInstanceId);

    // Auto-ocultar el banner de éxito tras 6 segundos
    setTimeout(() => {
      showSuccessResult.value = false;
    }, 6000);
  }

  startingKey.value = null;
};

/** Cancela la confirmación */
const cancelStart = (): void => {
  showConfirmModal.value = false;
  confirmProcess.value = null;
};

/** Formatea la fecha de despliegue a formato legible */
const formatDeployDate = (dateStr: string): string => {
  if (!dateStr) return '';
  try {
    return new Date(dateStr).toLocaleDateString('es-CO', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  } catch {
    return dateStr;
  }
};
</script>

<style scoped>
/* Animación del panel lateral */
@keyframes slide-in-right {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

.animate-slide-in-right {
  animation: slide-in-right 0.3s ease-out;
}

.panel-slide-enter-active {
  transition: opacity 0.2s ease;
}
.panel-slide-leave-active {
  transition: opacity 0.15s ease;
}
.panel-slide-enter-from,
.panel-slide-leave-to {
  opacity: 0;
}

.toast-slide-enter-active {
  transition: all 0.3s ease-out;
}
.toast-slide-leave-active {
  transition: all 0.2s ease-in;
}
.toast-slide-enter-from {
  opacity: 0;
  transform: translateY(8px);
}
.toast-slide-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
