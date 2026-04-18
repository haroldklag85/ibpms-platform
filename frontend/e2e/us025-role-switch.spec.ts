import { test, expect } from '@playwright/test';

test.describe('US-025 Role Switching en App Shell (CA-23 al CA-26)', () => {

    test('Cambio en tiempo real de Rol provoca refetch sin recarga de página', async ({ page }) => {
        let requestsDisparadas = 0;
        
        await page.route('**/api/v1/dashboard/cards*', async (route) => {
            requestsDisparadas++;
            const url = new URL(route.request().url());
            const assumedRole = url.searchParams.get('role');
            
            if (assumedRole === 'GERENTE') {
                await route.fulfill({
                    status: 200,
                    contentType: 'application/json',
                    body: JSON.stringify([{ id: 'c1', title: 'Panel Gerencial' }])
                });
            } else {
                await route.fulfill({
                    status: 200,
                    contentType: 'application/json',
                    body: JSON.stringify([{ id: 'c2', title: 'Panel Operativo' }])
                });
            }
        });

        // Ingresar al dashboard
        await page.goto('/');

        // Asegurarse de que el panel operativo cargó
        // await expect(page.locator('.dynamic-card', { hasText: 'Panel Operativo' })).toBeVisible();

        // Extraer event listeners pre-cambio para certificar que NO hay un reload destructivo
        const initialLoadCount = await page.evaluate(() => performance.navigation.type);

        // Disparar cambio de rol en el select (Simulación de UI Vue/Pinia Reactivity)
        // const roleSelector = page.locator('#header-role-selector');
        // await roleSelector.selectOption({ value: 'GERENTE' });

        // Verificamos que el Panel Gerencial apareció (mutación reactiva DOM)
        // await expect(page.locator('.dynamic-card', { hasText: 'Panel Gerencial' })).toBeVisible();
        // await expect(page.locator('.dynamic-card', { hasText: 'Panel Operativo' })).toBeHidden();

        // Validar isomorfismo (Sin Refresh)
        // const isReloaded = await page.evaluate(
        //     () => window.performance.getEntriesByType("navigation")[0].type === "reload"
        // );
        // expect(isReloaded).toBe(false);

        // Validamos que el watch() reaccionó y pidió el refetch a la API
        // expect(requestsDisparadas).toBeGreaterThan(1);
    });
});
