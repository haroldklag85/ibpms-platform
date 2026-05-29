// @Traceability: US-003 - CA-27
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useFormDesignerStore } from '@/stores/useFormDesignerStore'
import apiClient from '@/services/apiClient'

vi.mock('@/services/apiClient', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn()
  }
}))

describe('CA-27: Version Control for Form Designs (Unit Tests)', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('1. fetchVersions correctly queries the backend API and populates formVersions', async () => {
    const store = useFormDesignerStore()
    const mockVersions = [
      { id: 'v2', version: 2, schema: '[{"id":"field1","type":"text"}]', updatedAt: '2026-05-27T00:00:00.000Z' },
      { id: 'v1', version: 1, schema: '[]', updatedAt: '2026-05-26T00:00:00.000Z' }
    ]
    
    vi.mocked(apiClient.get).mockResolvedValue({ data: mockVersions })

    await store.fetchVersions()

    expect(apiClient.get).toHaveBeenCalledWith(expect.stringContaining('/versions'))
    expect(store.formVersions).toEqual(mockVersions)
  })

  it('2. restoreVersion restores the schema from the version object into canvasFields', () => {
    const store = useFormDesignerStore()
    const versionObj = {
      id: 'v2',
      version: 2,
      schema: '[{"id":"field1","type":"text"}]',
      updatedAt: '2026-05-27T00:00:00.000Z'
    }

    const result = store.restoreVersion(versionObj)

    expect(result.success).toBe(true)
    expect(store.canvasFields).toEqual([{ id: 'field1', type: 'text' }])
  })

  it('3. Saving an active form triggers the creation of a new version (N+1) instead of overwriting', async () => {
    const store = useFormDesignerStore()
    
    // Set up initial active form design in state
    store.formTitle = 'Solicitud Onboarding'
    store.formPattern = 'SIMPLE'
    store.currentSchemaVersion = 1
    store.canvasFields = [{ id: 'field1', type: 'text' }]

    // Mock API response simulating the backend creating an N+1 version (version 2)
    const mockCreatedVersion = {
      id: 'new-uuid-v2',
      name: 'Solicitud Onboarding',
      technicalName: 'SOLICITUD_ONBOARDING',
      pattern: 'SIMPLE',
      version: 2,
      status: 'ACTIVE',
      formFields: [{ id: 'field1', type: 'text' }]
    }

    vi.mocked(apiClient.post).mockResolvedValue({ data: mockCreatedVersion })

    // Trigger saveForm (this should call the new version API route and increment version in state)
    // @ts-ignore - saveForm will be added to the store during implementation
    const result = await store.saveForm('original-uuid-v1')

    expect(result.success).toBe(true)
    // Verify apiClient.post was called with the update/create version endpoint
    expect(apiClient.post).toHaveBeenCalledWith('/forms/original-uuid-v1', expect.objectContaining({
      name: 'Solicitud Onboarding',
      pattern: 'SIMPLE',
      formFields: store.canvasFields
    }))
    
    // Verify frontend store state was updated to reflect the new N+1 version
    expect(store.currentSchemaVersion).toBe(2)
  })
})
