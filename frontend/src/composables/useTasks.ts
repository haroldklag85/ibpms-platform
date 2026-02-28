import { ref } from 'vue';
import { TaskService } from '@/services/TaskService';
import type { TaskDto, TaskListRequest } from '@/types/Task';

export function useTasks() {
    const tasks = ref<TaskDto[]>([]);
    const candidates = ref<TaskDto[]>([]);
    const isLoading = ref<boolean>(false);
    const error = ref<string | null>(null);

    const fetchMyTasks = async (params?: TaskListRequest) => {
        isLoading.value = true;
        error.value = null;
        try {
            const data = await TaskService.getMyTasks(params);
            tasks.value = data;
        } catch (err: any) {
            error.value = err.message || 'Error cargando las tareas asignadas';
        } finally {
            isLoading.value = false;
        }
    };

    const fetchCandidateTasks = async (params?: TaskListRequest) => {
        isLoading.value = true;
        error.value = null;
        try {
            const data = await TaskService.getCandidateTasks(params);
            candidates.value = data;
        } catch (err: any) {
            error.value = err.message || 'Error cargando las tareas de la cola';
        } finally {
            isLoading.value = false;
        }
    };

    const claimTask = async (taskId: string) => {
        isLoading.value = true;
        error.value = null;
        try {
            await TaskService.claimTask(taskId);
            // Removemos temporalmente de la lista local o recargamos todo
            candidates.value = candidates.value.filter(t => t.id !== taskId);
        } catch (err: any) {
            error.value = err.message || `No se pudo tomar la tarea ${taskId}`;
            throw err; // Re-throw para capturarlo en la UI
        } finally {
            isLoading.value = false;
        }
    };

    return {
        tasks,
        candidates,
        isLoading,
        error,
        fetchMyTasks,
        fetchCandidateTasks,
        claimTask
    };
}
