import { describe, it, expect, beforeEach, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useAuthStore } from '@/stores/authStore';
import router from '@/router';

describe('Regression - Hallazgo 2: Unprotected Modeler and Admin Routes Security Test', () => {
    beforeEach(async () => {
        const pinia = createPinia();
        setActivePinia(pinia);
        localStorage.clear();
        vi.restoreAllMocks();

        // Reset the router to '/login' before each test to ensure fresh guard execution
        await router.push('/login');
    });

    const routesToTest = [
        { path: '/admin/incidents', allowedRoles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN_IT'] },
        { path: '/admin/modeler/bpmn', allowedRoles: ['ROLE_SUPER_ADMIN', 'ROLE_ANALYST_IT'] },
        { path: '/admin/modeler/forms', allowedRoles: ['ROLE_SUPER_ADMIN', 'ROLE_ANALYST_IT'] },
        { path: '/admin/modeler/forms/designer', allowedRoles: ['ROLE_SUPER_ADMIN', 'ROLE_ANALYST_IT'] },
        { path: '/admin/modeler/dmn', allowedRoles: ['ROLE_SUPER_ADMIN', 'ROLE_ANALYST_IT'] },
        { path: '/admin/analytics/bam', allowedRoles: ['ROLE_SUPER_ADMIN', 'Global Admin'] },
        { path: '/admin/integration/builder', allowedRoles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN_IT'] },
    ];

    routesToTest.forEach(({ path, allowedRoles }) => {
        describe(`Ruta: ${path}`, () => {
            it(`debe denegar el acceso a ${path} para un usuario con rol no autorizado (ROLE_OPERARIO)`, async () => {
                const authStore = useAuthStore();
                
                authStore.token = 'MOCK-JWT-TOKEN';
                authStore.user = { 
                    username: 'operario_user', 
                    roles: ['ROLE_OPERARIO'] 
                };
                authStore.activeRole = 'ROLE_OPERARIO';
                authStore.isGlobal404 = false;

                await router.push(path);

                // En estado RED (con bug), el guard permitirá el paso (isGlobal404 será false) porque no hay meta.roles definidos.
                // En estado GREEN (solucionado), el guard activará el falso 404 (isGlobal404 será true).
                expect(authStore.isGlobal404).toBe(true);
            });

            allowedRoles.forEach(role => {
                it(`debe permitir el acceso a ${path} para un usuario con rol autorizado (${role})`, async () => {
                    const authStore = useAuthStore();
                    
                    authStore.token = 'MOCK-JWT-TOKEN';
                    authStore.user = { 
                        username: `user_${role.toLowerCase()}`, 
                        roles: [role] 
                    };
                    authStore.activeRole = role;
                    authStore.isGlobal404 = false;

                    await router.push(path);

                    expect(authStore.isGlobal404).toBe(false);
                    expect(router.currentRoute.value.path).toBe(path);
                });
            });
        });
    });
});
