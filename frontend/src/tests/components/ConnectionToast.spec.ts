import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import ConnectionToast from '@/components/common/ConnectionToast.vue';
import { setActivePinia, createPinia } from 'pinia';
import { useConnectionStore } from '@/stores/connectionStore';

describe('ConnectionToast.vue', () => {
    let store: ReturnType<typeof useConnectionStore>;

    beforeEach(() => {
        setActivePinia(createPinia());
        store = useConnectionStore();
        vi.useFakeTimers();
    });

    it('CA-20: verifies positioning properties', async () => {
        store.setStatus('OFFLINE');
        const wrapper = mount(ConnectionToast);
        
        // Use nextTick or await wrapper.vm.$nextTick() if needed, but synchronous mount should render based on store
        const toastEl = wrapper.find('.connection-toast').element as HTMLElement;
        
        // Normally we check classes or inline styles
        expect(toastEl.style.position).toBe('fixed');
        expect(toastEl.style.bottom).toBe('1.5rem');
        expect(toastEl.style.left).toBe('1.5rem');
        expect(toastEl.style.zIndex).toBe('9990');
        expect(toastEl.style.maxWidth).toBe('320px');
    });

    it('CA-21: validates plain business text without technical jargon', () => {
        store.setStatus('OFFLINE');
        const wrapper = mount(ConnectionToast);
        
        const text = wrapper.text();
        expect(text).toContain('Trabajando sin conexión');
        
        const technicalJargon = ['CQRS', 'STOMP', 'Event Sourcing', 'WebSocket', 'Sync Eventual', 'Engine'];
        technicalJargon.forEach(word => {
            expect(text).not.toContain(word);
        });
    });

    it('CA-22: ensures no full-screen blocking overlay is present', () => {
        store.setStatus('OFFLINE');
        const wrapper = mount(ConnectionToast);
        
        const overlay = wrapper.find('.overlay');
        expect(overlay.exists()).toBe(false);
    });

    it('CA-25: fades out 3 seconds after RESTORED', async () => {
        store.setStatus('RESTORED');
        const wrapper = mount(ConnectionToast);
        
        expect(wrapper.text()).toContain('Conexión restaurada');
        
        // After 3 seconds, store should reset to ONLINE and toast disappears
        vi.advanceTimersByTime(3000);
        
        expect(store.status).toBe('ONLINE');
        // Since store is ONLINE, toast should not be visible
        expect(store.isVisible).toBe(false);
    });
});
