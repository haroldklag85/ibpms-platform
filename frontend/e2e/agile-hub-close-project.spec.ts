import { test, expect } from '@playwright/test';

test.describe('US-030 Hub Ágil - Cascading Freeze Cierre de Proyecto', () => {

  const projectId = 'PROJ-CLOSED';

  test.beforeEach(async ({ page }) => {
    // Mock board and close project APIs
    await page.route(`**/api/v1/projects/${projectId}*`, async route => {
      if (route.request().url().includes('close')) {
         console.log('PAGE ERROR LOG:', 'Intercepted CLOSE call!');
         await route.fulfill({ status: 200, json: { status: 'CLOSED' } });
      } else if (route.request().url().includes('board')) {
         await route.fulfill({ status: 200, json: { 
           project: { id: projectId, key: 'PRJ', name: 'Closed Project', status: 'ACTIVE' },
           sprints: [], backlogItems: [{ id: 'AT-1', title: 'Tarea Pronta a Cancelarse', status: 'TO_DO', assignees: [], tags: [] }]
         }});
      } else {
        await route.fallback();
      }
    });

    await page.goto(`/admin/projects/agile-hub/${projectId}`);
  });

  test('Validar Cascada Visual Solo Lectura al cerrar Proyecto (CA-10)', async ({ page }) => {
    const btnCerrar = page.getByRole('button', { name: /Terminar Proyecto|Cerrar Proyecto/i });
    
    // Validar visibilidad del master button
    await expect(btnCerrar).toBeVisible();
    await btnCerrar.click();

    // Esperar ruteo y recarga de la data (el componente recargará el board)
    await page.route(`**/api/v1/projects/${projectId}/board`, async route => {
      await route.fulfill({ status: 200, json: { 
        project: { id: projectId, key: 'PRJ', name: 'Closed Project', status: 'CLOSED' },
        sprints: [], 
        backlogItems: [{ id: 'AT-1', title: 'Tarea Pronta a Cancelarse', status: 'CANCELLED', assignees: [], tags: [] }] 
      }});
    });

    // Alerta de confirmación de cierre
    const confirmacion = page.getByRole('dialog');
    await confirmacion.getByRole('button', { name: /Confirmar/i }).click();

    // Simulamos un reload natural si fuera necesario, aunque el componente lo hará auto
    // await page.reload();

    const noButton = page.getByRole('button', { name: /\+ Nueva Tarea/i });
    await expect(noButton).toBeHidden(); // Solo lectura estricto

    await expect(page.getByText('CANCELLED').or(page.getByText('CANCELADA'))).toBeVisible();
  });
});
