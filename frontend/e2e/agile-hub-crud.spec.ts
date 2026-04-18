import { test, expect } from '@playwright/test';

test.describe('US-030 Hub Ágil - Operaciones CRUD Basales', () => {

  const projectId = 'PROJ-AGILE';

  test.beforeEach(async ({ page, request }) => {
    // Zero Trust: Mockeamos el project para garantizar vista
    await page.route(`**/api/v1/projects/${projectId}/agile/tasks*`, async route => {
      // Inicia vacío para CA-2
      await route.fulfill({ status: 200, json: { data: [] } });
    });

    await page.goto(`/projects/${projectId}/agile-hub`);
  });

  test('Inicia vacío y crea satisfactoriamente desde panel lateral (CA-2, CA-3)', async ({ page }) => {
    // Validar estado vacío
    await expect(page.getByText('No hay tareas en el Backlog')).toBeVisible();

    // Invocar Creación
    const btnCrear = page.getByRole('button', { name: /\+ Nueva Tarea/i });
    await btnCrear.click();

    // Validar visualización de slider panel
    const slidePanel = page.getByRole('complementary', { name: /Crear Nueva Tarea/i });
    await expect(slidePanel).toBeVisible();

    // Digitación de Atributos Básicos
    await slidePanel.getByLabel(/Título/i).fill('Migrar Login a Keycloak');
    // En Quill o editores enriquecidos, a veces ubicar el 'textbox' por rol funciona 
    await slidePanel.getByRole('textbox').nth(1).fill('Esta tarea comprende...');

    // Interceptar la petición de creación POST
    await page.route(`**/api/v1/projects/${projectId}/agile/tasks`, async route => {
      await route.fulfill({ status: 201, json: { id: 'AT-100', title: 'Migrar Login a Keycloak' } });
    });

    // Guardar
    await slidePanel.getByRole('button', { name: /Guardar/i }).click();

    // Verificación
    await expect(page.getByText('Migrar Login a Keycloak')).toBeVisible();
    await expect(slidePanel).toBeHidden();
  });
  
  test('Destrucción forense de tarjeta con alerta y hard delete (CA-4)', async ({ page }) => {
     // Modificamos el route para que sí venga una pre-existente local a este test
     await page.route(`**/api/v1/projects/${projectId}/agile/tasks*`, async route => {
      await route.fulfill({ status: 200, json: { data: [{id: 'AT-100', title: 'Tarea Pronta a Eliminar', status: 'TO_DO' }] } });
    });
    
    await page.goto(`/projects/${projectId}/agile-hub`);

    const grillaRow = page.getByText('Tarea Pronta a Eliminar');
    await expect(grillaRow).toBeVisible();

    // Click en eliminar
    await page.getByRole('button', { name: /Eliminar/i }).first().click();

    const dialog = page.getByRole('dialog', { name: /Eliminar Tarea/i });
    await expect(dialog).toBeVisible();

    await page.route(`**/api/v1/projects/${projectId}/agile/tasks/AT-100`, async route => {
      await route.fulfill({ status: 204 });
    });

    // Confirmación
    await dialog.getByRole('button', { name: /Confirmar/i }).click();

    // Comprobación de UI: El ticket ya no está
    await expect(grillaRow).toBeHidden();
  });
});
