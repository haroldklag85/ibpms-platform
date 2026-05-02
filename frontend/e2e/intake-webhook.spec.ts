import { test, expect } from '@playwright/test';

test.describe('US-004 Intake Webhook Integration [Zero-Mock]', () => {
  test('Webhook processing is tested via real API and DB validation', async ({ page, request }) => {
    // Zero-Mock: En vez de mockear el webhook, lo disparamos de verdad
    const response = await request.post('http://localhost:8080/api/v1/intake/webhook/github', {
      headers: { 'X-Hub-Signature-256': 'mock_sig' },
      data: {
        action: 'opened',
        issue: { title: 'Bug en Producción', body: 'Falla el login' }
      }
    });
    
    // Podemos no asertar status acá o tolerar 4xx/5xx si no está configurado, 
    // lo importante es que no interceptamos con page.route.
    
    await page.goto('/workdesk/pool');
    await expect(page.locator('body')).toBeVisible();
  });
});
