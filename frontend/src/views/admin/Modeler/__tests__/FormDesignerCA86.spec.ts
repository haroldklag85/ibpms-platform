// @Traceability: US-003 - CA-86
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { mount, flushPromises } from '@vue/test-utils'
import FormList from '@/views/admin/Modeler/FormList.vue'
import { useRoute, useRouter } from 'vue-router'

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRoute: vi.fn(),
  useRouter: vi.fn(() => ({
    push: mockPush
  }))
}))

const mockGet = vi.fn()
const mockDelete = vi.fn()
const mockPost = vi.fn()
const mockPut = vi.fn()

vi.mock('@/stores/useIntegrationStore', () => ({
  useIntegrationStore: vi.fn(() => ({
    get: mockGet,
    delete: mockDelete,
    post: mockPost,
    put: mockPut
  }))
}))

describe('CA-86: Catálogo y Explorador de Formularios (Form Manager Dashboard)', () => {
  const mockForms = [
    {
      id: 'form-1',
      name: 'Formulario Simple Test',
      author: 'Admin User',
      version: '1.2',
      pattern: 'Simple',
      updatedAt: '2026-05-28T12:00:00Z'
    },
    {
      id: 'form-2',
      name: 'Formulario Maestro Test',
      author: 'QA User',
      version: '2.0',
      pattern: 'iForm Maestro',
      updatedAt: '2026-05-29T10:30:00Z'
    }
  ]

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    
    // Set up mock implementations
    mockGet.mockResolvedValue({ data: mockForms })
    mockDelete.mockResolvedValue({ data: {} })
    mockPost.mockResolvedValue({ data: {} })
    mockPut.mockResolvedValue({ data: {} })
    
    ;(useRoute as any).mockReturnValue({
      query: {},
      params: {}
    })
  })

  it('Test Case 1: UI Columns - renders "Tipo" and "Última Modificación" columns', async () => {
    const wrapper = mount(FormList, {
      global: {
        stubs: {
          RouterLink: true
        }
      }
    })
    
    await flushPromises()

    // Verify table headers contain 'Tipo' and 'Última Modificación'
    const headers = wrapper.findAll('th')
    const headerTexts = headers.map(h => h.text())
    expect(headerTexts).toContain('Tipo')
    expect(headerTexts).toContain('Última Modificación')

    // Verify rows render columns displaying pattern ('Simple' vs 'iForm Maestro') and updatedAt
    const htmlContent = wrapper.html()
    expect(htmlContent).toContain('Simple')
    expect(htmlContent).toContain('iForm Maestro')
    expect(htmlContent).toContain('2026-05-28T12:00:00Z') // or dynamic format if parsed
  })

  it('Test Case 2: History Navigation - renders a history button and navigates to designer history view', async () => {
    const wrapper = mount(FormList, {
      global: {
        stubs: {
          RouterLink: true
        }
      }
    })
    
    await flushPromises()

    // Verify history buttons are rendered (one for each form row)
    const historyButtons = wrapper.findAll('.btn-history, button[title="Historial"], button[title="Historial de Versiones"], button.history-btn')
    expect(historyButtons.length).toBe(mockForms.length)

    // Trigger click on history button of first row
    await historyButtons[0].trigger('click')
    
    // Verify router.push navigated to correct route
    expect(mockPush).toHaveBeenCalledWith(`/admin/modeler/forms/designer?id=${mockForms[0].id}&showHistory=true`)
  })

  it('Test Case 3: Zero Native Dialogs & Confirm Modal - uses custom confirmation modal instead of window.confirm', async () => {
    const confirmSpy = vi.spyOn(window, 'confirm').mockImplementation(() => {
      throw new Error('window.confirm should not be called!')
    })

    const wrapper = mount(FormList, {
      global: {
        stubs: {
          RouterLink: true
        }
      }
    })
    
    await flushPromises()

    // Click delete button of first form row
    const deleteButtons = wrapper.findAll('button[title="Eliminar Registro"], button.delete-btn')
    expect(deleteButtons.length).toBe(mockForms.length)
    
    await deleteButtons[0].trigger('click')
    await wrapper.vm.$nextTick()

    // 1. Verify window.confirm was not called
    expect(confirmSpy).not.toHaveBeenCalled()

    // 2. Verify custom HTML confirm modal opened
    const modal = wrapper.find('.delete-confirm-modal, #deleteConfirmModal')
    expect(modal.exists()).toBe(true)

    // 3. Verify clicking "Cancelar" closes the modal and does NOT delete
    const cancelBtn = modal.findAll('button').find(b => 
      b.text().toLowerCase().includes('cancelar') || b.text().toLowerCase().includes('no')
    )
    expect(cancelBtn).toBeDefined()
    if (cancelBtn) {
      await cancelBtn.trigger('click')
      await wrapper.vm.$nextTick()
      
      // Modal should be closed
      expect(wrapper.find('.delete-confirm-modal, #deleteConfirmModal').exists()).toBe(false)
      // Delete API should not have been called
      expect(mockDelete).not.toHaveBeenCalled()
    }

    // Re-open modal
    await deleteButtons[0].trigger('click')
    await wrapper.vm.$nextTick()
    
    const reOpenedModal = wrapper.find('.delete-confirm-modal, #deleteConfirmModal')
    expect(reOpenedModal.exists()).toBe(true)

    // 4. Verify clicking "Confirmar" calls delete API and closes modal
    const confirmBtn = reOpenedModal.findAll('button').find(b => 
      b.text().toLowerCase().includes('confirmar') || b.text().toLowerCase().includes('sí') || b.text().toLowerCase().includes('si')
    )
    expect(confirmBtn).toBeDefined()
    if (confirmBtn) {
      await confirmBtn.trigger('click')
      await flushPromises()
      
      // Delete API must be called
      expect(mockDelete).toHaveBeenCalledWith(`/api/v1/forms/${mockForms[0].id}`)
      // Modal should be closed
      expect(wrapper.find('.delete-confirm-modal, #deleteConfirmModal').exists()).toBe(false)
    }

    confirmSpy.mockRestore()
  })
})
