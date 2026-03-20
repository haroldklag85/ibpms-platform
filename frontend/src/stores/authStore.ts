import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';

export const useAuthStore = defineStore('auth', () => {
    // Estado Reactivo
    const token = ref<string | null>(localStorage.getItem('ibpms_token'));
    const user = ref<{ username: string, roles: string[] } | null>(null);

    // Funciones de Mutación
    const login = (jwt: string) => {
        token.value = jwt;
        localStorage.setItem('ibpms_token', jwt);
        // En una versión real decodificaríamos el JWT base64 aquí
        user.value = { username: 'carlos.admin', roles: ['ROLE_OPERATOR'] };
    };

    const logout = () => {
        token.value = null;
        user.value = null;
        localStorage.removeItem('ibpms_token');
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
        login,
        logout,
        hasAnyRole
    };
});
