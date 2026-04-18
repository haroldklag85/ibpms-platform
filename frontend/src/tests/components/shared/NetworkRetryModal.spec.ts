import { mount } from '@vue/test-utils';
import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { createTestingPinia } from '@pinia/testing';
import NetworkRetryModal from '@/components/NetworkRetryModal.vue';
import { useFormStore } from '@/stores/useFormStore';

describe('NetworkRetryModal.vue (CA-31, CA-32)', () => {
    let pinia: any;

    beforeEach(() => {
        pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                formStore: { requiresRetry: true, retryCount: 2, idempotencyKey: 'xyz-123' }
            }
        });
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it('Renderiza cuando requiresRetry es true e incluye la clave de idempotencia', () => {
        const wrapper = mount(NetworkRetryModal, { global: { plugins: [pinia] } });
        expect(wrapper.find('#network-retry-modal').exists()).toBe(true);
        expect(wrapper.text()).toContain('xyz-123');
        expect(wrapper.text()).toContain('2 de 3');
    });

    it('Oculta el modal y reset state cuando se hace click en Cancelar', async () => {
        const wrapper = mount(NetworkRetryModal, { global: { plugins: [pinia] } });
        const store = useFormStore();

        await wrapper.findAll('button')[0].trigger('click'); // Cancelar
        expect(store.requiresRetry).toBe(false);
    });

    it('Despacha evento network-retry-dispatch cuando se hace click en Reintentar', async () => {
        const wrapper = mount(NetworkRetryModal, { global: { plugins: [pinia] } });
        vi.spyOn(window, 'dispatchEvent');

        await wrapper.findAll('button')[1].trigger('click'); // Reintentar
        expect(window.dispatchEvent).toHaveBeenCalledWith(expect.any(CustomEvent));
        expect((window.dispatchEvent as any).mock.calls[0][0].type).toBe('network-retry-dispatch');
    });

    it('El modal NO renderiza si requiresRetry es false', () => {
        const piniaHidden = createTestingPinia({
            createSpy: vi.fn,
            initialState: { formStore: { requiresRetry: false } }
        });
        const wrapper = mount(NetworkRetryModal, { global: { plugins: [piniaHidden] } });
        expect(wrapper.find('#network-retry-modal').exists()).toBe(false);
    });
});
