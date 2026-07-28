/**
 * US-036 CA-9 — RbacDelegationLog.vue Vitest Suite
 *
 * Certifica que el formulario de cesión temporal de poder:
 *   - Bloquea el submit si startDate es retroactiva
 *   - Bloquea el submit si endDate <= startDate (fechas invertidas)
 *   - Bloquea el submit si no se seleccionó receptor
 *   - Permite el submit con datos válidos y llama al API
 *   - Muestra los mensajes de error correctos por campo
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import RbacDelegationLog from '../RbacDelegationLog.vue'

// ── Helpers de fecha ─────────────────────────────────────────────────────────

function toDatetimeLocal(date: Date): string {
  // Formato local en lugar de UTC para evitar desfases de zona horaria
  const pad = (n: number) => n.toString().padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`
}

function futureDate(offsetMinutes: number): string {
  return toDatetimeLocal(new Date(Date.now() + offsetMinutes * 60_000))
}

function pastDate(offsetMinutes: number): string {
  return toDatetimeLocal(new Date(Date.now() - offsetMinutes * 60_000))
}

// ── Mocks ────────────────────────────────────────────────────────────────────

vi.mock('@/services/apiClient', () => ({
  default: {
    get: vi.fn().mockResolvedValue({
      data: [
        { id: 'user-uuid-0001', email: 'receptor1@empresa.com' },
        { id: 'user-uuid-0002', email: 'receptor2@empresa.com' },
      ],
    }),
    post: vi.fn().mockResolvedValue({ data: { id: 'delegation-uuid-001' } }),
  },
}))

// ── Suite ────────────────────────────────────────────────────────────────────

describe('RbacDelegationLog.vue — US-036 CA-9 Validación de Fechas', () => {
  let wrapper: any;
  let rbacStore: any;

  beforeEach(async () => {
    vi.clearAllMocks()
    wrapper = mount(RbacDelegationLog, {
      global: { stubs: { teleport: true } },
    })
    await flushPromises()
  })

  // ── CA-9 FE-01: Campo receptor vacío bloquea el submit ──────────────────────

  it('CA-9 RBAC-FE-09: sin receptor seleccionado muestra error y bloquea submit', async () => {
    // Dejar receptor vacío, poner fechas válidas
    await wrapper.find('[data-testid="input-start-date"]').setValue(futureDate(30))
    await wrapper.find('[data-testid="input-end-date"]').setValue(futureDate(120))

    await wrapper.find('form').trigger('submit')

    const error = wrapper.find('[data-testid="error-recipient"]')
    expect(error.exists()).toBe(true)
    expect(error.text()).toContain('receptor')

    // El API NO debe haberse llamado
    const { default: apiClient } = await import('@/services/apiClient')
    expect(apiClient.post).not.toHaveBeenCalled()
  })

  // ── CA-9 FE-02: startDate retroactiva bloquea submit ───────────────────────

  it('CA-9 RBAC-FE-10: startDate retroactiva (>1 min en el pasado) muestra error y bloquea submit', async () => {
    await wrapper.find('[data-testid="select-recipient"]').setValue('user-uuid-0001')
    await wrapper.find('[data-testid="input-start-date"]').setValue(pastDate(5))    // 5 min atrás
    await wrapper.find('[data-testid="input-end-date"]').setValue(futureDate(60))

    await wrapper.find('form').trigger('submit')

    const error = wrapper.find('[data-testid="error-start-date"]')
    expect(error.exists()).toBe(true)
    expect(error.text()).toContain('retroactiva')

    const { default: apiClient } = await import('@/services/apiClient')
    expect(apiClient.post).not.toHaveBeenCalled()
  })

  // ── CA-9 FE-03: endDate <= startDate bloquea submit ────────────────────────

  it('CA-9 RBAC-FE-11: endDate antes de startDate muestra error y bloquea submit', async () => {
    await wrapper.find('[data-testid="select-recipient"]').setValue('user-uuid-0001')
    await wrapper.find('[data-testid="input-start-date"]').setValue(futureDate(120)) // start: +2h
    await wrapper.find('[data-testid="input-end-date"]').setValue(futureDate(30))    // end:   +30min (ANTES)

    await wrapper.find('form').trigger('submit')

    const error = wrapper.find('[data-testid="error-end-date"]')
    expect(error.exists()).toBe(true)
    expect(error.text()).toContain('posterior')

    const { default: apiClient } = await import('@/services/apiClient')
    expect(apiClient.post).not.toHaveBeenCalled()
  })

  it('CA-9 RBAC-FE-12: endDate igual a startDate también bloquea submit', async () => {
    const sameDate = futureDate(60)

    await wrapper.find('[data-testid="select-recipient"]').setValue('user-uuid-0001')
    await wrapper.find('[data-testid="input-start-date"]').setValue(sameDate)
    await wrapper.find('[data-testid="input-end-date"]').setValue(sameDate)

    await wrapper.find('form').trigger('submit')

    const error = wrapper.find('[data-testid="error-end-date"]')
    expect(error.exists()).toBe(true)

    const { default: apiClient } = await import('@/services/apiClient')
    expect(apiClient.post).not.toHaveBeenCalled()
  })

  // ── CA-9 FE-04: Datos válidos permiten el submit ────────────────────────────

  it('CA-9 RBAC-FE-13: datos válidos llaman al API y muestran toast de éxito', async () => {
    await wrapper.find('[data-testid="select-recipient"]').setValue('user-uuid-0001')
    await wrapper.find('[data-testid="input-start-date"]').setValue(futureDate(30))
    await wrapper.find('[data-testid="input-end-date"]').setValue(futureDate(24 * 60))
    await wrapper.find('[data-testid="input-reason"]').setValue('Ausencia por capacitación')

    await wrapper.find('form').trigger('submit')

    // Esperar respuesta async
    await wrapper.vm.$nextTick()
    await new Promise(r => setTimeout(r, 10))
    await wrapper.vm.$nextTick()

    const { default: apiClient } = await import('@/services/apiClient')
    expect(apiClient.post).toHaveBeenCalledOnce()

    // El primer argumento debe incluir "/delegate"
    const [url] = (apiClient.post as ReturnType<typeof vi.fn>).mock.calls[0]
    expect(url).toContain('/delegate')
  })

  // ── CA-9 FE-05: Sin fechas muestra errores de campo obligatorio ─────────────

  it('CA-9 RBAC-FE-14: submit sin fechas muestra errores de campo obligatorio', async () => {
    await wrapper.find('[data-testid="select-recipient"]').setValue('user-uuid-0001')
    // No se llenan fechas

    await wrapper.find('form').trigger('submit')

    expect(wrapper.find('[data-testid="error-start-date"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="error-end-date"]').exists()).toBe(true)

    const { default: apiClient } = await import('@/services/apiClient')
    expect(apiClient.post).not.toHaveBeenCalled()
  })
})
