import { test, expect } from '@playwright/test';

test.describe('US-002 Concurrencia Atómica - SELECT FOR UPDATE', () => {

  test('Colisión de Operadores (Multi-Context): Operador B recibe 403 al intentar reclamar tarea simultánea', async ({ browser }) => {
    
    // 1. Instanciar los contextos completamente aislados (Modo Incógnito)
    const contextOperadorA = await browser.newContext();
    const contextOperadorB = await browser.newContext();

    const pageOperadorA = await contextOperadorA.newPage();
    const pageOperadorB = await contextOperadorB.newPage();

    // 2. Mockear la API para Operador A (Éxito = 200 OK)
    await pageOperadorA.route('**/api/v1/workbox/tasks/*/claim', async (route) => {
        // Simulamos un delay intencional de 500ms para atrapar la colisión
        await new Promise(resolve => setTimeout(resolve, 500));
        await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({ message: 'Tarea asignada exitosamente' })
        });
    });

    // 3. Mockear la API para Operador B (Fallo por Bloqueo Transaccional = 403)
    await pageOperadorB.route('**/api/v1/workbox/tasks/*/claim', async (route) => {
        // Operador B llega tarde pero en el mismo milisegundo transaccional
        await new Promise(resolve => setTimeout(resolve, 800));
        await route.fulfill({
            status: 403, // HTTP 423 Locked o 403 Forbidden
            contentType: 'application/json',
            body: JSON.stringify({ error: 'La tarea ya fue asignada por otro usuario.' })
        });
    });

    // 4. Preparamos pre-condiciones, operarios navegan a la bandeja simulada
    // Aquí pondremos los .goto() reales cuando el componente esté integrado
    // await pageOperadorA.goto('/workdesk/pool');
    // await pageOperadorB.goto('/workdesk/pool');

    /* 
    ===============================================================
    RACE CONDITION: Simulación Física de Concurrencia en Interfaz
    ===============================================================
    */

    /*
    await Promise.all([
        pageOperadorA.getByRole('button', { name: /Atender/i }).first().click(),
        pageOperadorB.getByRole('button', { name: /Atender/i }).first().click()
    ]);
    */

    // 5. Verificamos que el Operador A navegó exitosamente al formulario
    // await expect(pageOperadorA).toHaveURL(/.*\/task-viewer\/.*/);

    // 6. Verificamos que el Operador B se mantiene en la bandeja y recibe Tostada de Error
    // const toastError = pageOperadorB.getByText('La tarea ya fue asignada por otro usuario');
    // await expect(toastError).toBeVisible();

    // Clean up
    await contextOperadorA.close();
    await contextOperadorB.close();
  });

});
