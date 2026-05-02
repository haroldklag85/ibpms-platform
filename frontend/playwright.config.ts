import { defineConfig, devices } from '@playwright/test';

const E2E_JWT = 'eyJhbGciOiJub25lIn0=.eyJzdWIiOiJyb290X2UyZSIsInJvbGVzIjpbIlJPTEVfU1VQRVJfQURNSU4iLCJST0xFX09QRVJBRE9SIiwiUk9MRV9BSV9BRE1JTiJdLCJlbWFpbCI6InJvb3RAaWJwbXMubG9jYWwiLCJleHAiOjk5OTk5OTk5OTl9.e2e_sig';

export default defineConfig({
  testDir: './e2e',
  timeout: 180_000,
  expect: {
    timeout: 45_000
  },
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: 1,
  workers: 1,
  reporter: [['html'], ['list']],
  use: {
    actionTimeout: 30_000,
    navigationTimeout: 60_000,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
    baseURL: process.env.E2E_BASE_URL || 'http://localhost:5176',
  },
  projects: [
    {
      // Project para tests de LOGIN que NO deben tener token pre-inyectado
      name: 'login-tests',
      testMatch: /emergency-login/,
      use: { ...devices['Desktop Chrome'] },
    },
    {
      // Project para TODO lo demás: inyecta token para bypass del auth guard
      name: 'authenticated',
      testIgnore: /emergency-login/,
      use: {
        ...devices['Desktop Chrome'],
        storageState: {
          cookies: [],
          origins: [
            {
              origin: process.env.E2E_BASE_URL || 'http://localhost:5176',
              localStorage: [
                { name: 'ibpms_token', value: E2E_JWT },
                { name: 'ibpms_user', value: JSON.stringify({
                  username: 'root_e2e',
                  roles: ['ROLE_SUPER_ADMIN', 'ROLE_OPERADOR', 'ROLE_AI_ADMIN'],
                  email: 'root@ibpms.local'
                }) },
              ],
            },
          ],
        },
      },
    },
  ],
  webServer: {
    command: 'npm run dev',
    port: 5176,
    reuseExistingServer: true,
  },
});
