import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import ModelerWorkspace from '../../views/ModelerWorkspace.vue';

// Mock de LocalStorage BPO
const localStorageMock = (() => {
    let store: Record<string, string> = {};
    return {
        getItem: vi.fn((key: string) => store[key] || null),
        setItem: vi.fn((key: string, value: string) => { store[key] = value; }),
        removeItem: vi.fn((key: string) => { delete store[key]; }),
        clear: vi.fn(() => { store = {}; })
    };
})();

Object.defineProperty(window, 'localStorage', { value: localStorageMock });

describe('US-007 CA-03: Retención Transaccional de Modeler (Iteración 48)', () => {
    beforeEach(() => {
        window.localStorage.clear();
        vi.clearAllMocks();
    });

    it('El XML sobrevive a un reinicio abrupto (Unmount) salvaguardado en LocalStorage', async () => {
        const wrapper = mount(ModelerWorkspace);

        const dummyXml = '<?xml version="1.0" encoding="UTF-8"?><bpmn:definitions id="Definitions_1"></bpmn:definitions>';
        
        // Simulamos la interacción que inyecta XML en la caché antes de cerrar
        wrapper.vm.saveDraftLocally(dummyXml);
        expect(localStorage.setItem).toHaveBeenCalledWith('ibpms_draft_xml', dummyXml);
        
        // Simulamos la caída de navegador o cambio de página (Unmount)
        wrapper.unmount();
        
        // Aserción Matemática: La información persiste íntegra a pesar de la destrucción del DOM
        const savedDraft = window.localStorage.getItem('ibpms_draft_xml');
        expect(savedDraft).toBe(dummyXml);
    });

    it('El Modeler pulveriza (Purge) la Caché inmediatamente después de un Post de Guardado Exitoso', async () => {
        // Pre-condicionamos el localStorage con un borrador
        window.localStorage.setItem('ibpms_draft_xml', '<bpmn:draft/>');
        const wrapper = mount(ModelerWorkspace);
        
        // Simulamos (Mock) que el Axios o fetch de Guardado retorna Status 200/201
        vi.spyOn(wrapper.vm, 'postToServer').mockResolvedValue(true);

        await wrapper.vm.publishModelToServer();
        
        // Aserción: Al confirmar el éxito, la memoria caché debe purgarse para evitar solapamientos
        expect(localStorage.removeItem).toHaveBeenCalledWith('ibpms_draft_xml');
        expect(window.localStorage.getItem('ibpms_draft_xml')).toBeNull();
    });
});
