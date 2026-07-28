import { test, expect } from '@playwright/test';
import { USERS, API } from '../fixtures/e2e-data';

test.describe('OBS-1 / B5: Kanban View Real Backend', () => {

  test('Operario interactua con Kanban real (API, Blocked modal, Done inmutable)', async ({ page }) => {
    test.setTimeout(90000);
    // 1. Autenticación
    await page.goto('/login');
    await page.click('[data-testid="break-glass-toggle"]');
    await page.fill('[data-testid="email-input"]', USERS.ANALISTA_N1.email);
    await page.fill('[data-testid="password-input"]', USERS.ANALISTA_N1.password);
    await page.click('[data-testid="login-submit"]');

    // 2. Navegar a Kanban
    await page.goto('/kanban');
    
    // 3. Verificar API Real (Sin mock)
    const responsePromise = page.waitForResponse(response => 
      response.url().includes('/kanban') && response.status() === 200
    );
    await responsePromise;

    // Verificar renderizado de la tarjeta E2E
    const card = page.locator('[data-testid^="kanban-card-"]').first();
    await expect(card).toBeVisible({ timeout: 20000 });

    // 4. Modal de BLOCKED exige que no esté vacío (solo >= 1 char)
    const blockedColumn = page.locator('[data-testid^="column-"]').filter({ hasText: /Blocked|Bloqueado/i });
    if(await blockedColumn.isVisible()) {
      await card.dragTo(blockedColumn);

      const blockReasonInput = page.locator('[data-testid="block-reason-input"]');
      await expect(blockReasonInput).toBeVisible({ timeout: 5000 });
      
      const submitBtn = page.locator('[data-testid="confirm-block"]');
      
      // Test de validación (vacío -> disabled)
      await blockReasonInput.fill('');
      await expect(submitBtn).toBeDisabled();

      // Test de validación (no vacío -> enabled)
      await blockReasonInput.fill('Falla');
      await expect(submitBtn).toBeEnabled();
      
      // Abortar submit para no afectar test isolation (o enviarlo según se decida)
      await page.locator('[data-testid="cancel-block"]').click();
    }

    // 5. Inmutabilidad de columna DONE (No es draggable)
    const doneColumn = page.locator('[data-testid^="column-"]').filter({ hasText: /Done|Terminado/i });
    if(await doneColumn.isVisible() && await doneColumn.locator('[data-testid^="kanban-card-"]').count() > 0) {
       const doneCard = doneColumn.locator('[data-testid^="kanban-card-"]').first();
       const isDraggable = await doneCard.getAttribute('draggable');
       expect(['false', null]).toContain(isDraggable);
    }
  });
});
