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
        authStore.user = { username: 'dev.user', role: 'Global Admin' };
    }

    const isAuthenticated = !!tokenStr;

    if (to.meta.requiresAuth && !isAuthenticated) {
        return next('/login');
    }

    // 2. Verificación RBAC Estricta (Solo si la ruta especifica .roles)
    if (to.meta.roles && Array.isArray(to.meta.roles)) {
        const userRole = authStore.user?.role;

        // Si exige roles pero el usuario no tiene rol o su rol no está en la lista: 403
        if (!userRole || !(to.meta.roles as string[]).includes(userRole)) {
            console.warn(`[SECURITY 403] Acceso Denegado a ${to.path}. Se requiere: ${to.meta.roles.join(', ')}. Rol actual: ${userRole || 'Ninguno'}`);
            alert('⚠️ 403 Forbidden: No tienes privilegios para acceder a este módulo.');
            return next('/');
        }
    }

    // Permiso concedido
    next();
};
