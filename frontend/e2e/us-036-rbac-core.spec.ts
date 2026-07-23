import { test, expect } from '@playwright/test';

test.describe('US-036 Identity Governance - RBAC Core (CA-01 to CA-06)', () => {
  
  test.beforeEach(async ({ page }) => {
    // Login and navigate to RBAC Manager (Screen 14)
    await page.goto('/login');
    
    // Switch to local login mode
    await page.click('[data-testid="break-glass-toggle"]');
    
    await page.fill('[data-testid="email-input"]', 'root@ibpms.local');
    await page.fill('[data-testid="password-input"]', 'Root#Temp4Sys'); 
    await page.click('[data-testid="login-submit"]');
    
    // Wait for redirect (some users land on workdesk, others on dashboard)
    await page.waitForURL('**/{admin/dashboard,workdesk}');
    await page.goto('/admin/security/identity');
    
    // Wait for component to load
    await expect(page.locator('h1')).toContainText('Identity & Access Governance');
  });

  test('CA-01: Import Role from EntraID', async ({ page }) => {
    // Navigate to Roles tab
    await page.click('[data-testid="tab-roles"]');
    
    await page.click('[data-testid="btn-import-entraid"]');
    
    // Wait for EntraID list to be visible (groups might be loading)
    const firstGroup = page.locator('[data-testid="entraid-group-item"]').first();
    await expect(firstGroup).toBeVisible({ timeout: 10000 });
    
    const groupName = await firstGroup.locator('p.font-bold').textContent();
    
    // Click import on the first available group
    await firstGroup.locator('[data-testid="btn-import-group"]').click();
    
    // Check if the role appears in the global list
    // The importSingleGroup function in the UI pushes to systemRoles
    await expect(page.locator('table')).toContainText(groupName || 'Azure AD');
  });

  test('CA-02: Protect ROOT Role from deletion', async ({ page }) => {
    // Navigate to Roles tab
    await page.click('[data-testid="tab-roles"]');
    
    // Locate the ROLE_SUPER_ADMIN row
    const rootRow = page.locator('tr', { hasText: 'ROLE_SUPER_ADMIN' });
    await expect(rootRow).toBeVisible();
    
    // Verify delete button is NOT present for this row and "PROTEGER" label exists
    const deleteBtn = rootRow.locator('button:has-text("Eliminar")');
    await expect(deleteBtn).not.toBeVisible();
    
    await expect(rootRow).toContainText('PROTEGER');
  });

  test('CA-04: Granular Matrix - Toggle Initiator/Executor', async ({ page }) => {
    // Switch to Matriz de Seguridad tab
    await page.click('[data-testid="tab-matrix"]');
    
    // Find a checkbox in the matrix (Initiate for first process/role)
    const matrixCheckbox = page.locator('input[type="checkbox"]').first();
    await expect(matrixCheckbox).toBeVisible();
    
    const isChecked = await matrixCheckbox.isChecked();
    
    await matrixCheckbox.click();
    await expect(matrixCheckbox).toBeChecked({ checked: !isChecked });
    
    // Save matrix to persist in localStorage
    await page.click('[data-testid="btn-save-matrix"]');
    
    // Verify it persists after refresh (using matrixState persistence logic)
    await page.reload();
    await page.click('[data-testid="tab-matrix"]');
    await expect(matrixCheckbox).toBeChecked({ checked: !isChecked });
  });

  test('CA-06: Hierarchical Role Inheritance', async ({ page }) => {
    // Navigate to Roles tab
    await page.click('[data-testid="tab-roles"]');
    
    await page.click('[data-testid="btn-create-local-role"]');
    
    // Modal should be visible
    await page.fill('[data-testid="input-role-id"]', 'R_TESTER_PRO');
    await page.fill('[data-testid="input-role-name"]', 'Senior Tester');
    
    // Select a parent role (inheritance logic)
    await page.selectOption('[data-testid="select-parent-role"]', 'ROLE_SUPER_ADMIN');
    
    await page.click('[data-testid="btn-confirm-role"]');
    
    // Verify creation in table
    await expect(page.locator('table')).toContainText('Senior Tester');
    await expect(page.locator('table')).toContainText('R_TESTER_PRO');
  });
});
