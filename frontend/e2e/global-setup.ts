import { request, FullConfig } from '@playwright/test';
import * as fs from 'fs';
import * as path from 'path';

async function globalSetup(config: FullConfig) {
  const baseURL = process.env.E2E_BASE_URL || 'http://localhost:5173'; // Fallback to 5173 or 5176 depending on project
  // We'll use a direct request context to hit the backend directly or via the frontend proxy
  // It's safer to hit the proxy URL if it's running, or the backend at 8080.
  // Actually, let's hit the backend directly at 8080 to avoid depending on the dev server being fully up before setup
  // Wait, in Playwright, the webServer block starts the dev server.
  
  const requestContext = await request.newContext({
    baseURL: 'http://localhost:8080'
  });

  const response = await requestContext.post('/api/v1/auth/login', {
    data: {
      email: 'root@ibpms.local',
      password: 'admin'
    }
  });

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
            username: 'root@ibpms.local',
            roles: ['ROLE_PROCESS_ARCHITECT', 'ROLE_BPMN_DESIGNER', 'ROLE_USER'],
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
