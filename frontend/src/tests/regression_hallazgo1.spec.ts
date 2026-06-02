import { describe, it, expect, beforeEach, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useAuthStore } from '@/stores/authStore';
import router from '@/router';

describe('Regression - Hallazgo 1: DLQ Dashboard Route Bypass Security Test', () => {
    beforeEach(async () => {
        const pinia = createPinia();
        setActivePinia(pinia);
        localStorage.clear();
        vi.restoreAllMocks();

        // Reset the router to '/' before each test to ensure fresh guard execution
        await router.push('/login');
    });

    it('debe denegar el acceso a la ruta DLQ (/admin/integration/dlq) si el usuario NO tiene el rol requerido (ROLE_ADMIN_IT o ROLE_SUPER_ADMIN)', async () => {
        const authStore = useAuthStore();
        
        // Simular usuario autenticado pero con rol básico de OPERARIO
        authStore.token = 'MOCK-JWT-TOKEN';
        authStore.user = { 
            username: 'operario_user', 
            roles: ['ROLE_OPERARIO'] 
        };
        authStore.activeRole = 'ROLE_OPERARIO';
        authStore.isGlobal404 = false;

        // Intentar navegar al DLQ Dashboard
        await router.push('/admin/integration/dlq');

        // En estado RED, el test fallará aquí porque requiredRole no se evalúa por el guard y isGlobal404 queda en false (acceso concedido).
        // En estado GREEN, el guard detectará el rol no autorizado a través de la propiedad 'roles' y establecerá isGlobal404 a true.
        expect(authStore.isGlobal404).toBe(true);
    });

    it('debe permitir el acceso a la ruta DLQ (/admin/integration/dlq) si el usuario tiene el rol requerido de ROLE_ADMIN_IT', async () => {
        const authStore = useAuthStore();
        
        // Simular usuario autenticado con rol de administrador de TI
        authStore.token = 'MOCK-JWT-TOKEN';
        authStore.user = { 
            username: 'admin_it_user', 
            roles: ['ROLE_ADMIN_IT'] 
        };
        authStore.activeRole = 'ROLE_ADMIN_IT';
        authStore.isGlobal404 = false;

        // Intentar navegar al DLQ Dashboard
        await router.push('/admin/integration/dlq');

        // Debe permitir el acceso y mantener isGlobal404 en false
        expect(authStore.isGlobal404).toBe(false);
        expect(router.currentRoute.value.path).toBe('/admin/integration/dlq');
    });

    it('debe permitir el acceso a la ruta DLQ (/admin/integration/dlq) si el usuario tiene el rol requerido de ROLE_SUPER_ADMIN', async () => {
        const authStore = useAuthStore();
        
        // Simular usuario autenticado con rol de super administrador
        authStore.token = 'MOCK-JWT-TOKEN';
        authStore.user = { 
            username: 'super_admin_user', 
            roles: ['ROLE_SUPER_ADMIN'] 
        };
        authStore.activeRole = 'ROLE_SUPER_ADMIN';
        authStore.isGlobal404 = false;

        // Intentar navegar al DLQ Dashboard
        await router.push('/admin/integration/dlq');

        // Debe permitir el acceso y mantener isGlobal404 en false
        expect(authStore.isGlobal404).toBe(false);
        expect(router.currentRoute.value.path).toBe('/admin/integration/dlq');
    });
});
