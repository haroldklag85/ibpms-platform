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
    vi.spyOn(rbacStore, 'createServiceAccount').mockResolvedValue({ id: 'cli-1', plainApiKey: 'sk-1' })
    
    await wrapper.vm.generateApiKey()
    
    expect(wrapper.vm.newlyCreatedSecret).toBe('sk-1')
    
    // Close the secret notification
    wrapper.vm.closeSecretNotification()
    
    expect(wrapper.vm.newlyCreatedSecret).toBeNull()
  })

  it('CA-27: valida la inmutabilidad de roles CORE (SUPER_ADMIN, NATIVE_ADMIN)', async () => {
    const wrapper = mount(IdentityGovernance, {
      global: { plugins: [pinia], stubs: { Teleport: true } }
    })
    
    // Validar helper isCoreRole
    expect(wrapper.vm.isCoreRole('SUPER_ADMIN')).toBe(true)
    expect(wrapper.vm.isCoreRole('NATIVE_ADMIN')).toBe(true)
    expect(wrapper.vm.isCoreRole('SYSTEM_ADMIN')).toBe(true)
    expect(wrapper.vm.isCoreRole('ANALYST')).toBe(false)

    // Abrir modal con rol fundacional
    wrapper.vm.openRoleModal({ id: 'SUPER_ADMIN', name: 'Super Administrador', topology: {} })
    await wrapper.vm.$nextTick()
    
    expect(wrapper.vm.showRoleModal).toBe(true)
    
    // Cambiar a pestaña de topología
    wrapper.vm.roleModalTab = 'topology'
    await wrapper.vm.$nextTick()

    // Intentar disparar guardado
    await wrapper.vm.saveRole()
    // El toast de error debe haberse disparado si el guardado se bloqueó (validación reactiva en saveRole)
    expect(wrapper.vm.toast.msg).toContain('inmutables')
  })
})
