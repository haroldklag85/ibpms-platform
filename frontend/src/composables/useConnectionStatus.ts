import { onMounted, onUnmounted } from 'vue';
import { useConnectionStore } from '@/stores/connectionStore';

export function useConnectionStatus() {
    const store = useConnectionStore();
    
    let offlineTimeout: ReturnType<typeof setTimeout> | null = null;
    let degradedTimeout: ReturnType<typeof setTimeout> | null = null;
    let restoreTimeout: ReturnType<typeof setTimeout> | null = null;
    let hideTimeout: ReturnType<typeof setTimeout> | null = null;

    const clearAllTimeouts = () => {
        if (offlineTimeout) clearTimeout(offlineTimeout);
        if (degradedTimeout) clearTimeout(degradedTimeout);
        if (restoreTimeout) clearTimeout(restoreTimeout);
        if (hideTimeout) clearTimeout(hideTimeout);
    };

    const handleOffline = () => {
        clearAllTimeouts();
        // CA-19: Debounce 5s
        offlineTimeout = setTimeout(() => {
            store.setStatus('OFFLINE');
            
            // CA-23: Si supera 15s (5s + 10s) pasa a DEGRADED
            degradedTimeout = setTimeout(() => {
                store.setStatus('DEGRADED');
            }, 10000);
        }, 5000);
    };

    const handleOnline = () => {
        clearAllTimeouts();
        
        if (store.status !== 'ONLINE') {
            store.setStatus('RECONNECTING');
            
            // CA-24: Reconexión Silenciosa en Background
            if (store.requiresRetry) {
                window.dispatchEvent(new CustomEvent('network-retry-dispatch'));
            }
            
            // CA-25: Feedback Positivo y Desvanecimiento de Éxito (3s)
            restoreTimeout = setTimeout(() => {
                store.setStatus('RESTORED');
                
                hideTimeout = setTimeout(() => {
                    store.setStatus('ONLINE');
                }, 3000);
            }, 1500); // Artificial delay to ensure user sees "Reconnecting..."
        }
    };

    const handleSilence = () => {
        store.silence();
    };

    onMounted(() => {
        window.addEventListener('offline', handleOffline);
        window.addEventListener('online', handleOnline);
        window.addEventListener('global-error-dispatch', handleSilence);
        window.addEventListener('optimistic-lock-dispatch', handleSilence);
    });

    onUnmounted(() => {
        // OBS-1: Cleanup de TODOS los timers y event listeners
        window.removeEventListener('offline', handleOffline);
        window.removeEventListener('online', handleOnline);
        window.removeEventListener('global-error-dispatch', handleSilence);
        window.removeEventListener('optimistic-lock-dispatch', handleSilence);
        clearAllTimeouts();
    });
}
