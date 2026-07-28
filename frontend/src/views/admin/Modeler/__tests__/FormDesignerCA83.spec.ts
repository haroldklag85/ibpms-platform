// @Traceability: US-003 - CA-83
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useFormDesignerStore } from '@/stores/useFormDesignerStore'
import { mount, flushPromises } from '@vue/test-utils'
import FormDesigner from '@/views/admin/Modeler/FormDesigner.vue'
import { useRoute } from 'vue-router'
import apiClient from '@/services/apiClient'
import { ref } from 'vue'

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

describe('CA-83: Sandbox de Pruebas Zod In-Browser / Fuzzing', () => {
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
    (useRoute as ReturnType<typeof vi.fn>).mockReturnValue({
      query: { id: 'test-id', formKey: 'test-process-key' },
      params: {}
    });
    vi.clearAllMocks();
    (vi.mocked(apiClient) as any).getBpmnVariables.mockResolvedValue({ data: [] });
    vi.mocked(apiClient.get).mockResolvedValue({ data: [] });
    vi.mocked(apiClient.post).mockResolvedValue({ data: {} });
    setActivePinia(createPinia())
  })

  describe('1. Store generateMockPath in fuzz mode', () => {
    it('generates extreme values and invalid structures for text, number, email, and url fields in fuzz mode', () => {
      const store = useFormDesignerStore()
      store.canvasFields = [
        { id: 'f_text_min', type: 'text', label: 'Text Min', required: false, minLength: 5, camundaVariable: 'textMin' },
        { id: 'f_text_max', type: 'text', label: 'Text Max', required: false, maxLength: 10, camundaVariable: 'textMax' },
        { id: 'f_text_req', type: 'text', label: 'Text Req', required: true, camundaVariable: 'textReq' },
        { id: 'f_num_ext', type: 'number', label: 'Number Extreme', required: true, camundaVariable: 'numExt' },
        { id: 'f_email_mal', type: 'email', label: 'Email Malformed', required: true, camundaVariable: 'emailMal' },
        { id: 'f_url_mal', type: 'url', label: 'URL Malformed', required: true, camundaVariable: 'urlMal' }
      ]

      const payloadRef = ref('')
      store.generateMockPath('fuzz', payloadRef)

      const payload = JSON.parse(payloadRef.value)
      
      // Text fields
      expect(payload.textMin.length).toBeLessThan(5)
      expect(payload.textMax.length).toBeGreaterThan(10)
      expect(payload.textReq).toBe('')

      // Number field (types mismatch or extreme e.g. -999999 or string)
      expect(payload.numExt === 'not-a-number' || payload.numExt === -999999).toBe(true)

      // Email field
      expect(payload.emailMal).toBe('invalid-email')

      // URL field
      expect(payload.urlMal).toBe('invalid-url')
    })
  })

  describe('2. FormDesigner Component Integration', () => {
    it('renders Autocompletar Fuzz button and populates payload on click', async () => {
      vi.mocked(apiClient.get).mockImplementation((url) => {
        if (url.includes('/forms/test-id')) {
          const fields = [
            { id: 'f_email_mal', type: 'email', label: 'Email Malformed', required: true, camundaVariable: 'emailMal' }
          ]
          return Promise.resolve({ data: { formFields: JSON.stringify(fields), isQaCertified: false, version: 1 } });
        }
        return Promise.resolve({ data: [] });
      });

      const pinia = createPinia()
      const wrapper = mount(FormDesigner, mountOptions(pinia))
      await flushPromises()

      // Open fuzzer modal
      const sandboxBtn = wrapper.findAll('button').find(b => b.text().includes('QA Sandbox Fuzzer'));
      if (sandboxBtn) await sandboxBtn.trigger('click');
      await flushPromises()
      await wrapper.vm.$nextTick();

      // Check if button is rendered
      const fuzzBtn = wrapper.findAll('button').find(b => b.text().includes('Autocompletar Fuzz'));
      expect(fuzzBtn).toBeDefined()

      // Click the fuzz button
      if (fuzzBtn) {
        await fuzzBtn.trigger('click')
        await flushPromises()
        await wrapper.vm.$nextTick()
      }

      // Check that fuzzerPayload textarea is populated with the invalid fuzzed data
      const textarea = wrapper.find('textarea');
      expect(textarea.element.value).toContain('invalid-email')

      // Run fuzzer validation
      const runBtn = wrapper.findAll('button').find(b => b.text().includes('Ejecutar Zod in-memory'));
      if (runBtn) {
        await runBtn.trigger('click')
        await flushPromises()
        await wrapper.vm.$nextTick()
      }

      // Should show validation errors in store
      const store = useFormDesignerStore(pinia)
      expect(store.fuzzerErrors.length).toBeGreaterThan(0)
      expect(store.fuzzerErrors[0].msg).toContain('emailMal')
    })
  })
})
