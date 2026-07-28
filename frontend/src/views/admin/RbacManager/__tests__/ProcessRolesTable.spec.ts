import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ProcessRolesTable from '../ProcessRolesTable.vue'
import { useRbacStore } from '@/stores/rbacStore'

vi.mock('@/services/apiClient', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    patch: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}))

describe('ProcessRolesTable.vue', () => {
  let pinia: any

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
  })

  it('renderiza la matriz de permisos Iniciador vs Ejecutor (CA-04)', async () => {
    const store = useRbacStore(pinia)
    store.roles = [
      {
        id: 'role-1',
        name: 'PROCESS:Proc1:Lane1',
        type: 'PROCESS_GENERATED',
        laneId: 'Lane1',
        processDefinitionId: 'Proc1',
        processPermissions: [
          {
            id: 'perm-1',
            roleName: 'Grupo A',
            canInitiateProcess: true,
            canExecuteTasks: false,
            sourceRoleId: 'role-1'
          }
        ]
      }
    ]
    store.isLoading = false

    const wrapper = mount(ProcessRolesTable, {
      global: {
        plugins: [pinia]
      }
    })
    
    expect(wrapper.text()).toContain('Lane1')
    expect(wrapper.text()).toContain('Grupo A')
    
    const checkboxes = wrapper.findAll('input[type="checkbox"]')
    expect(checkboxes.length).toBe(2)
    expect((checkboxes[0].element as HTMLInputElement).checked).toBe(true)
    expect((checkboxes[1].element as HTMLInputElement).checked).toBe(false)
  })

  it('muestra indicador de herencia y deshabilita edición (CA-06)', async () => {
    const store = useRbacStore(pinia)
    store.roles = [
      {
        id: 'role-child',
        name: 'PROCESS:Proc1:Lane1',
        type: 'PROCESS_GENERATED',
        laneId: 'Lane1',
        processDefinitionId: 'Proc1',
        processPermissions: [
          {
            id: 'perm-inherited',
            roleName: 'Admin Group',
            canInitiateProcess: true,
            canExecuteTasks: true,
            sourceRoleId: 'role-parent' // Diferente a role-child
          }
        ]
      }
    ]
    store.isLoading = false

    const wrapper = mount(ProcessRolesTable, {
      global: {
        plugins: [pinia]
      }
    })
    
    expect(wrapper.text()).toContain('Heredado')
    
    const checkboxes = wrapper.findAll('input[type="checkbox"]')
    expect((checkboxes[0].element as HTMLInputElement).disabled).toBe(true)
    expect((checkboxes[1].element as HTMLInputElement).disabled).toBe(true)
  })
})
