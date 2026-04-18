import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import ErrorStateGlobal from './ErrorStateGlobal.vue';

describe('ErrorStateGlobal.vue (Testing Emisor-Receptor CA-1)', () => {

    it('test_Component_Receives_GlobalError_And_Shows_Alert', async () => {
        const wrapper = mount(ErrorStateGlobal, {
            global: {
                stubs: {
                    Teleport: true
                }
            }
        });

        // Pre-condición: No debe estar mostrando la alerta inicialmente
        expect(wrapper.text()).not.toContain('Colapso Crítico del Servidor');

        // Act: Disparar el evento artificialmente en el objeto global (window)
        const customEvent = new CustomEvent('global-error-dispatch', { 
            detail: { 
                code: 502,
                message: 'Colapso Crítico del Servidor simulado por QA'
            }
        });
        window.dispatchEvent(customEvent);

        // Esperar el ciclo de reactivity de Vue
        await wrapper.vm.$nextTick();

        // Assert: El DOM debe haberse actualizado y mostrar la advertencia
        expect(wrapper.text()).toContain('ALERTA DEL SISTEMA: NIVEL 0');
        expect(wrapper.text()).toContain('Colapso Crítico del Servidor simulado por QA');
        expect(wrapper.text()).toContain('Código de Error: 502');

        // Clean-up
        wrapper.unmount();
    });

    it('test_Component_Receives_OptimisticLock_And_Shows_Alert', async () => {
        const wrapper = mount(ErrorStateGlobal, {
            global: {
                stubs: {
                    Teleport: true
                }
            }
        });

        // Disparar Evento CA-3
        const customEvent = new CustomEvent('optimistic-lock-dispatch');
        window.dispatchEvent(customEvent);

        await wrapper.vm.$nextTick();

        // Assert
        expect(wrapper.text()).toContain('CONFLICTO DE CONCURRENCIA');
        expect(wrapper.text()).toContain('Datos Oxidados');

        wrapper.unmount();
    });
});
