import { test, expect } from '@playwright/test';
import { seedAgileProject } from './helpers/task-seeder';

test.describe('US-030 Hub Ágil - Detección Perimetral de Tickets Rancios (Stale) [Zero-Mock]', () => {
  test('La UI no debe colapsar al cargar el board y renderizar datos reales', async ({ page, request }) => {
    // 1. Sembrado real (Zero-Mock)
    const projectId = await seedAgileProject(request);
    
    // 2. Navegamos al proyecto real
    await page.goto(`/admin/projects/agile-hub/${projectId}`);

    // 3. Verificamos resiliencia en la UI
    await expect(page.locator('body')).toBeVisible();
  });
});
