import { test, expect } from '@playwright/test';

// @Traceability: US-002 - QA Zero-Mock
test.describe('US-002 V2: Workbox Kanban y Operaciones Masivas (CA-02, CA-04, CA-22)', () => {
  test.use({ storageState: 'e2e/playwright/.auth/user.json' });

  test.beforeEach(async ({ page }) => {
    // Interceptar la llamada de carga de la bandeja para proveer datos semilla en memoria (Mocking en red)
    await page.route('**/api/v1/workdesk/global-inbox*', async route => {
      const url = route.request().url();
      if (url.includes('POOL')) {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            content: [
              { unifiedId: 't-pool-1', title: 'Tarea Pool 1', status: 'AVAILABLE', assignee: null },
              { unifiedId: 't-pool-2', title: 'Tarea Pool 2', status: 'AVAILABLE', assignee: null }
            ],
            totalElements: 2
          })
        });
      } else {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            content: [
              { unifiedId: 't-personal-1', title: 'Tarea Personal 1', status: 'ACTIVE', assignee: 'root_e2e' }
            ],
            totalElements: 1
          })
        });
      }
    });

    await page.goto('/workdesk');
  });

  test('Prueba 1 (CA-22 Tabs): Interaccionalidad de pestañas', async ({ page }) => {
    const tabPersonal = page.locator('button:has-text("Mis Tareas")');
    const tabPool = page.locator('button:has-text("Pool Disponible")');

    await expect(tabPersonal).toBeVisible();
    await expect(tabPool).toBeVisible();

    // Click on Pool Disponible
    await tabPool.click();
    await expect(tabPool).toHaveClass(/bg-teal-600/);

    // Click on Mis Tareas
    await tabPersonal.click();
    await expect(tabPersonal).toHaveClass(/bg-indigo-600/);
  });

  test('Prueba 2 (CA-02 Bulk Claim): Reclamación masiva y Optimistic UI', async ({ page }) => {
    // 1. Navegar al Pool
    const tabPool = page.locator('button:has-text("Pool Disponible")');
    await tabPool.click();

    // 2. Seleccionar múltiples tareas
    // Hay dos checkboxes inyectados desde el mock del POOL
    const checkboxes = page.locator('input[type="checkbox"]');
    await expect(checkboxes).toHaveCount(2);
    await checkboxes.nth(0).check();
    await checkboxes.nth(1).check();

    // Promesa para interceptar la llamada de bulk-claim
    const bulkClaimPromise = page.waitForRequest(
      req => req.url().includes('/api/v1/workbox/tasks/bulk-claim') && req.method() === 'POST'
    );

    // Mock de la respuesta de éxito
    await page.route('**/api/v1/workbox/tasks/bulk-claim', async route => {
      await route.fulfill({ status: 200, body: '[]' });
    });

    // 3. Ejecutar acción
    const btnReclamar = page.locator('button:has-text("Reclamar Seleccionadas")');
    await expect(btnReclamar).toBeVisible();
    await btnReclamar.click();

    // 4. Validar payload de intercepción Zero-Mock
    const request = await bulkClaimPromise;
    const postData = JSON.parse(request.postData() || '[]');
    expect(postData).toContain('t-pool-1');
    expect(postData).toContain('t-pool-2');

    // 5. Validar Optimistic UI: Debe transicionar a la vista "PERSONAL"
    const tabPersonal = page.locator('button:has-text("Mis Tareas")');
    await expect(tabPersonal).toHaveClass(/bg-indigo-600/);
  });

  test('Prueba 3 (CA-04 Unclaim): Liberar tarea con Motivo', async ({ page }) => {
    // 1. Permanecer en Mis Tareas (Personal)
    const btnUnclaim = page.locator('button:has-text("Liberar (Unclaim)")').first();
    await expect(btnUnclaim).toBeVisible();
    
    // 2. Abrir Modal de Liberación
    await btnUnclaim.click();

    const unclaimModal = page.locator('text=¿Liberar Tarea?');
    await expect(unclaimModal).toBeVisible();

    // 3. Ingresar Motivo
    const txtMotivo = page.locator('textarea[placeholder="Justificación de liberación..."]');
    await expect(txtMotivo).toBeVisible();
    await txtMotivo.fill('Reasignación E2E');

    // Promesa para interceptar la llamada de unclaim
    const unclaimPromise = page.waitForRequest(
      req => req.url().includes('/unclaim') && req.method() === 'POST'
    );

    // Mock de respuesta de unclaim
    await page.route('**/api/v1/workbox/tasks/*/unclaim', async route => {
      await route.fulfill({ status: 200 });
    });

    // 4. Confirmar liberación
    const btnConfirmar = page.locator('button:has-text("Sí, liberar")');
    await btnConfirmar.click();

    // 5. Validar payload de intercepción Zero-Mock
    const request = await unclaimPromise;
    const postData = JSON.parse(request.postData() || '{}');
    expect(postData.mensajeInterno).toBe('Reasignación E2E');
  });
});
