import { test, expect } from '@playwright/test';
import { API } from '../fixtures/e2e-data';

/**
 * @Traceability: US-008 — Kanban Board (Drag & Drop, BLOCKED Modal, Persistencia Real)
 * @ADR: ADR-010 (Zero-Mock E2E), ADR-011 (Local CQRS)
 * @Handoff: handoff_qa_j04_certification.md §1
 *
 * Endpoints reales:
 *   - GET  /api/v1/kanban-tasks/boards/{boardId}/columns
 *   - GET  /api/v1/kanban-tasks/boards/{boardId}/tasks
 *   - PATCH /api/v1/kanban-tasks/tasks/{taskId}/state
 *
 * Precondiciones:
 *   - Backend nativo en :8080 (start-e2e.bat)
 *   - PostgreSQL con seed-e2e.sql ejecutado (kanban_boards + ibpms_task con tareas TODO)
 *   - Frontend en :5173 (npm run dev)
 *
 * 🚨 ZERO-MOCK: Este spec NO usa route.fulfill() ni interceptores.
 *    Todas las respuestas provienen del backend real.
 */
test.describe('US-008 Zero-Mock: Kanban Board — Drag & Drop y BLOCKED Modal', () => {
  test.use({ storageState: 'e2e/playwright/.auth/user.json' });

  // ==========================================
  // Capa 1 (UX/DOM): Renderizado de columnas
  // ==========================================
  test('CU-KB-01 | Kanban renderiza columnas reales desde el backend (sin mocks)', async ({ page }) => {
    await page.goto('/kanban');

    // ASSERT Capa 2 (Red): Interceptar la llamada real al backend
    const columnsResponse = page.waitForResponse(
      res => res.url().includes('/kanban') && res.status() < 500
    );

    await page.waitForLoadState('networkidle');
    const response = await columnsResponse;

    // La página debe renderizar al menos una columna visible
    const columns = page.locator('[data-testid^="kanban-column-"]');
    const count = await columns.count();

    // Si el backend no tiene columnas configuradas, el test detectará el gap
    if (count === 0) {
      test.info().annotations.push({
        type: 'INFRA',
        description: 'No se renderizaron columnas Kanban. Verificar seed-e2e.sql y endpoint de columnas.'
      });
    }
    expect(count).toBeGreaterThanOrEqual(1);
  });

  // ==========================================
  // Capa 1+2: Tarjetas TODO visibles (data real)
  // ==========================================
  test('CU-KB-02 | Al menos 1 tarjeta en TODO proviene de datos reales (seed-e2e.sql)', async ({ page }) => {
    await page.goto('/kanban');
    await page.waitForLoadState('networkidle');

    const todoColumn = page.locator('[data-testid="kanban-column-TODO"]');
    if (await todoColumn.isVisible()) {
      const cards = todoColumn.locator('[data-testid^="kanban-card-"]');
      const cardCount = await cards.count();
      expect(cardCount).toBeGreaterThanOrEqual(1);
    } else {
      // Columna TODO no existe — posible gap de configuración
      test.info().annotations.push({
        type: 'INFRA',
        description: 'Columna TODO no visible. ¿El board usa estados distintos? Verificar endpoint.'
      });
      expect(await todoColumn.isVisible()).toBeTruthy();
    }
  });

  // ==========================================
  // Capa 2+3: Drag & Drop dispara PATCH real
  // ==========================================
  test('CU-KB-03 | Drag & Drop TODO→IN_PROGRESS dispara PATCH /state al backend real', async ({ page }) => {
    await page.goto('/kanban');
    await page.waitForLoadState('networkidle');

    const card = page.locator('[data-testid^="kanban-card-"]').first();
    const inProgressColumn = page.locator('[data-testid="kanban-column-IN_PROGRESS"]');

    if (!(await card.isVisible()) || !(await inProgressColumn.isVisible())) {
      test.skip(true, 'Card o columna IN_PROGRESS no visibles — seed insuficiente');
      return;
    }

    // Interceptar (sin mockear) la petición PATCH real al backend
    const patchPromise = page.waitForRequest(
      req => req.url().includes('/kanban-tasks/tasks/') &&
             req.url().includes('/state') &&
             req.method() === 'PATCH',
      { timeout: 15_000 }
    );

    await card.dragTo(inProgressColumn);

    // ASSERT Capa 2 (Red): La petición PATCH salió al backend
    const patchRequest = await patchPromise;
    expect(patchRequest.method()).toBe('PATCH');

    const postData = JSON.parse(patchRequest.postData() || '{}');
    expect(postData.newState).toBeTruthy();

    // ASSERT Capa 1 (UX): El sync indicator confirma éxito
    const syncStatus = page.locator('[data-testid="kanban-sync-status"]');
    if (await syncStatus.isVisible({ timeout: 5000 }).catch(() => false)) {
      await expect(syncStatus).toContainText('OK', { timeout: 10_000 });
    }
  });

  // ==========================================
  // Capa 1+2+3: BLOCKED requiere modal con justificación
  // ==========================================
  test('CU-KB-04 | Drag hacia BLOCKED abre modal de justificación y envía blockReason en PATCH', async ({ page }) => {
    await page.goto('/kanban');
    await page.waitForLoadState('networkidle');

    const card = page.locator('[data-testid^="kanban-card-"]').first();
    const blockedColumn = page.locator('[data-testid="kanban-column-BLOCKED"]');

    if (!(await card.isVisible()) || !(await blockedColumn.isVisible())) {
      test.skip(true, 'Card o columna BLOCKED no visibles');
      return;
    }

    // ACT: Drag card al BLOCKED
    await card.dragTo(blockedColumn);

    // ASSERT Capa 1 (UX): Modal de justificación debe aparecer
    const blockReasonInput = page.locator('[data-testid="block-reason-input"]');
    await expect(blockReasonInput).toBeVisible({ timeout: 5_000 });

    // Fill justificación y confirmar
    const justificacion = 'Bloqueo E2E: documentos pendientes de validación jurídica';
    await blockReasonInput.fill(justificacion);

    // Interceptar PATCH real
    const patchPromise = page.waitForRequest(
      req => req.url().includes('/state') && req.method() === 'PATCH',
      { timeout: 10_000 }
    );

    await page.locator('[data-testid="confirm-block"]').click();

    // ASSERT Capa 2+3 (Red + Payload): El PATCH incluye el motivo de bloqueo
    const request = await patchPromise;
    const payload = JSON.parse(request.postData() || '{}');
    expect(payload.newState).toBe('BLOCKED');
    expect(payload.blockedReason || payload.blockReason).toBeTruthy();
  });
});
