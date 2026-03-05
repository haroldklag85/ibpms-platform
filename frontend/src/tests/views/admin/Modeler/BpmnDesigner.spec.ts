import { mount } from '@vue/test-utils';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import BpmnDesigner from '@/views/admin/Modeler/BpmnDesigner.vue';

// Mocking bpmn-js dynamic imports to completely avoid JSDOM SVG canvas failures
vi.mock('bpmn-js/lib/Modeler', () => {
    return {
        default: class MockModeler {
            constructor() { }
            importXML = vi.fn().mockResolvedValue({ warnings: [] });
            saveXML = vi.fn().mockResolvedValue({ xml: '<xml/>' });
            get = vi.fn().mockReturnValue({ zoom: vi.fn(), open: vi.fn() });
            on = vi.fn();
            destroy = vi.fn();
        }
    };
});
vi.mock('diagram-js-minimap', () => ({ default: {} }));

describe('BpmnDesigner.vue', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('renders the BpmnDesigner correctly with its canvas area', async () => {
        const wrapper = mount(BpmnDesigner);

        // Allow onMounted to run
        await wrapper.vm.$nextTick();

        // Canvas element should exist
        const canvas = wrapper.find('.bpmn-canvas');
        expect(canvas.exists()).toBe(true);

        // Top Toolbar elements should exist
        expect(wrapper.text()).toContain('Crédito de Consumo V1');
        expect(wrapper.text()).toContain('Catálogo');
    });

    it('simulates clicking "Desplegar" and opens deployment modal', async () => {
        const wrapper = mount(BpmnDesigner);
        await wrapper.vm.$nextTick();

        // The userRole is initially 'RELEASE_MANAGER', so 'Desplegar' should exist
        const buttons = wrapper.findAll('button');
        const deployBtn = buttons.find(b => b.text().includes('Desplegar'));

        expect(deployBtn).toBeDefined();
        await deployBtn!.trigger('click');

        // Expect the Modal to show up
        expect(wrapper.text()).toContain('Confirmar Despliegue');
    });

    it('shows AI Copilot panel when Copilot button is clicked', async () => {
        const wrapper = mount(BpmnDesigner);
        await wrapper.vm.$nextTick();

        const buttons = wrapper.findAll('button');
        const copilotBtn = buttons.find(b => b.text().includes('Copiloto IA'));

        expect(copilotBtn).toBeDefined();
        await copilotBtn!.trigger('click');

        // AI Copilot panel should become visible
        expect(wrapper.text()).toContain('Auditoría ISO 9001');
    });
});
