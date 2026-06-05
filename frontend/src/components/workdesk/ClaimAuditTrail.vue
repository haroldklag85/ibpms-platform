<template>
  <div class="timeline text-sm pl-2" data-testid="claim-audit-timeline">
    <div v-if="isLoading" class="animate-pulse flex flex-col gap-4">
        <div class="h-4 bg-gray-200 rounded w-full"></div>
        <div class="h-4 bg-gray-200 rounded w-2/3"></div>
    </div>
    <div v-else-if="auditEvents.length === 0" class="text-gray-500 italic p-3 bg-gray-50 rounded">
        No hay registros de delegación o reclamos previos para esta tarea.
    </div>
    <div v-else class="relative border-l-2 border-indigo-100 pl-4 space-y-6 pb-2">
        <div v-for="(event, index) in auditEvents" :key="event.id || index" class="relative">
            <div class="absolute -left-[21px] bg-white rounded-full p-0.5" :title="getEventStyle(event.action).label">
                <div class="w-2.5 h-2.5 rounded-full" :class="getEventStyle(event.action).dotClass"></div>
            </div>
            <div class="bg-gray-50 rounded-lg p-3 border border-gray-100 shadow-sm -mt-1.5">
                <div class="flex justify-between items-start mb-1">
                    <span class="font-bold text-gray-800 text-xs">{{ event.actor }}</span>
                    <span class="text-[10px] text-gray-500 font-mono">{{ new Date(event.timestamp).toLocaleString() }}</span>
                </div>
                <div class="flex items-center gap-2 mb-1.5">
                    <!-- CA-20: Icon + enriched label instead of raw action -->
                    <span class="text-[10px] uppercase font-bold tracking-wider px-1.5 py-0.5 rounded flex items-center gap-1" :class="getEventStyle(event.action).badgeClass">
                        <span>{{ getEventStyle(event.action).icon }}</span>
                        {{ getEventStyle(event.action).label }}
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

// -------------------------------------------------------------------
// CA-20: Enriched ACTION_STYLE_MAP with 6 new enum values + legacy keys
// @Traceability: US-002, CA-20 (Timeline enriquecido con iconos color-coded)
// -------------------------------------------------------------------
interface ActionStyle {
    icon: string;
    dotClass: string;
    badgeClass: string;
    label: string;
}

const ACTION_STYLE_MAP: Record<string, ActionStyle> = {
    // New enum values (ClaimActionType from Backend PM-01)
    CLAIMED:          { icon: '🟢', dotClass: 'bg-emerald-400', badgeClass: 'bg-emerald-100 text-emerald-800 border border-emerald-200', label: 'Reclamada voluntariamente' },
    RELEASED:         { icon: '🔵', dotClass: 'bg-blue-400',    badgeClass: 'bg-blue-100 text-blue-800 border border-blue-200',         label: 'Liberada por el operario' },
    FORCE_UNCLAIMED:  { icon: '🟠', dotClass: 'bg-orange-400',  badgeClass: 'bg-orange-100 text-orange-800 border border-orange-200',   label: 'Despojada por supervisor' },
    AUTO_UNCLAIMED:   { icon: '🔴', dotClass: 'bg-red-400',     badgeClass: 'bg-red-100 text-red-800 border border-red-200',             label: 'Liberada por inactividad' },
    TIMEOUT_EXTENDED: { icon: '⏰', dotClass: 'bg-sky-400',     badgeClass: 'bg-sky-100 text-sky-800 border border-sky-200',             label: 'Tiempo extendido' },
    BULK_CLAIMED:     { icon: '📦', dotClass: 'bg-indigo-400',  badgeClass: 'bg-indigo-100 text-indigo-800 border border-indigo-200',   label: 'Reclamada en lote' },
    // Legacy keys (backward-compatibility with pre-PM01 audit trails)
    CLAIM:            { icon: '🟢', dotClass: 'bg-emerald-400', badgeClass: 'bg-emerald-100 text-emerald-800 border border-emerald-200', label: 'Reclamada voluntariamente' },
    FORCE_UNCLAIM:    { icon: '🟠', dotClass: 'bg-red-400',     badgeClass: 'bg-red-100 text-red-800 border border-red-200',             label: 'Despojada por supervisor' },
    UNCLAIM:          { icon: '🔵', dotClass: 'bg-blue-400',    badgeClass: 'bg-blue-100 text-blue-800 border border-blue-200',         label: 'Liberada por el operario' },
};

const DEFAULT_STYLE: ActionStyle = {
    icon: '⚪',
    dotClass: 'bg-gray-400',
    badgeClass: 'bg-gray-100 text-gray-600 border border-gray-200',
    label: 'Acción desconocida',
};

const getEventStyle = (action: string): ActionStyle =>
    ACTION_STYLE_MAP[action] || { ...DEFAULT_STYLE, label: action };
</script>
