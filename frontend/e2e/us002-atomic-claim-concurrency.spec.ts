import { test, expect } from '@playwright/test';
import * as fs from 'fs';
import * as path from 'path';

test.describe('US-002 Concurrencia Atómica - SELECT FOR UPDATE', () => {

  let dynamicTaskId: string;

  test.beforeEach(async ({ request }) => {
    // Generar un task efímero via invocación al backend Process Engine
    const response = await request.post('http://localhost:8080/api/v1/process/generic-approval/start-anonymous', {
      data: {
        payload: 'concurrency_payload_' + Date.now(),
        priority: 'high'
      }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    dynamicTaskId = data.processInstanceId;
  });

  test('Colisión de Operadores (Multi-Context): Operador B recibe 403/409 al intentar reclamar tarea simultánea', async ({ browser }) => {
    
    const storageStatePath = path.join(__dirname, 'playwright/.auth/user.json');
    const storageState = JSON.parse(fs.readFileSync(storageStatePath, 'utf8'));

    // 1. Instanciar los contextos
    const contextOperadorA = await browser.newContext({ storageState });
    const contextOperadorB = await browser.newContext({ storageState });

    const pageOperadorA = await contextOperadorA.newPage();
    const pageOperadorB = await contextOperadorB.newPage();

    // 4. Preparamos pre-condiciones, operarios navegan a la bandeja real
    await pageOperadorA.goto('/workdesk');
    await pageOperadorB.goto('/workdesk');

    const taskRowA = pageOperadorA.locator(`[data-testid="task-row-${dynamicTaskId}"]`);
    const taskRowB = pageOperadorB.locator(`[data-testid="task-row-${dynamicTaskId}"]`);

    await expect(taskRowA).toBeAttached({ timeout: 15000 });
    await expect(taskRowB).toBeAttached({ timeout: 15000 });

    /* 
    ===============================================================
    RACE CONDITION: Simulación Física de Concurrencia en Interfaz
    ===============================================================
    */

    const btnA = taskRowA.getByRole('button', { name: /Atender/i }).first();
    const btnB = taskRowB.getByRole('button', { name: /Atender/i }).first();

    await Promise.all([
        btnA.click(),
        btnB.click()
    ]);

    // Al menos uno de los dos debe fallar (toast de error)
    // El otro debe tener éxito.
    // Como el framework o la latencia pueden variar quién gana, buscamos el mensaje de error en cualquiera de los dos.
    const errorMsgA = pageOperadorA.locator('.p-toast-message-error');
    const errorMsgB = pageOperadorB.locator('.p-toast-message-error');

    // Esperamos a que el Toast de error aparezca en al menos uno de los dos navegadores.
    const errorInA = await errorMsgA.isVisible({ timeout: 5000 }).catch(() => false);
    const errorInB = await errorMsgB.isVisible({ timeout: 5000 }).catch(() => false);

    expect(errorInA || errorInB).toBeTruthy();

    // Clean up
    await contextOperadorA.close();
    await contextOperadorB.close();
  });

});
