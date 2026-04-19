import { ref, computed, onUnmounted } from 'vue';

export const useSlaTrafficLight = (expirationDateIso: string | null) => {
    const timeRemainingMs = ref(0);
    const isValid = computed(() => expirationDateIso !== null && expirationDateIso !== '');

    let timer: ReturnType<typeof setInterval> | null = null;

    const startClock = () => {
        if (!isValid.value) return;
        const target = new Date(expirationDateIso as string).getTime();
        
        timer = setInterval(() => {
            timeRemainingMs.value = target - Date.now();
        }, 1000);
        timeRemainingMs.value = target - Date.now();
    };

    const stopClock = () => {
        if (timer) clearInterval(timer);
    };

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

    // Arrancar automaticamente si hay valor
    if (isValid.value) startClock();

    onUnmounted(() => {
        stopClock();
    });

    return {
        timeRemainingMs,
        trafficColor,
        isAtRisk,
        isExpired,
        startClock,
        stopClock
    };
};
