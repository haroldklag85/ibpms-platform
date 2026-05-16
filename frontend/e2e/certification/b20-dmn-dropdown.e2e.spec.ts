import { test, expect } from '@playwright/test';
import { USERS } from '../fixtures/e2e-data';

test.describe('B-20: Dropdown DMN en BpmnDesigner', () => {
  
  test('Seleccionar BusinessRuleTask muestra dropdown DMN con tablas publicadas', async ({ page }) => {
    test.setTimeout(60000);
    // Ya autenticado como Admin via global-setup y storageState
    await page.goto('/workdesk');
    await page.waitForSelector('[data-testid="workdesk-search-input"]', { timeout: 15000 });
    
    // Navegar al BPMN Designer
    await page.goto('/bpmn-designer');
    await page.waitForSelector('[data-testid="bpmn-canvas"]', { timeout: 20000 });
    // Esperar a que el Modeler termine de cargar asincrónicamente
    await page.waitForFunction(() => (window as any).__modelerInstance !== undefined, { timeout: 20000 });

    // Inyectar un BusinessRuleTask usando la API expuesta (CA-E2E)
    await page.evaluate(async () => {
      const xml = `<?xml version="1.0" encoding="UTF-8"?>
        <bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" id="Definitions_1x5" targetNamespace="http://bpmn.io/schema/bpmn">
          <bpmn:process id="Process_1" isExecutable="true">
            <bpmn:businessRuleTask id="Task_1" />
          </bpmn:process>
          <bpmndi:BPMNDiagram id="BPMNDiagram_1">
            <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1">
              <bpmndi:BPMNShape id="Task_1_di" bpmnElement="Task_1">
                <dc:Bounds x="160" y="120" width="100" height="80" />
              </bpmndi:BPMNShape>
            </bpmndi:BPMNPlane>
          </bpmndi:BPMNDiagram>
        </bpmn:definitions>`;
      await (window as any).__modelerInstance.importXML(xml);
    });
    
    // Asegurar selección del BusinessRuleTask inyectado
    await page.click('[data-type="bpmn:BusinessRuleTask"]');
    // Verificar que el sidebar muestra el dropdown DMN
    const dmnDropdown = page.locator('select:has(option:text("— Seleccionar tabla DMN —"))');
    await expect(dmnDropdown).toBeVisible({ timeout: 10000 });
    
    // Verificar que el dropdown tiene opciones (tablas DMN del seed)
    const options = dmnDropdown.locator('option');
    expect(await options.count()).toBeGreaterThan(1); // Al menos "— Seleccionar —" + 1 DMN
  });
});
