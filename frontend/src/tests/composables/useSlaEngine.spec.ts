import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { useSlaEngine, UrgencyType } from '@/composables/useSlaEngine';

describe('SLA Engine - Matemáticas de Urgencia (US-001/043)', () => {
    beforeEach(() => {
        vi.useFakeTimers();
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    it('debería retornar BLACK cuando la fecha actual supera el SLA Expiration', () => {
        const { calculateUrgency } = useSlaEngine();
        const start = new Date('2026-01-01T10:00:00Z').toISOString();
        const expir = new Date('2026-01-01T12:00:00Z').toISOString();
        const artificialNow = new Date('2026-01-01T12:01:00Z').getTime();

        const result = calculateUrgency(expir, start, artificialNow);
        expect(result).toBe(UrgencyType.BLACK);
    });

    it('debería calcular RED (Urgencia Crítica) cuando resta <= 15% del tiempo total de la tarea', () => {
        const { calculateUrgency } = useSlaEngine();
        const start = new Date('2026-01-01T10:00:00Z').toISOString();
        const expir = new Date('2026-01-01T12:00:00Z').toISOString(); // Total: 120 min
        
        // Faltan 10 min (Artificial Now -> 11:50)
        // Porcentaje = 10 / 120 = 8.33% -> RED
        const artificialNow = new Date('2026-01-01T11:50:00Z').getTime();

        const result = calculateUrgency(expir, start, artificialNow);
        expect(result).toBe(UrgencyType.RED);
    });

    it('debería calcular GREEN cuando resta más del 70% del tiempo', () => {
        const { calculateUrgency } = useSlaEngine();
        const start = new Date('2026-01-01T10:00:00Z').toISOString();
        const expir = new Date('2026-01-01T12:00:00Z').toISOString(); 
        
        // Ha pasado 10 min -> Quedan 110 min => 110 / 120 = 91% -> GREEN
        const artificialNow = new Date('2026-01-01T10:10:00Z').getTime();

        const result = calculateUrgency(expir, start, artificialNow);
        expect(result).toBe(UrgencyType.GREEN);
    });
});
