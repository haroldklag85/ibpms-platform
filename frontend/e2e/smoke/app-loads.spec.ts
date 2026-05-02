import { test, expect } from '@playwright/test';

/**
 * Sprint 0 — Smoke Test Suite
 * 
 * Objetivo: Validar que la infraestructura básica está operativa.
 * Gate: Estos tests DEBEN pasar antes de avanzar al Sprint 1.
 * 
 * Verifica:
 * 1. El frontend (Vite) carga sin errors de consola críticos
 * 2. El backend (Spring Boot) responde con status UP
 * 3. La app Vue se monta correctamente (no pantalla en blanco)
 */

test.describe('Sprint 0 — Smoke Tests', () => {

    test('S0-SMOKE-01: El backend responde con health UP', async ({ request }) => {
        // Playwright request context no usa el proxy de Vite, golpear backend directo
        const response = await request.get('http://localhost:8080/actuator/health');
        expect(response.ok()).toBeTruthy();

        const body = await response.json();
        expect(body.status).toBe('UP');
    });

    test('S0-SMOKE-02: El frontend carga sin errores críticos de JavaScript', async ({ page }) => {
        const consoleErrors: string[] = [];

        // Capturar errores de consola del navegador
        page.on('console', msg => {
            if (msg.type() === 'error') {
                consoleErrors.push(msg.text());
            }
        });

        // Capturar errores no controlados del navegador
        page.on('pageerror', error => {
            consoleErrors.push(`PAGE_ERROR: ${error.message}`);
        });

        // Mock del endpoint de autenticación para evitar redirect al login
        await page.route('**/api/v1/auth/me', async route => {
            await route.fulfill({
                json: {
                    username: 'smoke_test_user',
                    roles: ['ROLE_SUPER_ADMIN'],
                    email: 'smoke@test.ibpms.local'
                }
            });
        });

        // Inyectar token JWT falso para bypass de auth guards
        await page.addInitScript(() => {
            window.localStorage.setItem('ibpms_token', 'smoke.test.jwt.token');
            window.localStorage.setItem('ibpms_user', JSON.stringify({
                username: 'smoke_test_user',
                roles: ['ROLE_SUPER_ADMIN']
            }));
            window.sessionStorage.setItem('ibpms_token', 'smoke.test.jwt.token');
        });

        // Navegar a la página principal
        await page.goto('/');

        // Esperar a que Vue monte (dar tiempo razonable)
        await page.waitForTimeout(3000);

        // Filtrar errores esperados (Camunda external task errors son normales)
        const criticalErrors = consoleErrors.filter(err =>
            !err.includes('ERR_CONNECTION_REFUSED') &&  // Backend endpoints no mockeados
            !err.includes('401') &&                      // Auth redirects esperados
            !err.includes('favicon')                     // No hay favicon
        );

        // No debe haber errores CRÍTICOS de JavaScript (crashes de Vue, import errors, etc.)
        const jsErrors = criticalErrors.filter(err =>
            err.includes('PAGE_ERROR') ||
            err.includes('SyntaxError') ||
            err.includes('TypeError') ||
            err.includes('ReferenceError') ||
            err.includes('Cannot read properties')
        );

        expect(jsErrors, `Errores JS críticos detectados: ${jsErrors.join(', ')}`).toHaveLength(0);
    });

    test('S0-SMOKE-03: La aplicación Vue se monta (no pantalla en blanco)', async ({ page }) => {
        // Mock auth
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
            window.sessionStorage.setItem('ibpms_token', 'smoke.test.jwt.token');
        });

        await page.goto('/');
        await page.waitForTimeout(3000);

        // Verificar que el DOM tiene contenido renderizado por Vue (no es solo el <div id="app">)
        const appContent = await page.locator('#app').innerHTML();
        expect(appContent.length, 'La app Vue no renderizó nada dentro de #app').toBeGreaterThan(50);

        // Capturar screenshot como evidencia del Gate
        await page.screenshot({
            path: 'test-results/smoke-s0-app-mounted.png',
            fullPage: true
        });
    });

    test('S0-SMOKE-04: El backend responde con info del servicio', async ({ request }) => {
        const response = await request.get('http://localhost:8080/actuator/info');

        // Puede retornar 200 o 404 si info no está configurado,
        // pero la conexión debe funcionar (no timeout, no connection refused)
        expect(response.status()).toBeLessThan(500);
    });

});
