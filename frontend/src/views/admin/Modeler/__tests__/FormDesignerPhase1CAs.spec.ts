// @Traceability: US-003 - CA-88, CA-90, CA-91, CA-92, CA-93
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import fs from 'fs'
import path from 'path'

// Mock apiClient before imports
vi.mock('@/services/apiClient', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}))

// Mock lodash-es debounce to execute synchronously in tests
vi.mock('lodash-es', async (importOriginal) => {
  const original = await importOriginal<any>()
  return {
    ...original,
    debounce: (fn: any) => {
      const mockFn = (...args: any[]) => fn(...args)
      mockFn.cancel = vi.fn()
      mockFn.flush = vi.fn()
      return mockFn
    }
  }
})

// Mock draftCrypto to avoid window.crypto.subtle DOM exceptions in jsdom
vi.mock('@/utils/draftCrypto', () => ({
  encryptDraft: vi.fn((data) => Promise.resolve(data)),
  decryptDraft: vi.fn((data) => Promise.resolve(data))
}))

// Import stores and services to be tested
import apiClient from '@/services/apiClient'
import { useFormDesignerStore } from '@/stores/useFormDesignerStore'
import { useGenericFormStore } from '@/stores/genericFormStore'
import { LocalStorageGarbageCollector } from '@/services/LocalStorageGarbageCollector'
import FormReadOnlyView from '@/components/workdesk/FormReadOnlyView.vue'

