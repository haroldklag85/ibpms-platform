import { mount, flushPromises } from '@vue/test-utils';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import BpmnDesigner from './BpmnDesigner.vue';

// Mock console.error to keep tests clean from bpmn-js errors in test env
vi.stubGlobal('console', {
    ...console,
    error: vi.fn(),
    log: vi.fn()
});

describe('Pantalla 6: BPMN Designer (Frontend QA)', () => {
    let wrapper: any;

    beforeEach(() => {
        // Mount the component before each test
        wrapper = mount(BpmnDesigner, {
            global: {
                stubs: {
                    Transition: false
                }
            }
        });
    });

    afterEach(() => {
        wrapper.unmount();
        vi.clearAllTimers();
    });

    // 1. Test Auto-Save
    it('Debe tener un timer de auto-guardado que invoca saveDraft cada 30 segundos (Auto-Save)', async () => {
        vi.useFakeTimers();
        const saveSpy = vi.spyOn(wrapper.vm, 'saveDraft');

        // Avanzar 31 segundos
        vi.advanceTimersByTime(31000);

        expect(saveSpy).toHaveBeenCalled();
        vi.useRealTimers();
    });

    // 2. Test Invalidación Pre-Flight
    it('Debe invalidar el Pre-Flight (estado PENDING) al detectar cambios en el diagrama', async () => {
        // Estado inicial forzado a Validado
        wrapper.vm.preFlightStatus = 'VALIDATED';
        expect(wrapper.vm.preFlightStatus).toBe('VALIDATED');

        // Simular un cambio en el diagrama
        wrapper.vm.onDiagramEdit();

        // Verificar que vuelve a Pending
        expect(wrapper.vm.preFlightStatus).toBe('PENDING');
    });

    // 3. Test FormKey Dropdown (Patrón Simple vs Maestro)
    it('Debe filtrar la lista de formularios para que solo muestre Formularios Simples cuando el proceso es Simple', async () => {
        // Al instanciar, el proceso es SIMPLE por defecto
        expect(wrapper.vm.processPattern).toBe('SIMPLE');

        // Revisamos la computed property de formularios filtrados
        const forms = wrapper.vm.filteredForms;
        expect(forms.every((f: any) => f.type === 'SIMPLE')).toBe(true);
        expect(forms.some((f: any) => f.type === 'MAESTRO')).toBe(false);

        // Si cambiamos a Maestro, deberían salir todos
        wrapper.vm.processPattern = 'IFORM_MAESTRO';
        await wrapper.vm.$nextTick();
        const formsMaestro = wrapper.vm.filteredForms;
        expect(formsMaestro.length).toBeGreaterThan(forms.length);
    });

    // 4. Test Service Task Hub
    it('Debe contener los conectores V1 (O365, SharePoint, NetSuite) en el Dropdown de Connectors', () => {
        const connectors = wrapper.vm.availableConnectors;
        const names = connectors.map((c: any) => c.name);

        expect(names).toContain('O365/Exchange');
        expect(names).toContain('SharePoint MS');
        expect(names).toContain('Oracle NetSuite');
    });

    // 5. Test Complejidad Bpmn
    it('Debe generar un Toast de Advertencia al importar un archivo BPMN de alta complejidad (> 100 nodos)', async () => {
        // Crear un archivo falso con 102 nodos bpmn
        const mockBigBPMN = Array(102).fill('<bpmn:task id="t1" />').join('\\n');
        const mockFile = new File([mockBigBPMN], 'complex.bpmn', { type: 'text/xml' });

        // Mock event
        const event = {
            target: {
                files: [mockFile]
            }
        };

        // Invocar importación
        await wrapper.vm.handleFileUpload(event as any);

        // Validar visualización del Toast Error por complejidad
        expect(wrapper.vm.toast.type).toBe('error');
        expect(wrapper.vm.toast.msg).toContain('Alta complejidad');
    });
});
