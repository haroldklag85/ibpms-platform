import apiClient from './apiClient';
import type { TaskDto, TaskListRequest } from '@/types/Task';

export class TaskService {

    /**
     * Obtiene la bandeja transaccional personal (Tareas asignadas al usuario activo)
     */
    static async getMyTasks(params?: TaskListRequest): Promise<TaskDto[]> {
        try {
            const response = await apiClient.get<TaskDto[]>('/tareas', { params });
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
            const response = await apiClient.get<TaskDto[]>('/tareas/candidatas', { params: modifiedParams });
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
            await apiClient.post(`/tareas/${taskId}/claim`);
        } catch (error) {
            console.error(`Error asignándose la tarea ${taskId}:`, error);
            throw error;
        }
    }
}
