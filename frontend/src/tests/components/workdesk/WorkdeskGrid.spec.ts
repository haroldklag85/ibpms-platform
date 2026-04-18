import { mount } from '@vue/test-utils';
import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { createTestingPinia } from '@pinia/testing';
import WorkdeskGrid from '@/components/workdesk/WorkdeskGrid.vue';
import { useWorkdeskStore } from '@/stores/useWorkdeskStore';

describe('WorkdeskGrid.vue (CA-11 a CA-15 Reclamar y Liberar)', () => {
    let pinia: any;

    beforeEach(() => {
        pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                auth: { user: { username: 'testuser' }, activeRole: 'ROLE_OPERADOR' }
            }
        });
        vi.spyOn(window, 'confirm').mockImplementation(() => true);
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it('Muestra botón "Reclamar" si la tarea está AVAILABLE', async () => {
        const tasks = [
            { unifiedId: 't-1', title: 'Generar Informe', status: 'AVAILABLE', assignee: null }
        ];

        const wrapper = mount(WorkdeskGrid, {
            global: { plugins: [pinia] },
            props: { tasks }
        });

        const btn = wrapper.find('button.text-indigo-600');
        expect(btn.exists()).toBe(true);
        expect(btn.text()).toBe('Reclamar');

        await btn.trigger('click');
        const store = useWorkdeskStore();
        expect(store.claimTask).toHaveBeenCalledWith('t-1');
    });

    it('Muestra botón "Liberar" si la tarea está ACTIVE y asignada al usuario actual, y emite unclaimTask', async () => {
        const tasks = [
            { unifiedId: 't-2', title: 'Facturacion', status: 'ACTIVE', assignee: 'testuser' }
        ];

        const wrapper = mount(WorkdeskGrid, {
            global: { plugins: [pinia] },
            props: { tasks }
        });

        const btn = wrapper.find('button.text-red-600');
        expect(btn.exists()).toBe(true);
        expect(btn.text()).toContain('Liberar (Unclaim)');

        await btn.trigger('click');
        expect(window.confirm).toHaveBeenCalled();
        
        const store = useWorkdeskStore();
        expect(store.unclaimTask).toHaveBeenCalledWith('t-2');
    });
});
