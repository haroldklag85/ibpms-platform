import { test, expect } from '@playwright/test';

test.describe('US-030 Hub Ágil - Análisis Visual y Filtros', () => {

  const projectId = 'PROJ-AGILE';

  test.beforeEach(async ({ page }) => {
    await page.route(`**/api/v1/projects/${projectId}/agile/tasks*`, async route => {
      await route.fulfill({ status: 200, json: { data: [
        {id: 'AT-1', title: 'Fix Bug API', status: 'DOING', tags: ['Backend'] },
        {id: 'AT-2', title: 'Diseño UX', status: 'TO_DO', tags: ['Frontend'] },
        {id: 'AT-3', title: 'Test QA', status: 'TO_DO', tags: ['Backend'] }
      ] } });
    });

    await page.goto(`/projects/${projectId}/agile-hub`);
  });

  test('Filtros reactivos aíslan el contenido en la grilla (CA-12)', async ({ page }) => {
    // 1. Existen 3 tareas
    await expect(page.getByText('Fix Bug API')).toBeVisible();
    await expect(page.getByText('Diseño UX')).toBeVisible();
    await expect(page.getByText('Test QA')).toBeVisible();

    // 2. Ejecutar Filtro por texto
    const searchInput = page.getByPlaceholder(/Buscar/i);
    await searchInput.fill('API');

    // Mute de API calls no aplica si es Reactivo de front, pero 
    // asumimos intercept si fuera backend-driven:
    await page.route(`**/api/v1/projects/${projectId}/agile/tasks*search=API*`, async route => {
      await route.fulfill({ status: 200, json: { data: [{id: 'AT-1', title: 'Fix Bug API', status: 'DOING', tags: ['Backend'] }] } });
    });
    
    // Al filtrar por 'API' o disparar el back, 'Diseño UX' debe desaparecer
    await expect(page.getByText('Diseño UX')).toBeHidden();
    
    // Y 'Fix Bug API' prevalecer
    await expect(page.getByText('Fix Bug API')).toBeVisible();
  });
});