describe('US-003 Phase 1 Pending Acceptance Criteria Tests', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    localStorage.clear()
  })

  // =========================================================================
  // CA-88: Separación Arquitectónica de Contextos IDE vs Workdesk
  // =========================================================================
  describe('CA-88: Separación Arquitectónica de Contextos IDE vs Workdesk', () => {
    // @Traceability: US-003 - CA-88
    const idePath = path.resolve(__dirname, '../../../../composables/ide')
    const workdeskPath = path.resolve(__dirname, '../../../../composables/workdesk')

    const getFiles = (dir: string): string[] => {
      if (!fs.existsSync(dir)) return []
      const subdirs = fs.readdirSync(dir)
      const files = subdirs.map((subdir) => {
        const res = path.resolve(dir, subdir)
        return fs.statSync(res).isDirectory() ? getFiles(res) : res
      })
      return files.flat()
    }

    it('should verify programmatically that ide composables do not import from workdesk composables', () => {
      const ideFiles = getFiles(idePath)
      expect(ideFiles.length).toBeGreaterThan(0)

      ideFiles.forEach(file => {
        const content = fs.readFileSync(file, 'utf8')
        // Checks imports containing "composables/workdesk"
        const workdeskImportRegex = /from\s+['"][^'"]*composables\/workdesk[^'"]*['"]/g
        const hasWorkdeskImport = workdeskImportRegex.test(content)
        expect(hasWorkdeskImport).toBe(false)
      })
    })

    it('should verify programmatically that workdesk composables do not import from ide composables', () => {
      const workdeskFiles = getFiles(workdeskPath)
      expect(workdeskFiles.length).toBeGreaterThan(0)

      workdeskFiles.forEach(file => {
        const content = fs.readFileSync(file, 'utf8')
        // Checks imports containing "composables/ide"
        const ideImportRegex = /from\s+['"][^'"]*composables\/ide[^'"]*['"]/g
        const hasIdeImport = ideImportRegex.test(content)
        expect(hasIdeImport).toBe(false)
      })
    })
  })

  // =========================================================================
  // CA-90: Límites de Rendimiento y Lazy Mount para iForm Maestro
  // =========================================================================
  describe('CA-90: Límites de Rendimiento y Lazy Mount para iForm Maestro', () => {
    // @Traceability: US-003 - CA-90
    it('should calculate isHighDensityForm as true when number of fields exceeds MAX_FORM_FIELDS (200)', () => {
      const store = useFormDesignerStore()

      // Should have MAX_FORM_FIELDS set to 200
      expect((store as any).MAX_FORM_FIELDS).toBe(200)

      // Initially, with 0 fields, it should be false
      expect((store as any).isHighDensityForm).toBe(false)

      // Populate canvasFields with 201 fields
      const fields = []
      for (let i = 0; i < 201; i++) {
        fields.push({
          id: `FIELD_${i}`,
          type: 'text',
          label: `Field ${i}`,
          camundaVariable: `field_${i}`,
          required: false
        })
      }
      store.canvasFields = fields

      // Should now compute to true
      expect((store as any).isHighDensityForm).toBe(true)
    })

    it('should calculate isHighDensityForm as false when number of fields does not exceed MAX_FORM_FIELDS (200)', () => {
      const store = useFormDesignerStore()

      // Populate canvasFields with 199 fields
      const fields = []
      for (let i = 0; i < 199; i++) {
        fields.push({
          id: `FIELD_${i}`,
          type: 'text',
          label: `Field ${i}`,
          camundaVariable: `field_${i}`,
          required: false
        })
      }
      store.canvasFields = fields

      // Should be false
      expect((store as any).isHighDensityForm).toBe(false)
    })
  })

  // =========================================================================
  // CA-91: Validación de Contrato de Integración con US-029
  // =========================================================================
  describe('CA-91: Validación de Contrato de Integración con US-029', () => {
    // @Traceability: US-003 - CA-91
    beforeEach(() => {
      vi.useFakeTimers()
    })

    it('should call apiClient.put with /drafts/{taskId} on auto-save draft', async () => {
      vi.mocked(apiClient.put).mockResolvedValue({ data: {} })
      
      const store = useGenericFormStore()
      store.taskId = 'task-456'
      store.observations = 'Testing auto-save observations'
      store.result = 'APPROVED'

      // Trigger the watch and run debounced autoSaveDraft
      await nextTick()
      vi.advanceTimersByTime(10000)
      
      // Flush microtasks for the async autoSaveDraft method (encryptDraft + apiClient.put)
      await Promise.resolve()
      await Promise.resolve()
      await Promise.resolve()

      expect(apiClient.put).toHaveBeenCalledWith('/drafts/task-456', expect.objectContaining({
        observations: 'Testing auto-save observations',
        result: 'APPROVED'
      }))
    })

    it('should call apiClient.get with /drafts/{taskId} on loading draft', async () => {
      const mockDraft = {
        observations: 'Previously saved draft observations',
        result: 'RETURNED'
      }

      vi.mocked(apiClient.get).mockImplementation((url) => {
        if (url.includes('/generic-form-context')) {
          return Promise.resolve({ data: { prefillData: {}, allowedResults: [] } })
        }
        if (url.includes('/drafts/')) {
          return Promise.resolve({ data: mockDraft })
        }
        return Promise.resolve({ data: {} })
      })

      const store = useGenericFormStore()
      await store.init('task-456')

      expect(apiClient.get).toHaveBeenCalledWith('/drafts/task-456')
      expect(store.pendingDraft).toEqual(mockDraft)
    })

    it('should call apiClient.post with complete path and apiClient.delete on submit success', async () => {
      vi.mocked(apiClient.post).mockResolvedValue({ data: {} })
      vi.mocked(apiClient.delete).mockResolvedValue({ data: {} })

      const store = useGenericFormStore()
      store.taskId = 'task-456'
      store.observations = 'Observations to complete'
      store.result = 'APPROVED'

      const success = await store.submitForm()

      expect(success).toBe(true)
      expect(apiClient.post).toHaveBeenCalledWith('/workbox/tasks/task-456/generic-form-complete', expect.objectContaining({
        observations: 'Observations to complete',
        result: 'APPROVED'
      }))
      expect(apiClient.delete).toHaveBeenCalledWith('/drafts/task-456')
    })
  })

  // =========================================================================
  // CA-92: Política de Expiración y Limpieza de LocalStorage
  // =========================================================================
  describe('CA-92: Política de Expiración y Limpieza de LocalStorage', () => {
    // @Traceability: US-003 - CA-92
    it('should purge entries older than 7 days and ignore entries from other contexts', () => {
      const now = Date.now()
      const eightDaysAgo = now - 8 * 24 * 60 * 60 * 1000
      const sixDaysAgo = now - 6 * 24 * 60 * 60 * 1000

      localStorage.setItem('ibpms_draft_expired', JSON.stringify({ updatedAt: new Date(eightDaysAgo).toISOString(), data: 'expired draft' }))
      localStorage.setItem('ibpms_snapshot_expired', JSON.stringify({ updatedAt: new Date(eightDaysAgo).toISOString(), data: 'expired snapshot' }))
      localStorage.setItem('ibpms_draft_valid', JSON.stringify({ updatedAt: new Date(sixDaysAgo).toISOString(), data: 'valid draft' }))
      localStorage.setItem('other_expired_key', JSON.stringify({ updatedAt: new Date(eightDaysAgo).toISOString(), data: 'unrelated' }))

      // Execute garbage collection
      LocalStorageGarbageCollector.run()

      expect(localStorage.getItem('ibpms_draft_expired')).toBeNull()
      expect(localStorage.getItem('ibpms_snapshot_expired')).toBeNull()
      expect(localStorage.getItem('ibpms_draft_valid')).not.toBeNull()
      expect(localStorage.getItem('other_expired_key')).not.toBeNull()
    })

    it('should respect the max quota limit and purge oldest entries matching prefixes in FIFO order', () => {
      // Configure collector with a low test quota
      (LocalStorageGarbageCollector as any).MAX_STORAGE_BYTES = 350

      const now = Date.now()
      const entry1 = JSON.stringify({ updatedAt: new Date(now - 3000).toISOString(), data: 'small1' })
      const entry2 = JSON.stringify({ updatedAt: new Date(now - 2000).toISOString(), data: 'small2' })
      const entry3 = JSON.stringify({ updatedAt: new Date(now - 1000).toISOString(), data: 'small3' })

      localStorage.setItem('ibpms_draft_fifo1', entry1)
      localStorage.setItem('ibpms_draft_fifo2', entry2)
      localStorage.setItem('ibpms_draft_fifo3', entry3)

      // Execute garbage collection
      LocalStorageGarbageCollector.run()

      // Oldest draft (fifo1) should be deleted first to stay under 150 bytes limit
      expect(localStorage.getItem('ibpms_draft_fifo1')).toBeNull()
      expect(localStorage.getItem('ibpms_draft_fifo2')).not.toBeNull()
      expect(localStorage.getItem('ibpms_draft_fifo3')).not.toBeNull()
    })
  })

  // =========================================================================
  // CA-93: Componente Unificado de Vista Solo-Lectura
  // =========================================================================
  describe('CA-93: Componente Unificado de Vista Solo-Lectura', () => {
    // @Traceability: US-003 - CA-93
    it('should render audit header and metadata when mode="audit"', () => {
      const metadata = {
        user: 'Ing. Harolt Gómez',
        traceId: 'trace-1234',
        timestamp: Date.now()
      }

      const wrapper = mount(FormReadOnlyView, {
        props: {
          mode: 'audit',
          metadata,
          schema: [],
          formData: {}
        }
      })

      // Verify header with class .audit-mode
      const auditHeader = wrapper.find('.audit-mode')
      expect(auditHeader.exists()).toBe(true)

      // Verify metadata rendering
      const htmlContent = wrapper.html()
      expect(htmlContent).toContain('Ing. Harolt Gómez')
      expect(htmlContent).toContain('trace-1234')
    })

    it('should render report title and print button when mode="print"', () => {
      const wrapper = mount(FormReadOnlyView, {
        props: {
          mode: 'print',
          schema: [],
          formData: {}
        }
      })

      // Verify container with class .print-mode
      const printContainer = wrapper.find('.print-mode')
      expect(printContainer.exists()).toBe(true)

      // Verify print button exists
      const printBtn = wrapper.find('button')
      expect(printBtn.exists()).toBe(true)
      expect(printBtn.text()).toContain('Imprimir')
    })
  })
})
