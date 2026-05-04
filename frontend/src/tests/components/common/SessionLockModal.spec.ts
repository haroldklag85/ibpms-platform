import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createTestingPinia } from '@pinia/testing';
import SessionLockModal from '@/components/common/SessionLockModal.vue';
import { useAuthStore } from '@/stores/authStore';

describe('SessionLockModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('NO se muestra por defecto', () => {
    const wrapper = mount(SessionLockModal, {
      global: { plugins: [createTestingPinia()] }
    });
    expect(wrapper.find('[data-testid="session-lock-modal"]').exists()).toBe(false);
  });

  it('Se muestra al recibir evento SESSION_EXPIRED', async () => {
    const wrapper = mount(SessionLockModal, {
      global: { plugins: [createTestingPinia()] }
    });

    const event = new CustomEvent('global-error-dispatch', {
      detail: { type: 'SESSION_EXPIRED' }
    });
    window.dispatchEvent(event);

    await wrapper.vm.$nextTick();
    expect(wrapper.find('[data-testid="session-lock-modal"]').exists()).toBe(true);
  });

  it('Re-auth exitosa cierra el modal', async () => {
    const wrapper = mount(SessionLockModal, {
      global: { plugins: [createTestingPinia()] }
    });
    const authStore = useAuthStore();
    authStore.hydrateAuth = vi.fn().mockResolvedValue(true);

    // Abrir modal
    window.dispatchEvent(new CustomEvent('global-error-dispatch', { detail: { type: 'SESSION_EXPIRED' } }));
    await wrapper.vm.$nextTick();

    // Llenar password y submit
    await wrapper.find('[data-testid="session-lock-password"]').setValue('secret');
    await wrapper.find('form').trigger('submit.prevent');
    await wrapper.vm.$nextTick();

    expect(authStore.hydrateAuth).toHaveBeenCalled();
    // Flush promises
    await new Promise(r => setTimeout(r, 0));
    await wrapper.vm.$nextTick();
    
    expect(wrapper.find('[data-testid="session-lock-modal"]').exists()).toBe(false);
  });

  it('Re-auth fallida hace logout', async () => {
    const wrapper = mount(SessionLockModal, {
      global: { plugins: [createTestingPinia()] }
    });
    const authStore = useAuthStore();
    authStore.hydrateAuth = vi.fn().mockRejectedValue(new Error('Invalid credentials'));

    // Abrir modal
    window.dispatchEvent(new CustomEvent('global-error-dispatch', { detail: { type: 'SESSION_EXPIRED' } }));
    await wrapper.vm.$nextTick();

    // Llenar password y submit
    await wrapper.find('[data-testid="session-lock-password"]').setValue('wrong');
    await wrapper.find('form').trigger('submit.prevent');
    await wrapper.vm.$nextTick();

    // Flush promises
    await new Promise(r => setTimeout(r, 0));
    await wrapper.vm.$nextTick();

    expect(authStore.hydrateAuth).toHaveBeenCalled();
    expect(authStore.logout).toHaveBeenCalled();
    expect(wrapper.text()).toContain('Credenciales inválidas');
  });
});
