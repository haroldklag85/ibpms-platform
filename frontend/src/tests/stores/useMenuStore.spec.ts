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

  it('fetch exitoso: fetchMenuLayout() llama GET /users/me/menu-layout y popula layout', async () => {
    const store = useMenuStore();
    const mockData = [{ title: 'Módulo', items: [] }];
    (apiClient.get as any).mockResolvedValue({ data: mockData });

    await store.fetchMenuLayout();
    
    expect(apiClient.get).toHaveBeenCalledWith('/users/me/menu-layout');
    expect(store.layout).toEqual(mockData);
  });

  it('fetch fallido: Si el endpoint falla, layout queda como [] (Zero-Trust)', async () => {
    const store = useMenuStore();
    (apiClient.get as any).mockRejectedValue(new Error('Network error'));
    
    // We start with empty layout, fetch fails, should remain empty
    await store.fetchMenuLayout();
    
    expect(apiClient.get).toHaveBeenCalledWith('/users/me/menu-layout');
    expect(store.layout).toEqual([]); // Zero-Trust: it should remain empty on failure
  });

  it('cache hit: Si layout.length > 0, no hace segunda llamada HTTP', async () => {
    const store = useMenuStore();
    store.layout = [{ title: 'Workdesk', items: [] }];
    
    await store.fetchMenuLayout();
    
    expect(apiClient.get).not.toHaveBeenCalled();
    expect(store.layout.length).toBe(1);
  });

  it('purgeTopology: Resetea layout a []', () => {
    const store = useMenuStore();
    store.layout = [{ title: 'Módulo', items: [] }];
    
    store.purgeTopology();
    
    expect(store.layout).toEqual([]);
  });
});
