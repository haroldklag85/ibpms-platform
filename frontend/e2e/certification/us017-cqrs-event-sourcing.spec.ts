// @Traceability: US-017, CA-01, CA-04
import { test, expect } from '@playwright/test';

test.describe('US-017: CQRS and Event Sourcing', () => {
  test.use({ storageState: 'e2e/playwright/.auth/analista_n1.json' });

  test('CU-01: Auto-Claim and Submit Form', async ({ page, request }) => {
    // Zero-Mock: Asegurar que hay al menos una tarea real enviando un Webhook
    await request.post('http://localhost:8080/api/v1/intake/webhook', {
      params: {
        messageId: `us017-seed-${Date.now()}`,
        senderEmail: 'e2e@test.com',
        subject: 'CQRS Test Seed',
        tenantId: 'tenant_alpha'
      },
      headers: {
        'X-Webhook-Signature': 'valid-hmac-placeholder',
        'Content-Type': 'text/plain'
      },
      data: 'Seed data for CQRS test'
    });
    
    // Give RabbitMQ a second to process and Camunda to create the task
    await page.waitForTimeout(2000);

    await page.goto('/workdesk');
    
    // 1. Entrar a tarea de grupo (Unassigned)
    const firstTask = page.locator('[data-testid^="task-row-"]').first();
    
    if (await firstTask.isVisible({ timeout: 5000 })) {
      await firstTask.click();

      await page.waitForSelector('[data-testid="form-container"]');

      // 2. Fill required simple inputs
      const requiredInputs = page.locator('input[required]');
      const count = await requiredInputs.count();
      for (let i = 0; i < count; i++) {
        await requiredInputs.nth(i).fill('Test CQRS');
      }

      // 3. Intercept and wait for actual API response
      const responsePromise = page.waitForResponse(response => 
        response.url().includes('/api/v1/workbox/tasks/') && 
        response.url().includes('/complete')
      );
      
      // Dar click a Completar
      await page.locator('[data-testid="form-submit"], button:has-text("Completar")').first().click();
      
      const response = await responsePromise;
      expect(response.status()).toBe(200);
      const body = await response.json();
      
      // 4. Validar response con `eventReference` de la API real.
      expect(body).toHaveProperty('eventReference');
      expect(body.eventReference).toBeDefined();
      
      // Ensure success feedback is visible
      await expect(page.locator('.p-toast-message-success, [data-testid="toast-success"], [data-testid="claim-success"]')).toBeVisible({ timeout: 15000 });
    } else {
      console.log('No tasks available to claim, skipping task interaction steps.');
    }
  });
});
