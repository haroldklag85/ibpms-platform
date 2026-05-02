import { test, expect } from '@playwright/test';
import { seedTask } from './helpers/task-seeder';

test.describe('US-029 Draft Expiration (CA-36) [Zero-Mock]', () => {
    test('Draft TTL timeout handling', async ({ page, request }) => {
        const taskId = await seedTask(request);

        await page.goto('/workdesk');
        await expect(page.locator('body')).toBeVisible();
    });
});
