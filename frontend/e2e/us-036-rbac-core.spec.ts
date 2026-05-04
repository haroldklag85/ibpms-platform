import { test, expect } from '@playwright/test';

test.describe('US-036 Identity Governance - RBAC Core (CA-01 to CA-06)', () => {
  
  test.beforeEach(async ({ page }) => {
    // Login and navigate to RBAC Manager (Screen 14)
    await page.goto('/login');
    await page.fill('[data-testid="input-email"]', 'root@ibpms.local');
    await page.fill('[data-testid="input-password"]', 'ibpms2026');
    await page.click('[data-testid="btn-login-submit"]');
    await page.goto('/admin/security/rbac'); // Suponiendo esta ruta
  });

  test('CA-01: Import Role from EntraID', async ({ page }) => {
    await page.click('[data-testid="btn-open-import-entraid"]');
    await expect(page.locator('[data-testid="modal-import-entraid"]')).toBeVisible();
    
    // Click import on the first available group
    const firstGroupBtn = page.locator('[data-testid="modal-import-entraid"] button').first();
    await firstGroupBtn.click();
    
    // Check if the role appears in the global list
    // (This requires the mock/real backend to return the imported role in next fetch)
    await expect(page.locator('table')).toContainText('GG_IBPMS');
  });

  test('CA-02: Protect ROOT Role from deletion', async ({ page }) => {
    // Locate the ROOT role row
    const rootRow = page.locator('tr', { hasText: 'ROLE_SUPER_ADMIN' });
    await expect(rootRow).toContainText('🔒 ROOT');
    
    // Verify delete button is NOT present for this row
    const deleteBtn = rootRow.locator('[data-testid="btn-delete-role"]');
    await expect(deleteBtn).not.toBeVisible();
    
    await expect(rootRow).toContainText('— protegido —');
  });

  test('CA-04: Granular Matrix - Toggle Initiator/Executor', async ({ page }) => {
    // Switch to Process Roles tab
    await page.click('text=Roles de Proceso');
    
    const matrixCheckbox = page.locator('input[type="checkbox"]').first();
    const isChecked = await matrixCheckbox.isChecked();
    
    await matrixCheckbox.click();
    await expect(matrixCheckbox).toBeChecked({ checked: !isChecked });
    
    // Verify it persists after refresh (optional but recommended)
    await page.reload();
    await page.click('text=Roles de Proceso');
    await expect(matrixCheckbox).toBeChecked({ checked: !isChecked });
  });

  test('CA-06: Hierarchical Role Inheritance', async ({ page }) => {
    await page.click('[data-testid="btn-open-create-role"]');
    await page.fill('[data-testid="input-role-name"]', 'ROLE_JUNIOR_TESTER');
    
    // Select a parent role
    await page.selectOption('[data-testid="select-parent-role"]', { label: 'ROLE_SUPER_ADMIN' });
    
    await page.click('[data-testid="btn-confirm-create-role"]');
    
    // Verify creation
    await expect(page.locator('table')).toContainText('ROLE_JUNIOR_TESTER');
  });
});
