import { chromium } from 'playwright';

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage();
  
  page.on('console', msg => console.log(`BROWSER CONSOLE: ${msg.type()} - ${msg.text()}`));
  
  page.on('response', async response => {
    if (response.url().includes('/api/') || response.url().includes('login') || response.url().includes('auth')) {
      console.log(`API RESPONSE: ${response.status()} ${response.url()}`);
      if (response.status() >= 400) {
          try {
              console.log(`API ERROR BODY: ${await response.text()}`);
          } catch(e) {}
      }
    }
  });

  try {
    console.log("Navigating to frontend...");
    await page.goto('http://localhost:5173/');
    await page.waitForLoadState('networkidle');
    
    console.log("Clicking Break-Glass toggle...");
    await page.click('[data-testid="break-glass-toggle"]');
    
    await page.waitForSelector('input[type="email"]');
    console.log("Filling form...");
    await page.fill('input[type="email"]', 'admin@alpha.com');
    await page.fill('input[type="password"]', 'admin123');
    
    // There's a justification text area
    const textarea = page.locator('textarea');
    if (await textarea.count() > 0) {
        await textarea.fill('Testing emergency login error');
    }
    
    console.log("Clicking login...");
    // The button says "ACTIVAR ACCESO DE EMERGENCIA"
    const btn = page.locator('button:has-text("ACTIVAR ACCESO DE EMERGENCIA")');
    if (await btn.count() > 0) {
        await btn.click();
    } else {
        await page.click('button[type="submit"]');
    }
    
    // wait for response or timeout
    try {
        await page.waitForResponse(response => response.url().includes('/api/v1/auth/login'), { timeout: 3000 });
    } catch(e) {
        console.log("No API response detected in 3 seconds.");
    }
    
    console.log("DOM text after attempt:");
    const body = await page.evaluate(() => document.body.innerText);
    const alerts = await page.locator('.p-toast, .p-message, .error, .text-red-500').allInnerTexts();
    console.log("ALERTS ON SCREEN: ", alerts);
    
  } catch (err) {
    console.error("Test Error:", err);
  } finally {
    await browser.close();
  }
})();
