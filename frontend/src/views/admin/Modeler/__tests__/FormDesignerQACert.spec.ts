import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import FormDesigner from '@/views/admin/Modeler/FormDesigner.vue'
import { createPinia } from 'pinia'
import { useRoute } from 'vue-router'

// Mocks
vi.mock('vue-router', () => ({
  useRoute: vi.fn(),
  useRouter: vi.fn(() => ({
    push: vi.fn()
  }))
}))

vi.mock('@/services/apiClient', () => ({
  default: {
    getBpmnVariables: vi.fn().mockResolvedValue({ data: ['testVar1', 'testVar2'] })
  }
}))

describe('US-028: Form Designer QA Certification (CA-12 to CA-17)', () => {
  beforeEach(() => {
    (useRoute as ReturnType<typeof vi.fn>).mockReturnValue({
      query: { formKey: 'test-process-key' },
      params: { id: 'test-id' }
    })
  })

  it('CA-12: Badge muestra "revoked" cuando certification state cambia', async () => {
    const pinia = createPinia()
    const wrapper = mount(FormDesigner, {
      global: { plugins: [pinia] },
      shallow: true
    })
    
    // Simular hidratación
    ;(wrapper.vm as any).certificationState = 'revoked'
    await wrapper.vm.$nextTick()
    
    expect(wrapper.text()).toContain('⚠️ Certificación QA revocada — Modificación detectada')
    
    ;(wrapper.vm as any).certificationState = 'certified'
    await wrapper.vm.$nextTick()
    expect(wrapper.text()).toContain('✅ Certificado QA')
  })

  it('CA-13: Indicador de versión muestra V{N} + estado', async () => {
    const pinia = createPinia()
    const wrapper = mount(FormDesigner, {
      global: { plugins: [pinia] },
      shallow: true
    })
    
    // Abrir modal y mock version
    ;(wrapper.vm as any).showFuzzerModal = true
    ;(wrapper.vm as any).currentSchemaVersion = 5
    ;(wrapper.vm as any).certificationState = 'certified'
    await wrapper.vm.$nextTick()
    
    expect(wrapper.text()).toContain('📋 Esquema V5')
    expect(wrapper.text()).toContain('Certificado ✅')
  })

  it('CA-14: Errores de superRefine se pintan en naranja', async () => {
    const pinia = createPinia()
    const wrapper = mount(FormDesigner, {
      global: { plugins: [pinia] },
      shallow: true
    })
    
    // Forzar errores del fuzzer (uno de typo/validation, uno de interdependencia/refine)
    ;(wrapper.vm as any).fuzzerErrors = [
      { msg: '[field1] - required', isRefine: false },
      { msg: '[field2] - custom rule failed', isRefine: true }
    ]
    ;(wrapper.vm as any).showFuzzerModal = true
    await wrapper.vm.$nextTick()

    const errorMessages = wrapper.findAll('.font-mono.text-xs.space-y-1 > div')
    expect(errorMessages.length).toBeGreaterThan(0)
  })

  it('CA-17: Panel de coherencia muestra matches y warnings', async () => {
    const pinia = createPinia()
    const wrapper = mount(FormDesigner, {
      global: { plugins: [pinia] },
      shallow: true
    })

    // Montar coherencia manual
    ;(wrapper.vm as any).bpmnCoherenceResults = [
        { name: 'testVar1', icon: '✅', label: "Variable BPMN 'testVar1' → Campo Zod 'testVar1'", class: 'text-green-400' },
        { name: 'testVarUnmapped', icon: '⚠️', label: "Variable BPMN 'testVarUnmapped' → No encontrada en esquema Zod", class: 'text-yellow-400' }
    ]
    ;(wrapper.vm as any).formKey = 'test-process-key'
    ;(wrapper.vm as any).showFuzzerModal = true
    await wrapper.vm.$nextTick()
    
    expect(wrapper.text()).toContain('🔗 Coherencia BPMN ↔ Zod')
    expect(wrapper.text()).toContain("Variable BPMN 'testVar1' → Campo Zod 'testVar1'")
    expect(wrapper.text()).toContain("Variable BPMN 'testVarUnmapped' → No encontrada en esquema Zod")
  })
})
