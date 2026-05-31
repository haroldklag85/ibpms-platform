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
    getProcessHealth(config?: any) {
      return this.get('/analytics/process-health', config);
    },
    getAiMetrics(config?: any) {
      return this.get('/analytics/ai-metrics', config);
    }
  }
});
