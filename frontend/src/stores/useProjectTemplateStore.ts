import { defineStore } from 'pinia';
import apiClient from '@/services/apiClient';

interface Task {
    id: string;
    name: string;
    estimatedHours: number;
    formKey: string | null;
    orderIndex: number;
}

interface Milestone {
    id: string;
    name: string;
    orderIndex: number;
    isStageGate: boolean;
    tasks: Task[];
}

interface Phase {
    id: string;
    name: string;
    orderIndex: number;
    milestones: Milestone[];
}

interface Dependency {
    sourceTaskId: string;
    targetTaskId: string;
    dependencyType: string;
    lagHours: number;
}

export interface ProjectTemplate {
    id: string | null;
    name: string;
    description: string;
    status: 'DRAFT' | 'PUBLISHED';
    phases: Phase[];
    dependencies: Dependency[];
}

export const useProjectTemplateStore = defineStore('projectTemplate', {
    state: () => ({
        template: null as ProjectTemplate | null,
        isLoading: false,
        selectedTaskIds: [] as string[] // Support single or multi-select, currently we just care about 1 for property inspector
    }),

    getters: {
        isPublished(state): boolean {
            return state.template?.status === 'PUBLISHED';
        },

        // UX Defensiva: AC-1
        isPublishable(state): boolean {
            if (!state.template || state.template.phases.length === 0) return false;

            for (const phase of state.template.phases) {
                for (const milestone of phase.milestones) {
                    if (milestone.tasks.length === 0) return false; // Al menos una tarea por hito
                    for (const task of milestone.tasks) {
                        if (!task.formKey || task.formKey.trim() === '') {
                            return false; // Found a task without a formKey
                        }
                    }
                }
            }
            return true;
        },

        selectedTask(state): Task | null {
            if (state.selectedTaskIds.length === 0) return null;
            const targetId = state.selectedTaskIds[0];

            for (const phase of state.template?.phases || []) {
                for (const ms of phase.milestones) {
                    const found = ms.tasks.find(t => t.id === targetId);
                    if (found) return found;
                }
            }
            return null;
        }
    },

    actions: {
        async loadTemplate(id: string) {
            this.isLoading = true;
            try {
                const response = await apiClient.get(`/design/projects/templates/${id}`);
                this.template = response.data;
            } catch (e) {
                console.error("Error loading template", e);
            } finally {
                this.isLoading = false;
            }
        },

        async deepSave() {
            if (!this.template) return;
            this.isLoading = true;
            try {
                const response = await apiClient.post('/design/projects/templates', this.template);
                if (!this.template.id && response.data.id) {
                    this.template.id = response.data.id;
                }
            } catch (e) {
                console.error("Validation Error from Backend", e);
                throw e;
            } finally {
                this.isLoading = false;
            }
        },

        async publishTemplate() {
            if (!this.template || !this.template.id) return;
            this.isLoading = true;
            try {
                await apiClient.post(`/design/projects/templates/${this.template.id}/publish`);
                this.template.status = 'PUBLISHED';
            } catch (e: any) {
                console.error(e.response?.data?.message || "Error publishing");
                throw e;
            } finally {
                this.isLoading = false;
            }
        },

        selectTask(taskId: string) {
            this.selectedTaskIds = [taskId];
        },

        updateSelectedTask(updates: Partial<Task>) {
            const task = this.selectedTask;
            if (task) {
                Object.assign(task, updates);
            }
        }
    }
});
