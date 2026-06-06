import { defineConfig, devices } from '@playwright/experimental-ct-vue';
import { fileURLToPath } from 'url';
import path from 'path';
import dotenv from 'dotenv';

// Cargar variables de entorno de .env.local y .env
dotenv.config({ path: path.resolve(process.cwd(), '.env.local') });
dotenv.config({ path: path.resolve(process.cwd(), '.env') });

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
          '@': fileURLToPath(new URL('./src', import.meta.url)),
        },
      },
    },
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
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
});
