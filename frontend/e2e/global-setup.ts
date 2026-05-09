import { request, FullConfig } from '@playwright/test';
import * as fs from 'fs';
import * as path from 'path';

async function globalSetup(config: FullConfig) {
  const baseURL = process.env.E2E_BASE_URL || 'http://localhost:5176';
  
  const requestContext = await request.newContext({
    baseURL: 'http://localhost:8080'
  });

  // Retry login up to 90 times waiting for the backend to be ready (15 minutes total wait)
  let response;
  for (let attempt = 1; attempt <= 90; attempt++) {
    try {
      response = await requestContext.post('/api/v1/auth/emergency-login', {
        headers: { 'Content-Type': 'application/json' },
        data: '{"email":"root@ibpms.local","password":"Root#Temp4Sys"}',
        timeout: 10_000
      });
      if (!response.ok()) {
         throw new Error(`HTTP ${response.status()}`);
      }
      break; // Success!
    } catch (err) {
      console.warn(`[global-setup] Login attempt ${attempt}/90 failed (${err.message}), retrying in 10s...`);
      if (attempt === 90) {
        console.error('[global-setup] Backend not reachable after 15 minutes. Skipping auth setup.');
        return;
      }
      await new Promise(r => setTimeout(r, 10_000));
    }
  }
  if (!response) return;

  if (!response.ok()) {
    console.warn('Failed to login in global setup: ' + response.statusText());
    // Evitamos lanzar throw si la bd no tiene a root para no bloquear las demás suites
    return;
  }

  const { token, tenantId } = await response.json();

  // We need to write this to a storage state JSON that Playwright can use
  // We'll simulate the localStorage structure expected by our app
  const storageState = {
    cookies: [],
    origins: [
      {
        origin: baseURL,
        localStorage: [
          { name: 'ibpms_token', value: token },
          { name: 'ibpms_user', value: JSON.stringify({
            username: '[Super_Administrador]',
            roles: ['ROLE_SUPER_ADMIN'],
            email: 'root@ibpms.local',
            tenantId: tenantId
          }) }
        ]
      }
    ]
  };

  const authDir = path.resolve('e2e/playwright/.auth');
  if (!fs.existsSync(authDir)) {
    fs.mkdirSync(authDir, { recursive: true });
  }
  fs.writeFileSync(path.join(authDir, 'user.json'), JSON.stringify(storageState));
}

export default globalSetup;
