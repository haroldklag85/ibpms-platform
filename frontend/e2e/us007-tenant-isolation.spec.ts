import { test, expect } from '@playwright/test';

test.describe('US-007 CA-6: Tenant Isolation (IDOR 403)', () => {
  test('Acceso denegado a recurso de otro tenant intercepta 403 y muestra feedback de seguridad', async ({ page }) => {
    // Interceptamos una petición al backend que devuelve 403 por IDOR
    await page.route('**/api/v1/workdesk/tasks/task-other-tenant/preview', route => {
        route.fulfill({
            status: 403,
            body: JSON.stringify({
                status: 403,
                error: 'Forbidden',
                message: 'No tienes permiso para acceder a este recurso (Tenant no coincide)',
                path: '/api/v1/workdesk/tasks/task-other-tenant/preview'
            })
        });
    });

    await page.goto('/workdesk?previewTask=task-other-tenant');

    // Debemos esperar que el frontend ataje el 403. Generalmente puede mostrar un flag de error,
    // o un toast. El test falla si arroja uncrash fatal. Asumimos un Toast o error text
    const errorMsg = page.locator('text=No tienes permiso|preview fallido|Preview Fallido', { ignoreCase: true }).first();
    await errorMsg.waitFor({ state: 'visible', timeout: 5000 }).catch(() => null);

    // Como TaskPreviewModal setea "Preview Fallido" en el catch:
    await expect(page.getByText('Preview Fallido')).toBeVisible();
    await expect(page.getByText('No se pudo cargar la vista')).toBeVisible();
  });
});
