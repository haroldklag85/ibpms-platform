import { test, expect } from '@playwright/test';

test.describe('US-007 GAP-05: Hit Policy FIRST y Catch-All (Zero-Mock)', () => {
  test('Tabla DMN publicada tiene Hit Policy FIRST y rechaza UNIQUE (CA-22)', async ({ request, page }) => {
    const rawXmlUnique = `
      <definitions xmlns="https://www.omg.org/spec/DMN/20191111/MODEL/">
        <decision id="Decision_1" name="Test">
          <decisionTable id="DecisionTable_1" hitPolicy="UNIQUE">
             <output id="Output_1" typeRef="string" />
          </decisionTable>
        </decision>
      </definitions>
    `;
    
    // Validar CA-22: Rechazar UNIQUE
    const resUnique = await request.post('/api/v1/dmn/drafts', {
      data: { xml: rawXmlUnique }
    });
    expect(resUnique.status()).toBe(422); // Unprocessable Entity

    // Validar FIRST por defecto
    const draftRes = await request.post('/api/v1/dmn/generate', {
      data: { prompt: 'aprobar si la edad es mayor a 18' }
    });
    const generated = await draftRes.json();
    const pubRes = await request.post(`/api/v1/dmn/${generated.id}/publish`);
    const published = await pubRes.json();
    expect(published.xml).toContain('hitPolicy="FIRST"');
  });

  test('Catch-All es inamovible en la UI', async ({ page }) => {
    // Navegar a la pantalla de DMN manual o abrir un borrador
    await page.goto('/workdesk/dmn/new');
    
    // Esperar que la grilla cargue
    const grid = page.locator('.dmn-grid-container');
    await expect(grid).toBeVisible();
    
    // Localizar la fila catch-all (suele ser la última)
    const catchAllRow = page.locator('tr.catch-all-row');
    await expect(catchAllRow).toBeVisible();
    
    // Verificar icono 🔒
    await expect(catchAllRow.locator('.lock-icon')).toBeVisible();
    
    // Intentar buscar botón de eliminar (no debe existir o debe estar deshabilitado)
    const deleteBtn = catchAllRow.locator('button.delete-row');
    if (await deleteBtn.count() > 0) {
      await expect(deleteBtn).toBeDisabled();
    }
  });
});
