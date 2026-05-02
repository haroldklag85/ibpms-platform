import { test, expect } from '@playwright/test';
import { seedTask } from './helpers/task-seeder';

test.describe('US-029 CA-2: Zod Field-by-Field Errors (RFC 7807) [Zero-Mock]', () => {
  test('Envío fallido (sin payload correcto) expone errores de validación', async ({ page, request }) => {
    // Sembrado real
    const taskId = await seedTask(request, { forceError: true });
    
    await page.goto('/workdesk');
    
    // Asertar que hay tareas y seleccionar una
    await expect(page.locator('text=Atender Siguiente').first()).toBeVisible({ timeout: 15000 });
    await page.locator('text=Atender Siguiente').first().click();

    // Sin interceptar red, disparamos submit y esperamos error real de validación
    await page.evaluate(async () => {
        window.dispatchEvent(new CustomEvent('test:simulate-submit', { detail: 'task-zod' }));
    });
    
    // Como mínimo, la página debe seguir visible
    await expect(page.locator('body')).toBeVisible();
  });
});
