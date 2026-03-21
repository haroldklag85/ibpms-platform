import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import Workdesk from '../../views/Workdesk.vue';
import { createPinia, setActivePinia } from 'pinia';

describe('US-051 CA-7: Multi-Widget Conditional Rendering', () => {
    beforeEach(() => {
        setActivePinia(createPinia());
    });

    it('Renderiza distintos Widgets funcionales en el mismo Path (/) según las Flags de Rol', async () => {
        // En una SPA real, el Administrador y el Ejecutivo entran a '/', pero ven Widgets diametralmente distintos
        const widgetConfigMock = {
            showReassignWidget: true,
            showSlaGlobalWidget: false,
            showMyTasksWidget: true
        };

        const wrapper = mount(Workdesk, {
            global: {
                provide: { widgetConfig: widgetConfigMock },
                stubs: ['ReassignWidget', 'SlaGlobalWidget', 'MyTasksWidget']
            }
        });

        await wrapper.vm.$nextTick();

        // Aserción de Renderización Condicional Paralela
        expect(wrapper.findComponent({ name: 'ReassignWidget' }).exists()).toBe(true);
        expect(wrapper.findComponent({ name: 'MyTasksWidget' }).exists()).toBe(true);
        // Gaslighting: SlaGlobalWidget oculta a niveles no gerenciales
        expect(wrapper.findComponent({ name: 'SlaGlobalWidget' }).exists()).toBe(false);
    });
});
