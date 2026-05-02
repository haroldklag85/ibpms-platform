import { Page, expect } from '@playwright/test';

/**
 * Credenciales tácticas para UAT real (Zero-Mock).
 * Fuente: Handoff Arquitecto Líder - Operación Clean Sweep.
 */
const ROOT_CREDENTIALS = {
    email: 'root@ibpms.local',
    password: 'Root#Temp4Sys',
};

/**
 * Ejecuta el flujo de login real E2E contra el backend.
 * Usa las credenciales tácticas proporcionadas por el Arquitecto.
 * Soporta el flujo de emergency-login.
 */
export async function loginE2E(page: Page, role: 'CISO' | 'BUSINESS' | 'AGENT' | 'EXECUTIVE' | 'ROOT' = 'ROOT') {
    await page.goto('/login', { waitUntil: 'networkidle', timeout: 60_000 });

    // Siempre usamos las credenciales tácticas root para UAT
    const creds = ROOT_CREDENTIALS;

    // Buscar el campo de email/usuario — puede ser input[type=email] o input[name=email]
    const emailInput = page.locator('input[type="email"], input[name="email"], input[name="username"]').first();
    await emailInput.waitFor({ state: 'visible', timeout: 30_000 });
    await emailInput.fill(creds.email);

    // Buscar el campo de contraseña
    const passwordInput = page.locator('input[type="password"]').first();
    await passwordInput.waitFor({ state: 'visible', timeout: 15_000 });
    await passwordInput.fill(creds.password);

    // Check Terms si existe
    const termsCheckbox = page.locator('input[type="checkbox"]');
    if (await termsCheckbox.isVisible({ timeout: 3_000 }).catch(() => false)) {
        await termsCheckbox.check();
    }

    // Submit
    const submitButton = page.locator('button[type="submit"]').first();
    await submitButton.click();

    // Esperar navegación post-login (puede ser /portal, / o /workdesk)
    await page.waitForURL(/\/(portal|workdesk|$)/, { timeout: 60_000 });
}

/**
 * Login usando mocks (SOLO para US que NO estén en la lista de certificadas).
 */
export async function loginMocked(page: Page) {
    await page.route('**/api/v1/auth/me', async route => {
        await route.fulfill({
            json: {
                username: 'smoke_test_user',
                roles: ['ROLE_SUPER_ADMIN'],
                email: 'smoke@test.ibpms.local'
            }
        });
    });

    await page.addInitScript(() => {
        window.localStorage.setItem('ibpms_token', 'smoke.test.jwt.token');
        window.localStorage.setItem('ibpms_user', JSON.stringify({
            username: 'smoke_test_user',
            roles: ['ROLE_SUPER_ADMIN']
        }));
    });
}
