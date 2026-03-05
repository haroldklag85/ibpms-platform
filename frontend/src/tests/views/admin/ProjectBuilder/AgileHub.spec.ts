import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, config } from '@vue/test-utils';
import AgileHub from '@/views/admin/ProjectBuilder/AgileHub.vue';
import { api } from '@/services/apiClient';
import apiClient from '@/services/apiClient';

// Mock Frappe Gantt which depends on DOM SVG creation not present in JSDOM
vi.mock('frappe-gantt', () => {
    return {
        default: vi.fn().mockImplementation(() => {
            return {
                change_view_mode: vi.fn()
            };
        })
    };
});

describe('Epic 10.B Gantt Wrapper (QA)', () => {
    let wrapper: any;

    beforeEach(async () => {
        // Mock API response with 1 task that has NO assignee
        vi.spyOn(apiClient, 'get').mockResolvedValue({
            data: [
                {
                    id: 'task-1',
                    name: 'Design DB',
                    start: '2026-08-12',
                    end: '2026-08-14',
                    progress: 0,
                    dependencies: '',
                    status: 'PENDING',
                    assigneeUserId: null // Unassigned
                }
            ]
        });

        wrapper = mount(AgileHub, {
            global: {
                stubs: {
                    ResourcePanel: true
                }
            }
        });

        // Esperar mount loading API
        await wrapper.vm.$nextTick();
        await new Promise(resolve => setTimeout(resolve, 50)); // flush promises
    });

    it('Fijar Línea Base nace estrictamente bloqueado si existen tareas sin asignar', async () => {
        // Se asegura matemáticamente que compute disables el botón
        expect(wrapper.vm.hasUnassignedTasks).toBe(true);

        const button = wrapper.find('button.flash-glow');
        if (button.exists()) {
            expect(button.attributes('disabled')).toBeDefined();
        } else {
            // El botón original podría no tener flash-glow por la clase dinámica
            const baselineBtn = wrapper.findAll('button').find((btn: any) => btn.text().includes('FIJAR LÍNEA BASE'));
            expect(baselineBtn?.attributes('disabled')).toBeDefined();
        }
    });

    it('Habilita la Línea Base si el sistema confirma que se han instanciado recursos en TODAS las tareas', async () => {
        // En el lifecycle del componente, si cambiamos el unassigned a un userId, el trigger computed cambia
        wrapper.vm.rawTasks[0].assigneeUserId = "user-1234";

        await wrapper.vm.$nextTick();

        expect(wrapper.vm.hasUnassignedTasks).toBe(false);

        const baselineBtn = wrapper.findAll('button').find((btn: any) => btn.text().includes('FIJAR LÍNEA BASE'));
        expect(baselineBtn?.attributes('disabled')).toBeUndefined();
    });
});
