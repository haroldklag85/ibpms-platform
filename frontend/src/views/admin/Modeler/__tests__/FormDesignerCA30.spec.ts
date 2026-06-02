// @Traceability: US-003 - CA-30
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { mount, flushPromises } from '@vue/test-utils'
import { useFormDesignerStore } from '@/stores/useFormDesignerStore'
import FormRenderer from '@/components/forms/FormRenderer.vue'
import apiClient from '@/services/apiClient'

vi.mock('@/services/apiClient', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    getBpmnVariables: vi.fn()
  }
}))

describe('CA-30: Autocomplete with External API/DB Integration', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('1. useFormDesignerStore supports autocomplete configuration on fields', () => {
    const store = useFormDesignerStore()
    
    // Set up a field with autocomplete configurations
    const testField = {
      id: 'FIELD_USER_ID',
      type: 'text',
      label: 'User ID',
      camundaVariable: 'userId',
      required: true,
      enableAutocomplete: true,
      autocompleteUrl: '/api/v1/users',
      autocompleteMappings: [
        { from: 'fullName', to: 'userName' },
        { from: 'userEmail', to: 'email' }
      ]
    }
    
    store.canvasFields = [testField]
    
    // Verify store has the fields with the configuration
    expect(store.canvasFields[0].enableAutocomplete).toBe(true)
    expect(store.canvasFields[0].autocompleteUrl).toBe('/api/v1/users')
    expect(store.canvasFields[0].autocompleteMappings).toEqual([
      { from: 'fullName', to: 'userName' },
      { from: 'userEmail', to: 'email' }
    ])
  })

  it('2. computedCode compiles the @blur handler and handleAutocomplete_[fieldId]() function', () => {
    const store = useFormDesignerStore()
    
    // Setup state
    store.formTitle = 'Autocomplete Test Form'
    store.formPattern = 'SIMPLE'
    store.canvasFields = [
      {
        id: 'FIELD_USER_ID',
        type: 'text',
        label: 'User ID',
        camundaVariable: 'userId',
        required: true,
        enableAutocomplete: true,
        autocompleteUrl: '/api/v1/users',
        autocompleteMappings: [
          { from: 'fullName', to: 'userName' },
          { from: 'userEmail', to: 'email' }
        ]
      },
      {
        id: 'FIELD_USER_NAME',
        type: 'text',
        label: 'User Name',
        camundaVariable: 'userName',
        required: false
      },
      {
        id: 'FIELD_EMAIL',
        type: 'text',
        label: 'Email',
        camundaVariable: 'email',
        required: false
      }
    ]

    // Switch to SCRIPT tab to check code generation
    store.activeCodeTab = 'SCRIPT'
    const scriptCode = store.computedCode
    
    // Verify that the generated script contains the autocomplete function
    expect(scriptCode).toContain('const handleAutocomplete_FIELD_USER_ID = async () =>')
    expect(scriptCode).toContain('/api/v1/users')
    expect(scriptCode).toContain('formData.value.userName = res.data.fullName')
    expect(scriptCode).toContain('formData.value.email = res.data.userEmail')

    // Switch to TEMPLATE tab to check template code generation
    store.activeCodeTab = 'TEMPLATE'
    const templateCode = store.computedCode
    
    // Verify that the template contains the blur handler
    expect(templateCode).toContain('@blur="validateField(\'userId\'); handleAutocomplete_FIELD_USER_ID();"')
  })

  it('3. FormRenderer executes autocomplete at runtime and maps response properties', async () => {
    const schema = [
      {
        id: 'FIELD_USER_ID',
        type: 'text',
        label: 'User ID',
        camundaVariable: 'userId',
        required: true,
        enableAutocomplete: true,
        autocompleteUrl: '/api/v1/users',
        autocompleteMappings: [
          { from: 'fullName', to: 'userName' },
          { from: 'userEmail', to: 'email' }
        ]
      },
      {
        id: 'FIELD_USER_NAME',
        type: 'text',
        label: 'User Name',
        camundaVariable: 'userName',
        required: false
      },
      {
        id: 'FIELD_EMAIL',
        type: 'text',
        label: 'Email',
        camundaVariable: 'email',
        required: false
      }
    ]

    const formData = {
      userId: '12345',
      userName: '',
      email: ''
    }

    // Mock the api response for the autocomplete url
    vi.mocked(apiClient.get).mockResolvedValue({
      data: {
        fullName: 'John Doe',
        userEmail: 'john.doe@example.com'
      }
    })

    const wrapper = mount(FormRenderer, {
      props: {
        schema,
        modelValue: formData
      },
      global: {
        plugins: [createPinia()]
      }
    })

    await flushPromises()

    // Find the input element for User ID and trigger blur
    // Since FormRenderer uses Shadow DOM, we need to query inside hostRef
    const host = wrapper.find({ ref: 'hostRef' }).element as HTMLElement
    const shadowRoot = host.shadowRoot
    expect(shadowRoot).not.toBeNull()

    const userIdInput = shadowRoot!.querySelector('#field-wrapper-FIELD_USER_ID input') as HTMLInputElement
    expect(userIdInput).not.toBeNull()

    // Trigger blur event
    userIdInput.dispatchEvent(new Event('blur'))
    
    await flushPromises()

    // Verify apiClient.get was called with the correct endpoint and query parameter
    expect(apiClient.get).toHaveBeenCalledWith(expect.stringContaining('/api/v1/users'))
    expect(apiClient.get).toHaveBeenCalledWith(expect.stringContaining('q=12345'))

    // Verify that the formData was populated according to mappings
    const updatedFormData = wrapper.emitted('update:modelValue')?.[0]?.[0] || formData
    expect(updatedFormData.userName).toBe('John Doe')
    expect(updatedFormData.email).toBe('john.doe@example.com')
  })
})
