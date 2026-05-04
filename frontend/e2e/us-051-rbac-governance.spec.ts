import { test, expect } from '@playwright/test';

/**
 * US-051: Matriz de Gobernanza Visual y Enrutamiento RBAC (Frontend)
 * Validación E2E de Seguridad "Zero-Trust" y Composición Dinámica.
 */

const CREDENTIALS = {
  email: 'root@ibpms.local',
  password: 'Root#Temp4Sys'
};

const GLOBAL_TIMEOUT = 60000; // 60s por la lentitud del backend en Docker

async function login(page) {
  // Navegar con espera de red para asegurar que el bundle se cargue
  await page.goto('/login', { waitUntil: 'networkidle', timeout: GLOBAL_TIMEOUT });
  
  // Activar modo Break-Glass
  const breakGlassBtn = page.locator('[data-testid="break-glass-toggle"]');
  await breakGlassBtn.scrollIntoViewIfNeeded();
  await breakGlassBtn.click();

  // Esperar a que el formulario Break-Glass se despliegue (animación)
  const emailInput = page.locator('[data-testid="email-input"]');
  await expect(emailInput).toBeVisible({ timeout: GLOBAL_TIMEOUT });
  
  await emailInput.fill(CREDENTIALS.email);
  await page.fill('[data-testid="password-input"]', CREDENTIALS.password);
  
  // Click en login y esperar navegación
  await Promise.all([
    page.waitForURL(url => url.pathname.includes('/workdesk'), { timeout: GLOBAL_TIMEOUT }),
    page.click('[data-testid="login-submit"]')
  ]);
  
  // Asegurar que el loader desaparezca
  await page.waitForSelector('.animate-spin', { state: 'hidden', timeout: GLOBAL_TIMEOUT });
  
  // Esperar un momento para la hidratación de Pinia (800ms en authStore.ts)
  await page.waitForTimeout(1500);
}

