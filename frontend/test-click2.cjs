const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext();
  const page = await context.newPage();
  
  page.on('console', msg => console.log('PAGE LOG:', msg.text()));
  page.on('pageerror', err => console.log('PAGE ERROR:', err.message));
  
  await page.goto('http://127.0.0.1:8080/api/v1/auth/emergency-login', { waitUntil: 'networkidle' });
  const response = await page.request.post('http://127.0.0.1:8080/api/v1/auth/emergency-login', {
    data: {
      email: 'root@ibpms.local',
      password: 'Root#Temp4Sys',
      tenantId: 'tenant_alpha'
    }
  });
  const data = await response.json();
  console.log('Login Response:', data);
  
  const state = await context.storageState();
  const origins = state.origins || [];
  origins.push({
    origin: 'http://localhost:5173',
    localStorage: [
      { name: 'ibpms_token', value: data.token },
      { name: 'ibpms_user', value: JSON.stringify({
            username: '[Super_Administrador]',
            roles: ['ROLE_SUPER_ADMIN'],
            email: 'root@ibpms.local',
            tenantId: 'tenant_alpha'
      }) }
    ]
  });
  
  const context2 = await browser.newContext({ storageState: { cookies: [], origins } });
  const page2 = await context2.newPage();
  page2.on('console', msg => console.log('PAGE2 LOG:', msg.text()));
  
  console.log('Going to Identity Governance...');
  await page2.goto('http://localhost:5173/admin/security/identity', { waitUntil: 'networkidle' });
  
  const content = await page2.innerHTML('body');
  require('fs').writeFileSync('dom-debug.html', content);
  console.log('DOM length:', content.length);
  
  const btn = page2.locator('[data-testid="btn-generate-iso"]');
  const count = await btn.count();
  console.log('Button count:', count);
  
  if (count > 0) {
    await btn.click({ timeout: 5000 });
    console.log('Clicked!');
  } else {
    console.log('Button not found!');
  }
  
  await page2.screenshot({ path: 'debug-screen.png' });
  console.log('Done');
  
  await browser.close();
})();
