import { setActivePinia, createPinia } from 'pinia';
import { useTimeboxStore } from '@/stores/useTimeboxStore';
import { api } from '@/services/apiClient';
import { describe, it, expect, beforeEach, vi } from 'vitest';

vi.mock('@/services/apiClient', () => ({
    api: {
        getSlaLogs: vi.fn(),
        requestTimeboxExtension: vi.fn()
    }
}));

describe('useTimeboxStore (Sprint 5 - Iteration 2)', () => {
    let store: ReturnType<typeof useTimeboxStore>;

    beforeEach(() => {
        setActivePinia(createPinia());
        store = useTimeboxStore();
        vi.clearAllMocks();
    });

    it('estado inicial correcto', () => {
        expect(store.changelogs).toEqual([]);
        expect(store.isLoadingLogs).toBe(false);
        expect(store.isExtendingSla).toBe(false);
        expect(store.errorState).toBeNull();
        expect(store.hasActiveExtensions).toBe(false);
    });

    it('fetchLogs hidrata la bitácora exitosamente', async () => {
        const mockData = {
            data: {
                content: [{ id: 'log-1', reason: 'Mock' }]
            }
        };
        (api.getSlaLogs as any).mockResolvedValue(mockData);

        await store.fetchLogs('task-123');
        
        expect(api.getSlaLogs).toHaveBeenCalledWith('task-123');
        expect(store.changelogs.length).toBe(1);
        expect(store.changelogs[0].id).toBe('log-1');
        expect(store.errorState).toBeNull();
        expect(store.hasActiveExtensions).toBe(true);
    });

    it('fetchLogs maneja fallos estructurales', async () => {
        const errorMock = {
            response: { status: 403, data: { message: 'El perfil no tiene permisos' } }
        };
        (api.getSlaLogs as any).mockRejectedValue(errorMock);

        await store.fetchLogs('task-403');
        
        expect(store.changelogs.length).toBe(0);
        expect(store.errorState).toEqual({ code: '403', message: 'El perfil no tiene permisos' });
    });

    it('extendSla fluye correctamente y refresca bitácora', async () => {
        // Mock the extension
        (api.requestTimeboxExtension as any).mockResolvedValue({ status: 200 });
        
        // Mock the refresh logs
        const mockData = {
            data: {
                content: [{ id: 'log-extend', reason: 'Extension Exitosa' }]
            }
        };
        (api.getSlaLogs as any).mockResolvedValue(mockData);

        await store.extendSla('task-ok', { reason: 'Demora cliente', extensionHours: 24 });
        
        expect(api.requestTimeboxExtension).toHaveBeenCalledWith('task-ok', { reason: 'Demora cliente', extensionHours: 24 });
        expect(api.getSlaLogs).toHaveBeenCalledWith('task-ok'); // Verifies reactive refresh
        expect(store.changelogs[0].id).toBe('log-extend');
        expect(store.errorState).toBeNull();
    });

    it('extendSla captura Error de Idempotencia (Conflict 409 o 429)', async () => {
        const errorMock = {
            response: { status: 409, data: { code: 'IDEMPOTENCY_CONFLICT', message: 'Doble sumisión interceptada' } }
        };
        (api.requestTimeboxExtension as any).mockRejectedValue(errorMock);

        try {
            await store.extendSla('task-dup', { reason: 'Duplicado', extensionHours: 24 });
        } catch (e: any) {
            expect(e).toBe(errorMock);
        }
        
        expect(api.requestTimeboxExtension).toHaveBeenCalledTimes(1);
        expect(api.getSlaLogs).not.toHaveBeenCalled(); // No refresh if failed
        expect(store.errorState).toEqual({ code: 'IDEMPOTENCY_CONFLICT', message: 'Doble sumisión interceptada' });
    });
});
