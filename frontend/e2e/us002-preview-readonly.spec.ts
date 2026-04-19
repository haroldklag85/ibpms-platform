import { test, expect } from '@playwright/test';

test.describe('US-002 CA-5: Preview de Tarea Read-Only', () => {
  test('Abre modal con detalle de solo lectura sin poder editar', async ({ page }) => {
    await page.route('**/api/v1/workdesk/tasks/t-ro-1/preview', route => {
        route.fulfill({
            status: 200,
            body: JSON.stringify({
                unifiedId: 't-ro-1',
                title: 'Lectura Segura',
                description: 'Instrucciones vitales',
                slaExpirationDate: new Date().toISOString()
            })
        });
    });

    await page.goto('/workdesk?previewTask=t-ro-1');

    const modal = page.locator('.modal-content');
    await modal.waitFor({ state: 'visible' });

    await expect(modal.getByText('Lectura Segura')).toBeVisible();
    await expect(modal.getByText('Instrucciones vitales')).toBeVisible();

    // No debe haber inputs editables para metadata de la tarea (el preview es inmutable)
    const formInputs = modal.locator('input:not([readonly]), textarea:not([readonly]), select');
    await expect(formInputs).toHaveCount(0);
  });
});
