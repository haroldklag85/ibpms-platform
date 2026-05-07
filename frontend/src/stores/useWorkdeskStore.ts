import { defineStore } from 'pinia';
import apiClient from '@/services/apiClient';
import { useAuthStore } from '@/stores/authStore';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

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
    pageInfo: { pageNumber: 0, pageSize: 15, totalElements: 0 } as PageableResponse,
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
    isAttending: false,
    // CA-22: Tabs Workdesk
    activeView: 'PERSONAL' as 'PERSONAL' | 'POOL',
    _bulkDebounce: null as ReturnType<typeof setTimeout> | null
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

    // Sprint 5.1 CA-5 / CA-9
    async fetchTaskPreview(taskId: string) {
        try {
            const { data } = await apiClient.get(`/workdesk/tasks/${taskId}/preview`);
            return data;
        } catch (error) {
            console.error('Error en fetchTaskPreview', error);
            throw error;
        }
    },

    async fetchAuditTrail(taskId: string) {
        try {
            const { data } = await apiClient.get(`/workdesk/tasks/${taskId}/audit-trail`);
            return data;
        } catch (error) {
            console.error('Error en fetchAuditTrail', error);
            return []; // Fallback empty array
        }
    },

    // CA-22: Cambio de Vista Workdesk
    async setActiveView(view: 'PERSONAL' | 'POOL') {
        this.activeView = view;
        await this.fetchGlobalInbox(0, 15);
    },

    // US-002: Task Claim UI (CA-21 Optimistic UI Rollback)
    async claimTask(taskId: string) {
        // Snapshot
        const snapshot = structuredClone(this.items);
        const taskIdx = this.items.findIndex(i => i.unifiedId === taskId || i.originalTaskId === taskId);
        let claimedTask: any = null;
        
        // Mutar Optimistically
        if (taskIdx !== -1) {
            claimedTask = this.items.splice(taskIdx, 1)[0];
            claimedTask._isConfirming = true; // Flag for UI "Confirmando con el servidor..."
            if (this.activeView === 'PERSONAL') {
                this.items.unshift(claimedTask);
            }
        }
        
        const delays = [2000, 4000, 8000];
        for (let attempt = 0; attempt <= 3; attempt++) {
            try {
                const { data } = await apiClient.post(`/tasks/${taskId}/claim`);
                if (claimedTask) {
                    claimedTask._isConfirming = false;
                }
                return data;
            } catch (err: any) {
                if (attempt < 3) {
                    // Backoff
                    await new Promise(res => setTimeout(res, delays[attempt]));
                } else {
                    // Rollback
                    this.items = snapshot;
                    // Mostrar Modal / Alerta CA-21
                    const body = document.querySelector('body');
                    if (body && !document.getElementById('claim-rollback-toast')) {
                        const toast = document.createElement('div');
                        toast.id = 'claim-rollback-toast';
                        toast.style.cssText = 'position:fixed; top:80px; right:20px; background:#ef4444; color:white; padding:12px 20px; border-radius:8px; z-index:99999; box-shadow:0 10px 15px -3px rgba(0,0,0,0.1); font-family:sans-serif; font-size:14px; transition:opacity 0.5s;';
                        toast.innerHTML = '❌ No pudimos confirmar tu reclamo porque la conexión con el servidor no se restableció. La tarea sigue disponible en la cola del equipo.';
                        body.appendChild(toast);
                        setTimeout(() => {
                            toast.style.opacity = '0';
                            setTimeout(() => toast.remove(), 500);
                        }, 6000);
                    }
                    throw err;
                }
            }
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

    async fetchGlobalInbox(page: number = 0, size: number = 15, search?: string, delegatedToId?: string, typeFilter?: string, slaFilter?: string, statusFilter?: string) {
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
              ...(statusFilter ? { status: statusFilter } : {}),
              view: this.activeView
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

<<<<<<< HEAD
      // URL SockJS relativa al origen del frontend — el proxy Vite (/ws → http://127.0.0.1:8080)
      // la reenvía al backend sin CORS. En producción usar VITE_WS_HTTP_URL absoluta.
      const httpUrl = (import.meta as any).env?.VITE_WS_HTTP_URL
        || `${window.location.origin}/ws/workdesk`;
=======
      // URL base nativa para WebSockets STOMP hacia el backend
      const socketUrl = (import.meta as any).env?.VITE_WS_URL || 'ws://localhost:8080/ws/workdesk/websocket';
>>>>>>> sprint-6

      this.stompClient = new Client({
        // webSocketFactory reemplaza brokerURL cuando el servidor usa SockJS
        webSocketFactory: () => new SockJS(httpUrl),
        debug: (_str) => {
          // console.log('STOMP: ', _str); // Oculto para evitar ruido en consola
        },
        reconnectDelay: 30000, // 30s entre reintentos para evitar storm de errores en consola
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onWebSocketError: (_evt) => {
          // Silencio controlado: el backend puede no tener WebSocket activo aún
          // El error ya se refleja en stompConnected = false en la UI
          this.stompConnected = false;
        },
      });

      this.stompClient.onConnect = (_frame) => {
        this.stompConnected = true;
        
        // Ghost Deletion Listener (Paso 4.A)
        this.stompClient?.subscribe('/topic/workdesk/ghost-deletes', (message) => {
           try {
               const event = JSON.parse(message.body);
               const currentUser = useAuthStore().user?.username;
               if (event.status === 'CLAIMED' && event.assignee !== currentUser) {
                   this.removeTaskWithGhostAnimation(event.taskId);
               }
           } catch(e) {
               console.error("Error parsing STOMP Ghost Delete event", e);
           }
        });

        // CA-27: Suscripción segregada por Tenant
        const tenantId = (useAuthStore() as any).tenantId || 'default';
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
                     case 'TASK_FORCE_UNCLAIMED':
                         this._handleWsRemove(event.taskId);
                         this._showForceUnclaimToast();
                         break;
                     case 'TASKS_BULK_UPDATED':
                         this._handleWsBulkUpdate();
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

    removeTaskWithGhostAnimation(taskId: string) {
        this._handleWsRemove(taskId);
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
    },

    _handleWsBulkUpdate() {
        if (this._bulkDebounce) clearTimeout(this._bulkDebounce);
        this._bulkDebounce = setTimeout(() => {
            this.fetchGlobalInbox(this.currentPage, 15);
            this._bulkDebounce = null;
        }, 300);
    },

    _showForceUnclaimToast() {
        const body = document.querySelector('body');
        if (body && !document.getElementById('force-unclaim-toast')) {
            const toast = document.createElement('div');
            toast.id = 'force-unclaim-toast';
            toast.style.cssText = 'position:fixed; top:80px; right:20px; background:#f59e0b; color:white; padding:12px 20px; border-radius:8px; z-index:99999; box-shadow:0 10px 15px -3px rgba(0,0,0,0.1); font-family:sans-serif; font-size:14px; transition:opacity 0.5s;';
            toast.innerHTML = '⚠️ Un supervisor ha reasignado tu tarea.';
            body.appendChild(toast);
            setTimeout(() => {
                toast.style.opacity = '0';
                setTimeout(() => toast.remove(), 500);
            }, 4000);
        }
    }

  }
});
