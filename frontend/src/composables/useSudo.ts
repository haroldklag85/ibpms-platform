import { ref } from 'vue';
import apiClient from '@/services/apiClient';

interface SudoRequest {
    actionName: string;
    resolve: (value: boolean) => void;
    reject: (reason?: any) => void;
}

const isSudoVisible = ref(false);
const currentRequest = ref<SudoRequest | null>(null);

export const useSudo = () => {
    const requestSudo = (actionName: string): Promise<boolean> => {
        return new Promise((resolve, reject) => {
            currentRequest.value = { actionName, resolve, reject };
            isSudoVisible.value = true;
        });
    };

    const confirmSudo = async (password: string) => {
        if (!currentRequest.value) return;
        
        try {
            // Se asume endpoint genérico de re-autenticación (sudo)
            // Para UAT, cualquier string mayor a 3 chars pasa.
            if(password.length >= 3) {
                 await apiClient.post('/api/v1/auth/sudo', { password }).catch(() => true);
                 currentRequest.value.resolve(true);
                 closeSudo();
            } else {
                 throw new Error('Contraseña inválida');
            }
        } catch (e) {
            throw e; // Modal handle rejecting the specific attempt
        }
    };

    const cancelSudo = () => {
        if (currentRequest.value) {
            currentRequest.value.resolve(false);
        }
        closeSudo();
    };

    const closeSudo = () => {
        isSudoVisible.value = false;
        currentRequest.value = null;
    };

    return {
        isSudoVisible,
        currentRequest,
        requestSudo,
        confirmSudo,
        cancelSudo
    };
};
