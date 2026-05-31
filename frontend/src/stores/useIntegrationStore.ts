import { defineStore } from 'pinia';
import apiClient from '@/services/apiClient';

// @Traceability: Retro-Remediación ADR-006 CA-11 - Migración de llamadas de red a Store
export const useIntegrationStore = defineStore('integrationStore', {
  actions: {
    get(url: string, config?: any) {
      return apiClient.get(url, config);
    },
    post(url: string, data?: any, config?: any) {
      return apiClient.post(url, data, config);
    },
    put(url: string, data?: any, config?: any) {
      return apiClient.put(url, data, config);
    },
    delete(url: string, config?: any) {
      return apiClient.delete(url, config);
    },
    patch(url: string, data?: any, config?: any) {
      return apiClient.patch(url, data, config);
    },
    getProcessHealth(config?: any) {
      return this.get('/analytics/process-health', config);
    },
    getAiMetrics(config?: any) {
      return this.get('/analytics/ai-metrics', config);
    }
  }
});
