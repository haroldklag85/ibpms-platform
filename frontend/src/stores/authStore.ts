import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useAuthStore = defineStore('auth', () => {
    // Estado Reactivo
    const token = ref<string | null>(localStorage.getItem('ibpms_token'));
    const user = ref<{ username: string, roles: string[] } | null>(null);

    // CA-2 y CA-3: Estados de Gobernanza Visual
    const isHydrating = ref(false);
    const isGlobal404 = ref(false);

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

    // Funciones de Mutación
    const login = (jwt: string) => {
        token.value = jwt;
        localStorage.setItem('ibpms_token', jwt);
        
        // Decodificación Mock (UAT)
        if (jwt.includes('EMERGENCY_LOCAL_JWT')) {
            user.value = { username: 'root@ibpms.local', roles: ['ROLE_SUPER_ADMIN'] };
        } else {
            // SSO Normal fallback
            user.value = { username: 'carlos.admin', roles: ['ROLE_USER', 'ROLE_APPROVER'] };
        }
        initSecurityListener();
    };

    const logout = () => {
        if (sseSource) {
            sseSource.close();
            sseSource = null;
        }
        token.value = null;
        user.value = null;
        isGlobal404.value = false;
        localStorage.removeItem('ibpms_token');
        // Redirección manejada por RouteGuard o Router al perder state.
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
                 user.value = { username: 'carlos.admin', roles: ['ROLE_USER', 'ROLE_APPROVER'] };
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
        isHydrating,
        isGlobal404,
        login,
        logout,
        hydrateAuth,
        hasAnyRole
    };
});
