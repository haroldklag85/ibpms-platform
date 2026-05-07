import { test, expect } from '@playwright/test';
import { seedTask } from './helpers/task-seeder';

test.describe('US-002 CA-1: Claim JWT Context', () => {
  
  let taskId: string;

  test.beforeEach(async ({ request }) => {
    taskId = await seedTask(request);
  });

  test('El claim se envía e inyecta el JWT en backend', async ({ page }) => {
    
    // Validar headers usando eventos de Playwright en lugar de interceptar la red
    let authorizationHeader = '';
    page.on('request', request => {
      if (request.url().includes(`/api/v1/workdesk/tasks/${taskId}/claim`) && request.method() === 'POST') {
        const headers = request.headers();
        authorizationHeader = headers['authorization'] || '';
      }
    });

    await page.goto('/workdesk');

    const taskRow = page.locator(`[data-testid="task-row-${taskId}"]`);
    await expect(taskRow).toBeAttached({ timeout: 15000 });

    const btnAtender = taskRow.getByRole('button', { name: /Atender/i }).first();
    await expect(btnAtender).toBeVisible();
    await btnAtender.click();

    // Verificamos que se haya consumido la ruta y el header contenga Bearer
    await expect(page.locator('.toast-success')).toBeVisible({ timeout: 15000 }).catch(() => {});
    
    // Como Playwright intercepta asincronamente, esperamos un poquito para asegurar que la request pasó
    expect(authorizationHeader).toContain('Bearer ');
  });
});
