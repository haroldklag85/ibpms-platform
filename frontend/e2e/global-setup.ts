import { request, FullConfig } from '@playwright/test';
import * as fs from 'fs';
import * as path from 'path';
import { USERS } from './fixtures/e2e-data';

async function globalSetup(config: FullConfig) {
  const baseURL = process.env.E2E_BASE_URL || 'http://localhost:5173';
  
  const requestContext = await request.newContext({
    baseURL: 'http://127.0.0.1:8080'
  });

<<<<<<< HEAD
  const authDir = path.resolve('e2e/playwright/.auth');
  if (!fs.existsSync(authDir)) {
    fs.mkdirSync(authDir, { recursive: true });
  }

  // Usuarios requeridos para J-04 usando los importados de e2e-data
  const usersToLogin = [
    { email: USERS.ADMIN_ALPHA.email, password: USERS.ADMIN_ALPHA.password, filename: 'user.json' },
    { email: USERS.ANALISTA_N1.email, password: USERS.ANALISTA_N1.password, filename: 'analista_n1.json' },
    { email: USERS.DIRECTOR_1.email, password: USERS.DIRECTOR_1.password, filename: 'director_1.json' },
    { email: USERS.PERITO_A.email, password: USERS.PERITO_A.password, filename: 'perito_a.json' },
    { email: USERS.PERITO_B.email, password: USERS.PERITO_B.password, filename: 'perito_b.json' }
  ];

  for (const user of usersToLogin) {
    try {
      const response = await requestContext.post('/api/v1/auth/login', {
        data: {
          email: user.email,
          password: user.password
        }
=======
  // Retry login up to 90 times waiting for the backend to be ready (15 minutes total wait)
  let response;
  for (let attempt = 1; attempt <= 90; attempt++) {
    try {
      response = await requestContext.post('/api/v1/auth/emergency-login', {
        headers: { 'Content-Type': 'application/json' },
        data: {
          email: 'root@ibpms.local',
          password: 'Root#Temp4Sys'
        },
        timeout: 10_000
>>>>>>> origin/DevDavid
      });
      if (!response.ok()) {
         throw new Error(`HTTP ${response.status()}`);
      }
      break; // Success!
    } catch (err: any) {
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
        // Para root, usamos "admin" si "password123" falla
        if (user.email === 'root@ibpms.local') {
          const rootRetry = await requestContext.post('/api/v1/auth/login', {
            data: { email: user.email, password: 'admin' }
          });
          if (rootRetry.ok()) {
            const { token, tenantId } = await rootRetry.json();
            saveStorageState(authDir, user.filename, baseURL, user.email, token, tenantId);
            continue;
          }
        }
        throw new Error(`Failed to login ${user.email} in global setup: ` + response.statusText());
      }
<<<<<<< HEAD

      const { token, tenantId } = await response.json();
      saveStorageState(authDir, user.filename, baseURL, user.email, token, tenantId);

    } catch (e: any) {
      throw new Error(`Backend unreachable or login failed in global setup for ${user.email}. ` + e.message);
    }
  }
}
=======
>>>>>>> origin/DevDavid

function saveStorageState(authDir: string, filename: string, baseURL: string, email: string, token: string, tenantId: string) {
  const storageState = {
    cookies: [],
    origins: [
      {
        origin: baseURL,
        localStorage: [
          { name: 'ibpms_token', value: token },
          { name: 'ibpms_user', value: JSON.stringify({
<<<<<<< HEAD
            username: email,
            roles: ['ROLE_OPERARIO', 'ROLE_USER'], // Simplified for tests
            email: email,
=======
            username: '[Super_Administrador]',
            roles: ['ROLE_SUPER_ADMIN'],
            email: 'root@ibpms.local',
>>>>>>> origin/DevDavid
            tenantId: tenantId
          }) }
        ]
      }
    ]
  };
  fs.writeFileSync(path.join(authDir, filename), JSON.stringify(storageState));
}

export default globalSetup;
