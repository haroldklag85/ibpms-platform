import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { useSlaTrafficLight } from '@/composables/useSlaTrafficLight';

// Mock onUnmounted since we're outside a Vue component context
vi.mock('vue', async (importOriginal) => {
    const actual = await importOriginal<typeof import('vue')>();
    return { ...actual, onUnmounted: vi.fn() };
});

describe('useSlaTrafficLight (CA-24 / CA-25)', () => {
    beforeEach(() => {
        vi.useFakeTimers();
    });

    afterEach(() => {
        vi.runOnlyPendingTimers();
        vi.useRealTimers();
    });

    it('Devuelve sla-green si quedan más de 24 horas', () => {
        const future = new Date(Date.now() + 48 * 3600 * 1000).toISOString(); // +48h
        const { trafficColor, isAtRisk, isExpired } = useSlaTrafficLight(future);
        expect(trafficColor.value).toContain('bg-green');
        expect(isAtRisk.value).toBe(false);
        expect(isExpired.value).toBe(false);
    });

    it('Devuelve sla-yellow si quedan entre 1h y 24h (CA-24)', () => {
        const future = new Date(Date.now() + 5 * 3600 * 1000).toISOString(); // +5h
        const { trafficColor, isAtRisk } = useSlaTrafficLight(future);
        expect(trafficColor.value).toContain('bg-yellow');
        expect(isAtRisk.value).toBe(true);
    });

    it('Devuelve sla-red si queda menos de 1h (CA-25)', () => {
        const future = new Date(Date.now() + 30 * 60 * 1000).toISOString(); // +30min
        const { trafficColor, isExpired } = useSlaTrafficLight(future);
        expect(trafficColor.value).toContain('bg-red');
        expect(isExpired.value).toBe(false);
    });

    it('Devuelve expirado si ya pasó la fecha', () => {
        const past = new Date(Date.now() - 1000).toISOString();
        const { isExpired, trafficColor } = useSlaTrafficLight(past);
        expect(isExpired.value).toBe(true);
        expect(trafficColor.value).toContain('bg-red');
    });

    it('Devuelve gris si expirationDate es null', () => {
        const { trafficColor } = useSlaTrafficLight(null);
        expect(trafficColor.value).toContain('bg-gray');
    });
});
