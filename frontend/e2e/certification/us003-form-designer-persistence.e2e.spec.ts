import { test, expect } from '@playwright/test';
import { USERS } from '../fixtures/e2e-data';

test.describe('US-003: FormDesigner IDE Persistence', () => {

  // Use the injected storage state but we'll override its roles below
  test.use({ storageState: 'e2e/playwright/.auth/user.json' });

  test('QA-003-01: FormDesigner can create and persist dynamic schemas', async ({ page }) => {
    // 1. Override roles in localStorage since global-setup hardcodes OPERARIO
    await page.goto('/');
    await page.evaluate(() => {
        const userStr = localStorage.getItem('ibpms_user');
        if (userStr) {
            const user = JSON.parse(userStr);
            user.roles = ['ROLE_SUPER_ADMIN', 'Global Admin'];
            localStorage.setItem('ibpms_user', JSON.stringify(user));
        }
    });

    // 2. Navigate directly to FormDesigner
    await page.goto('/admin/modeler/forms/designer');

    // 3. Select Pattern (Formulario Simple)
    await page.getByText('Formulario Simple').click();

    // 4. Verify Canvas is active
    await expect(page.getByText('IDE de Formularios Vue3/Zod')).toBeVisible();

    // 4.1. Intercept the request to ensure uniqueness on the real backend
    await page.route('**/api/v1/forms', async route => {
        const req = route.request();
        if (req.method() === 'POST') {
            const data = JSON.parse(req.postData() || '{}');
            data.technicalName = data.technicalName + '_' + Date.now();
            await route.continue({ postData: JSON.stringify(data) });
        } else {
            await route.continue();
        }
    });

    // 5. Simulate Form Submit (Save Schema to Backend)
    await page.getByRole('button', { name: '🚀 Probar (Submit)' }).click();

    // 6. Verify result modal indicates success
    await expect(page.getByText(/VALIDACION EXITOSA/)).toBeVisible({ timeout: 10000 });
    const modalText = await page.locator('.text-green-400').innerText();
    console.log('--- MODAL TEXT ---');
    console.log(modalText);
    console.log('------------------');
    await expect(page.getByText(/201 CREATED/)).toBeVisible();

    // Close Modal
    await page.getByRole('button', { name: 'Cerrar', exact: true }).click({ force: true });
  });
});
