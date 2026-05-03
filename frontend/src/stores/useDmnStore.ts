import { defineStore } from 'pinia';
import { ref } from 'vue';
import { api } from '@/services/apiClient';

export const useDmnStore = defineStore('dmnStore', () => {
    const generatedXml = ref<string | null>(null);
    const confidence = ref<number>(0);
    const isGenerating = ref(false);
    const generationError = ref<string | null>(null);

    // ERRORES DEFS
    const rateLimitSeconds = ref(0);
    const isRateLimited = ref(false);
    let rateLimitTimer: ReturnType<typeof setInterval> | null = null;
    const requiresFallback = ref(false);
    const isManual = ref(false);

    const generateFromPrompt = async (prompt: string) => {
        isGenerating.value = true;
        generationError.value = null;
        try {
            const { data } = await api.generateDmnRules({ prompt });
            generatedXml.value = data.dmnXml || data.xml || '';
            confidence.value = data.confidence || 0;
            return data;
        } catch (e: any) {
            console.error('NLP DMN Generation err', e);
            if (e.response) {
                const status = e.response.status;
                const data = e.response.data;

                if (status === 422) {
                    generationError.value = data?.message || 'XML Generado Inválido';
                } else if (status === 403 && data?.type === 'HIT_POLICY_FORBIDDEN') {
                    window.dispatchEvent(new CustomEvent('hit-policy-forbidden'));
                } else if (status === 429) {
                    const retryAfter = parseInt(e.response.headers?.['retry-after'] || '0', 10);
                    if (retryAfter > 0) {
                        isRateLimited.value = true;
                        rateLimitSeconds.value = retryAfter;
                        if (rateLimitTimer) clearInterval(rateLimitTimer);
                        rateLimitTimer = setInterval(() => {
                            rateLimitSeconds.value--;
                            if (rateLimitSeconds.value <= 0) {
                                isRateLimited.value = false;
                                if (rateLimitTimer) clearInterval(rateLimitTimer);
                            }
                        }, 1000);
                    }
                } else if (status === 504) {
                    requiresFallback.value = true;
                } else {
                    generationError.value = data?.message || 'Fallo de Generación NLP';
                }
            } else {
                generationError.value = 'Fallo de Generación NLP';
            }
            throw e;
        } finally {
            isGenerating.value = false;
        }
    };

    const resetState = () => {
        generatedXml.value = null;
        confidence.value = 0;
        generationError.value = null;
    };

    const saveDmn = async (dmnId: string) => {
        const payload = { xmlContent: generatedXml.value, isManual: isManual.value };
        await api.updateDmnModel(dmnId, payload);
    };

    return {
        generatedXml,
        confidence,
        isGenerating,
        generationError,
        isRateLimited,
        rateLimitSeconds,
        requiresFallback,
        isManual,
        generateFromPrompt,
        resetState,
        saveDmn
    };
});
