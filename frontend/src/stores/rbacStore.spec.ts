import { setActivePinia, createPinia } from 'pinia'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { useRbacStore } from './rbacStore'
import apiClient from '@/services/apiClient'

vi.mock('@/services/apiClient', () => ({
    default: {
        get: vi.fn(),
        post: vi.fn(),
        put: vi.fn(),
        delete: vi.fn()
    }
}))

describe('rbacStore', () => {
    beforeEach(() => {
        setActivePinia(createPinia())
        vi.clearAllMocks()
    })

    it('should revoke user session (CA-14)', async () => {
        const store = useRbacStore()
        const userId = 'user-123'
        
        await store.revokeUserSession(userId)
        
        expect(apiClient.post).toHaveBeenCalledWith(`/admin/security/users/${userId}/revoke-session`)
    })

    it('should toggle process public status (CA-15)', async () => {
        const store = useRbacStore()
        const processId = 'proc-abc'
        
        vi.mocked(apiClient.get).mockResolvedValue({ data: [] })
        
        await store.toggleProcessPublicStatus(processId, true)
        
        expect(apiClient.put).toHaveBeenCalledWith(`/design/processes/${processId}/public`, { isPublic: true })
        expect(apiClient.get).toHaveBeenCalledWith('/design/processes')
    })

    it('should generate CISO report (CA-16)', async () => {
        const store = useRbacStore()
        
        // Mock blob response
        const mockBlob = new Blob(['csv,data'], { type: 'text/csv' })
        vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockBlob })
        vi.mocked(apiClient.get).mockResolvedValueOnce({ data: [] }) // for fetchCisoReports
        
        // Mock window.URL.createObjectURL
        window.URL.createObjectURL = vi.fn().mockReturnValue('blob:url')
        
        // Mock document.createElement for download link
        const mockLink = {
            href: '',
            setAttribute: vi.fn(),
            click: vi.fn(),
            remove: vi.fn()
        }
        document.body.appendChild = vi.fn()
        document.createElement = vi.fn().mockReturnValue(mockLink)

        await store.generateCisoReport()
        
        expect(apiClient.get).toHaveBeenCalledWith('/admin/security/reports/iso27001', { responseType: 'blob' })
        expect(document.createElement).toHaveBeenCalledWith('a')
        expect(mockLink.click).toHaveBeenCalled()
    })

    it('should fetch audit logs (CA-17)', async () => {
        const store = useRbacStore()
        const mockLogs = [{ id: 1, action: 'TEST_ACTION', adminId: 'admin1', timestamp: '2026-05-06T10:00:00Z' }]
        vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockLogs })

        await store.fetchAuditLogs()

        expect(apiClient.get).toHaveBeenCalledWith('/admin/security/audit-logs')
        expect(store.auditLogs).toEqual(mockLogs)
    })

    it('should create service account and return secret (CA-22)', async () => {
        const store = useRbacStore()
        const payload = { appName: 'SAP', roleId: 'ADMIN', expirationDate: '2027-01-01' }
        const mockRes = { clientId: 'cli-123', secretKey: 'sk-secret' }
        vi.mocked(apiClient.post).mockResolvedValueOnce({ data: mockRes })

        const result = await store.createServiceAccount(payload)

        expect(apiClient.post).toHaveBeenCalledWith('/admin/security/m2m', payload)
        expect(result).toEqual(mockRes)
    })
})
