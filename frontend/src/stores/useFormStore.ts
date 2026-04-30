import { defineStore } from 'pinia';
import { ref } from 'vue';
import { z } from 'zod';
import { api } from '@/services/apiClient';
import { useConnectionStore } from '@/stores/connectionStore';

export const useFormStore = defineStore('formStore', () => {
    const formData = ref<Record<string, any>>({});
    const isDirty = ref(false);
    const isSubmitting = ref(false);
    const validationErrors = ref<Record<string, string>>({});
    
    // CA-29: Soft-Undo
    const isUndoAvailable = ref(false);
    const undoTimeLeft = ref(0);
    let undoTimer: ReturnType<typeof setInterval> | null = null;
    let pendingSubmitDraft: { taskId: string; payload: any } | null = null;

    // CA-31 & CA-32: Idempotency Retry Limit
    const idempotencyKey = ref('');

    const setFormData = (data: Record<string, any>, taskId?: string) => {
        formData.value = { ...data };
        isDirty.value = true;
        
        // Amnesia Cero: Persistir draft en local
        if (taskId) {
            localStorage.setItem(`ibpms_draft_${taskId}`, JSON.stringify(data));
        }
    };

    const loadLocalDraft = (taskId: string) => {
        const draft = localStorage.getItem(`ibpms_draft_${taskId}`);
        if (draft) {
            try {
                formData.value = JSON.parse(draft);
                isDirty.value = true;
                return true;
            } catch(e) {
                console.error('Error loading draft', e);
            }
        }
        return false;
    };

    const clearLocalDraft = (taskId: string) => {
        localStorage.removeItem(`ibpms_draft_${taskId}`);
    };

    const validateForm = (schema: z.ZodSchema): boolean => {
        validationErrors.value = {};
        const result = schema.safeParse(formData.value);
        if (!result.success) {
            const errors: Record<string, string> = {};
            result.error.errors.forEach((err) => {
                if (err.path[0]) {
                    errors[err.path[0].toString()] = err.message;
                }
            });
            validationErrors.value = errors;
            return false;
        }
        return true;
    };

    const saveDraft = async (taskId: string) => {
        try {
            await api.saveTaskDraft(taskId, formData.value);
            isDirty.value = false;
        } catch (e) {
            console.error('Failed to save draft', e);
            throw e;
        }
    };

    const submitForm = async (taskId: string, payload: any, enableUndo: boolean = true, isRetry: boolean = false) => {
        isSubmitting.value = true;
        try {
            if (enableUndo && !isRetry) {
                // Emulamos el envio retrasandolo para permitir soft-undo
                pendingSubmitDraft = { taskId, payload };
                startUndoTimer(5);
                return;
            }
            
            const connectionStore = useConnectionStore();
            if (!isRetry) {
                idempotencyKey.value = (typeof crypto !== 'undefined' && crypto.randomUUID) ? crypto.randomUUID() : Math.random().toString(36).substring(2);
                connectionStore.retryCount = 0;
            } else {
                connectionStore.retryCount++;
            }

            const config = { headers: { 'Idempotency-Key': idempotencyKey.value } };
            // Envío normal o Retry
            await api.completeTask(taskId, payload, config);
            
            clearLocalDraft(taskId);
            isDirty.value = false;
            formData.value = {};
            validationErrors.value = {};
            connectionStore.requiresRetry = false;
            connectionStore.retryCount = 0;
        } catch (e: any) {
            console.error('Failed to submit form', e);
            if (e.response && e.response.status === 400 && e.response.data && Array.isArray(e.response.data.errors)) {
                // CA-2 Validation Field-by-Field
                const backendErrors: Record<string, string> = {};
                e.response.data.errors.forEach((err: any) => {
                    backendErrors[err.field] = err.message;
                });
                validationErrors.value = backendErrors;
                throw new Error('ValidationFailed(RFC7807)');
            } else if (e.response && (e.response.status === 504 || typeof e.response.status === 'undefined')) {
                if (connectionStore.retryCount < 3) {
                    connectionStore.requiresRetry = true;
                } else {
                    connectionStore.requiresRetry = false;
                }
            } else if (e.response && e.response.status === 409 && e.response.data && e.response.data.type === 'SESSION_CONFLICT') {
                window.dispatchEvent(new CustomEvent('session-conflict-dispatch'));
            }
            throw e; // Lanzado para que el componente atrape y muestre Modal de rechazo Server-Side
        } finally {
            isSubmitting.value = false;
        }
    };

    const startUndoTimer = (seconds: number) => {
        isUndoAvailable.value = true;
        undoTimeLeft.value = seconds;
        
        if (undoTimer) clearInterval(undoTimer);
        
        undoTimer = setInterval(() => {
            undoTimeLeft.value--;
            if (undoTimeLeft.value <= 0) {
                commitPendingSubmit();
            }
        }, 1000);
    };

    const softUndo = () => {
        if (!isUndoAvailable.value) return false;
        
        if (undoTimer) clearInterval(undoTimer);
        isUndoAvailable.value = false;
        undoTimeLeft.value = 0;
        pendingSubmitDraft = null;
        isSubmitting.value = false;
        return true; // Undo exito
    };

    const commitPendingSubmit = async () => {
        if (!pendingSubmitDraft) return;
        
        if (undoTimer) clearInterval(undoTimer);
        isUndoAvailable.value = false;
        
        try {
            const config = { headers: { 'Idempotency-Key': idempotencyKey.value || ((typeof crypto !== 'undefined' && crypto.randomUUID) ? crypto.randomUUID() : Math.random().toString(36).substring(2)) } };
            await api.completeTask(pendingSubmitDraft.taskId, pendingSubmitDraft.payload, config);
            clearLocalDraft(pendingSubmitDraft.taskId);
            isDirty.value = false;
            formData.value = {};
        } catch (e) {
            console.error('Final commit failed', e);
            throw e;
        } finally {
            pendingSubmitDraft = null;
            isSubmitting.value = false;
        }
    };

    return {
        formData,
        isDirty,
        isSubmitting,
        validationErrors,
        isUndoAvailable,
        undoTimeLeft,
        idempotencyKey,
        setFormData,
        loadLocalDraft,
        clearLocalDraft,
        validateForm,
        saveDraft,
        submitForm,
        softUndo,
        commitPendingSubmit
    };
});
