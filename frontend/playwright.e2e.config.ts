import { defineConfig, devices } from '@playwright/test';
import * as dotenv from 'dotenv';
import path from 'path';

// Read from default ".env" or equivalent
dotenv.config();

export default defineConfig({
  testDir: './e2e/certification',
  timeout: 420000,
  expect: {
    timeout: 60000
  },
  // globalSetup: './e2e/global-setup.ts',
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
    baseURL: process.env.E2E_BASE_URL || 'http://localhost:5174',
    trace: 'retain-on-failure',
    video: 'on-first-retry',
    screenshot: 'only-on-failure',
    actionTimeout: 30000,
  },
  projects: [
    {
      name: 'e2e-certification',
      use: {
        ...devices['Desktop Chrome'],
        channel: 'chrome',
      },
    },
    {
      name: 'Zero-Mock-E2E',
      use: {
        ...devices['Desktop Chrome'],
        channel: 'chrome',
        baseURL: process.env.ZERO_MOCK_URL || 'http://localhost:5174',
      },
    },
  ],
});
