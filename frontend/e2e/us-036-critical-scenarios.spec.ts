import { test, expect } from '@playwright/test';

/**
 * US-036 Identity Governance - Critical Scenarios Certification
 * 
 * Escenario A (M2M): Cuenta de Servicio + Secret Reveal + Destruction.
 * Escenario B (Soft-Delete): Desactivación + Bloqueo de Edición + Login Fail.
 * Escenario C (Delegación): Validación de Fechas UI.
 */

test.describe('US-036 Critical Scenarios - Identity Governance', () => {

  test.beforeEach(async ({ page }) => {
    // 1. Login as Root
    await page.goto('/login');
    await page.fill('input[type="email"]', 'root@ibpms.local');
    await page.fill('input[type="password"]', 'Root#Temp4Sys');
    await page.click('button[type="submit"]');
    
    // 2. Navigate to Identity Governance
    await page.click('[data-testid="sidebar-admin-security"]');
    await page.click('[data-testid="menu-identity-governance"]');
    await expect(page).toHaveURL(/.*identity/);
  });

  test('Escenario A: M2M - Service Account Lifecycle (CA-10)', async ({ page }) => {
    // Navigate to M2M tab
    await page.click('[data-testid="tab-m2m"]');
    
    // Create new Service Account
    await page.click('[data-testid="btn-new-m2m"]');
    await page.fill('[data-testid="input-m2m-name"]', 'SAP_CONNECTOR_E2E');
    await page.selectOption('[data-testid="select-m2m-role"]', { label: 'ROLE_SUPER_ADMIN' });
    await page.click('[data-testid="btn-generate-m2m"]');
    
    // Verify secret is hidden initially
    const secretDisplay = page.locator('[data-testid="secret-value-display"]');
    await expect(secretDisplay).toContainText('********************************');
    
    // Reveal secret
    await page.click('[data-testid="btn-reveal-secret"]');
    await expect(secretDisplay).not.toContainText('********************************');
    await expect(page.locator('text=VISIBLE')).toBeVisible();
    
    // Destroy secret view
    await page.click('[data-testid="btn-destroy-secret-view"]');
    await expect(page.locator('[data-testid="secret-value-display"]')).not.toBeVisible();
  });

  test('Escenario B: Soft-Delete - Deactivation & Access Control (CA-07)', async ({ page }) => {
    // Navigate to Users tab (default)
    await page.click('[data-testid="tab-users"]');
    
    // Find a local user to deactivate (e.g. Maria)
    const userRow = page.locator('tr').filter({ hasText: 'maria.tr@empresa.com' });
    await expect(userRow).toBeVisible();
    
    // Click Kill (which deactivates the user in this mock/impl)
    await page.on('dialog', dialog => dialog.accept());
    await userRow.locator('button:has-text("Kill")').click();
    
    // Verify label [Usuario Inactivo]
    await expect(userRow).toContainText('[Usuario Inactivo]');
    
    // Verify "Editar" button is disabled
    const editBtn = userRow.locator('[data-testid="btn-edit-user"]');
    await expect(editBtn).toBeDisabled();
    
    // Try login (should fail)
    await page.click('[data-testid="user-profile-menu"]');
    await page.click('[data-testid="logout-btn"]');
    
    await page.fill('input[type="email"]', 'maria.tr@empresa.com');
    await page.fill('input[type="password"]', 'Maria#Temp123'); // Assuming some password
    await page.click('button[type="submit"]');
    
    // Verify error message or blocked login
    await expect(page.locator('.toast-error, .error-message')).toBeVisible();
  });

  test('Escenario C: Delegation - Date Validation (CA-09)', async ({ page }) => {
    // Navigate to Delegations tab
    await page.click('[data-testid="tab-delegations"]');
    
    // Set invalid dates: End < Start
    await page.selectOption('select', { index: 1 }); // Select first active user
    await page.fill('input[type="date"] >> nth=0', '2026-12-31'); // Start
    await page.fill('input[type="date"] >> nth=1', '2026-01-01'); // End
    
    await page.click('[data-testid="btn-activate-delegation"]');
    
    // Verify error toast
    await expect(page.locator('text=La fecha de inicio no puede ser posterior a la de fin')).toBeVisible();
  });

});
