import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createTestingPinia } from '@pinia/testing';
import ImpersonationBanner from '@/components/admin/ImpersonationBanner.vue';
import { useAuthStore } from '@/stores/authStore';
import { createI18n } from 'vue-i18n';

const i18n = createI18n({
  legacy: false,
  locale: 'es',
  messages: {
    es: {
      impersonation: { banner: 'Viendo como', exit: 'Volver' }
    }
  }
});

describe('ImpersonationBanner.vue', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('Se renderiza cuando isImpersonating === true', async () => {
    const wrapper = mount(ImpersonationBanner, {
      global: { 
        plugins: [
          createTestingPinia({
            initialState: {
              auth: {
                isImpersonating: true,
                impersonatedBy: 'john.doe',
                impersonationExpiresAt: Date.now() + 60000
              }
            }
          }), 
          i18n
        ] 
      }
    });

    await wrapper.vm.$nextTick();
    expect(wrapper.find('[data-testid="impersonation-banner"]').exists()).toBe(true);
    expect(wrapper.text()).toContain('Viendo como [john.doe]');
  });

  it('NO se renderiza cuando isImpersonating === false', () => {
    const wrapper = mount(ImpersonationBanner, {
      global: { plugins: [createTestingPinia(), i18n] }
    });
    expect(wrapper.find('[data-testid="impersonation-banner"]').exists()).toBe(false);
  });

  it('Click en "Volver a mi sesión" llama exitImpersonation()', async () => {
    const wrapper = mount(ImpersonationBanner, {
      global: { 
        plugins: [
          createTestingPinia({
            initialState: {
              auth: {
                isImpersonating: true,
                impersonationExpiresAt: Date.now() + 60000
              }
            }
          }), 
          i18n
        ] 
      }
    });
    const authStore = useAuthStore();
    
    await wrapper.vm.$nextTick();
    await wrapper.find('button').trigger('click');
    expect(authStore.exitImpersonation).toHaveBeenCalled();
  });

  it('Countdown muestra tiempo restante formateado', async () => {
    const wrapper = mount(ImpersonationBanner, {
      global: { 
        plugins: [
          createTestingPinia({
            initialState: {
              auth: {
                isImpersonating: true,
                impersonationExpiresAt: Date.now() + 180000
              }
            }
          }), 
          i18n
        ] 
      }
    });
    
    await wrapper.vm.$nextTick();
    expect(wrapper.text()).toContain('03:00');

    vi.advanceTimersByTime(1000);
    await wrapper.vm.$nextTick();
    expect(wrapper.text()).toContain('02:59');
  });
});
