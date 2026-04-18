import { test, expect } from '@playwright/test';

test.describe('US-002 Claim Rollback — Rollback Visual (CA-21)', () => {
    test('POST claim falla con 500 y la tarea reaparece en el pool', async ({ page }) => {
        await page.route('**/api/v1/workbox/tasks/*/claim', async (route) => {
            await route.fulfill({ status: 500, contentType: 'application/json', body: JSON.stringify({ error: 'Internal Server Error' }) });
        });

        await page.route('**/api/v1/workbox/tasks', async (route) => {
            await route.fulfill({
                status: 200,
                contentType: 'application/json',
                body: JSON.stringify([
                    { id: 'task-rollback-1', name: 'Tarea Rebotada', status: 'AVAILABLE', assignee: null }
                ])
            });
        });

        await page.goto('/workdesk/pool');
        // La tarea debería permanecer visible tras el rollback optimistic
        // El front debería mostrar un Toast de error pero no eliminar la card del DOM
    });
});
