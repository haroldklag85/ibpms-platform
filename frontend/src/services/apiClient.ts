import axios, { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios';
import { useAuthStore } from '@/stores/authStore';
import { useMenuStore } from '@/stores/useMenuStore';

// Instancia global con baseUrl que pasa por el Proxy de Vite (/api -> localhost:8080)
const apiClient: AxiosInstance = axios.create({
    baseURL: '/api/v1',
    headers: {
        'Content-Type': 'application/json',
    },
    timeout: 10000, // Timeout seguro
});


// Interceptor de Request para anexar el Bearer Token corporativo si existe
apiClient.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        // CA-19: La detección offline se maneja en el interceptor de response.
        const authStore = useAuthStore();
        if (authStore.token && config.headers) {
            config.headers.Authorization = `Bearer ${authStore.token}`;
        }
        if (authStore.activeRole && config.headers) {
            config.headers['X-Active-Role'] = authStore.activeRole;
        }
        // @Traceability: US-038 - CA-09 (Trazabilidad Quirúrgica)
        if (config.headers && !config.headers['X-Correlation-ID']) {
            config.headers['X-Correlation-ID'] = typeof crypto !== 'undefined' && crypto.randomUUID ? crypto.randomUUID() : Math.random().toString(36).substring(2) + Date.now().toString(36);
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
            const event = new CustomEvent('global-error-dispatch', { detail: { 
                code: 'NETWORK_ERR',
                message: `Modo Desconectado. Revisa tu conexión de red.`
            }});
            window.dispatchEvent(event);
            return Promise.reject(error); // Silently stops component logic without crash
        }
        
        const config = error.config as InternalAxiosRequestConfig & { _retryCount?: number };
        
        // J-04: Optimistic UI / Backoff Exponencial para 429, 502 y 503
        // @Traceability: US-003 - ADR-014 - Reintento de 502
        if (config && error.response && [429, 502, 503].includes(error.response.status)) {
            config._retryCount = config._retryCount || 0;
            if (config._retryCount < 3) {
                config._retryCount += 1;
                const backoff = Math.pow(2, config._retryCount) * 1000; // 2s, 4s, 8s
                console.warn(`J-04: Reintento automático (${config._retryCount}/3) en ${backoff}ms por HTTP ${error.response.status}`);
                return new Promise(resolve => {
                    setTimeout(() => resolve(apiClient(config)), backoff);
                });
            }
        }
        
        // @Traceability: US-000 - CA-01 (Degradación Grácil HTTP 500/503)
        // ═══ ADR-014: Diferenciación Semántica de Errores 5xx ═══
        if (error.response && error.response.status >= 500) {
            const status = error.response.status;
            const traceId = error.response.headers?.['x-correlation-id'] || 'N/A';
            
            if (status === 500) {
                // Categoría 1: Bug en el backend — Toast imborrable con traceId
                console.error(`[ADR-014] Error 500 — Trace: ${traceId}`);
                
                // CA-25: Fail-Safe Session Recovery
                // Si el error 500 ocurre en un endpoint crítico de hidratación/auth, purgamos el token malformado
                const url = error.config?.url || '';
                if (url.includes('/auth/') || url.includes('/users/me/')) {
                    console.warn('CA-25: Detectada anomalía crítica en Auth (500). Purgando sesión local para auto-recuperación.');
                    localStorage.removeItem('ibpms_token');
                    localStorage.removeItem('ibpms_user');
                    // No redirigimos inmediatamente para permitir que el usuario vea el traceId si es necesario, 
                    // pero el "REINICIAR CONTEXTO" ahora funcionará limpio.
                }

                const event = new CustomEvent('global-error-dispatch', { detail: { 
                    code: 500,
                    type: 'SERVER_ERROR',
                    message: `Error interno del servidor (Trace: ${traceId}). Contacte soporte.`,
                    dismissible: false
                }});
                window.dispatchEvent(event);
                
                // Toast DOM fallback (CA-37)
                const body = document.querySelector('body');
                if (body && !document.getElementById('server-error-toast')) {
                    const toast = document.createElement('div');
                    toast.id = 'server-error-toast';
                    toast.style.cssText = 'position:fixed; bottom:20px; right:20px; background:#ef4444; color:white; padding:12px 20px; border-radius:8px; z-index:99999; box-shadow:0 10px 15px -3px rgba(0,0,0,0.1); font-family:sans-serif; font-size:14px; font-weight:bold;';
                    toast.innerHTML = `❌ Error interno del servidor (Trace: ${traceId}). Contacte soporte.`;
                    body.appendChild(toast);
                    // NO auto-remove: este toast es imborrable per ADR-014
                }
            } else if (status === 502 || status === 503) {
                // Categoría 2: Servidor no disponible o Degradación Segura (Fail-Open)
                console.warn(`[ADR-014] Servidor no disponible o en Degradación (${status})`);
                
                // CA-01: Detectar Modo de Degradación Segura (Redis Caído)
                const isMutation = ['post', 'put', 'delete', 'patch'].includes(error.config?.method?.toLowerCase() || '');
                if (status === 503 && isMutation) {
                    console.error('CA-01: Sistema en Degradación Segura (Redis Fail-Open). Bloqueando mutación.');
                    const event = new CustomEvent('global-error-dispatch', { detail: { 
                        code: 503,
                        type: 'DEGRADED_MODE',
                        message: `Operación Denegada: Sistema en Degradación Segura (Modo Solo Lectura).`,
                        dismissible: true
                    }});
                    window.dispatchEvent(event);
                    return Promise.reject(error);
                }

                // Para operaciones seguras (GET), mostramos Toast silencioso (Fase 3 Vite Handoff) para no bloquear la UI
                const body = document.querySelector('body');
                if (body && !document.getElementById('silent-restart-toast')) {
                    const toast = document.createElement('div');
                    toast.id = 'silent-restart-toast';
                    toast.style.cssText = 'position:fixed; top:10px; right:10px; background:#3b82f6; color:white; padding:8px 16px; border-radius:20px; z-index:99999; box-shadow:0 4px 6px -1px rgba(0,0,0,0.1); font-family:sans-serif; font-size:12px; opacity:0.9; transition:opacity 0.5s; pointer-events:none;';
                    toast.innerHTML = `🔄 Servidor no disponible (${status})... verificando reconexión.`;
                    body.appendChild(toast);
                    setTimeout(() => {
                        toast.style.opacity = '0';
                        setTimeout(() => toast.remove(), 500);
                    }, 4000);
                }
            } else if (status === 504) {
                // Categoría 3: Timeout del proxy — Toast dismissible
                console.warn(`[ADR-014] Gateway Timeout (504)`);
                const event = new CustomEvent('global-error-dispatch', { detail: { 
                    code: 504,
                    type: 'GATEWAY_TIMEOUT',
                    message: 'Tiempo de espera agotado. Verifique que el servidor esté activo.',
                    dismissible: true
                }});
                window.dispatchEvent(event);
            }
            return Promise.reject(error);
        }

        // @Traceability: US-000 - CA-03 (Bloqueo de Concurrencia Optimista)
        // Interceptar CA-3: Bloqueo de Concurrencia Optimista
        if (error.response && error.response.status === 409) {
            if(error.response.data?.type?.includes("optimistic-lock")) {
                console.warn('Bloqueo de Concurrencia UI Disparado');
                const event = new CustomEvent('optimistic-lock-dispatch');
                window.dispatchEvent(event);
            }
        }

        if (error.response && error.response.status === 401) {
            const url = error.config?.url || '';
            const isCredentialCheck = url.includes('/auth/login') || 
                                      url.includes('/auth/emergency-login') || 
                                      url.includes('/auth/break-glass') || 
                                      url.includes('/auth/change-password') ||
                                      url.includes('/auth/effective-roles');
            if (isCredentialCheck) {
                return Promise.reject(error);
            }
            console.warn('CA-27: Emitiendo Soft-Lock por Expiración de Token en Backend');
            const event = new CustomEvent('global-error-dispatch', { detail: { type: 'SESSION_EXPIRED' } });
            window.dispatchEvent(event);
            return new Promise(() => {}); // Interceptar y suspender en lugar de destruir estado
        }
        
        // CA-3: Interceptar HTTP 428 (Perfil Incompleto)
        if (error.response && error.response.status === 428) {
            console.warn('[HTTP 428 Interceptor] Perfil incompleto detectado. Abriendo Guardrail JIT.');
            const event = new CustomEvent('jit-428-dispatch', { detail: error.response.data });
            window.dispatchEvent(event);
            return new Promise(() => {}); // Suspend forever
        }
        
        // CA-30: Rate Limiting Preventivo (429)
        if (error.response && error.response.status === 429) {
            console.warn('CA-30: Rate Limiting detectado. Frenando requests.');
            const body = document.querySelector('body');
            if (body && !document.getElementById('rate-limit-toast')) {
                const toast = document.createElement('div');
                toast.id = 'rate-limit-toast';
                toast.style.cssText = 'position:fixed; top:20px; right:20px; background:#f59e0b; color:white; padding:12px 20px; border-radius:8px; z-index:99999; box-shadow:0 10px 15px -3px rgba(0,0,0,0.1); font-family:sans-serif; font-size:14px; font-weight:bold; transition:opacity 0.5s;';
                toast.innerHTML = '🕒 Te has excedido del límite de peticiones. Por favor, espera un minuto.';
                body.appendChild(toast);
                setTimeout(() => {
                    toast.style.opacity = '0';
                    setTimeout(() => toast.remove(), 500);
                }, 4000);
            }
            return Promise.reject(error);
        }
        
        // CA-05: Expulsión por Manipulación Cognitiva (Prompt Injection / Abuso)
        if (error.response && error.response.status === 403) {
            // Evaluamos si trae bandera de Seguridad
            if (error.response.data?.code === 'SECURITY_VIOLATION' || error.response.data?.message?.includes('RAG') || error.response.data?.code === 'PROMPT_INJECTION') {
               console.error('[CA-05 ERROR] Infracción de Ciber-Seguridad. Interceptando flujo...');
               
               // Renderizado nativo de Modal Inevitable en el Top DOM (Vanilla injection para no depender de Vue Router)
               const body = document.querySelector('body');
               if (body && !document.getElementById('ciso-alert-modal')) {
                  const modalHTML = `
                     <div id="ciso-alert-modal" style="position:fixed; top:0; left:0; width:100vw; height:100vh; background:rgba(0,0,0,0.9); z-index:99999; display:flex; flex-direction:column; align-items:center; justify-content:center; color:white; font-family:sans-serif;">
                        <span style="font-size: 80px; margin-bottom: 20px;">🛑</span>
                        <h1 style="color:#ef4444; font-size:32px; margin-bottom:10px;">VETA CIBER-COGNITIVA ACTIVADA</h1>
                        <p style="max-width: 600px; text-align:center; font-size: 16px; margin-bottom: 30px; line-height:1.5;">
                           Se ha detectado una anomalía crítica en sus patrones de Prompt hacia la IA. 
                           Por protocolo Zero-Trust del CISO (US-027 CA-05), sus credenciales estructurales han sido revocadas instantáneamente mediante un Token Blacklist.
                        </p>
                        <p style="color:#f87171; font-weight:bold; font-family:monospace; font-size: 14px;">Iniciando expulsión forzada en <span id="ciso-countdown">5</span> segundos...</p>
                     </div>
                  `;
                  body.insertAdjacentHTML('beforeend', modalHTML);

                  let count = 5;
                  const int = setInterval(() => {
                     count--;
                     const span = document.getElementById('ciso-countdown');
                     if(span) span.innerText = count.toString();
                     if (count <= 0) {
                        clearInterval(int);
                        const authStore = useAuthStore();
                        authStore.logout();
                        window.location.href = '/login?alert=Expulsión%20por%20Violación%20Cognitiva';
                     }
                  }, 1000);
               }
               return new Promise(() => {}); // Suspende la cadena perpetuamente para abortar Vue
            } else if (error.response.data?.code === 'PRIVILEGES_CHANGED') {
               // CA-7: Refresco Forzoso estándar
               const authStore = useAuthStore();
               authStore.logout();
               window.location.href = '/login?alert=Sesión Invalidada por Seguridad';
            } else {
               // CA-32: Auto-Curación Zero-Trust
               console.warn('CA-32: Revocación de acceso detectada (403). Purgando topología local.');
               const menuStore = useMenuStore();
               menuStore.$reset();
               
               const body = document.querySelector('body');
               if (body && !document.getElementById('privilege-update-toast')) {
                   const toast = document.createElement('div');
                   toast.id = 'privilege-update-toast';
                   toast.style.cssText = 'position:fixed; top:20px; left:50%; transform:translateX(-50%); background:#f59e0b; color:white; padding:12px 20px; border-radius:8px; z-index:99999; box-shadow:0 10px 15px -3px rgba(0,0,0,0.1); font-family:sans-serif; font-size:14px; font-weight:bold; transition:opacity 0.5s;';
                   toast.innerHTML = 'Sus accesos han sido actualizados por el Administrador';
                   body.appendChild(toast);
                   setTimeout(() => {
                       toast.style.opacity = '0';
                       setTimeout(() => toast.remove(), 500);
                   }, 4000);
               }
            }
        }
        return Promise.reject(error);
    }
);

