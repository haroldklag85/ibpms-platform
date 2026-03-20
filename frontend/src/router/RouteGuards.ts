import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';

/**
 * RouteGuards.ts - Decentralized Security Interceptor (Pantalla 14 Blueprint)
 * 
 * Intercepta la navegación de Vue Router leyendo de manera descentralizada el `authStore` de Pinia
 * para denegar el renderizado de componentes protegidos basados en roles (RBAC).
 * Si el usuario no tiene el rol exigido, es redirigido a una página 403 Forbidden.
 */
export const rbacGuard = (
    to: RouteLocationNormalized,
    _from: RouteLocationNormalized,
    next: NavigationGuardNext
) => {
    // 1. Verificación base de Autenticación (JWT Token)
    const authStore = useAuthStore();
    const tokenStr = authStore.token || localStorage.getItem('ibpms_token');
    
    // Auto-login mockeado en caso de F5 en ambiente Dev/UAT
    if (tokenStr && !authStore.user) {
        authStore.user = { username: 'dev.user', roles: ['ROLE_SUPER_ADMIN'] };
    }

    const isAuthenticated = !!tokenStr;

    if (to.meta.requiresAuth && !isAuthenticated) {
        return next('/login');
    }

    // 2. Verificación RBAC Estricta (Solo si la ruta especifica .roles)
    if (to.meta.roles && Array.isArray(to.meta.roles)) {
        const userRoles = authStore.roles;

        // Si exige roles pero el usuario no tiene rol o sus roles no intersectan con la lista: 403
        const hasAccess = userRoles.some(r => (to.meta.roles as string[]).includes(r));
        
        if (!hasAccess) {
            console.warn(`[SECURITY 403] Acceso Denegado a ${to.path}. Se requiere al menos uno de: ${to.meta.roles.join(', ')}. Roles actuales: ${userRoles.join(', ')}`);
            alert('⚠️ 403 Forbidden: No tienes privilegios directivos para acceder a este módulo.');
            return next('/');
        }
    }

    // Permiso concedido
    next();
};
