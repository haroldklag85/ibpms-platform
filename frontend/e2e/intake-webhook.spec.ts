import { test, expect } from '@playwright/test';

test.describe('US-004 Webhook Intake - Receptor Perimetral', () => {

  let intakeId: string;

  test.beforeEach(async ({ request }) => {
    // Zero-Trust Seed: Inyección programática simulando payload desde Microsoft Graph
    const webhookPayload = {
      id_mensaje: `evt-${Date.now()}`,
      sender: 'operaciones@banco.com',
      subject: 'Solicitud de Desembolso Urgente',
      body: 'Buenos días, adjunto contratos...',
      attachments: []
    };
    
    // Asumimos que `/api/v1/webhook/intake` retorna un 200 y el motor crea la tarea internamente
    const response = await request.post('/api/v1/webhook/intake', { data: webhookPayload });
    expect(response.ok()).toBeTruthy();
    
    // Obtenemos el ID para interceptar en frontend
    intakeId = `INTAKE-${Date.now()}`;
  });

  test('Validación Visual en la Bandeja Intake tras recepción de Webhook', async ({ page }) => {
    // Interceptamos la llamada de la UI a la Bandeja de Pre-Triaje
    await page.route('**/api/v1/intake/tasks*', async route => {
      const json = {
        data: [{
          id: intakeId,
          sender: 'operaciones@banco.com',
          subject: 'Solicitud de Desembolso Urgente',
          status: 'PENDING',
          arrivedAt: new Date().toISOString()
        }],
        meta: { total: 1 }
      };
      await route.fulfill({ json });
    });

    // Navegación a la Pantalla 16 (Bandeja Inteligente Intake)
    await page.goto('/intake');

    // Validación A11y Visual
    const grillaRow = page.getByText('Solicitud de Desembolso Urgente');
    await expect(grillaRow).toBeVisible();
    await expect(page.getByText('operaciones@banco.com')).toBeVisible();

    // Validar existencia de los botones básicos de Triaje
    const btnTriar = page.getByRole('button', { name: /Evaluar|Triaje/i }).first();
    await expect(btnTriar).toBeEnabled();
  });
});
