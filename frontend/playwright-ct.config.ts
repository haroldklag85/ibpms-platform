import { defineConfig, devices } from '@playwright/experimental-ct-vue';

/**
 * Playwright Component Testing Configuration
 * Cierre de hallazgo: Testing Stack Audit - Nivel 2 (Componente)
 *
 * Permite montar componentes Vue en un navegador REAL (Chromium)
 * para detectar bugs de CSS, scroll, layout que jsdom ignora.
 *
 * Ejecutar: npm run test:ct
 */
export default defineConfig({
  testDir: './src/tests/ct',
  snapshotDir: './src/tests/ct/__snapshots__',
  timeout: 10_000,
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  reporter: 'html',
  use: {
    trace: 'on-first-retry',
    ctPort: 3100,
    ctViteConfig: {
      resolve: {
        alias: {
          '@': '/src',
        },
      },
    },
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
});
