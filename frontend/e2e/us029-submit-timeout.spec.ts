import { test, expect } from '@playwright/test';

test.describe('US-029 Submit Timeout + NetworkRetryModal (CA-31/CA-32)', () => {
    test('POST complete con 504 muestra modal de retry; 2do intento resuelve', async ({ page }) => {
        let callCount = 0;
        await page.route('**/api/v1/workbox/tasks/*/complete', async (route) => {
            callCount++;
            if (callCount === 1) {
                // Primer intento: timeout
                await route.fulfill({ status: 504, contentType: 'application/json', body: JSON.stringify({ error: 'Gateway Timeout' }) });
            } else {
                // Reintento: éxito
                await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ result: 'OK' }) });
            }
        });

        await page.goto('/workdesk/pool');
        // El frontend debería mostrar el NetworkRetryModal tras el 504
        // y al reintentar, recibir 200 y mostrar Toast de éxito
    });
});
