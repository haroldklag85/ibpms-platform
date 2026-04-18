import { test, expect } from '@playwright/test';

test.describe('US-004 Webhook Intake - Aprobación Humana', () => {

  const intakeId = 'INTAKE-MOCK-123';

  test.beforeEach(async ({ page }) => {
    // Intercepción para la pre-visualización de un caso Intake Específico
    await page.route(`**/api/v1/intake/tasks/${intakeId}`, async route => {
      await route.fulfill({
        status: 200,
        json: {
          id: intakeId,
          sender: 'cliente@corporativo.com',
          subject: 'Contrato de Adhesión',
          body: 'Adjunto contrato firmado',
          status: 'PENDING'
        }
      });
    });

    // Mock para obtener el combo box de Procesos BPMN disponibles (Canalización)
    await page.route('**/api/v1/process/definitions', async route => {
      await route.fulfill({
        status: 200,
        json: {
          data: [
            { id: 'onboarding-process', name: 'High-Touch Onboarding' },
            { id: 'legal-review', name: 'Revisión Legal Documental' }
          ]
        }
      });
    });

    await page.goto(`/intake/evaluar/${intakeId}`);
  });

  test('Operario canaliza y aprueba inyección al motor BPMN', async ({ page }) => {
    // Validar pre-visualización del correo
    await expect(page.getByText('Contrato de Adhesión')).toBeVisible();

    // Flujo de Aprobación
    await page.getByRole('button', { name: /Aprobar Ingesta/i }).click();

    // Interacción con ComboBox para seleccionar "Revisión Legal Documental"
    const selectProceso = page.getByLabel(/Tipo de Proceso/i);
    // Expandimos el Dropdown
    await selectProceso.click();
    await page.getByRole('option', { name: 'Revisión Legal Documental' }).click();

    // Mock del POST de disparo BPMN
    await page.route(`**/api/v1/intake/tasks/${intakeId}/approve`, async route => {
      await route.fulfill({
        status: 200,
        json: { success: true, processInstanceId: 'PI-999', targetProcess: 'legal-review' }
      });
    });

    // Click en Crear Caso Oficial
    await page.getByRole('button', { name: /Crear Caso/i }).click();

    // Validación de Redireccionamiento / Éxito
    await expect(page.getByText(/Caso Inyectado Exitosamente/i)).toBeVisible();
    await expect(page).toHaveURL(/.*\/workdesk.*/); 
  });
});
