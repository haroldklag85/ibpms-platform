<template>
  <Teleport to="body">
    <div v-if="taskId" class="fixed inset-0 bg-gray-900/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
       <!-- CA-5 Read Only Modal -->
       <div class="bg-white rounded-xl shadow-2xl flex flex-col max-h-[90vh] w-full max-w-3xl overflow-hidden relative modal-content">
           <header class="bg-indigo-50 border-b border-indigo-100 px-6 py-4 flex justify-between items-start">
               <div>
                   <h2 class="text-xl font-bold text-indigo-900">{{ taskDetail?.title || 'Cargando detalle...' }}</h2>
                   <div class="flex items-center gap-2 mt-2">
                       <span class="px-2 py-0.5 bg-indigo-100 text-indigo-800 text-xs font-bold rounded-full border border-indigo-200">
                           {{ taskDetail?.typeBadge || 'Tarea' }}
                       </span>
                       <span class="text-xs bg-gray-100 text-gray-600 px-2 py-0.5 rounded border border-gray-200" v-if="taskDetail?.candidateGroup">
                           Grupo: {{ taskDetail.candidateGroup }}
                       </span>
                   </div>
               </div>
               <button @click="$emit('close')" class="text-gray-400 hover:text-gray-600 transition p-1">
                   ✕
               </button>
           </header>
           
           <div class="flex-1 overflow-y-auto px-6 py-5">
               <div v-if="isLoading" class="flex justify-center p-8">
                   <span class="animate-spin h-8 w-8 border-4 border-indigo-500 border-t-transparent rounded-full"></span>
               </div>
               <div v-else-if="taskDetail">
                   <div class="mb-6 grid grid-cols-2 gap-4">
                       <div class="p-4 bg-gray-50 border border-gray-100 rounded-lg">
                           <label class="text-xs font-bold text-gray-500 uppercase tracking-wide">ID de Tarea</label>
                           <p class="font-mono text-sm text-gray-900 mt-1">{{ taskDetail.unifiedId || taskId }}</p>
                       </div>
                       <div class="p-4 bg-gray-50 border border-gray-100 rounded-lg">
                           <label class="text-xs font-bold text-gray-500 uppercase tracking-wide">SLA Vencimiento</label>
                           <p class="text-sm font-medium mt-1" :class="taskDetail.slaExpirationDate ? 'text-red-700' : 'text-gray-600'">
                               {{ taskDetail.slaExpirationDate ? new Date(taskDetail.slaExpirationDate).toLocaleString() : 'N/A' }}
                           </p>
                       </div>
                   </div>
                   <div class="mb-6" v-if="taskDetail.description">
                       <label class="text-xs font-bold text-gray-500 uppercase tracking-wide mb-2 block">Descripción / Instrucciones</label>
                       <div class="p-4 bg-white border border-gray-200 shadow-inner rounded-md text-sm text-gray-700 min-h-[100px] select-all">
                           {{ taskDetail.description }}
                       </div>
                   </div>
                   
                   <!-- CA-9: Auditoria sub-componente -->
                   <div class="border-t border-gray-200 pt-6">
                       <h3 class="text-sm font-bold text-gray-900 mb-4 flex items-center gap-2">
                           <span class="text-gray-500 text-sm">⏱️</span>
                           Historial de Reasignaciones (Audit Trail)
                       </h3>
                       <ClaimAuditTrail :taskId="taskId" />
                   </div>
               </div>
           </div>

           <footer class="bg-gray-50 px-6 py-4 border-t border-gray-200 flex justify-end gap-3 sticky bottom-0">
               <button @click="$emit('close')" class="px-4 py-2 border border-gray-300 text-gray-700 rounded-md font-medium text-sm hover:bg-gray-100 transition">
                   Cancelar
               </button>
               <button @click="handleClaim" :disabled="isLoading || isClaiming" class="px-5 py-2 bg-indigo-600 text-white rounded-md font-bold text-sm hover:bg-indigo-700 shadow-sm transition flex gap-2 items-center disabled:opacity-50" data-test="btn-claim">
                   <span v-if="isClaiming" class="animate-spin h-4 w-4 border-2 border-white border-t-transparent rounded-full"></span>
                   {{ isClaiming ? 'Reclamando...' : 'Reclamar Tarea' }}
               </button>
           </footer>
       </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import { useWorkdeskStore } from '@/stores/useWorkdeskStore';
import ClaimAuditTrail from './ClaimAuditTrail.vue';

const props = defineProps<{ taskId: string | null }>();
const emit = defineEmits(['close']);

const store = useWorkdeskStore();
const taskDetail = ref<any>(null);
const isLoading = ref(false);
const isClaiming = ref(false);

const loadTask = async (id: string) => {
    isLoading.value = true;
    try {
        taskDetail.value = await store.fetchTaskPreview(id);
    } catch (e) {
        console.error('Error cargando preview', e);
        taskDetail.value = {
            unifiedId: id,
            title: 'Tarea (Preview Fallido)',
            description: 'No se pudo cargar la vista de previa detallada.'
        };
    } finally {
        isLoading.value = false;
    }
};

onMounted(() => {
    if (props.taskId) loadTask(props.taskId);
});

watch(() => props.taskId, (newVal) => {
    if (newVal) {
        loadTask(newVal);
    } else {
        taskDetail.value = null;
    }
});

const handleClaim = async () => {
    if (!props.taskId) return;
    isClaiming.value = true;
    try {
        await store.claimTask(props.taskId);
        emit('close');
    } catch(e) {
        emit('close'); 
    } finally {
        isClaiming.value = false;
    }
};
</script>
