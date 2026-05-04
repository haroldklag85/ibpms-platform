import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import MockAdapter from 'axios-mock-adapter';
import apiClient from '@/services/apiClient';

describe('apiClient Interceptors (ADR-014)', () => {

    let mock: MockAdapter;

    beforeEach(() => {
        mock = new MockAdapter(apiClient);
        vi.spyOn(window, 'dispatchEvent');
        vi.spyOn(console, 'error').mockImplementation(() => {});
        vi.spyOn(console, 'warn').mockImplementation(() => {});
    });

    afterEach(() => {
        mock.restore();
        vi.restoreAllMocks();
    });

    it('Error 500: Dispatcha evento con type SERVER_ERROR y dismissible false, incluye traceId', async () => {
        mock.onGet('/test-500').reply(500, {}, { 'x-correlation-id': 'trace-123' });

        try {
            await apiClient.get('/test-500');
        } catch (error) {}

        expect(window.dispatchEvent).toHaveBeenCalled();
        const call = vi.mocked(window.dispatchEvent).mock.calls.find(
            c => (c[0] as CustomEvent).type === 'global-error-dispatch'
        );
        expect(call).toBeDefined();

        const customEvent = call![0] as CustomEvent;
        expect(customEvent.detail).toEqual(expect.objectContaining({
            code: 500,
            type: 'SERVER_ERROR',
            dismissible: false,
        }));
        expect(customEvent.detail.message).toContain('trace-123');
    });

    it('Error 502: Dispatcha evento con type SERVICE_UNAVAILABLE y autoRetry true', async () => {
        mock.onGet('/test-502').reply(502);

        try {
            await apiClient.get('/test-502');
        } catch (error) {}

        expect(window.dispatchEvent).toHaveBeenCalled();
        const call = vi.mocked(window.dispatchEvent).mock.calls.find(
            c => (c[0] as CustomEvent).type === 'global-error-dispatch'
        );
        
        const customEvent = call![0] as CustomEvent;
        expect(customEvent.detail).toEqual(expect.objectContaining({
            code: 502,
            type: 'SERVICE_UNAVAILABLE',
            autoRetry: true,
            dismissible: true
        }));
    });

    it('Error 504: Dispatcha evento con type GATEWAY_TIMEOUT y dismissible true, sin autoRetry', async () => {
        mock.onGet('/test-504').reply(504);

        try {
            await apiClient.get('/test-504');
        } catch (error) {}

        expect(window.dispatchEvent).toHaveBeenCalled();
        const call = vi.mocked(window.dispatchEvent).mock.calls.find(
            c => (c[0] as CustomEvent).type === 'global-error-dispatch'
        );
        
        const customEvent = call![0] as CustomEvent;
        expect(customEvent.detail).toEqual(expect.objectContaining({
            code: 504,
            type: 'GATEWAY_TIMEOUT',
            dismissible: true
        }));
        expect(customEvent.detail.autoRetry).toBeUndefined();
    });

    it('Error sin response: Dispara evento global-error-dispatch por CA-19 (Network Error)', async () => {
        mock.onGet('/test-network-error').networkError();

        try {
            await apiClient.get('/test-network-error');
        } catch (error) {}

        const call = vi.mocked(window.dispatchEvent).mock.calls.find(
            c => (c[0] as CustomEvent).type === 'global-error-dispatch'
        );
        expect(call).toBeDefined();
        
        const customEvent = call![0] as CustomEvent;
        expect(customEvent.detail).toEqual(expect.objectContaining({
            code: 'NETWORK_ERR'
        }));
    });
});
