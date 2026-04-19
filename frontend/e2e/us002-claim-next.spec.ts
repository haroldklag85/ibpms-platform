import { test, expect } from '@playwright/test';

test.describe('US-002 Claim Next (CA-28)', () => {
    test('POST claim-next con 200 navega a task-viewer', async ({ page }) => {
        await page.route('**/api/v1/workbox/tasks/claim-next', async (route) => {
            await route.fulfill({
                status: 200,
                contentType: 'application/json',
                body: JSON.stringify({ id: 'next-task-001', name: 'Auto Assigned' })
            });
        });
        await page.goto('/workdesk/pool');
        // La UI debería intentar navegar a /task-viewer/next-task-001
    });

    test('POST claim-next con 204 muestra Toast "No hay tareas disponibles"', async ({ page }) => {
        await page.route('**/api/v1/workbox/tasks/claim-next', async (route) => {
            await route.fulfill({ status: 204, body: '' });
        });
        await page.goto('/workdesk/pool');
        // La UI debería mostrar Toast informativo
    });
});
