import { test, expect } from '@playwright/test';

test.describe('US-030 Hub Ágil - Detección Perimetral de Tickets Rancios (Stale)', () => {

  const projectId = 'PROJ-AGILE';

  test.beforeEach(async ({ page }) => {
    // Intercepción explícita pedida por el GATE para simular Ticket Rancio
    // Forzamos un updatedAt ubicado estratégicamente en "-32 días" al pasado.
    
    const pastelDate = new Date();
    pastelDate.setDate(pastelDate.getDate() - 32); // > 30 days threshold

    await page.route(`**/api/v1/projects/${projectId}/board`, async route => {
      await route.fulfill({ status: 200, json: { 
        project: { id: projectId, key: 'PRJ', name: 'Agile Project', status: 'ACTIVE' },
        sprints: [], 
        backlogItems: [
          {
            id: 'AT-STALE', 
            title: 'Refactor Legado', 
            type: 'BUG',
            status: 'TO_DO', 
            assignees: [],
            updatedAt: pastelDate.toISOString() 
          }
        ] 
      } });
    });

    await page.goto(`/admin/projects/agile-hub/${projectId}`);
  });

  test('Renderiza Alarma Ámbar y Días Inactivos para la Tarea Stale (CA-13)', async ({ page }) => {
    const ticketElement = page.locator('.bg-white.border.border-slate-200.rounded').filter({ hasText: 'Refactor Legado' });
    await expect(ticketElement).toBeAttached();

    // Verificamos visualización del Badge (Title attribute in div)
    const badgeRancio = ticketElement.locator('div[title*=">30 días inactivo"]');
    await expect(badgeRancio).toBeAttached();
  });
});
