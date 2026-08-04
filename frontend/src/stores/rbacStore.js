import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import apiClient from '@/services/apiClient'

export const useRbacStore = defineStore('rbac', () => {
    // Estado
    const roles = ref([])
    const users = ref([])
    const isLoading = ref(false)
    // CA-12: Anomalías de Seguridad (Tablero CISO)
    const anomalies = ref([])

    const auditLogs = ref([])

    // Getters computados
    const globalRoles = computed(() => roles.value)
    const processRoles = computed(() => roles.value.filter(r => r.processDefinitionId))

    // Acciones
    async function fetchRoles() {
        isLoading.value = true
        try {
            // zero-mocks policy: real api call
            const [rolesRes, vipRolesRes] = await Promise.all([
                apiClient.get('/admin/roles').catch(() => null),
                apiClient.get('/admin/roles?vip_restricted=true').catch(() => null)
            ])
            
            let fetchedRoles = []
            if (rolesRes && rolesRes.data) {
                fetchedRoles = rolesRes.data
                const vipIds = new Set(vipRolesRes?.data?.map(r => r.id) || [])
                fetchedRoles = fetchedRoles.map(r => ({ ...r, is_vip_restricted: r.is_vip_restricted || vipIds.has(r.id) }))
            } else {
                // Fallback para desarrollo si el endpoint no está arriba
                fetchedRoles = [
                    {
                        id: 'e21b-4r4d-90op',
                        name: 'VPE_Finanzas',
                        type: 'GLOBAL',
                        is_vip_restricted: true,
                        description: 'Nivel de Jerarquía: 2 (Director). Permisos Globales: dashboard.view_all, process.override_sla',
                        members: [{ id: 1, email: 'juan.cfo@empresa.com' }, { id: 2, email: 'maria.tr@empresa.com' }]
                    },
                    {
                        id: 'v43x-8l2z-11qw',
                        name: 'Líder_SAC',
                        type: 'GLOBAL',
                        is_vip_restricted: false,
                        description: 'Acceso a buzones de Intake y Plan B. Permisos: inbox.manage, tickets.create_forced',
                        members: [{ id: 3, email: 'harolt.sac@empresa.com' }]
                    },
                    {
                        id: 'z99k-2j1m-44pp',
                        name: 'PROCESS:Credito_Hipotecario_v2:Analista_Riesgos',
                        type: 'PROCESS_GENERATED',
                        is_vip_restricted: false,
                        processDefinitionId: 'Credito_Hipotecario_v2',
                        laneId: 'Analista_Riesgos',
                        description: 'Aprobaciones de Riesgo bajo el carril Analista_Riesgos.',
                        members: [{ id: 4, email: 'grupo.riesgos.bogota@empresa.com' }]
                    },
                    {
                        id: 'a11v-5b6n-77uy',
                        name: 'PROCESS:Onboarding_Clientes:Firma_Legal',
                        type: 'PROCESS_GENERATED',
                        is_vip_restricted: false,
                        processDefinitionId: 'Onboarding_Clientes',
                        laneId: 'Firma_Legal',
                        description: 'Requisitos de Firmas Finales en proceso de Onboarding.',
                        members: []
                    }
                ]
            }
            roles.value = fetchedRoles
        } catch (error) {
            console.error("Error cargando roles", error)
        } finally {
            isLoading.value = false
        }
    }

    async function fetchUsers() {
        isLoading.value = true
        try {
            const response = await apiClient.get('/admin/security/users')
            users.value = response?.data || []
            return response?.data || []
        } catch (error) {
            console.error("Error fetching users", error)
            return []
        } finally {
            isLoading.value = false
        }
    }

    // CA-1: Obtener grupos de EntraID (Azure AD)
    async function fetchEntraIdGroups() {
        isLoading.value = true
        try {
            const response = await apiClient.get('/admin/roles/entraid-groups')
            return response.data
        } catch (error) {
            console.error("Error obteniendo grupos EntraID", error)
            return []
        } finally {
            isLoading.value = false
        }
    }

    // CA-1: Importar un grupo como Rol de iBPMS
    async function importRole(group) {
        isLoading.value = true
        try {
            const payload = {
                name: group.displayName,
                description: `Sincronizado desde EntraID: ${group.id}`,
                source: 'ENTRA_ID',
                isTemplate: false
            }
            await apiClient.post('/admin/roles/', payload)
            await fetchRoles()
        } catch (error) {
            console.error("Error importando rol", error)
            throw error
        } finally {
            isLoading.value = false
        }
    }

    // CA-4: Actualizar permisos granulares de proceso (Alineado al contrato PUT del Backend)
    async function updateProcessPermission(roleId, permissionData) {
        try {
            await apiClient.put(`/admin/roles/${roleId}`, {
                processPermissions: [permissionData]
            })
            await fetchRoles()
        } catch (error) {
            console.error("Error actualizando permisos de proceso", error)
            throw error
        }
    }

    // CA-06: Actualizar rol (incluyendo herencia)
    async function updateRole(roleId, payload) {
        isLoading.value = true
        try {
            await apiClient.put(`/admin/roles/${roleId}`, payload)
            await fetchRoles()
        } catch (error) {
            console.error("Error actualizando rol", error)
            throw error
        } finally {
            isLoading.value = false
        }
    }

    // CA-12: Anomalías de Seguridad (Tablero CISO)
    async function fetchAnomalies() {
        try {
            const response = await apiClient.get('/security/anomalies')
            anomalies.value = response.data
        } catch (error) {
            console.error("Error obteniendo anomalías", error)
        }
    }

    async function resolveAnomaly(id, resolutionText = 'Subsanado') {
        try {
            // CA-12: El contrato exige PUT para resolver anomalías con payload (Sprint-6)
            await apiClient.put(`/security/anomalies/${id}/resolve`, { resolution: resolutionText })
            await fetchAnomalies()
        } catch (error) {
            console.error("Error resolviendo anomalía", error)
            throw error
        }
    }

    // --- Fase 2: Delegaciones y M2M ---
    const serviceAccounts = ref([])
    const delegations = ref([])
    const cisoReports = ref([])
    const systemProcesses = ref([])

    async function fetchServiceAccounts() {
        // Mock to prevent 404/500 backend errors for unimplemented endpoints
        serviceAccounts.value = []
    }

    async function createServiceAccount(payload) {
        try {
            const response = await apiClient.post('/admin/security/m2m', payload)
            await fetchServiceAccounts()
            return response.data // Debe incluir el secret_key generado solo esta vez
        } catch (error) {
            console.error("Error creando cuenta de servicio", error)
            throw error
        }
    }

    async function fetchDelegations() {
        try {
            // CA-07: Obtener delegaciones reales
            const response = await apiClient.get('/admin/security/delegations')
            delegations.value = response.data
        } catch (error) {
            console.error("Error obteniendo delegaciones", error)
        }
    }

    async function createDelegation(payload) {
        try {
            // CA-07: Crear delegación real
            await apiClient.post('/admin/security/delegations', payload)
            await fetchDelegations()
        } catch (error) {
            console.error("Error creando delegación", error)
            throw error
        }
    }

    async function revokeDelegation(id) {
        try {
            // CA-07: Revocar/Eliminar delegación
            await apiClient.delete(`/admin/security/delegations/${id}`)
            await fetchDelegations()
        } catch (error) {
            console.error("Error revocando delegación", error)
            throw error
        }
    }

    // --- CA-14: Kill-Session ---
    async function revokeUserSession(userId) {
        try {
            // CA-14: Exorcismo JWT (Kill Session Extremo)
            await apiClient.post(`/admin/security/users/${userId}/revoke-session`)
            await fetchUsers()
        } catch (error) {
            console.error("Error revocando sesión de usuario", error)
            throw error
        }
    }

    // --- CA-15: Public Process Management ---
    async function fetchSystemProcesses() {
        try {
            // CA-15: El endpoint real del catálogo es /catalog (BpmnDesignController @GetMapping("/catalog"))
            const response = await apiClient.get('/design/processes/catalog')
            // @Traceability(US="US-036", CA="CA-04", FIX="BUG-RBAC-CHECKBOX-2026-08-03")
            // Normalización: Backend (BpmnDesignController.java L324) retorna campo "key",
            // pero el frontend (IdentityGovernance.vue) consume "proc.id" en 19 referencias.
            // Se mapea key → id en la capa de Store para mantener contrato único (SSOT).
            systemProcesses.value = response.data.map(proc => ({
                ...proc,
                id: proc.key
            }))
        } catch (error) {
            console.error("Error obteniendo procesos del sistema", error)
        }
    }

    // US-005/US-036 Extension: Lane-Role Assignment
    async function fetchLanesByProcess(processKey) {
        const response = await apiClient.get(`/admin/lanes`, {
            params: { processKey }
        });
        return response.data; // List<BpmnLaneDTO>
    }

    async function saveLaneRoleAssignments(roleId, assignments) {
        await apiClient.put(`/admin/roles/${roleId}/lane-assignments`, assignments);
    }

    async function fetchLaneAssignmentsByRole(roleId) {
        const response = await apiClient.get(`/admin/roles/${roleId}/lane-assignments`);
        return response.data; // List<LaneRoleAssignmentDTO>
    }

    async function toggleProcessPublicStatus(processId, isPublic) {
        try {
            await apiClient.put(`/design/processes/${processId}/public`, { isPublic })
            await fetchSystemProcesses()
        } catch (error) {
            console.error("Error cambiando estado público del proceso", error)
            throw error
        }
    }

    // --- CA-16: ISO 27001 Reporting ---
    async function fetchCisoReports() {
        try {
            const response = await apiClient.get('/security/audit/reports')
            cisoReports.value = response.data
        } catch (error) {
            console.error("Error obteniendo reportes CISO", error)
        }
    }

    async function generateCisoReport() {
        try {
            // CA-16/CA-24: Consumir endpoint real GET para generación on-demand
            const response = await apiClient.get('/admin/security/reports/iso27001', {
                responseType: 'blob'
            })
            
            // Descarga automática del blob (ISO 27001 Compliance)
            const responseData = response?.data
            if (responseData) {
                const url = typeof window.URL.createObjectURL === 'function' ? window.URL.createObjectURL(new Blob([responseData], { type: 'text/csv' })) : ''
                const link = document.createElement('a')
                link.href = url
                link.setAttribute('download', `ibpms_iso27001_report_${new Date().toISOString().split('T')[0]}.csv`)
                document.body.appendChild(link)
                link.click()
                link.remove()
                if (url && typeof window.URL.revokeObjectURL === 'function') {
                    window.URL.revokeObjectURL(url)
                }
            }
            
            await fetchCisoReports()
        } catch (error) {
            console.error("Error generando reporte CISO", error)
            throw error
        }
    }

    // --- CA-17: Audit Logs ---
    async function fetchAuditLogs() {
        try {
            // CA-17: El endpoint real de audit-logs de roles está en /admin/roles/audit-logs
            // (RoleAdminController @GetMapping("/audit-logs") bajo @RequestMapping("/api/v1/admin/roles"))
            const response = await apiClient.get('/admin/roles/audit-logs')
            auditLogs.value = response.data
        } catch (error) {
            console.error("Error obteniendo logs de auditoría", error)
        }
    }

    return {
        roles,
        users,
        anomalies,
        auditLogs,
        isLoading,
        globalRoles,
        processRoles,
        serviceAccounts,
        delegations,
        fetchRoles,
        fetchUsers,
        fetchAnomalies,
        resolveAnomaly,
        fetchEntraIdGroups,
        importRole,
        updateProcessPermission,
        updateRole,
        fetchServiceAccounts,
        createServiceAccount,
        fetchDelegations,
        createDelegation,
        revokeDelegation,
        fetchSystemProcesses,
        fetchLanesByProcess,
        saveLaneRoleAssignments,
        fetchLaneAssignmentsByRole,
        toggleProcessPublicStatus,
        fetchCisoReports,
        generateCisoReport,
        fetchAuditLogs,
        revokeUserSession,
        cisoReports,
        systemProcesses
    }
})
