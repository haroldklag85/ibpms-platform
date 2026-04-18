import { mount } from '@vue/test-utils';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { createTestingPinia } from '@pinia/testing';
import DynamicRoleCards from '@/components/agile/DynamicRoleCards.vue';
import SkeletonCard from '@/components/agile/SkeletonCard.vue';
import { useAuthStore } from '@/stores/authStore';

describe('DynamicRoleCards.vue (CA-09 al CA-14)', () => {
    let pinia: any;

    beforeEach(() => {
        pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                auth: { activeRole: null, roles: [] }
            }
        });
    });

    it('Renderiza SkeletonCard si isLoading es true (CA-15)', () => {
        const wrapper = mount(DynamicRoleCards, {
            global: { plugins: [pinia] },
            props: { isLoading: true, cards: [] }
        });
        
        expect(wrapper.findComponent(SkeletonCard).exists()).toBe(true);
        expect(wrapper.find('.real-card').exists()).toBe(false);
    });

    it('Aplica borrado V-IF atómico (CA-11): no renderiza tarjeta si rol no coincide', () => {
        const authStore = useAuthStore();
        (authStore as any).activeRole = 'ROLE_OPERADOR';

        const mockCards = [
            { id: 1, title: 'Gestión BD', requiredRole: 'ROLE_DBA' }
        ];

        const wrapper = mount(DynamicRoleCards, {
            global: { plugins: [pinia] },
            props: { isLoading: false, cards: mockCards }
        });

        // La tarjeta de DBA no debe existir en el DOM
        expect(wrapper.find('.real-card').exists()).toBe(false);
        expect(wrapper.html()).not.toContain('Gestión BD');
    });

    it('Renderiza tarjeta si rol coincide o está permitido (CA-09)', () => {
        const authStore = useAuthStore();
        (authStore as any).activeRole = 'ROLE_DBA';

        const mockCards = [
            { id: 1, title: 'Gestión BD', requiredRole: 'ROLE_DBA' }
        ];

        const wrapper = mount(DynamicRoleCards, {
            global: { plugins: [pinia] },
            props: { isLoading: false, cards: mockCards }
        });

        expect(wrapper.find('.real-card').exists()).toBe(true);
        expect(wrapper.html()).toContain('Gestión BD');
    });
});
