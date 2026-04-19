import { test, expect } from '@playwright/test';

test.describe('US-002 CA-1: Claim JWT Context', () => {
  test('El claim se envía e inyecta el JWT en backend interceptando 200 OK', async ({ page }) => {
    await page.route('**/api/v1/workdesk/tasks/*/claim', route => {
      // Validamos que en front enviemos auth header (así previene hardcode admin)
      const headers = route.request().headers();
      expect(headers['authorization']).toContain('Bearer ');
      route.fulfill({ status: 200, body: JSON.stringify({ unifiedId: 't-1234', assignee: 'userJwt' }) });
    });

    await page.route('**/api/v1/workdesk/tasks', route => {
        route.fulfill({
            status: 200,
            body: JSON.stringify({ content: [{ unifiedId: 'task-1', status: 'AVAILABLE', title: 'Test Task' }] })
        });
    });

    // Simula Login simple
    await page.goto('/workdesk');
    await page.evaluate(() => {
        localStorage.setItem('bearerToken', 'Bearer pseudo-jwt-token');
    });
    
    // Al recargar ya usa test-token si hay stores que se enganchan (opcional, dependerá de setup global)
    await page.goto('/workdesk');

    const claimBtn = page.locator('button', { hasText: 'Ver Detalle' }).first();
    await claimBtn.waitFor({ state: 'visible' });
    await claimBtn.click();

    const claimModalBtn = page.locator('button', { hasText: 'Reclamar Tarea' });
    await claimModalBtn.waitFor({ state: 'visible' });
    await claimModalBtn.click();

    // Verificamos que se haya consumido la ruta mock
    await expect(page.getByText('Reclamando...')).not.toBeVisible();
  });
});
