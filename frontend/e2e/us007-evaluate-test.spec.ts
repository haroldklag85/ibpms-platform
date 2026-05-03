import { test, expect } from '@playwright/test';

test.describe('US-007 GAP-13: Endpoint Simulador Funcional (Zero-Mock)', () => {
  test('Evaluate-test retorna resultado correcto y estructurado', async ({ request }) => {
    // Para testear esto genuinamente en E2E, requerimos una tabla ACTIVA en BD.
    // Usamos el ID asumiendo que el seeder la plantó, o publicamos una tabla al vuelo.
    const rawXml = `
      <definitions xmlns="https://www.omg.org/spec/DMN/20191111/MODEL/">
        <decision id="risk_matrix" name="Risk Matrix">
          <decisionTable id="DecisionTable_1" hitPolicy="FIRST">
             <input id="Input_1"><inputExpression typeRef="number"><text>monto</text></inputExpression></input>
             <output id="Output_1" name="decision" typeRef="string" />
             <rule id="Rule_1">
               <inputEntry id="In_1"><text>&lt; 1000</text></inputEntry>
               <outputEntry id="Out_1"><text>"Aprobado"</text></outputEntry>
             </rule>
          </decisionTable>
        </decision>
      </definitions>
    `;
    
    // Crear borrador y publicar
    const draftRes = await request.post('/api/v1/dmn/drafts', { data: { xml: rawXml } });
    const draft = await draftRes.json();
    const pubRes = await request.post(`/api/v1/dmn/${draft.id}/publish`);
    const published = await pubRes.json();

    // Evaluar
    const evalRes = await request.post(`/api/v1/dmn/${published.id}/evaluate-test`, {
      data: {
        variables: { monto: 500 }
      }
    });
    
    expect(evalRes.ok()).toBeTruthy();
    const result = await evalRes.json();
    
    expect(result).toHaveProperty('matched_rule_index');
    expect(result).toHaveProperty('output');
    expect(result).toHaveProperty('all_rules_evaluated');
    expect(result.output.decision).toBe('Aprobado');
    expect(result.matched_rule_index).toBe(0); // Primera regla
  });

  test('Evaluate-test rechaza DMN en estado DRAFT con HTTP 409', async ({ request }) => {
    // Crear borrador (estado DRAFT por defecto)
    const draftRes = await request.post('/api/v1/dmn/drafts', {
      data: { xml: '<definitions></definitions>' }
    });
    const draft = await draftRes.json();
    
    // Intentar evaluar el DRAFT directamente
    const evalRes = await request.post(`/api/v1/dmn/${draft.id}/evaluate-test`, {
      data: { variables: {} }
    });
    
    expect(evalRes.status()).toBe(409);
    const errorBody = await evalRes.json();
    expect(errorBody.message).toContain('Solo DMNs publicadas pueden evaluarse');
  });
});
