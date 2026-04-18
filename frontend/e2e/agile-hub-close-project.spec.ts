import { test, expect } from '@playwright/test';

test.describe('US-030 Hub Ágil - Cascading Freeze Cierre de Proyecto', () => {

  const projectId = 'PROJ-CLOSED';

  test.beforeEach(async ({ page }) => {
    // Escenario de Proyecto Activo Cerrándose
    await page.route(`**/api/v1/projects/${projectId}*`, async route => {
      // Endpoint de cierre del proyecto
      if (route.request().method() === 'POST' && route.request().url().includes('close')) {
         await route.fulfill({ status: 200, json: { status: 'CLOSED' } });
      } else {
        await route.fallback();
      }
    });

    await page.route(`**/api/v1/projects/${projectId}/agile/tasks*`, async route => {
      await route.fulfill({ status: 200, json: { data: [
        { id: 'AT-1', title: 'Tarea Pronta a Cancelarse', status: 'TO_DO' }
      ] } });
    });

    await page.goto(`/projects/${projectId}`);
  });

  test('Validar Cascada Visual Solo Lectura al cerrar Proyecto (CA-10)', async ({ page }) => {
    const btnCerrar = page.getByRole('button', { name: /Terminar Proyecto|Cerrar Proyecto/i });
    
    // Validar visibilidad del master button
    await expect(btnCerrar).toBeVisible();
    await btnCerrar.click();

    // Alerta de confirmación de cierre
    const confirmacion = page.getByRole('dialog');
    await confirmacion.getByRole('button', { name: /Confirmar/i }).click();

    // Esperar ruteo y recarga de la data
    await page.route(`**/api/v1/projects/${projectId}/agile/tasks*`, async route => {
      await route.fulfill({ status: 200, json: { data: [
        { id: 'AT-1', title: 'Tarea Pronta a Cancelarse', status: 'CANCELLED' } // Cascading update simulada
      ] } });
    });

    // Validar estado de la tarea (Cancelada) y solo-lectura UI (Botón Añadir Inhabilidades)
    await page.goto(`/projects/${projectId}/agile-hub`);

    const noButton = page.getByRole('button', { name: /\+ Nueva Tarea/i });
    await expect(noButton).toBeHidden(); // Solo lectura estricto

    await expect(page.getByText('CANCELLED').or(page.getByText('CANCELADA'))).toBeVisible();
  });
});
