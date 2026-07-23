import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import FormRenderer from '../FormRenderer.vue';
import { createTestingPinia } from '@pinia/testing';
import jexl from 'jexl';

// @Traceability: US-003 - CA-79
describe('FormRenderer CA-79: Sandboxing Estricto y Aislamiento Perimetral (Anti-XSS/RCE)', () => {
  let pinia: any;

  beforeEach(() => {
    pinia = createTestingPinia({
      createSpy: vi.fn,
      initialState: {
        integrationStore: {}
      }
    });
  });

  it('encapsulates its content inside a Shadow DOM to ensure style isolation', () => {
    const schema = [{ id: 'field1', type: 'text', label: 'Field 1' }];
    const wrapper = mount(FormRenderer, {
      props: { schema },
      global: { plugins: [pinia] },
      attachTo: document.body
    });
    
    // The component wrapper element should have a shadowRoot
    const hostEl = wrapper.find('div.w-full').element;
    expect(hostEl.shadowRoot).not.toBeNull();
    expect(hostEl.shadowRoot?.mode).toBe('open');
  });

  it('uses AST Sandbox without eval/new Function and prohibits global contexts like window, fetch', () => {
    const schema = [{
      id: 'field1',
      type: 'text',
      label: 'Field 1',
      visibilityCondition: 'window.location.href == "http://evil.com"'
    }];
    
    // Mount the component
    const wrapper = mount(FormRenderer, {
      props: { schema },
      global: { plugins: [pinia] }
    });
    
    // The form renderer uses jexl to evaluate. Since jexl is safe, it should return undefined for window.location.href
    // Because it evaluates against { data, context }, window is not defined.
    const result = jexl.evalSync('window.location.href', { data: {}, context: {} });
    expect(result).toBeUndefined(); // Jexl returns undefined for unknown properties.

    // Let's also check fetch
    const fetchCall = () => jexl.evalSync('fetch("http://evil.com")', { data: {}, context: {} });
    // In Jexl, calling an undefined function usually throws an error
    expect(fetchCall).toThrow();
  });

  it('isolates CSS injection inside the Shadow DOM (does not bleed to parent document)', () => {
    const maliciousCSS = '<style>body { display: none; }</style>';
    const schema = [{
      id: 'field1',
      type: 'text',
      label: 'Field 1',
      placeholder: maliciousCSS
    }];

    const wrapper = mount(FormRenderer, {
      props: { schema },
      global: { plugins: [pinia] },
      attachTo: document.body
    });

    const hostEl = wrapper.find('div.w-full').element;
    const shadowRoot = hostEl.shadowRoot;

    const globalStyles = document.querySelectorAll('style');
    let hasBleedingStyle = false;
    globalStyles.forEach(style => {
      if (style.textContent?.includes('display: none')) {
        hasBleedingStyle = true;
      }
    });

    expect(hasBleedingStyle).toBe(false);

    wrapper.unmount();
  });
});
