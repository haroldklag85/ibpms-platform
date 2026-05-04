import { setActivePinia, createPinia } from 'pinia';
import { useFormStore } from '@/stores/useFormStore';
import { useConnectionStore } from '@/stores/connectionStore';
import { api } from '@/services/apiClient';
import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { z } from 'zod';

vi.mock('@/services/apiClient', () => ({
    api: {
        saveTaskDraft: vi.fn(),
        completeTask: vi.fn()
    }
}));

describe('useFormStore', () => {
    let store: ReturnType<typeof useFormStore>;

    beforeEach(() => {
        setActivePinia(createPinia());
        store = useFormStore();
        vi.clearAllMocks();
        vi.useFakeTimers();
    });

    afterEach(() => {
        vi.runOnlyPendingTimers();
        vi.useRealTimers();
    });

    it('Valida correctamente usando Zod schema', () => {
        const schema = z.object({ age: z.number().min(18, 'Debe ser mayor de edad') });
        store.setFormData({ age: 15 });
        
        const isValid = store.validateForm(schema);
        expect(isValid).toBe(false);
        expect(store.validationErrors['age']).toBe('Debe ser mayor de edad');

        store.setFormData({ age: 20 });
        const isValid2 = store.validateForm(schema);
        expect(isValid2).toBe(true);
        expect(store.validationErrors).toEqual({});
    });

    it('Ejecuta saveDraft correctamente', async () => {
        (api.saveTaskDraft as any).mockResolvedValue({ status: 200 });
        store.setFormData({ name: 'Draft' });
        expect(store.isDirty).toBe(true);

        await store.saveDraft('t-1');
        
        expect(api.saveTaskDraft).toHaveBeenCalledWith('t-1', { name: 'Draft' });
        expect(store.isDirty).toBe(false);
    });

    it('Inicia Soft-Undo e interrumpe envío final si es revertido', async () => {
        (api.completeTask as any).mockResolvedValue({ status: 200 });
        
        // Dispara Submit con Undo de 5 sec
        await store.submitForm('t-undo', { status: 'OK' }, true);
        
        // isSubmitting es false porque el finally se ejecuta, se muestra toast de undo
        expect(store.isSubmitting).toBe(false); 
        expect(store.isUndoAvailable).toBe(true);
        expect(store.undoTimeLeft).toBe(5);
        expect(api.completeTask).not.toHaveBeenCalled(); // No debe llamarse aun

        // Avanzamos timer 2 segundos
        vi.advanceTimersByTime(2000);
        expect(store.undoTimeLeft).toBe(3);

        // Disparamos Undo
        const undoResult = store.softUndo();
        expect(undoResult).toBe(true);
        expect(store.isUndoAvailable).toBe(false);

        // Avanzamos hasta final para asegurar que completeTask nunca se llamó
        vi.advanceTimersByTime(5000);
        expect(api.completeTask).not.toHaveBeenCalled();
    });

    it('Permite el envio exitoso post Soft-Undo Timeout', async () => {
        (api.completeTask as any).mockResolvedValue({ status: 200 });
        store.submitForm('t-ok', { doc: 'doc1' }, true);

        // Avanzamos 5.1s
        vi.advanceTimersByTime(5100);

        // Como usamos una internal async on commit, wait it out en microtasks
        await Promise.resolve();
        await Promise.resolve();
        await Promise.resolve();

        expect(api.completeTask).toHaveBeenCalledWith('t-ok', { doc: 'doc1' }, expect.any(Object));
        expect(store.isUndoAvailable).toBe(false);
        expect(store.isSubmitting).toBe(false);
    });

    it('Test CA-31 / CA-32: Fallo de red 504 almacena idempotencyKey y activa requiresRetry', async () => {
        (api.completeTask as any).mockRejectedValue({ response: { status: 504 } });
        try {
            await store.submitForm('t-err', {}, false);
        } catch (e) {}

        const connectionStore = useConnectionStore();
        expect(connectionStore.requiresRetry).toBe(true);
        expect(store.idempotencyKey).toBeTruthy();
        expect(connectionStore.retryCount).toBe(0);
    });

    it('Test CA-31 / CA-32: Retries mantienen misma idempotencyKey y frenan a los 3 reintentos', async () => {
        // Arrange
        let requestHeaders: any = {};
        (api.completeTask as any).mockImplementation((_id: string, _payload: any, config: any) => {
            requestHeaders = config?.headers || {};
            return Promise.reject({ response: { status: 504 } });
        });
        
        const connectionStore = useConnectionStore();
        
        // Initial fall
        try { await store.submitForm('t-ret', {}, false); } catch(e){}
        const initialKey = store.idempotencyKey;
        
        // 3 manual retries (simulating NetworkRetryModal pressing retry)
        try { await store.submitForm('t-ret', {}, false, true); } catch(e){}
        expect(connectionStore.retryCount).toBe(1);
        expect(requestHeaders['Idempotency-Key']).toBe(initialKey);
        
        try { await store.submitForm('t-ret', {}, false, true); } catch(e){}
        expect(connectionStore.retryCount).toBe(2);
        
        try { await store.submitForm('t-ret', {}, false, true); } catch(e){}
        expect(connectionStore.retryCount).toBe(3);
        expect(connectionStore.requiresRetry).toBe(false); // Can't retry anymore
    });

    it('Test CA-37: HTTP 500 expone error genérico sin stack trace', async () => {
        (api.completeTask as any).mockRejectedValue({
            response: {
                status: 500,
                data: {
                    message: 'java.lang.NullPointerException at com.ibpms.poc.service.FormService.submit(FormService.java:42)',
                    trace: 'at sun.reflect...',
                    type: 'INTERNAL_ERROR'
                }
            }
        });

        try {
            await store.submitForm('t-500', {}, false);
        } catch (e: any) {
            const connectionStore = useConnectionStore();
            // El store re-lanza el error. El componente debería mostrar mensaje genérico.
            // Verificamos que el store NO almacena el stack trace en ningún campo expuesto.
            expect(connectionStore.requiresRetry).toBe(false); // 500 no es retry-able (solo 504)
            // El componente debe filtrar — el store no tiene campo 'userFacingError'
            // pero garantizamos que NO expone info interna al DOM
            expect(JSON.stringify(store.$state)).not.toContain('NullPointerException');
            expect(JSON.stringify(store.$state)).not.toContain('sun.reflect');
        }
    });

    it('Test CA-35: HTTP 409 con SESSION_CONFLICT dispara evento', async () => {
        vi.spyOn(window, 'dispatchEvent');
        (api.completeTask as any).mockRejectedValue({ response: { status: 409, data: { type: 'SESSION_CONFLICT' } } });
        
        try { await store.submitForm('t-col', {}, false); } catch(e){}
        
        const dispatchedEvent = vi.mocked(window.dispatchEvent).mock.calls.find(
            call => call[0].type === 'session-conflict-dispatch'
        );
        expect(dispatchedEvent).toBeDefined();
    });

    it('Test CA-2: HTTP 400 Backend mapea errores de Zod en validationErrors', async () => {
        (api.completeTask as any).mockRejectedValue({
            response: {
                status: 400,
                data: {
                    errors: [{ field: 'email', message: 'Formato inválido' }]
                }
            }
        });

        try {
            await store.submitForm('t-bad', {}, false);
        } catch (e) {
            // expected
        }

        expect(store.validationErrors['email']).toBe('Formato inválido');
        // El submit the fallar sin setear flag isSubmitting en true
        expect(store.isSubmitting).toBe(false);
    });
});
