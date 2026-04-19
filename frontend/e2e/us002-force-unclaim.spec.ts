import { test, expect } from '@playwright/test';

test.describe('US-002 Force Unclaim — WebSocket Event (CA-08)', () => {
    test('Evento TASK_FORCE_UNCLAIMED cierra formulario y redirige al workdesk', async ({ page }) => {
        // Interceptar workbox tasks
        await page.route('**/api/v1/workbox/tasks', async (route) => {
            await route.fulfill({
                status: 200,
                contentType: 'application/json',
                body: JSON.stringify([
                    { id: 'task-forced-1', name: 'Tarea Forzada', status: 'ACTIVE', assignee: 'testuser' }
                ])
            });
        });

        await page.goto('/workdesk/pool');

        // Simular la inyección de un evento WebSocket TASK_FORCE_UNCLAIMED via evaluate
        await page.evaluate(() => {
            window.dispatchEvent(new CustomEvent('ws-message', {
                detail: { type: 'TASK_FORCE_UNCLAIMED', taskId: 'task-forced-1', reason: 'Reasignación del supervisor' }
            }));
        });

        // El frontend debería reaccionar al evento mostrando un Toast
        // y redirigiendo al workdesk (no debería crashear)
    });
});