test.describe('US-051: RBAC Governance & Visual Security', () => {

  test.beforeEach(async ({ page }) => {
    // Aumentar timeout para el inicio del backend
    test.setTimeout(120000);

    // Interceptar /login para devolver un JWT controlado (CA-51-FIX)
    await page.route('**/api/v1/auth/login', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          token: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJyb290QGlicG1zLmxvY2FsIiwicm9sZXMiOlsiaWJwbXNfcm9sX1JPTEVfU1VQRVJfQURNSU4iLCJpYnBtc19yb2xfR2xvYmFsIEFkbWluIl0sImlhdCI6MTUxNjIzOTAyMn0.dummy-signature"
        })
      });
    });

    // Interceptar /me para asegurar roles de super admin por defecto (CA-51-FIX)
    await page.route('**/api/v1/users/me', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          username: 'root@ibpms.local',
          email: 'root@ibpms.local',
          roles: ['ROLE_SUPER_ADMIN', 'Global Admin', 'SUPER_ADMIN', 'ROLE_ADMIN', 'ADMIN'],
          permissions: ['READ', 'WRITE', 'DELETE', 'ADMIN']
        })
      });
    });
  });

  test('CA-06: Dynamic Menu Composition - Should hide empty parent nodes', async ({ page }) => {
    // Interceptamos el layout del menú para simular una estructura con una carpeta vacía
    await page.route('**/api/v1/users/me/menu-layout', async (route) => {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          { 
            title: 'Módulo Visible', 
            icon: 'pi pi-check', 
            children: [{ title: 'Subitem', path: '/workdesk', icon: 'pi pi-circle' }] 
          },
          { 
            title: 'Carpeta Fantasma', 
            icon: 'pi pi-ghost', 
            children: [] // Vacío, no debería renderizarse el padre
          }
        ])
      });
    });

    await login(page);

    // Expandir Sidebar para ver los textos (MainLayout.vue por defecto inicia colapsado)
    const sidebarToggle = page.locator('aside button').first();
    await sidebarToggle.click();
    await page.waitForTimeout(1000); // Esperar a que termine la animación de expansión

    // Verificar que "Módulo Visible" existe
    await expect(page.locator('aside').getByText('Módulo Visible')).toBeVisible({ timeout: GLOBAL_TIMEOUT});
    
    // Verificar que "Carpeta Fantasma" NO existe en el DOM
    await expect(page.locator('text=Carpeta Fantasma')).toBeHidden();
  });

  test('CA-07: Dynamic Dashboard - Should inject widgets based on role', async ({ page }) => {
    await login(page);

    // El widget puede tardar en aparecer por el Suspense y la hidratación
    const widgetHeader = page.locator('span:has-text("Módulo Aditivo")');
    await expect(widgetHeader).toBeVisible({ timeout: GLOBAL_TIMEOUT});
  });

  test('CA-08: Read-Only Security - Should remove write buttons from DOM', async ({ page }) => {
    // Forzamos un estado de "Solo Lectura" inyectando un rol restrictivo en la respuesta de /me
    await page.route('**/api/v1/users/me', async (route) => {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          username: 'auditor@ibpms.local',
          roles: ['ROLE_AUDITOR_READONLY'],
          permissions: ['READ']
        })
      });
    });

    await login(page);

    // Ir a la pantalla de gobernanza (PATH CORREGIDO)
    await page.goto('/admin/security/identity');
    await page.waitForLoadState('networkidle');

    // Verificar que los botones de escritura NO EXISTEN en el DOM (v-if)
    const newUserBtn = page.locator('button:has-text("Nuevo Usuario")');
    const killAllBtn = page.locator('button:has-text("Revocar Todo")');
    
    await expect(newUserBtn).toBeHidden();
    await expect(killAllBtn).toBeHidden();
  });

  test('CA-09: Sudo-Mode - Should intercept destructive actions', async ({ page }) => {
    await login(page);
    await page.goto('/admin/security/identity', { waitUntil: 'networkidle' });
    
    // Interceptamos la llamada de kill-session
    let requestSent = false;
    await page.route('**/api/v1/kill-session', (route) => {
      requestSent = true;
      route.fulfill({ status: 200, body: 'ok' });
    });

    // Confirmar diálogo nativo ANTES de dispararlo
    page.on('dialog', async dialog => {
      await dialog.accept();
    });

    // Intentar acción destructiva
    const killAllBtn = page.locator('button:has-text("Revocar Todo")');
    await expect(killAllBtn).toBeVisible({ timeout: GLOBAL_TIMEOUT });
    await killAllBtn.click();

    // Verificar que el Modal de Sudo aparece
    await expect(page.locator('text=Aprobación Sudo')).toBeVisible({ timeout: GLOBAL_TIMEOUT });
    
    // Verificar que la petición NO se ha enviado aún
    expect(requestSent).toBe(false);

    // Ingresar contraseña incorrecta
    await page.fill('input[type="password"]', 'WrongPass123!');
    await page.press('input[type="password"]', 'Enter');
    await expect(page.locator('text=Contraseña incorrecta')).toBeVisible();

    // Ingresar contraseña correcta
    await page.fill('input[type="password"]', CREDENTIALS.password);
    await page.click('button:has-text("Confirmar Acción")');

    // Verificar que el modal se cierra y la petición se envía
    await expect(page.locator('text=Aprobación Sudo')).toBeHidden({ timeout: GLOBAL_TIMEOUT});
    await expect.poll(() => requestSent, { timeout: GLOBAL_TIMEOUT }).toBe(true);
  });

  test('CA-10: Secrets Audit - Should emit telemetry before revealing secret', async ({ page }) => {
    await login(page);
    await page.goto('/admin/security/identity', { waitUntil: 'networkidle' });
    
    // Esperar a que los tabs estén listos
    const apiKeyTab = page.locator('button:has-text("Cuentas de Servicio")');
    await expect(apiKeyTab).toBeVisible({ timeout: GLOBAL_TIMEOUT });
    await apiKeyTab.click();

    // Interceptamos la telemetría de auditoría
    let telemetrySent = false;
    await page.route('**/api/v1/admin/security/audit/telemetry', (route) => {
      telemetrySent = true;
      route.fulfill({ status: 200, body: 'ok' });
    });

    // Simular prompt ANTES de dispararlo
    page.on('dialog', async dialog => {
      if (dialog.type() === 'prompt') {
        await dialog.accept('Test App');
      } else {
        await dialog.accept();
      }
    });

    // Buscar una API Key y presionar Generar
    const generateBtn = page.locator('button:has-text("Generar Nueva API Key")');
    await expect(generateBtn).toBeVisible({ timeout: GLOBAL_TIMEOUT });
    await generateBtn.click();
    
    // Esperar a que aparezca el secreto ofuscado
    const secretInput = page.locator('input[value="********************************"]');
    await expect(secretInput).toBeVisible({ timeout: GLOBAL_TIMEOUT });

    // Presionar Mostrar
    const revealBtn = page.locator('button:has-text("Mostrar")');
    await revealBtn.click();

    // Verificar que la telemetría se envió ANTES o DURANTE la revelación
    await expect.poll(() => telemetrySent, { timeout: GLOBAL_TIMEOUT }).toBe(true);
    
    // Verificar que el secreto ya no está ofuscado
    await expect(page.locator('input[value*="sk_live_"]')).toBeVisible({ timeout: GLOBAL_TIMEOUT });
  });

});
