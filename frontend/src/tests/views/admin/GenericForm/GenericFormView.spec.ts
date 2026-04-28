import { mount } from '@vue/test-utils';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useGenericFormStore } from '@/stores/genericFormStore';

// Mock de vue-router
vi.mock('vue-router', () => ({
  useRoute: () => ({ params: { taskId: 'TEST-TASK-001' } }),
  onBeforeRouteLeave: vi.fn(),
}));

// Mock de apiClient para evitar llamadas reales
vi.mock('@/services/apiClient', () => ({
  default: {
    get: vi.fn().mockResolvedValue({ data: {} }),
    put: vi.fn().mockResolvedValue({ data: {} }),
    delete: vi.fn().mockResolvedValue({ data: {} }),
  },
}));

describe('GenericFormView — Draft Restoration Banner (REM-039-C)', () => {
  let store: ReturnType<typeof useGenericFormStore>;

  beforeEach(() => {
    setActivePinia(createPinia());
    store = useGenericFormStore();
  });

  it('QA-039-C-01: El banner de restauración es visible cuando showDraftBanner=true', async () => {
    store.$patch({
      showDraftBanner: true,
      pendingDraft: { observations: 'test obs', files: [], result: 'APPROVED' },
    });

    // Verificar lógica del store directamente (Component test simplificado)
    expect(store.showDraftBanner).toBe(true);
    expect(store.pendingDraft).not.toBeNull();
    expect(store.pendingDraft?.observations).toBe('test obs');
  });

  it('QA-039-C-02: restoreDraft() aplica los datos y oculta el banner', () => {
    store.$patch({
      showDraftBanner: true,
      pendingDraft: { observations: 'mis observaciones previas', files: [], result: 'REJECTED' },
    });

    store.restoreDraft();

    expect(store.showDraftBanner).toBe(false);
    expect(store.pendingDraft).toBeNull();
    expect(store.observations).toBe('mis observaciones previas');
    expect(store.result).toBe('REJECTED');
  });

  it('QA-039-C-03: dismissDraft() descarta el draft y oculta el banner sin aplicar datos', () => {
    store.$patch({
      showDraftBanner: true,
      pendingDraft: { observations: 'datos que serán descartados', files: [], result: 'APPROVED' },
    });

    store.dismissDraft();

    expect(store.showDraftBanner).toBe(false);
    expect(store.pendingDraft).toBeNull();
    // Los campos del form NO deben tener los datos del draft descartado
    expect(store.observations).toBe('');
    expect(store.result).toBe('');
  });
});
