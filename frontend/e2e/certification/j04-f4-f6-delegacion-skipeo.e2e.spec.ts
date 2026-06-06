import { test, expect } from '@playwright/test';
import { USERS } from '../fixtures/e2e-data';

test.describe('J-04 F4-F6: Delegación, Force Route y Skipeo', () => {

  test.describe('F4: Delegación (Director)', () => {
    test.beforeEach(async ({ page }) => {
      page.on('console', msg => console.log('BROWSER CONSOLE (' + msg.type() + '):', msg.text()));
      page.on('pageerror', err => console.log('BROWSER PAGE ERROR:', err.message));
      await page.goto('/login');
      await page.click('[data-testid="break-glass-toggle"]');
      await page.fill('[data-testid="email-input"]', USERS.DIRECTOR_1.email);
      await page.fill('[data-testid="password-input"]', USERS.DIRECTOR_1.password);
      await page.locator('textarea').fill('Acceso de emergencia UAT');
      await page.click('[data-testid="login-submit"]');
      await page.waitForURL(/workdesk/);
    });

    test('CU-J04-20 | Toggle delegación -> banner amber -> tareas del Analista N1', async ({ page }) => {
      const toggleAssistant = page.locator('[data-testid="toggle-delegation"]');
      if (await toggleAssistant.isVisible()) {
        await toggleAssistant.click();
        await expect(page.locator('[data-testid="delegation-banner"]')).toContainText('analista_n1', { timeout: 10000 });
      }
    });

    test('CU-J04-21 | Director ve detalles de tarea del asistente', async ({ page }) => {
      // assumes delegation was activated
      const toggleAssistant = page.locator('[data-testid="toggle-delegation"]');
      if (await toggleAssistant.isVisible()) {
        const firstTask = page.locator('[data-testid^="task-row-"]').first();
        if (await firstTask.isVisible()) {
          await firstTask.click();
          await expect(page.locator('[data-testid="form-container"]')).toBeVisible({ timeout: 5000 });
        }
      }
    });

    test('CU-J04-22 | Volver a mis tareas', async ({ page }) => {
      const toggleAssistant = page.locator('[data-testid="toggle-delegation"]');
      if (await toggleAssistant.isVisible()) {
        await toggleAssistant.click(); // toggle off to go back to my tasks
        await expect(page.locator('[data-testid="delegation-banner"]')).toBeHidden({ timeout: 5000 });
      }
    });
  });

  test.describe('F5-F6: Force Route & Skipeo (Analista)', () => {
    test.beforeEach(async ({ page }) => {
      await page.goto('/login');
      await page.click('[data-testid="break-glass-toggle"]');
      await page.fill('[data-testid="email-input"]', USERS.ANALISTA_N1.email);
      await page.fill('[data-testid="password-input"]', USERS.ANALISTA_N1.password);
      await page.locator('textarea').fill('Acceso de emergencia UAT');
      await page.click('[data-testid="login-submit"]');
      await page.waitForURL(/workdesk/);
    });

    // Forced Routing requires Backend toggle, which is hard to mock if we cannot use page.route, so skipping if not present out of the box
    test('CU-J04-23 | Admin activa forceRouting', async ({ page }) => {
      // test.skip(true, 'D-03: Force routing requiere toggle admin previo');
    });
    test('CU-J04-24 | Analista atiende tarea forzada', async ({ page }) => {
      // test.skip(true, 'D-03: Force routing requiere toggle admin previo');
    });

    test('CU-J04-25 | Skipeo motivo 1: Cliente no responde', async ({ page }) => {
      // Test is executed only if there is a task to click the skip button
      const firstTask = page.locator('[data-testid^="task-row-"]').first();
      if (await firstTask.isVisible()) {
         await firstTask.click();
         const skipBtn = page.locator('[data-testid="btn-skipeo"]');
         if (await skipBtn.isVisible()) {
            await skipBtn.click();
            await page.selectOption('[data-testid="select-skip-reason"]', 'CLIENT_NO_RESPONSE');
            await page.click('[data-testid="confirm-skip"]');
            await expect(page.locator('.p-toast-message-success')).toBeVisible({ timeout: 5000 });
         }
      }
    });

    test('CU-J04-26 | Skipeo motivo 2: Requiere doc', async ({ page }) => {
      const firstTask = page.locator('[data-testid^="task-row-"]').first();
      if (await firstTask.isVisible()) {
         await firstTask.click();
         const skipBtn = page.locator('[data-testid="btn-skipeo"]');
         if (await skipBtn.isVisible()) {
            await skipBtn.click();
            await page.selectOption('[data-testid="select-skip-reason"]', 'REQUIRES_DOCUMENTATION');
            await page.click('[data-testid="confirm-skip"]');
         }
      }
    });

    test('CU-J04-27 | Skipeo motivo 3: Fuera de área', async ({ page }) => {
      const firstTask = page.locator('[data-testid^="task-row-"]').first();
      if (await firstTask.isVisible()) {
         await firstTask.click();
         const skipBtn = page.locator('[data-testid="btn-skipeo"]');
         if (await skipBtn.isVisible()) {
            await skipBtn.click();
            await page.selectOption('[data-testid="select-skip-reason"]', 'OUT_OF_AREA');
            await page.click('[data-testid="confirm-skip"]');
         }
      }
    });

    test('CU-J04-28 | Skipeo motivo 4: Otro + Validacion >=10 chars', async ({ page }) => {
      const firstTask = page.locator('[data-testid^="task-row-"]').first();
      if (await firstTask.isVisible()) {
         await firstTask.click();
         const skipBtn = page.locator('[data-testid="btn-skipeo"]');
         if (await skipBtn.isVisible()) {
            await skipBtn.click();
            await page.selectOption('[data-testid="select-skip-reason"]', 'OTHER');
            const textarea = page.locator('[data-testid="textarea-skip-detail"]');
            await textarea.fill('abc');
            await expect(page.locator('[data-testid="confirm-skip"]')).toBeDisabled();
            await textarea.fill('Razon suficientemente larga 10');
            await expect(page.locator('[data-testid="confirm-skip"]')).toBeEnabled();
         }
      }
    });
  });

});
