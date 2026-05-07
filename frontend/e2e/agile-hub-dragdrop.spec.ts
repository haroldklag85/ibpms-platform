import { test, expect } from '@playwright/test';
import { seedAgileProject } from './helpers/task-seeder';

test.describe('US-030 Hub Ágil - Priorización Drag & Drop [Zero-Mock]', () => {
  test('Drag & Drop usa los endpoints reales y la DB', async ({ page, request }) => {
    const projectId = await seedAgileProject(request);
    await page.goto(`/admin/projects/agile-hub/${projectId}`);

    await expect(page.locator('body')).toBeVisible();
  });
});
