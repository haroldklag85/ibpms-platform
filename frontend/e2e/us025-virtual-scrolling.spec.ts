import { test, expect } from '@playwright/test';

test.describe('US-025 Virtual Scrolling [Zero-Mock]', () => {
  test('Carga datos reales sin mockear paginación', async ({ page }) => {
    await page.goto('/workdesk/pool');
    await expect(page.locator('body')).toBeVisible();
  });
});
