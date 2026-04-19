import { defineStore } from 'pinia';
import { api } from '@/services/apiClient';

export interface KanbanItem {
    id: string;
    title: string;
    status: string;
    createdAt: string;
    slaHours: number;
    hoursElapsed: number;
    assignee?: string;
    blockReason?: string;
    processName?: string;
    priority?: string;
}

export interface KanbanColumn {
    id: string;
    title: string;
    items: KanbanItem[];
    wipLimit?: number;
}

export const useKanbanStore = defineStore('kanban', {
    state: () => ({
        columns: [] as KanbanColumn[],
        loading: false,
        error: null as string | null
    }),
    actions: {
        async fetchBoard() {
            this.loading = true;
            this.error = null;
            try {
                const { data } = await api.getKanbanBoard();
                this.columns = data;
            } catch (error: any) {
                // If it fails (like offline/mock behavior in local dev without live backend), prepopulate a mock to prevent UI crash
                console.error("Error fetching board", error);
                this.columns = [
                    {
                        id: 'TODO',
                        title: 'Por Hacer',
                        items: [
                             { id: 'TASK-100', title: 'Revisión Documental', status: 'TODO', createdAt: new Date().toISOString(), slaHours: 24, hoursElapsed: 2, assignee: 'Ana Torres' }
                        ]
                    },
                    {
                        id: 'IN_PROGRESS',
                        title: 'En Progreso',
                        items: [],
                        wipLimit: 3
                    },
                    {
                        id: 'BLOCKED',
                        title: 'Bloqueado',
                        items: []
                    },
                    {
                        id: 'DONE',
                        title: 'Completado',
                        items: []
                    }
                ];
                this.error = "Error al conectar con el servidor.";
            } finally {
                this.loading = false;
            }
        },
        async moveTask(taskId: string, newStatus: string, blockReason?: string) {
            try {
                // Find and optimistically update
                let movedItem: KanbanItem | undefined;
                let oldStatus = '';
                for (const col of this.columns) {
                    const idx = col.items.findIndex(i => i.id === taskId);
                    if (idx > -1) {
                        movedItem = col.items.splice(idx, 1)[0];
                        oldStatus = col.id;
                        break;
                    }
                }

                if (movedItem) {
                    movedItem.status = newStatus;
                    if (blockReason) movedItem.blockReason = blockReason;
                    
                    const targetCol = this.columns.find(c => c.id === newStatus);
                    if (targetCol) {
                        targetCol.items.push(movedItem);
                    }
                }

                const payload: any = { status: newStatus };
                if (blockReason) payload.blockReason = blockReason;
                await api.updateKanbanStatus(taskId, payload);
            } catch (error) {
                // Rollback on failure
                this.fetchBoard(); // Re-sync
                throw error;
            }
        }
    }
});
