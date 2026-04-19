import { defineConfig, devices } from '@playwright/test';
import * as dotenv from 'dotenv';
import path from 'path';

import { fileURLToPath } from 'url';
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Read from default ".env" or equivalent
dotenv.config({ path: path.resolve(__dirname, '.env') });

export default defineConfig({
  testDir: './e2e/certification',
  timeout: 60000,
  expect: {
    timeout: 10000
  },
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 1, // UAT Resilience
  workers: process.env.CI ? 2 : undefined,
  reporter: [
    ['html', { outputFolder: 'playwright-e2e-report' }],
    ['list']
  ],
  use: {
    // E2E against real backend
    baseURL: process.env.E2E_BASE_URL || 'http://localhost:4173',
    trace: 'retain-on-failure',
    video: 'on-first-retry',
    screenshot: 'only-on-failure',
    actionTimeout: 15000,
  },
  projects: [
    {
      name: 'Google Chrome UAT',
      use: {
        ...devices['Desktop Chrome'],
        channel: 'chrome',
      },
    },
    // More browsers can be added later if needed
  ],
  // Emulate web server ONLY if we want frontend to start. 
  // For Real E2E, we assume Backend (8080) and DB are running externally mapped.
});
