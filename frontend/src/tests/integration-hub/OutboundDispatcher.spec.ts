/**
 * @vitest-environment jsdom
 */
import { mount } from '@vue/test-utils';
import { describe, it, expect } from 'vitest';
import OutboundDispatcherConfig from '@/views/admin/Integration/OutboundDispatcherConfig.vue';

// ── QA Instruction 1: SSRF UI Validation (CA-12) ──
describe('Pantalla 8: Integration Hub - OutboundDispatcher', () => {

    it('Debe bloquear el guardado de un Webhook si la IP apunta a Localhost (SSRF Guardrail)', async () => {
        const wrapper = mount(OutboundDispatcherConfig);
        const input = wrapper.find('[data-test="url-input"]');

        // Inyectamos una URL hack
        await input.setValue('http://127.0.0.1:8080/admin/delete-all');
        await wrapper.find('form').trigger('submit.prevent');

        // Esperar a que el DOM reaccione
        await wrapper.vm.$nextTick();

        const errorMsg = wrapper.find('[data-test="error-message"]');

        expect(errorMsg.exists()).toBe(true);
        expect(errorMsg.text()).toContain('Localhost');
        expect(errorMsg.text()).toContain('SSRF Blocked');
    });

    it('Debe bloquear el guardado si intenta alcanzar metadatos Cloud de AWS (169.254.169.254)', async () => {
        const wrapper = mount(OutboundDispatcherConfig);
        const input = wrapper.find('[data-test="url-input"]');

        await input.setValue('http://169.254.169.254/latest/meta-data');
        await wrapper.find('form').trigger('submit.prevent');

        await wrapper.vm.$nextTick();

        const errorMsg = wrapper.find('[data-test="error-message"]');

        expect(errorMsg.exists()).toBe(true);
        expect(errorMsg.text()).toContain('metadatos Cloud');
        expect(errorMsg.text()).toContain('SSRF Blocked');
    });

});
