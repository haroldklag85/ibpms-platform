import { test, expect } from '@playwright/test';
import { seedDmnTable } from './helpers/task-seeder';

test.describe('US-007 Generador Cognitivo de DMN - Modo Manual [Zero-Mock]', () => {
  const testDmnId = 'test-dmn-manual';

  test.beforeEach(async ({ request }) => {
    // Zero-Mock: Seeder real al backend
    await seedDmnTable(request, testDmnId, 1);
  });

  test('Simultaneous display of Chat and Manual Grid (CA-26)', async ({ page }) => {
    await page.goto(`/admin/modeler/dmn-intelligence/${testDmnId}`);
    
    // Verificamos resiliencia en la UI de que los paneles coexisten
    await expect(page.locator('.chat-nlp-panel')).toBeVisible();
    await expect(page.locator('.dmn-grid-panel')).toBeVisible();
  });

  test('Validates FEEL syntax correctly (CA-28)', async ({ page }) => {
    await page.goto(`/admin/modeler/dmn-intelligence/${testDmnId}`);
    
    const conditionCell = page.locator('.dmn-cell.condition').first();
    const saveButton = page.getByRole('button', { name: /Guardar|Publicar/i });

    // Insertar error de sintaxis FEEL
    await conditionCell.click();
    await page.keyboard.type('texto_invalido');
    await page.keyboard.press('Enter');

    // Validación visual de error
    await expect(conditionCell).toHaveClass(/error-feel|invalid/);
    await expect(saveButton).toBeDisabled();

    // Corregir sintaxis
    await conditionCell.click();
    await page.keyboard.press('Control+A');
    await page.keyboard.press('Backspace');
    await page.keyboard.type('"texto_valido"');
    await page.keyboard.press('Enter');

    // Validación visual de éxito
    await expect(conditionCell).not.toHaveClass(/error-feel|invalid/);
    await expect(saveButton).toBeEnabled();
  });

  test('Catch-all row is permanently locked (CA-29)', async ({ page }) => {
    await page.goto(`/admin/modeler/dmn-intelligence/${testDmnId}`);
    
    const catchAllRow = page.locator('tr.catch-all');
    await expect(catchAllRow).toBeVisible();
    await expect(catchAllRow).toContainText('Revisión Humana');
    await expect(catchAllRow.locator('.delete-btn')).not.toBeVisible();
  });

  test('Max 100 rows SRE limit enforced (CA-31)', async ({ page, request }) => {
    // Seed con 99 filas para llegar rápidamente al límite
    await seedDmnTable(request, testDmnId, 99);
    await page.goto(`/admin/modeler/dmn-intelligence/${testDmnId}`);
    
    const addRowBtn = page.locator('button.add-row-btn');
    
    // Agregar la fila número 100
    await addRowBtn.click();
    
    // El límite se alcanza
    await expect(addRowBtn).toBeDisabled();
    await expect(page.locator('text=Límite SRE alcanzado')).toBeVisible();
  });

  test('Tags manual modification correctly (CA-32)', async ({ page }) => {
    await page.goto(`/admin/modeler/dmn-intelligence/${testDmnId}`);
    
    // Simular un cambio
    const conditionCell = page.locator('.dmn-cell.condition').first();
    await conditionCell.click();
    await page.keyboard.type('"modified"');
    await page.keyboard.press('Enter');
    
    // Guardar
    const saveButton = page.getByRole('button', { name: /Guardar/i });
    await saveButton.click();

    // Navegar al catálogo
    await page.goto('/admin/modeler/dmn-intelligence');
    
    // Verificar el badge de trazabilidad
    const modelRow = page.locator(`tr[data-id="${testDmnId}"]`);
    await expect(modelRow.locator('text=Modificada Manualmente')).toBeVisible();
  });
});
