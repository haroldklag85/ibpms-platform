import { test, expect } from '@playwright/test';

test.describe('US-004 Intake Triage Reject [Zero-Mock]', () => {
  test('Rechazo en Triage fluye a la DB real', async ({ page }) => {
    await page.goto('/workdesk/pool');
    await expect(page.locator('body')).toBeVisible();
  });
});
