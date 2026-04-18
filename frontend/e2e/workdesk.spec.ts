import { test, expect, APIRequestContext } from '@playwright/test';

// Zero-Trust E2E Testing para Workdesk y Reclamo Concurrente (US-001/US-002)
test.describe('Workdesk Hybrid Grid - Atomic Claim & Reactivity', () => {
  
  let dynamicTaskId: string;

  // Sembrado Aleatorio Dinámico (Zero Database Persistence Pattern)
  test.beforeEach(async ({ request }) => {
    // Generar un task efímero via invocación al backend Process Engine
    const response = await request.post('/api/v1/process/generic-approval/start-anonymous', {
      data: {
        payload: 'test_payload_generated_by_playwright_' + Date.now(),
        priority: 'high'
      }
    });

    if (response.ok()) {
      const data = await response.json();
      dynamicTaskId = data.processInstanceId || `T-${Date.now()}`;
    } else {
      // Fallback seguro si el proceso no está implementado o falla (Graceful degradation en E2E)
      dynamicTaskId = `T-${Date.now()}`;
    }
  });

  test('Atomic Claim: Interceptación 200 OK y Ruteo Exitoso (Happy Path)', async ({ page }) => {
    // 1. Logueo simulado (o acceso directo a dashboard dada la capa UI reactiva)
    await page.goto('/');

    // 2. Mockear el listado inicial si el backend requiere contexto de login que E2E omite.
    // Usamos el id dinámico inyectado en el paso anterior.
    await page.route('**/api/v1/workdesk/tasks*', async route => {
      const json = {
        data: [
          {
            id: dynamicTaskId,
            name: 'Solicitud E2E Dinámica',
            sla_status: 'ACTIVE',
            process_definition: 'generic-approval',
            tenant_id: 'tenant_1',
            progress_percent: 0,
            assignee_id: null
          }
        ],
        meta: { total: 1 }
      };
      await route.fulfill({ json });
    });

    // 3. Mockear el endpoitn de claim atómico
    await page.route(`**/api/v1/workdesk/${dynamicTaskId}/claim`, async route => {
      await route.fulfill({ status: 200, json: { status: 'CLAIMED', taskId: dynamicTaskId } });
    });

    // Validar renderizado de la grilla
    await page.reload();
    
    // Ubicar el botón Atender (Gate de Salida Sprint 2: usar Locator funcional)
    const btnAtender = page.getByRole('button', { name: /Atender/i }).first();
    await expect(btnAtender).toBeVisible();

    // 4. Click y validación de ruteo
    await btnAtender.click();

    // Esperar ruteo a FormDesigner o equivalente basado en router dinámico
    await expect(page).toHaveURL(/.*\/form-designer.*/);
  });

  test('Ghost Deletion E2E: Reactividad concurrente vía Multi-browser Context', async ({ browser }) => {
    // El Test Final de Fuego: Concurrencia sin F5.
    const contextA = await browser.newContext();
    const contextB = await browser.newContext();

    const pageJuan = await contextA.newPage();
    const pageRoberto = await contextB.newPage();

    // Setup de mocks compartidos (Simulando la misma base de datos inicial para ambos)
    const taskJson = {
      data: [{
        id: dynamicTaskId,
        name: 'Ghost Task E2E',
        sla_status: 'WARNING',
        assignee_id: null
      }],
      meta: { total: 1 }
    };

    await pageJuan.route('**/api/v1/workdesk/tasks*', async route => route.fulfill({ json: taskJson }));
    await pageRoberto.route('**/api/v1/workdesk/tasks*', async route => route.fulfill({ json: taskJson }));

    await pageJuan.goto('/');
    await pageRoberto.goto('/');

    await expect(pageJuan.getByText('Ghost Task E2E')).toBeVisible();
    await expect(pageRoberto.getByText('Ghost Task E2E')).toBeVisible();

    // Analista Juan reclama la tarea
    await pageJuan.route(`**/api/v1/workdesk/${dynamicTaskId}/claim`, async route => {
      await route.fulfill({ status: 200, json: { status: 'CLAIMED' } });
    });
    
    // Al reclamar, el frontend local de Juan dispara /claim vía API, pero
    // ¿Como comprobamos que Roberto reacciona? Simulando el Push WebSocket de STOMP hacia Roberto
    await pageJuan.getByRole('button', { name: /Atender/i }).first().click();

    // Simulamos el evento STOMP que recibiría Roberto emitido por el broker en la vida real
    await pageRoberto.evaluate((id) => {
      window.dispatchEvent(new CustomEvent('stomp-task-claimed', { detail: { taskId: id } }));
    }, dynamicTaskId);

    // CRÍTICO: Roberto debe dejar de ver la tarea en < 1 segundo SIN que se llame a reload()
    await expect(pageRoberto.getByText('Ghost Task E2E')).toBeHidden({ timeout: 1000 });

    await contextA.close();
    await contextB.close();
  });

});
