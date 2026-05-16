import { test, expect } from '@playwright/experimental-ct-vue';
import DynamicForm from '@/components/forms/DynamicForm.vue';
import { createTestingPinia } from '@pinia/testing';
import { z } from 'zod';

// @Traceability: US-029 - CA-22 - Validando bloqueo de Submit si el Wizard no está completado
test.describe('US-029: Wizard Multi-Etapa Navigation', () => {

  test.use({ viewport: { width: 800, height: 600 } });

  test('Valida que el botón de Completar Tarea NO existe en el paso 1 y se habilita al final', async ({ mount }) => {
    // 1. Configuramos un Schema con múltiples etapas para activar el Wizard
    const mockSchema = {
      title: 'Wizard Testing Form',
      description: 'E2E Testing',
      fields: [
        { key: 'campoA', type: 'string', label: 'Campo A', stage: 'ANALYSIS', required: true },
        { key: 'campoB', type: 'string', label: 'Campo B', stage: 'DECISION', required: true },
        { key: 'campoC', type: 'string', label: 'Campo C', stage: 'EXECUTION', required: true }
      ]
    };

    // 2. Montar el componente
    const component = await mount(DynamicForm, {
      props: {
        schema: mockSchema,
        currentStage: 'ALL'
      }
    });

    // 3. Verificamos que estamos en el primer paso (ANALYSIS)
    await expect(component.locator('text=ANALYSIS').first()).toBeVisible();
    await expect(component.locator('text=Campo A')).toBeVisible();
    
    const submitButton = component.locator('button', { hasText: 'Completar Tarea' });
    await expect(submitButton).toHaveCount(0);

    // Llenamos el primer paso para poder avanzar
    await component.locator('#campoA').fill('Valor A');

    // 4. Navegar al segundo paso (DECISION)
    const nextButton = component.locator('button', { hasText: 'Siguiente' });
    await nextButton.click();
    
    // Verificamos que avanzó
    await expect(component.locator('text=Campo B')).toBeVisible();

    // El botón "Completar Tarea" AÚN NO debe existir en el paso 2
    await expect(submitButton).toHaveCount(0);

    // Llenamos el segundo paso
    await component.locator('#campoB').fill('Valor B');

    // 5. Navegar al último paso (EXECUTION)
    await nextButton.click();

    // Verificamos que avanzó al último paso
    await expect(component.locator('text=Campo C')).toBeVisible();

    // El botón "Completar Tarea" DEBE existir ahora
    await expect(submitButton).toHaveCount(1);
    await expect(submitButton).toBeVisible();

    // Validamos que el botón de Siguiente ya no existe
    await expect(nextButton).toHaveCount(0);
  });

});
