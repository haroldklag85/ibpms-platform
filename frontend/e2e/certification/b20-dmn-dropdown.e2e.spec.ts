import { test, expect } from '@playwright/test';
import { USERS } from '../fixtures/e2e-data';

test.describe('B-20: Dropdown DMN en BpmnDesigner', () => {
  
  test('Seleccionar BusinessRuleTask muestra dropdown DMN con tablas publicadas', async ({ page }) => {
    test.setTimeout(60000);
    
    // Login como Arquitecto
    await page.goto('/login');
    await page.click('[data-testid="break-glass-toggle"]');
    await page.fill('[data-testid="email-input"]', USERS.ARQUITECTO_ALPHA.email);
    await page.fill('[data-testid="password-input"]', USERS.ARQUITECTO_ALPHA.password);
    await page.click('[data-testid="login-submit"]');
    
    // Navegar al BPMN Designer
    await page.goto('/bpmn-designer');
    await page.waitForSelector('[data-testid="bpmn-canvas"]', { timeout: 20000 });
    
    // Agregar BusinessRuleTask al canvas (si existe palette)
    // Seleccionar el BusinessRuleTask existente
    await page.click('[data-testid="bpmn-canvas"] [data-type="bpmn:BusinessRuleTask"]');
    
    // Verificar que el sidebar muestra el dropdown DMN
    const dmnDropdown = page.locator('select:has(option:text("— Seleccionar tabla DMN —"))');
    await expect(dmnDropdown).toBeVisible({ timeout: 10000 });
    
    // Verificar que el dropdown tiene opciones (tablas DMN del seed)
    const options = dmnDropdown.locator('option');
    expect(await options.count()).toBeGreaterThan(1); // Al menos "— Seleccionar —" + 1 DMN
  });
});
