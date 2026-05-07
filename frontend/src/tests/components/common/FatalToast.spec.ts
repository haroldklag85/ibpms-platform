import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { mount } from '@vue/test-utils';
import FatalToast from '@/components/common/FatalToast.vue';

describe('FatalToast.vue', () => {
  let wrapper: any;

  beforeEach(() => {
    wrapper = mount(FatalToast);
  });

  afterEach(() => {
    wrapper.unmount();
  });

  it('NO se muestra por defecto', () => {
    expect(wrapper.find('[data-testid="fatal-toast"]').exists()).toBe(false);
  });

  it('Muestra el toast en 500 (SERVER_ERROR) con el traceId', async () => {
    const event = new CustomEvent('global-error-dispatch', {
      detail: { type: 'SERVER_ERROR', traceId: '123-abc' }
    });
    window.dispatchEvent(event);

    await wrapper.vm.$nextTick();

    const toast = wrapper.find('[data-testid="fatal-toast"]');
    expect(toast.exists()).toBe(true);
    expect(toast.text()).toContain('Error crítico del servidor');
    expect(toast.text()).toContain('TraceId: 123-abc');
  });

  it('NO muestra el toast en 502/504 (otros tipos de error)', async () => {
    const event = new CustomEvent('global-error-dispatch', {
      detail: { type: 'GATEWAY_ERROR', traceId: '456-def' }
    });
    window.dispatchEvent(event);

    await wrapper.vm.$nextTick();

    expect(wrapper.find('[data-testid="fatal-toast"]').exists()).toBe(false);
  });
});
