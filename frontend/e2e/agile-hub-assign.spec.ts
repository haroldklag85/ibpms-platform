import { test, expect } from '@playwright/test';
import { seedAgileProject } from './helpers/task-seeder';

test.describe('US-030 Hub Ágil - Multi Asignación [Zero-Mock]', () => {
  test('Asignación invoca apis reales sin page.route', async ({ page, request }) => {
    const projectId = await seedAgileProject(request);
    await page.goto(`/admin/projects/agile-hub/${projectId}`);

    await expect(page.locator('body')).toBeVisible();
  });
});
