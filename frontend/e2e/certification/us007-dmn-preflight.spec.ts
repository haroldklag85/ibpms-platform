import { test, expect } from '@playwright/test';

test.describe('US-007 V2: Certificación Zero-Mock Pre-Flight DMN (CA-14)', () => {
  test.use({ storageState: 'e2e/playwright/.auth/user.json' });

  test.beforeEach(async ({ page }) => {
    // Inyectar el borrador en localStorage para activar el UI del DMN
    await page.addInitScript(() => {
      localStorage.setItem('ibpms_dmn_draft_v1', JSON.stringify({
        prompt: 'Regla de prueba generada por E2E',
        hasData: true,
        xmlData: '<?xml version="1.0" encoding="UTF-8"?><definitions id="mock"><decision id="decision_test" name="Decision Test"><decisionTable><output id="output_1" typeRef="string" /><rule id="rule_1"><outputEntry id="literal_1"><text>"Success"</text></outputEntry></rule></decisionTable></decision></definitions>'
      }));
    });

    // Inicializar el estado de la aplicación
    await page.goto('/');
  });

  // @Traceability: Certificación E2E J-02 (T-24)
  test('CA-14: Simulación DMN (Pre-Flight Zero-Mock)', async ({ page }) => {
    // 1. Navegar al DMN Intelligence
    await page.goto('/admin/modeler/dmn');

    // Promesa para interceptar la llamada de red al backend real (sin mockear la respuesta)
    const simulationResponsePromise = page.waitForResponse(
      response => response.url().includes('/api/v1/dmn-models/simulate-sandbox') && response.status() === 200
    );

    // 2. Hacer clic en [🧪 Probar DMN]
    const testButton = page.locator('button', { hasText: 'Probar DMN' });
    await expect(testButton).toBeVisible();
    await testButton.click();

    // 3. Validar interceptación de red (rompiendo el ciclo del mock de la capa de componentes)
    const response = await simulationResponsePromise;
    const responseData = await response.json();
    
    // Verificar que el backend real haya procesado correctamente
    expect(responseData).toBeDefined();

    // 4. Afirmar visualmente que el frontend cambia el estado indicando simulación exitosa
    // El frontend asigna: lastAction.value = "[XAI Simulación] Ejecución de prueba hit row ID: 1" o similar
    const lastActionLabel = page.locator('aside >> text=/Ejecución de prueba hit row ID/i');
    await expect(lastActionLabel).toBeVisible();
  });
});
