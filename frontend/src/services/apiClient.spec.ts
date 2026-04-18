import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import MockAdapter from 'axios-mock-adapter';
import apiClient from './apiClient';
import { createPinia, setActivePinia } from 'pinia';

describe('apiClient Interceptors (CA-1 / CA-3)', () => {

    let mock: MockAdapter;

    beforeEach(() => {
        setActivePinia(createPinia());
        // Deshabilitar mockAdapter interno para que no interfiera y crear uno fresco
        // Sin embargo apiClient ya trae el setupMockAdapter, lo podemos sobreescribir con otro mock 
        // local para tests o usar el que ya tiene interceptando la peticion.
        mock = new MockAdapter(apiClient);
        
        // Espiar el dispatchEvent de Window para confirmar CA-1 y CA-3
        vi.spyOn(window, 'dispatchEvent');
        // Espiar console.error para no llenar la consola del test
        vi.spyOn(console, 'error').mockImplementation(() => {});
        vi.spyOn(console, 'warn').mockImplementation(() => {});
        // Mockear alert por si se llegase a usar (Aserción de Inexistencia)
        vi.spyOn(window, 'alert').mockImplementation(() => {});
    });

    afterEach(() => {
        mock.restore();
        vi.restoreAllMocks();
    });

    it('test_Frontend_Interceptor_EmitsCustomEvent - Envía 500 y emite global-error-dispatch sin usar alert()', async () => {
        // Arrange
        mock.onGet('/test-500').reply(500);

        // Act
        try {
            await apiClient.get('/test-500');
        } catch (error) {
            // Se espera que falle y caiga al catch
        }

        // Assert (QA-CA-1): Aseguramos que dispare el CustomEvent
        expect(window.dispatchEvent).toHaveBeenCalled();
        const dispatchedEvent = vi.mocked(window.dispatchEvent).mock.calls.find(
            call => call[0].type === 'global-error-dispatch'
        );
        expect(dispatchedEvent).toBeDefined();

        // Extra info: validación de payload
        const customEvent = dispatchedEvent![0] as CustomEvent;
        expect(customEvent.detail.code).toBe(500);
        expect(customEvent.detail.message).toContain('Colapso del Servidor');

        // Assert (QA-CA-1 Strict): alert() jamaś se debe invocar
        expect(window.alert).not.toHaveBeenCalled();
    });

    it('test_Frontend_Interceptor_OptimisticLock - Envía 409 y emite optimistic-lock-dispatch', async () => {
        // Arrange
        mock.onGet('/test-409').reply(409, { type: 'optimistic-lock' });

        // Act
        try {
            await apiClient.get('/test-409');
        } catch (error) {
            // ...
        }

        // Assert (QA-CA-3)
        expect(window.dispatchEvent).toHaveBeenCalled();
        const dispatchedEvent = vi.mocked(window.dispatchEvent).mock.calls.find(
            call => call[0].type === 'optimistic-lock-dispatch'
        );
        expect(dispatchedEvent).toBeDefined();
    });
});
