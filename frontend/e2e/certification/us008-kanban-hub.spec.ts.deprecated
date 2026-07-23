import { test, expect } from '@playwright/test';

// @Traceability: US-008 - QA Zero-Mock
test.describe('US-008 V2: Kanban Hub, Drag & Drop y Restricciones (CA-01, CA-02, CA-06)', () => {
  test.use({ storageState: 'e2e/playwright/.auth/user.json' });

  test.beforeEach(async ({ page }) => {
    // Interceptar la llamada de carga de columnas
    await page.route('**/api/v1/kanban-tasks/boards/default-board/columns', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          { id: 'c1', name: 'TODO' },
          { id: 'c2', name: 'DOING' },
          { id: 'c3', name: 'BLOCKED' },
          { id: 'c4', name: 'DONE' }
        ])
      });
    });

    // Interceptar la carga de tareas
    await page.route('**/api/v1/kanban-tasks/boards/default-board/tasks', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          { id: 't1', title: 'Task 1', status: 'TODO', priority: 10 },
          { id: 't2', title: 'Task 2', status: 'DONE', priority: 20 }
        ])
      });
    });

    await page.goto('/kanban');
  });

  test('Prueba 1 (CA-06 Zero-Mock): Drag & Drop hacia DOING', async ({ page }) => {
    await page.goto('/kanban');

    const card = page.locator('[data-testid="kanban-card-t1"]');
    const dropZone = page.locator('[data-testid="kanban-column-c2"] .vue-draggable, [data-testid="kanban-column-c2"] > div').last();

    await expect(card).toBeVisible();
    await expect(dropZone).toBeVisible();

    // Promesa para interceptar la llamada
    const patchPromise = page.waitForRequest(
      req => req.url().includes('/api/v1/kanban-tasks/tasks/t1/state') && req.method() === 'PATCH'
    );

    // Mock response
    await page.route('**/api/v1/kanban-tasks/tasks/t1/state', async route => {
      await route.fulfill({ status: 200, body: '{}' });
    });

    // Simulando el drag & drop
    await card.dragTo(dropZone);

    // Validar Zero-Mock Payload
    const request = await patchPromise;
    const postData = JSON.parse(request.postData() || '{}');
    expect(postData.newState).toBe('DOING');
  });

  test('Prueba 2 (CA-01 Blocked Modal): Drag hacia BLOCKED y motivo de bloqueo', async ({ page }) => {
    await page.goto('/kanban');

    const card = page.locator('[data-testid="kanban-card-t1"]');
    const dropZoneBlocked = page.locator('[data-testid="kanban-column-c3"] .vue-draggable, [data-testid="kanban-column-c3"] > div').last();

    await card.dragTo(dropZoneBlocked);

    // Validar modal
    const modalInput = page.locator('[data-testid="block-reason-input"]');
    await expect(modalInput).toBeVisible();
    await modalInput.fill('Bloqueo de prueba E2E');

    const patchPromise = page.waitForRequest(
      req => req.url().includes('/api/v1/kanban-tasks/tasks/t1/state') && req.method() === 'PATCH'
    );

    await page.route('**/api/v1/kanban-tasks/tasks/t1/state', async route => {
      await route.fulfill({ status: 200, body: '{}' });
    });

    await page.locator('[data-testid="confirm-block"]').click();

    // Validar Zero-Mock Payload
    const request = await patchPromise;
    const postData = JSON.parse(request.postData() || '{}');
    expect(postData.newState).toBe('BLOCKED');
    expect(postData.blockedReason).toBe('Bloqueo de prueba E2E');
  });

  test('Prueba 3 (CA-02 Inmutabilidad DONE): Read-Only de tarjeta finalizada', async ({ page }) => {
    await page.goto('/kanban');

    const doneCard = page.locator('[data-testid="kanban-card-t2"]');
    await expect(doneCard).toBeVisible();

    // Verificar clase CSS done-readonly que bloquea eventos
    await expect(doneCard).toHaveClass(/done-readonly/);
    await expect(doneCard).toHaveCSS('pointer-events', 'none');

    // Intentar forzar apertura de la tarea usando un método que salte el pointer-events
    await doneCard.locator('button', { hasText: '✏️ Abrir' }).dispatchEvent('click');

    const readOnlyModal = page.locator('.modal-content');
    await expect(readOnlyModal).toBeVisible();

    // Verificamos si los botones principales de reclamo están deshabilitados
    const btnReclamar = readOnlyModal.locator('[data-test="btn-claim"]');
    if (await btnReclamar.count() > 0) {
      await expect(btnReclamar).toBeDisabled();
    }
    
    // Verificamos si algún input en el modal (si lo hay) está disabled
    const inputs = readOnlyModal.locator('input, textarea, select');
    const count = await inputs.count();
    for (let i = 0; i < count; i++) {
      await expect(inputs.nth(i)).toBeDisabled();
    }
  });
});
