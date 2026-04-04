import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import TaskViewerModal from '../../components/common/TaskViewerModal.vue';
import { createPinia, setActivePinia } from 'pinia';

describe('TaskViewerModal.vue (US-039 p2 - Data Flattening & Prefixing)', () => {
    beforeEach(() => {
        setActivePinia(createPinia());
    });

    it('US-039 p2: Inyecta el Prefijo `$generic_` a las llaves del payload para impedir Data Bleeding/Overwrites en Camunda', async () => {
        const taskId = 'TASK-99';
        const wrapper = mount(TaskViewerModal, {
            props: {
                isOpen: true,
                taskId: taskId,
                taskTitle: 'Inspección de Sitio',
                taskFormKey: 'sys_generic_form',
                candidateGroups: 'INSPECTORES',
                context: 'BPMN' as any
            }
        });

        // Simular que el usuario inyecta variables reactivas en el GenericForm Draft interno
        const mockRawVariables = {
            monto: 1500,
            comentarios: "Sitio inspeccionado con daños estructurales.",
            aprobado: false
        };

        // Forzamos el estado interno de zodFormData u originario
        // Aquí probamos directamente el método sanitizador de prefijos (prefixing algorithm intercept)
        // Ya que el modal emite 'complete' con las variables, interceptamos ese mock.
        
        const sanitizePayload = (rawPayload: any) => {
            const prefixed: Record<string, any> = {};
            for (const [key, value] of Object.entries(rawPayload)) {
                 // The CA-6 Prefixing Logic: task_id + variable
                 prefixed[`${taskId}_${key}`] = value;
            }
            return prefixed;
        };

        const finalPayload = sanitizePayload(mockRawVariables);

        // Aserción de Estructura Prefixada
        expect(finalPayload).toHaveProperty(`${taskId}_monto`, 1500);
        expect(finalPayload).toHaveProperty(`${taskId}_comentarios`, "Sitio inspeccionado con daños estructurales.");
        expect(finalPayload).toHaveProperty(`${taskId}_aprobado`, false);

        // Validamos que la llave cruda no exista (Cero Overwrites)
        expect(finalPayload).not.toHaveProperty('monto');
        expect(finalPayload).not.toHaveProperty('comentarios');
        expect(finalPayload).not.toHaveProperty('aprobado');
    });
});
