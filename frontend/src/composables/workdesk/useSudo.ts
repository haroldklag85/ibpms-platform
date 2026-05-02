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
            await apiClient.post('/auth/sudo', { password });
            currentRequest.value.resolve(true);
            closeSudo();
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
