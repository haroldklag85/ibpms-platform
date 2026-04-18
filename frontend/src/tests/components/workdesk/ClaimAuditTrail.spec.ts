import { mount } from '@vue/test-utils';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { createTestingPinia } from '@pinia/testing';
import ClaimAuditTrail from '@/components/workdesk/ClaimAuditTrail.vue';
import { useWorkdeskStore } from '@/stores/useWorkdeskStore';

describe('ClaimAuditTrail.vue (CA-9)', () => {
    let pinia: any;

    beforeEach(() => {
        pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {}
        });
    });

    it('Renderiza lista vacia o loader mientras busca datos', () => {
        const wrapper = mount(ClaimAuditTrail, {
            global: { plugins: [pinia] },
            props: { taskId: 't-123' }
        });
        const store = useWorkdeskStore();
        expect(store.fetchAuditTrail).toHaveBeenCalledWith('t-123');
        expect(wrapper.find('.timeline').exists()).toBe(true); // O asercion de loader
    });

    it('Mapea eventos de auditoría (CLAIM / UNCLAIM / FORCE_UNCLAIM) a la línea de tiempo temporal', async () => {
        const store = useWorkdeskStore();
        (store.fetchAuditTrail as any).mockResolvedValue([
            { id: 1, action: 'CLAIM', actor: 'userA', timestamp: '2026-04-18T10:00:00Z', reason: null },
            { id: 2, action: 'FORCE_UNCLAIM', actor: 'admin', timestamp: '2026-04-18T11:00:00Z', reason: 'Reasignación Urgente' }
        ]);

        const wrapper = mount(ClaimAuditTrail, {
            global: { plugins: [pinia] },
            props: { taskId: 't-123' }
        });

        await new Promise(r => setTimeout(r, 0));

        const html = wrapper.html();
        expect(html).toContain('userA');
        expect(html).toContain('admin');
        expect(html).toContain('Reasignación Urgente');
        expect(html).toContain('CLAIM');
        expect(html).toContain('FORCE_UNCLAIM');
    });
});
