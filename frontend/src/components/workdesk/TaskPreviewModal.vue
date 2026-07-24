<template>
  <Teleport to="body">
    <div v-if="taskId" class="fixed inset-0 bg-gray-900/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
       <!-- CA-5 Read Only Modal -->
       <div class="bg-white rounded-xl shadow-2xl flex flex-col max-h-[90vh] w-full max-w-3xl overflow-hidden relative modal-content mt-12">
            <!-- CA-18 Warning Banner (improved with user name) -->
            <div v-if="isAlreadyClaimed" class="bg-amber-100 border-l-4 border-amber-500 text-amber-700 p-4 w-full z-10 flex items-center shadow-sm" data-testid="claimed-by-other-banner">
              <span class="mr-2">⚠️</span>
              <p class="font-medium text-sm">
                Esta tarea fue reclamada por
                <strong v-if="claimedByName">{{ claimedByName }}</strong>
                <span v-else>otro compañero</span>
                y ya no está disponible
              </p>
            </div>

            <!-- CA-16: Banner de nota del operario anterior -->
            <div v-if="taskDetail?.mensajeInterno" class="bg-blue-50 border-l-4 border-blue-400 text-blue-800 p-4 w-full z-10 flex items-start gap-3 shadow-sm" data-testid="internal-note-banner">
              <span class="text-lg shrink-0">📝</span>
              <div>
                <p class="font-bold text-sm">Nota del operario anterior:</p>
                <p class="text-sm italic mt-1">{{ taskDetail.mensajeInterno }}</p>
                <span v-if="taskDetail.mensajeInternoAuthor" class="text-xs text-blue-600 mt-1 block">
                  — {{ taskDetail.mensajeInternoAuthor }}{{ taskDetail.mensajeInternoAt ? ', ' + formatTimeAgo(taskDetail.mensajeInternoAt) : '' }}
                </span>
              </div>
            </div>
           
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
               <button v-if="!(readOnly && taskDetail?.assignee)" @click="handleClaim" :disabled="isLoading || isClaiming || isAlreadyClaimed" class="px-5 py-2 bg-indigo-600 text-white rounded-md font-bold text-sm hover:bg-indigo-700 shadow-sm transition flex gap-2 items-center disabled:opacity-50 disabled:bg-gray-400 disabled:cursor-not-allowed" data-test="btn-claim">
                   <span v-if="isAlreadyClaimed" class="text-sm">🔒</span>
                   <span v-else-if="isClaiming" class="animate-spin h-4 w-4 border-2 border-white border-t-transparent rounded-full"></span>
                   {{ isAlreadyClaimed ? 'No Disponible' : (isClaiming ? 'Reclamando...' : 'Reclamar Tarea') }}
               </button>
           </footer>
       </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue';
import { useWorkdeskStore } from '@/stores/useWorkdeskStore';
import { useAuthStore } from '@/stores/authStore';
import ClaimAuditTrail from './ClaimAuditTrail.vue';

const props = defineProps<{ taskId: string | null; readOnly?: boolean }>();
const emit = defineEmits(['close']);

const store = useWorkdeskStore();
const authStore = useAuthStore();
const taskDetail = ref<any>(null);
const isLoading = ref(false);
const isClaiming = ref(false);
const isAlreadyClaimed = ref(false);
const claimedByName = ref('');
let wsSubscription: any = null;

const setupWebSocket = () => {
    if (!props.taskId || !store.stompClient?.connected) return;
    
    const tenantId = (authStore as any).tenantId || 'default';
    wsSubscription = store.stompClient.subscribe(`/topic/workdesk/${tenantId}`, (message) => {
        try {
            const event = JSON.parse(message.body);
            if (event.action === 'REMOVE' && event.taskId === props.taskId) {
                isAlreadyClaimed.value = true;
                claimedByName.value = event.claimedByName || event.assignee || '';
            }
        } catch(e) {}
    });
};

onUnmounted(() => {
    if (wsSubscription) {
        wsSubscription.unsubscribe();
    }
});

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
    if (props.taskId) {
        loadTask(props.taskId);
        setupWebSocket();
    }
});

watch(() => props.taskId, (newVal) => {
    if (wsSubscription) {
        wsSubscription.unsubscribe();
        wsSubscription = null;
    }
    isAlreadyClaimed.value = false;
    
    if (newVal) {
        loadTask(newVal);
        setupWebSocket();
    } else {
        taskDetail.value = null;
    }
});

// @Traceability: US-002, CA-16 — Relative time formatting for internal note
const formatTimeAgo = (dateStr: string): string => {
    const now = Date.now();
    const past = new Date(dateStr).getTime();
    const diffMin = Math.floor((now - past) / 60000);
    if (diffMin < 1) return 'hace un momento';
    if (diffMin < 60) return `hace ${diffMin} min`;
    const diffH = Math.floor(diffMin / 60);
    if (diffH < 24) return `hace ${diffH}h`;
    return `hace ${Math.floor(diffH / 24)}d`;
};

const handleClaim = async () => {
    if (!props.taskId) return;
    isClaiming.value = true;
    try {
        await store.claimTask(props.taskId);
        emit('close');
    } catch(err) {
        if (err.response && err.response.status === 409) {
            isAlreadyClaimed.value = true;
        } else {
            emit('close'); 
        }
    } finally {
        isClaiming.value = false;
    }
};
</script>
