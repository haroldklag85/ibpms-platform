import { ref } from 'vue';
import { TaskService } from '@/services/TaskService';
import type { TaskDto, TaskListRequest } from '@/types/Task';

export function useTasks() {
    const tasks = ref<TaskDto[]>([]);
    const candidates = ref<TaskDto[]>([]);
    const isLoading = ref<boolean>(false);
    const error = ref<string | null>(null);

    // ── Toast / Notification helpers ──────────────────────────────
    const toastSuccess = ref<string | null>(null);
    const toastError = ref<string | null>(null);

    const clearToasts = () => {
        toastSuccess.value = null;
        toastError.value = null;
    };

    const showSuccess = (msg: string) => {
        toastSuccess.value = msg;
        setTimeout(clearToasts, 4000);
    };

    const showError = (msg: string) => {
        toastError.value = msg;
        setTimeout(clearToasts, 6000);
    };

    // ── Fetch Tasks ──────────────────────────────────────────────
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

    // ── Claim ────────────────────────────────────────────────────
    const claimTask = async (taskId: string) => {
        isLoading.value = true;
        error.value = null;
        try {
            await TaskService.claimTask(taskId);
            candidates.value = candidates.value.filter(t => t.id !== taskId);
            showSuccess('✅ Tarea reclamada exitosamente');
        } catch (err: any) {
            if (err.response?.status === 409) {
                showError('❌ Tarea ya fue reclamada por un colega');
            } else {
                showError('Error del sistema reclamando tarea');
            }
            throw err;
        } finally {
            isLoading.value = false;
        }
    };

    // ── Unclaim (Liberar) ────────────────────────────────────────
    const unclaimTask = async (taskId: string, reason?: string) => {
        isLoading.value = true;
        error.value = null;
        try {
            await TaskService.unclaimTask(taskId, reason);
            tasks.value = tasks.value.filter(t => t.id !== taskId);
            showSuccess('✅ Tarea liberada al grupo exitosamente');
        } catch (err: any) {
            if (err.response?.status === 409) {
                showError('❌ Conflicto al liberar: la tarea ya fue reasignada');
            } else {
                showError('Error del sistema liberando la tarea');
            }
            throw err;
        } finally {
            isLoading.value = false;
        }
    };

    // ── Reassign (Reasignar) ─────────────────────────────────────
    const reassignTask = async (taskId: string, targetUserId: string) => {
        isLoading.value = true;
        error.value = null;
        try {
            await TaskService.reassignTask(taskId, targetUserId);
            tasks.value = tasks.value.filter(t => t.id !== taskId);
            showSuccess('✅ Tarea reasignada correctamente');
        } catch (err: any) {
            if (err.response?.status === 409) {
                showError('❌ Conflicto: la tarea ya cambió de estado');
            } else {
                showError('Error del sistema reasignando la tarea');
            }
            throw err;
        } finally {
            isLoading.value = false;
        }
    };

    // ── Peer Users (para dropdown de reasignación) ───────────────
    const peerUsers = ref<{ id: string; name: string; role: string }[]>([]);

    const fetchPeerUsers = async () => {
        peerUsers.value = await TaskService.getPeerUsers();
    };

    return {
        tasks,
        candidates,
        isLoading,
        error,
        toastSuccess,
        toastError,
        clearToasts,
        peerUsers,
        fetchMyTasks,
        fetchCandidateTasks,
        claimTask,
        unclaimTask,
        reassignTask,
        fetchPeerUsers
    };
}
