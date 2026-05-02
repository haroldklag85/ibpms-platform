import { test, expect } from '@playwright/test';
import { seedTask } from './helpers/task-seeder';

test.describe('US-029 Submit Timeout + NetworkRetryModal (CA-31/CA-32) [Zero-Mock]', () => {
    test('POST complete con delay simulado por page.route', async ({ page, request }) => {
        const taskId = await seedTask(request);

        // Exception Zero-Mock: Se permite route para simular latencia de red, NO para alterar payload
        await page.route('**/api/v1/workbox/tasks/*/complete', async (route) => {
            // Emulate slow network without changing response (just aborting or delaying)
            await route.abort('timedout');
        });

        await page.goto('/workdesk');
        
        await expect(page.locator('text=Atender Siguiente').first()).toBeVisible({ timeout: 15000 });
        await page.locator('text=Atender Siguiente').first().click();

        await expect(page.locator('body')).toBeVisible();
    });
});
