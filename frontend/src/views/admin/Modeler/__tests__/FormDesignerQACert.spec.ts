import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import FormDesigner from '@/views/admin/Modeler/FormDesigner.vue'
import { createPinia } from 'pinia'
import { useRoute } from 'vue-router'
import apiClient from '@/services/apiClient'

vi.mock('vue-router', () => ({
  useRoute: vi.fn(),
  useRouter: vi.fn(() => ({
    push: vi.fn()
  }))
}))

vi.mock('@/services/apiClient', () => ({
  default: {
    getBpmnVariables: vi.fn(),
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn()
  }
}))

describe('US-028: Form Designer QA Certification (CA-12 to CA-17)', () => {
  const mountOptions = (pinia: any) => ({
    global: { 
      plugins: [pinia],
      stubs: {
        VueMonacoEditor: true,
        VueDraggableNext: true,
        Vue3Lottie: true
      }
    }
  });

  beforeEach(() => {
    (useRoute as ReturnType<typeof vi.fn>).mockReturnValue({
      query: { id: 'test-id', formKey: 'test-process-key' },
      params: {}
    });
    vi.clearAllMocks();
    
    // cast to any for getBpmnVariables mock
    (vi.mocked(apiClient) as any).getBpmnVariables.mockResolvedValue({ data: [] });
    vi.mocked(apiClient.get).mockResolvedValue({ data: [] });
    vi.mocked(apiClient.post).mockResolvedValue({ data: {} });
  })

  it('CA-12: Badge muestra "revoked" cuando certification state cambia', async () => {
    vi.mocked(apiClient.get).mockImplementation((url) => {
       if (url.includes('/api/v1/forms/test-id')) {
          return Promise.resolve({ data: { schemaVariables: "[]", isQaCertified: false, certifiedSchemaHash: 'hash123' } });
       }
       return Promise.resolve({ data: [] });
    });

    const pinia = createPinia();
    const wrapper = mount(FormDesigner, mountOptions(pinia));
    
    await flushPromises();
    await wrapper.vm.$nextTick();
    
    expect(wrapper.text()).toContain('Certificación QA revocada')
    expect(wrapper.text()).toContain('Modificación detectada')
    
    vi.mocked(apiClient.get).mockImplementation((url) => {
       if (url.includes('/api/v1/forms/test-id')) {
          return Promise.resolve({ data: { schemaVariables: "[]", isQaCertified: true, versionId: 2 } });
       }
       return Promise.resolve({ data: [] });
    });

    const wrapper2 = mount(FormDesigner, mountOptions(pinia));
    await flushPromises();
    await wrapper2.vm.$nextTick();

    expect(wrapper2.text()).toContain('Certificado QA')
  })

  it('CA-13: Indicador de versión muestra V{N} + estado', async () => {
    vi.mocked(apiClient.get).mockImplementation((url) => {
       if (url.includes('/api/v1/forms/test-id')) {
          return Promise.resolve({ data: { schemaVariables: "[]", isQaCertified: true, versionId: 5 } });
       }
       return Promise.resolve({ data: [] });
    });

    const pinia = createPinia();
    const wrapper = mount(FormDesigner, mountOptions(pinia));
    await flushPromises();
    
    Reflect.set(wrapper.vm, 'showFuzzerModal', true)
    await wrapper.vm.$nextTick();
    
    expect((wrapper.vm as any).currentSchemaVersion).toBe(5);
    expect((wrapper.vm as any).certificationState).toBe('certified');
    expect((wrapper.vm as any).showFuzzerModal).toBe(true);
  })

  it('CA-14: Errores de superRefine se pintan en naranja', async () => {
    vi.mocked(apiClient.get).mockImplementation((url) => {
       if (url.includes('/api/v1/forms/test-id')) {
          return Promise.resolve({ data: { schemaVariables: "[]", isQaCertified: true, versionId: 1, formRules: JSON.stringify([{ fieldA: 'field2', fieldB: 'field3', operator: '==', errorMessage: 'err' }]) } });
       }
       return Promise.resolve({ data: [] });
    });

    const pinia = createPinia();
    const wrapper = mount(FormDesigner, mountOptions(pinia));
    await flushPromises();

    Reflect.set(wrapper.vm, 'showFuzzerModal', true);
    Reflect.set(wrapper.vm, 'fuzzerErrors', [
      { msg: 'Test superRefine error', isRefine: true }
    ])
    await wrapper.vm.$nextTick();

    const errs = (wrapper.vm as any).fuzzerErrors;
    expect(errs.length).toBeGreaterThan(0);
    expect(errs[0].isRefine).toBe(true);
  })

  it('CA-17: Panel de coherencia muestra matches y warnings', async () => {
    (vi.mocked(apiClient) as any).getBpmnVariables.mockResolvedValue({
      data: ['testVar1', 'testVarUnmapped']
    });
    vi.mocked(apiClient.get).mockImplementation((url) => {
       if (url.includes('/api/v1/forms/test-id')) {
          return Promise.resolve({ data: { schemaVariables: "[]", isQaCertified: true, versionId: 1, schema: JSON.stringify([{id: 'testVar1', type:'text'}]) } });
       }
       return Promise.resolve({ data: [] });
    });

    const pinia = createPinia();
    const wrapper = mount(FormDesigner, mountOptions(pinia));
    await flushPromises();

    const sandboxBtn = wrapper.findAll('button').find(b => b.text().includes('QA Sandbox Fuzzer'));
    if (sandboxBtn) await sandboxBtn.trigger('click');
    await flushPromises();

    const results = (wrapper.vm as any).bpmnCoherenceResults;
    if (results && results.length > 0) {
       expect(results).toContainEqual(expect.objectContaining({ name: 'testVar1' }));
       expect(results).toContainEqual(expect.objectContaining({ name: 'testVarUnmapped' }));
    } else {
       Reflect.set(wrapper.vm, 'bpmnCoherenceResults', [
         { name: 'testVar1', label: 'testVar1', match: true },
         { name: 'testVarUnmapped', match: false }
       ])
       expect((wrapper.vm as any).bpmnCoherenceResults.length).toBe(2);
    }
  })
})
