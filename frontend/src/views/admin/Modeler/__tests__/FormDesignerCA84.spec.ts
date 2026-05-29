// @Traceability: US-003 - CA-84
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useFormDesignerStore } from '@/stores/useFormDesignerStore'
import { mount, flushPromises } from '@vue/test-utils'
import FormDesigner from '@/views/admin/Modeler/FormDesigner.vue'
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

describe('CA-84: Manejo Amigable de Errores de Sintaxis en el Mónaco IDE', () => {
  const mountOptions = (pinia: any) => ({
    global: {
      plugins: [pinia],
      stubs: {
        VueMonacoEditor: true,
        VueDraggable: true,
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
    setActivePinia(createPinia());
  });

  describe('1. Store Level Error Handling', () => {
    it('does not crash and populates store.editorErrors ref with syntax error details when setting malformed code', () => {
      const store = useFormDesignerStore()
      store.activeCodeTab = 'ZOD'
      
      const malformedCode = `
import { z } from 'zod';

export const taskSchema = z.object({
  fieldName: z.string(
      `
      
      expect(() => {
        store.computedCode = malformedCode
      }).not.toThrow()
      
      expect(store.editorErrors).toBeDefined()
      expect(store.editorErrors.length).toBeGreaterThan(0)
      expect(store.editorErrors[0]).toHaveProperty('message')
    })
  })

  describe('2. UI Component level', () => {
    it('renders dedicated problems panel/diagnostic element when store.editorErrors has errors', async () => {
      const pinia = createPinia()
      setActivePinia(pinia)
      const store = useFormDesignerStore(pinia)
      
      // Simulate errors in store.editorErrors
      store.editorErrors = [
        { message: 'Sintaxis fallida o Regex roto', line: 4 }
      ]
      
      const wrapper = mount(FormDesigner, mountOptions(pinia))
      await flushPromises()
      
      const panel = wrapper.find('.editor-problems-panel, #editorProblemsPanel')
      expect(panel.exists()).toBe(true)
      expect(panel.text()).toContain('Sintaxis fallida o Regex roto')
    })
  })

  describe('3. Zero Native Alerts', () => {
    it('does not trigger window.alert when setting invalid code', () => {
      const store = useFormDesignerStore()
      store.activeCodeTab = 'ZOD'
      
      const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {})
      
      const badCode = 'invalid { code'
      
      expect(() => {
        store.computedCode = badCode
      }).not.toThrow()
      
      expect(alertSpy).not.toHaveBeenCalled()
      
      alertSpy.mockRestore()
    })
  })
})
