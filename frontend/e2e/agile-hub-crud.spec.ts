import { test, expect } from '@playwright/test';

test.describe('US-030 Hub Ágil - Operaciones CRUD Basales', () => {

  const projectId = 'PROJ-AGILE';

    test.beforeEach(async ({ page }) => {
    page.on('console', msg => {
        if (msg.type() === 'error') console.log('PAGE ERROR LOG:', msg.text());
        else console.log('PAGE LOG:', msg.text());
    });
    page.on('pageerror', error => console.log('PAGE UNCAUGHT ERROR:', error.message));
    page.on('response', response => { 
        if(response.status() === 401) console.log('401 ON URL:', response.url());
        if(response.status() === 404) console.log('404 ON URL:', response.url());
    });

    // Zero Trust: Mockeamos el project para garantizar vista
    await page.route(`**/api/v1/projects/${projectId}/board`, async route => {
      // Inicia vacío para CA-2
      await route.fulfill({ status: 200, json: { 
        project: { id: projectId, key: 'PRJ', name: 'Agile Project', status: 'ACTIVE' },
        sprints: [], backlogItems: [] 
      }});
    });

    await page.route(`**/api/v1/auth/effective-roles`, async route => {
      await route.fulfill({ status: 200, json: ['ROLE_SUPER_ADMIN'] });
    });

    await page.route(`**/api/v1/users/me/menu-layout`, async route => {
      await route.fulfill({ status: 200, json: [] });
    });

    await page.route(`**/api/v1/users*`, async route => {
      await route.fulfill({ status: 200, json: [ 
        { userId: 'usr-1', name: 'Alfonso QA', email: 'alfonso@qa.com' }
      ] });
    });

    await page.goto(`/admin/projects/agile-hub/${projectId}`);

    // Expandir el sidebar para evitar que la UI quede oculta/colapsada visualmente
    const toggleBtn = page.locator('button.shrink-0.p-1.rounded-md').first();
    if (await toggleBtn.isVisible()) {
      await toggleBtn.click();
    }
  });

  test('Inicia vacío y crea satisfactoriamente desde panel lateral (CA-2, CA-3)', async ({ page }) => {
    // Validar estado vacío
    console.log('CURRENT URL IS:', page.url());
    console.log(await page.content());
    await expect(page.getByText('No hay tareas en el Backlog')).toBeVisible({ timeout: 5000 });

    // Invocar Creación
    const btnCrear = page.getByRole('button', { name: /\+ Nueva Tarea/i });
    await btnCrear.click();

    // Validar visualización de slider panel
    const slidePanel = page.getByRole('complementary', { name: /Crear Nueva Tarea/i });
    await expect(slidePanel).toBeVisible();

    // Digitación de Atributos Básicos
    await slidePanel.getByLabel(/Título/i).fill('Migrar Login a Keycloak');
    await slidePanel.locator('textarea').fill('Esta tarea comprende...');

    // Interceptar la petición de creación POST
    await page.route(`**/api/v1/projects/${projectId}/agile/tasks`, async route => {
      await route.fulfill({ status: 201, json: { id: 'AT-100', title: 'Migrar Login a Keycloak' } });
    });

    // Guardar (z-index of panel is now higher than Botón de Fuga)
    await slidePanel.getByRole('button', { name: /Guardar/i }).click();

    // Verificación (usamos toBeAttached por quirks del RecycleScroller en headless)
    await expect(page.getByText('Migrar Login a Keycloak')).toBeAttached();
    await expect(slidePanel).toBeHidden();
  });
  
  test('Destrucción forense de tarjeta con alerta y hard delete (CA-4)', async ({ page }) => {
     // Modificamos el route para que sí venga una pre-existente local a este test
     await page.route(`**/api/v1/projects/${projectId}/board`, async route => {
      await route.fulfill({ status: 200, json: { 
          project: { id: projectId, key: 'PRJ', name: 'Agile Project', status: 'ACTIVE' },
          sprints: [], 
          backlogItems: [{ id: 'AT-100', title: 'Tarea Pronta a Eliminar', status: 'TO_DO', assignees: [], tags: [] }] 
      }});
    });
    
    await page.goto(`/admin/projects/agile-hub/${projectId}`);

    const grillaRow = page.getByText('Tarea Pronta a Eliminar');
    await expect(grillaRow).toBeAttached();

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
    await expect(grillaRow).not.toBeAttached();
  });
});
