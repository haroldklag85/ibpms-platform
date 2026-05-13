import { defineStore } from 'pinia';
import { ref, watch } from 'vue';
import { api } from '@/services/apiClient';
import { fetchEventSource } from '@microsoft/fetch-event-source';
import { useTimeStore } from '@/stores/timeStore';

// @Traceability: Testabilidad J-02 (T-24)
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
    const isSseTimeout = ref(false);

    const generateFromPrompt = async (prompt: string) => {
        isGenerating.value = true;
        generationError.value = null;
        isSseTimeout.value = false;
        requiresFallback.value = false;
        generatedXml.value = '';

        const timeStore = useTimeStore();
        let startTick = timeStore.currentTick;
        let lastRowTick = timeStore.currentTick;
        let hasReceivedRows = false;
        const ctrl = new AbortController();

        const unwatch = watch(() => timeStore.currentTick, (current) => {
            if (!hasReceivedRows && current - startTick > 30000) {
                ctrl.abort();
                isSseTimeout.value = true;
                generationError.value = 'La generación tardó más de lo esperado. Pulse [🔄 Reintentar]';
                isGenerating.value = false;
                unwatch();
            } else if (hasReceivedRows && current - lastRowTick > 15000) {
                ctrl.abort();
                requiresFallback.value = true;
                generationError.value = 'Estancamiento de 15s en generación. Generación parcial recuperada.';
                isGenerating.value = false;
                unwatch();
            }
        });

        const clearTimers = () => {
            unwatch();
        };

        try {
            await fetchEventSource('/api/v1/dmn/generate-stream', {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + localStorage.getItem('token')
                },
                body: JSON.stringify({ prompt }),
                signal: ctrl.signal,
                onmessage(msg) {
                    if (!hasReceivedRows) {
                        hasReceivedRows = true;
                    }
                    lastRowTick = timeStore.currentTick;

                    if (msg.event === 'row') {
                        generatedXml.value += msg.data;
                    } else if (msg.event === 'confidence') {
                        confidence.value = parseFloat(msg.data);
                    } else if (msg.event === 'error') {
                        throw new Error(msg.data);
                    }
                },
                onclose() {
                    clearTimers();
                    isGenerating.value = false;
                },
                async onerror(err) {
                    clearTimers();
                    throw err;
                }
            });
        } catch (e: any) {
            clearTimers();
            console.error('NLP DMN Generation err', e);
            // Re-throw specific errors if needed by UI
            if (e.message && e.message.includes('403')) {
                window.dispatchEvent(new CustomEvent('hit-policy-forbidden'));
            } else if (e.message && e.message.includes('429')) {
                isRateLimited.value = true;
                rateLimitSeconds.value = 60;
            } else if (e.message && e.message.includes('504')) {
                requiresFallback.value = true;
            } else {
                generationError.value = e.message || 'Fallo de Generación NLP';
            }
            isGenerating.value = false;
        }
    };

    const resetState = () => {
        generatedXml.value = null;
        confidence.value = 0;
        generationError.value = null;
        isSseTimeout.value = false;
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
        isSseTimeout,
        generateFromPrompt,
        resetState,
        saveDmn
    };
});
