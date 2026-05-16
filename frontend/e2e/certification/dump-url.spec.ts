import { test, expect } from '@playwright/test';

test('Check URL and content', async ({ page }) => {
    await page.goto('/');
    
    // Attempt to set local storage, but wait, global-setup already does this!
    // But wait, the test is running in 'authenticated' context. So it should have ibpms_token.
    
    await page.goto('/admin/modeler/forms/designer');
    await page.waitForLoadState('networkidle', { timeout: 10000 }).catch(() => {});
    
    console.log('Current URL:', page.url());
    const content = await page.content();
    console.log('Contains IDE:', content.includes('IDE de Formularios'));
    console.log('Contains Login:', content.includes('login') || content.includes('Iniciar'));
});