export default apiClient;

// ---------- Integration Gaps (08_integration_gaps_prompt.md) ----------
export const api = {
    // -------------------------------------------------------------
    // US-001 (Iteración 76-DEV): Workdesk Global Inbox (CA-09, CA-10, CA-19, CA-20)
    // -------------------------------------------------------------
    getGlobalInbox: (params: { page?: number; size?: number; sort?: string; search?: string; delegatedUserId?: string }) => 
        apiClient.get('/workdesk/global-inbox', { params }),

    // US-002: Workbox Tasks
    claimTask: (taskId: string) => apiClient.post(`/workbox/tasks/${taskId}/claim`),
    // @Traceability: US-003 - CA-72
    completeTask: (taskId: string, payload: any, config?: any) => apiClient.post(`/workbox/tasks/${taskId}/complete`, payload, config),
    saveTaskDraft: (taskId: string, payload: any) => apiClient.put(`/workbox/tasks/${taskId}/draft`, payload),

    // 1. AI Correct (Partial Regeneration CA-28)
    correctAiText: (payload: { text: string; delta: string }) => apiClient.post('/ai/correct', payload),

    // 2. Service Delivery (Pantalla 16)
    manualStart: (payload: any) => apiClient.post('/service-delivery/manual-start', payload),

    // 3. Customer 360 (Pantalla 17)
    getCustomer360: (id: string) => apiClient.get(`/customers/${id}/360`),

    // 4. Project Templates (Pantalla 8)
    createProjectTemplate: (payload: any) => apiClient.post('/projects/templates', payload),

    // 5. BPMN Draft / Deploy / Versioning (Pantalla 6)
    // @Traceability: US-005 - Desplegar y Versionar un Modelo de Proceso (BPMN)
    saveProcessDraft: (id: string, payload: any) => apiClient.put(`/design/processes/${id}/draft`, payload),
    validateProcess: (payload: any) => apiClient.post(`/design/processes/validate`, payload),
    deployProcess: (payload: FormData) => apiClient.post(`/design/processes/deploy`, payload, { headers: { 'Content-Type': 'multipart/form-data' } }),
    requestDeployment: (id: string, payload?: any) => apiClient.post(`/design/processes/${id}/request-deployment`, payload),
    getCatalogProcesses: () => apiClient.get(`/design/processes/catalog`),
    getBpmnTemplates: () => apiClient.get(`/design/processes/templates`),
    archiveProcess: (id: string) => apiClient.post(`/design/processes/${id}/archive`), // CA-32
    
    // Gobernanza CA-6 & CA-7 & Rollback CA-15 & Heartbeat CA-66/64:
    getProcessVersions: (id: string) => apiClient.get(`/design/processes/${id}/versions`),
    restoreProcessVersion: (id: string, version: number) => apiClient.post(`/design/processes/${id}/rollback/${version}`),
    getProcessLock: (id: string) => apiClient.get(`/design/processes/${id}/lock`),
    heartbeatProcessLock: (id: string) => apiClient.post(`/design/processes/${id}/lock/heartbeat`), // CA-66
    forceUnlockProcess: (id: string) => apiClient.delete(`/design/processes/${id}/lock/force`), // CA-64
    getProcessAuditLogs: (id: string) => apiClient.get(`/design/processes/${id}/audit-logs`), // CA-42

    // 6. BPMN Sandbox (Pantalla 6 / CA-41)
    deployToSandbox: (id: string, payload: any) => apiClient.post(`/design/processes/${id}/sandbox`, payload),
    spawnSandbox: (payload: any) => apiClient.post(`/design/processes/sandbox-spawn`, payload), // CA-41

    // 6.5 Panel Solicitudes de Despliegue (CA-69)
    getDeployRequests: (key: string) => apiClient.get(`/design/processes/${key}/deploy-requests`),
    approveDeployRequest: (id: string, payload?: any) => apiClient.post(`/design/deploy-requests/${id}/approve`, payload),
    rejectDeployRequest: (id: string, payload: any) => apiClient.post(`/design/deploy-requests/${id}/reject`, payload),

    // Integraciones / Conectores (CA-45, CA-49, CA-68, CA-70)
    getIntegrationConnectors: () => apiClient.get(`/integrations/connectors`),
    getConnectorSchema: (id: string) => apiClient.get(`/integrations/connectors/${id}/schema`), // CA-49
    getProcessVariables: (id: string) => apiClient.get(`/design/processes/${id}/variables`), // CA-49
    // CA-17: Variables BPMN para coherencia
    getBpmnVariables: (processKey: string) => apiClient.get(`/design/processes/${processKey}/variables`),
    getExternalTaskTopics: () => apiClient.get(`/design/external-task-topics`), // CA-70
    saveDataMappings: (key: string, taskId: string, payload: any) => apiClient.post(`/design/processes/${key}/tasks/${taskId}/mappings`, payload), // CA-68

    // 7. BAM Analytics - Process Health (Pantalla 5)
    getProcessHealth: () => apiClient.get('/analytics/process-health'),

    // 8. BAM Analytics - AI Metrics (Pantalla 5)
    getAiMetrics: () => apiClient.get('/analytics/ai-metrics'),

    // 9. Formularios (Pantalla 7 / CA-30)
    // @Traceability: US-005, CA-40
    getForms: (processKey?: string) => apiClient.get('/forms/active', { params: { processKey } }),
    getFormVersions: (id: string) => apiClient.get(`/design/form-definitions/${id}/versions`),
    saveFormVersion: (id: string, payload: any) => apiClient.post(`/design/form-definitions/${id}`, payload),

    // 10. Kanban Status Update (Pantalla 3)
    getKanbanBoard: () => apiClient.get('/kanban/board'), // This one is fine because KanbanStateController exposes /kanban/board
    updateKanbanStatus: (id: string, payload: any) => apiClient.patch(`/kanban-tasks/tasks/${id}/state`, payload),

    // 10. AI Agents & Copilot (CA-8 US-005)
    // @Traceability: US-007 - Generador Cognitivo de DMN (NLP a Tablas de Decisión)
    translateDmnToRules: (payload: any) => apiClient.post('/ai/dmn/translate', payload),
    analyzeBpmnWithCopilot: (id: string, payload: any) => apiClient.post(`/ai/copilot/bpmn/${id}`, payload),
    generateDmnRules: (payload: any) => apiClient.post(`/dmn/generate`, payload),
    updateDmnModel: (id: string, payload: any) => apiClient.put(`/dmn-models/${id}`, payload),

    // Sprint 6.1: DMN Definitions
    // @Traceability: US-007 - Generador Cognitivo de DMN (NLP a Tablas de Decisión)
    getDmnDefinitions: () => apiClient.get('/dmn-models/definitions'),

    // Configuraciones Administrativas (CA-30)
    getBpmnComplexityLimit: () => apiClient.get('/admin/settings/bpmn-complexity-limit'),

    // -------------------------------------------------------------
    // US-048 (Iteración 2): Renovación Silenciosa
    // -------------------------------------------------------------
    refreshToken: () => apiClient.post('/auth/refresh'),

    // 11. Public Tracking (Pantalla 18)
    getPublicTracking: (trackingCode: string) => apiClient.get(`/public/tracking/${trackingCode}`),

    // 12. Centro de Incidentes SysAdmin (CA-13 DRP)
    getIncidents: () => apiClient.get('/admin/incidents'),
    retryIncident: (id: string) => apiClient.post(`/admin/incidents/${id}/retry`),
    abortIncident: (id: string) => apiClient.delete(`/admin/incidents/${id}`),
    
    // CA-04: Limpieza de contexto IA al abandonar Sesión (Purga RAG)
    destroyCopilotSession: (sessionId: string) => fetch(`/api/v1/ai/copilot/session?sessionId=${encodeURIComponent(sessionId)}`, { method: 'DELETE', keepalive: true, headers: { 'Authorization': `Bearer ${localStorage.getItem('ibpms_token')}` } }),

    // CA-09: Trazador Forense de Descartes ISO (Override)
    reportIsoOverride: (payload: any) => apiClient.post('/forensics/iso-override', payload),

    // Sprint 5 - Iteración 2: Timebox & SLA
    getSlaLogs: (taskId: string, page = 0, size = 20) => 
        apiClient.get(`/agile/tasks/${taskId}/sla-log`, { params: { page, size } }),

    requestTimeboxExtension: (taskId: string, payload: { reason: string; extensionHours: number }) => {
        // Enviar Idempotency-Key para proteger la concurrencia Zero-Trust
        const uuid = (typeof crypto !== 'undefined' && crypto.randomUUID) ? crypto.randomUUID() : Math.random().toString(36).substring(2) + Date.now().toString(36);
        return apiClient.post(`/agile/tasks/${taskId}/timebox`, payload, {
            headers: { 'Idempotency-Key': uuid }
        });
    }
};
