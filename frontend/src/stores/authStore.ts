import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import apiClient from '@/services/apiClient';

export const useAuthStore = defineStore('auth', () => {
    // Estado Reactivo
    const token = ref<string | null>(localStorage.getItem('ibpms_token'));
    const user = ref<{ username: string, roles: string[] } | null>(null);

    // CA-2 y CA-3: Estados de Gobernanza Visual
    const isHydrating = ref(false);
    const isGlobal404 = ref(false);

    // Sprint 5 (Iteración 1) - Inicialización forzosa de ActiveRole
    const activeRole = ref<string | null>(null);
    const effectiveRoles = ref<string[]>([]);

    const initActiveRole = () => {
        if (user.value && user.value.roles.length > 0) {
            activeRole.value = user.value.roles[0];
        }
    };

    // CA-11: Instancia del SSE Listener
    let sseSource: EventSource | null = null;

    // CA-11: Initialize SSE Listner for Security Event [ROLE_REVOKED]
    const initSecurityListener = () => {
        if (!token.value) return;
        if (sseSource) sseSource.close();
        
        try {
            // Mock de UAT, en Producción apunta a: /api/v1/security/stream?streamId=...
            const TARGET_SSE = (import.meta as any).env.VITE_API_URL ? `${(import.meta as any).env.VITE_API_URL}/api/v1/security/stream` : 'http://localhost:8080/api/v1/security/stream';
            
            sseSource = new EventSource(TARGET_SSE);
            sseSource.onmessage = (event) => {
                if (event.data === '[ROLE_REVOKED]') {
                    console.error("ALERTA DE SEGURIDAD (CA-11): Revocación detectada vía SSE.");
                    alert("⚠️ Sus privilegios direccionales han sido erradicados. Terminando sesión mandatoria.");
                    logout();
                }
            };
            sseSource.onerror = () => {
                // Silently fails to not spam console in dev mode
                if (sseSource) sseSource.close();
            };
        } catch (e) {
            console.warn("SSE EventSource Init failed", e);
        }
    };

    // CA-4011: Token Rotator Interval (Silent Auto-Renewal)
    let rotatorInterval: ReturnType<typeof setInterval> | null = null;

    const startTokenRotator = () => {
        if (rotatorInterval) clearInterval(rotatorInterval);
        // Desencadena cada 10 minutos (600,000 ms) para renovar el JWT antes del TTL de 15mins
        rotatorInterval = setInterval(async () => {
            if (!token.value) return;
            try {
                const { data } = await apiClient.post('/auth/refresh');
                if (data && data.token) {
                    token.value = data.token;
                    localStorage.setItem('ibpms_token', data.token);
                    console.info('[AuthStore] CA-4011: Token renovado silenciosamente.');
                }
            } catch (error) {
                console.error('[AuthStore] Falla en la Rotación del Token. Forzando expiración por seguridad (Kill-Switch / Timeout).');
                alert('Sesión expirada o privilegios revocados. Inicie sesión nuevamente.');
                logout();
                window.location.href = '/login';
            }
        }, 600000); // 10 Minutos
    };

    const stopTokenRotator = () => {
        if (rotatorInterval) {
            clearInterval(rotatorInterval);
            rotatorInterval = null;
        }
    };

    // Funciones de Mutación
    const login = (jwt: string) => {
        token.value = jwt;
        localStorage.setItem('ibpms_token', jwt);
        
        // Decodificación Mock (UAT)
        if (jwt.includes('EMERGENCY_LOCAL_JWT')) {
            user.value = { username: 'root@ibpms.local', roles: ['ROLE_SUPER_ADMIN'] };
        } else {
            // SSO Normal fallback
            user.value = { username: 'carlos.admin', roles: ['ROLE_USER', 'ROLE_APPROVER', 'ROLE_SUPER_ADMIN', 'Global Admin'] };
        }
        initActiveRole();
        initSecurityListener();
        startTokenRotator();
    };

    const logout = () => {
        if (sseSource) {
            sseSource.close();
            sseSource = null;
        }
        stopTokenRotator();
        token.value = null;
        user.value = null;
        effectiveRoles.value = [];
        isGlobal404.value = false;
        localStorage.removeItem('ibpms_token');
        // Redirección manejada por RouteGuard o Router al perder state.
    };

    const switchRole = (roleId: string) => {
        activeRole.value = roleId;
        window.dispatchEvent(new CustomEvent('role-switched', { detail: { roleId } }));
    };

    // CA-1: Espera síncrona de hidratación
    const hydrateAuth = async () => {
        isHydrating.value = true;
        try {
            // Emulando latencia de red para mostrar CA-2
            await new Promise(resolve => setTimeout(resolve, 800));
            
            const jwt = token.value || localStorage.getItem('ibpms_token');
            if (!jwt) throw { status: 401 };

            // Simulación Validación API Backend (V1)
             if (jwt.includes('EMERGENCY_LOCAL_JWT')) {
                 user.value = { username: 'root@ibpms.local', roles: ['ROLE_SUPER_ADMIN'] };
             } else {
                 user.value = { username: 'carlos.admin', roles: ['ROLE_USER', 'ROLE_APPROVER', 'ROLE_SUPER_ADMIN', 'Global Admin'] };
             }
             
             initActiveRole();
             // Consumir Api para effective roles
             try {
                const { data } = await apiClient.get('/auth/effective-roles');
                effectiveRoles.value = data || [];
             } catch(e) {
                console.warn('Could not fetch effective-roles', e);
             }

             // Enchufamos el SSE
             initSecurityListener();
        } catch (error: any) {
             if (error?.status === 401) {
                 logout();
             }
             throw error;
        } finally {
            isHydrating.value = false;
        }
    };

    const hasAnyRole = (rolesToCheck: string[]) => {
        if (!user.value || !user.value.roles) return false;
        return rolesToCheck.some(r => user.value!.roles.includes(r));
    };

    const roles = computed(() => user.value?.roles || []);

    return {
        token,
        user,
        roles,
        activeRole,
        effectiveRoles,
        isHydrating,
        isGlobal404,
        login,
        logout,
        switchRole,
        hydrateAuth,
        hasAnyRole
    };
});
