import { ref, onUnmounted } from 'vue';

export const useDraftTtl = (initialSeconds: number) => {
    const secondsRemaining = ref(initialSeconds);
    const isDraftExpired = ref(false);
    let timer: ReturnType<typeof setInterval> | null = null;

    const startTtlClock = () => {
        if (timer) clearInterval(timer);
        timer = setInterval(() => {
            if (secondsRemaining.value > 0) {
                secondsRemaining.value--;
            } else {
                isDraftExpired.value = true;
                if (timer) clearInterval(timer);
            }
        }, 1000);
    };

    const resetTtlClock = (newSeconds: number) => {
        secondsRemaining.value = newSeconds;
        isDraftExpired.value = false;
        startTtlClock();
    };

    onUnmounted(() => {
        if (timer) clearInterval(timer);
    });

    return {
        secondsRemaining,
        isDraftExpired,
        startTtlClock,
        resetTtlClock
    };
};
