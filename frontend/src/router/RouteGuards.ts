import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';

/**
 * RouteGuards.ts - Decentralized Security Interceptor (Pantalla 14 Blueprint)
 * 
 * Intercepta la navegación de Vue Router leyendo de manera descentralizada el `authStore` de Pinia
 * para denegar el renderizado de componentes protegidos basados en roles (RBAC).
 * Si el usuario no tiene el rol exigido, es redirigido a una página 403 Forbidden.
 */
export const rbacGuard = async (
    to: RouteLocationNormalized,
    _from: RouteLocationNormalized,
    next: NavigationGuardNext
) => {
    const authStore = useAuthStore();
    
    // CA-3: Reset por defecto de Security by Obscurity
    authStore.isGlobal404 = false;

    // CA-5: Rutas Eximidas (Mejora de Rendimiento)
    if (to.meta.isPublic) {
        return next();
    }

    // 1. Verificación base de Autenticación (JWT Token)
    const tokenStr = authStore.token || localStorage.getItem('ibpms_token');
    
    if (to.meta.requiresAuth && !tokenStr) {
        return next('/login');
    }

    // CA-1: Espera síncrona obligatoria (Prevención Amnesia F5)
    if (tokenStr && !authStore.user) {
        try {
            await authStore.hydrateAuth();
        } catch (e: any) {
            // CA-4: Expiración de Token (401) depura Storage y patea al login.
            if (e?.status === 401) {
                authStore.logout();
                return next('/login');
            }
        }
    }

    // 2. Verificación RBAC Estricta (Solo si la ruta especifica .roles)
    if (to.meta.roles && Array.isArray(to.meta.roles)) {
        const userRoles = authStore.roles;

        // Si exige roles pero el usuario no tiene rol o sus roles no intersectan con la lista
        const hasAccess = userRoles.some(r => (to.meta.roles as string[]).includes(r));
        
        if (!hasAccess) {
            console.warn(`[SECURITY 403] Interceptor Obscurity CA-3 Activado. Ocultando URL ${to.path}. Roles provistos: ${userRoles.join(', ')}`);
            // CA-3: Falso 404. Se mantiene URL intacta en barra de navegación, el render pasa a NotFound.
            authStore.isGlobal404 = true;
            return next(); // Pasa la barrera del Router, pero el DOM colapsa en App.vue
        }
    }

    // Permiso concedido
    next();
};
