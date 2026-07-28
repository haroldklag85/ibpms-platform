import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import FormDesigner from '@/views/admin/Modeler/FormDesigner.vue';
import FormRenderer from '@/components/forms/FormRenderer.vue';
import { useFormDesignerStore } from '@/stores/useFormDesignerStore';
import apiClient from '@/services/apiClient';

vi.mock('vue-router', () => ({
  useRoute: vi.fn(() => ({
    query: { id: 'test-id', formKey: 'test-process-key' },
    params: {}
  })),
  useRouter: vi.fn(() => ({
    push: vi.fn()
  }))
}));

vi.mock('@/services/apiClient', () => ({
  default: {
    getBpmnVariables: vi.fn().mockResolvedValue({ data: [] }),
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn()
  }
}));

describe('US-003: CA-77 Integration and Anti-DDoS / SSRF Barricade', () => {
  const mountOptions = (pinia: any) => ({
    global: {
      plugins: [pinia],
      stubs: {
        VueMonacoEditor: true,
        VueDraggableNext: true,
        Vue3Lottie: true,
        Teleport: true
      }
    }
  });

  beforeEach(() => {
    vi.clearAllMocks();
    vi.useRealTimers();
    const pinia = createPinia();
    setActivePinia(pinia);
  });

  it('1. fetchApprovedConnectors populates approvedConnectors from the backend API', async () => {
    const mockConnectors = [
      '/api/v1/integrations/connectors/RENIEC_PROXY',
      '/api/v1/integrations/connectors/CRM_SALESFORCE'
    ];
    vi.mocked(apiClient.get).mockImplementation((url: string) => {
      if (url.includes('/integrations/connectors')) {
        return Promise.resolve({ data: mockConnectors });
      }
      return Promise.resolve({ data: [] });
    });

    const store = useFormDesignerStore();
    // @ts-ignore
    await store.fetchApprovedConnectors();

    // @ts-ignore
    expect(store.approvedConnectors).toEqual(mockConnectors);
  });

  it('2. In FormDesigner.vue, properties editor renders a select dropdown for autocompleteUrl with approved connectors', async () => {
    const pinia = createPinia();
    const store = useFormDesignerStore(pinia);
    
    // Setup approved connectors and active autocomplete field
    // @ts-ignore
    store.approvedConnectors = [
      '/api/v1/integrations/connectors/RENIEC_PROXY',
      '/api/v1/integrations/connectors/CRM_SALESFORCE'
    ];
    store.editingField = {
      id: 'FIELD_TEST',
      type: 'text',
      label: 'Doc Number',
      enableAutocomplete: true,
      autocompleteUrl: ''
    };

    const wrapper = mount(FormDesigner, mountOptions(pinia));
    await wrapper.vm.$nextTick();

    // Check if dropdown is rendered instead of a raw text input for autocompleteUrl
    // We expect a select element to have options corresponding to the approved connectors
    const select = wrapper.find('select[data-test="autocomplete-select"]');
    expect(select.exists()).toBe(true);

    const options = select.findAll('option');
    expect(options.length).toBeGreaterThan(0);
    expect(options[0].text()).toContain('/api/v1/integrations/connectors/RENIEC_PROXY');
  });

  it('3. validateSchemaSecurity blocks raw external URLs (SSRF) and JS code injection (XSS)', () => {
    const store = useFormDesignerStore();

    // Case A: SSRF Attempt - http external URL
    store.canvasFields = [{
      id: 'FIELD_1',
      type: 'text',
      enableAutocomplete: true,
      autocompleteUrl: 'http://malicious-server.com/api'
    }];
    
    // @ts-ignore
    let result = store.validateSchemaSecurity();
    expect(result.success).toBe(false);
    expect(result.message).toContain('SSRF');

    // Case B: SSRF Attempt - https external URL
    store.canvasFields = [{
      id: 'FIELD_1',
      type: 'text',
      enableAutocomplete: true,
      autocompleteUrl: 'https://malicious-server.com/api'
    }];
    
    // @ts-ignore
    result = store.validateSchemaSecurity();
    expect(result.success).toBe(false);
    expect(result.message).toContain('SSRF');

    // Case C: SSRF Attempt - Not starting with approved prefix
    store.canvasFields = [{
      id: 'FIELD_1',
      type: 'text',
      enableAutocomplete: true,
      autocompleteUrl: '/api/v1/user-info'
    }];
    
    // @ts-ignore
    result = store.validateSchemaSecurity();
    expect(result.success).toBe(false);
    expect(result.message).toContain('SSRF');

    // Case D: XSS / JS Injection in label
    store.canvasFields = [{
      id: 'FIELD_1',
      type: 'text',
      enableAutocomplete: true,
      autocompleteUrl: '/api/v1/integrations/connectors/RENIEC_PROXY',
      label: 'eval("inject")'
    }];
    
    // @ts-ignore
    result = store.validateSchemaSecurity();
    expect(result.success).toBe(false);
    expect(result.message).toContain('XSS');

    // Case E: XSS / JS Injection with fetch(
    store.canvasFields = [{
      id: 'FIELD_1',
      type: 'text',
      label: 'Normal label',
      placeholder: 'fetch("http://evil.com")'
    }];
    
    // @ts-ignore
    result = store.validateSchemaSecurity();
    expect(result.success).toBe(false);
    expect(result.message).toContain('XSS');
  });

  it('4. In FormRenderer.vue, autocomplete triggers a debounced call (500ms) on typing', async () => {
    vi.useFakeTimers();

    const mockResponse = { data: { nombre: 'Juan Pérez' } };
    vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

    const schema = [{
      id: 'FIELD_AUTO',
      type: 'text',
      label: 'Type here',
      enableAutocomplete: true,
      autocompleteUrl: '/api/v1/integrations/connectors/RENIEC_PROXY',
      autocompleteMappings: [
        { from: 'nombre', to: 'nombre_completo' }
      ]
    }];

    const formData = {
      FIELD_AUTO: '',
      nombre_completo: ''
    };

    const wrapper = mount(FormRenderer, {
      props: {
        schema,
        modelValue: formData
      },
      global: {
        stubs: {
          Teleport: true
        }
      }
    });

    await flushPromises();

    // We must find the shadow root app to trigger the input event
    const host = wrapper.find({ ref: 'hostRef' }).element;
    const shadowRoot = host.shadowRoot;
    expect(shadowRoot).not.toBeNull();

    const input = shadowRoot!.querySelector('input[type="text"]') as HTMLInputElement;
    expect(input).not.toBeNull();

    // Simulate typing
    input.value = '12345678';
    input.dispatchEvent(new Event('input'));

    // Should not call API immediately
    expect(apiClient.get).not.toHaveBeenCalled();

    // Fast-forward 200ms
    vi.advanceTimersByTime(200);
    expect(apiClient.get).not.toHaveBeenCalled();

    // Fast-forward another 300ms (total 500ms)
    vi.advanceTimersByTime(300);

    // Now it should be called
    expect(apiClient.get).toHaveBeenCalledWith('/api/v1/integrations/connectors/RENIEC_PROXY?q=12345678');
  });
});
