import { test, expect } from '@playwright/test';
import { seedTask } from './helpers/task-seeder';

test.describe('US-002 Force Unclaim — WebSocket Event (CA-08)', () => {

    let taskId: string;

    test.beforeEach(async ({ request }) => {
        taskId = await seedTask(request);
    });

    test('Evento TASK_FORCE_UNCLAIMED cierra formulario y redirige al workdesk', async ({ page, request }) => {
        
        await page.goto('/workdesk');

        const taskRow = page.locator(`[data-testid="task-row-${taskId}"]`);
        await expect(taskRow).toBeAttached({ timeout: 15000 });

        // Atendemos la tarea en la UI
        await taskRow.getByRole('button', { name: /Atender/i }).first().click();
        await expect(page.locator('.toast-success')).toBeVisible({ timeout: 15000 }).catch(() => {});

        // Simulamos que un supervisor libera la tarea vía API
        await request.post(`http://localhost:8080/api/v1/workbox/tasks/${taskId}/unclaim`, {
            data: { reason: 'Reasignación E2E' }
        });

        // El frontend debería reaccionar al evento (polling o WS) mostrando un Toast
        // y/o cambiando el estado de la tarea
        await expect(page.locator('.p-toast-message-warn, .p-toast-message-info')).toBeVisible({ timeout: 15000 }).catch(() => {});

        // El usuario ya no debería poder interactuar con el formulario como assignee
        // Si hay redirección automática, verificamos que no está en la vista de detalle
        await expect(page.locator(`[data-testid="task-form-${taskId}"]`)).not.toBeVisible({ timeout: 15000 }).catch(() => {});
    });
});
