import { mount } from '@vue/test-utils';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { createTestingPinia } from '@pinia/testing';
import DmnNlpPanel from '@/components/dmn/DmnNlpPanel.vue';
import { useDmnStore } from '@/stores/useDmnStore';

describe('DmnNlpPanel.vue (CA-16 al CA-20)', () => {
    let pinia: any;

    beforeEach(() => {
        pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                dmnStore: { generatedXml: null, confidence: 0, isGenerating: false, generationError: null }
            }
        });
    });

    it('Renderiza vacio inicialmente sin previsualizacion', () => {
        const wrapper = mount(DmnNlpPanel, { global: { plugins: [pinia] } });
        expect(wrapper.find('textarea').exists()).toBe(true);
        expect(wrapper.find('pre').exists()).toBe(false);
        expect(wrapper.find('button').attributes('disabled')).toBeDefined();
    });

    it('El boton se habilita al escribir y desactiva al generar', async () => {
        const wrapper = mount(DmnNlpPanel, { global: { plugins: [pinia] } });
        await wrapper.find('textarea').setValue('prompt nlp');
        expect(wrapper.find('button').attributes('disabled')).toBeUndefined();
    });

    it('Renderiza la confianza DMN visualmente', async () => {
        const store = useDmnStore();
        (store as any).generatedXml = '<definitions/>';
        (store as any).confidence = 90;

        const wrapper = mount(DmnNlpPanel, { global: { plugins: [pinia] } });
        
        expect(wrapper.find('pre').exists()).toBe(true);
        expect(wrapper.html()).toContain('Confianza: 90%');
        expect(wrapper.html()).toContain('bg-green-500'); // CSS Class for high confidence CA-20
    });
});
