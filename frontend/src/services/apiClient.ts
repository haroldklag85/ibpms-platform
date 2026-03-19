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
        // CA-19: La detección offline se maneja en el interceptor de response.
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
        // CA-19: Detección Offline Instintiva
        if (!error.response || error.code === 'ERR_NETWORK') {
            console.error('Modo Desconectado. La aplicación se ha congelado por falta de Red.');
            alert('Modo Desconectado. Revisa tu conexión de red.');
            return Promise.reject(error); // Silently stops component logic without crash
        }
        
        // CA-21: Alertas Rojas Imborrables (Fatal Level 0)
        if (error.response && [500, 502, 503, 504].includes(error.response.status)) {
            console.error('Fatal Level 0 Dispatching');
            alert(`Colapso del Servidor (Code ${error.response.status}). Reinicie aplicación o contacte IT de inmediato.`);
            return Promise.reject(error);
        }

        if (error.response && error.response.status === 401) {
            const authStore = useAuthStore();
            console.warn('CA-27: Emitiendo Soft-Lock por Expiración de Token en Backend');
            authStore.logout();
            // Ya no redirigimos ni hacemos logout destructivo
        }
        // CA-7: Refresco Forzoso (Supervivencia de JWT Roles mutados)
        if (error.response && error.response.status === 403 && error.response.data?.code === 'PRIVILEGES_CHANGED') {
            const authStore = useAuthStore();
            authStore.logout();
            window.location.href = '/login?alert=Sesión Invalidada por Seguridad';
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

    // 5. BPMN Draft / Deploy / Versioning (Pantalla 6)
    saveProcessDraft: (id: string, payload: any) => apiClient.put(`/design/processes/${id}/draft`, payload),
    validateProcess: (payload: any) => apiClient.post(`/design/processes/validate`, payload),
    deployProcess: (payload: FormData) => apiClient.post(`/design/processes/deploy`, payload, { headers: { 'Content-Type': 'multipart/form-data' } }),
    requestDeployment: (id: string, payload?: any) => apiClient.post(`/design/processes/${id}/request-deployment`, payload),
    getCatalogProcesses: () => apiClient.get(`/design/processes/catalog`),
    getBpmnTemplates: () => apiClient.get(`/design/processes/templates`),
    archiveProcess: (id: string) => apiClient.post(`/design/processes/${id}/archive`), // CA-32
    
    // Gobernanza CA-6 & CA-7 & Rollback CA-15:
    getProcessVersions: (id: string) => apiClient.get(`/design/processes/${id}/versions`),
    restoreProcessVersion: (id: string, version: number) => apiClient.post(`/design/processes/${id}/rollback/${version}`),
    getProcessLock: (id: string) => apiClient.get(`/design/processes/${id}/lock`),

    // 6. BPMN Sandbox (Pantalla 6)
    deployToSandbox: (id: string, payload: any) => apiClient.post(`/design/processes/${id}/sandbox`, payload),

    // 7. BAM Analytics - Process Health (Pantalla 5)
    getProcessHealth: () => apiClient.get('/analytics/process-health'),

    // 8. BAM Analytics - AI Metrics (Pantalla 5)
    getAiMetrics: () => apiClient.get('/analytics/ai-metrics'),

    // 9. Formularios (Pantalla 7 / CA-30)
    getForms: () => apiClient.get('/forms'),

    // 10. Kanban Status Update (Pantalla 3)
    updateKanbanStatus: (id: string, status: string) => apiClient.patch(`/kanban/items/${id}/status`, { status }),

    // 10. AI Agents & Copilot (CA-8 US-005)
    translateDmnToRules: (payload: any) => apiClient.post('/ai/dmn/translate', payload),
    analyzeBpmnWithCopilot: (id: string, payload: any) => apiClient.post(`/ai/copilot/bpmn/${id}`, payload),

    // Configuraciones Administrativas (CA-30)
    getBpmnComplexityLimit: () => apiClient.get('/admin/settings/bpmn-complexity-limit'),

    // 11. Public Tracking (Pantalla 18)
    getPublicTracking: (trackingCode: string) => apiClient.get(`/public/tracking/${trackingCode}`),

    // 12. Centro de Incidentes SysAdmin (CA-13 DRP)
    getIncidents: () => apiClient.get('/admin/incidents'),
    retryIncident: (id: string) => apiClient.post(`/admin/incidents/${id}/retry`),
    abortIncident: (id: string) => apiClient.delete(`/admin/incidents/${id}`)
};
