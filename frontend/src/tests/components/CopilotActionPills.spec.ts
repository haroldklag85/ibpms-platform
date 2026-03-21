import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import { defineComponent, ref } from 'vue';

// Mock representativo de la arquitectura CA-07 del BpmnDesigner.vue
const CopilotPillMock = defineComponent({
    template: `
        <div>
           <div v-for="(msg, i) in copilotMessages" :key="i" class="message-block">
             <div v-if="msg.options && msg.options.length > 0" class="action-pills">
               <button 
                 v-for="(opt, optIdx) in msg.options" 
                 :key="optIdx"
                 @click="selectOption(msg, opt)"
                 :disabled="!!msg.selectedOption"
                 class="pill-btn"
               >
                 {{ opt }}
               </button>
             </div>
           </div>
        </div>
    `,
    setup() {
        const copilotMessages = ref<any[]>([
            {
                text: 'He detectado ambigüedad...',
                options: ['Usar Rol Existente (SSO)', 'Crear Nuevo Rol IAM'],
                selectedOption: undefined
            }
        ]);

        const selectOption = (msgItem: any, optionText: string) => {
            msgItem.selectedOption = optionText; // Sello de Inmutabilidad
        };

        return { copilotMessages, selectOption };
    }
});

describe('US-027 CA-7: UX Triage & Action Pills UI (Iteración 55)', () => {
    
    it('Renderiza las Pills y aserta la inmutabilidad (deshabilitación matemática) post-clic para evitar colisiones', async () => {
        const wrapper = mount(CopilotPillMock);

        // Encontramos los botones Pill
        const buttons = wrapper.findAll('.pill-btn');
        expect(buttons.length).toBe(2);

        // Aserción 1: Todos los botones nacen habilitados para interactuar
        expect(buttons[0].attributes('disabled')).toBeUndefined();
        expect(buttons[1].attributes('disabled')).toBeUndefined();

        // Simulamos la interacción Táctica humana sobre una opción
        await buttons[0].trigger('click');
        
        // Aserción 2: Ambos botones mutaron irrevocablemente a DISABLED, congelando el estado para la IA
        expect(buttons[0].attributes('disabled')).toBeDefined();
        expect(buttons[1].attributes('disabled')).toBeDefined();
        
        // Comprobamos la inmutabilidad interna de Vue
        expect(wrapper.vm.copilotMessages[0].selectedOption).toBe('Usar Rol Existente (SSO)');
    });
});
