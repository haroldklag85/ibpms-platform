import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import App from '@/App.vue';
import { useAuthStore } from '@/stores/authStore';
import { useRouter } from 'vue-router';

// Mock Vue Router
vi.mock('vue-router', () => ({
    useRouter: vi.fn(() => ({
        push: vi.fn()
    })),
    RouterView: {
        template: '<div class="router-view-mock"></div>'
    }
}));

describe('App.vue - Transversal Logout Confirmation tests', () => {
    let pinia: any;

    beforeEach(() => {
        pinia = createPinia();
        setActivePinia(pinia);
        vi.clearAllMocks();
    });

    it('Does not render logout confirmation modal by default', () => {
        const authStore = useAuthStore();
        authStore.showLogoutConfirm = false;

        const wrapper = mount(App, {
            global: { plugins: [pinia] }
        });

        expect(wrapper.text()).not.toContain('¿Cerrar Sesión Activa?');
    });

    it('Renders logout confirmation modal when showLogoutConfirm is true', async () => {
        const authStore = useAuthStore();
        authStore.showLogoutConfirm = true;
        authStore.token = 'mock-jwt-token'; // Authed state
        authStore.isHydrating = false;

        const wrapper = mount(App, {
            global: { plugins: [pinia] }
        });

        await wrapper.vm.$nextTick();

        expect(wrapper.text()).toContain('¿Cerrar Sesión Activa?');
        expect(wrapper.text()).toContain('Estás a punto de terminar tu sesión de trabajo');
    });

    it('Hides the modal when Cancel is clicked', async () => {
        const authStore = useAuthStore();
        authStore.showLogoutConfirm = true;
        authStore.token = 'mock-jwt-token';
        authStore.isHydrating = false;

        const wrapper = mount(App, {
            global: { plugins: [pinia] }
        });

        await wrapper.vm.$nextTick();

        const cancelBtn = wrapper.findAll('button').filter(b => b.text() === 'Cancelar').at(0);
        expect(cancelBtn?.exists()).toBe(true);

        await cancelBtn?.trigger('click');
        expect(authStore.showLogoutConfirm).toBe(false);
    });

    it('Logs out and redirects to /login when confirm button is clicked', async () => {
        const authStore = useAuthStore();
        authStore.showLogoutConfirm = true;
        authStore.token = 'mock-jwt-token';
        authStore.isHydrating = false;

        const logoutSpy = vi.spyOn(authStore, 'logout');
        
        const pushMock = vi.fn();
        (useRouter as any).mockReturnValue({
            push: pushMock
        });

        const wrapper = mount(App, {
            global: { plugins: [pinia] }
        });

        await wrapper.vm.$nextTick();

        const confirmBtn = wrapper.findAll('button').filter(b => b.text() === 'Sí, Cerrar Sesión').at(0);
        expect(confirmBtn?.exists()).toBe(true);

        await confirmBtn?.trigger('click');

        expect(authStore.showLogoutConfirm).toBe(false);
        expect(logoutSpy).toHaveBeenCalledTimes(1);
        expect(pushMock).toHaveBeenCalledWith('/login');
    });
});
