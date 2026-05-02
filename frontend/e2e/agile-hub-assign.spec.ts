import { test, expect } from '@playwright/test';

test.describe('US-030 Hub Ágil - Multi Asignación', () => {

  const projectId = 'PROJ-AGILE';

  test.beforeEach(async ({ page }) => {
    await page.route(`**/api/v1/projects/${projectId}/board`, async route => {
      await route.fulfill({ 
        status: 200, 
        json: { 
          project: { id: projectId, name: 'Proyecto Agile' },
          sprints: [],
          backlogItems: [{id: 'AT-200', title: 'Estructurar E2E', type: 'STORY', status: 'TO_DO', assignees: [], tags: [] }] 
        } 
      });
    });

    await page.route(`**/api/v1/users*`, async route => {
      await route.fulfill({ status: 200, json: [ 
        { userId: 'usr-1', name: 'Alfonso QA', email: 'alfonso@qa.com' }, 
        { userId: 'usr-2', name: 'Laura Dev', email: 'laura@dev.com' }
      ] });
    });

    await page.goto(`/admin/projects/agile-hub/${projectId}`);
  });

  test('Permitir multi-asignación en backlogs cruzados (CA-5)', async ({ page }) => {
    // Interactuar con AssigneeMultiSelect inline (dentro de la tarjeta)
    const card = page.locator('.bg-white.border.border-slate-200.rounded').filter({ hasText: 'Estructurar E2E' });
    await expect(card).toBeAttached();
    
    // Find the "+" button (the one without img, SVG with d="M12 4v16m8-8H4")
    const assignBtn = card.locator('button.inline-flex');
    await assignBtn.click();

    // Seleccionamos ambos usuarios
    await page.getByText('Alfonso QA').click();
    await page.getByText('Laura Dev').click();

    await page.route(`**/api/v1/agile/items/AT-200/assignees`, async route => {
      await route.fulfill({ status: 200, json: { assignees: [
        { userId: 'usr-1', name: 'Alfonso QA', email: 'alfonso@qa.com' }, 
        { userId: 'usr-2', name: 'Laura Dev', email: 'laura@dev.com' }
      ] } });
    });

    await page.getByRole('button', { name: /Guardar/i }).click();

    // Verificar en Grilla Visual (Avatares)
    const imgAlfonso = card.locator('img[title="Alfonso QA"]');
    const imgLaura = card.locator('img[title="Laura Dev"]');
    await expect(imgAlfonso).toBeAttached();
    await expect(imgLaura).toBeAttached();
  });
});
