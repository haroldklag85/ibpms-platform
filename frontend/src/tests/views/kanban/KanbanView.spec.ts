import { mount } from '@vue/test-utils';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { createTestingPinia } from '@pinia/testing';
import KanbanView from '@/views/kanban/KanbanView.vue';
import { useKanbanStore } from '@/stores/kanbanStore';
import apiClient from '@/services/apiClient';

// @Traceability: US-008, CA-12

// Mock dialog component implicitly as VTU handles standard HTML unless imported globally. 
// Since we might use a standard HTML dialog or a dummy component, we ignore subcomponents if needed, or stub them.
vi.mock('@/services/apiClient', () => ({
    default: {
        get: vi.fn(),
        post: vi.fn(),
        put: vi.fn(),
        delete: vi.fn()
    }
}));

vi.mock('vue-router', () => ({
    useRoute: vi.fn(() => ({
        params: { projectId: 'test-project' }
    }))
}));

describe('KanbanView.vue', () => {
    let wrapper: any;

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('renders the board and calls the real API store on mount', async () => {
        wrapper = mount(KanbanView, {
            global: {
                plugins: [createTestingPinia({ createSpy: vi.fn, initialState: { kanban: { columns: [] } } })]
            }
        });
        
        const store = useKanbanStore();
        expect(store.fetchBoard).toHaveBeenCalled();
        expect(wrapper.text()).toContain('Tablero Kanban Interactivo');
    });

    it('displays a block reason modal when a task is moved to BLOCKED', async () => {
        wrapper = mount(KanbanView, {
            global: {
                plugins: [createTestingPinia({
                    createSpy: vi.fn,
                    initialState: {
                        auth: {
                            user: { roles: ['OPERATOR'] }
                        },
                        kanban: {
                            columns: [
                                { id: 'DOING', title: 'Doing', items: [{ id: 'T-1', title: 'Task 1', status: 'DOING' }] },
                                { id: 'BLOCKED', title: 'Blocked', items: [] }
                            ]
                        }
                    }
                })]
            }
        });

        await wrapper.vm.$nextTick();

        // Simulate item drop to blocked
        await wrapper.vm.handleItemMove({ item: { id: 'T-1', title: 'Task 1', status: 'DOING' }, newStatus: 'BLOCKED' });

        // Modal should appear
        expect(wrapper.vm.showBlockModal).toBe(true);
        expect(wrapper.vm.taskToBlock).toEqual({ id: 'T-1', title: 'Task 1', status: 'DOING' });
        
        await wrapper.vm.$nextTick();
        expect(wrapper.text()).toContain('Motivo de Bloqueo');
    });

    it('calls store.moveTask with blockReason when modal is confirmed', async () => {
        wrapper = mount(KanbanView, {
            global: {
                plugins: [createTestingPinia({
                    createSpy: vi.fn,
                    initialState: { kanban: { columns: [] } }
                })]
            }
        });

        const store = useKanbanStore();

        wrapper.vm.taskToBlock = { id: 'T-1', title: 'Task 1', status: 'DOING' };
        
        await wrapper.vm.confirmBlock('Falta documentación');

        expect(store.moveTask).toHaveBeenCalledWith('T-1', 'BLOCKED', 'Falta documentación');
        expect(wrapper.vm.showBlockModal).toBe(false);
    });

    it('shows readonly status for users without permissions', async () => {
        wrapper = mount(KanbanView, {
            global: {
                plugins: [createTestingPinia({
                    createSpy: vi.fn,
                    initialState: {
                        kanban: { columns: [{ id: 'TODO', title: 'To Do', items: [] }] }
                    }
                })]
            }
        });
        
        // This simulates a prop or state indicating readonly
        wrapper.vm.isReadonly = true;
        await wrapper.vm.$nextTick();

        // Should render a visual indicator or disable draggable layout
        expect(wrapper.text()).toContain('Modo Lectura');
    });
});
