import { test, expect } from '@playwright/test';

test.describe('US-004 Webhook Intake - Rechazo y Prevención de Abuso', () => {

  const intakeId = 'INTAKE-MOCK-999';

  test.beforeEach(async ({ page }) => {
    await page.route(`**/api/v1/intake/tasks/${intakeId}`, async route => {
      await route.fulfill({
        status: 200,
        json: {
          id: intakeId,
          sender: 'spam@robot.com',
          subject: 'Oferta Viagra Especial',
          body: 'Compra barata.',
          status: 'PENDING'
        }
      });
    });

    await page.goto(`/intake/evaluar/${intakeId}`);
  });

  test('Rechazo exige justificación obligatoria antes de cancelar', async ({ page }) => {
    // Intentar Rechazar directamente
    const btnRechazar = page.getByRole('button', { name: /Rechazar Petición/i });
    await btnRechazar.click();

    // Validar Dialog/Formulario de Justificación Reactivo (Zod/Vuelidate)
    const modalJustificacion = page.getByRole('dialog', { name: /Motivo del Rechazo/i });
    await expect(modalJustificacion).toBeVisible();

    // Intentar confirmar sin escribir arroja alerta local de campo obligatorio
    const btnConfirmar = modalJustificacion.getByRole('button', { name: /Confirmar Rechazo/i });
    await btnConfirmar.click();
    
    // Validación Zod ("Requerido")
    const errorText = modalJustificacion.getByText(/Requerido/i).or(page.getByText(/Este campo es obligatorio/i));
    await expect(errorText).toBeVisible();

    // Rellenar justificación
    await modalJustificacion.getByLabel(/Motivo/i).fill('Correo se considera SPAM o Phishing');

    // Mock backend reject request
    await page.route(`**/api/v1/intake/tasks/${intakeId}/reject`, async route => {
      await route.fulfill({ status: 200, json: { status: 'CANCELLED' } });
    });

    // Enviar finalmente
    await btnConfirmar.click();

    // Validar cerrado del modal y redirección al intake grid
    await expect(modalJustificacion).toBeHidden();
    await expect(page).toHaveURL(/.*\/intake/);
  });
});
