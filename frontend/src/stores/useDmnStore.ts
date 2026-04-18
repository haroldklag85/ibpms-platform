import { defineStore } from 'pinia';
import { ref } from 'vue';
import { api } from '@/services/apiClient';

export const useDmnStore = defineStore('dmnStore', () => {
    const generatedXml = ref<string | null>(null);
    const confidence = ref<number>(0);
    const isGenerating = ref(false);
    const generationError = ref<string | null>(null);

    const generateFromPrompt = async (prompt: string) => {
        isGenerating.value = true;
        generationError.value = null;
        try {
            const { data } = await api.generateDmnRules({ prompt });
            generatedXml.value = data.dmnXml || data.xml || '';
            confidence.value = data.confidence || 0;
            return data;
        } catch (e: any) {
            generationError.value = e.response?.data?.message || 'Fallo de Generación NLP';
            console.error('NLP DMN Generation err', e);
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

    return {
        generatedXml,
        confidence,
        isGenerating,
        generationError,
        generateFromPrompt,
        resetState
    };
});
