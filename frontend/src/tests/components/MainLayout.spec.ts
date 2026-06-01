import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import MainLayout from '@/layouts/MainLayout.vue';
import { createPinia, setActivePinia } from 'pinia';
import { createRouter, createWebHistory } from 'vue-router';
import { useMenuStore } from '@/stores/useMenuStore';
import apiClient from '@/services/apiClient';

vi.mock('@/services/apiClient', () => {
  return {
    default: {
      get: vi.fn(),
    }
  };
});

describe('US-051 CA-6: MainLayout Renderizado Dinámico', () => {
    let router: any;

    beforeEach(() => {
        setActivePinia(createPinia());
        router = createRouter({
            history: createWebHistory(),
            routes: [
                { path: '/', component: { template: '<div>Dashboard</div>' } },
                { path: '/workdesk', component: { template: '<div>Workdesk</div>' } },
                { path: '/kanban', component: { template: '<div>Kanban</div>' } },
                { path: '/admin', component: { template: '<div>Admin</div>' } }
            ]
        });
        vi.clearAllMocks();
    });

    it('renders sidebar layout groups correctly with flat and accordion rendering', async () => {
        const mockData = [
            {
                title: 'groupA',
                icon: 'mdi-desktop-mac',
                children: [
                    { title: 'portal', path: '/', icon: 'mdi-home' },
                    { title: 'workdesk', path: '/workdesk', icon: 'mdi-desktop-mac' }
                ]
            },
            {
                title: 'groupB',
                icon: 'mdi-shield-alert',
                children: [
                    { title: 'settings', path: '/admin', icon: 'mdi-cog-box' }
                ]
            }
        ];
        (apiClient.get as any).mockResolvedValue({ data: mockData });

        const wrapper = mount(MainLayout, {
            global: {
                plugins: [router]
            }
        });

        // Set sidebar to expanded to test full menu text and flat group headers
        (wrapper.vm as any).isSidebarCollapsed = false;

        // Wait for asynchronous fetchMenuLayout and reactive updates
        await new Promise(resolve => setTimeout(resolve, 100));
        await wrapper.vm.$nextTick();

        // 1. Group A rendering (flat)
        // Group A title translates to "Grupo A: Operación Diaria" (es locale is active by default)
        expect(wrapper.text()).toContain('Grupo A: Operación Diaria');

        // Check flat link items are rendered directly
        expect(wrapper.text()).toContain('Portal');
        expect(wrapper.text()).toContain('Mesa de Trabajo');

        // 2. Group B rendering (accordion)
        // Group B title translates to "Grupo B: Gobierno, Seguridad e Incidentes"
        expect(wrapper.text()).toContain('Grupo B: Gobierno, Seguridad e Incidentes');

        // Group B icon is mapped to "gpp_maybe"
        expect(wrapper.html()).toContain('gpp_maybe');
    });
});
