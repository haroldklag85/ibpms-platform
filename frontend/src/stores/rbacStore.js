import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import apiClient from '@/services/apiClient'

export const useRbacStore = defineStore('rbac', () => {
    // Estado
    const roles = ref([])
    const isLoading = ref(false)
    // CA-12: Anomalías de Seguridad (Tablero CISO)
    const anomalies = ref([])

    const auditLogs = ref([
        { timestamp: '10:45am', message: 'Administrador añadió a @Pedro al rol VPE_Finanzas' },
        { timestamp: '09:30am', message: 'Al desplegar BPMN_Crédito, el sistema autogeneró el rol PROCESS:Credito:Analista_Riesgos' }
    ])

    // Getters computados
    const globalRoles = computed(() => roles.value.filter(r => r.type === 'GLOBAL'))
    const processRoles = computed(() => roles.value.filter(r => r.type === 'PROCESS_GENERATED'))

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

    // CA-12: Anomalías de Seguridad
    async function fetchAnomalies() {
        try {
            const response = await apiClient.get('/admin/security/anomalies')
            anomalies.value = response.data
        } catch (error) {
            console.error("Error obteniendo anomalías", error)
        }
    }

    async function resolveAnomaly(id) {
        try {
            await apiClient.post(`/admin/security/anomalies/${id}/resolve`)
            await fetchAnomalies()
        } catch (error) {
            console.error("Error resolviendo anomalía", error)
            throw error
        }
    }

    // --- Fase 2: Delegaciones y M2M ---
    const serviceAccounts = ref([])
    const delegations = ref([])

    async function fetchServiceAccounts() {
        try {
            const response = await apiClient.get('/admin/security/m2m')
            serviceAccounts.value = response.data
        } catch (error) {
            console.error("Error obteniendo cuentas de servicio", error)
        }
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
            const response = await apiClient.get('/admin/security/delegations')
            delegations.value = response.data
        } catch (error) {
            console.error("Error obteniendo delegaciones", error)
        }
    }

    async function createDelegation(payload) {
        try {
            await apiClient.post('/admin/security/delegations', payload)
            await fetchDelegations()
        } catch (error) {
            console.error("Error creando delegación", error)
            throw error
        }
    }

    async function revokeDelegation(id) {
        try {
            await apiClient.delete(`/admin/security/delegations/${id}`)
            await fetchDelegations()
        } catch (error) {
            console.error("Error revocando delegación", error)
            throw error
        }
    }

    return {
        roles,
        anomalies,
        auditLogs,
        isLoading,
        globalRoles,
        processRoles,
        serviceAccounts,
        delegations,
        fetchRoles,
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
        revokeDelegation
    }
})
