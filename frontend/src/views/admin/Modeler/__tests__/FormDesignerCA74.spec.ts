// @Traceability: US-003 - CA-74
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { mount, flushPromises } from '@vue/test-utils'
import { useFormDesignerStore } from '@/stores/useFormDesignerStore'
import FormDesigner from '@/views/admin/Modeler/FormDesigner.vue'
import apiClient from '@/services/apiClient'
import { useRoute } from 'vue-router'

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
    post: vi.fn()
  }
}))

describe('CA-74: Global Dictionary and Reusable Fragments (Snippets) Unit & Integration Tests', () => {
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
    setActivePinia(createPinia())
    vi.clearAllMocks()
    
    ;(useRoute as ReturnType<typeof vi.fn>).mockReturnValue({
      query: { id: 'test-id', formKey: 'test-process-key' },
      params: {}
    });
    ;(vi.mocked(apiClient) as any).getBpmnVariables.mockResolvedValue({ data: [] })
    vi.mocked(apiClient.get).mockResolvedValue({ data: [] })
  })

  it('1. fetchDictionary in the store queries /api/v1/design/dictionary and populates dictionaryItems', async () => {
    const store = useFormDesignerStore() as any
    const mockDict = [
      { id: 'user_email', label: 'User Email Address', type: 'email', isPII: true },
      { id: 'user_phone', label: 'User Phone Number', type: 'text', isPII: true }
    ]
    vi.mocked(apiClient.get).mockResolvedValue({ data: mockDict })

    await store.fetchDictionary()

    expect(apiClient.get).toHaveBeenCalledWith('/api/v1/design/dictionary')
    expect(store.dictionaryItems).toEqual(mockDict)
  })

  it('2. fetchSnippets in the store queries /api/v1/design/snippets and populates "Mis Fragmentos" toolbox category', async () => {
    const store = useFormDesignerStore() as any
    const mockSnippets = [
      {
        name: 'Fragmento Prueba',
        components: [{ id: 'field_test', type: 'text', label: 'Test Label' }]
      }
    ]
    vi.mocked(apiClient.get).mockResolvedValue({ data: mockSnippets })

    await store.fetchSnippets()

    expect(apiClient.get).toHaveBeenCalledWith('/api/v1/design/snippets')
    const fragmentCategory = store.toolboxCategories.find((c: any) => c.name === 'Mis Fragmentos')
    expect(fragmentCategory).toBeDefined()
    expect(fragmentCategory.items).toContainEqual(expect.objectContaining({
      name: 'Fragmento Prueba',
      components: expect.any(Array)
    }))
  })

  it('3. saveAsFragment or saveSnippet in the store POSTs the correct payload structure (name and components) to /api/v1/design/snippets', async () => {
    const store = useFormDesignerStore() as any
    vi.mocked(apiClient.post).mockResolvedValue({ status: 201 })

    const componentToSave = { id: 'some_field', type: 'text', label: 'Test Field' }
    
    // We test saveSnippet or saveAsFragment. If saveSnippet exists, call it.
    await store.saveSnippet('My Snippet', [componentToSave])

    expect(apiClient.post).toHaveBeenCalledWith('/api/v1/design/snippets', {
      name: 'My Snippet',
      components: [componentToSave]
    })
  })

  it('4. In FormDesigner.vue, editingField camundaVariable input has a datalist displaying dictionary items', async () => {
    const mockDict = [
      { id: 'user_email', label: 'User Email Address', type: 'email', isPII: true }
    ]
    vi.mocked(apiClient.get).mockImplementation((url) => {
      if (url.includes('/api/v1/design/dictionary')) {
        return Promise.resolve({ data: mockDict }) as any
      }
      return Promise.resolve({ data: [] }) as any
    })
    
    const pinia = createPinia()
    const store = useFormDesignerStore(pinia) as any
    store.dictionaryItems = mockDict

    const wrapper = mount(FormDesigner, mountOptions(pinia))
    await flushPromises()

    // Trigger editing a field
    const mockField = { id: 'field_1', type: 'text', label: 'Email Field', camundaVariable: '' }
    store.editingField = mockField
    await wrapper.vm.$nextTick()

    // Check for datalist
    const datalist = wrapper.find('#dictionary-datalist')
    expect(datalist.exists()).toBe(true)

    const option = datalist.find('option')
    expect(option.exists()).toBe(true)
    expect(option.attributes('value')).toBe('user_email')
  })

  it('5. Selecting a dictionary item automatically copies its label and isPII properties onto editingField', async () => {
    const mockDict = [
      { id: 'user_email', label: 'User Email Address', type: 'email', isPII: true }
    ]
    vi.mocked(apiClient.get).mockImplementation((url) => {
      if (url.includes('/api/v1/design/dictionary')) {
        return Promise.resolve({ data: mockDict }) as any
      }
      return Promise.resolve({ data: [] }) as any
    })

    const pinia = createPinia()
    const store = useFormDesignerStore(pinia) as any
    store.dictionaryItems = mockDict

    const wrapper = mount(FormDesigner, mountOptions(pinia))
    await flushPromises()

    // Trigger editing a field
    const mockField = { id: 'field_1', type: 'text', label: 'Initial Label', camundaVariable: '', isPII: false }
    store.editingField = mockField
    await wrapper.vm.$nextTick()

    // Find the camundaVariable input connected to the datalist
    const input = wrapper.find('input[list="dictionary-datalist"]')
    expect(input.exists()).toBe(true)

    // Simulate user entering/selecting a dictionary item ID/key
    await input.setValue('user_email')
    // Trigger change event to mimic browser behavior when choosing from datalist
    await input.trigger('change')
    await wrapper.vm.$nextTick()

    // Expect label and isPII properties to be copied onto the edited field
    expect(store.editingField.label).toBe('User Email Address')
    expect(store.editingField.isPII).toBe(true)
    expect(store.editingField.type).toBe('email')
  })
})
