import { mount } from '@vue/test-utils';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { createTestingPinia } from '@pinia/testing';
import TaskPreviewModal from '@/components/workdesk/TaskPreviewModal.vue';
import { useWorkdeskStore } from '@/stores/useWorkdeskStore';

describe('TaskPreviewModal.vue (CA-5)', () => {
    let pinia: any;

    beforeEach(() => {
        pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                workdesk: {
                    // Pre-fill state if needed
                }
            }
        });
    });

    it('No renderiza si no se pasa taskId', () => {
        const wrapper = mount(TaskPreviewModal, {
            global: { plugins: [pinia] },
            props: { taskId: null }
        });
        expect(wrapper.find('.modal-content').exists()).toBe(false);
    });

    it('Llama a fetchTaskPreview al montarse o al recibir taskId', async () => {
        const wrapper = mount(TaskPreviewModal, {
            global: { plugins: [pinia] },
            props: { taskId: 't-123' }
        });
        const store = useWorkdeskStore();
        expect(store.fetchTaskPreview).toHaveBeenCalledWith('t-123');
    });

    it('Muestra los datos Read-Only y el botón Reclamar, y emite al reclamar', async () => {
        const store = useWorkdeskStore();
        (store.fetchTaskPreview as any).mockResolvedValue({
            unifiedId: 't-123',
            title: 'Revision Doc',
            description: 'Aprobar anexo',
            slaExpirationDate: '2026-12-31',
            typeBadge: '⚡ Flujo',
            candidateGroup: 'GROUP_LEGAL'
        });

        const wrapper = mount(TaskPreviewModal, {
            global: { plugins: [pinia], stubs: { Teleport: true } },
            props: { taskId: 't-123' }
        });

        // Simular resolución promesas
        await new Promise(r => setTimeout(r, 0));
        
        expect(wrapper.html()).toContain('Revision Doc');
        expect(wrapper.html()).toContain('GROUP_LEGAL');

        const btnClaim = wrapper.find('[data-test="btn-claim"]');
        expect(btnClaim.exists()).toBe(true);

        await btnClaim.trigger('click');
        expect(store.claimTask).toHaveBeenCalledWith('t-123');
        expect(wrapper.emitted('close')).toBeTruthy();
    });
});
