const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext();
  const page = await context.newPage();
  
  page.on('console', msg => console.log('PAGE LOG:', msg.text()));
  page.on('pageerror', err => console.log('PAGE ERROR:', err.message));
  
  await page.goto('http://localhost:5173/login');
  await page.fill('input[type="email"]', 'root@ibpms.local');
  await page.fill('input[type="password"]', 'Root#Temp4Sys');
  await page.click('button[type="submit"]');
  
  await page.waitForNavigation({ waitUntil: 'domcontentloaded' });
  console.log('Navigated to:', page.url());
  
  page.on('request', req => {
    if (req.method() === 'POST' && req.url().includes('iso27001')) {
      console.log('REQ:', req.method(), req.url());
    }
  });
  page.on('response', res => {
    if (res.request().method() === 'POST' && res.url().includes('iso27001')) {
      console.log('RES:', res.status(), res.url());
    }
  });
  
  await page.goto('http://localhost:5173/admin/security/identity', { waitUntil: 'domcontentloaded' });
  console.log('Navigated to identity governance');
  
  console.log('Clicking button...');
  await page.click('[data-testid="btn-generate-iso"]');
  console.log('Button clicked!');
  
  await page.waitForTimeout(5000);
  console.log('Done');
  await browser.close();
})();
