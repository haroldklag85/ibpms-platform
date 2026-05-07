import { setActivePinia, createPinia } from 'pinia';
import { useDmnStore } from '@/stores/useDmnStore';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { fetchEventSource } from '@microsoft/fetch-event-source';

vi.mock('@microsoft/fetch-event-source', () => ({
    fetchEventSource: vi.fn()
}));

describe('useDmnStore', () => {
    let store: ReturnType<typeof useDmnStore>;

    beforeEach(() => {
        setActivePinia(createPinia());
        store = useDmnStore();
        vi.clearAllMocks();
    });

    it('Inicializa vacio', () => {
        expect(store.generatedXml).toBeNull();
        expect(store.confidence).toBe(0);
        expect(store.isGenerating).toBe(false);
    });

    it('Ejecuta generateFromPrompt y populariza NLP XML', async () => {
        (fetchEventSource as any).mockImplementation(async (url: string, options: any) => {
            options.onmessage({ event: 'row', data: '<definitions></definitions>' });
            options.onmessage({ event: 'confidence', data: '85' });
            options.onclose();
        });

        await store.generateFromPrompt('generar tabla clientes premium');
        
        expect(fetchEventSource).toHaveBeenCalledWith('/api/v1/dmn/generate-stream', expect.anything());
        expect(store.generatedXml).toBe('<definitions></definitions>');
        expect(store.confidence).toBe(85);
        expect(store.generationError).toBeNull();
    });

    it('Maneja errores de caidas en Prompt NLP', async () => {
        (fetchEventSource as any).mockImplementation(async (url: string, options: any) => {
            throw new Error('Infraccion RAG');
        });

        try {
            await store.generateFromPrompt('bypassear');
        } catch(e) { }
        
        expect(store.generatedXml).toBe('');
        expect(store.confidence).toBe(0);
        expect(store.generationError).toBe('Infraccion RAG');
    });

    it('Test CA-21: 422 XML Inválido expone el error SAX y evita renderizado', async () => {
        (fetchEventSource as any).mockImplementation(async (url: string, options: any) => {
            throw new Error('SAX Error: Unexpected character');
        });

        try { await store.generateFromPrompt('prompt malo'); } catch(e) {}
        
        expect(store.generatedXml).toBe('');
        expect(store.generationError).toBe('SAX Error: Unexpected character');
    });

    it('Test CA-22: 403 HIT_POLICY_FORBIDDEN emite evento hit-policy-forbidden', async () => {
        vi.spyOn(window, 'dispatchEvent');
        (fetchEventSource as any).mockImplementation(async (url: string, options: any) => {
            throw new Error('403 HIT_POLICY_FORBIDDEN');
        });

        try { await store.generateFromPrompt('politica'); } catch(e) {}
        
        const dispatchedEvent = vi.mocked(window.dispatchEvent).mock.calls.find(call => call[0].type === 'hit-policy-forbidden');
        expect(dispatchedEvent).toBeDefined();
    });

    it('Test CA-23: 429 Activa contador Rate Limit basado en header Retry-After', async () => {
        (fetchEventSource as any).mockImplementation(async (url: string, options: any) => {
            throw new Error('429');
        });

        try { await store.generateFromPrompt('rate'); } catch(e) {}
        
        expect(store.rateLimitSeconds).toBe(60);
        expect(store.isRateLimited).toBe(true);
    });

    it('Test CA-24: 504 Timeout activa prevents normal flow and sets requiresFallback', async () => {
        (fetchEventSource as any).mockImplementation(async (url: string, options: any) => {
            throw new Error('504');
        });

        try { await store.generateFromPrompt('timeout'); } catch(e) {}
        
        expect(store.requiresFallback).toBe(true);
    });
});
