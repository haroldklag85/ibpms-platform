import { test, expect } from '@playwright/test';

test.describe('US-007 GAP-09: Simulador XAI Iluminación (Zero-Mock)', () => {
  test('Simulador ilumina fila correcta y traduce FEEL', async ({ page }) => {
    // 1. Navegar a un DMN publicado o mockeado en BDD
    await page.goto('/workdesk/dmn/catalog');
    
    // Entrar al detalle de la primera DMN activa
    await page.locator('tr').filter({ hasText: 'ACTIVA' }).first().click();
    
    // Abrir el panel simulador
    await page.locator('button:has-text("Simulador")').click();
    
    // Llenar variables de prueba (asumimos un input genérico 'monto')
    const input = page.locator('input[name="monto"]');
    if (await input.count() > 0) {
       await input.fill('500');
       await page.locator('button:has-text("Ejecutar Prueba")').click();
       
       // Verificar fila iluminada (verde)
       const highlightedRow = page.locator('tr.bg-green-100, tr.highlight-success');
       await expect(highlightedRow).toBeVisible();
       
       // Verificar XAI: traducción de FEEL a humano
       const xaiPanel = page.locator('.xai-panel');
       await expect(xaiPanel).toBeVisible();
       await expect(xaiPanel).toContainText(/Si el monto/i);
    } else {
       // Si no hay inputs predictivos en la data dummy, la prueba falla constructivamente
       // para recordar sembrar datos.
       test.fail(true, 'No se encontraron inputs de simulación. Falta task-seeder para DMN ACTIVA.');
    }
  });

  test('Rate limit del simulador 21 requests en 60s (CA-23)', async ({ request }) => {
    const endpoint = '/api/v1/dmn/1/evaluate-test'; // Id dummy, puede dar 404 pero queremos validar 429
    for (let i = 0; i < 20; i++) {
      const res = await request.post(endpoint, { data: { variables: {} } });
      expect(res.status()).not.toBe(429);
    }
    const res21 = await request.post(endpoint, { data: { variables: {} } });
    // Si la limitación es per endpoint, esperamos un 429
    expect(res21.status()).toBe(429);
  });
});
