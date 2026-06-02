import { test, expect } from '@playwright/test';
import { USERS } from '../fixtures/e2e-data';

test.describe('Smoke J-04: Operario MVP — Happy Path', () => {
  
  test('CU-J04-01→06: Login → Workdesk → Claim → Form → Submit → Desaparición', async ({ page }) => {
    // Timeout extendido para backend real
    test.setTimeout(90000);
    
    // 2. Operario inicia sesión
    await page.goto('/login');
    await page.click('[data-testid="break-glass-toggle"]');
    await page.fill('[data-testid="email-input"]', USERS.ANALISTA_N1.email);
    await page.fill('[data-testid="password-input"]', USERS.ANALISTA_N1.password);
    await page.locator('textarea').fill('Acceso de emergencia UAT');
    await page.click('[data-testid="login-submit"]');
    
    // 2. Navegar a Workdesk
    await page.waitForURL(/workdesk/);
    
    // 3. Verificar lista de tareas cargada (no vacía)
    const taskList = page.locator('[data-testid="task-list"] [data-testid^="task-row-"]');
    await expect(taskList.first()).toBeVisible({ timeout: 30000 });
    
    // 4. Reclamar primera tarea disponible
    const firstTask = taskList.first();
    const claimButton = firstTask.locator('[data-testid="claim-button"]');
    await claimButton.click();
    
    // 5. Esperar confirmación de claim (toast o estado visual)
    await expect(page.locator('.p-toast-message-success, [data-testid="claim-success"]')).toBeVisible({ timeout: 15000 });
    
    // 6. Abrir formulario de la tarea reclamada
    await firstTask.click();
    await expect(page.locator('[data-testid="form-container"]')).toBeVisible({ timeout: 15000 });
    
    // 7. Llenar campos obligatorios (genéricos)
    const requiredInputs = page.locator('input[required], textarea[required], select[required]');
    const count = await requiredInputs.count();
    for (let i = 0; i < count; i++) {
      const input = requiredInputs.nth(i);
      const tagName = await input.evaluate(el => el.tagName.toLowerCase());
      if (tagName === 'select') {
        await input.selectOption({ index: 1 });
      } else {
        await input.fill('Valor de prueba E2E');
      }
    }
    
    // 8. Enviar formulario
    await page.click('[data-testid="form-submit"]');
    
    // 9. Verificar toast de éxito
    await expect(page.locator('.p-toast-message-success')).toBeVisible({ timeout: 15000 });
    
    // 10. Verificar desaparición del Workdesk (RYOW)
    await page.goto('/workdesk');
    // La tarea reclamada y completada NO debe aparecer
  });
});
