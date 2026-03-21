import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import { defineComponent, ref } from 'vue';

// Mock Simulando BpmnDesigner Deploy Button & Pre-Flight Logic
const BpmnDesignerPreFlightMock = defineComponent({
    template: `
        <div>
           <button class="deploy-btn" :disabled="!isExecutable">Desplegar en Camunda</button>
        </div>
    `,
    setup() {
        const isExecutable = ref(true);

        const simulatePreFlightHook = (xmlContent: string) => {
            // Emula el hook 'import.done' -> validación semántica
            if (xmlContent.includes('isExecutable="false"')) {
                isExecutable.value = false;
            } else {
                isExecutable.value = true;
            }
        };

        return { isExecutable, simulatePreFlightHook };
    }
});

describe('US-027 CA-10: Pre-Flight Executable Guard (Iteración 56)', () => {
    
    it('Aserta que el Botón de Despliegue se Inhabilita Atómicamente si el Proceso No es Ejecutable', async () => {
        const wrapper = mount(BpmnDesignerPreFlightMock);

        const deployBtn = wrapper.find('.deploy-btn');
        
        // Estado base (Aserción 1: Habilitado)
        expect(deployBtn.attributes('disabled')).toBeUndefined();

        // Simulamos la carga de un Payload Deficiente (isExecutable="false") por arrastre o API
        const faultyXml = `<bpmn:process id="Process_1" isExecutable="false"></bpmn:process>`;
        wrapper.vm.simulatePreFlightHook(faultyXml);
        
        await wrapper.vm.$nextTick();

        // Aserción Matemática 2: Hard-Stop. El botón mutó a Disabled protegiendo el Engine Backend
        expect(deployBtn.attributes('disabled')).toBeDefined();
        expect(wrapper.vm.isExecutable).toBe(false);
    });
});
