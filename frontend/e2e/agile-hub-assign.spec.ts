import { test, expect } from '@playwright/test';

test.describe('US-030 Hub Ágil - Multi Asignación', () => {

  const projectId = 'PROJ-AGILE';

  test.beforeEach(async ({ page }) => {
    await page.route(`**/api/v1/projects/${projectId}/agile/tasks*`, async route => {
      await route.fulfill({ status: 200, json: { data: [{id: 'AT-200', title: 'Estructurar E2E', status: 'TO_DO', assignees: [] }] } });
    });

    await page.route(`**/api/v1/projects/${projectId}/members`, async route => {
      await route.fulfill({ status: 200, json: { data: [ 
        { id: 'usr-1', name: 'Alfonso QA' }, 
        { id: 'usr-2', name: 'Laura Dev' }
      ] } });
    });

    await page.goto(`/projects/${projectId}/agile-hub`);
  });

  test('Permitir multi-asignación en backlogs cruzados (CA-5)', async ({ page }) => {
    // Abrimos el panel de edición
    await page.getByText('Estructurar E2E').click();

    const slidePanel = page.getByRole('complementary', { name: /Tarea/i });
    
    // Interactuar con Vue-Multiselect o Element-Plus Select Multiple
    const selectUsuarios = slidePanel.getByLabel(/Responsable/i);
    await selectUsuarios.click();

    // Seleccionamos ambos usuarios
    await page.getByRole('option', { name: 'Alfonso QA' }).click();
    await page.getByRole('option', { name: 'Laura Dev' }).click();

    // Dar escape o click fuera para cerrar dropdown
    await page.keyboard.press('Escape');

    // Intercept de actualización
    await page.route(`**/api/v1/projects/${projectId}/agile/tasks/AT-200`, async route => {
      await route.fulfill({ status: 200, json: { id: 'AT-200', assignees: ['usr-1', 'usr-2'] } });
    });

    await slidePanel.getByRole('button', { name: /Guardar/i }).click();

    // Verificar en Grilla Visual
    const rowContent = page.getByText('Estructurar E2E').locator('..');
    // Ambos deben mostrarse condensados o en su avatar en la fila
    await expect(rowContent.getByText('Alfonso QA')).toBeVisible();
    await expect(rowContent.getByText('Laura Dev')).toBeVisible();
  });
});
