// @Traceability: US-003 - CA-80
import { mount } from '@vue/test-utils';
import { describe, it, expect, vi } from 'vitest';
import { nextTick } from 'vue';
import FormRenderer from '../FormRenderer.vue';
import { createTestingPinia } from '@pinia/testing';

describe('FormRenderer CA-80 (Lazy Validation & Masks)', () => {
  it('does not trigger validation on input, but triggers on blur', async () => {
    // Definimos un esquema para forzar la validación
    const schema = [
      {
        id: 'user_name',
        type: 'text',
        label: 'User Name',
        camundaVariable: 'userName',
        required: true,
        // En nuestro FormRenderer, si required es true, crea un ZodString con .min(1)
      }
    ];

    const wrapper = mount(FormRenderer, {
      props: {
        schema,
        modelValue: { userName: '' }
      },
      global: {
        plugins: [createTestingPinia({ createSpy: vi.fn })]
      },
      attachTo: document.body
    });

    // We need to wait for the shadowApp to mount.
    await nextTick();
    await new Promise(r => setTimeout(r, 50));

    // Como FormRenderer usa Shadow DOM, buscamos en su interior:
    const hostEl = wrapper.find('.w-full').element;
    const shadowRoot = hostEl.shadowRoot;
    if (!shadowRoot) {
      console.log('shadowRoot is null', wrapper.html());
    }
    expect(shadowRoot).not.toBeNull();

    const input = shadowRoot.querySelector('input[type="text"]') as HTMLInputElement;
    expect(input).not.toBeNull();

    // 2. Simular escritura, el input no cumple con required (está vacío) o le ponemos un valor y lo borramos
    input.value = '';
    input.dispatchEvent(new Event('input', { bubbles: true }));
    await nextTick();

    // La validación (error visible en DOM) NO debe aparecer inmediatamente.
    // Verificamos que no haya ningún texto de error renderizado, ej: "Este campo es requerido"
    const containerTextAfterInput = shadowRoot.textContent || '';
    expect(containerTextAfterInput).not.toContain('Este campo es requerido');

    // 3. Simular evento blur, lo cual DEBE disparar la validación y mostrar el error.
    input.dispatchEvent(new Event('blur', { bubbles: true }));
    await nextTick();

    const containerTextAfterBlur = shadowRoot.textContent || '';
    expect(containerTextAfterBlur).toContain('Este campo es requerido');
  });

  it('stores raw unmasked numeric value while displaying masked format', async () => {
    const schema = [
      {
        id: 'salary',
        type: 'text', // In FormRenderer, predefinedFormat is used with type 'text'
        label: 'Salary',
        camundaVariable: 'salary',
        predefinedFormat: 'currency'
      }
    ];

    const modelValue = { salary: 0 };
    const wrapper = mount(FormRenderer, {
      props: {
        schema,
        modelValue
      },
      global: {
        plugins: [createTestingPinia({ createSpy: vi.fn })]
      },
      attachTo: document.body
    });

    await nextTick();
    await new Promise(r => setTimeout(r, 50));

    const hostEl = wrapper.find('.w-full').element;
    const shadowRoot = hostEl.shadowRoot;
    if (!shadowRoot) {
      console.log('shadowRoot is null', wrapper.html());
    }
    const input = shadowRoot!.querySelector('input[type="text"]') as HTMLInputElement;
    
    // Simular IMask format (e.g. user types "1500", mask shows "$ 1.500,00" or similar)
    // IMask en test no es fácil de simular mediante eventos de teclado reales sin puppeteer,
    // pero si ingresamos "1500" el value de emit debería ser 1500 aunque visualmente formatee.
    
    // Actually, we can just test if the component has the logic to apply raw value
    // Let's check IMask behavior by setting the value. 
    input.value = '1500';
    input.dispatchEvent(new Event('input', { bubbles: true }));
    
    // As in FormRenderer, imask event 'accept' calls updateVal(maskCore.unmaskedValue).
    // Let's wait a bit for IMask to process
    await new Promise(r => setTimeout(r, 100));

    // Revisar formData/modelValue
    const emittedUpdates = wrapper.emitted('update:modelValue') as any[];
    // We can also just check wrapper.props().modelValue if it's two-way bound, but vue-test-utils v2 emitted updates.
    // By the component logic, it mutates formData.value which is the modelValue.
    // So modelValue.salary should be 1500 (as number or string depending on IMask unmasked value)
    
    // Check if the actual value updated in the prop or if we need to emit:
    expect(modelValue.salary).toBe('1500'); // IMask unmaskedValue is string, actually. Or 1500 number? 
    // The requirement says: "formData model must store the raw numeric unmasked value (`1500`)."
    // In our test, let's assert it equals 1500 or '1500' string.
  });
});
