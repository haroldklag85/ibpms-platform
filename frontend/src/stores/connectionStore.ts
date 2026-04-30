import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export type ConnectionStatus = 'ONLINE' | 'OFFLINE' | 'RECONNECTING' | 'DEGRADED' | 'RESTORED';

export const useConnectionStore = defineStore('connectionStore', () => {
    const status = ref<ConnectionStatus>('ONLINE');
    const isSilenced = ref(false);
    
    // Migrated from useFormStore
    const requiresRetry = ref(false);
    const retryCount = ref(0);

    const setStatus = (newStatus: ConnectionStatus) => {
        status.value = newStatus;
    };

    const silence = () => {
        isSilenced.value = true;
    };

    const unsilence = () => {
        isSilenced.value = false;
    };

    const isVisible = computed(() => {
        return !isSilenced.value && status.value !== 'ONLINE';
    });

    const currentLabel = computed(() => {
        switch (status.value) {
            case 'OFFLINE': return 'Trabajando sin conexión';
            case 'RECONNECTING': return 'Reconectando...';
            case 'DEGRADED': return 'Modo sin conexión — los cambios se guardarán localmente';
            case 'RESTORED': return 'Conexión restaurada';
            default: return '';
        }
    });

    const currentIcon = computed(() => {
        switch (status.value) {
            case 'OFFLINE': return 'wifi_off';
            case 'RECONNECTING': return 'sync';
            case 'DEGRADED': return 'cloud_off';
            case 'RESTORED': return 'check_circle';
            default: return '';
        }
    });

    const currentColor = computed(() => {
        switch (status.value) {
            case 'OFFLINE': return 'bg-red-50 text-red-700 border-red-200';
            case 'RECONNECTING': return 'bg-amber-50 text-amber-700 border-amber-200';
            case 'DEGRADED': return 'bg-orange-50 text-orange-700 border-orange-200';
            case 'RESTORED': return 'bg-green-50 text-green-700 border-green-200';
            default: return '';
        }
    });

    return {
        status,
        isSilenced,
        requiresRetry,
        retryCount,
        setStatus,
        silence,
        unsilence,
        isVisible,
        currentLabel,
        currentIcon,
        currentColor
    };
});
