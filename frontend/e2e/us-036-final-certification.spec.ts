import { test, expect } from '@playwright/test';

/**
 * US-036 Final Certification - Phase CA-12 to CA-16
 * 
 * Escenario 1: Kill-Session (CA-14)
 * Escenario 2: Public Process (CA-15)
 * Escenario 3: CISO Report (CA-16)
 */

test.describe('US-036 Final Certification - Identity Governance', () => {

  test('CA-14: El Exorcismo Táctico (Kill-Session)', async ({ browser }) => {
    // 1. Setup User Context (Target)
    const userContext = await browser.newContext();
    const userPage = await userContext.newPage();
    await userPage.goto('/login');
    await userPage.fill('input[type="email"]', 'maria.tr@empresa.com');
    await userPage.fill('input[type="password"]', 'Maria#Temp123');
    await userPage.click('button[type="submit"]');
    // Wait for workdesk or dashboard
    await userPage.waitForURL(url => url.pathname.includes('workdesk') || url.pathname.includes('dashboard'));

    // 2. Setup Admin Context (Executioner)
    const adminContext = await browser.newContext();
    const adminPage = await adminContext.newPage();
    await adminPage.goto('/login');
    await adminPage.fill('input[type="email"]', 'root@ibpms.local');
    await adminPage.fill('input[type="password"]', 'Root#Temp4Sys');
    await adminPage.click('button[type="submit"]');
    
    // Navigate to Identity Governance
    await adminPage.click('[data-testid="sidebar-admin-security"]');
    await adminPage.click('[data-testid="menu-identity-governance"]');
    
    // Find Maria and Kill Session
    const userRow = adminPage.locator('tr').filter({ hasText: 'maria.tr@empresa.com' });
    adminPage.on('dialog', dialog => dialog.accept());
    await userRow.locator('[data-testid="btn-kill-session"]').click();
    
    // Wait for toast confirmation
    await expect(adminPage.locator('text=Sesión de maria.tr@empresa.com terminada')).toBeVisible();
    
    // 3. Verify User Expulsion
    // User tries to navigate or perform action
    await userPage.reload();
    // Should be redirected to login because the token is now blacklisted/invalid
    await userPage.waitForURL(/.*login/, { timeout: 10000 });
    await expect(userPage).toHaveURL(/.*login/);
  });

  test('CA-15: Acceso Ciudadano Anónimo (Public URL)', async ({ browser }) => {
    const adminContext = await browser.newContext();
    const adminPage = await adminContext.newPage();
    // Login as Admin
    await adminPage.goto('/login');
    await adminPage.fill('input[type="email"]', 'root@ibpms.local');
    await adminPage.fill('input[type="password"]', 'Root#Temp4Sys');
    await adminPage.click('button[type="submit"]');
    
    // Go to Identity Governance -> Processes
    await adminPage.click('[data-testid="sidebar-admin-security"]');
    await adminPage.click('[data-testid="menu-identity-governance"]');
    await adminPage.click('[data-testid="tab-processes"]');
    
    // Enable Public for the first process
    const processRow = adminPage.locator('.grid > div').first();
    const toggle = processRow.locator('[data-testid="toggle-public-process"]');
    await toggle.check();
    
    const processId = await processRow.locator('.font-mono').textContent();
    const publicUrl = `/public/process/${processId?.trim()}`;
    
    // 4. Verify Anonymous Access
    const anonymousContext = await browser.newContext();
    const anonPage = await anonymousContext.newPage();
    await anonPage.goto(publicUrl);
    
    // Form should load (assuming some content exists or at least no redirect to login)
    await expect(anonPage).not.toHaveURL(/.*login/);
    
    // Disable Public
    await toggle.uncheck();
    
    // Verify 404 (Falso 404) or redirect to login for private process
    await anonPage.reload();
    // The requirement says "retornar 404 (Falso 404 / CA-03 de US-051)"
    await expect(anonPage.locator('text=404')).toBeVisible();
  });

  test('CA-16: Integridad del Reporte CISO (ISO 27001)', async ({ page }) => {
    // Login as Admin
    await page.goto('/login');
    await page.fill('input[type="email"]', 'root@ibpms.local');
    await page.fill('input[type="password"]', 'Root#Temp4Sys');
    await page.click('button[type="submit"]');
    
    // Go to Identity Governance -> CISO Reports
    await page.click('[data-testid="sidebar-admin-security"]');
    await page.click('[data-testid="menu-identity-governance"]');
    await page.click('[data-testid="tab-ciso_reports"]');
    
    // Generate Report
    await page.click('[data-testid="btn-generate-ciso-report"]');
    await expect(page.locator('text=Reporte ISO 27001 generado')).toBeVisible();
    
    // Wait for download of the newly generated or existing report
    const [ download ] = await Promise.all([
      page.waitForEvent('download'),
      page.click('[data-testid="btn-download-report"]')
    ]);
    
    // Basic filename validation
    // The code shows `showToast(`Iniciando descarga de reporte firmado: ${report.fileHash}`, 'success');`
    // but the test should check if a file is actually received.
    expect(download.suggestedFilename()).toBeTruthy();
  });

});
