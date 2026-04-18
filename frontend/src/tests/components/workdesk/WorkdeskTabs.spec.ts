import { mount } from '@vue/test-utils';
import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { createTestingPinia } from '@pinia/testing';
import WorkdeskTabs from '@/components/WorkdeskTabs.vue';
import { useWorkdeskStore } from '@/stores/useWorkdeskStore';

describe('WorkdeskTabs.vue (CA-22)', () => {
    let pinia: any;

    beforeEach(() => {
        pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                workdeskStore: { activeView: 'PERSONAL' }
            }
        });
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it('Renderiza las pestañas y muestra PERSONAL como activo por defecto', () => {
        const wrapper = mount(WorkdeskTabs, { global: { plugins: [pinia] } });
        
        const personalBtn = wrapper.findAll('button').at(0);
        expect(personalBtn?.text()).toContain('Mi Bandeja');
        expect(personalBtn?.classes()).toContain('border-blue-600');
    });

    it('Cambia la vista a POOL al hacer click y dispara setActiveView', async () => {
        const wrapper = mount(WorkdeskTabs, { global: { plugins: [pinia] } });
        const store = useWorkdeskStore();
        
        const poolBtn = wrapper.findAll('button').at(1);
        await poolBtn?.trigger('click');
        
        expect(store.setActiveView).toHaveBeenCalledWith('POOL');
    });
});
