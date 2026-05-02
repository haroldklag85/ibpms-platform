import { test, expect } from '@playwright/test';

/**
 * Emergency Login — Feedback Diferenciado
 * Tests adaptados para UAT real con credenciales tácticas.
 * Puerto dinámico: usa baseURL de playwright.config.ts
 */

const LOGIN_PATH = '/login?emergency=true';

test.describe('Emergency Login — Feedback Diferenciado', () => {

    test('ESC-01: Muestra banner ámbar cuando el usuario no existe', async ({ page }) => {
        await page.goto(LOGIN_PATH);
        await page.fill('[data-testid="email-input"]', 'no-existe@alpha.com');
        await page.fill('[data-testid="password-input"]', 'cualquiera');
        await page.click('[data-testid="login-submit"]');
        
        const banner = page.locator('[data-testid="login-error-banner"]');
        await expect(banner).toBeVisible({ timeout: 30_000 });
        await expect(banner).toContainText('No existe una cuenta asociada');
    });

    test('ESC-02: Muestra banner rojo cuando la contraseña es incorrecta', async ({ page }) => {
        await page.goto(LOGIN_PATH);
        // Usar credencial REAL que existe en la BD, pero con contraseña INCORRECTA
        await page.fill('[data-testid="email-input"]', 'root@ibpms.local');
        await page.fill('[data-testid="password-input"]', 'WrongPassword999');
        await page.click('[data-testid="login-submit"]');
        
        const banner = page.locator('[data-testid="login-error-banner"]');
        await expect(banner).toBeVisible({ timeout: 30_000 });
        await expect(banner).toContainText('contraseña proporcionada es incorrecta');
    });

    test('ESC-03: Login exitoso redirige a /workdesk sin banner', async ({ page }) => {
        await page.goto(LOGIN_PATH);
        // Usar credenciales tácticas reales
        await page.fill('[data-testid="email-input"]', 'root@ibpms.local');
        await page.fill('[data-testid="password-input"]', 'Root#Temp4Sys');
        await page.click('[data-testid="login-submit"]');
        
        await expect(page).toHaveURL(/\/(workdesk|portal|dashboard|\s*)$/, { timeout: 30_000 });
        const banner = page.locator('[data-testid="login-error-banner"]');
        await expect(banner).not.toBeVisible();
    });

    test('ESC-04: Muestra banner gris cuando la cuenta está deshabilitada (Mock)', async ({ page }) => {
        // Intercept network call — este mock es LEGÍTIMO (no es US certificada)
        await page.route('**/auth/emergency-login', async route => {
            const json = { code: "ACCOUNT_DISABLED", message: "La cuenta de usuario está desactivada." };
            await route.fulfill({ status: 403, contentType: 'application/json', json });
        });

        await page.goto(LOGIN_PATH);
        await page.fill('[data-testid="email-input"]', 'analista_n1@alpha.com');
        await page.fill('[data-testid="password-input"]', 'Test123!');
        await page.click('[data-testid="login-submit"]');
        
        const banner = page.locator('[data-testid="login-error-banner"]');
        await expect(banner).toBeVisible({ timeout: 15_000 });
        await expect(banner).toContainText('cuenta de usuario está desactivada');
    });

    test('ESC-05: El banner se limpia al reintentar', async ({ page }) => {
        await page.goto(LOGIN_PATH);
        
        // Primer intento fallido (usuario inexistente)
        await page.fill('[data-testid="email-input"]', 'no-existe@alpha.com');
        await page.fill('[data-testid="password-input"]', 'x');
        await page.click('[data-testid="login-submit"]');
        
        const banner = page.locator('[data-testid="login-error-banner"]');
        await expect(banner).toBeVisible({ timeout: 30_000 });
        
        // Segundo intento exitoso (credenciales tácticas reales)
        await page.fill('[data-testid="email-input"]', 'root@ibpms.local');
        await page.fill('[data-testid="password-input"]', 'Root#Temp4Sys');
        await page.click('[data-testid="login-submit"]');
        
        await expect(page).toHaveURL(/\/(workdesk|portal|dashboard|\s*)$/, { timeout: 30_000 });
    });

    test('ESC-06: El banner se destruye al volver a SSO', async ({ page }) => {
        await page.goto(LOGIN_PATH);
        await page.fill('[data-testid="email-input"]', 'root@ibpms.local');
        await page.fill('[data-testid="password-input"]', 'WrongPassword999');
        await page.click('[data-testid="login-submit"]');
        
        const banner = page.locator('[data-testid="login-error-banner"]');
        await expect(banner).toBeVisible({ timeout: 30_000 });

        // Volver a SSO
        await page.locator('text=Volver al SSO Corporativo').click();
        
        // Regresar a login emergency
        await page.goto(LOGIN_PATH);
        await expect(banner).not.toBeVisible();
    });

    test('ESC-07: Muestra banner rojo oscuro genérico cuando el backend está caído (Mock)', async ({ page }) => {
        // Mock legítimo — simular caída de red
        await page.route('**/auth/emergency-login', route => route.abort('failed'));

        await page.goto(LOGIN_PATH);
        await page.fill('[data-testid="email-input"]', 'root@ibpms.local');
        await page.fill('[data-testid="password-input"]', 'Root#Temp4Sys');
        await page.click('[data-testid="login-submit"]');
        
        const banner = page.locator('[data-testid="login-error-banner"]');
        await expect(banner).toBeVisible({ timeout: 15_000 });
        await expect(banner).toContainText('Error de conexión con el servidor. Verifique que el backend esté activo.');
    });

});
