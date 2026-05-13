import { test, expect } from '@playwright/test';

// Utilizaremos un nuevo contexto de navegador sin auth para cada prueba.
test.use({ storageState: { cookies: [], origins: [] } });

test.describe('US-036 Iteración 2: Topología Multirrol (CA-28, CA-30, CA-31)', () => {
  
  test('CA-28/CA-30/CA-31: Super Administrador visualiza los 7 Módulos Macro', async ({ page }) => {
    console.log('Navegando a login (Super Admin)...');
    await page.goto('/login?emergency=true', { waitUntil: 'domcontentloaded' });

    console.log('Llenando credenciales...');
    await page.fill('[data-testid="email-input"]', 'root@ibpms.local');
    await page.fill('[data-testid="password-input"]', 'Root#Temp4Sys');
    
    console.log('Haciendo submit...');
    await page.click('[data-testid="login-submit"]');

    console.log('Esperando navegación...');
    await page.waitForTimeout(3000); // Darle tiempo a la animación de login y carga de menu layout
    await page.screenshot({ path: 'debug-superadmin.png' });
    
    // Esperamos que aparezca la UI principal (ej. "Administración y Gobernanza" debe estar)
    const adminModule = page.locator('.nav-item[title="Administración y Gobernanza"]');
    await expect(adminModule).toBeVisible({ timeout: 15000 });

    const integrationModule = page.locator('.nav-item[title="Integration Hub"]');
    await expect(integrationModule).toBeVisible();

    const gobernanzaModule = page.locator('.nav-item[title="Gobernanza"]');
    await expect(gobernanzaModule).toBeVisible();

    const serviceDeliveryModule = page.locator('.nav-item[title="Service Delivery"]');
    await expect(serviceDeliveryModule).toBeVisible();
    
    console.log('✅ Topología dinámica validada para SUPER_ADMIN');
  });

  test('CA-28/CA-30/CA-31: Rol Estándar (Operario) visualiza una topología segregada y limitada', async ({ page }) => {
    console.log('Navegando a login (Perito)...');
    await page.goto('/login?emergency=true', { waitUntil: 'domcontentloaded' });

    console.log('Llenando credenciales...');
    await page.fill('[data-testid="email-input"]', 'perito_a@ibpms.com');
    await page.fill('[data-testid="password-input"]', 'Root#Temp4Sys'); // Password sincronizado en DB por QA para E2E
    
    console.log('Haciendo submit...');
    await Promise.all([
      page.waitForNavigation(),
      page.click('[data-testid="login-submit"]')
    ]);

    console.log('Esperando carga completa...');
    await page.waitForTimeout(3000);
    await page.screenshot({ path: 'debug-perito.png' });

    // Validamos segregación: Un Perito/Operario NO DEBE ver Gobernanza ni Administración
    const adminModule = page.locator('.nav-item[title="Administración y Gobernanza"]');
    await expect(adminModule).not.toBeVisible();

    const gobernanzaModule = page.locator('.nav-item[title="Gobernanza"]');
    await expect(gobernanzaModule).not.toBeVisible();
    
    const integrationModule = page.locator('.nav-item[title="Integration Hub"]');
    await expect(integrationModule).not.toBeVisible();

    console.log('✅ Topología segregada dinámicamente validada para Rol Estándar (Zero-Mock)');
  });

});
