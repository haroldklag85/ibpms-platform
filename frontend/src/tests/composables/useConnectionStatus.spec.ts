import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { useConnectionStatus } from '@/composables/useConnectionStatus';
import { setActivePinia, createPinia } from 'pinia';
import { useConnectionStore } from '@/stores/connectionStore';
import * as vue from 'vue';

vi.mock('vue', async () => {
    const actual: any = await vi.importActual('vue');
    return {
        ...actual,
        onMounted: vi.fn((fn) => fn()),
        onUnmounted: vi.fn()
    };
});

describe('useConnectionStatus', () => {
    let store: ReturnType<typeof useConnectionStore>;

    beforeEach(() => {
        setActivePinia(createPinia());
        store = useConnectionStore();
        vi.useFakeTimers();
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it('CA-19: debounces offline status by 5 seconds', () => {
        useConnectionStatus();
        
        window.dispatchEvent(new Event('offline'));
        
        // Before 5 seconds, status should still be ONLINE
        vi.advanceTimersByTime(3000);
        expect(store.status).toBe('ONLINE');
        
        // After 5 seconds, status should be OFFLINE
        vi.advanceTimersByTime(2000);
        expect(store.status).toBe('OFFLINE');
    });

    it('CA-19: ignores micro-cuts (offline then online before 5s)', () => {
        useConnectionStatus();
        
        window.dispatchEvent(new Event('offline'));
        vi.advanceTimersByTime(2000);
        
        // Reconnects before 5s debounce ends
        window.dispatchEvent(new Event('online'));
        vi.advanceTimersByTime(3000);
        
        // Should remain ONLINE the whole time
        expect(store.status).toBe('ONLINE');
    });

    it('registers window event listeners', () => {
        const addSpy = vi.spyOn(window, 'addEventListener');
        
        useConnectionStatus();
        
        expect(addSpy).toHaveBeenCalledWith('online', expect.any(Function));
        expect(addSpy).toHaveBeenCalledWith('offline', expect.any(Function));
    });
});
