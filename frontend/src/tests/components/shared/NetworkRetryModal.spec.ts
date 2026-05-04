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

    it('is deprecated and renders an empty div', () => {
        const wrapper = mount(NetworkRetryModal, { global: { plugins: [pinia] } });
        expect(wrapper.find('#network-retry-modal').exists()).toBe(false);
        expect(wrapper.html()).toContain('display: none');
    });
});
