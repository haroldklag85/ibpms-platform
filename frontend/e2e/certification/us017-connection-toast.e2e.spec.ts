import { test, expect } from '@playwright/test';

test.describe('US-017 Connection Toast E2E [Zero-Mock]', () => {
  test('Connection Toast reacciona a eventos reales de red', async ({ page }) => {
    await page.goto('/workdesk/pool');
    await expect(page.locator('body')).toBeVisible();

    // Simular desconexión
    await page.evaluate(() => window.dispatchEvent(new Event('offline')));

    const toast = page.locator('.connection-toast');
    
    // CA-19: Debounce Visual de 5s para estado OFFLINE
    await expect(toast).toBeVisible({ timeout: 6000 });
    await expect(toast).toContainText('Trabajando sin conexión');
    await expect(toast).toHaveClass(/bg-red-50/);

    // Simular reconexión
    await page.evaluate(() => window.dispatchEvent(new Event('online')));

    // CA-25: Feedback Positivo RESTORED (espera artificial de 1.5s antes de "RESTORED")
    await expect(toast).toContainText('Conexión restaurada', { timeout: 3000 });
    await expect(toast).toHaveClass(/bg-green-50/);

    // Desvanecimiento de éxito (3s)
    await expect(toast).toBeHidden({ timeout: 4000 });
  });
});
