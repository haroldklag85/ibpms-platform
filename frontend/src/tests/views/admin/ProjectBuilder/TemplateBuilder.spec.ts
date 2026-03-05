import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import TemplateBuilder from '@/views/admin/ProjectBuilder/TemplateBuilder.vue';
import { useProjectTemplateStore } from '@/stores/useProjectTemplateStore';

// Mock components
vi.mock('@/views/admin/ProjectBuilder/WbsTreeView.vue', () => ({
    default: { template: '<div>WBS Tree View Mock</div>' }
}));
vi.mock('@/views/admin/ProjectBuilder/PropertyInspector.vue', () => ({
    default: { template: '<div>Property Inspector Mock</div>' }
}));

const pinia = createPinia();

describe('TemplateBuilder.vue (Orphan Task Protection)', () => {
    beforeEach(() => {
        setActivePinia(pinia);
        const store = useProjectTemplateStore();
        store.$reset();

        // Mock successful template load
        vi.spyOn(store, 'loadTemplate').mockImplementation(async () => {
            store.template = {
                id: 'tpl-123',
                name: 'Test Project WBS',
                description: 'Testing WBS',
                status: 'DRAFT',
                dependencies: [],
                phases: [
                    {
                        id: 'phase-1',
                        name: 'Phase 1',
                        orderIndex: 0,
                        milestones: [
                            {
                                id: 'ms-1',
                                name: 'Milestone 1',
                                orderIndex: 0,
                                isStageGate: false,
                                tasks: [
                                    {
                                        id: 'task-1',
                                        name: 'Task With Form Key',
                                        estimatedHours: 8,
                                        formKey: 'form_payment_request',
                                        orderIndex: 0
                                    },
                                    {
                                        id: 'task-2',
                                        name: 'Task WITHOUT Form Key (Orphan)',
                                        estimatedHours: 4,
                                        formKey: null, // Orphan task
                                        orderIndex: 1
                                    }
                                ]
                            }
                        ]
                    }
                ]
            };
        });
    });

    it('Disables PUBLISH button if any task is missing a form_key', async () => {
        const wrapper = mount(TemplateBuilder, {
            global: {
                plugins: [pinia]
            }
        });

        // Wait for onMounted loadTemplate
        await wrapper.vm.$nextTick();
        await new Promise(r => setTimeout(r, 50));

        const store = useProjectTemplateStore();

        // Assert store computed logic is false
        expect(store.isPublishable).toBe(false);

        // Assert DOM Button state
        const publishBtn = wrapper.findAll('button').find(b => b.text().includes('[ PUBLICAR PLANTILLA ]'));
        expect(publishBtn).toBeDefined();

        // Button must be strictly disabled
        expect(publishBtn!.attributes('disabled')).toBeDefined();
    });

    it('Enables PUBLISH button when all tasks have valid form_keys', async () => {
        const wrapper = mount(TemplateBuilder, {
            global: {
                plugins: [pinia]
            }
        });

        await wrapper.vm.$nextTick();
        await new Promise(r => setTimeout(r, 50));

        const store = useProjectTemplateStore();

        // Fix the orphan task
        store.template!.phases[0].milestones[0].tasks[1].formKey = 'form_document_upload';

        await wrapper.vm.$nextTick();

        // Assert store computed logic is true
        expect(store.isPublishable).toBe(true);

        const publishBtn = wrapper.findAll('button').find(b => b.text().includes('[ PUBLICAR PLANTILLA ]'));

        // Button should no longer be disabled
        expect(publishBtn!.attributes('disabled')).toBeUndefined();
    });
});
