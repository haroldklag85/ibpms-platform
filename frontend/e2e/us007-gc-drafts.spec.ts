import { test, expect } from '@playwright/test';

test.describe('US-007 GAP-02: GC Drafts y Minificación XML (Zero-Mock)', () => {
  test('Borradores expiran después de 24 horas y arrojan 404', async ({ request }) => {
    // Para no esperar 24 horas en un test E2E real, usamos un borrador forzado al pasado
    // O invocamos un endpoint de utilidad de pruebas si existe. 
    // Dado que el arquitecto pidió: "(crear draft -> esperar/forzar TTL -> verificar 404)"
    
    const draftRes = await request.post('/api/v1/dmn/drafts', {
      data: { xml: '<definitions></definitions>' }
    });
    expect(draftRes.ok()).toBeTruthy();
    const draft = await draftRes.json();
    
    // Forzamos la expiración vía API de testing o esperamos que task-seeder ya haya expirado uno específico
    const forceRes = await request.post(`/api/v1/dmn/drafts/${draft.id}/force-expire`);
    // Permitimos que falle si no existe este endpoint, es TDD Red.
    
    const getRes = await request.get(`/api/v1/dmn/drafts/${draft.id}`);
    expect(getRes.status()).toBe(404);
  });

  test('XML publicado se minifica sin corrupción y sin whitespace innecesario', async ({ request }) => {
    const rawXml = `
      <definitions xmlns="https://www.omg.org/spec/DMN/20191111/MODEL/">
        <decision id="Decision_1" name="Test">
          <decisionTable id="DecisionTable_1">
             <output id="Output_1" typeRef="string" />
          </decisionTable>
        </decision>
      </definitions>
    `;
    
    // Crear borrador
    const draftRes = await request.post('/api/v1/dmn/drafts', {
      data: { xml: rawXml }
    });
    const draft = await draftRes.json();
    
    // Publicar
    const pubRes = await request.post(`/api/v1/dmn/${draft.id}/publish`);
    expect(pubRes.ok()).toBeTruthy();
    const published = await pubRes.json();
    
    // El XML no debe tener retornos de carro excesivos o espacios entre tags principales
    expect(published.xml).not.toMatch(/>\s+</);
  });
});
