import { defineStore } from 'pinia';
import { ref } from 'vue';
import { useRouter } from 'vue-router';

export const useAuthStore = defineStore('auth', () => {
    // Estado Reactivo
    const token = ref<string | null>(localStorage.getItem('ibpms_token'));
    const user = ref<{ username: string, role: string } | null>(null);

    // Funciones de Mutación
    const login = (jwt: string) => {
        token.value = jwt;
        localStorage.setItem('ibpms_token', jwt);
        // En una versión real decodificaríamos el JWT base64 aquí
        user.value = { username: 'carlos.admin', role: 'OPERATOR' };
    };

    const logout = () => {
        token.value = null;
        user.value = null;
        localStorage.removeItem('ibpms_token');
    };

    return {
        token,
        user,
        login,
        logout
    };
});
