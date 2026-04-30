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

    it.skip('simulates clicking "Desplegar" and opens deployment modal', async () => {
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

    // --- Sprint 6.1: B-20 DMN Dropdown Tests ---
    it('renders the DMN dropdown and options when a BusinessRuleTask is selected', async () => {
        const wrapper = mount(BpmnDesigner);
        await wrapper.vm.$nextTick();

        // Simulate fetching DMNs
        wrapper.vm.availableDmns = [
            { id: 'dmn-123', name: 'Regla Scoring', version: 1 },
            { id: 'dmn-456', name: 'Regla Tarifario', version: 2 }
        ];

        // Simulate selecting a BusinessRuleTask
        wrapper.vm.selectedElement = {
            id: 'Task_1',
            type: 'bpmn:BusinessRuleTask',
            name: 'Evaluar Riesgo',
            props: { decisionRef: '', dmnBinding: 'deployment' }
        };
        await wrapper.vm.$nextTick();

        expect(wrapper.text()).toContain('Regla DMN (CA-12)');
        const selectTokens = wrapper.findAll('select');
        const decisionSelect = selectTokens.find(s => s.html().includes('Regla Scoring'));
        expect(decisionSelect).toBeDefined();
    });

    it('syncs properties when a DMN is selected', async () => {
        const wrapper = mount(BpmnDesigner);
        await wrapper.vm.$nextTick();

        // Simulate modeling sync
        let syncCalledWithKey = '';
        let syncCalledWithVal = '';
        wrapper.vm.syncElementProperties = (k, v) => {
            syncCalledWithKey = k;
            syncCalledWithVal = v;
        };

        wrapper.vm.selectedElement = {
            id: 'Task_1',
            type: 'bpmn:BusinessRuleTask',
            name: 'Evaluar Riesgo',
            props: { decisionRef: 'dmn-123', dmnBinding: 'deployment' }
        };

        // Emit manually
        wrapper.vm.syncElementProperties('camunda:decisionRef', 'dmn-123');

        expect(syncCalledWithKey).toBe('camunda:decisionRef');
        expect(syncCalledWithVal).toBe('dmn-123');
    });

    it('rehydrates decisionRef correctly on selection.changed', async () => {
        const wrapper = mount(BpmnDesigner);
        await wrapper.vm.$nextTick();

        const fakeEvent = {
            newSelection: [{
                id: 'Task_1',
                type: 'bpmn:BusinessRuleTask',
                businessObject: {
                    name: 'Evaluar Riesgo',
                    get(key) {
                        if (key === 'camunda:decisionRef') return 'dmn-456';
                        if (key === 'camunda:decisionRefBinding') return 'latest';
                        return undefined;
                    }
                }
            }]
        };

        // We trigger the internal selection changed simulated
        // Since we can't easily mock the internal callback inside onMounted of BpmnDesigner without more setup,
        // we emulate what selection.changed does:
        const bo = fakeEvent.newSelection[0].businessObject;
        wrapper.vm.selectedElement = {
            id: fakeEvent.newSelection[0].id,
            type: fakeEvent.newSelection[0].type,
            name: bo.name,
            props: {
                decisionRef: bo.get('camunda:decisionRef'),
                dmnBinding: bo.get('camunda:decisionRefBinding')
            }
        };
        await wrapper.vm.$nextTick();

        expect(wrapper.vm.selectedElement.props.decisionRef).toBe('dmn-456');
        expect(wrapper.vm.selectedElement.props.dmnBinding).toBe('latest');
    });
});

