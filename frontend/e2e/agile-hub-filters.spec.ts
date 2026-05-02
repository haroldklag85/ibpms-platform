import { test, expect } from '@playwright/test';

test.describe('US-030 Hub Ágil - Análisis Visual y Filtros', () => {

  const projectId = 'PROJ-AGILE';

  test.beforeEach(async ({ page }) => {
    await page.route(`**/api/v1/projects/${projectId}/board`, async route => {
      await route.fulfill({ status: 200, json: { 
        project: { id: projectId, key: 'PRJ', name: 'Agile Project', status: 'ACTIVE' },
        sprints: [], 
        backlogItems: [
          {id: 'AT-1', title: 'Fix Bug API', type: 'BUG', status: 'DOING', tags: [{id:'tg1', label:'Backend', color:'#000'}], assignees: [] },
          {id: 'AT-2', title: 'Diseño UX', type: 'STORY', status: 'TO_DO', tags: [{id:'tg2', label:'Frontend', color:'#000'}], assignees: [] },
          {id: 'AT-3', title: 'Test QA', type: 'STORY', status: 'TO_DO', tags: [{id:'tg1', label:'Backend', color:'#000'}], assignees: [] }
        ] 
      }});
    });

    await page.goto(`/admin/projects/agile-hub/${projectId}`);
  });

  test('Filtros reactivos aíslan el contenido en la grilla (CA-12)', async ({ page }) => {
    // 1. Existen 3 tareas
    await expect(page.locator('.bg-white.border.border-slate-200.rounded').filter({ hasText: 'Fix Bug API' })).toBeAttached();
    await expect(page.locator('.bg-white.border.border-slate-200.rounded').filter({ hasText: 'Diseño UX' })).toBeAttached();
    await expect(page.locator('.bg-white.border.border-slate-200.rounded').filter({ hasText: 'Test QA' })).toBeAttached();

    // 2. Ejecutar Filtro por texto
    const searchInput = page.getByPlaceholder(/Filtrar tickets/i);
    await searchInput.fill('API');

    // Mute de API calls no aplica si es Reactivo de front, pero 
    // asumimos intercept si fuera backend-driven:
    await page.route(`**/api/v1/projects/${projectId}/board*search=API*`, async route => {
      await route.fulfill({ status: 200, json: { 
        project: { id: projectId, key: 'PRJ', name: 'Agile Project', status: 'ACTIVE' },
        sprints: [], 
        backlogItems: [{id: 'AT-1', title: 'Fix Bug API', type: 'BUG', status: 'DOING', tags: [{id:'tg1', label:'Backend', color:'#000'}], assignees: [] }] 
      }});
    });
    
    // Al filtrar por 'API' o disparar el back, 'Diseño UX' debe desaparecer visualmente (RecycleScroller lo mueve a Y < 0)
    await expect.poll(async () => {
      const box = await page.locator('.bg-white.border.border-slate-200.rounded').filter({ hasText: 'Diseño UX' }).boundingBox();
      return box ? box.y : -1;
    }, { timeout: 10000 }).toBeLessThan(0);
    
    // Y 'Fix Bug API' prevalecer
    await expect(page.locator('.bg-white.border.border-slate-200.rounded').filter({ hasText: 'Fix Bug API' })).toBeAttached();
  });
});
