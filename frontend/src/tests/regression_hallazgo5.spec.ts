import { describe, it, expect, vi, beforeEach } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useWorkdeskStore } from '@/stores/useWorkdeskStore';
import apiClient from '@/services/apiClient';

vi.mock('@/services/apiClient');

describe('Regression - Hallazgo 5 Consolidation Tests', () => {
    let store: ReturnType<typeof useWorkdeskStore>;

    beforeEach(() => {
        setActivePinia(createPinia());
        store = useWorkdeskStore();
        vi.spyOn(console, 'warn').mockImplementation(() => {});
        vi.spyOn(console, 'error').mockImplementation(() => {});
    });

    it('unclaimTask debe invocar el endpoint sin el prefijo duplicado /api/v1', async () => {
        const taskId = 'task-123';
        const msg = 'mensaje de prueba';
        
        vi.spyOn(apiClient, 'post').mockResolvedValueOnce({ data: { status: 'OK' } });

        await store.unclaimTask(taskId, msg);

        expect(apiClient.post).toHaveBeenCalledWith(
            `/workbox/tasks/${taskId}/unclaim`,
            { mensajeInterno: msg }
        );
    });

    it('bulkClaimTasks debe invocar el endpoint sin el prefijo duplicado /api/v1', async () => {
        const taskIds = ['t-1', 't-2'];
        
        vi.spyOn(apiClient, 'post').mockResolvedValueOnce({ data: { status: 'OK' } });

        await store.bulkClaimTasks(taskIds);

        expect(apiClient.post).toHaveBeenCalledWith(
            '/workbox/tasks/bulk-claim',
            taskIds
        );
    });
});
