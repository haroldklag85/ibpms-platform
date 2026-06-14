import { computed } from 'vue';
import { useTimeStore } from '@/stores/timeStore';

// @Traceability: Remediación Deuda Técnica - CA-11 / ADR-006 (Pinia Centralizado)
export const useSlaTrafficLight = (expirationDateIso: string | null) => {
    const timeStore = useTimeStore();
    const isValid = computed(() => expirationDateIso !== null && expirationDateIso !== '');

    const timeRemainingMs = computed(() => {
        if (!isValid.value) return 0;
        const target = new Date(expirationDateIso as string).getTime();
        return target - timeStore.currentTick;
    });

    const startClock = () => {};
    const stopClock = () => {};

    const trafficColor = computed(() => {
        if (!isValid.value) return 'bg-gray-100 text-gray-500';
        // Menos de 1h (3600000ms) = ROJO
        if (timeRemainingMs.value <= 3600000) return 'bg-red-100 text-red-700 border-red-500';
        // Menos de 24h (86400000ms) = AMARILLO
        if (timeRemainingMs.value <= 86400000) return 'bg-yellow-100 text-yellow-700 border-yellow-500';
        // VERDE
        return 'bg-green-100 text-green-700 border-green-500';
    });

    const isAtRisk = computed(() => timeRemainingMs.value <= 86400000 && timeRemainingMs.value > 0);
    const isExpired = computed(() => timeRemainingMs.value <= 0 && isValid.value);

    return {
        timeRemainingMs,
        trafficColor,
        isAtRisk,
        isExpired,
        startClock,
        stopClock
    };
};
