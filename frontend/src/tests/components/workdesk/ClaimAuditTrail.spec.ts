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
        // CA-20: Enriched labels replace raw action strings
        expect(html).toContain('Reclamada voluntariamente');   // was 'CLAIM'
        expect(html).toContain('Despojada por supervisor');     // was 'FORCE_UNCLAIM'
    });

    // ===================================================================
    // CA-20: Timeline enriquecido con iconos color-coded
    // @Traceability: US-002, CA-20
    // ===================================================================
    it('CA-20: renderiza los 6 action types nuevos con labels legibles', async () => {
        const store = useWorkdeskStore();
        (store.fetchAuditTrail as any).mockResolvedValue([
            { id: 10, action: 'CLAIMED', actor: 'user1', timestamp: '2026-06-01T08:00:00Z', reason: null },
            { id: 11, action: 'RELEASED', actor: 'user1', timestamp: '2026-06-01T09:00:00Z', reason: 'Turno finalizado' },
            { id: 12, action: 'FORCE_UNCLAIMED', actor: 'admin', timestamp: '2026-06-01T10:00:00Z', reason: 'Reasignación' },
            { id: 13, action: 'AUTO_UNCLAIMED', actor: 'system', timestamp: '2026-06-01T11:00:00Z', reason: 'Inactividad 30 min' },
            { id: 14, action: 'TIMEOUT_EXTENDED', actor: 'user1', timestamp: '2026-06-01T12:00:00Z', reason: null },
            { id: 15, action: 'BULK_CLAIMED', actor: 'supervisor', timestamp: '2026-06-01T13:00:00Z', reason: null },
        ]);

        const wrapper = mount(ClaimAuditTrail, {
            global: { plugins: [pinia] },
            props: { taskId: 't-300' }
        });

        await new Promise(r => setTimeout(r, 0));

        const html = wrapper.html();
        expect(html).toContain('Reclamada voluntariamente');
        expect(html).toContain('Liberada por el operario');
        expect(html).toContain('Despojada por supervisor');
        expect(html).toContain('Liberada por inactividad');
        expect(html).toContain('Tiempo extendido');
        expect(html).toContain('Reclamada en lote');
    });

    it('CA-20: muestra label correcto para AUTO_UNCLAIMED', async () => {
        const store = useWorkdeskStore();
        (store.fetchAuditTrail as any).mockResolvedValue([
            { id: 20, action: 'AUTO_UNCLAIMED', actor: 'system', timestamp: '2026-06-01T11:00:00Z', reason: 'Inactividad' },
        ]);

        const wrapper = mount(ClaimAuditTrail, {
            global: { plugins: [pinia] },
            props: { taskId: 't-301' }
        });

        await new Promise(r => setTimeout(r, 0));
        expect(wrapper.html()).toContain('Liberada por inactividad');
        expect(wrapper.html()).toContain('🔴');
    });

    it('CA-20: muestra label correcto para BULK_CLAIMED', async () => {
        const store = useWorkdeskStore();
        (store.fetchAuditTrail as any).mockResolvedValue([
            { id: 21, action: 'BULK_CLAIMED', actor: 'supervisor', timestamp: '2026-06-01T13:00:00Z', reason: null },
        ]);

        const wrapper = mount(ClaimAuditTrail, {
            global: { plugins: [pinia] },
            props: { taskId: 't-302' }
        });

        await new Promise(r => setTimeout(r, 0));
        expect(wrapper.html()).toContain('Reclamada en lote');
        expect(wrapper.html()).toContain('📦');
    });
});
