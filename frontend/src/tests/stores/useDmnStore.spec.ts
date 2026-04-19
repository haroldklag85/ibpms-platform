import { setActivePinia, createPinia } from 'pinia';
import { useDmnStore } from '@/stores/useDmnStore';
import { api } from '@/services/apiClient';
import { describe, it, expect, beforeEach, vi } from 'vitest';

vi.mock('@/services/apiClient', () => ({
    api: {
        generateDmnRules: vi.fn()
    }
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
        const apiResponse = { data: { dmnXml: '<definitions></definitions>', confidence: 85 } };
        (api.generateDmnRules as any).mockResolvedValue(apiResponse);

        await store.generateFromPrompt('generar tabla clientes premium');
        
        expect(api.generateDmnRules).toHaveBeenCalledWith({ prompt: 'generar tabla clientes premium' });
        expect(store.generatedXml).toBe('<definitions></definitions>');
        expect(store.confidence).toBe(85);
        expect(store.generationError).toBeNull();
    });

    it('Maneja errores de caidas en Prompt NLP', async () => {
        const errorResp = { response: { data: { message: 'Infraccion RAG' } } };
        (api.generateDmnRules as any).mockRejectedValue(errorResp);

        try {
            await store.generateFromPrompt('bypassear');
        } catch(e) {
            // Ignorado intencionalmente el propagado
        }
        
        expect(store.generatedXml).toBeNull();
        expect(store.confidence).toBe(0);
        expect(store.generationError).toBe('Infraccion RAG');
    });

    it('Test CA-21: 422 XML Inválido expone el error SAX y evita renderizado', async () => {
        const errorResp = { response: { status: 422, data: { message: 'SAX Error: Unexpected character' } } };
        (api.generateDmnRules as any).mockRejectedValue(errorResp);

        try { await store.generateFromPrompt('prompt malo'); } catch(e) {}
        
        expect(store.generatedXml).toBeNull();
        expect(store.generationError).toBe('SAX Error: Unexpected character');
    });

    it('Test CA-22: 403 HIT_POLICY_FORBIDDEN emite evento hit-policy-forbidden', async () => {
        vi.spyOn(window, 'dispatchEvent');
        const errorResp = { response: { status: 403, data: { type: 'HIT_POLICY_FORBIDDEN' } } };
        (api.generateDmnRules as any).mockRejectedValue(errorResp);

        try { await store.generateFromPrompt('politica'); } catch(e) {}
        
        const dispatchedEvent = vi.mocked(window.dispatchEvent).mock.calls.find(call => call[0].type === 'hit-policy-forbidden');
        expect(dispatchedEvent).toBeDefined();
    });

    it('Test CA-23: 429 Activa contador Rate Limit basado en header Retry-After', async () => {
        vi.useFakeTimers();
        const errorResp = { response: { status: 429, headers: { 'retry-after': '3' } } };
        (api.generateDmnRules as any).mockRejectedValue(errorResp);

        try { await store.generateFromPrompt('rate'); } catch(e) {}
        
        expect(store.rateLimitSeconds).toBe(3);
        expect(store.isRateLimited).toBe(true);
        
        vi.advanceTimersByTime(1100);
        expect(store.rateLimitSeconds).toBe(2);
        
        vi.advanceTimersByTime(2000);
        expect(store.rateLimitSeconds).toBe(0);
        expect(store.isRateLimited).toBe(false);
        vi.useRealTimers();
    });

    it('Test CA-24: 504 Timeout activa prevents normal flow and sets requiresFallback', async () => {
        const errorResp = { response: { status: 504 } };
        (api.generateDmnRules as any).mockRejectedValue(errorResp);

        try { await store.generateFromPrompt('timeout'); } catch(e) {}
        
        expect(store.requiresFallback).toBe(true);
    });
});
