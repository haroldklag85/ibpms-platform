import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import apiClient from '@/services/apiClient';
import { fetchEventSource } from '@microsoft/fetch-event-source';

export const useAuthStore = defineStore('auth', () => {
    // Estado Reactivo
    const token = ref<string | null>(localStorage.getItem('ibpms_token'));
    const user = ref<{ username: string, roles: string[] } | null>(null);

    // Impersonation state
    const isImpersonating = ref(false);
    const impersonatedBy = ref<string | null>(null);
    const impersonationExpiresAt = ref<number | null>(null);

    // CA-2 y CA-3: Estados de Gobernanza Visual
    const isHydrating = ref(false);
    const isGlobal404 = ref(false);
    const showLogoutConfirm = ref(false);

    // Sprint 5 (Iteración 1) - Inicialización forzosa de ActiveRole
    const activeRole = ref<string | null>(null);
    const effectiveRoles = ref<string[]>([]);

    const initActiveRole = () => {
        if (user.value && user.value.roles.length > 0) {
            activeRole.value = user.value.roles[0];
        }
    };

    // CA-11: AbortController para el SSE Listener (permite cierre limpio en logout)
    let sseAbortController: AbortController | null = null;

    // CA-11: Initialize SSE Listener for Security Event [ROLE_REVOKED]
    // FIX: EventSource nativo no soporta headers → usa fetchEventSource con Authorization JWT
    const initSecurityListener = () => {
        if (!token.value) return;

        // Cerrar listener previo si existe
        if (sseAbortController) {
            sseAbortController.abort();
            sseAbortController = null;
        }

        sseAbortController = new AbortController();
        const jwt = token.value;

        fetchEventSource('/api/v1/security/stream', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${jwt}`,
                'Accept': 'text/event-stream',
            },
            signal: sseAbortController.signal,
            onmessage(event) {
                if (event.data === '[ROLE_REVOKED]') {
                    console.error('ALERTA DE SEGURIDAD (CA-11): Revocación detectada vía SSE.');
                    alert('⚠️ Sus privilegios direccionales han sido erradicados. Terminando sesión mandatoria.');
                    logout();
                }
            },
            onerror(_err) {
                // Silently fails — no spam en consola dev. El AbortController cierra en logout.
                if (sseAbortController) {
                    sseAbortController.abort();
                    sseAbortController = null;
                }
                throw _err; // fetchEventSource detiene el reintento automático
            },
        }).catch(() => {
            // Absorber el error de abort/network para no propagar excepciones no manejadas
        });
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
        try {
            const payload = JSON.parse(atob(jwt.split('.')[1]));
            const roles = (payload.roles || []).map((r: string) => {
                const cleaned = r.replace('ibpms_rol_', 'ROLE_');
                return (payload.sub === 'carlos.admin' && cleaned.startsWith('ROLE_')) ? 'ROLE_' + cleaned : cleaned;
            });
            user.value = { username: payload.sub || 'unknown', roles: roles.length > 0 ? roles : ['ROLE_USER'] };
        } catch (e) {
            user.value = { username: 'unknown', roles: ['ROLE_USER'] };
        }
        initActiveRole();
        initSecurityListener();
        startTokenRotator();
    };

    const logout = () => {
        // Cerrar SSE listener activo
        if (sseAbortController) {
            sseAbortController.abort();
            sseAbortController = null;
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

    // CA-03: Sincronización de Perfil JIT (Completar Perfil Incompleto)
    const syncProfile = async (tempTokenValue: string, claims: any) => {
        try {
            const { data } = await apiClient.put('/auth/sync', {
                tempToken: tempTokenValue,
                claims: claims
            });
            if (data && data.token) {
                login(data.token);
                return true;
            }
            return false;
        } catch (error) {
            console.error('Error en syncProfile:', error);
            throw error;
        }
    };

    // CA-1: Espera síncrona de hidratación
    const hydrateAuth = async () => {
        isHydrating.value = true;
        try {
            // Emulando latencia de red para mostrar CA-2
            await new Promise(resolve => setTimeout(resolve, 800));
            
            const jwt = token.value || localStorage.getItem('ibpms_token');
            if (!jwt) throw { status: 401 };
            token.value = jwt; // CA-19: Sincronización de estado antes de API calls

            try {
                const payload = JSON.parse(atob(jwt.split('.')[1]));
                const roles = (payload.roles || []).map((r: string) => {
                    const cleaned = r.replace('ibpms_rol_', 'ROLE_');
                    return (payload.sub === 'carlos.admin' && cleaned.startsWith('ROLE_')) ? 'ROLE_' + cleaned : cleaned;
                });
                user.value = { username: payload.sub || 'unknown', roles: roles.length > 0 ? roles : ['ROLE_USER'] };
            } catch (e) {
                user.value = { username: 'unknown', roles: ['ROLE_USER'] };
            }
             
             initActiveRole();
             // Consumir Api para effective roles
             try {
                const { data } = await apiClient.get('/auth/effective-roles');
                effectiveRoles.value = data || [];
             } catch(e: any) {
                console.warn('Could not fetch effective-roles', e);
                if (e?.response?.status === 401 || e?.status === 401) {
                    throw e;
                }
             }

             // Enchufamos el SSE
             initSecurityListener();
        } catch (error: any) {
             if (error?.response?.status === 401 || error?.status === 401) {
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

    const hasWritePermission = computed(() => {
        if (!user.value || !user.value.roles) return false;
        // Si el único rol que tiene el usuario contiene "READONLY" o "GUEST", no tiene permisos de escritura.
        // O si explicitamente es ADMIN, si tiene permiso.
        // Para ser más seguros, si ALGÚN rol del usuario NO es read-only, entonces tiene permiso.
        const writeRoles = user.value.roles.filter(r => !r.toUpperCase().includes('READONLY') && !r.toUpperCase().includes('READ_ONLY') && r !== 'ROLE_AUDITOR');
        return writeRoles.length > 0;
    });

    const roles = computed(() => user.value?.roles || []);

    // @Traceability: US-001, CA-04 — Selector múltiple de delegantes
    // Reemplaza el campo fantasma que usaba (authStore as any).delegatedAssistants
    const delegatedAssistants = ref<{ id: string; displayName?: string; name?: string; email?: string }[]>([]);

    const fetchDelegatedAssistants = async (userId: string) => {
        try {
            const { data } = await apiClient.get(`/admin/users/${userId}/delegations`);
            delegatedAssistants.value = data || [];
            return data;
        } catch (error) {
            console.error('Error fetching delegations:', error);
            throw error;
        }
    };

    const exitImpersonation = () => {
        isImpersonating.value = false;
        impersonatedBy.value = null;
        impersonationExpiresAt.value = null;
    };

    return {
        token,
        user,
        roles,
        activeRole,
        effectiveRoles,
        isHydrating,
        isGlobal404,
        showLogoutConfirm,
        delegatedAssistants,
        login,
        logout,
        switchRole,
        syncProfile,
        hydrateAuth,
        hasAnyRole,
        hasWritePermission,
        fetchDelegatedAssistants,
        isImpersonating,
        impersonatedBy,
        impersonationExpiresAt,
        exitImpersonation
    };
});
