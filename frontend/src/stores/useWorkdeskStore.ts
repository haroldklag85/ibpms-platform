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
    activeView: 'POOL' as 'PERSONAL' | 'POOL',
    _bulkDebounce: null as ReturnType<typeof setTimeout> | null
  }),

  // @Traceability: US-002, CA-22 (Contadores N y M en store)
  getters: {
    personalTaskCount: (state) => state.items.filter((t: any) => t.assignee).length,
    poolTaskCount: (state) => state.items.filter((t: any) => !t.assignee).length,
  },

  actions: {
    // @Traceability: US-001, CA-24 (Remediación ADR-006: Manejo Global de Estados Asíncronos)
    async _withNetworkSafety<T>(operation: () => Promise<T>): Promise<T> {
        try {
            return await operation();
        } catch (error) {
            throw error;
        } finally {
            this.isLoading = false;
            this.isAttending = false;
            // Cualquier otra bandera global se apaga aquí para evitar DOM Thrashing
        }
    },
    // @Traceability(US = "US-001", CA = {"CA-08"})
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

    // @Traceability(US = "US-001", CA = {"CA-08"})
    async updateFeatureToggle(toggleId: string, enabled: boolean) {
      try {
        await apiClient.put(`/workdesk/feature-toggles/${toggleId}`, { enabled });
        if (toggleId === 'force-routing' || toggleId === 'FORCE_ROUTING') {
          this.forceRoutingEnabled = enabled;
        }
      } catch (err: any) {
        throw new Error(err.response?.data?.message || 'Error updating feature toggle');
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
            const { data } = await apiClient.get(`/workbox/tasks/${taskId}/preview`);
            return data;
        } catch (error) {
            console.error('Error en fetchTaskPreview', error);
            throw error;
        }
    },

    async fetchAuditTrail(taskId: string) {
        try {
            const { data } = await apiClient.get(`/workbox/tasks/${taskId}/audit-trail`);
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
      return this._withNetworkSafety(async () => {
        // Snapshot
        const snapshot = JSON.parse(JSON.stringify(this.items));
        const taskIdx = this.items.findIndex(i => i.unifiedId === taskId || i.originalTaskId === taskId);
        let claimedTask: any = null;
        
        // Mutar Optimistically
        if (taskIdx !== -1) {
            claimedTask = this.items[taskIdx];
            claimedTask._isConfirming = true; // Flag for UI "Confirmando con el servidor..."
            claimedTask.assignee = 'analista'; // Asignar al usuario actual
        }
        
        const delays = [2000, 4000, 8000];
        for (let attempt = 0; attempt <= 3; attempt++) {
            try {
                const { data } = await apiClient.post(`/workbox/tasks/${taskId}/claim`);
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
      });
    },

    // @Traceability: US-002 - CA-10, CA-22
    async unclaimTask(taskId: string, internalMessage?: string) {
      return this._withNetworkSafety(async () => {
        const snapshot = JSON.parse(JSON.stringify(this.items));
        const taskIdx = this.items.findIndex(i => i.unifiedId === taskId || i.originalTaskId === taskId);
        
        if (taskIdx !== -1) {
            this.items.splice(taskIdx, 1);
        }
        
        try {
        const payload = internalMessage ? { mensajeInterno: internalMessage } : {};
        const { data } = await apiClient.post(`/api/v1/workbox/tasks/${taskId}/unclaim`, payload);
        return data;
        } catch (err: any) {
          this.items = snapshot;
          throw err;
        }
      });
    },

    // @Traceability: US-017 - CA-01, CA-15
    async completeTask(taskId: string, variables: any = {}) {
      return this._withNetworkSafety(async () => {
        const snapshot = JSON.parse(JSON.stringify(this.items));
        const taskIdx = this.items.findIndex(i => i.unifiedId === taskId || i.originalTaskId === taskId);
        if (taskIdx !== -1) {
            this.items.splice(taskIdx, 1);
        }
        try {
            const { data } = await apiClient.post(`/workbox/tasks/${taskId}/complete`, variables);
            return data;
        } catch (err: any) {
            this.items = snapshot;
            throw err;
        }
      });
    },

    // @Traceability: US-002 - CA-10, CA-22
    async bulkClaimTasks(taskIds: string[]) {
      return this._withNetworkSafety(async () => {
        const snapshot = JSON.parse(JSON.stringify(this.items));
        
        if (this.activeView === 'POOL') {
           this.items = this.items.filter(t => !taskIds.includes(t.unifiedId) && !taskIds.includes(t.originalTaskId));
        }

        try {
           const { data } = await apiClient.post('/api/v1/workbox/tasks/bulk-claim', taskIds);
           return data;
        } catch (err: any) {
           this.items = snapshot;
           throw err;
        }
      });
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
              ...(delegatedToId ? { delegatedUserId: delegatedToId } : {}),
              ...(typeFilter ? { type: typeFilter } : {}),
              ...(slaFilter ? { slaLevel: slaFilter } : {}),
              ...(statusFilter ? { status: statusFilter } : {}),
              view: this.activeView
            }
        });
        
        const responseData = response.data || {};
        
        // CA-20: Adaptarse al DTO canónico { data: [], pagination: {} } de WorkdeskResponseDTO
        const isNestedPage = responseData.content && !Array.isArray(responseData.content) && Array.isArray(responseData.content.content);
        let actualItems = responseData.data;
        if (!Array.isArray(actualItems) || actualItems.length === 0) {
           actualItems = isNestedPage ? responseData.content.content : (Array.isArray(responseData.content) ? responseData.content : []);
        }
        
        const actualPageable = responseData.pagination || (isNestedPage ? responseData.content.pageable : responseData.pageable) || {};
        const totalElements = responseData.pagination?.totalElements ?? (isNestedPage ? responseData.content.totalElements : responseData.totalElements);

        if (Array.isArray(actualItems)) {
            this.items = actualItems;
            this.pageInfo = { 
                pageNumber: actualPageable.page !== undefined ? actualPageable.page : (actualPageable.pageNumber || page), 
                pageSize: actualPageable.size !== undefined ? actualPageable.size : (actualPageable.pageSize || size), 
                totalElements: totalElements !== undefined ? totalElements : this.items.length 
            };
            // @Traceability(US = "US-001", CA = {"CA-07"})
            this.isDegraded = responseData.degraded === true;
            
            // @Traceability: US-001, CA-29 Contadores de Facetas
            if (responseData.facets && typeof responseData.facets === 'object' && !Array.isArray(responseData.facets)) {
                const statusMap = responseData.facets.status || {};
                this.facets = Object.entries(statusMap).map(([status, count]) => ({
                    status,
                    statusName: status === 'PENDING' ? 'Pendientes' : status === 'IN_PROGRESS' ? 'En Progreso' : status === 'OVERDUE' ? 'Vencidas' : status,
                    count: Number(count)
                }));
            } else {
                this.facets = responseData.facets || [];
            }
            this.lastDelegationContext = responseData.delegationContext || null;
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

      // URL SockJS relativa al origen del frontend — el proxy Vite (/ws → http://127.0.0.1:8080)
      // la reenvía al backend sin CORS. En producción usar VITE_WS_HTTP_URL absoluta.
      const httpUrl = (import.meta as any).env?.VITE_WS_HTTP_URL
        || `${window.location.origin}/ws/workdesk`;

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
        
        // @Traceability(US = "US-001", CA = {"CA-27", "CA-06", "CA-14"}) Suscripción segregada por Tenant (Remediado)
        const tenantId = (useAuthStore() as any).tenantId || 'default';
        this.stompClient?.subscribe(`/topic/workdesk/${tenantId}`, (message) => {
          if (message.body) {
             try {
                 const event = JSON.parse(message.body);
                 switch (event.action) {
                     case 'REMOVE':
                         // @Traceability(US = "US-001", CA = {"CA-13"}) Acierto Throttling: Payload atómico {action, id} y buffering visual con opacity 0 (_isGhost)
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
                             // @Traceability(US = "US-001", CA = {"CA-09"}) 
                             // Force global fetch si no hay payload en el websocket (Límite estricto de 15)
                             this.fetchGlobalInbox(this.currentPage, this.pageInfo.pageSize || 15, '', '', '', '', 'AVAILABLE');
                         }
                         break;
                     case 'TASK_FORCE_UNCLAIMED':
                         this._handleWsRemove(event.taskId);
                         this._showForceUnclaimToast();
                         break;
                     case 'GHOST_CLAIM':
                         const currentUser = useAuthStore().user?.username;
                         if (event.assignee !== currentUser) {
                             // @Traceability(US = "US-001", CA = {"CA-13"}) Ghost Delete con Toast Discreto
                             this.removeTaskWithGhostAnimation(event.taskId);
                             this._showGhostClaimToast();
                         }
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
    // @Traceability(US = "US-001", CA = {"CA-26", "CA-13"})
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
    

    _showGhostClaimToast() {
        const body = document.querySelector('body');
        if (body && !document.getElementById('ghost-claim-toast')) {
             const toast = document.createElement('div');
             toast.id = 'ghost-claim-toast';
             toast.style.cssText = 'position:fixed; bottom:24px; right:24px; background:#6366f1; color:white; padding:12px 20px; border-radius:8px; z-index:99999; box-shadow:0 10px 15px -3px rgba(0,0,0,0.1); font-family:sans-serif; font-size:14px; transition:opacity 0.5s;';
             toast.innerHTML = '👻 Tarea reclamada por otro analista.';
             body.appendChild(toast);
             setTimeout(() => {
                 toast.style.opacity = '0';
                 setTimeout(() => toast.remove(), 500);
             }, 3000);
        }
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

    // @Traceability(US = "US-001", CA = {"CA-26", "CA-12"})
    async _checkAutoRefill() {
        if (this.items.length < 15 && this.pageInfo.totalElements > this.items.length) {
            if (this._refillDebounce) clearTimeout(this._refillDebounce);
            this._refillDebounce = setTimeout(async () => {
                await this.fetchGlobalInbox(this.currentPage, 15);
                this._refillDebounce = null;
            }, 5000);
        }
        
        // @Traceability(US = "US-001", CA = {"CA-12"}) Acierto UX: Auto-Redirect a Pagina 1 si se vacía la bandeja
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
