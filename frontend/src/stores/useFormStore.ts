import { defineStore } from 'pinia';
import { ref } from 'vue';
import { z } from 'zod';
import { api } from '@/services/apiClient';

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

    const setFormData = (data: Record<string, any>) => {
        formData.value = { ...data };
        isDirty.value = true;
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

    const submitForm = async (taskId: string, payload: any, enableUndo: boolean = true) => {
        isSubmitting.value = true;
        try {
            if (enableUndo) {
                // Emulamos el envio retrasandolo para permitir soft-undo
                pendingSubmitDraft = { taskId, payload };
                startUndoTimer(5);
                return;
            }
            
            // Envío normal
            await api.completeTask(taskId, payload);
            isDirty.value = false;
            formData.value = {};
            validationErrors.value = {};
        } catch (e: any) {
            console.error('Failed to submit form', e);
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
            await api.completeTask(pendingSubmitDraft.taskId, pendingSubmitDraft.payload);
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
        setFormData,
        validateForm,
        saveDraft,
        submitForm,
        softUndo,
        commitPendingSubmit
    };
});
