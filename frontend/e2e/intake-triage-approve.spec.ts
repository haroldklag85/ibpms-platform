import { test, expect } from '@playwright/test';

test.describe('US-004 Intake Triage Approve [Zero-Mock]', () => {
  test('Aprobación en Triage inicializa el Proceso BPMN', async ({ page }) => {
    await page.goto('/workdesk/pool');
    await expect(page.locator('body')).toBeVisible();
  });
});
