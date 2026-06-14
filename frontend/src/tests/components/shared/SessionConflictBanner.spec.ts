import { mount, flushPromises } from '@vue/test-utils';
import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import SessionConflictBanner from '@/components/SessionConflictBanner.vue';
import { api } from '@/services/apiClient';

describe('SessionConflictBanner.vue (CA-35)', () => {
    beforeEach(() => {
        vi.restoreAllMocks();
    });

    it('No es visible inicialmente', () => {
        const wrapper = mount(SessionConflictBanner);
        expect(wrapper.find('#session-conflict-banner').exists()).toBe(false);
    });

    it('Se hace visible cuando se despacha session-conflict-dispatch', async () => {
        const wrapper = mount(SessionConflictBanner);
        window.dispatchEvent(new CustomEvent('session-conflict-dispatch', { detail: { taskId: 't-conflicto' } }));
        await flushPromises();

        expect(wrapper.find('#session-conflict-banner').exists()).toBe(true);
        expect(wrapper.text()).toContain('Conflicto de Sesión Detectado');
    });

    it('Oculta el banner al hacer click en la X', async () => {
        const wrapper = mount(SessionConflictBanner);
        // Mostrar
        window.dispatchEvent(new CustomEvent('session-conflict-dispatch', { detail: { taskId: 't-conflicto' } }));
        await flushPromises();

        // Ocultar
        const closeBtn = wrapper.findAll('button')[1];
        await closeBtn.trigger('click');
        expect(wrapper.find('#session-conflict-banner').exists()).toBe(false);
    });
});
