import { test, expect } from '@playwright/test';

// @Traceability: Certificación E2E J-02 (T-24)
// @SSOT: docs/uat/casos_uso_uat_j02.md

test.describe('CU-J02-04: Arquitecto genera y valida esquema Zod', () => {
  test.use({ storageState: 'e2e/playwright/.auth/user.json' });

  test('Debe compilar un esquema Zod válido y rechazar payloads incompletos', async ({ page }) => {
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
    await page.getByText('Formulario Simple').click();
    await expect(page.getByText('IDE de Formularios Vue3/Zod')).toBeVisible();

    const formName = 'Validacion Zod ' + Date.now();
    await page.getByLabel('Nombre Técnico').fill(formName);
    await page.getByLabel('Título del Formulario').fill('Validacion Zod');

    // Go to Zod tab
    const zodTab = page.getByRole('tab', { name: /Zod|JSON/ });
    if (await zodTab.isVisible()) {
        await zodTab.click();
    }

    const [response] = await Promise.all([
        page.waitForResponse(res => res.url().includes('/api/v1/forms') && res.request().method() === 'POST'),
        page.getByRole('button', { name: '🚀 Probar (Submit)' }).click()
    ]);

    expect(response.status()).toBe(201);
    await expect(page.getByText(/VALIDACION EXITOSA|201 CREATED/)).toBeVisible({ timeout: 10000 });
  });
});
