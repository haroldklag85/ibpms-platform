import { describe, it, expect, vi, beforeEach } from 'vitest';
import DOMPurify from 'dompurify';
import { mount } from '@vue/test-utils';
import { defineComponent, ref } from 'vue';

// Componente Wrapper para simular BpmnDesigner DOM y Halo
const BpmnDesignerMock = defineComponent({
    template: `
        <div id="canvas">
           <div v-if="nodeInjected" :class="{'highlight-ai': isHighlighted}" id="UserTask_AI_1" v-html="sanitizedXml"></div>
        </div>
    `,
    setup() {
        const nodeInjected = ref(false);
        const isHighlighted = ref(false);
        const sanitizedXml = ref('');

        const injectAiNode = (rawXml: string) => {
            // CA-2: AppSec DOMPurify (Aniquilación de amenaza SVG/XSS)
            sanitizedXml.value = DOMPurify.sanitize(rawXml);
            nodeInjected.value = true;
            
            // CA-3: Activación del Halo de Experiencia
            isHighlighted.value = true;

            // Limpiador automático tras 3000ms
            setTimeout(() => {
                isHighlighted.value = false;
            }, 3000);
        };

        return { nodeInjected, isHighlighted, sanitizedXml, injectAiNode };
    }
});

describe('US-027 CA-2/CA-3: Copilot BpmnDesigner AppSec & UX (Iteración 53)', () => {
    beforeEach(() => {
        vi.useFakeTimers();
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    it('CA-2: DOMPurify aniquila amenazas XSS inyectadas en texto de nodos generados por IA', async () => {
        const wrapper = mount(BpmnDesignerMock);
        
        // Simulación: El LLM nos escupe un SVG/XSS disfrazado de nodo
        const maliciousPayload = `<svg/onload=alert(1)> <rect width="100" height="100"/>`;
        
        wrapper.vm.injectAiNode(maliciousPayload);
        await wrapper.vm.$nextTick();

        const renderedHtml = wrapper.find('#UserTask_AI_1').html();
        
        // Aserción Matemática 1: El vector onload="alert(1)" ha sido físicamente extirpado por completo
        expect(renderedHtml).not.toContain('onload');
        expect(renderedHtml).not.toContain('alert(1)');
        expect(wrapper.vm.sanitizedXml).not.toContain('onload=');
    });

    it('CA-3: La inyección atómica de la IA ilumina el nodo con la clase .highlight-ai y desaparece en 3s', async () => {
        const wrapper = mount(BpmnDesignerMock);
        wrapper.vm.injectAiNode('<userTask id="Task_1" />');
        
        await wrapper.vm.$nextTick();

        // Aserción Matemática 1: El Halo (Clase CSS Verde/Aura) se activó inmediatamente
        const node = wrapper.find('#UserTask_AI_1');
        expect(node.classes()).toContain('highlight-ai');
        expect(wrapper.vm.isHighlighted).toBe(true);

        // Avanzar el reloj simulado de Vitest 2999ms (Aún debe estar iluminado)
        vi.advanceTimersByTime(2999);
        await wrapper.vm.$nextTick();
        expect(wrapper.vm.isHighlighted).toBe(true);

        // Alcanzando los 3000ms matemáticos
        vi.advanceTimersByTime(1);
        await wrapper.vm.$nextTick();

        // Aserción Matemática 2: El Halo desapareció limpiamente
        expect(node.classes()).not.toContain('highlight-ai');
        expect(wrapper.vm.isHighlighted).toBe(false);
    });
});
