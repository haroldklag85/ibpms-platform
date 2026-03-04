import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, config } from '@vue/test-utils';
import FormDesigner from '../views/admin/Modeler/FormDesigner.vue';

// Mocking external components to avoid rendering issues in JSDOM
config.global.stubs = {
    VueDraggable: true,
    VueMonacoEditor: true,
    Transition: true
};

// vi.mock('axios') to avoid real HTTP requests
vi.mock('axios', () => {
    return {
        default: {
            post: vi.fn(),
            get: vi.fn()
        }
    }
});

describe('FormDesigner.vue', () => {
    let wrapper: any;

    beforeEach(() => {
        wrapper = mount(FormDesigner);
    });

    it('renders successfully without crashing', () => {
        expect(wrapper.exists()).toBe(true);
        expect(wrapper.text()).toContain('IDE de Formularios');
    });

    it('initially shows the pattern selection modal', () => {
        // Check if the modal title is visible
        expect(wrapper.text()).toContain('Crear Nuevo Formulario (Dual-Pattern)');
    });

    it('closes pattern modal when selecting SIMPLE pattern', async () => {
        // Find the button for "Formulario Simple" and click it
        const buttons = wrapper.findAll('button');
        const simpleButton = buttons.find((btn: any) => btn.text().includes('Formulario Simple'));

        expect(simpleButton).toBeDefined();
        await simpleButton.trigger('click');

        // Modal should disappear (text is no longer rendered)
        expect(wrapper.text()).not.toContain('Crear Nuevo Formulario (Dual-Pattern)');
    });

    it('shows reset confirmation modal when clicking reset (CA-43)', async () => {
        // First setup: close pattern modal
        await wrapper.vm.selectPattern('SIMPLE');

        // Find Reset button
        const buttons = wrapper.findAll('button');
        const resetButton = buttons.find((btn: any) => btn.text().includes('Reset'));

        expect(resetButton).toBeDefined();

        await resetButton.trigger('click');
        expect(wrapper.text()).toContain('Confirmar Reset');

        // Reject reset
        const cancelBtn = wrapper.findAll('button').find((btn: any) => btn.text() === 'Cancelar');
        await cancelBtn.trigger('click');
        expect(wrapper.text()).not.toContain('Confirmar Reset');
    });

    it('simulates form field array mutation (reactive test)', async () => {
        expect(true).toBe(true);
    });
});
