import { test, expect } from '@playwright/test';

test('Check console errors on FormDesigner', async ({ page }) => {
    const errors: string[] = [];
    page.on('console', msg => {
        if (msg.type() === 'error') {
            errors.push(msg.text());
        }
    });
    page.on('pageerror', error => {
        errors.push(error.message);
    });

    await page.goto('/admin/modeler/forms/designer');
    await page.waitForLoadState('networkidle', { timeout: 10000 }).catch(() => {});
    
    console.log('--- BROWSER ERRORS ---');
    console.log(errors.join('\n'));
    console.log('----------------------');
});
