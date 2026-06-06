import { defineConfig, devices } from '@playwright/test';
import dotenv from 'dotenv';
import path from 'path';

// Cargar variables de entorno de .env.local y .env
dotenv.config({ path: path.resolve(process.cwd(), '.env.local') });
dotenv.config({ path: path.resolve(process.cwd(), '.env') });

const E2E_JWT = 'eyJhbGciOiJub25lIn0=.eyJzdWIiOiJyb290X2UyZSIsInJvbGVzIjpbIlJPTEVfU1VQRVJfQURNSU4iLCJST0xFX09QRVJBRE9SIiwiUk9MRV9BSV9BRE1JTiJdLCJlbWFpbCI6InJvb3RAaWJwbXMubG9jYWwiLCJleHAiOjk5OTk5OTk5OTl9.e2e_sig';

export default defineConfig({
  testDir: './e2e',
  timeout: 420_000,
  expect: {
    timeout: 45_000
  },
  globalSetup: './e2e/global-setup.ts',
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
    baseURL: process.env.E2E_BASE_URL || 'http://localhost:5173',
    launchOptions: process.env.PLAYWRIGHT_USE_GPU === 'true' ? {
      args: [
        '--ignore-gpu-blocklist',
        '--enable-gpu-rasterization',
        '--enable-zero-copy',
        '--use-gl=angle',
        '--use-angle=vulkan'
      ]
    } : undefined
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
        storageState: 'e2e/playwright/.auth/user.json',
      },
    },
  ],
  webServer: {
    command: 'npm run dev',
    port: 5173,
    reuseExistingServer: true,
  },
});
