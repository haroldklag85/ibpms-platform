import { setActivePinia, createPinia } from 'pinia';
import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { useAuthStore } from '@/stores/authStore';
import apiClient from '@/services/apiClient';

// @Traceability: US-005, CA-15
vi.mock('@/services/apiClient', () => {
    return {
        default: {
            get: vi.fn(),
            post: vi.fn().mockResolvedValue({ data: {} }),
            put: vi.fn().mockResolvedValue({ data: {} }),
            delete: vi.fn().mockResolvedValue({ data: {} })
        }
    };
});

// Bloque 1: Aislamiento Pinia y Security Context (US-036)
describe('AuthStore - Security & RBAC', () => {
    beforeEach(() => {
        setActivePinia(createPinia());
        localStorage.clear();
        vi.restoreAllMocks();
        // @Traceability: US-005, CA-15
        vi.spyOn(apiClient, 'get').mockResolvedValue({ data: [] });
    });

    afterEach(() => {
        localStorage.clear();
    });

    it('debería inicializar vacío sin un token de localStorage', () => {
        const store = useAuthStore();
        expect(store.token).toBeNull();
        expect(store.user).toBeNull();
        expect(store.roles.length).toBe(0);
    });

    it('debería forzar autenticación fallida y purgar si un hacker inyecta un localStorage forjado con un JWT base roto', async () => {
        // Simulando un token falso o no válido inyectado manualmente
        localStorage.setItem('ibpms_token', 'HACKED_FORGED_JWT_NO_EMERGENCY');
        const store = useAuthStore();

        // Verificamos que el state inicial asume el token pero no el user
        expect(store.token).toBe('HACKED_FORGED_JWT_NO_EMERGENCY');
        
        // Ejecutamos hidratación, el backend mock en frontend lo validará
        await store.hydrateAuth();
        
        // El test mock actual asume fallback a carlos.admin por el mock, pero NO es super admin.
        // Validemos el RBAC degradation (No debe tener ROLE_SUPER_ADMIN)
        expect(store.hasAnyRole(['ROLE_SUPER_ADMIN'])).toBe(false);
        expect(store.user?.username).toBe('unknown');
    });

    it('debería asignar rol administrativo solo si el JWT porta las claims validadas (EMERGENCY_LOCAL_JWT)', async () => {
        const validJwt = 'header.eyJzdWIiOiJyb290QGlicG1zLmxvY2FsIiwgInJvbGVzIjpbIlJPTEVfU1VQRVJfQURNSU4iXX0=.signature';
        localStorage.setItem('ibpms_token', validJwt);
        const store = useAuthStore();
        
        await store.hydrateAuth();
        
        expect(store.token).toBe(validJwt);
        expect(store.hasAnyRole(['ROLE_SUPER_ADMIN'])).toBe(true);
        expect(store.user?.username).toBe('root@ibpms.local');
    });

    it('debería erradicar el estado por completo al ejecutar logout (Prevención DOM Thrashing y Fugas)', () => {
        const store = useAuthStore();
        const validJwt = 'header.eyJzdWIiOiJyb290QGlicG1zLmxvY2FsIiwgInJvbGVzIjpbIlJPTEVfU1VQRVJfQURNSU4iXX0=.signature';
        store.login(validJwt);
        
        expect(store.token).not.toBeNull();
        expect(store.user).not.toBeNull();
        
        store.logout();
        
        expect(store.token).toBeNull();
        expect(store.user).toBeNull();
        expect(localStorage.getItem('ibpms_token')).toBeNull();
    });
});
