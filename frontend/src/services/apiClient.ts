import axios, { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios';
import { useAuthStore } from '@/stores/authStore';

// Instancia global con baseUrl que pasa por el Proxy de Vite (/api -> localhost:8080)
const apiClient: AxiosInstance = axios.create({
    baseURL: '/api/v1',
    headers: {
        'Content-Type': 'application/json',
    },
    timeout: 10000, // Timeout seguro
});

// [Handoff Requirement]: Backend no dispone de servidor live en pipeline local. Activamos Modo Mock Estricto.
import { setupMockAdapter } from './mockAdapter';
setupMockAdapter(apiClient);

// Interceptor de Request para anexar el Bearer Token corporativo si existe
apiClient.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        const authStore = useAuthStore();
        if (authStore.token && config.headers) {
            config.headers.Authorization = `Bearer ${authStore.token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Interceptor global para respuestas (Ej: Expulsar a login si 401 Unauthorized)
apiClient.interceptors.response.use(
    (response: AxiosResponse) => {
        return response;
    },
    (error) => {
        if (error.response && error.response.status === 401) {
            const authStore = useAuthStore();
            authStore.logout();
        }
        return Promise.reject(error);
    }
);

export default apiClient;

// ---------- Integration Gaps (08_integration_gaps_prompt.md) ----------
export const api = {
    // 1. AI Correct (Partial Regeneration CA-28)
    correctAiText: (payload: { text: string; delta: string }) => apiClient.post('/ai/correct', payload),

    // 2. Service Delivery (Pantalla 16)
    manualStart: (payload: any) => apiClient.post('/service-delivery/manual-start', payload),

    // 3. Customer 360 (Pantalla 17)
    getCustomer360: (id: string) => apiClient.get(`/customers/${id}/360`),

    // 4. Project Templates (Pantalla 8)
    createProjectTemplate: (payload: any) => apiClient.post('/projects/templates', payload),

    // 5. BPMN Draft (Pantalla 6)
    saveProcessDraft: (id: string, payload: any) => apiClient.put(`/design/processes/${id}/draft`, payload),

    // 6. BPMN Sandbox (Pantalla 6)
    deployToSandbox: (id: string, payload: any) => apiClient.post(`/design/processes/${id}/sandbox`, payload),

    // 7. BAM Analytics - Process Health (Pantalla 5)
    getProcessHealth: () => apiClient.get('/analytics/process-health'),

    // 8. BAM Analytics - AI Metrics (Pantalla 5)
    getAiMetrics: () => apiClient.get('/analytics/ai-metrics'),

    // 9. Kanban Status Update (Pantalla 3)
    updateKanbanStatus: (id: string, status: string) => apiClient.patch(`/kanban/items/${id}/status`, { status }),

    // 10. AI DMN Translate (Pantalla 4/15)
    translateDmnToRules: (payload: any) => apiClient.post('/ai/dmn/translate', payload),

    // 11. Public Tracking (Pantalla 18)
    getPublicTracking: (trackingCode: string) => apiClient.get(`/public/tracking/${trackingCode}`)
};
