import { test, expect } from '@playwright/test';

test.describe('US-030 Hub Ágil - Detección Perimetral de Tickets Rancios (Stale)', () => {

  const projectId = 'PROJ-AGILE';

  test.beforeEach(async ({ page }) => {
    // Intercepción explícita pedida por el GATE para simular Ticket Rancio
    // Forzamos un updatedAt ubicado estratégicamente en "-16 días" al pasado.
    
    const pastelDate = new Date();
    pastelDate.setDate(pastelDate.getDate() - 16); // > 15 days threshold

    await page.route(`**/api/v1/projects/${projectId}/agile/tasks*`, async route => {
      await route.fulfill({ status: 200, json: { data: [
        {
          id: 'AT-STALE', 
          title: 'Refactor Legado', 
          status: 'TO_DO', 
          updatedAt: pastelDate.toISOString() 
        }
      ] } });
    });

    await page.goto(`/projects/${projectId}/agile-hub`);
  });

  test('Renderiza Alarma Ámbar y Días Inactivos para la Tarea Stale (CA-13)', async ({ page }) => {
    const ticketElement = page.getByText('Refactor Legado');
    await expect(ticketElement).toBeVisible();

    // Verificamos visualización del Badge
    const badgeRancio = page.locator('text=/Inactivo .* días/i'); // Ej: Inactivo 16 días
    await expect(badgeRancio).toBeVisible();

    // En CSS verificaríamos la clase ámbar, por ahora verificamos a11y DOM string
  });
});
