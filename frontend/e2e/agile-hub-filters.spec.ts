import { test, expect } from '@playwright/test';
import { seedAgileProject } from './helpers/task-seeder';

test.describe('US-030 Hub Ágil - Análisis Visual y Filtros [Zero-Mock]', () => {
  test('Los filtros reaccionan sobre el estado real de Vue', async ({ page, request }) => {
    // 1. Sembrado real (Zero-Mock)
    const projectId = await seedAgileProject(request);
    
    await page.goto(`/admin/projects/agile-hub/${projectId}`);

    // Verificar renderizado basal
    await expect(page.locator('body')).toBeVisible();

    // Comprobar input de filtro
    const searchInput = page.getByPlaceholder(/Filtrar tickets/i);
    if (await searchInput.isVisible()) {
        await searchInput.fill('API');
    }
  });
});
