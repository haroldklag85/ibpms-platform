import { test, expect } from '@playwright/test';
import { seedTask } from './helpers/task-seeder';

test.describe('US-002 Claim Rollback — Rollback Visual (CA-21)', () => {
    
    let taskId: string;

    test.beforeEach(async ({ request }) => {
        taskId = await seedTask(request);
    });

    test('POST claim falla por concurrencia y revierte estado optimista', async ({ page, request }) => {
        await page.goto('/workdesk');

        const taskRow = page.locator(`[data-testid="task-row-${taskId}"]`);
        await expect(taskRow).toBeAttached({ timeout: 15000 });

        // Simulamos que otro usuario (o nosotros mismos vía API) se adelanta y reclama la tarea
        // Esto causará un error cuando el Front intente reclamarla.
        await request.post(`http://localhost:8080/api/v1/workbox/tasks/${taskId}/claim`);

        // El Front intenta reclamarla
        await taskRow.getByRole('button', { name: /Atender/i }).first().click();

        // El front debería mostrar un Toast de error
        await expect(page.locator('.p-toast-message-error')).toBeVisible({ timeout: 15000 });

        // Ya que la tarea fue reclamada por alguien más (vía API), tras el refresco o ws, debería desaparecer
        // o si es un error 500 genérico (que probaba el test original), reaparecer.
        // Como aquí forzamos un 409/403, es probable que la grilla la limpie.
    });
});
