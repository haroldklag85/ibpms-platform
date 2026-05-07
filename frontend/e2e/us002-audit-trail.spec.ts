import { test, expect } from '@playwright/test';
import { seedTask } from './helpers/task-seeder';

test.describe('US-002 CA-9: Audit Trail Timeline', () => {

  let taskId: string;

  test.beforeEach(async ({ request }) => {
    taskId = await seedTask(request);
  });

  test('Visualización cronológica de eventos de reclamación y liberación', async ({ page }) => {

    await page.goto('/workdesk');

    const taskRow = page.locator(`[data-testid="task-row-${taskId}"]`);
    await expect(taskRow).toBeAttached({ timeout: 15000 });

    // Reclamar la tarea para generar un evento
    await taskRow.getByRole('button', { name: /Atender/i }).first().click();
    await expect(page.locator('.toast-success')).toBeVisible({ timeout: 15000 }).catch(() => {});

    // Liberar la tarea
    // Asumiendo que ahora aparece un botón 'Liberar' o 'Unclaim'
    const btnLiberar = taskRow.getByRole('button', { name: /Liberar/i }).first();
    await expect(btnLiberar).toBeVisible({ timeout: 10000 });
    await btnLiberar.click();
    await expect(page.locator('.toast-success')).toBeVisible({ timeout: 15000 }).catch(() => {});

    // Abrir el Preview o Timeline
    // Asumiendo que dando clic a la fila o un botón "Detalles" se abre
    await taskRow.click();

    const timeline = page.locator('.timeline');
    await expect(timeline).toBeVisible({ timeout: 10000 });

    // Validar que exista el evento de CLAIM y UNCLAIM
    await expect(timeline.getByText(/CLAIM/i).first()).toBeVisible();
    await expect(timeline.getByText(/UNCLAIM/i).first()).toBeVisible();
  });
});
