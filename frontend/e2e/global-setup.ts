import { request, FullConfig } from '@playwright/test';
import * as fs from 'fs';
import * as path from 'path';
import { USERS } from './fixtures/e2e-data';

async function globalSetup(config: FullConfig) {
  const baseURL = process.env.E2E_BASE_URL || 'http://localhost:5173';
  
  const requestContext = await request.newContext({
    baseURL: 'http://localhost:8080'
  });

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
      });

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

      const { token, tenantId } = await response.json();
      saveStorageState(authDir, user.filename, baseURL, user.email, token, tenantId);

    } catch (e: any) {
      throw new Error(`Backend unreachable or login failed in global setup for ${user.email}. ` + e.message);
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
            roles: ['ROLE_OPERARIO', 'ROLE_USER'], // Simplified for tests
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
