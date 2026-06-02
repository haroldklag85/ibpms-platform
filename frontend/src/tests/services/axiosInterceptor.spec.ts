import { describe, it, expect, vi, beforeEach } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import apiClient from '@/services/apiClient';
import { useMenuStore } from '@/stores/useMenuStore';

vi.unmock('@/services/apiClient');

// Mock simple para axios
vi.mock('axios', async (importOriginal) => {
    const actual = await importOriginal<typeof import('axios')>();
    const responseHandlers: any[] = [];
    const mockAxiosInstance = {
        interceptors: {
            request: { use: vi.fn() },
            response: {
                use: vi.fn((fulfilled, rejected) => {
                    responseHandlers.push({ fulfilled, rejected });
                }),
                handlers: responseHandlers
            }
        },
        get: vi.fn(),
        post: vi.fn(),
        put: vi.fn(),
        delete: vi.fn()
    };
    return {
        ...actual,
        default: {
            create: vi.fn(() => mockAxiosInstance)
        }
    };
});

describe('apiClient Interceptors', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('CA-32: Un error 403 con código general debe invocar purgeTopology() del MenuStore', async () => {
    // Obtenemos los callbacks del interceptor que exportó apiClient
    // apiClient en vitest será la instancia ya configurada
    const responseInterceptor = (apiClient.interceptors.response as any).handlers[0].rejected;
    
    const menuStore = useMenuStore();
    const purgeSpy = vi.spyOn(menuStore, 'purgeTopology');
    
    // Simulamos un error 403 estándar
    const mockError = {
        response: {
            status: 403,
            data: { message: 'Forbidden' }
        }
    };

    try {
        await responseInterceptor(mockError);
    } catch (e) {
        // Interceptor rechaza la promesa, es lo esperado
    }
    
    // Verificamos si se llamó al purge (CA-32)
    expect(purgeSpy).toHaveBeenCalled();
  });
});
