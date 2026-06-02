// @Traceability: US-003 - CA-52
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { mount, flushPromises } from '@vue/test-utils'
import { useFormDesignerStore } from '@/stores/useFormDesignerStore'
import FormRenderer from '@/components/forms/FormRenderer.vue'
import TaskViewerModal from '@/components/common/TaskViewerModal.vue'
import apiClient from '@/services/apiClient'

vi.mock('@/services/apiClient', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    getBpmnVariables: vi.fn()
  }
}))

// Mock integration store since it is used in FormRenderer and TaskViewerModal
vi.mock('@/stores/useIntegrationStore', () => ({
  useIntegrationStore: () => ({
    get: vi.fn(),
    post: vi.fn()
  })
}))

describe('CA-52: Feedback Visual en Llamadas a APIs / Estado Indeterminado', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('1. useFormDesignerStore.ts generates button_submit and fallback submit with isAsyncLoading bindings and spinner', () => {
    const store = useFormDesignerStore()
    
    // Test with button_submit field
    store.canvasFields = [
      {
        id: 'SUBMIT_BTN',
        type: 'button_submit',
        label: 'Enviar Solicitud',
        required: false
      }
    ]
    store.activeCodeTab = 'TEMPLATE'
    
    let templateCode = store.computedCode
    expect(templateCode).toContain(':disabled="isAsyncLoading"')
    expect(templateCode).toContain('v-if="isAsyncLoading"')
    expect(templateCode).toContain('animate-spin')
    
    // Test with auto-generated fallback submit button
    store.canvasFields = [
      {
        id: 'FIELD_TEXT',
        type: 'text',
        label: 'Nombre',
        camundaVariable: 'nombre',
        required: false
      }
    ]
    
    templateCode = store.computedCode
    expect(templateCode).toContain(':disabled="isAsyncLoading"')
    expect(templateCode).toContain('v-if="isAsyncLoading"')
    expect(templateCode).toContain('animate-spin')
  })

  it('2. FormRenderer.vue defines, exposes isAsyncLoading and sets it during autocomplete api calls', async () => {
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
          { from: 'fullName', to: 'userName' }
        ]
      },
      {
        id: 'FIELD_USER_NAME',
        type: 'text',
        label: 'User Name',
        camundaVariable: 'userName',
        required: false
      }
    ]

    const formData = {
      userId: '12345',
      userName: ''
    }

    // Delay the API resolution so we can inspect the isAsyncLoading value
    let resolveApiCall: any
    const apiPromise = new Promise((resolve) => {
      resolveApiCall = resolve
    })
    
    vi.mocked(apiClient.get).mockImplementation(() => apiPromise as any)

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

    // Initially loading is false
    expect(wrapper.vm.isAsyncLoading).toBe(false)

    // Trigger blur on input to call autocomplete
    const host = wrapper.find({ ref: 'hostRef' }).element as HTMLElement
    const shadowRoot = host.shadowRoot
    expect(shadowRoot).not.toBeNull()

    const userIdInput = shadowRoot!.querySelector('#field-wrapper-FIELD_USER_ID input') as HTMLInputElement
    expect(userIdInput).not.toBeNull()

    // Trigger blur event
    userIdInput.dispatchEvent(new Event('blur'))
    
    // Allow macrotasks / microtasks to start the async function
    await new Promise(resolve => setTimeout(resolve, 10))

    // Verify it is loading
    expect(wrapper.vm.isAsyncLoading).toBe(true)

    // Resolve API call
    resolveApiCall({
      data: {
        fullName: 'John Doe'
      }
    })

    await flushPromises()

    // Verify it stopped loading
    expect(wrapper.vm.isAsyncLoading).toBe(false)
  })

  it('3. TaskViewerModal.vue disables completion button and shows spinner when FormRenderer isAsyncLoading is true', async () => {
    const context = {
      taskId: 'task-123',
      sourceEngine: 'BPMN' as const,
      prefillData: {
        Case_ID: 'case-999',
        Client_Name: 'Acme Corp',
        Priority: 'HIGH',
        SLA: '24h'
      },
      formSnapshot: [
        {
          id: 'FIELD_TEXT',
          type: 'text',
          label: 'Nombre',
          camundaVariable: 'nombre',
          required: false
        }
      ]
    }

    const wrapper = mount(TaskViewerModal, {
      props: {
        isOpen: true,
        context
      },
      global: {
        plugins: [createPinia()]
      }
    })

    await flushPromises()

    // Find the complete/submit button
    const completeButton = wrapper.find('footer button:not(.text-red-600)')
    expect(completeButton.exists()).toBe(true)
    
    // Initially not loading, and not disabled
    expect(completeButton.attributes('disabled')).toBeUndefined()
    expect(completeButton.find('.animate-spin').exists()).toBe(false)

    // Simulate child FormRenderer setting isAsyncLoading = true
    const renderer = wrapper.findComponent(FormRenderer)
    expect(renderer.exists()).toBe(true)
    
    // Set isAsyncLoading to true on FormRenderer vm
    renderer.vm.isAsyncLoading = true
    await wrapper.setValue() // force update/nextTick
    
    // Complete button should now be disabled and show spinner
    expect(completeButton.attributes('disabled')).toBeDefined()
    expect(completeButton.find('.animate-spin').exists()).toBe(true)
  })
})
