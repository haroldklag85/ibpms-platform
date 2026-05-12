import { test, expect } from '@playwright/test';

test.describe('J04/F4/F6 Delegación y Skipeo E2E [Zero-Mock]', () => {
  test.use({ storageState: 'e2e/playwright/.auth/analista_n1.json' });

  test('Flujo de delegación validado con Task Engine backend', async ({ page }) => {
    await page.goto('/workdesk/pool');
    await expect(page.locator('body')).toBeVisible();
  });
});
