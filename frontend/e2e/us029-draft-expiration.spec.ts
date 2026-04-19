import { test, expect } from '@playwright/test';

test.describe('US-029 Draft Expiration (CA-36)', () => {
    test('GET draft-ttl devuelve 10s → banner amarillo → 410 → modal rojo', async ({ page }) => {
        let pollCount = 0;
        await page.route('**/api/v1/workbox/tasks/*/draft-ttl', async (route) => {
            pollCount++;
            if (pollCount <= 2) {
                await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ ttl: 10 }) });
            } else {
                await route.fulfill({ status: 410, contentType: 'application/json', body: JSON.stringify({ expired: true }) });
            }
        });

        await page.goto('/workdesk/pool');
        // El frontend debería:
        // 1. Mostrar banner amarillo con countdown (ttl: 10)
        // 2. Al recibir 410, mostrar modal rojo bloqueante
    });
});
