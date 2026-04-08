/**
 * ============================================================
 * QA Suite — US-001 Iteración 77-DEV
 * Archivo: WorkdeskGrid.spec.ts
 * CAs cubiertos: CA-03, CA-23, CA-07, CA-18, CA-12
 * Convención: test(QA): US-001 CA-01/03/23/12/07/18
 * ============================================================
 */
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { nextTick } from 'vue';
import Workdesk from '@/views/Workdesk.vue';
import { useWorkdeskStore } from '@/stores/useWorkdeskStore';

// ── Mocks globales ──
vi.mock('@/stores/authStore', () => ({
  useAuthStore: () => ({
    hasAnyRole: () => false,
    user: { name: 'QA Agent', roles: [] },
    isAuthenticated: true
  })
}));

vi.mock('@/services/apiClient', () => ({
  default: {
    get: vi.fn().mockResolvedValue({
      data: { content: [], degraded: false, pageable: { pageNumber: 0, pageSize: 50, totalElements: 0 } }
    }),
    post: vi.fn().mockResolvedValue({ data: {} }),
    interceptors: { request: { use: vi.fn() }, response: { use: vi.fn() } }
  }
}));

vi.mock('@stomp/stompjs', () => {
  class MockClient {
    brokerURL = '';
    debug = () => {};
    reconnectDelay = 0;
    heartbeatIncoming = 0;
    heartbeatOutgoing = 0;
    onConnect: any = null;
    onStompError: any = null;
    active = false;
    constructor(_opts?: any) {
      if (_opts) Object.assign(this, _opts);
    }
    activate() {}
    deactivate() {}
    subscribe() { return { unsubscribe: () => {} }; }
  }
  return { Client: MockClient };
});

// Helper: crea un mock task completo
function createMockTask(overrides: Partial<any> = {}) {
  return {
    unifiedId: 'TK-001',
    sourceSystem: 'BPMN' as const,
    originalTaskId: 'camunda-001',
    title: 'Solicitud de Crédito Hipotecario',
    slaExpirationDate: new Date(Date.now() + 86400000 * 2).toISOString(),
    status: 'ACTIVE',
    assignee: 'juan.perez',
    progressPercent: 60,
    typeBadge: '⚡ Flujo',
    financialImpactHigh: false,
    impactLevel: 3,
    ...overrides
  };
}

// Helper: mount y esperar que onMounted llame loadData, luego inyectar state
async function mountAndInjectState(stateOverrides: Partial<ReturnType<typeof useWorkdeskStore>>) {
  const wrapper = mount(Workdesk, {
    global: {
      stubs: {
        Suspense: true,
        Transition: false,
        DashboardBAM: true
      }
    }
  });

  // Esperar que onMounted y fetchGlobalInbox() (mock) terminen
  await flushPromises();
  await nextTick();

  // AHORA inyectamos el state DESPUÉS del mount cycle
  const store = useWorkdeskStore();
  Object.assign(store, stateOverrides);

  // Esperar re-render reactivo
  await nextTick();
  await flushPromises();

  return { wrapper, store };
}

describe('US-001 Iteración 77-DEV: Workdesk Data Grid (QA Certification)', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  // ===========================================================
  // TEST 1 (CA-03): Data Grid renderiza exactamente 5 columnas
  // ===========================================================
  it('[CA-03] Data Grid renderiza 5 columnas: Nombre, SLA, Estado, Avance, Recurso', async () => {
    const { wrapper } = await mountAndInjectState({
      items: [createMockTask()] as any,
      isLoading: false,
    });

    const headers = wrapper.findAll('thead th');
    expect(headers.length).toBe(5);

    const headerTexts = headers.map(h => h.text());
    expect(headerTexts).toContain('Nombre');
    expect(headerTexts).toContain('SLA');
    expect(headerTexts).toContain('Estado');
    expect(headerTexts).toContain('Avance');
    expect(headerTexts).toContain('Recurso');
  });

  // ===========================================================
  // TEST 2 (CA-23): Progress bar muestra porcentaje
  // ===========================================================
  it('[CA-23] Progress bar muestra porcentaje cuando progressPercent != null', async () => {
    const { wrapper } = await mountAndInjectState({
      items: [createMockTask({ progressPercent: 60 })] as any,
      isLoading: false,
    });

    const html = wrapper.html();
    // CA-23: La barra de progreso renderiza con estilo inline width
    expect(html).toContain('60%');
    // NO debe mostrar N/D cuando hay progreso
    const progressCells = wrapper.findAll('td');
    const avanceCell = progressCells.find(td => td.html().includes('60%'));
    expect(avanceCell).toBeTruthy();
  });

  // ===========================================================
  // TEST 3 (CA-23): Columna Avance muestra "N/D" cuando null
  // ===========================================================
  it('[CA-23] Columna Avance muestra "N/D" cuando progressPercent es null', async () => {
    const { wrapper } = await mountAndInjectState({
      items: [createMockTask({ progressPercent: null })] as any,
      isLoading: false,
    });

    const html = wrapper.html();
    // CA-23: Debe mostrar N/D como fallback
    expect(html).toContain('N/D');
  });

  // ===========================================================
  // TEST 4 (CA-07 / CA-18): Banner degradación BPMN visible
  // ===========================================================
  it('[CA-07/CA-18] Banner degradación visible cuando store.isDegraded = true', async () => {
    const { wrapper } = await mountAndInjectState({
      items: [] as any,
      isDegraded: true,
      isLoading: false,
    });

    const html = wrapper.html();
    // CA-07: Texto del banner de degradación
    expect(html).toContain('Sincronización BPMN degradada temporalmente');
    // CA-18: Subtexto explicativo
    expect(html).toContain('Las tareas Kanban operan con normalidad');
  });

  // ===========================================================
  // TEST 5 (CA-12): Empty State gamificado con celebración
  // ===========================================================
  it('[CA-12] Empty State gamificado muestra celebración cuando items vacíos', async () => {
    const { wrapper } = await mountAndInjectState({
      items: [] as any,
      isLoading: false,
      isDegraded: false,
    });

    const html = wrapper.html();
    // CA-12: Texto gamificado de bandeja vacía
    expect(html).toContain('¡Bandeja Vacía!');
    // CA-12: Ícono de celebración
    expect(html).toContain('celebration');
    // CA-12: Mensaje motivacional
    expect(html).toContain('Has resuelto todas tus tareas pendientes');
  });
});
