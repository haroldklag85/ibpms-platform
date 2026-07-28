import { test, expect } from '@playwright/test';

test('Check button click', async ({ page }) => {
  page.on('console', msg => console.log('PAGE LOG:', msg.text()));
  page.on('pageerror', err => console.log('PAGE ERROR:', err));
  page.on('request', req => console.log('REQ:', req.method(), req.url()));

  await page.goto('/admin/security/identity', { waitUntil: 'domcontentloaded' });
  
  // click the button
  console.log('Clicking button...');
  const reportBtn = page.locator('[data-testid="btn-generate-iso"]');
  await reportBtn.click();
  
  await page.waitForTimeout(5000);
});
