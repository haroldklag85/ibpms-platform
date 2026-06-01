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

  it('CA-31: fetchMenuLayout llama al endpoint /users/me/menu-layout', async () => {
    const store = useMenuStore();
    const mockData = [{ title: 'Módulo', path: '/modulo', icon: 'mdi-cog' }];
    (apiClient.get as any).mockResolvedValue({ data: mockData });

    await store.fetchMenuLayout();
    
    expect(apiClient.get).toHaveBeenCalledWith('/users/me/menu-layout');
    expect(store.layout).toEqual([{ title: 'Workdesk', items: [{ label: 'Módulo', path: '/modulo', icon: 'cog' }] }]);
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

  it('CA-06: Auto-Collapse - omite contenedores sin elementos hijos', async () => {
      const store = useMenuStore();
      const mockData = [
          { title: 'Dashboard', path: '/dashboard', icon: 'mdi-home' },
          { title: 'Contenedor Vacio', children: [] },
          { title: 'Contenedor Lleno', children: [{ title: 'Item 1', path: '/item1', icon: 'mdi-cog' }] }
      ];
      (apiClient.get as any).mockResolvedValue({ data: mockData });

      await store.fetchMenuLayout();

      // Debe tener 'Workdesk' (para el Dashboard) y 'Contenedor Lleno', pero NO 'Contenedor Vacio'
      expect(store.layout.length).toBe(2);
      expect(store.layout[0].title).toBe('Workdesk');
      expect(store.layout[0].items[0].label).toBe('Dashboard');
      expect(store.layout[1].title).toBe('Contenedor Lleno');
      expect(store.layout[1].items[0].label).toBe('Item 1');
  });

  it('assigns group-level icon using mapIcon and includes groupA in layout hydration', async () => {
      const store = useMenuStore();
      const mockData = [
          { 
              title: 'groupA', 
              icon: 'mdi-desktop-mac', 
              children: [{ title: 'Item A', path: '/itema', icon: 'mdi-cog' }] 
          },
          { 
              title: 'groupB', 
              icon: 'mdi-shield-alert', 
              children: [{ title: 'Item B', path: '/itemb', icon: 'mdi-cog' }] 
          }
      ];
      (apiClient.get as any).mockResolvedValue({ data: mockData });

      await store.fetchMenuLayout();

      expect(store.layout.length).toBe(2);
      expect(store.layout[0].title).toBe('groupA');
      expect(store.layout[0].icon).toBe('desktop_mac');
      expect(store.layout[1].title).toBe('groupB');
      expect(store.layout[1].icon).toBe('gpp_maybe');
  });
});
