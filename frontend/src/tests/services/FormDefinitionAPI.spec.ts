import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import axios from 'axios';
import MockAdapter from 'axios-mock-adapter';
import apiClient, { api } from '@/services/apiClient';
import { setActivePinia, createPinia } from 'pinia';

describe('FormDefinitionAPI (CA-89)', () => {
    let mock: MockAdapter;

    beforeEach(() => {
        // Inicializar Pinia para el useAuthStore en apiClient interceptors
        setActivePinia(createPinia());

        // Enforce Auto-Vitest static assertions strictly via mock adapter.
        // Prevents networking overlap into Sandbox In-Browser environments.
        mock = new MockAdapter(apiClient);
    });

    afterEach(() => {
        mock.restore();
        vi.restoreAllMocks();
    });

    it('should successfully GET form versions from /api/v1/forms/{id}/versions', async () => {
        const fakeFormId = 'FORM-123';
        const expectedData = [
            { version_id: 1, created_by: 'admin', created_at: '2026-04-05T00:00:00Z', hash: 'abc123hash' },
            { version_id: 2, created_by: 'architect', created_at: '2026-04-05T01:00:00Z', hash: 'def456hash' }
        ];

        mock.onGet(`/forms/${fakeFormId}/versions`).reply(200, expectedData);

        const response = await api.getFormVersions(fakeFormId);

        expect(response.status).toBe(200);
        expect(response.data).toEqual(expectedData);
        expect(mock.history.get.length).toBe(1);
        expect(mock.history.get[0].url).toBe(`/forms/${fakeFormId}/versions`);
    });

    it('should successfully POST a new form version to /api/v1/forms/{id}', async () => {
        const fakeFormId = 'FORM-123';
        const payload = {
            schema: { type: 'object', properties: {} },
            zod_schema: '...',
            layout: 'grid'
        };
        const expectedResponse = { success: true, new_version_id: 3 };

        mock.onPost(`/forms/${fakeFormId}`).reply(200, expectedResponse);

        const response = await api.saveFormVersion(fakeFormId, payload);

        expect(response.status).toBe(200);
        expect(response.data).toEqual(expectedResponse);
        expect(mock.history.post.length).toBe(1);
        expect(mock.history.post[0].url).toBe(`/forms/${fakeFormId}`);
        expect(JSON.parse(mock.history.post[0].data)).toEqual(payload);
    });
});
