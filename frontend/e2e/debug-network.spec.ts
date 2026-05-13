import { test, expect } from '@playwright/test';

test.use({ storageState: 'e2e/playwright/.auth/analista_n1.json' });

test('Debug network', async ({ page }) => {
  page.on('response', async response => {
    if (response.url().includes('global-inbox')) {
      console.log('STATUS:', response.status());
      try {
        const body = await response.json();
        console.log('BODY:', JSON.stringify(body, null, 2));
      } catch (e) {}
    }
  });

  await page.goto('/workdesk');
  await page.waitForTimeout(5000); // Wait 5 seconds
});
