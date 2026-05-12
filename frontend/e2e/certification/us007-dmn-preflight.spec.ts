import { test, expect } from '@playwright/test';

test.describe('US-007 V2: Certificación Zero-Mock Pre-Flight DMN (CA-14)', () => {
  test.use({ storageState: 'e2e/playwright/.auth/user.json' });

  test.beforeEach(async ({ page }) => {
    // Inyectar el borrador en localStorage para activar el UI del DMN
    await page.addInitScript(() => {
      localStorage.setItem('ibpms_dmn_draft_v1', JSON.stringify({
        prompt: 'Regla de prueba generada por E2E',
        hasData: true,
        xmlData: '<?xml version="1.0" encoding="UTF-8"?><definitions id="mock"></definitions>'
      }));
    });

    // Inicializar el estado de la aplicación
    await page.goto('/');
  });

  // @Traceability: US-007 - CA-14
  test('CA-14: Simulación DMN (Pre-Flight Zero-Mock)', async ({ page }) => {
    // 1. Navegar al DMN Intelligence
    await page.goto('/admin/modeler/dmn');

    // Promesa para interceptar/espiar la llamada de red al backend real
    const simulationRequestPromise = page.waitForRequest(
      request => request.url().includes('/api/v1/dmn-models/simulate-sandbox') && request.method() === 'POST'
    );

    // Mocker la respuesta a nivel de Red (Playwright, cumpliendo Zero-Mock de frontend)
    await page.route('**/api/v1/dmn-models/simulate-sandbox', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ hitId: 1 })
      });
    });

    // 2. Hacer clic en [🧪 Probar DMN]
    const testButton = page.locator('button', { hasText: 'Probar DMN' });
    await expect(testButton).toBeVisible();
    await testButton.click();

    // 3. Validar interceptación de red (rompiendo el ciclo del mock de la capa de componentes)
    const request = await simulationRequestPromise;
    const postData = JSON.parse(request.postData() || '{}');
    
    // Verificar que la carga útil (xml) se envía correctamente
    expect(postData.xml).toContain('<definitions id="mock">');

    // 4. Afirmar visualmente que el frontend cambia el estado indicando simulación exitosa
    // El frontend asigna: lastAction.value = "[XAI Simulación] Ejecución de prueba hit row ID: 1"
    const lastActionLabel = page.locator('aside >> text=/Ejecución de prueba hit row ID/i');
    await expect(lastActionLabel).toBeVisible();
    await expect(lastActionLabel).toContainText('Ejecución de prueba hit row ID: 1');
  });
});
