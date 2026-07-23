import { describe, it, expect, beforeEach, vi } from 'vitest';
import { createRouter, createWebHistory } from 'vue-router';
import { createPinia, setActivePinia } from 'pinia';
import { defineComponent } from 'vue';
import { useAuthStore } from '@/stores/authStore';
import { rbacGuard } from '@/router/RouteGuards';

// Mock components for route rendering
const AdminDashboard = defineComponent({ template: '<div>Admin Dashboard</div>' });
const PublicLogin = defineComponent({ template: '<div>Login</div>' });

describe('Router Guard - Active Role Spoof Bypass Security Test (RouterGuardSpoofBypass.spec.ts)', () => {
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

    it('should determine if spoofing activeRole to ROLE_SUPER_ADMIN allows bypass for a user who only has ROLE_OPERARIO', async () => {
        const authStore = useAuthStore();
        
        // Mock authorization state with JWT roles NOT including ROLE_SUPER_ADMIN
        authStore.token = 'MOCK-JWT-TOKEN';
        authStore.user = { 
            username: 'spoofer', 
            roles: ['ROLE_OPERARIO'] // User ONLY has ROLE_OPERARIO
        };
        
        // Spoof activeRole directly in Pinia to ROLE_SUPER_ADMIN
        authStore.activeRole = 'ROLE_SUPER_ADMIN';
        authStore.isGlobal404 = false;

        // Try to navigate to /admin (which requires ROLE_SUPER_ADMIN)
        await router.push('/admin');

        // Let's verify whether they were allowed access (bypass vulnerability).
        // If the vulnerability is present, isGlobal404 will remain false (permission granted)
        // and we will be on /admin.
        expect(authStore.isGlobal404).toBe(false);
        expect(router.currentRoute.value.path).toBe('/admin');
    });
});
