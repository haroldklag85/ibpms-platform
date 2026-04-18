import { test, expect } from '@playwright/test';

test.describe('US-004 Webhook Intake - SLA Indicador de Emergencia', () => {

  const intakeId = 'INTAKE-SLA-TEST';

  test.beforeEach(async ({ page }) => {
    // Interceptamos la fecha en un estado crítico (Eg. entró hace muchas horas y no ha sido tocado)
    await page.route('**/api/v1/intake/tasks*', async route => {
      // Creamos una fecha que deliberadamente expira en 5 segundos artificiales o que ya expiró 
      // para forzar el badge ROJO del CA-16.
      
      const json = {
        data: [{
          id: intakeId,
          sender: 'ceo@empresa.com',
          subject: 'Demanda Urgente',
          status: 'PENDING',
          slaExpiration: new Date(Date.now() - 3600000).toISOString() // Hace 1 hora (Vencido)
        }],
        meta: { total: 1 }
      };
      await route.fulfill({ json });
    });

    await page.goto('/intake');
  });

  test('Validación de mutación semáforo SLA de Tarea Pre-Triaje Vencida', async ({ page }) => {
    // Buscamos la fila de la demanda
    const filaDemanda = page.getByText('Demanda Urgente', { exact: false });
    await expect(filaDemanda).toBeVisible();

    // El color o el icono del Semáforo SLA debería ser Rojo o indicar Peligro.
    // Dependiendo del framework UI, validaremos texto o clases de Severidad.
    const badgeVencido = page.getByText(/Vencida/i).or(page.getByText(/Overdue/i));
    await expect(badgeVencido).toBeVisible();
    
    // Aseguramos que el operario pueda reaccionar ante este indicador entrando al registro
    await page.getByRole('button', { name: /Evaluar/i }).first().click();
    await expect(page).toHaveURL(/.*\/intake\/evaluar\/.*/);
  });
});
