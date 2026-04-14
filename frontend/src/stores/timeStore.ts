import { defineStore } from 'pinia';
import { ref, readonly } from 'vue';

/**
 * CA-05: SLA Ticking Engine Vivo (requestAnimationFrame global)
 * CA-11: Anti DOM-Thrashing — un solo heartbeat, TODAS las tarjetas heredan pasivamente.
 * CA-25: Recálculo inmediato al volver de pestaña inactiva (visibilitychange).
 */
export const useTimeStore = defineStore('timeStore', () => {
    const currentTick = ref(Date.now());
    let animationFrameId: number | null = null;
    let isActive = false;
    let lastUpdateTime = 0;

    // CA-11: Throttle de actualización — re-calcular cada 1 segundo, no cada frame
    const TICK_INTERVAL_MS = 1000;

    const tick = () => {
        const now = Date.now();
        // Solo actualizar el ref reactivo cada 1s para evitar DOM-thrashing
        if (now - lastUpdateTime >= TICK_INTERVAL_MS) {
            currentTick.value = now;
            lastUpdateTime = now;
        }
        if (isActive) {
            animationFrameId = requestAnimationFrame(tick);
        }
    };

    const startEngine = () => {
        if (isActive) return;
        isActive = true;
        lastUpdateTime = Date.now();
        currentTick.value = Date.now();
        animationFrameId = requestAnimationFrame(tick);

        // CA-25: Listener de visibilitychange para recálculo inmediato
        document.addEventListener('visibilitychange', _onVisibilityChange);
    };

    const stopEngine = () => {
        isActive = false;
        if (animationFrameId !== null) {
            cancelAnimationFrame(animationFrameId);
            animationFrameId = null;
        }
        document.removeEventListener('visibilitychange', _onVisibilityChange);
    };

    // CA-25: Al volver de tab inactiva, recálculo INMEDIATO
    const _onVisibilityChange = () => {
        if (document.visibilityState === 'visible') {
            currentTick.value = Date.now();
            lastUpdateTime = Date.now();
        }
    };

    // CA-25: Exponer el tiempo de inactividad al Workdesk para CA-31
    const getInactivityMs = (): number => {
        return Date.now() - lastUpdateTime;
    };

    return {
        currentTick: readonly(currentTick),
        startEngine,
        stopEngine,
        getInactivityMs
    };
});
