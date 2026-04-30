import apiClient from './apiClient';
import type { TaskDto, TaskListRequest } from '@/types/Task';

export class TaskService {

    /**
     * Obtiene la bandeja transaccional personal (Tareas asignadas al usuario activo)
     */
    static async getMyTasks(params?: TaskListRequest): Promise<TaskDto[]> {
        try {
            const response = await apiClient.get<TaskDto[]>('/tasks', { params });
            return response.data;
        } catch (error) {
            console.error('Error obteniendo mis tareas:', error);
            throw error;
        }
    }

    /**
     * Obtiene las tareas "Candidatas" que no tienen dueño pero pertenecen a los grupos del usuario
     */
    static async getCandidateTasks(params?: TaskListRequest): Promise<TaskDto[]> {
        try {
            // Usamos el query params ?candidatas=true u otra convención del backend
            const modifiedParams = { ...params, assigned: false };
            const response = await apiClient.get<TaskDto[]>('/tasks/candidatas', { params: modifiedParams });
            return response.data;
        } catch (error) {
            console.error('Error obteniendo tareas candidatas:', error);
            throw error;
        }
    }

    /**
     * Método de mutación: El usuario toma ownership de una tarea grupal
     */
    static async claimTask(taskId: string): Promise<void> {
        try {
            await apiClient.post(`/tasks/${taskId}/claim`);
        } catch (error) {
            console.error(`Error asignándose la tarea ${taskId}:`, error);
            throw error;
        }
    }

    /**
     * Liberar (unclaim) una tarea propia, devolviéndola a la cola grupal.
     */
    static async unclaimTask(taskId: string, reason?: string): Promise<void> {
        try {
            await apiClient.post(`/tasks/${taskId}/unclaim`, { reason });
        } catch (error) {
            console.error(`Error liberando la tarea ${taskId}:`, error);
            throw error;
        }
    }

    /**
     * Reasignar una tarea a otro usuario (peer o superior).
     */
    static async reassignTask(taskId: string, targetUserId: string): Promise<void> {
        try {
            await apiClient.post(`/tasks/${taskId}/reassign`, { assignee: targetUserId });
        } catch (error) {
            console.error(`Error reasignando la tarea ${taskId}:`, error);
            throw error;
        }
    }

    /**
     * Obtener lista de usuarios pares/superiores para el dropdown de reasignación.
     */
    static async getPeerUsers(): Promise<{ id: string; name: string; role: string }[]> {
        try {
            const response = await apiClient.get<{ id: string; name: string; role: string }[]>('/users/peers');
            return response.data;
        } catch (error) {
            console.error('Error obteniendo lista de peers:', error);
            // Fallback: mock data para desarrollo
            return [
                { id: 'user-001', name: 'Ana García', role: 'Operador SAC' },
                { id: 'user-002', name: 'Carlos Mendoza', role: 'Líder SAC' },
                { id: 'user-003', name: 'Laura Ríos', role: 'Supervisor' },
                { id: 'user-004', name: 'Miguel Torres', role: 'Operador SAC' }
            ];
        }
    }

    /**
     * CA-5 + CA-14: Asignación masiva interactiva
     */
    static async bulkAssign(projectId: string, taskIds: string[], userId: string): Promise<void> {
        try {
            await apiClient.post(`/agile/projects/${projectId}/tasks/bulk-assign`, {
                taskIds,
                userId
            });
        } catch (error) {
            console.error('Error en asignación masiva de tareas:', error);
            throw error;
        }
    }
}
