// @Traceability: US-003 - CA-75
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import FormDesigner from '@/views/admin/Modeler/FormDesigner.vue';
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

describe('US-003: CA-75 - Peaje Analítico (Data Diet) Unit Tests', () => {
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
    const pinia = createPinia();
    setActivePinia(pinia);
  });

  it('1. In FormDesigner.vue, editing a field displays a select dropdown with id or data-testid "destinoEstrategicoSelect"', async () => {
    const pinia = createPinia();
    const store = useFormDesignerStore(pinia);

    // Set editing field to a text field
    store.editingField = {
      id: 'FIELD_TEST',
      type: 'text',
      label: 'Document Number',
      required: true,
      destinoEstrategico: ''
    };

    const wrapper = mount(FormDesigner, mountOptions(pinia));
    await wrapper.vm.$nextTick();

    const select = wrapper.find('select[data-testid="destinoEstrategicoSelect"]');
    expect(select.exists()).toBe(true);

    const options = select.findAll('option');
    const optionValues = options.map(o => o.text());
    expect(optionValues).toContain('Regla DMN');
    expect(optionValues).toContain('Integración Externa');
    expect(optionValues).toContain('Documento PDF SGDEA');
    expect(optionValues).toContain('Analítica Pasiva');
  });

  it('2. When setting destinoEstrategico to "Analítica Pasiva", required checkbox is disabled andrequired is set to false', async () => {
    const pinia = createPinia();
    const store = useFormDesignerStore(pinia);

    store.editingField = {
      id: 'FIELD_TEST',
      type: 'text',
      label: 'Email Address',
      required: true,
      destinoEstrategico: 'Analítica Pasiva'
    };

    const wrapper = mount(FormDesigner, mountOptions(pinia));
    await wrapper.vm.$nextTick();

    const reqCheck = wrapper.find('#reqCheck');
    expect(reqCheck.exists()).toBe(true);
    expect(reqCheck.attributes('disabled')).toBeDefined();
    expect(store.editingField.required).toBe(false);
  });

  it('3. validateSchemaSecurity in store returns an error if any input field in canvasFields has an empty destinoEstrategico', () => {
    const store = useFormDesignerStore();

    // Field is input (e.g. text) and has empty/missing destinoEstrategico
    store.canvasFields = [{
      id: 'FIELD_1',
      type: 'text',
      destinoEstrategico: ''
    }];

    const result = store.validateSchemaSecurity();
    expect(result.success).toBe(false);
    expect(result.message).toContain('destino estratégico');

    // Field is a layout component, shouldn't trigger error if empty
    store.canvasFields = [{
      id: 'CONTAINER_1',
      type: 'container',
      destinoEstrategico: ''
    }];

    const layoutResult = store.validateSchemaSecurity();
    expect(layoutResult.success).toBe(true);

    // Field has valid strategic destination
    store.canvasFields = [{
      id: 'FIELD_1',
      type: 'text',
      destinoEstrategico: 'Regla DMN'
    }];

    const validResult = store.validateSchemaSecurity();
    expect(validResult.success).toBe(true);
  });

  it('4. cloneComponent initializes destinoEstrategico to an empty string', () => {
    const store = useFormDesignerStore();
    const originalComponent = {
      label: 'Input field label',
      type: 'text'
    };

    const cloned = store.cloneComponent(originalComponent);
    expect(cloned.destinoEstrategico).toBe('');
  });
});
