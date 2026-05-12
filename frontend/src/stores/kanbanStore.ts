import { defineStore } from 'pinia';
import axios from 'axios';
import { Client } from '@stomp/stompjs';

export interface KanbanItem {
    id: string;
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
 * @Traceability(US = "US-008", CA = {"CA-11", "CA-12"})
 */
export const useKanbanStore = defineStore('kanban', {
    state: () => ({
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
        async fetchBoard(boardId: string) {
            this.boardId = boardId;
            this.loading = true;
            this.error = null;
            try {
                const [colsRes, tasksRes] = await Promise.all([
                    axios.get(`/api/v1/kanban-tasks/boards/${boardId}/columns`),
                    axios.get(`/api/v1/kanban-tasks/boards/${boardId}/tasks`)
                ]);
                
                this.columns = colsRes.data.map((c: any) => ({
                    ...c,
                    title: c.name,
                    items: []
                }));
                
                this.tasks = tasksRes.data;
                for(const task of this.tasks) {
                    const col = this.columns.find(c => c.name === task.status);
                    if(col) col.items.push(task);
                }
                
                this.initWebSocket();
            } catch (error: any) {
                console.error("Error fetching board", error);
                this.error = "Error al conectar con el servidor.";
            } finally {
                this.loading = false;
            }
        },
        async moveTask(taskId: string, newStatus: string, blockedReason?: string) {
            let targetTask: KanbanItem | undefined;
            let currentArrayCol: KanbanColumn | undefined;
            
            // Find task in current columns (VueDraggable might have already moved it)
            for(const col of this.columns) {
                const item = col.items.find(i => i.id === taskId);
                if (item) {
                    targetTask = item;
                    currentArrayCol = col;
                    break;
                }
            }
            if(!targetTask) return;
            
            const originalStatus = targetTask.status;
            const originalReason = targetTask.blockedReason;
            
            // Only manipulate arrays if not already moved by VueDraggable
            if (currentArrayCol?.name !== newStatus) {
                if (currentArrayCol) {
                    currentArrayCol.items = currentArrayCol.items.filter(i => i.id !== taskId);
                }
                const newCol = this.columns.find(c => c.name === newStatus);
                if(newCol) newCol.items.push(targetTask);
            }
            
            targetTask.status = newStatus;
            if(blockedReason !== undefined) targetTask.blockedReason = blockedReason;
            
            const payload: any = { newState: newStatus };
            if(blockedReason) payload.blockedReason = blockedReason;
            
            try {
                await axios.patch(`/api/v1/kanban-tasks/tasks/${taskId}/state`, payload);
            } catch(error) {
                console.warn("Fallo en Optimistic UI, revirtiendo estado...", error);
                
                targetTask.status = originalStatus;
                targetTask.blockedReason = originalReason;
                
                const currentNewCol = this.columns.find(c => c.name === newStatus);
                if(currentNewCol) {
                    currentNewCol.items = currentNewCol.items.filter(i => i.id !== taskId);
                }
                const originalCol = this.columns.find(c => c.name === originalStatus);
                if(originalCol) originalCol.items.push(targetTask);
                
                this.error = "Error al mover la tarjeta";
                throw error;
            }
        },
        async startTimer(taskId: string) {
            try {
                const res = await axios.post(`/api/v1/time-tracking/start`, { referenceId: taskId, referenceType: 'TASK_AGILE' });
                this.activeTimers[taskId] = res.data.id;
            } catch (e: any) {
                console.error("Error startTimer", e);
                throw e;
            }
        },
        async stopTimer(logId: string) {
            try {
                await axios.post(`/api/v1/time-tracking/stop/${logId}`);
                for(const key in this.activeTimers) {
                    if (this.activeTimers[key] === logId) {
                        delete this.activeTimers[key];
                        break;
                    }
                }
            } catch (e: any) {
                console.error("Error stopTimer", e);
                throw e;
            }
        },
        async addColumn(boardId: string, name: string) {
            try {
                const res = await axios.post(`/api/v1/kanban-tasks/boards/${boardId}/columns`, { name });
                const newCol = res.data;
                this.columns.push({ ...newCol, title: newCol.name, items: [] });
            } catch (e: any) {
                if(e.response?.status === 409) this.error = "Límite de columnas alcanzado.";
                throw e;
            }
        },
        async removeColumn(boardId: string, colId: string) {
            try {
                await axios.delete(`/api/v1/kanban-tasks/boards/${boardId}/columns/${colId}`);
                this.columns = this.columns.filter(c => c.id !== colId);
            } catch (e: any) {
                throw e;
            }
        },
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
                this.stompClient?.subscribe(`/topic/kanban/${this.boardId}/tasks`, (message) => {
                    if (message.body) {
                        try {
                            const eventTask = JSON.parse(message.body);
                            if (eventTask && eventTask.id && eventTask.status) {
                                // Eliminar de la columna vieja si existe
                                for(const col of this.columns) {
                                    col.items = col.items.filter(i => i.id !== eventTask.id);
                                }
                                // Añadir a la nueva columna
                                const newCol = this.columns.find(c => c.name === eventTask.status);
                                if (newCol) {
                                    newCol.items.push(eventTask);
                                }
                            }
                        } catch(e) {
                            console.error("Error procesando evento WS Kanban", e);
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
        }
    }
});
