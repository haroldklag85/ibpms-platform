import { defineStore } from 'pinia';
import apiClient from '@/services/apiClient';
import { Client } from '@stomp/stompjs';

export interface KanbanItem {
    id: string;
    originalTaskId?: string;
    boardId?: string;
    title: string;
    description?: string;
    status: string;
    createdAt?: string;
    slaDueDate?: string;
    assignee?: string;
    blockedReason?: string;
    processName?: string;
    priority?: string;
}

export interface KanbanColumn {
    id: string;
    boardId?: string;
    name: string;
    title?: string;
    position?: number;
    items: KanbanItem[];
}

/**
 * @Traceability: US-008, CA-4, CA-5, CA-6, CA-8, CA-12
 * Store Kanban Real — Consume endpoints reales del backend via CQRS.
 * ADR-010: Zero-Mock. ADR-002: Pinia + Axios.
 */
export const useKanbanStore = defineStore('kanban', {
    state: () => ({
        projectId: '' as string,
        boardId: '' as string,
        columns: [] as KanbanColumn[],
        tasks: [] as KanbanItem[],
        activeTimers: {} as Record<string, string>,
        loading: false,
        error: null as string | null,
        stompClient: null as Client | null,
        stompConnected: false,
    }),
    actions: {
        /**
         * @Traceability: CA-6 — Carga del tablero Kanban desde endpoint unificado.
         * Endpoint: GET /projects/{projectId}/kanban
         * Respuesta esperada: { columns: KanbanColumn[] } donde cada columna incluye sus items.
         */
        async fetchBoard(projectId: string) {
            this.projectId = projectId;
            this.loading = true;
            this.error = null;
            try {
                const response = await apiClient.get(`/projects/${projectId}/kanban`);
                const data = response.data;

                if (data.columns && Array.isArray(data.columns)) {
                    this.columns = data.columns.map((col: any) => ({
                        id: col.id,
                        boardId: col.boardId,
                        name: col.name,
                        title: col.name,
                        position: col.position,
                        items: Array.isArray(col.items) ? col.items : [],
                    }));
                } else {
                    this.columns = [];
                }

                this.tasks = this.columns.flatMap(col => col.items);

                this.initWebSocket();
            } catch (error: any) {
                console.error('Error fetching kanban board', error);
                this.error = 'Error al conectar con el servidor.';
            } finally {
                this.loading = false;
            }
        },

        /**
         * @Traceability: CA-4 — Optimistic UI con rollback ante 409 Conflict (Single-Assignee).
         * Endpoint: PATCH /projects/{projectId}/kanban/tasks/{taskId}/state
         * Body: { new_status: string, reason?: string }
         */
        async moveTask(taskId: string, newStatus: string, blockedReason?: string) {
            let targetTask: KanbanItem | undefined;
            let currentArrayCol: KanbanColumn | undefined;

            for (const col of this.columns) {
                const item = col.items.find(i => i.id === taskId);
                if (item) {
                    targetTask = item;
                    currentArrayCol = col;
                    break;
                }
            }
            if (!targetTask) return;

            const originalStatus = targetTask.status;
            const originalReason = targetTask.blockedReason;

            // Optimistic UI: mover localmente antes de la llamada HTTP
            if (currentArrayCol?.name !== newStatus) {
                if (currentArrayCol) {
                    currentArrayCol.items = currentArrayCol.items.filter(i => i.id !== taskId);
                }
                const newCol = this.columns.find(c => c.name === newStatus);
                if (newCol) newCol.items.push(targetTask);
            }

            targetTask.status = newStatus;
            if (blockedReason !== undefined) targetTask.blockedReason = blockedReason;

            const payload: Record<string, string> = { new_status: newStatus };
            if (blockedReason) payload.reason = blockedReason;

            try {
                await apiClient.patch(
                    `/projects/${this.projectId}/kanban/tasks/${taskId}/state`,
                    payload
                );
            } catch (error: any) {
                // Rollback completo del Optimistic UI
                console.warn('Optimistic UI rollback ejecutado', error);

                targetTask.status = originalStatus;
                targetTask.blockedReason = originalReason;

                const currentNewCol = this.columns.find(c => c.name === newStatus);
                if (currentNewCol) {
                    currentNewCol.items = currentNewCol.items.filter(i => i.id !== taskId);
                }
                const originalCol = this.columns.find(c => c.name === originalStatus);
                if (originalCol) originalCol.items.push(targetTask);

                if (error.response?.status === 409) {
                    this.error = 'Conflicto: esta tarea fue reclamada por otro usuario.';
                } else {
                    this.error = 'Error al mover la tarjeta.';
                }
                throw error;
            }
        },

        async startTimer(taskId: string) {
            try {
                const res = await apiClient.post('/time-tracking/start', {
                    referenceId: taskId,
                    referenceType: 'TASK_AGILE',
                });
                this.activeTimers[taskId] = res.data.id;
            } catch (e: any) {
                console.error('Error startTimer', e);
                throw e;
            }
        },

        async stopTimer(logId: string) {
            try {
                await apiClient.post(`/time-tracking/stop/${logId}`);
                for (const key in this.activeTimers) {
                    if (this.activeTimers[key] === logId) {
                        delete this.activeTimers[key];
                        break;
                    }
                }
            } catch (e: any) {
                console.error('Error stopTimer', e);
                throw e;
            }
        },

        async addColumn(boardId: string, name: string) {
            try {
                const res = await apiClient.post(`/kanban/boards/${boardId}/columns`, { name });
                const newCol = res.data;
                this.columns.push({ ...newCol, title: newCol.name, items: [] });
            } catch (e: any) {
                if (e.response?.status === 409) this.error = 'Límite de columnas alcanzado.';
                throw e;
            }
        },

        async removeColumn(boardId: string, colId: string) {
            try {
                await apiClient.delete(`/kanban/boards/${boardId}/columns/${colId}`);
                this.columns = this.columns.filter(c => c.id !== colId);
            } catch (e: any) {
                throw e;
            }
        },

        /**
         * @Traceability: CA-12 — WebSocket STOMP para actualización en tiempo real.
         * Suscripción: /topic/workdesk/kanban
         */
        initWebSocket() {
            if (this.stompClient && this.stompClient.active) return;
            const socketUrl = (import.meta as any).env?.VITE_WS_URL || 'ws://localhost:8080/ws/workdesk/websocket';
            this.stompClient = new Client({
                brokerURL: socketUrl,
                heartbeatIncoming: 4000,
                heartbeatOutgoing: 4000,
            });
            this.stompClient.onConnect = () => {
                this.stompConnected = true;
                this.stompClient?.subscribe('/topic/workdesk/kanban', (message) => {
                    if (message.body) {
                        try {
                            const eventTask = JSON.parse(message.body);
                            if (eventTask && eventTask.id && eventTask.status) {
                                // Eliminar de la columna actual si existe
                                for (const col of this.columns) {
                                    col.items = col.items.filter(i => i.id !== eventTask.id);
                                }
                                // Añadir a la nueva columna
                                const newCol = this.columns.find(c => c.name === eventTask.status);
                                if (newCol) {
                                    newCol.items.push(eventTask);
                                }
                            }
                        } catch (e) {
                            console.error('Error procesando evento WS Kanban', e);
                        }
                    }
                });
            };
            this.stompClient.onStompError = () => {
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
    },
});
