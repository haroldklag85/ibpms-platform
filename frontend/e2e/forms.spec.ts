import { test, expect } from '@playwright/test';

test.describe('Form IDE - Zod Resilience (US-003/US-028)', () => {

  test.beforeEach(async ({ page }) => {
    // Interceptar la carga del esquema inicial
    await page.route('**/api/v1/forms/*', async route => {
      const formSchema = {
        id: 'form-101',
        components: [
          {
            id: 'field_name',
            type: 'text',
            label: 'Nombre Completo',
            validation: {
              required: true,
              minLength: 5
            }
          },
          {
            id: 'field_age',
            type: 'number',
            label: 'Edad',
            validation: {
              min: 18
            }
          }
        ]
      };
      await route.fulfill({ status: 200, json: formSchema });
    });
    
    await page.goto('/form-designer/form-101');
  });

  test('Validación visual inmediata de componentes usando Zod (Payload Forzado)', async ({ page }) => {
    // Rellenamos el formulario de forma errónea
    
    // El locator asume que los inputs están asociados a sus labels accesibles (Buenas prácticas a11y)
    const nameInput = page.getByLabel('Nombre Completo');
    const ageInput = page.getByLabel('Edad');
    
    // Inyectamos valores intencionalmente defectuosos contra la métrica Zod
    await nameInput.fill('Ana'); // < 5 caracteres
    await ageInput.fill('16'); // < 18 min

    // Intentamos emular la pérdida de enfoque o guardado para disparar validación Zod
    await ageInput.blur();
    
    // Dependiendo de cómo se implemente la vista, buscaríamos el mensaje de error de Zod
    // Generalmente Zod renderizará "String must contain at least 5 character(s)"
    // Usaremos Regex genéricos por resiliencia.
    await expect(page.getByText(/5 caracteres/i).or(page.getByText(/String must contain at least 5 character/i))).toBeVisible();
    await expect(page.getByText(/menor a 18/i).or(page.getByText(/Number must be greater than or equal to 18/i))).toBeVisible();
  });

  test('Intercepción de validaciones asíncronas de servidor (Feedback Visual 422)', async ({ page }) => {
    // Simulamos un click en Enviar, donde el FrontEnd (VeeValidate/Zod) pasa, 
    // pero el Servidor devuelve un 422 Strict validation error
    const nameInput = page.getByLabel('Nombre Completo');
    const ageInput = page.getByLabel('Edad');
    
    await nameInput.fill('Harolt Andres'); 
    await ageInput.fill('25'); 
    
    // Interceptamos la llamada al servidor
    await page.route('**/api/v1/form-instances*', async route => {
      await route.fulfill({
        status: 422,
        json: {
          fieldErrors: {
            field_name: 'Dato reportado en lista negra por el servidor'
          }
        }
      });
    });

    await page.getByRole('button', { name: /Guardar|Enviar/i }).click();

    // Validar visualmente la inyección de errores del server post-vuelo en la UI reactiva de Zod
    await expect(page.getByText(/lista negra por el servidor/i)).toBeVisible();
  });

});
