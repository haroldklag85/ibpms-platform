import { test, expect } from '@playwright/test';
import { API } from '../fixtures/e2e-data';

test.describe('P0: EmailWebhookController — Deprecated Legacy Endpoint', () => {
  
  test('CU-JSEC-17: POST /inbound/email-webhook retorna HTTP 410 Gone', async ({ request }) => {
    const res = await request.post(`${API.BASE_URL}${API.WEBHOOK_LEGACY}`, {
      headers: { 'ClientState': 'secreto-compartido-m365' },
      data: { subject: 'Test Email', body: 'Body', sender: 'test@domain.com' }
    });
    
    // MUST be 410 Gone (deprecated) — NOT 202 Accepted
    expect(res.status()).toBe(410);
    
    const body = await res.json();
    expect(body.error).toBe('ENDPOINT_DEPRECATED');
    expect(body.migration).toContain('/api/v1/intake/webhook');
  });

  test('CU-JSEC-17b: POST sin ClientState también retorna 410 (no 403)', async ({ request }) => {
    const res = await request.post(`${API.BASE_URL}${API.WEBHOOK_LEGACY}`, {
      data: { subject: 'Attack', body: '<script>alert(1)</script>', sender: 'hacker@evil.com' }
    });
    
    // Deprecado = todo request retorna 410, independientemente de autenticación
    expect(res.status()).toBe(410);
  });
});
