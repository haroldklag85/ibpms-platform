const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch();
  const context = await browser.newContext();
  const page = await context.newPage();

  const requests = [];
  page.on('request', request => {
    if (request.url().includes('global-inbox')) {
      requests.push(request.url());
    }
  });

  console.log('Navigating to login...');
  await page.goto('http://localhost:5177/login?emergency=true');
  
  // Login
  await page.fill('input[type="text"]', 'root@ibpms.local');
  await page.fill('input[type="password"]', 'admin');
  await page.click('button:has-text("Ingresar al Portal")');
  
  console.log('Navigating to workdesk...');
  await page.waitForTimeout(2000);
  await page.goto('http://localhost:5177/workdesk');
  await page.waitForTimeout(2000);
  
  // Click Tareas de mi asistente
  const delegacionButton = await page.$('text="Tareas de mi Asistente"');
  if (delegacionButton) {
     console.log('Clicking Tareas de mi asistente...');
     await delegacionButton.click();
     await page.waitForTimeout(2000);
  } else {
     // Maybe it's a toggle
     const toggle = await page.$('.group\\/toggles');
     if (toggle) {
         await toggle.click();
         await page.waitForTimeout(2000);
     }
  }

  console.log('Intercepted requests to global-inbox:');
  requests.forEach(req => console.log(req));

  const hasSize15 = requests.every(req => req.includes('size=15'));
  const hasSize50 = requests.some(req => req.includes('size=50'));

  if (requests.length > 0 && hasSize15 && !hasSize50) {
      console.log('RESULT: PASS - All requests use size=15');
  } else {
      console.log('RESULT: FAIL - Found invalid sizes or no requests');
  }

  await browser.close();
})();
