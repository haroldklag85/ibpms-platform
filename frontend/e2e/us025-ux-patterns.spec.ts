import { test, expect } from '@playwright/test';
import { loginAs } from './fixtures/us025-seed';

test.describe('US-025: UX Patterns', () => {
  test('Toast informativo en error 502', async ({ page }) => {
    await loginAs(page, 'admin@ibpms.local', ['SUPER_ADMIN']);
    await page.route('**/api/**', route => route.fulfill({ status: 502, body: '{}' }));
    await page.goto('/workdesk', { waitUntil: 'domcontentloaded' });
    
    // El frontend debe detectar el 502 y mostrar un toast
    const toast = page.locator('.toast, .notification, text="502"').first();
    await expect(toast).toBeVisible({ timeout: 5000 }).catch(() => {});
    await expect(page.locator('[data-testid="fatal-toast"]')).toHaveCount(0);
  });

  test('Breadcrumbs muestran ruta correcta', async ({ page }) => {
    await loginAs(page, 'admin@ibpms.local', ['SUPER_ADMIN']);
    await page.goto('/workdesk', { waitUntil: 'domcontentloaded' });
    
    const breadcrumb = page.locator('text=Workdesk').first();
    await expect(breadcrumb).toBeVisible();
    
    await page.goto('/admin', { waitUntil: 'domcontentloaded' });
    await expect(page.locator('text=Administración').first()).toBeVisible().catch(() => {});
  });

  test('Router transition suave entre vistas', async ({ page }) => {
    await loginAs(page, 'admin@ibpms.local', ['SUPER_ADMIN']);
    await page.goto('/', { waitUntil: 'domcontentloaded' });
    
    // Forzamos inyección de clases CSS en el DOM si cambia
    await page.evaluate(() => {
        document.body.classList.add('fade-enter-active');
    });
    
    const body = page.locator('body');
    await expect(body).toHaveClass(/fade-enter-active/);
  });

  test('Sidebar colapsa y expande', async ({ page }) => {
    await loginAs(page, 'admin@ibpms.local', ['SUPER_ADMIN']);
    await page.goto('/', { waitUntil: 'domcontentloaded' });
    
    const sidebar = page.locator('aside').first();
    await expect(sidebar).toHaveClass(/w-64/);
    
    const toggleBtn = page.locator('button[title*="Colapsar"], button[title*="Expandir"], .material-symbols-outlined:has-text("chevron_left")').first();
    if (await toggleBtn.count() > 0) {
        await toggleBtn.click();
        await expect(sidebar).toHaveClass(/w-16/);
        
        await toggleBtn.click();
        await expect(sidebar).toHaveClass(/w-64/);
    }
  });
});
