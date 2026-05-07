import { test, expect } from '@playwright/test';

test.describe('US-007 GAP-11: Persistencia Dual Drafts (Zero-Mock)', () => {
  test('Borrador se persiste en PostgreSQL via API REST', async ({ request }) => {
    // Simulamos que iteramos en el chat NLP y no publicamos
    const draftPayload = {
      xml: '<definitions id="draft_pg"><decision></decision></definitions>',
      metadata: { source: 'NLP_CHAT' }
    };
    
    // Guardar en BD (PostgreSQL) vía API
    const postRes = await request.post('/api/v1/dmn/drafts', { data: draftPayload });
    expect(postRes.ok()).toBeTruthy();
    const draft = await postRes.json();
    expect(draft).toHaveProperty('id');
    
    // Recuperar vía API
    const getRes = await request.get(`/api/v1/dmn/drafts/${draft.id}`);
    expect(getRes.ok()).toBeTruthy();
    const fetchedDraft = await getRes.json();
    expect(fetchedDraft.xml).toContain('draft_pg');
  });

  test('Borrador se cachea en LocalStorage en la UI', async ({ page }) => {
    await page.goto('/workdesk/dmn/new');
    
    // Esperar a que la página cargue y asigne un UUID local o de draft
    await page.waitForLoadState('networkidle');
    
    // Simular escritura en la grilla para desencadenar el autoSave
    // Idealmente localizamos una celda y tipeamos
    const cell = page.locator('td.editable-cell').first();
    if (await cell.count() > 0) {
       await cell.click();
       await page.keyboard.type('test value');
       await page.keyboard.press('Enter');
       
       // Esperar debounced save (ej: 1s)
       await page.waitForTimeout(1500);
       
       // Verificar LocalStorage
       const hasDraft = await page.evaluate(() => {
          for (let i = 0; i < localStorage.length; i++) {
             const key = localStorage.key(i);
             if (key && key.includes('dmn_draft_')) {
                return true;
             }
          }
          return false;
       });
       
       expect(hasDraft).toBe(true);
       
       // Simular cierre y reapertura recargando
       await page.reload();
       await page.waitForLoadState('networkidle');
       
       // El dato debería seguir allí si fue restaurado desde el draft local o remoto
       await expect(cell).toContainText('test value');
    } else {
       test.fail(true, 'No se pudo editar celda para probar LocalStorage');
    }
  });
});
