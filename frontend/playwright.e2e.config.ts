import { defineConfig, devices } from '@playwright/test';
import * as dotenv from 'dotenv';
import path from 'path';

// Read from default ".env" or equivalent
dotenv.config({ path: path.resolve(__dirname, '.env') });

export default defineConfig({
  testDir: './e2e/certification',
  timeout: 90000,
  expect: {
    timeout: 10000
  },
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 1, // UAT Resilience
  workers: process.env.CI ? 1 : undefined,
  reporter: [
    ['html', { outputFolder: 'playwright-e2e-report' }],
    ['list']
  ],
  use: {
    // E2E against real backend
    baseURL: process.env.E2E_BASE_URL || 'http://localhost:5173',
    trace: 'retain-on-failure',
    video: 'on-first-retry',
    screenshot: 'only-on-failure',
    actionTimeout: 15000,
  },
  projects: [
    {
      name: 'e2e-auth-setup',
      testMatch: /.*\.setup\.ts/,
    },
    {
      name: 'e2e-certification',
      use: {
        ...devices['Desktop Chrome'],
        channel: 'chrome',
        // Use prepared auth state.
        storageState: 'e2e/.auth/user.json',
      },
      dependencies: ['e2e-auth-setup'],
    },
  ],
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: !process.env.CI,
    timeout: 120 * 1000,
  },
});
