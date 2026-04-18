import { defineStore } from 'pinia';
import apiClient from '@/services/apiClient';
import { useAuthStore } from '@/stores/authStore';
import { Client } from '@stomp/stompjs';

export interface WorkdeskGlobalItemDTO {
  unifiedId: string;
  sourceSystem: 'BPMN' | 'KANBAN';
  originalTaskId: string;
  title: string;
  slaExpirationDate: string; // ISO 8601
  status: string;
  assignee: string | null;
  isSlaAtRisk?: boolean; // CA-6 Semáforo Naranja Early Warning
  candidateGroup?: string; // CA-10 Visibilidad Multi-Rol
  
  // 77-DEV: Nuevos campos CA-01/CA-03/CA-23
  progressPercent: number | null;    // CA-23: null = N/D
  typeBadge: string;                 // CA-03: '⚡ Flujo' o '📅 Proyecto'
  financialImpactHigh: boolean;      // CA-17: Badge 🔥
  impactLevel?: number;
}

export interface FacetCountDTO {
  status: string;
  statusName?: string;
  count: number;
}

export interface PageableResponse {
  pageNumber: number;
  pageSize: number;
  totalElements: number;
}

export const useWorkdeskStore = defineStore('workdesk', {
  state: () => ({
    items: [] as WorkdeskGlobalItemDTO[],
    facets: [] as FacetCountDTO[],
    pageInfo: { pageNumber: 0, pageSize: 50, totalElements: 0 } as PageableResponse,
    isDegraded: false,
    isLoading: false,
    isError: false,
    errorMessage: '',
    currentPage: 0,
    stompClient: null as Client | null,
    stompConnected: false,
    _pendingRemovals: [] as string[],
    _removalTimer: null as ReturnType<typeof setTimeout> | null,
    _refillDebounce: null as ReturnType<typeof setTimeout> | null,
    // CA-15: Contexto de delegación de la última respuesta
    lastDelegationContext: null as { delegatedUserId: string; delegatedUserDisplayName: string; delegationActive: boolean } | null,
    // CA-08: Modo Atender Siguiente
    forceRoutingEnabled: false,
    isAttending: false
  }),

  actions: {
    // CA-08: Verificar si el Feature Toggle está activado
    async checkForceRouting() {
      try {
        const { data } = await apiClient.get('/workdesk/feature-toggles/FORCE_ROUTING');
        this.forceRoutingEnabled = data.enabled;
      } catch (err) {
        console.warn('CA-08: No se pudo obtener feature toggle', err);
        this.forceRoutingEnabled = false;
      }
    },

    // CA-08 / CA-16: Atender Siguiente Tarea (Skill-Based Routing)
    async attendNext() {
      this.isAttending = true;
      try {
        const { data } = await apiClient.post('/workdesk/attend-next');
        return data;
      } catch (err: any) {
        if (err.response?.status === 404) {
          throw new Error('No hay tareas disponibles en este momento.');
        }
        throw err;
      } finally {
        this.isAttending = false;
      }
    },

    // US-002: Task Claim UI
    async claimTask(taskId: string) {
      try {
        const { data } = await apiClient.post(`/tasks/${taskId}/claim`);
        return data;
      } catch (err: any) {
        throw err;
      }
    },

    async unclaimTask(taskId: string) {
      try {
        const { data } = await apiClient.post(`/tasks/${taskId}/unclaim`);
        return data;
      } catch (err: any) {
        throw err;
      }
    },

    // CA-21: Skipeo Justificado
    async skipAndNext(taskId: string, reason: string, detail?: string) {
      this.isAttending = true;
      try {
        const { data } = await apiClient.post('/workdesk/attend-next/skip', {
          taskId, skipReason: reason, skipReasonDetail: detail
        });
        return data;
      } catch (err: any) {
        throw err;
      } finally {
        this.isAttending = false;
      }
    },

    async fetchGlobalInbox(page: number = 0, size: number = 50, search?: string, delegatedToId?: string, typeFilter?: string, slaFilter?: string, statusFilter?: string) {
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
              ...(delegatedToId ? { delegatedToId } : {}),
              ...(typeFilter ? { type: typeFilter } : {}),
              ...(slaFilter ? { slaLevel: slaFilter } : {}),
              ...(statusFilter ? { status: statusFilter } : {})
            }
        });
        
        if (response.data && Array.isArray(response.data.content)) {
            this.items = response.data.content;
            this.pageInfo = response.data.pageable || { pageNumber: page, pageSize: size, totalElements: response.data.totalElements || this.items.length };
            this.isDegraded = response.data?.degraded === true;
            this.facets = response.data.facets || [];
            this.lastDelegationContext = response.data.delegationContext || null;
        } else {
             // Fallback defensive
             this.items = [];
        }
      } catch (error: any) {
        if (error.response && error.response.status === 429) {
             console.warn("CA-30: Ignorando error 429 para mantener listado UI intacto");
             return;
        }
        console.error("Failed to fetch secure workdesk queues", error);
        this.isError = true;
        this.errorMessage = error.response?.data?.message || "Ocurrió un error al cargar la bandeja segura CA-5.";
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
        
        // CA-27: Suscripción segregada por Tenant
        const tenantId = useAuthStore().tenantId || 'default';
        this.stompClient?.subscribe(`/topic/workdesk/${tenantId}`, (message) => {
          if (message.body) {
             try {
                 const event = JSON.parse(message.body);
                 switch (event.action) {
                     case 'REMOVE':
                         this._handleWsRemove(event.taskId);
                         break;
                     case 'ADD':
                         if (event.payload) this._handleWsAdd(event.payload);
                         break;
                     case 'UPDATE':
                         if (event.payload) this._handleWsUpdate(event.taskId, event.payload);
                         break;
                     case 'TASK_UNCLAIMED':
                         // Lo inyectamos de nuevo como disponible o actualizamos
                         if (event.payload) {
                             this._handleWsAdd(event.payload); 
                         } else {
                             // Force global fetch si no hay payload en el websocket
                             this.fetchGlobalInbox(this.currentPage, 50, '', '', '', '', 'AVAILABLE');
                         }
                         break;
                     case 'PRIORITY_CHANGE':
                         this._handleWsPriorityChange();
                         break;
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
    },

    // CA-13: Throttling & Auto-refill helpers
    _handleWsRemove(taskId: string) {
        this._pendingRemovals.push(taskId);
        if (this._removalTimer) return;
        
        this._removalTimer = setTimeout(() => {
            const idsToRemove = [...this._pendingRemovals];
            this._pendingRemovals = [];
            this._removalTimer = null;
            
            idsToRemove.forEach(id => {
                const idx = this.items.findIndex(i => i.unifiedId === id || i.originalTaskId === id);
                if (idx !== -1) {
                    (this.items[idx] as any)._isGhost = true;
                }
            });
            
            setTimeout(() => {
                this.items = this.items.filter(i => !(i as any)._isGhost);
                this._checkAutoRefill();
            }, 800);
        }, 2000);
    },
    
    _handleWsAdd(payload: WorkdeskGlobalItemDTO) {
        // CA-26: Fade-in animation logic
        (payload as any)._isNew = true;
        this.items.unshift(payload);
        setTimeout(() => {
            (payload as any)._isNew = false;
        }, 500);
    },
    
    _handleWsUpdate(taskId: string, payload: WorkdeskGlobalItemDTO) {
        const idx = this.items.findIndex(i => i.unifiedId === taskId || i.originalTaskId === taskId);
        if (idx !== -1) {
            this.items[idx] = { ...this.items[idx], ...payload };
        }
    },
    
    _handleWsPriorityChange() {
        // Re-ordenar items por urgencia SLA o recargar total
        this._checkAutoRefill();
    },

    async _checkAutoRefill() {
        if (this.items.length < 15 && this.pageInfo.totalElements > this.items.length) {
            if (this._refillDebounce) clearTimeout(this._refillDebounce);
            this._refillDebounce = setTimeout(async () => {
                await this.fetchGlobalInbox(this.currentPage, 15);
                this._refillDebounce = null;
            }, 5000);
        }
        
        if (this.items.length === 0 && this.currentPage > 0) {
            await this.fetchGlobalInbox(0, 15);
        }
    }

  }
});
