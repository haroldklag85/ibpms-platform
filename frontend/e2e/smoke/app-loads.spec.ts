import { test, expect } from '@playwright/test';

test.describe('Smoke Test - App Loads [Zero-Mock]', () => {
  test('La aplicación inicializa y enruta correctamente sin Mocks', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('body')).toBeVisible();
  });
});
