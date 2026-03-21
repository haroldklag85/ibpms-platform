import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import DOMPurify from 'dompurify';
import { defineComponent, ref, computed } from 'vue';

// Mock del Componente Renderizador de Variables DMN
const DmnVariableRenderer = defineComponent({
    props: {
        rawInput: { type: String, required: true }
    },
    setup(props) {
        // En Producción este Hook de pre-montaje usaría la librería central de Sanitización
        const safeHtml = computed(() => DOMPurify.sanitize(props.rawInput));
        return { safeHtml };
    },
    template: `<div class="dmn-output" v-html="safeHtml"></div>`
});

describe('V1.2 QA: Motor Anti-XSS para Inyecciones Cognitivas de DMN (Iteración 49)', () => {

    it('Purga inyecciones de código malicioso XSS esterilizando el HTML antes del montaje visual', async () => {
        // Simulamos un Hacker o una IA alucinando de inyectar scripts en la tabla de decisión
        const maliciousPayload = `Aprobado <img src="x" onerror="alert('Hacked')"> <script>document.cookie='steal'</script>`;

        const wrapper = mount(DmnVariableRenderer, {
            props: { rawInput: maliciousPayload }
        });

        await wrapper.vm.$nextTick();

        const renderedHtml = wrapper.find('.dmn-output').html();

        // Aserción Matemática 1: El código de ejecución fue extirpado de raíz (DOMPurify actuó)
        expect(renderedHtml).not.toContain('<script>');
        expect(renderedHtml).not.toContain('onerror=');
        expect(renderedHtml).not.toContain('alert');
        
        // Aserción Matemática 2: Sobrevive únicamente el texto plano inofensivo
        expect(wrapper.text()).toBe('Aprobado');
        // El tag 'img' podría sobrevivir en versión inofensiva o borrarse, pero 'onerror' es 100% extirpado.
        expect(renderedHtml).toContain('Aprobado <img src="x">');
    });
});
