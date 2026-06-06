import { mount } from '@vue/test-utils';
import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { createTestingPinia } from '@pinia/testing';
import TaskFormSubmit from '@/components/forms/TaskFormSubmit.vue';
import { useFormStore } from '@/stores/useFormStore';
import { z } from 'zod';

describe('TaskFormSubmit.vue (CA-21 a CA-30)', () => {
    let pinia: any;

    beforeEach(() => {
        pinia = createTestingPinia({
            createSpy: vi.fn,
            stubActions: false
        });
        vi.useFakeTimers();
    });

    afterEach(() => {
        vi.runOnlyPendingTimers();
        vi.useRealTimers();
    });

    it('Valida formulario vacío e inyecta errores visuales (CA-21)', async () => {
        const schema = z.object({ doc: z.string().min(1, 'Required') });
        const wrapper = mount(TaskFormSubmit, {
            global: { plugins: [pinia] },
            props: { taskId: 't-1', schema }
        });

        // Click submit
        await wrapper.find('button').trigger('click');

        // Renderiza el banner de errores reactivamente
        expect(wrapper.html()).toContain('Errores de Validación:');
        expect(wrapper.html()).toContain('Required');
    });

    it('Muestra botón de Soft-Undo si submit pasa Zod con delay de 5s (CA-29)', async () => {
        const schema = z.object({ field: z.string().optional() });
        const wrapper = mount(TaskFormSubmit, {
            global: { plugins: [pinia] },
            props: { taskId: 't-2', schema }
        });

        const store = useFormStore();
        store.setFormData({ field: 'data' }); // Rellena valid data

        // Submits
        await wrapper.find('button').trigger('click');
        
        // Deberia mostrar el Soft Undo banner (ya que TDD de store dice formStore.undoTimeLeft=5 inicialmente)
        expect(store.isUndoAvailable).toBe(true);
        expect(wrapper.html()).toContain('Deshacer Envío (Soft-Undo)');
        
        // El boton submit desaparece / se desactiva
        const submitBtn = wrapper.findAll('button').filter(b => b.text().includes('Completar Tarea'));
        if(submitBtn.length > 0) {
           expect(submitBtn[0].attributes('disabled')).toBeDefined();
        }

        // Hacemos el Undo
        await wrapper.find('button.bg-yellow-500').trigger('click');
        expect(store.isUndoAvailable).toBe(false);
        expect(wrapper.html()).not.toContain('Deshacer Envío');
    });
});
