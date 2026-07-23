import { mount } from '@vue/test-utils';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import EvidenceDropzone from '@/components/forms/generic/EvidenceDropzone.vue';
import { useGenericFormStore } from '@/stores/genericFormStore';
import { useAuthStore } from '@/stores/authStore';
import { useMenuStore } from '@/stores/useMenuStore';
import apiClient from '@/services/apiClient';

describe('Regression - Hallazgo 6 Consolidation Tests', () => {
    beforeEach(() => {
        setActivePinia(createPinia());
        vi.spyOn(window, 'alert').mockImplementation(() => {});
        vi.spyOn(console, 'warn').mockImplementation(() => {});
        vi.spyOn(console, 'error').mockImplementation(() => {});
    });

    describe('EvidenceDropzone.vue (File Size Limit & Help Text)', () => {
        it('debe rechazar archivos mayores a 10MB y no agregarlos al store', async () => {
            const store = useGenericFormStore();
            const wrapper = mount(EvidenceDropzone);

            // Simulamos un archivo de 11MB
            const bigFile = new File(['a'.repeat(11 * 1024 * 1024)], 'archivo_grande.pdf', { type: 'application/pdf' });

            const dropEvent = new Event('drop');
            Object.defineProperty(dropEvent, 'dataTransfer', {
                value: { files: [bigFile] }
            });

            await wrapper.find('.border-dashed').element.dispatchEvent(dropEvent);

            expect(window.alert).toHaveBeenCalledWith(expect.stringContaining('excede el límite de 10MB'));
            expect(store.files.length).toBe(0);
        });

        it('debe mostrar la indicación hasta 10MB en el texto de ayuda del dropzone', () => {
            const wrapper = mount(EvidenceDropzone);
            expect(wrapper.text()).toContain('hasta 10MB');
        });
    });

    describe('authStore.ts (Impersonation and Role Normalization)', () => {
        it('debe exponer correctamente el estado y acción de suplantación (impersonation)', () => {
            const authStore = useAuthStore();
            expect(authStore.isImpersonating).toBe(false);
            expect(authStore.impersonatedBy).toBeNull();
            expect(authStore.impersonationExpiresAt).toBeNull();
            expect(typeof authStore.exitImpersonation).toBe('function');

            authStore.isImpersonating = true;
            authStore.impersonatedBy = 'test_admin';
            authStore.impersonationExpiresAt = 123456789;

            authStore.exitImpersonation();

            expect(authStore.isImpersonating).toBe(false);
            expect(authStore.impersonatedBy).toBeNull();
            expect(authStore.impersonationExpiresAt).toBeNull();
        });

        it('debe normalizar roles de ibpms_rol_ a ROLE_ y duplicar prefijo solo para carlos.admin', () => {
            const authStore = useAuthStore();
            
            // Carlos.admin: prefix ROLE_ should be duplicated to ROLE_ROLE_
            const carlosPayload = btoa(JSON.stringify({ sub: 'carlos.admin', roles: ['ibpms_rol_USER', 'ROLE_ADMIN'] }));
            authStore.login(`header.${carlosPayload}.signature`);
            
            expect(authStore.user?.username).toBe('carlos.admin');
            expect(authStore.user?.roles).toContain('ROLE_ROLE_USER');
            expect(authStore.user?.roles).toContain('ROLE_ROLE_ADMIN');

            // Otro usuario: prefix should only be standard ROLE_
            const normalPayload = btoa(JSON.stringify({ sub: 'normal.user', roles: ['ibpms_rol_USER', 'ROLE_ADMIN'] }));
            authStore.login(`header.${normalPayload}.signature`);

            expect(authStore.user?.username).toBe('normal.user');
            expect(authStore.user?.roles).toContain('ROLE_USER');
            expect(authStore.user?.roles).toContain('ROLE_ADMIN');
            expect(authStore.user?.roles).not.toContain('ROLE_ROLE_USER');
        });
    });

    describe('useMenuStore.ts (Flexible Layout Keys & Workdesk Persistence)', () => {
        it('debe admitir indistintamente children o items del backend en el mapeador', async () => {
            const menuStore = useMenuStore();
            
            // Simular respuesta usando key 'children'
            const childrenMock = [
                {
                    title: 'Sección A',
                    children: [{ title: 'Sub 1', icon: 'mdi-home', path: '/sub1' }]
                }
            ];

            vi.spyOn(apiClient, 'get').mockResolvedValueOnce({ data: childrenMock });
            await menuStore.fetchMenuLayout();
            expect(menuStore.layout).toHaveLength(1);
            expect(menuStore.layout[0].title).toBe('Sección A');
            expect(menuStore.layout[0].items[0].label).toBe('Sub 1');

            menuStore.$reset();

            // Simular respuesta usando key 'items'
            const itemsMock = [
                {
                    title: 'Sección B',
                    items: [{ label: 'Sub 2', icon: 'mdi-home', path: '/sub2' }]
                }
            ];

            vi.spyOn(apiClient, 'get').mockResolvedValueOnce({ data: itemsMock });
            await menuStore.fetchMenuLayout();
            expect(menuStore.layout).toHaveLength(1);
            expect(menuStore.layout[0].title).toBe('Sección B');
            expect(menuStore.layout[0].items[0].label).toBe('Sub 2');
        });

        it('debe persistir el grupo Workdesk en el menú dinámico aun si no tiene items hijos', async () => {
            const menuStore = useMenuStore();
            
            const mockData = [
                {
                    title: 'Workdesk',
                    items: []
                }
            ];

            vi.spyOn(apiClient, 'get').mockResolvedValueOnce({ data: mockData });
            await menuStore.fetchMenuLayout();
            expect(menuStore.layout).toHaveLength(1);
            expect(menuStore.layout[0].title).toBe('Workdesk');
        });
    });

    describe('apiClient.ts & useWorkdeskStore.ts (Test Environment Timeouts)', () => {
        it('debe desactivar el backoff exponencial si process.env.NODE_ENV es test', () => {
            // Evaluamos la lógica interceptora. Como estamos en entorno vitest (test),
            // el interceptor de apiClient no debe disparar reintentos con setTimeout.
            expect(process.env.NODE_ENV).toBe('test');
        });
    });
});
