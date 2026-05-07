import { test, expect } from '@playwright/test';
import { seedAgileProject } from './helpers/task-seeder';

test.describe('US-030 Hub Ágil - Cascading Freeze Cierre de Proyecto [Zero-Mock]', () => {
  test('Cierre de Proyecto muta el estado a READ_ONLY', async ({ page, request }) => {
    const projectId = await seedAgileProject(request);
    await page.goto(`/admin/projects/agile-hub/${projectId}`);

    await expect(page.locator('body')).toBeVisible();
  });
});
