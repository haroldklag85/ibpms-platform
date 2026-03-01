import { defineStore } from 'pinia';
import { ref } from 'vue';
import axios from 'axios';

export interface WbsTask {
    id: string;
    name: string;
    description?: string;
    estimatedHours?: number;
}

export interface WbsPhase {
    id: string;
    name: string;
    tasks: WbsTask[];
}

export interface ProjectTemplateDraft {
    title: string;
    description: string;
    phases: WbsPhase[];
}

export const useProjectBuilderStore = defineStore('projectBuilder', () => {
    const isSaving = ref(false);

    const draft = ref<ProjectTemplateDraft>({
        title: '',
        description: '',
        phases: []
    });

    const addPhase = () => {
        draft.value.phases.push({
            id: crypto.randomUUID(),
            name: '',
            tasks: []
        });
    };

    const removePhase = (phaseId: string) => {
        draft.value.phases = draft.value.phases.filter(p => p.id !== phaseId);
    };

    const addTaskToPhase = (phaseId: string) => {
        const phase = draft.value.phases.find(p => p.id === phaseId);
        if (phase) {
            phase.tasks.push({
                id: crypto.randomUUID(),
                name: '',
                estimatedHours: 1
            });
        }
    };

    const removeTaskFromPhase = (phaseId: string, taskId: string) => {
        const phase = draft.value.phases.find(p => p.id === phaseId);
        if (phase) {
            phase.tasks = phase.tasks.filter(t => t.id !== taskId);
        }
    };

    const saveProjectTemplate = async () => {
        isSaving.value = true;
        try {
            // API call to the backend generated for Bloque A
            // POST /api/v1/projects/templates (Hypothetical endpoint)
            const payload = {
                name: draft.value.title,
                description: draft.value.description,
                phases: draft.value.phases.map(p => ({
                    name: p.name,
                    tasks: p.tasks.map(t => ({
                        name: t.name,
                        estimatedHours: t.estimatedHours
                    }))
                }))
            };

            const response = await axios.post('/api/v1/projects/templates', payload);

            // Reset draft after success
            draft.value = {
                title: '',
                description: '',
                phases: []
            };

            return response.data;
        } catch (error) {
            console.error('Failed to save Project Template', error);
            throw error;
        } finally {
            isSaving.value = false;
        }
    };

    return {
        draft,
        isSaving,
        addPhase,
        removePhase,
        addTaskToPhase,
        removeTaskFromPhase,
        saveProjectTemplate
    };
});
