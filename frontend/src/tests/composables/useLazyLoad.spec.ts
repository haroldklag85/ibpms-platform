import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { useLazyLoad } from '@/composables/useLazyLoad';

describe('useLazyLoad', () => {
  let mockObserve: any;
  let mockUnobserve: any;
  let mockDisconnect: any;
  let observerCallback: any;

  beforeEach(() => {
    mockObserve = vi.fn();
    mockUnobserve = vi.fn();
    mockDisconnect = vi.fn();

    global.IntersectionObserver = vi.fn().mockImplementation((cb) => {
      observerCallback = cb;
      return {
        observe: mockObserve,
        unobserve: mockUnobserve,
        disconnect: mockDisconnect,
      };
    }) as any;
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('debe registrar el observer en onMounted y limpiar en onUnmounted', () => {
    const TestComponent = {
      template: '<div ref="targetRef"></div>',
      setup() {
        return useLazyLoad();
      }
    };

    const wrapper = mount(TestComponent);
    
    expect(global.IntersectionObserver).toHaveBeenCalled();
    expect(mockObserve).toHaveBeenCalled();

    wrapper.unmount();
    expect(mockDisconnect).toHaveBeenCalled();
  });

  it('debe cambiar isVisible a true cuando el elemento interseca', () => {
    const TestComponent = {
      template: '<div ref="targetRef"></div>',
      setup() {
        return useLazyLoad();
      }
    };

    const wrapper = mount(TestComponent);
    
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    expect((wrapper.vm as any).isVisible).toBe(false);

    // Simular intersección
    observerCallback([{ isIntersecting: true }]);

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    expect((wrapper.vm as any).isVisible).toBe(true);
    expect(mockUnobserve).toHaveBeenCalled();
  });
});
