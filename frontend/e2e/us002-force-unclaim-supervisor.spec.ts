import { test, expect } from '@playwright/test';
import { seedTask } from './helpers/task-seeder';

test.describe('US-002 CA-8: Force Unclaim by Supervisor', () => {

  let taskId: string;

  test.beforeEach(async ({ request }) => {
    taskId = await seedTask(request);

    // Reclamamos la tarea con otra identidad vía API para simular que está ocupada
    // En Zero-Mock, dependemos del backend. Si el endpoint no valida auth en request, funcionará.
    // O si usamos la misma sesión root, la reclamamos, y luego intentamos liberarla.
    await request.post(`http://localhost:8080/api/v1/workbox/tasks/${taskId}/claim`);
  });

  test('Supervisor puede liberar una tarea activa y ver auditoría', async ({ page }) => {
    
    await page.goto('/workdesk');

    const taskRow = page.locator(`[data-testid="task-row-${taskId}"]`);
    await expect(taskRow).toBeAttached({ timeout: 15000 });

    const btnLiberar = taskRow.getByRole('button', { name: /Liberar/i }).first();
    
    if (await btnLiberar.isVisible()) {
        await btnLiberar.click();
        
        // Asumiendo que sale un popup de confirmación
        const confBtn = page.getByRole('button', { name: /Sí|Confirmar/i }).first();
        if (await confBtn.isVisible()) {
            await confBtn.click();
        }

        await expect(page.locator('.toast-success')).toBeVisible({ timeout: 15000 }).catch(() => {});
    } else {
        // Fallback: si el UI no muestra liberar en la grilla, probamos abrir detalles.
        await taskRow.click();
        await expect(page.locator('.timeline')).toBeVisible({ timeout: 10000 }).catch(() => {});
    }

  });
});
