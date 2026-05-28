// @Traceability: US-003 - CA-72
import { setActivePinia, createPinia } from 'pinia';
import { useFormStore } from '@/stores/useFormStore';
import { api } from '@/services/apiClient';
import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { ref } from 'vue';

vi.mock('@/services/apiClient', () => ({
    api: {
        completeTask: vi.fn()
    }
}));

const currentTickRef = ref(1700000000000);

vi.mock('@/stores/timeStore', () => ({
    useTimeStore: () => ({
        get currentTick() {
            return currentTickRef.value;
        },
        startEngine: vi.fn(),
        stopEngine: vi.fn(),
        getInactivityMs: vi.fn()
    })
}));

describe('FormStoreCA72 - Resiliencia Periférica Offline y Tolerancia a Conflictos', () => {
    let store: ReturnType<typeof useFormStore>;

    const tickSeconds = async (seconds: number) => {
        for (let i = 0; i < seconds; i++) {
            currentTickRef.value += 1000;
            vi.advanceTimersByTime(1000);
            await Promise.resolve();
            await Promise.resolve();
        }
    };

    beforeEach(() => {
        setActivePinia(createPinia());
        store = useFormStore();
        vi.clearAllMocks();
        vi.useFakeTimers();
        currentTickRef.value = 1700000000000;
        localStorage.clear();
    });

    afterEach(() => {
        vi.runOnlyPendingTimers();
        vi.useRealTimers();
        localStorage.clear();
    });

    it('Test Case 1: When store.submitForm fails with a network error, it must serialize the payload to localStorage under ibpms_network_fallback_${taskId} and re-throw the error', async () => {
        const taskId = 'task-net-err';
        const payload = { field1: 'value1', field2: 42 };
        
        // Mock a network error (no response object)
        const networkError = new Error('Network Error');
        (networkError as any).code = 'ERR_NETWORK';
        (api.completeTask as any).mockRejectedValue(networkError);

        await expect(store.submitForm(taskId, payload, false)).rejects.toThrow('Network Error');

        const savedPayload = localStorage.getItem(`ibpms_network_fallback_${taskId}`);
        expect(savedPayload).not.toBeNull();
        expect(JSON.parse(savedPayload!)).toEqual(payload);
    });

    it('Test Case 2: When store.submitForm fails with a server 5xx error, it must serialize the payload to localStorage under ibpms_network_fallback_${taskId} and re-throw the error', async () => {
        const taskId = 'task-5xx-err';
        const payload = { field1: 'value2', field2: 99 };

        // Mock a server 5xx error
        const serverError = {
            response: {
                status: 500,
                data: { message: 'Internal Server Error' }
            }
        };
        (api.completeTask as any).mockRejectedValue(serverError);

        await expect(store.submitForm(taskId, payload, false)).rejects.toEqual(serverError);

        const savedPayload = localStorage.getItem(`ibpms_network_fallback_${taskId}`);
        expect(savedPayload).not.toBeNull();
        expect(JSON.parse(savedPayload!)).toEqual(payload);
    });

    it('Test Case 3: When store.submitForm succeeds, it must clear the key ibpms_network_fallback_${taskId} from localStorage', async () => {
        const taskId = 'task-success';
        const payload = { field1: 'value3' };

        // Pre-populate fallback key
        localStorage.setItem(`ibpms_network_fallback_${taskId}`, JSON.stringify(payload));

        (api.completeTask as any).mockResolvedValue({ status: 200 });

        await store.submitForm(taskId, payload, false);

        expect(localStorage.getItem(`ibpms_network_fallback_${taskId}`)).toBeNull();
    });

    it('Test Case 4: When store.submitForm is called with enableUndo: true and the soft-undo timer expires, if completeTask fails with a network error, it must serialize the payload to localStorage and re-throw the error', async () => {
        const taskId = 'task-undo-err';
        const payload = { field1: 'value4' };

        const networkError = new Error('Network Error');
        (networkError as any).code = 'ERR_NETWORK';
        (api.completeTask as any).mockRejectedValue(networkError);

        // submitForm with enableUndo: true
        await store.submitForm(taskId, payload, true);

        // At this point, the API is not called yet, timer should be running
        expect(api.completeTask).not.toHaveBeenCalled();

        // Advance timer to trigger commitPendingSubmit
        await tickSeconds(6);

        // Check if the payload is saved in localStorage.
        const savedPayload = localStorage.getItem(`ibpms_network_fallback_${taskId}`);
        expect(savedPayload).not.toBeNull();
        expect(JSON.parse(savedPayload!)).toEqual(payload);
    });

    it('Test Case 5: When calling submitForm with a fifth parameter versionId, it must be passed in the If-Match header of the config object when invoking api.completeTask', async () => {
        const taskId = 'task-versioned';
        const payload = { field1: 'value5' };
        const versionId = 'opt-hash-123';

        (api.completeTask as any).mockResolvedValue({ status: 200 });

        // Using "as any" since the original typescript signature doesn't have 5th parameter yet
        await (store as any).submitForm(taskId, payload, false, false, versionId);

        expect(api.completeTask).toHaveBeenCalledWith(
            taskId,
            payload,
            expect.objectContaining({
                headers: expect.objectContaining({
                    'If-Match': versionId
                })
            })
        );
    });
});
