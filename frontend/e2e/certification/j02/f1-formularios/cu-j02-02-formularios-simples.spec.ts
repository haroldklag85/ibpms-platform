import { test, expect } from '@playwright/test';

// @Traceability: Certificación E2E J-02 (T-24)
// @SSOT: docs/uat/casos_uso_uat_j02.md

test.describe('CU-J02-02: Arquitecto crea formularios Simple', () => {
  test.use({ storageState: 'e2e/playwright/.auth/user.json' });

  test('Debe crear FORM-02 (Veredicto Escalamiento) con validación Zero-Mock', async ({ page }) => {
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

    const formName = 'Veredicto Escalamiento ' + Date.now();
    await page.getByLabel('Nombre Técnico').fill(formName);
    await page.getByLabel('Título del Formulario').fill('Veredicto Escalamiento');

    // Add required fields
    const textFieldBtn = page.locator('.component-item').filter({ hasText: 'Text' }).first();
    if (await textFieldBtn.isVisible()) {
        await textFieldBtn.click(); // Add Poliza
    }

    const [response] = await Promise.all([
        page.waitForResponse(res => res.url().includes('/api/v1/forms') && res.request().method() === 'POST'),
        page.getByRole('button', { name: '🚀 Probar (Submit)' }).click()
    ]);

    expect(response.status()).toBe(201);
    await expect(page.getByText(/VALIDACION EXITOSA|201 CREATED/)).toBeVisible({ timeout: 10000 });
  });

  test('Debe crear FORM-04 (Firma Final Director) con validación Zero-Mock', async ({ page }) => {
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

    const formName = 'Firma Final Director ' + Date.now();
    await page.getByLabel('Nombre Técnico').fill(formName);
    await page.getByLabel('Título del Formulario').fill('Firma Final Director');

    // Add signature field
    const sigFieldBtn = page.locator('.component-item').filter({ hasText: 'Signature' }).first();
    if (await sigFieldBtn.isVisible()) {
        await sigFieldBtn.click(); // Add Signature
    }

    const [response] = await Promise.all([
        page.waitForResponse(res => res.url().includes('/api/v1/forms') && res.request().method() === 'POST'),
        page.getByRole('button', { name: '🚀 Probar (Submit)' }).click()
    ]);

    expect(response.status()).toBe(201);
    await expect(page.getByText(/VALIDACION EXITOSA|201 CREATED/)).toBeVisible({ timeout: 10000 });
  });
});
