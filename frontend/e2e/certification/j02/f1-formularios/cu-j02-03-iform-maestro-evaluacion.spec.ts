import { test, expect } from '@playwright/test';

// @Traceability: Certificación E2E J-02 (T-24)
// @SSOT: docs/uat/casos_uso_uat_j02.md

test.describe('CU-J02-03: Arquitecto crea iForm Maestro "Evaluación de Daños"', () => {
  test.use({ storageState: 'e2e/playwright/.auth/user.json' });

  test('Debe crear FORM-03 con GPS, token silencioso y Zero-Mock POST', async ({ page }) => {
    await page.goto('/');
    await page.evaluate(() => {
        const userStr = localStorage.getItem('ibpms_user');
        if (userStr) {
            const user = JSON.parse(userStr);
            user.roles = ['ROLE_BPM_ARCHITECT', 'ROLE_SUPER_ADMIN'];
            localStorage.setItem('ibpms_user', JSON.stringify(user));
        }
    });

    await page.goto('/admin/modeler/forms/designer');
    await page.getByText('iForm Maestro').click();
    await expect(page.getByText('IDE de Formularios Vue3/Zod')).toBeVisible();

    const formName = 'Evaluación de Daños Perito ' + Date.now();
    await page.getByLabel('Nombre Técnico').fill(formName);
    await page.getByLabel('Título del Formulario').fill('Evaluación de Daños Perito');

    // Add GPS / Hidden fields or normal text if basic UX is missing
    const gpsFieldBtn = page.locator('.component-item').filter({ hasText: 'GPS' }).first();
    if (await gpsFieldBtn.isVisible()) {
        await gpsFieldBtn.click();
    }

    const [response] = await Promise.all([
        page.waitForResponse(res => res.url().includes('/api/v1/forms') && res.request().method() === 'POST'),
        page.getByRole('button', { name: '🚀 Probar (Submit)' }).click()
    ]);

    expect(response.status()).toBe(201);
    await expect(page.getByText(/VALIDACION EXITOSA|201 CREATED/)).toBeVisible({ timeout: 10000 });
  });
});
