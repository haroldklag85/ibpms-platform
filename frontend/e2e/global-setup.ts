import { request, FullConfig } from '@playwright/test';
import * as fs from 'fs';
import * as path from 'path';
import { USERS } from './fixtures/e2e-data';

async function globalSetup(config: FullConfig) {
  const baseURL = process.env.E2E_BASE_URL || 'http://localhost:5173';
  
  const requestContext = await request.newContext({
    baseURL: 'http://127.0.0.1:8080'
  });

  const authDir = path.resolve('e2e/playwright/.auth');
  if (!fs.existsSync(authDir)) {
    fs.mkdirSync(authDir, { recursive: true });
  }

  // Usuarios requeridos para la suite E2E
  const usersToLogin = [
    { email: USERS.ADMIN_ALPHA?.email || 'root@ibpms.local', password: USERS.ADMIN_ALPHA?.password || 'Root#Temp4Sys', filename: 'user.json' },
    { email: USERS.ANALISTA_N1?.email || 'analista@ibpms.local', password: USERS.ANALISTA_N1?.password || 'password123', filename: 'analista_n1.json' },
    { email: USERS.DIRECTOR_1?.email || 'director@ibpms.local', password: USERS.DIRECTOR_1?.password || 'password123', filename: 'director_1.json' },
    { email: USERS.PERITO_A?.email || 'peritoa@ibpms.local', password: USERS.PERITO_A?.password || 'password123', filename: 'perito_a.json' },
    { email: USERS.PERITO_B?.email || 'peritob@ibpms.local', password: USERS.PERITO_B?.password || 'password123', filename: 'perito_b.json' }
  ];

  for (const user of usersToLogin) {
    let response;
    // Retry login up to 90 times waiting for the backend to be ready (15 minutes total wait)
    for (let attempt = 1; attempt <= 90; attempt++) {
      try {
        const endpoint = user.email.includes('root') ? '/api/v1/auth/emergency-login' : '/api/v1/auth/login';
        response = await requestContext.post(endpoint, {
          headers: { 'Content-Type': 'application/json' },
          data: {
            email: user.email,
            password: user.password
          },
          timeout: 10_000
        });
        
        if (response.ok()) {
            break; // Success!
        }
        
        // If not ok, and it's root, maybe it's just /api/v1/auth/login with admin password?
        if (!response.ok() && user.email.includes('root')) {
           const rootRetry = await requestContext.post('/api/v1/auth/login', {
             data: { email: user.email, password: 'admin' },
             timeout: 10_000
           });
           if (rootRetry.ok()) {
             response = rootRetry;
             break;
           }
        }
        throw new Error(`HTTP ${response.status()}`);
      } catch (err: any) {
        console.warn(`[global-setup] Login attempt ${attempt}/90 failed for ${user.email} (${err.message}), retrying in 5s...`);
        if (attempt === 90) {
          console.error(`[global-setup] Backend not reachable for ${user.email} after max attempts. Skipping auth setup.`);
          return;
        }
        await new Promise(r => setTimeout(r, 5_000));
      }
    }
    
    if (response && response.ok()) {
        const { token, tenantId } = await response.json();
        saveStorageState(authDir, user.filename, baseURL, user.email, token, tenantId);
    }
  }
}

function saveStorageState(authDir: string, filename: string, baseURL: string, email: string, token: string, tenantId: string) {
  const storageState = {
    cookies: [],
    origins: [
      {
        origin: baseURL,
        localStorage: [
          { name: 'ibpms_token', value: token },
          { name: 'ibpms_user', value: JSON.stringify({
            username: email,
            roles: email.includes('root') ? ['ROLE_SUPER_ADMIN'] : ['ROLE_OPERARIO', 'ROLE_USER'],
            email: email,
            tenantId: tenantId
          }) }
        ]
      }
    ]
  };
  fs.writeFileSync(path.join(authDir, filename), JSON.stringify(storageState));
}

export default globalSetup;
