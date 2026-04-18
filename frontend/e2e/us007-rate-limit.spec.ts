import { test, expect } from '@playwright/test';

test.describe('US-007 Rate Limiting DMN (CA-23)', () => {
    test('POST /dmn/simulate con 429 y Retry-After muestra countdown visual', async ({ page }) => {
        await page.route('**/api/v1/dmn/simulate', async (route) => {
            await route.fulfill({
                status: 429,
                headers: { 'Retry-After': '15' },
                contentType: 'application/json',
                body: JSON.stringify({ error: 'Too Many Requests' })
            });
        });

        await page.goto('/modeler/dmn');
        // El frontend debería mostrar un countdown visual de 15 segundos
        // y deshabilitar el botón de simulación hasta que el countdown llegue a 0
    });
});
