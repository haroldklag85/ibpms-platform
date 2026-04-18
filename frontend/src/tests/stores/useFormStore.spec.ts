import { setActivePinia, createPinia } from 'pinia';
import { useFormStore } from '@/stores/useFormStore';
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

        // Como usamos una internal async on commit, wait it out
        await Promise.resolve();

        expect(api.completeTask).toHaveBeenCalledWith('t-ok', { doc: 'doc1' });
        expect(store.isUndoAvailable).toBe(false);
        expect(store.isSubmitting).toBe(false);
    });
});
