import { test, expect } from '@playwright/test';
import { USERS } from '../fixtures/e2e-data';

test.describe('US-003: App Bootstrap Garbage Collector', () => {

  test('QA-003-02: Stale drafts older than 7 days are wiped on boot', async ({ page }) => {
    // 1. Visit home to inject localStorage
    await page.goto('/');

    // 2. Inyectar un draft viejo (10 días atrás) y uno reciente (1 día atrás)
    await page.evaluate(() => {
      const past10Days = new Date(Date.now() - 10 * 24 * 60 * 60 * 1000).toISOString();
      const past1Day = new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString();
      
      localStorage.setItem('ibpms_draft_old123', JSON.stringify({
        obs: 'Borrador Viejo',
        _timestamp: past10Days
      }));
      
      localStorage.setItem('ibpms_draft_new456', JSON.stringify({
        obs: 'Borrador Nuevo',
        _timestamp: past1Day
      }));
    });

    // 3. Login to Trigger Bootstrap sequence
    await page.goto('/login');
    await page.click('[data-testid="break-glass-toggle"]');
    await page.fill('[data-testid="email-input"]', USERS.ANALISTA_N1.email);
    await page.fill('[data-testid="password-input"]', USERS.ANALISTA_N1.password);
    await page.click('[data-testid="login-submit"]');
    
    // Wait for the app to load Workdesk
    await page.waitForURL(/workdesk/);

    // 4. Verify LocalStorage after Garbage Collector run (usually runs in App.vue or auth router guard)
    const survivedDrafts = await page.evaluate(() => {
      return {
        oldExists: !!localStorage.getItem('ibpms_draft_old123'),
        newExists: !!localStorage.getItem('ibpms_draft_new456')
      };
    });

    // Validations: El viejo debe morir, el nuevo debe vivir
    expect(survivedDrafts.oldExists).toBe(false);
    expect(survivedDrafts.newExists).toBe(true);
  });

});
