import { describe, it, expect, vi, beforeEach } from 'vitest';
import { createRouter, createWebHistory } from 'vue-router';
import { createPinia, setActivePinia } from 'pinia';
import { defineComponent } from 'vue';

// Componentes Mock para renderizado E2E del Enlazador
const Workdesk = defineComponent({ template: '<div>Workspace</div>' });
const Special404 = defineComponent({ template: '<div class="gaslight-404">Not Found Or Unauthorized</div>' });
const PublicLogin = defineComponent({ template: '<div>Login</div>' });

describe('US-051: Router Guard & Cyber Gaslighting (Iteración 46)', () => {
    let router: any;
    let authStoreMock: any;

    beforeEach(() => {
        setActivePinia(createPinia());
        
        // Simulación del estado reactivo de Pinia
        authStoreMock = {
            isStateHydrated: false,
            isAuthenticated: false,
            userRoles: [],
            hydrateState: vi.fn(),
        };

        router = createRouter({
            history: createWebHistory(),
            routes: [
                { path: '/login', component: PublicLogin, meta: { isPublic: true } },
                { path: '/workdesk/vip', component: Workdesk, meta: { isPublic: false, requiredRoles: ['VIP_ROLE'] } },
                // Ruta Catch-all simulada que actúa como Gaslighting
                { path: '/:pathMatch(.*)*', component: Special404, name: 'CyberGaslight404' }
            ]
        });

        // Simulación estricta del Arquitecto: router.beforeResolve
        router.beforeResolve(async (to: any, from: any, next: any) => {
            // 1. Bypass IsPublic
            if (to.meta.isPublic) {
                return next();
            }

            // 2. Bloqueo de Estado Histórico Pinia (Anti-FOUC)
            if (!authStoreMock.isStateHydrated) {
                await authStoreMock.hydrateState();
                authStoreMock.isStateHydrated = true;
            }

            if (!authStoreMock.isAuthenticated) {
                return next('/login');
            }

            // 3. Verificación RBAC para Gaslighting Cibernético (Rendering 404 Virtual sin alterar URL)
            const requiredRoles = to.meta.requiredRoles as string[];
            if (requiredRoles && !requiredRoles.some(r => authStoreMock.userRoles.includes(r))) {
                // Devolver el componente 404 pero MANTENER el path original
                return next({
                    name: 'CyberGaslight404',
                    params: { pathMatch: to.path.split('/').slice(1) },
                    query: to.query,
                    hash: to.hash,
                    replace: true
                });
            }

            next();
        });
    });

    it('CA-1: router.beforeResolve bloquea navegación hasta recuperar estado histórico de Pinia', async () => {
        authStoreMock.isAuthenticated = true; // Simular logueado pero sin estado hidratado en RAM
        authStoreMock.userRoles = ['VIP_ROLE'];
        
        await router.push('/workdesk/vip');
        
        // Aserción Matemática: El store fue invocado para hidratarse ANTES de resolver la ruta
        expect(authStoreMock.hydrateState).toHaveBeenCalledTimes(1);
        expect(authStoreMock.isStateHydrated).toBe(true);
        expect(router.currentRoute.value.path).toBe('/workdesk/vip');
    });

    it('CA-2: Gaslighting Cibernético - Ruta prohibida devuelve componente 404 sin mutar URL del navegador', async () => {
        authStoreMock.isStateHydrated = true;
        authStoreMock.isAuthenticated = true;
        authStoreMock.userRoles = ['GUEST_ROLE']; // Roles insuficientes (falta VIP_ROLE)

        await router.push('/workdesk/vip');

        // Aserción Matemática 1: La URL queda intacta engañando al usuario
        expect(router.currentRoute.value.path).toBe('/workdesk/vip');
        // Aserción Matemática 2: Se inyectó lógicamente la plantilla del special 404
        expect(router.currentRoute.value.name).toBe('CyberGaslight404');
    });

    it('CA-3: Rutas con isPublic dan bypass matemático al ciclo pesado', async () => {
        authStoreMock.isStateHydrated = false; // El estado NO importa para rutas públicas
        
        await router.push('/login');

        expect(authStoreMock.hydrateState).not.toHaveBeenCalled();
        expect(router.currentRoute.value.path).toBe('/login');
    });
});
