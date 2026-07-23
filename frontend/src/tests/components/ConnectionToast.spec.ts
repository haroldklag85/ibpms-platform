import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import ConnectionToast from '@/components/common/ConnectionToast.vue';
import { setActivePinia, createPinia } from 'pinia';
import { useConnectionStore } from '@/stores/connectionStore';

describe('ConnectionToast.vue', () => {
    let store: ReturnType<typeof useConnectionStore>;
    let pinia: ReturnType<typeof createPinia>;

    beforeEach(() => {
        pinia = createPinia();
        setActivePinia(pinia);
        store = useConnectionStore();
        vi.useFakeTimers();
    });

    it('CA-20: verifies positioning properties', async () => {
        store.setStatus('OFFLINE');
        const wrapper = mount(ConnectionToast, {
            global: { plugins: [pinia] }
        });
        
        await wrapper.vm.$nextTick();
        const toastEl = wrapper.find('.connection-toast');
        expect(toastEl.exists()).toBe(true);
        
        const classes = toastEl.classes();
        expect(classes).toContain('fixed');
        expect(classes).toContain('bottom-6');
        expect(classes).toContain('left-6');
        expect(classes).toContain('z-[9990]');
        expect(classes).toContain('max-w-[320px]');
    });

    it('CA-21: validates plain business text without technical jargon', async () => {
        store.setStatus('OFFLINE');
        const wrapper = mount(ConnectionToast, {
            global: { plugins: [pinia] }
        });
        await wrapper.vm.$nextTick();
        
        const text = wrapper.text();
        expect(text).toContain('Trabajando sin conexión');
        
        const technicalJargon = ['CQRS', 'STOMP', 'Event Sourcing', 'WebSocket', 'Sync Eventual', 'Engine'];
        technicalJargon.forEach(word => {
            expect(text).not.toContain(word);
        });
    });

    it('CA-22: ensures no full-screen blocking overlay is present', async () => {
        store.setStatus('OFFLINE');
        const wrapper = mount(ConnectionToast, {
            global: { plugins: [pinia] }
        });
        await wrapper.vm.$nextTick();
        
        const overlay = wrapper.find('.overlay');
        expect(overlay.exists()).toBe(false);
    });

    it('CA-25: fades out 3 seconds after RESTORED', async () => {
        store.setStatus('RESTORED');
        const wrapper = mount(ConnectionToast, {
            global: { plugins: [pinia] }
        });
        await wrapper.vm.$nextTick();
        
        expect(wrapper.text()).toContain('Conexión restaurada');
        
        // After 3 seconds, store should reset to ONLINE and toast disappears
        vi.advanceTimersByTime(3000);
        await wrapper.vm.$nextTick();
        
        expect(store.status).toBe('ONLINE');
        // Since store is ONLINE, toast should not be visible
        expect(store.isVisible).toBe(false);
    });
});
