// @Traceability: US-005, CA-41 - ADR-001
import { defineStore } from 'pinia';
import apiClient from '@/services/apiClient';

// @Traceability: Retro-Remediación ADR-006 CA-11 - Migración de llamadas de red a Store
export const useIntegrationStore = defineStore('integrationStore', {
  actions: {
    get(url: string, ...args: any[]) {
      return apiClient.get(url, ...args);
    },
    post(url: string, ...args: any[]) {
      return apiClient.post(url, ...args);
    },
    put(url: string, ...args: any[]) {
      return apiClient.put(url, ...args);
    },
    delete(url: string, ...args: any[]) {
      return apiClient.delete(url, ...args);
    },
    patch(url: string, ...args: any[]) {
      return apiClient.patch(url, ...args);
    },
    // @Traceability: US-005, CA-40
    getProcessHealth(config?: any) {
      return this.get('/analytics/process-health', config);
    },
    getAiMetrics(config?: any) {
      return this.get('/analytics/ai-metrics', config);
    },
    saveProcessDraft(id: string, payload: any) {
      return this.put(`/design/processes/${id}/draft`, payload);
    },
    // @Traceability: US-005, CA-65
    validateProcess(payload: { xml: string }) {
      const formData = new FormData();
      const blob = new Blob([payload.xml], { type: 'application/xml' });
      formData.append('file', blob, 'process.bpmn');
      
      return this.post(`/design/processes/validate`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
    },
    deployProcess(payload: any) {
      return this.post(`/design/processes/deploy`, payload, { headers: { 'Content-Type': 'multipart/form-data' } });
    },
    getCatalogProcesses() {
      return this.get(`/design/processes/catalog`);
    },
    getBpmnTemplates() {
      return this.get(`/design/processes/templates`);
    },
    archiveProcess(id: string) {
      return this.post(`/design/processes/${id}/archive`);
    },
    getProcessVersions(id: string) {
      return this.get(`/design/processes/${id}/versions`);
    },
    restoreProcessVersion(id: string, version: number) {
      return this.post(`/design/processes/${id}/rollback/${version}`);
    },
    getProcessLock(id: string) {
      return this.get(`/design/processes/${id}/lock`);
    },
    heartbeatProcessLock(id: string) {
      return this.post(`/design/processes/${id}/lock/heartbeat`);
    },
    forceUnlockProcess(id: string) {
      return this.delete(`/design/processes/${id}/lock/force`);
    },
    getProcessAuditLogs(id: string) {
      return this.get(`/design/processes/${id}/audit-logs`);
    },
    spawnSandbox(payload: any) {
      return this.post(`/design/processes/sandbox-spawn`, payload, { headers: { 'X-Sandbox-Mode': 'true' } });
    },
    getIntegrationConnectors() {
      return this.get(`/integrations/connectors`);
    },
    getConnectorSchema(id: string) {
      return this.get(`/integrations/connectors/${id}/schema`);
    },
    getProcessVariables(id: string) {
      return this.get(`/design/processes/${id}/variables`);
    },
    getExternalTaskTopics() {
      return this.get(`/design/external-task-topics`);
    },
    saveDataMappings(key: string, taskId: string, payload: any) {
      return this.post(`/design/processes/${key}/tasks/${taskId}/mappings`, payload);
    },
    // @Traceability: US-005, CA-39
    getForms(processKey?: string) {
      const params: Record<string, string> = {};
      if (processKey && processKey.trim() !== '') {
        params.processKey = processKey;
      }
      return this.get('/forms/active', { params });
    },
    getBpmnComplexityLimit() {
      return this.get('/admin/settings/bpmn-complexity-limit');
    },
    reportIsoOverride(payload: any) {
      return this.post('/forensics/iso-override', payload);
    },
    getDmnDefinitions() {
      return this.get('/dmn-models/definitions');
    },
    getGlobalInbox(params?: { page?: number; size?: number; sort?: string; search?: string; delegatedUserId?: string }) {
      return this.get('/workdesk/global-inbox', { params });
    },
    claimTask(taskId: string) {
      return this.post(`/workbox/tasks/${taskId}/claim`);
    },
    completeTask(taskId: string, payload: any, config?: any) {
      return this.post(`/workbox/tasks/${taskId}/complete`, payload, config);
    },
    saveTaskDraft(taskId: string, payload: any) {
      return this.put(`/workbox/tasks/${taskId}/draft`, payload);
    },
    correctAiText(payload: { text: string; delta: string }) {
      return this.post('/ai/correct', payload);
    },
    manualStart(payload: any) {
      return this.post('/service-delivery/manual-start', payload);
    },
    getCustomer360(id: string) {
      return this.get(`/customers/${id}/360`);
    },
    createProjectTemplate(payload: any) {
      return this.post('/projects/templates', payload);
    },
    // @Traceability: US-005, CA-69
    requestDeployment(payload: FormData) {
      return this.post('/design/processes/deploy-request', payload, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
    },
    deployToSandbox(id: string, payload: any) {
      return this.post(`/design/processes/${id}/sandbox`, payload);
    },
    getDeployRequests(key: string) {
      return this.get(`/design/processes/${key}/deploy-requests`);
    },
    // @Traceability: US-005, CA-69
    reviewDeployRequest(id: string, payload: { approved: boolean, comment?: string }) {
      return this.post(`/design/processes/deploy-requests/${id}/review`, payload);
    },
    // @Traceability: US-005, CA-15
    getProcessXml(key: string) {
      return this.get(`/design/processes/${key}/xml`);
    },
    getBpmnVariables(processKey: string) {
      return this.get(`/design/processes/${processKey}/variables`);
    },
    getFormVersions(id: string) {
      return this.get(`/design/form-definitions/${id}/versions`);
    },
    saveFormVersion(id: string, payload: any) {
      return this.post(`/design/form-definitions/${id}`, payload);
    },
    getKanbanBoard() {
      return this.get('/kanban/board');
    },
    updateKanbanStatus(id: string, payload: any) {
      return this.patch(`/kanban-tasks/tasks/${id}/state`, payload);
    },
    translateDmnToRules(payload: any) {
      return this.post('/ai/dmn/translate', payload);
    },
    analyzeBpmnWithCopilot(id: string, payload: any) {
      return this.post(`/ai/copilot/bpmn/${id}`, payload);
    },
    generateDmnRules(payload: any) {
      return this.post(`/dmn/generate`, payload);
    },
    updateDmnModel(id: string, payload: any) {
      return this.put(`/dmn-models/${id}`, payload);
    },
    refreshToken() {
      return this.post('/auth/refresh');
    },
    getPublicTracking(trackingCode: string) {
      return this.get(`/public/tracking/${trackingCode}`);
    },
    getIncidents() {
      return this.get('/admin/incidents');
    },
    retryIncident(id: string) {
      return this.post(`/admin/incidents/${id}/retry`);
    },
    abortIncident(id: string) {
      return this.delete(`/admin/incidents/${id}`);
    },
    destroyCopilotSession(sessionId: string) {
      return fetch(`/api/v1/ai/copilot/session?sessionId=${encodeURIComponent(sessionId)}`, {
        method: 'DELETE',
        keepalive: true,
        headers: { 'Authorization': `Bearer ${localStorage.getItem('ibpms_token')}` }
      });
    },
    getSlaLogs(taskId: string, page = 0, size = 20) {
      return this.get(`/agile/tasks/${taskId}/sla-log`, { params: { page, size } });
    },
    requestTimeboxExtension(taskId: string, payload: { reason: string; extensionHours: number }) {
      const uuid = (typeof crypto !== 'undefined' && crypto.randomUUID) ? crypto.randomUUID() : Math.random().toString(36).substring(2) + Date.now().toString(36);
      return this.post(`/agile/tasks/${taskId}/timebox`, payload, {
        headers: { 'Idempotency-Key': uuid }
      });
    }
  }
});
