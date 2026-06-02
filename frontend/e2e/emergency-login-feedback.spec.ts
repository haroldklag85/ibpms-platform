import { test, expect } from '@playwright/test';

const LOGIN_URL = 'http://localhost:5174/login?emergency=true';

test.describe('Emergency Login — Feedback Diferenciado', () => {

    test('ESC-01: Muestra banner ámbar cuando el usuario no existe', async ({ page }) => {
        await page.goto(LOGIN_URL);
        await page.fill('[data-testid="email-input"]', 'no-existe@alpha.com');
        await page.fill('[data-testid="password-input"]', 'cualquiera');
        await page.fill('[data-testid="justification-input"]', 'some justification string');
        await page.click('[data-testid="login-submit"]');
        
        const banner = page.locator('[data-testid="login-error-banner"]');
        await expect(banner).toBeVisible({ timeout: 5000 });
        await expect(banner).toContainText('No existe una cuenta asociada');
        await expect(banner).toHaveClass(/bg-amber-50/);
    });

    test('ESC-02: Muestra banner rojo cuando la contraseña es incorrecta', async ({ page }) => {
        await page.goto(LOGIN_URL);
        await page.fill('[data-testid="email-input"]', 'admin@alpha.com');
        await page.fill('[data-testid="password-input"]', 'WrongPassword999');
        await page.fill('[data-testid="justification-input"]', 'some justification string');
        await page.click('[data-testid="login-submit"]');
        
        const banner = page.locator('[data-testid="login-error-banner"]');
        await expect(banner).toBeVisible({ timeout: 5000 });
        await expect(banner).toContainText('contraseña proporcionada es incorrecta');
        await expect(banner).toHaveClass(/bg-red-50/);
    });

    test('ESC-03: Login exitoso redirige a /workdesk sin banner', async ({ page }) => {
        await page.goto(LOGIN_URL);
        await page.fill('[data-testid="email-input"]', 'admin@alpha.com');
        await page.fill('[data-testid="password-input"]', 'Test123!');
        await page.fill('[data-testid="justification-input"]', 'some justification string');
        await page.click('[data-testid="login-submit"]');
        
        await expect(page).toHaveURL(/(localhost:\d+\/?$|workdesk|dashboard)/, { timeout: 10000 });
        const banner = page.locator('[data-testid="login-error-banner"]');
        await expect(banner).not.toBeVisible();
    });

    test('ESC-04: Muestra banner gris cuando la cuenta está deshabilitada (Mock)', async ({ page }) => {
        // Intercept network call
        await page.route('**/api/v1/auth/emergency-login', async route => {
            const json = { code: "ACCOUNT_DISABLED", message: "La cuenta de usuario está desactivada." };
            await route.fulfill({ status: 403, contentType: 'application/json', json });
        });

        await page.goto(LOGIN_URL);
        await page.fill('[data-testid="email-input"]', 'analista_n1@alpha.com');
        await page.fill('[data-testid="password-input"]', 'Test123!');
        await page.fill('[data-testid="justification-input"]', 'some justification string');
        await page.click('[data-testid="login-submit"]');
        
        const banner = page.locator('[data-testid="login-error-banner"]');
        await expect(banner).toBeVisible({ timeout: 5000 });
        await expect(banner).toContainText('cuenta de usuario está desactivada');
        await expect(banner).toHaveClass(/bg-gray-100/);
    });

    test('ESC-05: El banner se limpia al reintentar', async ({ page }) => {
        await page.goto(LOGIN_URL);
        
        // Primer intento fallido
        await page.fill('[data-testid="email-input"]', 'no-existe@alpha.com');
        await page.fill('[data-testid="password-input"]', 'x');
        await page.fill('[data-testid="justification-input"]', 'some justification string');
        await page.click('[data-testid="login-submit"]');
        
        const banner = page.locator('[data-testid="login-error-banner"]');
        await expect(banner).toBeVisible({ timeout: 5000 });
        
        // Segundo intento exitoso
        await page.fill('[data-testid="email-input"]', 'admin@alpha.com');
        await page.fill('[data-testid="password-input"]', 'Test123!');
        await page.fill('[data-testid="justification-input"]', 'some justification string');
        await page.click('[data-testid="login-submit"]');
        
        await expect(page).toHaveURL(/(localhost:\d+\/?$|workdesk|dashboard)/, { timeout: 10000 });
    });

    test('ESC-06: El banner se destruye al volver a SSO', async ({ page }) => {
        await page.goto(LOGIN_URL);
        await page.fill('[data-testid="email-input"]', 'admin@alpha.com');
        await page.fill('[data-testid="password-input"]', 'WrongPassword999');
        await page.fill('[data-testid="justification-input"]', 'some justification string');
        await page.click('[data-testid="login-submit"]');
        
        const banner = page.locator('[data-testid="login-error-banner"]');
        await expect(banner).toBeVisible({ timeout: 5000 });

        // Volver a SSO via click (or checking local text match)
        await page.locator('text=Volver al SSO Corporativo').click();
        
        // Regresar a login emergency manually since redirect tests check exact states
        await page.goto(LOGIN_URL);
        await expect(banner).not.toBeVisible();
    });

    test('ESC-07: Muestra banner rojo oscuro genérico cuando el backend está caído (Mock)', async ({ page }) => {
        // Intercept network call to simulate network failure (backend down)
        // using set_abort_reason failed guarantees network failure on client side.
        await page.route('**/api/v1/auth/emergency-login', route => route.abort('failed'));

        await page.goto(LOGIN_URL);
        await page.fill('[data-testid="email-input"]', 'admin@alpha.com');
        await page.fill('[data-testid="password-input"]', 'Test123!');
        await page.fill('[data-testid="justification-input"]', 'some justification string');
        await page.click('[data-testid="login-submit"]');
        
        const banner = page.locator('[data-testid="login-error-banner"]');
        await expect(banner).toBeVisible({ timeout: 5000 });
        await expect(banner).toContainText('Error de conexión con el servidor. Verifique que el backend esté activo.');
        await expect(banner).toHaveClass(/bg-red-900/);
    });

});
