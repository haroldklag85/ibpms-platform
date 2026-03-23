import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useTimeStore = defineStore('timeStore', () => {
    const currentTick = ref(Date.now());
    let animationFrameId: number | null = null;
    let isActive = false;

    const startEngine = () => {
        if (isActive) return;
        isActive = true;
        const tick = () => {
            currentTick.value = Date.now();
            if (isActive) {
                animationFrameId = requestAnimationFrame(tick);
            }
        };
        animationFrameId = requestAnimationFrame(tick);
    };

    const stopEngine = () => {
        isActive = false;
        if (animationFrameId !== null) {
            cancelAnimationFrame(animationFrameId);
            animationFrameId = null;
        }
    };

    return { currentTick, startEngine, stopEngine };
});
