<template>
  <div class="timeline text-sm pl-2">
    <div v-if="isLoading" class="animate-pulse flex flex-col gap-4">
        <div class="h-4 bg-gray-200 rounded w-full"></div>
        <div class="h-4 bg-gray-200 rounded w-2/3"></div>
    </div>
    <div v-else-if="auditEvents.length === 0" class="text-gray-500 italic p-3 bg-gray-50 rounded">
        No hay registros de delegación o reclamos previos para esta tarea.
    </div>
    <div v-else class="relative border-l-2 border-indigo-100 pl-4 space-y-6 pb-2">
        <div v-for="(event, index) in auditEvents" :key="event.id || index" class="relative">
            <div class="absolute -left-[21px] bg-white rounded-full p-0.5" title="Hito Transaccional">
                <div class="w-2.5 h-2.5 rounded-full" :class="getDotColor(event.action)"></div>
            </div>
            <div class="bg-gray-50 rounded-lg p-3 border border-gray-100 shadow-sm -mt-1.5">
                <div class="flex justify-between items-start mb-1">
                    <span class="font-bold text-gray-800 text-xs">{{ event.actor }}</span>
                    <span class="text-[10px] text-gray-500 font-mono">{{ new Date(event.timestamp).toLocaleString() }}</span>
                </div>
                <div class="flex items-center gap-2 mb-1.5">
                    <span class="text-[10px] uppercase font-bold tracking-wider px-1.5 py-0.5 rounded" :class="getActionBadge(event.action)">
                        {{ event.action }}
                    </span>
                </div>
                <p v-if="event.reason" class="text-xs text-gray-600 italic bg-white p-2 rounded border border-gray-200 mt-2">
                    "{{ event.reason }}"
                </p>
            </div>
        </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useWorkdeskStore } from '@/stores/useWorkdeskStore';

const props = defineProps<{ taskId: string }>();
const auditEvents = ref<any[]>([]);
const isLoading = ref(true);
const store = useWorkdeskStore();

const loadAudit = async (id: string) => {
    isLoading.value = true;
    try {
        const payload = await store.fetchAuditTrail(id);
        auditEvents.value = Array.isArray(payload) ? payload : [];
    } catch (e) {
        console.error('Audit Load Fallback', e);
        auditEvents.value = [];
    } finally {
        isLoading.value = false;
    }
};

onMounted(() => { if (props.taskId) loadAudit(props.taskId) });
watch(() => props.taskId, (val) => { if (val) loadAudit(val) });

const getDotColor = (action: string) => {
    if (action === 'FORCE_UNCLAIM') return 'bg-red-400';
    if (action === 'CLAIM') return 'bg-emerald-400';
    return 'bg-amber-400';
};
const getActionBadge = (action: string) => {
    if (action === 'FORCE_UNCLAIM') return 'bg-red-100 text-red-800 border border-red-200';
    if (action === 'CLAIM') return 'bg-emerald-100 text-emerald-800 border border-emerald-200';
    return 'bg-amber-100 text-amber-800 border border-amber-200';
};
</script>
