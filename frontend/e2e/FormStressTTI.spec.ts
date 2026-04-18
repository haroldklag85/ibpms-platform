import { test, expect } from '@playwright/test';
import * as fs from 'fs';

// Utils para generar un payload masivo
function generateMassiveFormMock(fieldCount: number, gridCount: number) {
    const layoutConfig: any[] = [];

    // Campos individuales normales (+250)
    for (let i = 0; i < fieldCount; i++) {
        layoutConfig.push({ 
            id: `FIELD_${i}`,
            type: 'text', 
            label: `Campo de prueba ${i}`,
            camundaVariable: `campo_${i}`,
            required: false,
            zodType: 'string'
        });
    }

    // Grids Repetibles
    for (let g = 0; g < gridCount; g++) {
        layoutConfig.push({ 
            id: `GRID_${g}`,
            type: 'field_array', 
            label: `Grid Repetible ${g}`,
            camundaVariable: `grid_${g}`,
            required: false,
            zodType: 'array',
            children: [
               { id: `COL_${g}_A`, type: 'text', label: 'Columna A', camundaVariable: 'colA' }
            ]
        });
    }

    return {
        form_id: 'FORM-STRESS-999',
        name: 'Formulario TTI Stress',
        version: 1,
        schemaVariables: layoutConfig // Importante: FormDesigner mapea contra esto
    };
}

test.describe('US-003 Remediation: CA-90 - E2E Form TTI Profiling', () => {
    
    test('El renderizado masivo (250+ fields, 3 grids) DOM TTI debe ser <= 3.0 segundos', async ({ page }) => {
        
        // 1. Interceptar peticiones de autenticación y forms
        await page.route('**/api/v1/forms/FORM-STRESS-999', async route => {
            const json = generateMassiveFormMock(260, 3);
            await route.fulfill({ json });
        });

        // Mock empty versions to prevent Vue v-for crash
        await page.route('**/api/v1/forms/FORM-STRESS-999/versions', async route => {
            await route.fulfill({ json: [] });
        });

        // Fake Login para bypass JWT
        await page.route('**/api/v1/auth/me', async route => {
            await route.fulfill({ json: { roles: ['ROLE_SUPER_ADMIN'], username: 'pqa_mock' } });
        });

        // Debug route requests
        page.on('request', request => console.log('>>', request.method(), request.url()));
        page.on('response', response => console.log('<<', response.status(), response.url()));
        page.on('console', msg => console.log('[BROWSER]', msg.type(), msg.text()));
        page.on('pageerror', error => console.log('[BROWSER ERROR]', error.message));

        const massSchemaStr = JSON.stringify(generateMassiveFormMock(260, 3).schemaVariables);

        await page.addInitScript(({ massSchemaStr }) => {
            window.localStorage.setItem('ibpms_token', 'fake.jwt.token');
            window.localStorage.setItem('ibpms_user', JSON.stringify({ roles: ['ROLE_SUPER_ADMIN'] }));
            window.sessionStorage.setItem('ibpms_token', 'fake.jwt.token'); // Por si acaso
            window.localStorage.setItem('form_draft_ca85_modeler', massSchemaStr);
        }, { massSchemaStr });

        // 2. Medición TTI - Previo a navegación
        const startTime = Date.now();

        // 3. Ejecutar Montado en el navegador (Vue VueRouter Load)
        await page.goto('/admin/modeler/forms/designer');

        // 4. Capturar Screenshot inicial
        await page.screenshot({ path: 'test-results/debug-tti-load.png' });
        // Wait un poco para que Vue monte
        await page.waitForTimeout(2000);
        await page.screenshot({ path: 'test-results/debug_render.png', fullPage: true });

        // Evaluamos TTI
        const htmlContent = await page.content();
        fs.writeFileSync('test-results/dom_snapshot.html', htmlContent);

        // Esperamos el label del primer y el último campo
        await expect(page.locator('text=Campo de prueba 0').first()).toBeVisible({ timeout: 5000 });
        await expect(page.locator('text=Campo de prueba 259').first()).toBeVisible({ timeout: 5000 });
        await expect(page.locator('text=Grid Repetible 2').first()).toBeVisible({ timeout: 5000 });

        const endTime = Date.now();
        const tti = endTime - startTime;

        console.log(`[TTI PROFILER] Form Rendering interactivo logrado en: ${tti}ms`);

        // 5. Aserción Arquitectónica (CA-90): TTI no puede exceder 3.0s (3000ms)
        expect(tti, `El Time-to-Interactive de ${tti}ms superó la cota de asfixia permitida (3000ms)`).toBeLessThanOrEqual(3000);
    });

});
