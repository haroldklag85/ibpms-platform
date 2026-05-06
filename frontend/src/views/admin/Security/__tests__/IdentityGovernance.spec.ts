import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import IdentityGovernance from '../IdentityGovernance.vue'
import { useRbacStore } from '@/stores/rbacStore'
import { useAuthStore } from '@/stores/authStore'

// Mocking dependencies
vi.mock('@/services/apiClient', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
    telemetryAudit: vi.fn().mockResolvedValue({})
  }
}))

describe('IdentityGovernance.vue - Phase 2 (US-036)', () => {
  let pinia: any

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
    
    const authStore = useAuthStore()
    authStore.user = { id: 'admin-1', roles: ['ROLE_SUPER_ADMIN'] }
    authStore.permissions = ['write:all']
  })

  it('CA-07: muestra el sello [Usuario Inactivo] y deshabilita edición para usuarios inactivos', async () => {
    const rbacStore = useRbacStore()
    // Mock API response for users
    vi.spyOn(rbacStore, 'fetchRoles').mockResolvedValue()
    vi.spyOn(rbacStore, 'fetchDelegations').mockResolvedValue()
    vi.spyOn(rbacStore, 'fetchServiceAccounts').mockResolvedValue()

    const wrapper = mount(IdentityGovernance, {
      global: { plugins: [pinia], stubs: { Teleport: true } }
    })

    // Manually set systemUsers for testing visual state
    wrapper.vm.systemUsers = [
      { id: 'user-1', name: 'Juan Inactivo', active: false, email: 'juan@inactive.com', roles: ['ROLE_ANALYST'] }
    ]
    
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('[Usuario Inactivo]')
    
    const editBtn = wrapper.find('[data-testid="btn-edit-user"]')
    expect(editBtn.attributes('disabled')).toBeDefined()
  })

  it('CA-09: valida que la fecha de fin no sea anterior a la de inicio en delegaciones', async () => {
    const wrapper = mount(IdentityGovernance, {
      global: { plugins: [pinia], stubs: { Teleport: true } }
    })
    
    // Switch to Delegations tab
    wrapper.vm.currentTab = 'delegations'
    await wrapper.vm.$nextTick()
    
    wrapper.vm.delForm = { targetUser: 'u-1', start: '2026-05-10', end: '2026-05-05' }
    
    await wrapper.vm.createDelegation()
    
    const rbacStore = useRbacStore()
    expect(rbacStore.delegations.length).toBe(0)
  })

  it('CA-10: muestra el secreto de la API Key solo una vez al crearla', async () => {
    const wrapper = mount(IdentityGovernance, {
      global: { plugins: [pinia], stubs: { Teleport: true } }
    })
    
    wrapper.vm.currentTab = 'api_keys'
    await wrapper.vm.$nextTick()
    
    wrapper.vm.apiKeyForm = { appName: 'Test App', roleId: 'ROLE_ANALYST', expirationDate: '' }
    
    // Mock successful creation
    const rbacStore = useRbacStore()
    vi.spyOn(rbacStore, 'createServiceAccount').mockResolvedValue({ clientId: 'cli-1', secretKey: 'sk-1' })
    
    await wrapper.vm.generateApiKey()
    
    expect(wrapper.vm.newlyCreatedSecret).toBe('sk-1')
    
    // Close the secret notification
    wrapper.vm.closeSecretNotification()
    
    expect(wrapper.vm.newlyCreatedSecret).toBeNull()
  })
})
