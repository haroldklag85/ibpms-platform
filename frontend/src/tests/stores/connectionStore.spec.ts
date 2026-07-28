import { setActivePinia, createPinia } from 'pinia';
import { useConnectionStore } from '@/stores/connectionStore';
import { describe, it, expect, beforeEach } from 'vitest';

describe('connectionStore', () => {
    beforeEach(() => {
        setActivePinia(createPinia());
    });

    it('should initialize with ONLINE status', () => {
        const store = useConnectionStore();
        expect(store.status).toBe('ONLINE');
        expect(store.isVisible).toBe(false);
    });

    it('should transition to OFFLINE and be visible', () => {
        const store = useConnectionStore();
        store.setStatus('OFFLINE');
        expect(store.status).toBe('OFFLINE');
        expect(store.isVisible).toBe(true);
        expect(store.currentLabel).toBe('Trabajando sin conexión');
    });

    it('should transition to RECONNECTING', () => {
        const store = useConnectionStore();
        store.setStatus('RECONNECTING');
        expect(store.status).toBe('RECONNECTING');
        expect(store.isVisible).toBe(true);
        expect(store.currentLabel).toBe('Reconectando...');
    });

    it('should transition to DEGRADED', () => {
        const store = useConnectionStore();
        store.setStatus('DEGRADED');
        expect(store.status).toBe('DEGRADED');
        expect(store.isVisible).toBe(true);
        expect(store.currentLabel).toBe('Modo sin conexión — los cambios se guardarán localmente');
    });

    it('should transition to RESTORED', () => {
        const store = useConnectionStore();
        store.setStatus('RESTORED');
        expect(store.status).toBe('RESTORED');
        expect(store.isVisible).toBe(true);
        expect(store.currentLabel).toBe('Conexión restaurada');
    });

    it('CA-26: Toast entra en SILENCED cuando ErrorStateGlobal está visible', () => {
        const store = useConnectionStore();
        store.setStatus('OFFLINE');
        store.silence();
        expect(store.isSilenced).toBe(true);
        expect(store.isVisible).toBe(false);
    });

    it('should restore visibility when unsilenced', () => {
        const store = useConnectionStore();
        store.setStatus('OFFLINE');
        store.silence();
        expect(store.isVisible).toBe(false);
        
        store.unsilence();
        expect(store.isVisible).toBe(true);
    });
});
