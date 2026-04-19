import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { useDraftTtl } from '@/composables/useDraftTtl';

// Mock onUnmounted since we're outside a Vue component context
vi.mock('vue', async (importOriginal) => {
    const actual = await importOriginal<typeof import('vue')>();
    return { ...actual, onUnmounted: vi.fn() };
});

describe('useDraftTtl (CA-36)', () => {
    beforeEach(() => {
        vi.useFakeTimers();
    });

    afterEach(() => {
        vi.runOnlyPendingTimers();
        vi.useRealTimers();
    });

    it('Inicializa con los segundos provistos y no está expirado', () => {
        const { secondsRemaining, isDraftExpired } = useDraftTtl(250);
        expect(secondsRemaining.value).toBe(250);
        expect(isDraftExpired.value).toBe(false);
    });

    it('Decrementa el contador cada segundo tras startTtlClock', () => {
        const { secondsRemaining, startTtlClock } = useDraftTtl(10);
        startTtlClock();

        vi.advanceTimersByTime(3000);
        expect(secondsRemaining.value).toBe(7);
    });

    it('Marca isDraftExpired = true cuando llega a 0 (modal rojo bloqueante)', () => {
        const { secondsRemaining, isDraftExpired, startTtlClock } = useDraftTtl(3);
        startTtlClock();

        vi.advanceTimersByTime(3000); // 3 ticks
        expect(secondsRemaining.value).toBe(0);
        
        vi.advanceTimersByTime(1000); // 1 tick más para activar la condición
        expect(isDraftExpired.value).toBe(true);
    });

    it('resetTtlClock reinicia el contador con nuevos segundos', () => {
        const { secondsRemaining, isDraftExpired, startTtlClock, resetTtlClock } = useDraftTtl(5);
        startTtlClock();

        vi.advanceTimersByTime(3000);
        expect(secondsRemaining.value).toBe(2);

        resetTtlClock(100);
        expect(secondsRemaining.value).toBe(100);
        expect(isDraftExpired.value).toBe(false);

        vi.advanceTimersByTime(2000);
        expect(secondsRemaining.value).toBe(98);
    });
});
