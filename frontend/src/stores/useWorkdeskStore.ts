import { defineStore } from 'pinia';
import apiClient from '@/services/apiClient';

export interface WorkdeskGlobalItemDTO {
  unifiedId: string;
  sourceSystem: 'BPMN' | 'KANBAN';
  originalTaskId: string;
  title: string;
  slaExpirationDate: string; // ISO 8601
  status: string;
  assignee: string | null;
}

export interface PageableResponse {
  pageNumber: number;
  pageSize: number;
  totalElements: number;
}

export const useWorkdeskStore = defineStore('workdesk', {
  state: () => ({
    items: [] as WorkdeskGlobalItemDTO[],
    pageInfo: { pageNumber: 0, pageSize: 50, totalElements: 0 } as PageableResponse,
    isLoading: false,
    isError: false,
    errorMessage: '',
    currentPage: 0
  }),

  actions: {
    async fetchGlobalInbox(page: number = 0, size: number = 50) {
      this.isLoading = true;
      this.isError = false;
      this.errorMessage = '';
      this.currentPage = page;

      try {
        const response = await apiClient.get('/workdesk/global-inbox', {
            params: { page, size, sort: 'slaExpirationDate,asc' }
        });
        
        if (response.data && Array.isArray(response.data.content)) {
            this.items = response.data.content;
            this.pageInfo = response.data.pageable || { pageNumber: page, pageSize: size, totalElements: this.items.length };
        } else {
             // Fallback defensive
             this.items = [];
        }
      } catch (error: any) {
        console.error("Failed to fetch hybrid workdesk items", error);
        this.isError = true;
        this.errorMessage = error.response?.data?.message || "Ocurrió un error al cargar la bandeja global.";
        this.items = [];
      } finally {
        this.isLoading = false;
      }
    }
  }
});
