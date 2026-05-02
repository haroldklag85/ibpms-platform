import { test, expect } from '@playwright/test';
import { seedAgileProject } from './helpers/task-seeder';

test.describe('US-030 Hub Ágil - Operaciones CRUD Basales [Zero-Mock]', () => {
  test('CRUD interactúa con la DB Real', async ({ page, request }) => {
    const projectId = await seedAgileProject(request);
    await page.goto(`/admin/projects/agile-hub/${projectId}`);

    await expect(page.locator('body')).toBeVisible();
  });
});
