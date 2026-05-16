// @Traceability: US-017, CA-22, CA-25
import { test, expect } from '@playwright/test';

test.describe('US-017: CQRS Offline Toast', () => {
  test.use({ storageState: 'e2e/playwright/.auth/analista_n1.json' });

  test('CU-02: Offline Toast appears when context goes offline', async ({ context, page }) => {
    await page.goto('/workdesk');
    
    // Simular red desconectada
    await context.setOffline(true);
    
    // Validar Toast inferior izquierdo
    const toast = page.locator('text=Trabajando sin conexión');
    await expect(toast).toBeVisible({ timeout: 10000 });
    
    // Restablecer red
    await context.setOffline(false);
    
    // Validar que el toast desaparece o se sincroniza
    await expect(toast).toBeHidden({ timeout: 10000 });
  });
});
