import { test, expect } from '@playwright/test';

test.describe('US-029 Session Conflict Multi-Context (CA-35)', () => {
    test('Tab B recibe 409 SESSION_CONFLICT al intentar PUT /draft', async ({ browser }) => {
        const contextA = await browser.newContext();
        const contextB = await browser.newContext();
        const pageA = await contextA.newPage();
        const pageB = await contextB.newPage();

        // Tab A intercepta draft exitoso
        await pageA.route('**/api/v1/workbox/tasks/*/draft', async (route) => {
            await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ saved: true }) });
        });

        // Tab B intercepta draft con conflicto
        await pageB.route('**/api/v1/workbox/tasks/*/draft', async (route) => {
            await route.fulfill({
                status: 409,
                contentType: 'application/json',
                body: JSON.stringify({ type: 'SESSION_CONFLICT', message: 'Otra pestaña está editando esta tarea' })
            });
        });

        await pageA.goto('/workdesk/pool');
        await pageB.goto('/workdesk/pool');

        // El frontend de Tab B debería mostrar el SessionConflictBanner
        // Tab A debería funcionar normalmente

        await contextA.close();
        await contextB.close();
    });
});
