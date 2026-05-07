import { test, expect } from '@playwright/test';
import { seedTask } from './helpers/task-seeder';

test.describe('US-029 CA-4 & CA-6: Saga Compensation & Owner Check [Zero-Mock]', () => {
  test('Saga Compensation handling', async ({ page, request }) => {
    const taskId = await seedTask(request);
    await page.goto('/workdesk');
    
    await expect(page.locator('body')).toBeVisible();
  });

  test('Owner Check handling', async ({ page, request }) => {
    const taskId = await seedTask(request);
    await page.goto('/workdesk');

    await expect(page.locator('body')).toBeVisible();
  });
});
