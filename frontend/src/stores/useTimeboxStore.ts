import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { api } from '@/services/apiClient';

export interface SlaChangelog {
    id: string;
    taskId: string;
    requestSlaStart: string;
    grantedSlaEnd: string;
    reason: string;
    changedBy: string;
}

export const useTimeboxStore = defineStore('timeboxStore', () => {
    const changelogs = ref<SlaChangelog[]>([]);
    const isLoadingLogs = ref(false);
    const isExtendingSla = ref(false);
    const errorState = ref<{ code: string; message: string } | null>(null);

    const hasActiveExtensions = computed(() => changelogs.value.length > 0);

    const fetchLogs = async (taskId: string) => {
        isLoadingLogs.value = true;
        errorState.value = null;
        try {
            const response = await api.getSlaLogs(taskId);
            changelogs.value = response.data.content || [];
        } catch (err: any) {
            errorState.value = {
                code: err.response?.status?.toString() || 'FETCH_ERR',
                message: err.response?.data?.message || 'Error cargando SLA logs'
            };
        } finally {
            isLoadingLogs.value = false;
        }
    };

    const extendSla = async (taskId: string, payload: { reason: string; extensionHours: number }) => {
        isExtendingSla.value = true;
        errorState.value = null;
        try {
            await api.requestTimeboxExtension(taskId, payload);
            // Re-fetch to update logs
            await fetchLogs(taskId);
        } catch (err: any) {
            // CA-1: Zero-Trust error tracking
            errorState.value = {
                code: err.response?.data?.code || err.response?.status?.toString() || 'EXTEND_ERR',
                message: err.response?.data?.message || 'Error extendiendo el Timebox'
            };
            throw err; // Propagate for specific UI handling if needed later
        } finally {
            isExtendingSla.value = false;
        }
    };

    return {
        changelogs,
        isLoadingLogs,
        isExtendingSla,
        errorState,
        hasActiveExtensions,
        fetchLogs,
        extendSla
    };
});
