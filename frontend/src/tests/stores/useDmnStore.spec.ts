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
});
