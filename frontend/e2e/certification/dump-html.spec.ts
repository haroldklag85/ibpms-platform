import { test, expect } from '@playwright/test';
import * as fs from 'fs';

test('Dump FormDesigner HTML', async ({ page }) => {
    await page.goto('/');
    await page.evaluate(() => {
        const userStr = localStorage.getItem('ibpms_user');
        if (userStr) {
            const user = JSON.parse(userStr);
            user.roles = ['ROLE_SUPER_ADMIN', 'Global Admin'];
            localStorage.setItem('ibpms_user', JSON.stringify(user));
        }
    });

    await page.goto('/admin/modeler/forms/designer');
    // Wait for network idle or a short timeout
    await page.waitForLoadState('networkidle', { timeout: 10000 }).catch(() => {});
    
    // Dump HTML
    const html = await page.content();
    fs.writeFileSync('form-designer-dump.html', html);
});
