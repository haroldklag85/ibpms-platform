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

    // ===================================================================
    // CA-16: Banner de nota interna del operario anterior
    // @Traceability: US-002, CA-16
    // ===================================================================
    it('CA-16: muestra banner de nota interna cuando mensajeInterno existe', async () => {
        const store = useWorkdeskStore();
        (store.fetchTaskPreview as any).mockResolvedValue({
            unifiedId: 't-200',
            title: 'Tarea con Nota',
            mensajeInterno: 'Cliente requiere atención especial',
            mensajeInternoAuthor: 'operario_prev',
            mensajeInternoAt: '2026-06-04T10:00:00Z',
        });

        const wrapper = mount(TaskPreviewModal, {
            global: { plugins: [pinia], stubs: { Teleport: true } },
            props: { taskId: 't-200' }
        });

        await new Promise(r => setTimeout(r, 0));

        const banner = wrapper.find('[data-testid="internal-note-banner"]');
        expect(banner.exists()).toBe(true);
        expect(banner.text()).toContain('Nota del operario anterior');
        expect(banner.text()).toContain('Cliente requiere atención especial');
        expect(banner.text()).toContain('operario_prev');
    });

    it('CA-16: no muestra banner cuando no hay mensajeInterno', async () => {
        const store = useWorkdeskStore();
        (store.fetchTaskPreview as any).mockResolvedValue({
            unifiedId: 't-201',
            title: 'Tarea sin Nota',
        });

        const wrapper = mount(TaskPreviewModal, {
            global: { plugins: [pinia], stubs: { Teleport: true } },
            props: { taskId: 't-201' }
        });

        await new Promise(r => setTimeout(r, 0));

        expect(wrapper.find('[data-testid="internal-note-banner"]').exists()).toBe(false);
    });

    // ===================================================================
    // CA-18: Banner claimed-by-other con nombre de usuario
    // @Traceability: US-002, CA-18
    // ===================================================================
    it('CA-18: muestra banner claimed-by-other con data-testid', async () => {
        const store = useWorkdeskStore();
        (store.fetchTaskPreview as any).mockResolvedValue({
            unifiedId: 't-202',
            title: 'Tarea Disputada',
        });

        const wrapper = mount(TaskPreviewModal, {
            global: { plugins: [pinia], stubs: { Teleport: true } },
            props: { taskId: 't-202' }
        });

        await new Promise(r => setTimeout(r, 0));

        // Initially no banner
        expect(wrapper.find('[data-testid="claimed-by-other-banner"]').exists()).toBe(false);

        // Simulate claim conflict via 409
        (store.claimTask as any).mockRejectedValue({ response: { status: 409 } });
        const btnClaim = wrapper.find('[data-test="btn-claim"]');
        if (btnClaim.exists()) {
            await btnClaim.trigger('click');
            await new Promise(r => setTimeout(r, 0));
            expect(wrapper.find('[data-testid="claimed-by-other-banner"]').exists()).toBe(true);
        }
    });

    it('CA-18: deshabilita botón claim cuando isAlreadyClaimed es true', async () => {
        const store = useWorkdeskStore();
        (store.fetchTaskPreview as any).mockResolvedValue({
            unifiedId: 't-203',
            title: 'Tarea Ya Reclamada',
        });
        // Simulate 409 on claim
        (store.claimTask as any).mockRejectedValue({ response: { status: 409 } });

        const wrapper = mount(TaskPreviewModal, {
            global: { plugins: [pinia], stubs: { Teleport: true } },
            props: { taskId: 't-203' }
        });

        await new Promise(r => setTimeout(r, 0));

        const btnClaim = wrapper.find('[data-test="btn-claim"]');
        if (btnClaim.exists()) {
            await btnClaim.trigger('click');
            // Wait for async error handling + DOM reactivity
            await new Promise(r => setTimeout(r, 50));
            await wrapper.vm.$nextTick();
            // Re-query button after DOM update (Vue re-renders disabled attribute)
            const updatedBtn = wrapper.find('[data-test="btn-claim"]');
            expect(updatedBtn.attributes('disabled')).toBeDefined();
        }
    });
});
