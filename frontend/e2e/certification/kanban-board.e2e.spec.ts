import { test, expect } from '@playwright/test';
import { USERS, API } from '../fixtures/e2e-data';

test.describe('OBS-1 / B5: Kanban View Real Backend', () => {

  test('Operario interactua con Kanban real (API, Blocked modal, Done inmutable)', async ({ page }) => {
    test.setTimeout(90000);
    // 1. Autenticación
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', USERS.OPERARIO_ALPHA.email);
    await page.fill('[data-testid="password-input"]', USERS.OPERARIO_ALPHA.password);
    await page.click('[data-testid="login-submit"]');

    // 2. Navegar a Kanban
    await page.goto('/kanban');
    
    // 3. Verificar API Real (Sin mock)
    const responsePromise = page.waitForResponse(response => 
      response.url().includes('/kanban') && response.status() === 200
    );
    await responsePromise;

    // Verificar renderizado de la tarjeta E2E
    const card = page.locator('.kanban-card, [data-testid^="kanban-card-"]').first();
    await expect(card).toBeVisible({ timeout: 20000 });

    // 4. Modal de BLOCKED exige 10 caracteres mínimos
    const blockedColumn = page.locator('.kanban-column, [data-testid^="column-"]').filter({ hasText: /Blocked|Bloqueado/i });
    if(await blockedColumn.isVisible()) {
      await card.dragTo(blockedColumn);

      const blockReasonInput = page.locator('textarea, input').filter({ hasText: /reason|motivo|justification/i }).first();
      await expect(blockReasonInput).toBeVisible({ timeout: 5000 });
      
      // Test de validación (< 10 chars)
      await blockReasonInput.fill('Falla');
      const submitBtn = page.locator('button').filter({ hasText: /Save|Guardar|Confirm/i });
      await expect(submitBtn).toBeDisabled();

      // Test de validación (>= 10 chars)
      await blockReasonInput.fill('Razon documentada correctamente superior a 10 cols');
      await expect(submitBtn).toBeEnabled();
      // Abortar submit para no afectar test isolation (o enviarlo según se decida)
      await page.locator('button').filter({ hasText: /Cancel|Cerrar/i }).click();
    }

    // 5. Inmutabilidad de columna DONE (No es draggable)
    const doneColumn = page.locator('.kanban-column, [data-testid^="column-"]').filter({ hasText: /Done|Terminado/i });
    if(await doneColumn.isVisible() && await doneColumn.locator('.kanban-card').count() > 0) {
       const doneCard = doneColumn.locator('.kanban-card').first();
       const isDraggable = await doneCard.getAttribute('draggable');
       expect(['false', null]).toContain(isDraggable);
    }
  });
});
