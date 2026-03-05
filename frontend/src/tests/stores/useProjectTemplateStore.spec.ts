import { describe, it, expect, beforeEach } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useProjectTemplateStore } from '@/stores/useProjectTemplateStore';

describe('useProjectTemplateStore (Epic 8 UX Defensive Rules)', () => {
    beforeEach(() => {
        setActivePinia(createPinia());
    });

    it('isPublishable debe ser FALSE si hay una o mas Tareas sin formKey (null)', () => {
        const store = useProjectTemplateStore();

        // Inyectar estado mock invalido
        store.template = {
            id: "test",
            name: "Test",
            description: "",
            status: "DRAFT",
            phases: [{
                id: "p1", name: "P1", orderIndex: 0,
                milestones: [{
                    id: "m1", name: "M1", orderIndex: 0, isStageGate: false,
                    tasks: [
                        { id: "t1", name: "T1", estimatedHours: 10, formKey: "valid_form", orderIndex: 0 },
                        { id: "t2", name: "T2", estimatedHours: 10, formKey: null, orderIndex: 1 } // INVALID
                    ]
                }]
            }],
            dependencies: []
        } as any;

        expect(store.isPublishable).toBe(false);
    });

    it('isPublishable debe ser TRUE si todas las tareas tienen formKey', () => {
        const store = useProjectTemplateStore();

        // Inyectar estado mock valido
        store.template = {
            id: "test",
            name: "Test",
            description: "",
            status: "DRAFT",
            phases: [{
                id: "p1", name: "P1", orderIndex: 0,
                milestones: [{
                    id: "m1", name: "M1", orderIndex: 0, isStageGate: false,
                    tasks: [
                        { id: "t1", name: "T1", estimatedHours: 10, formKey: "valid_form", orderIndex: 0 },
                        { id: "t2", name: "T2", estimatedHours: 10, formKey: "also_valid", orderIndex: 1 } // VALID
                    ]
                }]
            }],
            dependencies: []
        } as any;

        expect(store.isPublishable).toBe(true);
    });

    it('Al modificar el selectedTask, se recalcula isPublishable dinamicamente en la RAM (TopSort Mock)', () => {
        const store = useProjectTemplateStore();

        // Instanciar template Invalido (Un null)
        store.template = {
            id: "t_dynamic", status: "DRAFT", dependencies: [], name: 'Test', description: '',
            phases: [{
                id: "p", name: "P", orderIndex: 0,
                milestones: [{
                    id: "m", name: "M", isStageGate: false, orderIndex: 0,
                    tasks: [{ id: "t_target", name: "Target", estimatedHours: 5, formKey: null as any, orderIndex: 0 }]
                }]
            }]
        };

        expect(store.isPublishable).toBe(false); // Invalido al principio

        // Seleccionar tarea
        store.selectTask("t_target");
        const selected = store.selectedTask;
        expect(selected?.id).toBe("t_target");

        // Actualizar el formKey emulando el comportamiento del input del PropertyInspector
        store.updateSelectedTask({ formKey: "ahora_tengo_form" });

        // Evaluar la mutabilidad global
        expect(store.selectedTask?.formKey).toBe("ahora_tengo_form");
        // La magia de Pinia Computed getters
        expect(store.isPublishable).toBe(true);
    });

});
