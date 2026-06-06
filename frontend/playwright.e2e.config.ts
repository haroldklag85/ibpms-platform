import { defineConfig, devices } from '@playwright/test';
import * as dotenv from 'dotenv';
import path from 'path';

// Read from .env.local first, then fallback to default ".env"
dotenv.config({ path: path.resolve(process.cwd(), '.env.local') });
dotenv.config();

export default defineConfig({
  testDir: './e2e/certification',
  timeout: 420000,
  expect: {
    timeout: 60000
  },
  globalSetup: './e2e/global-setup.ts',
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
