import { defineStore } from 'pinia';
import apiClient from '@/services/apiClient';
import { Client } from '@stomp/stompjs';

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
    currentPage: 0,
    stompClient: null as Client | null,
    stompConnected: false
  }),

  actions: {
    async fetchGlobalInbox(page: number = 0, size: number = 50, search?: string, delegatedToId?: string) {
      this.isLoading = true;
      this.isError = false;
      this.errorMessage = '';
      this.currentPage = page;

      try {
        const response = await apiClient.get('/workdesk/global-inbox', {
            params: { 
              page, 
              size, 
              sort: 'slaExpirationDate,asc',
              ...(search && search.trim() !== '' ? { search: search.trim() } : {}),
              ...(delegatedToId ? { delegatedToId } : {})
            }
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
    },

    // ==========================================
    // CA-6: Ghost Deletion via STOMP WebSocket
    // ==========================================
    initWebSocket() {
      if (this.stompClient && this.stompClient.active) return;

      // URL base nativa para WebSockets STOMP hacia el backend
      const socketUrl = (import.meta as any).env?.VITE_WS_URL || 'ws://localhost:8080/ws-endpoint';

      this.stompClient = new Client({
        brokerURL: socketUrl,
        debug: (_str) => {
          // console.log('STOMP: ', _str); // Oculto para evitar ruido en consola
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      });

      this.stompClient.onConnect = (_frame) => {
        this.stompConnected = true;
        
        // CA-6 Ghost Deletion Subscription
        this.stompClient?.subscribe('/topic/workdesk.updates', (message) => {
          if (message.body) {
             try {
                 const event = JSON.parse(message.body);
                 // Si otro usuario reclama la tarea (TASK_CLAIMED), la sacamos de la vista actual 'Ghost Deletion'
                 if (event.type === 'TASK_CLAIMED' && event.taskId) {
                     this.items = this.items.filter(item => item.unifiedId !== event.taskId && item.originalTaskId !== event.taskId);
                 }
             } catch(e) {
                 console.error("Error parsing STOMP message", e);
             }
          }
        });
      };

      this.stompClient.onStompError = (frame) => {
        console.error('Broker reported error: ' + frame.headers['message']);
        this.stompConnected = false;
      };

      this.stompClient.activate();
    },

    disconnectWebSocket() {
        if (this.stompClient) {
            this.stompClient.deactivate();
            this.stompConnected = false;
        }
    }
  }
});
