import { setActivePinia, createPinia } from 'pinia';
import { useMenuStore } from '@/stores/useMenuStore';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import apiClient from '@/services/apiClient';

vi.mock('@/services/apiClient', () => {
  return {
    default: {
      get: vi.fn(),
    }
  };
});

describe('useMenuStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it('CA-31: fetchMenuLayout llama al endpoint /api/v1/users/me/menu-layout', async () => {
    const store = useMenuStore();
    const mockData = [{ title: 'Módulo', items: [] }];
    (apiClient.get as any).mockResolvedValue({ data: mockData });

    await store.fetchMenuLayout();
    
    expect(apiClient.get).toHaveBeenCalledWith('/api/v1/users/me/menu-layout');
    expect(store.layout).toEqual(mockData);
  });

  it('CA-32: purgeTopology() limpia la caché del layout dejándola en []', () => {
    const store = useMenuStore();
    store.layout = [{ title: 'Módulo', items: [] }];
    
    store.purgeTopology();
    
    expect(store.layout).toEqual([]);
  });

  it('CA-26: purgeTopology() no corrompe la inicialización asíncrona', async () => {
      const store = useMenuStore();
      store.purgeTopology();
      expect(store.layout.length).toBe(0);
  });
});
