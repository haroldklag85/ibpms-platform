import { mount } from '@vue/test-utils';
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { ref, reactive } from 'vue';
import UniversalSlaTimer from '@/components/common/UniversalSlaTimer.vue';

// @Traceability: Test - Certificación Remediación CA-11

// Inyectamos un timeStore simulado (mock)
const mockStore = reactive({
  currentTick: new Date('2026-05-11T12:00:00Z').getTime(),
  startEngine: vi.fn(),
  stopEngine: vi.fn(),
  getInactivityMs: vi.fn()
});

vi.mock('@/stores/timeStore', () => {
  return {
    useTimeStore: () => mockStore
  };
});

describe('UniversalSlaTimer.vue - CA-11 Dumb Component Certification', () => {
  beforeEach(() => {
    // Resetear el tiempo en el mock antes de cada prueba
    mockStore.currentTick = new Date('2026-05-11T12:00:00Z').getTime();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('debe reaccionar a la mutación del timeStore global sin usar setInterval local', async () => {
    const now = new Date('2026-05-11T12:00:00Z').getTime();
    const due = new Date(now + 2 * 60 * 60 * 1000).toISOString();

    const wrapper = mount(UniversalSlaTimer, {
      props: {
        taskId: 'TASK-1',
        referenceType: 'SLA',
        currentState: 'DOING',
        slaDueDate: due,
      }
    });

    // Validar el formato inicial "2h 0m"
    expect(wrapper.text()).toContain('2h 0m');

    // Manipula manualmente el currentTick del mock store (sin usar advanceTimersByTime)
    mockStore.currentTick = now + 1.5 * 60 * 60 * 1000;
    
    // Esperar la actualización de la UI
    await wrapper.vm.$nextTick();

    // Validar la reacción instantánea
    expect(wrapper.text()).toContain('0h 30m');
  });

  it('debe mostrar "Expirado" inmediatamente cuando currentTick supera la fecha de vencimiento', async () => {
    const now = new Date('2026-05-11T12:00:00Z').getTime();
    const due = new Date(now + 1000).toISOString();

    const wrapper = mount(UniversalSlaTimer, {
      props: {
        taskId: 'TASK-1',
        referenceType: 'SLA',
        currentState: 'DOING',
        slaDueDate: due,
      }
    });

    expect(wrapper.text()).toContain('0h 0m');

    // Manipula manualmente el currentTick del mock store
    mockStore.currentTick = now + 2000;
    
    await wrapper.vm.$nextTick();

    expect(wrapper.text()).toContain('Expirado');
  });

  it('Caza de Fugas: no debe inicializar ningún setInterval interno al montarse', () => {
    const setIntervalSpy = vi.spyOn(global, 'setInterval');

    const wrapper = mount(UniversalSlaTimer, {
      props: {
        taskId: 'TASK-1',
        referenceType: 'SLA',
        currentState: 'DOING',
        slaDueDate: new Date().toISOString(),
      }
    });

    // Validamos que el componente está 100% tonto (no ejecuta timers propios)
    expect(setIntervalSpy).not.toHaveBeenCalled();
    wrapper.unmount();
  });
});
