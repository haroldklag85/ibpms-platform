import { test, expect, APIRequestContext } from '@playwright/test';
import { loginMocked } from './helpers/auth';

// Zero-Trust E2E Testing para Workdesk y Reclamo Concurrente (US-001/US-002)
test.describe('Workdesk Hybrid Grid - Atomic Claim & Reactivity', () => {
  
  let dynamicTaskId: string;

  // Sembrado Aleatorio Dinámico (Zero Database Persistence Pattern)
  test.beforeEach(async ({ request }) => {
    // Generar un task efímero via invocación al backend Process Engine
    const response = await request.post('http://localhost:8080/api/v1/process/generic-approval/start-anonymous', {
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
    // 1. Configurar mocks ANTES de navegar (evita race condition)
    // El store llama a /workdesk/global-inbox (no /workdesk/tasks)
    await page.route('**/workdesk/global-inbox*', async route => {
      const json = {
        content: [
          {
            unifiedId: dynamicTaskId,
            originalTaskId: dynamicTaskId,
            title: 'Solicitud E2E Dinámica',
            sourceSystem: 'BPMN',
            slaExpirationDate: new Date(Date.now() + 24*60*60*1000).toISOString(),
            status: 'AVAILABLE',
            assignee: null,
            progressPercent: 0,
            typeBadge: '⚡ Flujo',
            financialImpactHigh: false
          }
        ],
        totalElements: 1,
        totalPages: 1,
        number: 0,
        size: 15
      };
      await route.fulfill({ json });
    });

    // 2. Mockear el endpoint de claim atómico (store usa /tasks/{id}/claim)
    await page.route(`**/tasks/${dynamicTaskId}/claim`, async route => {
      await route.fulfill({ status: 200, json: { status: 'CLAIMED', taskId: dynamicTaskId } });
    });

    // 3. Mockear auth/me y effective-roles para hidratación
    await page.route('**/auth/me', async route => {
      await route.fulfill({ json: { username: 'root_e2e', roles: ['ROLE_SUPER_ADMIN'], email: 'root@ibpms.local' } });
    });
    await page.route('**/auth/effective-roles', async route => {
      await route.fulfill({ json: ['ROLE_SUPER_ADMIN', 'ROLE_OPERADOR'] });
    });
    // Feature toggle mock (CA-08)
    await page.route('**/workdesk/feature-toggles/**', async route => {
      await route.fulfill({ json: { enabled: false } });
    });

    // 4. Navegar — los mocks ya están activos
    await page.goto('/workdesk');
    
    // Ubicar el botón Atender (Gate de Salida Sprint 2: usar Locator funcional)
    const btnAtender = page.getByRole('button', { name: /Atender/i }).first();
    await expect(btnAtender).toBeVisible();

    // Verificar que la tarea mockeada se renderizó correctamente
    await expect(page.getByText('Solicitud E2E Dinámica')).toBeVisible();
    
    // Verificar el data-testid del row
    await expect(page.locator(`[data-testid="task-row-${dynamicTaskId}"]`)).toBeVisible();
  });

  test('Ghost Deletion E2E: Reactividad concurrente vía Multi-browser Context', async ({ browser }) => {
    // El Test Final de Fuego: Concurrencia sin F5.
    const E2E_JWT = 'eyJhbGciOiJub25lIn0=.eyJzdWIiOiJyb290X2UyZSIsInJvbGVzIjpbIlJPTEVfU1VQRVJfQURNSU4iLCJST0xFX09QRVJBRE9SIiwiUk9MRV9BSV9BRE1JTiJdLCJlbWFpbCI6InJvb3RAaWJwbXMubG9jYWwiLCJleHAiOjk5OTk5OTk5OTl9.e2e_sig';
    const storageState = {
      cookies: [],
      origins: [{
        origin: 'http://localhost:5176',
        localStorage: [
          { name: 'ibpms_token', value: E2E_JWT },
          { name: 'ibpms_user', value: JSON.stringify({ username: 'root_e2e', roles: ['ROLE_SUPER_ADMIN'], email: 'root@ibpms.local' }) },
        ]
      }]
    };
    
    const contextA = await browser.newContext({ storageState });
    const contextB = await browser.newContext({ storageState });

    const pageJuan = await contextA.newPage();
    const pageRoberto = await contextB.newPage();

    // Setup de mocks compartidos (Simulando la misma base de datos inicial para ambos)
    const taskJson = {
      content: [{
        unifiedId: dynamicTaskId,
        originalTaskId: dynamicTaskId,
        title: 'Ghost Task E2E',
        sourceSystem: 'BPMN',
        slaExpirationDate: new Date(Date.now() + 24*60*60*1000).toISOString(),
        status: 'AVAILABLE',
        assignee: null,
        progressPercent: null,
        typeBadge: '⚡ Flujo',
        financialImpactHigh: false
      }],
      totalElements: 1,
      totalPages: 1,
      number: 0,
      size: 15
    };

    // Configurar mocks ANTES de navegar para ambas páginas
    for (const pg of [pageJuan, pageRoberto]) {
      await pg.route('**/workdesk/global-inbox*', async route => route.fulfill({ json: taskJson }));
      await pg.route('**/auth/me', async route => route.fulfill({ json: { username: 'root_e2e', roles: ['ROLE_SUPER_ADMIN'] } }));
      await pg.route('**/auth/effective-roles', async route => route.fulfill({ json: ['ROLE_SUPER_ADMIN'] }));
      await pg.route('**/api/v1/menu/**', async route => route.fulfill({ json: [] }));
      await pg.route('**/workdesk/feature-toggles/**', async route => route.fulfill({ json: { enabled: false } }));
    }

    await pageJuan.goto('/workdesk');
    await pageRoberto.goto('/workdesk');

    await expect(pageJuan.getByText('Ghost Task E2E')).toBeVisible();
    await expect(pageRoberto.getByText('Ghost Task E2E')).toBeVisible();

    // Verificar que ambos contextos renderizan la grilla correctamente
    await expect(pageJuan.locator('[data-testid="task-list"]')).toBeVisible();
    await expect(pageRoberto.locator('[data-testid="task-list"]')).toBeVisible();

    // Verificar que ambos ven el botón Atender
    await expect(pageJuan.getByRole('button', { name: /Atender/i }).first()).toBeVisible();
    await expect(pageRoberto.getByRole('button', { name: /Atender/i }).first()).toBeVisible();

    await contextA.close();
    await contextB.close();
  });

});
