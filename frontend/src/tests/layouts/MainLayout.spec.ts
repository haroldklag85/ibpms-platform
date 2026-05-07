import { mount } from '@vue/test-utils';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { createTestingPinia } from '@pinia/testing';
import MainLayout from '@/layouts/MainLayout.vue';
import { useMenuStore } from '@/stores/useMenuStore';
import { useAuthStore } from '@/stores/authStore';
import { usePreferencesStore } from '@/stores/usePreferencesStore';

// Stub components used in MainLayout's router-view
const RouterViewStub = { template: '<div><slot :Component="{}" /></div>' };
const RouterLinkStub = { template: '<a><slot /></a>', props: ['to'] };

vi.mock('vue-router', () => ({
    useRoute: vi.fn(() => ({
        path: '/admin/modeler/bpmn'
    })),
    useRouter: vi.fn(() => ({
        push: vi.fn()
    }))
}));

describe('MainLayout.vue', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    const getWrapper = (roles: string[], initialLayout: any[]) => {
        return mount(MainLayout, {
            global: {
                plugins: [
                    createTestingPinia({
                        createSpy: vi.fn,
                        initialState: {
                            auth: { user: { roles } },
                            menu: { layout: initialLayout },
                            preferences: { uiDensity: 'STANDARD' }
                        }
                    })
                ],
                stubs: {
                    RouterView: RouterViewStub,
                    RouterLink: RouterLinkStub
                }
            }
        });
    };

    it('Sidebar RBAC: Con roles ["ROLE_SUPER_ADMIN"] el sidebar muestra todos los grupos. Con ["ROLE_OPERADOR"] muestra solo Workdesk', async () => {
        const layoutMock = [
            { title: 'Workdesk', roles: ['ROLE_OPERADOR', 'ROLE_SUPER_ADMIN'], items: [{ label: 'Inbox', path: '/workdesk/inbox' }] },
            { title: 'Administración', roles: ['ROLE_SUPER_ADMIN'], items: [{ label: 'Users', path: '/admin/users' }] }
        ];

        // 1. Super Admin
        let wrapper = getWrapper(['ROLE_SUPER_ADMIN'], layoutMock);
        // Auth store mock definition
        const authStoreSA = useAuthStore();
        authStoreSA.roles = ['ROLE_SUPER_ADMIN'];
        authStoreSA.hasAnyRole = vi.fn((rolesToCheck) => rolesToCheck.includes('ROLE_SUPER_ADMIN'));
        
        await wrapper.vm.$nextTick();
        
        // El titulo debe mostrarse en expanded mode (o inspeccionamos el render)
        // Por defecto está colapsado, debemos expandirlo o inspeccionar el v-if interno.
        wrapper.vm.isSidebarCollapsed = false;
        await wrapper.vm.$nextTick();
        expect(wrapper.text()).toContain('Workdesk');
        expect(wrapper.text()).toContain('Administración');
        wrapper.unmount();

        // 2. Operador
        wrapper = getWrapper(['ROLE_OPERADOR'], layoutMock);
        const authStoreOp = useAuthStore();
        authStoreOp.roles = ['ROLE_OPERADOR'];
        authStoreOp.hasAnyRole = vi.fn((rolesToCheck) => rolesToCheck.includes('ROLE_OPERADOR'));

        await wrapper.vm.$nextTick();
        wrapper.vm.isSidebarCollapsed = false;
        await wrapper.vm.$nextTick();
        
        expect(wrapper.find('nav').text()).toContain('Workdesk');
        expect(wrapper.find('nav').text()).not.toContain('Administración');
        wrapper.unmount();
    });

    it('Colapso/Expansión: Al invocar toggleSidebar(), la clase CSS muta de w-64 a w-16', async () => {
        const wrapper = getWrapper(['ROLE_SUPER_ADMIN'], []);
        
        // Inicialmente está en w-16 (colapsado por defecto)
        let aside = wrapper.find('aside');
        expect(aside.classes()).toContain('w-16');
        expect(aside.classes()).not.toContain('w-64');

        // Disparar toggle
        await wrapper.vm.toggleSidebar();
        
        // Ahora debe ser w-64
        aside = wrapper.find('aside');
        expect(aside.classes()).toContain('w-64');
        expect(aside.classes()).not.toContain('w-16');
    });

    it('Breadcrumbs: La ruta /admin/modeler/bpmn genera 3 breadcrumbs con labels legibles del routeNameMap', async () => {
        const wrapper = getWrapper(['ROLE_SUPER_ADMIN'], []);
        await wrapper.vm.$nextTick();

        const breadcrumbs = wrapper.vm.breadcrumbs;
        expect(breadcrumbs.length).toBe(3);
        expect(breadcrumbs[0].label).toBe('Administración'); // admin
        expect(breadcrumbs[1].label).toBe('Diseñador'); // modeler
        expect(breadcrumbs[2].label).toBe('BPMN Modeler'); // bpmn
    });

    it('Density Toggle: Al hacer clic en botón COMPACT, preferencesStore.uiDensity muta a "COMPACT"', async () => {
        const wrapper = getWrapper(['ROLE_SUPER_ADMIN'], []);
        const preferencesStore = usePreferencesStore();
        
        // Inicialmente estándar
        expect(preferencesStore.uiDensity).toBe('STANDARD');

        // Encontrar botón COMPACT y hacer click. Hay 3 botones con títulos Compacto, Estándar, Cómodo
        const compactButton = wrapper.find('button[title="Compacto"]');
        expect(compactButton.exists()).toBe(true);

        await compactButton.trigger('click');

        // Verificar el store
        expect(preferencesStore.uiDensity).toBe('COMPACT');
    });

    it('Llama menuStore.fetchMenuLayout() en onMounted', () => {
        const wrapper = getWrapper(['ROLE_SUPER_ADMIN'], []);
        const menuStore = useMenuStore();
        expect(menuStore.fetchMenuLayout).toHaveBeenCalled();
    });

    it('Muestra topRolesTipText formateado desde authStore.roles', async () => {
        const wrapper = getWrapper(['ROLE_SUPER_ADMIN', 'ROLE_OPERADOR'], []);
        await wrapper.vm.$nextTick();
        
        // Por defecto sidebar está colapsado, el tooltip contiene el texto
        expect(wrapper.vm.topRolesTipText).toBe('Super admin | Operador');
    });
});
