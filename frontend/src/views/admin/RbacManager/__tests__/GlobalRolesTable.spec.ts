/**
 * US-036 CA-2 & CA-3 — GlobalRolesTable.vue Vitest Suite
 *
 * CA-2: El botón de borrado (data-testid="btn-delete-role") y el de edición
 *       NO deben renderizarse cuando el rol es ROLE_SUPER_ADMIN.
 * CA-3: El botón de asignación masiva (data-testid="btn-mass-assign")
 *       SOLO se renderiza cuando rol.isTemplate === true.
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import GlobalRolesTable from '../GlobalRolesTable.vue'

// ── Roles de prueba ──────────────────────────────────────────────────────────
const ROOT_ROLE = {
  id: 'root-uuid-0000',
  name: 'ROLE_SUPER_ADMIN',
  description: 'Guardián Root — inmutable',
  isTemplate: false,
  is_vip_restricted: false,
  members: [],
  type: 'GLOBAL',
}

const ANALYST_ROLE = {
  id: 'analyst-uuid-1111',
  name: 'ROLE_ANALYST',
  description: 'Analista operativo',
  isTemplate: false,
  is_vip_restricted: false,
  members: [{ id: 1, email: 'ana@empresa.com' }],
  type: 'GLOBAL',
}

const TEMPLATE_ROLE = {
  id: 'template-uuid-2222',
  name: 'ROLE_ONBOARDING_TEMPLATE',
  description: 'Plantilla para nuevos empleados',
  isTemplate: true,
  is_vip_restricted: false,
  members: [],
  type: 'GLOBAL',
}

// ── Mock del store ───────────────────────────────────────────────────────────
const fetchRolesMock = vi.fn()

vi.mock('@/stores/rbacStore', () => ({
  useRbacStore: vi.fn(() => ({
    isLoading: false,
    globalRoles: [ROOT_ROLE, ANALYST_ROLE, TEMPLATE_ROLE],
    fetchRoles: fetchRolesMock,
  })),
}))

// ── Mock de apiClient (evita llamadas HTTP reales) ───────────────────────────
vi.mock('@/services/apiClient', () => ({
  default: {
    delete: vi.fn().mockResolvedValue({}),
    post: vi.fn().mockResolvedValue({ data: { assigned: 2, notFound: [] } }),
  },
}))

// ── Suite ────────────────────────────────────────────────────────────────────
describe('GlobalRolesTable.vue — US-036 CA-2 & CA-3', () => {

  let wrapper: ReturnType<typeof mount>

  beforeEach(() => {
    vi.clearAllMocks()
    wrapper = mount(GlobalRolesTable, {
      global: { stubs: { teleport: true } },
    })
  })

  // ── CA-2: Inmutabilidad del Guardián ────────────────────────────────────────

  it('CA-2 RBAC-FE-01: NO renderiza btn-delete-role para ROLE_SUPER_ADMIN', () => {
    // Obtener todas las filas de la tabla
    const rows = wrapper.findAll('tbody tr')
    const rootRow = rows.find(r => r.text().includes('ROLE_SUPER_ADMIN'))
    expect(rootRow).toBeDefined()

    // El botón de borrado NO debe existir en la fila Root
    const deleteBtn = rootRow!.find('[data-testid="btn-delete-role"]')
    expect(deleteBtn.exists()).toBe(false)
  })

  it('CA-2 RBAC-FE-02: NO renderiza btn-edit-role para ROLE_SUPER_ADMIN', () => {
    const rows = wrapper.findAll('tbody tr')
    const rootRow = rows.find(r => r.text().includes('ROLE_SUPER_ADMIN'))
    expect(rootRow).toBeDefined()

    const editBtn = rootRow!.find('[data-testid="btn-edit-role"]')
    expect(editBtn.exists()).toBe(false)
  })

  it('CA-2 RBAC-FE-03: muestra indicador "protegido" en la fila Root', () => {
    const rows = wrapper.findAll('tbody tr')
    const rootRow = rows.find(r => r.text().includes('ROLE_SUPER_ADMIN'))
    expect(rootRow!.text()).toContain('protegido')
  })

  it('CA-2 RBAC-FE-04: SÍ renderiza btn-delete-role para roles no-Root', () => {
    const rows = wrapper.findAll('tbody tr')
    const analystRow = rows.find(r => r.text().includes('ROLE_ANALYST'))
    expect(analystRow).toBeDefined()

    const deleteBtn = analystRow!.find('[data-testid="btn-delete-role"]')
    expect(deleteBtn.exists()).toBe(true)
  })

  it('CA-2 RBAC-FE-05: badge 🔒 ROOT es visible solo en la fila ROLE_SUPER_ADMIN', () => {
    // La fila root debe tener el badge ROOT
    expect(wrapper.text()).toContain('ROOT')

    // La fila analyst NO debe tener el badge ROOT
    const rows = wrapper.findAll('tbody tr')
    const analystRow = rows.find(r => r.text().includes('ROLE_ANALYST'))
    expect(analystRow!.text()).not.toContain('ROOT')
  })

  // ── CA-3: Botón de Asignación Masiva ────────────────────────────────────────

  it('CA-3 RBAC-FE-06: btn-mass-assign se renderiza SOLO para roles isTemplate=true', () => {
    // La fila plantilla debe tener el botón
    const rows = wrapper.findAll('tbody tr')
    const templateRow = rows.find(r => r.text().includes('ROLE_ONBOARDING_TEMPLATE'))
    expect(templateRow).toBeDefined()
    const massBtn = templateRow!.find('[data-testid="btn-mass-assign"]')
    expect(massBtn.exists()).toBe(true)
  })

  it('CA-3 RBAC-FE-07: btn-mass-assign NO aparece en roles isTemplate=false', () => {
    const rows = wrapper.findAll('tbody tr')
    const analystRow = rows.find(r => r.text().includes('ROLE_ANALYST'))
    const massBtn = analystRow!.find('[data-testid="btn-mass-assign"]')
    expect(massBtn.exists()).toBe(false)
  })

  it('CA-3 RBAC-FE-08: hacer click en btn-mass-assign abre el modal de confirmación', async () => {
    const rows = wrapper.findAll('tbody tr')
    const templateRow = rows.find(r => r.text().includes('ROLE_ONBOARDING_TEMPLATE'))
    const massBtn = templateRow!.find('[data-testid="btn-mass-assign"]')

    await massBtn!.trigger('click')

    const modal = wrapper.find('[data-testid="modal-mass-assign"]')
    expect(modal.exists()).toBe(true)
    expect(modal.text()).toContain('ROLE_ONBOARDING_TEMPLATE')
  })

  // ── CA-6: Edición y Herencia ────────────────────────────────────────────────

  it('CA-6 RBAC-FE-09: hacer click en btn-edit-role abre el modal de edición', async () => {
    const rows = wrapper.findAll('tbody tr')
    const analystRow = rows.find(r => r.text().includes('ROLE_ANALYST'))
    const editBtn = analystRow!.find('[data-testid="btn-edit-role"]')

    await editBtn!.trigger('click')

    const modal = wrapper.find('[data-testid="modal-edit-role"]')
    expect(modal.exists()).toBe(true)
    expect(modal.text()).toContain('Editar Rol Global')
    
    const nameInput = modal.find('[data-testid="input-edit-role-name"]')
    expect((nameInput.element as HTMLInputElement).value).toBe('ROLE_ANALYST')
  })
})
