import { describe, it, expect, beforeEach, vi } from 'vitest';
import { createRouter, createWebHistory } from 'vue-router';
import { createPinia, setActivePinia } from 'pinia';
import { defineComponent } from 'vue';
import { useAuthStore } from '@/stores/authStore';
import { rbacGuard } from '@/router/RouteGuards';

// Mock components for route rendering
const AdminDashboard = defineComponent({ template: '<div>Admin Dashboard</div>' });
const PublicLogin = defineComponent({ template: '<div>Login</div>' });

describe('Router Guard - Active Role Validation (RouterGuardActiveRole.spec.ts)', () => {
    let router: any;

    beforeEach(() => {
        // Set up fresh Pinia active instance
        const pinia = createPinia();
        setActivePinia(pinia);
        localStorage.clear();
        vi.restoreAllMocks();

        // Create router instance
        router = createRouter({
            history: createWebHistory(),
            routes: [
                { path: '/login', component: PublicLogin, meta: { isPublic: true } },
                { 
                    path: '/admin', 
                    component: AdminDashboard, 
                    meta: { 
                        requiresAuth: true, 
                        roles: ['ROLE_SUPER_ADMIN'] 
                    } 
                }
            ]
        });

        // Add the real rbacGuard under test
        router.beforeResolve(rbacGuard);
    });

    it('should block navigation to /admin when activeRole is ROLE_OPERARIO, even if all roles list contains ROLE_SUPER_ADMIN', async () => {
        const authStore = useAuthStore();
        
        // Mock authorization state
        authStore.token = 'MOCK-JWT-TOKEN';
        authStore.user = { 
            username: 'test.user', 
            roles: ['ROLE_SUPER_ADMIN', 'ROLE_OPERARIO'] 
        };
        
        // Mock activeRole (the active role selected by user)
        authStore.activeRole = 'ROLE_OPERARIO';
        authStore.isGlobal404 = false;

        // Try to navigate to /admin (which requires ROLE_SUPER_ADMIN)
        await router.push('/admin');

        // Assertions:
        // 1. isGlobal404 should be set to true because the activeRole 'ROLE_OPERARIO' is not allowed on '/admin'
        // 2. The navigation remains at '/admin' but components colapse due to isGlobal404 (Security by Obscurity)
        expect(authStore.isGlobal404).toBe(true);
        expect(router.currentRoute.value.path).toBe('/admin');
    });
});
